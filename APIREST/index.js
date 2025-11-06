
// cargo las variables de entorno como contrasenas y otros datos sensibles
require('dotenv').config();

// importo las librerias necesarias para crear la api
const express = require('express');
const mysql = require('mysql2');
const cors = require('cors');
const jwt = require('jsonwebtoken');
const bcrypt = require('bcryptjs');
const authMiddleware = require('./middleware/auth');
const multer = require('multer');

// inicializo la aplicacion express
const app = express();

// configuro los middlewares para aceptar peticiones de otros origenes y procesar json
app.use(cors());
app.use(express.json());
app.use(express.urlencoded({ extended: true }));

// ----- configuracion de multer (para subir archivos) -----
const storage = multer.diskStorage({
    // le digo donde guardar los archivos
    destination: function (req, file, cb) {
        cb(null, 'uploads/'); // ¡asegurate de que la carpeta 'uploads/' exista!
    },
    // le digo que nombre de archivo poner
    filename: function (req, file, cb) {
        // creo un nombre unico para que no se pisen (ej: 'idusuario-timestamp.jpg')
        const uniqueSuffix = Date.now() + '-' + Math.round(Math.random() * 1e9);
        cb(null, req.user.id + '-' + uniqueSuffix + '-' + file.originalname);
    }
});

// creo el "middleware" de multer
const upload = multer({ storage: storage });

// configuro la conexion a la base de datos usando variables de entorno
const db = mysql.createPool({
    host: process.env.DB_HOST || 'localhost',
    user: process.env.DB_USER,
    password: process.env.DB_PASSWORD,
    database: process.env.DB_DATABASE
}).promise();

console.log("intentando conectar a la base de datos...");



// endpoint de autenticacion de usuario (login)
app.post('/api/auth/login', async (req, res) => {
    // obtengo los datos del cuerpo de la peticion
    const { Usuario, Clave } = req.body;

    // valido que usuario y clave sean obligatorios
    if (!Usuario || !Clave) {
        return res.status(400).json({ message: "usuario y clave son obligatorios" });
    }

    try {
        // busco al usuario en la base de datos por su email
        const [usuarios] = await db.query(
            'SELECT * FROM usuarios WHERE mail = ?',
            [Usuario]
        );

        // verifico si el usuario existe
        if (usuarios.length === 0) {
            // uso 401 por seguridad y no doy pistas
            return res.status(401).json({ message: "credenciales incorrectas" });
        }

        const usuario = usuarios[0];

        // verifico si el usuario esta activo
        if (usuario.estado === 0) {
             return res.status(403).json({ message: "usuario inactivo" });
        }

        // comparo la contrasena ingresada con la hasheada en la base de datos
        const passwordCoincide = await bcrypt.compare(Clave, usuario.contraseña);

        if (!passwordCoincide) {
            // doy respuesta generica por seguridad
            return res.status(401).json({ message: "credenciales incorrectas" });
        }

        // si las credenciales son correctas creo el token jwt
        const payload = {
            id: usuario.idusuarios,
            rol: usuario.rol,
            nombre: usuario.nombre
        };

        const token = jwt.sign(
            payload, 
            process.env.JWT_SECRET,
            { expiresIn: '1h' }
        );

        // envio el token al cliente
        res.json({ token: token });

    } catch (err) {
        console.error("error en el login:", err);
        res.status(500).json({ message: "error en el servidor durante el login" });
    }
});

// endpoint de registro de nuevo paciente
app.post('/api/pacientes/registro', async (req, res) => {
    // obtengo los datos del cuerpo de la peticion
    const {
        nombre,
        apellido,
        telefono,
        mail,
        password,
        dni,
        idObraSocial
    } = req.body;

    // valido los datos obligatorios
    if (!mail || !password || !nombre || !apellido || !dni) {
        return res.status(400).json({ message: "faltan datos obligatorios" });
    }

    // inicio la transaccion para el registro
    let connection;
    try {
        connection = await db.getConnection(); 
        await connection.beginTransaction(); 

        // verifico si el email o dni ya existen
        const [usuariosExistentes] = await connection.query(
            'SELECT * FROM usuarios WHERE mail = ?',
            [mail]
        );
        
        if (usuariosExistentes.length > 0) {
            await connection.rollback(); 
            return res.status(409).json({ message: "el email ya esta registrado" });
        }

        const [pacientesExistentes] = await connection.query(
            'SELECT * FROM paciente WHERE DNI = ?',
            [dni]
        );

        if (pacientesExistentes.length > 0) {
            await connection.rollback(); 
            return res.status(409).json({ message: "el dni ya esta registrado" });
        }

        // encripto la contrasena
        const salt = await bcrypt.genSalt(10);
        const hashedPassword = await bcrypt.hash(password, salt);

        // creo el registro en la tabla usuarios
        const [resultadoUsuario] = await connection.query(
            'INSERT INTO usuarios (nombre, apellido, telefono, mail, rol, contraseña, estado) VALUES (?, ?, ?, ?, ?, ?, ?)',
            [nombre, apellido, telefono, mail, 'paciente', hashedPassword, 1]
        );

        const nuevoUsuarioId = resultadoUsuario.insertId;

        // creo el registro en la tabla paciente
        await connection.query(
            'INSERT INTO paciente (Nombre, Apellido, DNI, Email, Telefono, IdObraSocial, id_usuario) VALUES (?, ?, ?, ?, ?, ?, ?)',
            [nombre, apellido, dni, mail, telefono, idObraSocial, nuevoUsuarioId]
        );

        // si todo salio bien confirmo los cambios
        await connection.commit();

        res.status(201).json({ message: "paciente registrado exitosamente", usuarioId: nuevoUsuarioId });

    } catch (err) {
        if (connection) await connection.rollback(); 
        console.error("error en el registro:", err);
        res.status(500).json({ message: "error en el servidor al registrar el paciente" });
    } finally {
        if (connection) connection.release();
    }
});

// endpoint para enlazar cuenta a paciente existente
app.post('/api/pacientes/paciente_existente', async (req, res) => {
    // obtengo los datos del cuerpo de la peticion
    const { dni, mail, password } = req.body;

    // valido los datos obligatorios
    if (!dni || !mail || !password) {
        return res.status(400).json({ message: "dni, mail y password son obligatorios" });
    }

    let connection;
    try {
        connection = await db.getConnection();
        await connection.beginTransaction();

        // busco al paciente por dni
        const [pacientes] = await connection.query(
            'SELECT * FROM paciente WHERE DNI = ?', 
            [dni]
        );

        if (pacientes.length === 0) {
            await connection.rollback();
            return res.status(404).json({ message: "no se encontro ningun paciente con ese dni" });
        }

        const paciente = pacientes[0];

        // verifico que el paciente no tenga ya una cuenta enlazada
        if (paciente.id_usuario !== null) {
            await connection.rollback();
            return res.status(409).json({ message: "este paciente ya tiene una cuenta enlazada" });
        }

        // verifico que el email no este en uso por otro usuario
        const [usuariosExistentes] = await connection.query(
            'SELECT * FROM usuarios WHERE mail = ?',
            [mail]
        );

        if (usuariosExistentes.length > 0) {
            await connection.rollback();
            return res.status(409).json({ message: "el email ya esta en uso por otra cuenta" });
        }

        // si todo esta bien creo la cuenta de usuario
        const salt = await bcrypt.genSalt(10);
        const hashedPassword = await bcrypt.hash(password, salt);

        // copio los datos del paciente existente a la nueva cuenta de usuario
        const [resultadoUsuario] = await connection.query(
            'INSERT INTO usuarios (nombre, apellido, telefono, mail, rol, contraseña, estado) VALUES (?, ?, ?, ?, ?, ?, ?)',
            [paciente.Nombre, paciente.Apellido, paciente.Telefono, mail, 'paciente', hashedPassword, 1]
        );

        const nuevoUsuarioId = resultadoUsuario.insertId;

        // enlazo la cuenta actualizando la tabla paciente
        await connection.query(
            'UPDATE paciente SET id_usuario = ?, Email = ? WHERE IdPaciente = ?',
            [nuevoUsuarioId, mail, paciente.IdPaciente]
        );

        // confirmo la transaccion
        await connection.commit();

        res.status(201).json({ message: "cuenta de usuario creada y enlazada" });

    } catch (err) {
        if (connection) await connection.rollback();
        console.error("error en link-account:", err);
        res.status(500).json({ message: "error en el servidor" });
    } finally {
        if (connection) connection.release();
    }
});

// endpoint para obtener mis turnos, requiere autenticacion
app.get('/api/turnos/mis-turnos', authMiddleware, async (req, res) => {
    // si llego aqui el token es valido y obtengo el id del usuario logueado
    const idUsuarioLogueado = req.user.id;

    try {
        // consulto los turnos del paciente correspondiente al usuario logueado
        const [turnos] = await db.query(
            `SELECT 
                t.turno_id,
                t.fecha,
                t.horario,
                t.tipoconsulta,
                t.motivo_consulta,
                t.estado,
                m.Nombre AS medico_nombre,
                m.Apellido AS medico_apellido,
                e.Nombre AS especialidad_nombre
            FROM 
                turnos AS t
            JOIN 
                paciente AS p ON t.paciente_id = p.IdPaciente
            JOIN 
                medico AS m ON t.profesional_id = m.IdMedico
            JOIN 
                especialidad AS e ON m.IdEspecialidad = e.IdEspecialidad
            WHERE 
                p.id_usuario = ?
            ORDER BY 
                t.fecha DESC, t.horario DESC`,
            [idUsuarioLogueado]
        );
        // verifico si la lista de turnos esta vacia
        if (turnos.length === 0) {
            // no es un error, solo que no tiene turnos.
            // uso status 200 (ok) porque la peticion fue exitosa
            return res.status(200).json({ message: "no hay turnos registrados para este paciente." });
        }

        // si encontramos turnos, devuelvo la lista
        res.json(turnos);

    } catch (err) {
        console.error("error en /api/turnos/mis-turnos:", err);
        res.status(500).json({ message: "error en el servidor al obtener los turnos." });
    }
});

//nuevo turno
app.post('/api/turnos/solicitar', authMiddleware, async (req, res) => {
    
    // si llego aqui, mi token es valido.
    const idUsuarioLogueado = req.user.id;

    // 1. obtengo los datos del turno desde el body
    const {
        profesional_id,
        fecha,
        horario,
        tipoconsulta,
        motivo_consulta
    } = req.body;

    // ... (validacion de datos basicos) ...
    if (!profesional_id || !fecha || !horario || !motivo_consulta) {
        return res.status(400).json({ message: "faltan datos obligatorios para el turno." });
    }

    let connection;
    try {
        connection = await db.getConnection();
        await connection.beginTransaction(); 

        // --- validacion 1: ¿el medico trabaja ese dia y a esa hora? ---
        
        // 1a. obtengo el dia de la semana
        const [diaSemanaResult] = await connection.query(
            "SELECT ELT(DAYOFWEEK(?), 'domingo', 'lunes', 'martes', 'miercoles', 'jueves', 'viernes', 'sabado') AS dia",
            [fecha]
        );
        const diaSemana = diaSemanaResult[0].dia;

        // 1b. verifico la plantilla de horarios
        const [plantillas] = await connection.query(
            'SELECT Hora_inicio, Hora_fin FROM horarios WHERE Profesional_id = ? AND Dia_semana = ? AND ? >= Hora_inicio AND ? < Hora_fin',
            [profesional_id, diaSemana, horario, horario]
        );
        
        if (plantillas.length === 0) {
            await connection.rollback();
            return res.status(409).json({ message: "el medico no atiende en el dia u horario seleccionado." });
        }

        // --- validacion 2: ¿el turno ya esta ocupado? ---
        const [turnosOcupados] = await connection.query(
            "SELECT turno_id FROM turnos WHERE profesional_id = ? AND fecha = ? AND horario = ? AND estado IN ('reservado', 'confirmado', 'atendido')",
            [profesional_id, fecha, horario]
        );

        if (turnosOcupados.length > 0) {
            await connection.rollback();
            return res.status(409).json({ message: "el turno seleccionado ya no esta disponible." });
        }

        // --- si paso las 2 validaciones, lo creo ---
        
        // 3. busco el idpaciente de mi usuario logueado
        const [pacientes] = await connection.query(
            'SELECT IdPaciente FROM paciente WHERE id_usuario = ?',
            [idUsuarioLogueado]
        );
        const pacienteId = pacientes[0].IdPaciente;

        // 4. inserto el nuevo turno
        const [resultadoInsert] = await connection.query(
            'INSERT INTO turnos (paciente_id, profesional_id, fecha, horario, tipoconsulta, motivo_consulta, estado, creadoPor) VALUES (?, ?, ?, ?, ?, ?, ?, ?)',
            [
                pacienteId,
                profesional_id,
                fecha,
                horario,
                tipoconsulta || 'NORMAL',
                motivo_consulta,
                'reservado', // estado inicial
                idUsuarioLogueado
            ]
        );

        // 5. confirmo la transaccion
        await connection.commit();

        res.status(201).json({ message: "turno registrado exitosamente", turnoId: resultadoInsert.insertId });

    } catch (err) {
        if (connection) await connection.rollback();
        console.error("error en /api/turnos/solicitar:", err);
        res.status(500).json({ message: "error en el servidor al solicitar el turno." });
    } finally {
        if (connection) connection.release();
    }
});

// obtener turnos disponibles para un medico en una fecha
app.get('/api/turnos/disponibles/:idmedico', async (req, res) => {
    
    const { idmedico } = req.params;
    const { fecha } = req.query; // ej: "2025-11-10"

    if (!fecha) {
        return res.status(400).json({ message: "la fecha es obligatoria." });
    }

    let connection;
    try {
        connection = await db.getConnection();
        
        // 1. averiguo el dia de la semana (ej: 'lunes') usando la fecha
        // elt() es una funcion de mysql que elige un string de una lista
        // elt(2, 'domingo', 'lunes') devuelve 'lunes'
        const [diaSemanaResult] = await connection.query(
            "SELECT ELT(DAYOFWEEK(?), 'domingo', 'lunes', 'martes', 'miercoles', 'jueves', 'viernes', 'sabado') AS dia",
            [fecha]
        );
        const diaSemana = diaSemanaResult[0].dia;

        if (!diaSemana) {
             return res.status(400).json({ message: "fecha invalida." });
        }

        // 2. busco la plantilla de horario para ese medico ese dia
        const [plantillas] = await connection.query(
            'SELECT Hora_inicio, Hora_fin, Intervalo FROM horarios WHERE Profesional_id = ? AND Dia_semana = ?',
            [idmedico, diaSemana]
        );

        if (plantillas.length === 0) {
            // el medico no trabaja ese dia
            return res.json([]); // devuelvo un array vacio
        }
        
        const plantilla = plantillas[0]; // ej: 09:00, 17:00, 30 min

        // 3. busco los turnos que ya estan ocupados
        const [turnosOcupados] = await connection.query(
            "SELECT horario FROM turnos WHERE profesional_id = ? AND fecha = ? AND estado IN ('reservado', 'confirmado', 'atendido')",
            [idmedico, fecha]
        );
        
        // creo un "set" para buscar rapido, ej: {'09:30:00', '11:00:00'}
        const horariosOcupados = new Set(turnosOcupados.map(t => t.horario));

        // 4. ¡la magia! genero los horarios en javascript
        const horariosDisponibles = [];
        
        // convierto las horas a objetos 'date' para poder sumar minutos
        const [hInicio, mInicio] = plantilla.Hora_inicio.split(':');
        const [hFin, mFin] = plantilla.Hora_fin.split(':');
        
        let horaActual = new Date(fecha);
        horaActual.setHours(hInicio, mInicio, 0, 0); // ej: 2025-11-10t09:00:00
        
        let horaTope = new Date(fecha);
        horaTope.setHours(hFin, mFin, 0, 0); // ej: 2025-11-10t17:00:00

        // loop mientras la hora actual sea menor que la hora fin
        while (horaActual < horaTope) {
            
            // formateo la hora a "hh:mm:ss"
            const horaString = horaActual.toTimeString().split(' ')[0]; // ej: "09:00:00"

            // si este horario no esta en el set de ocupados, lo agrego
            if (!horariosOcupados.has(horaString)) {
                horariosDisponibles.push(horaString);
            }
            
            // le sumo el intervalo a la hora actual
            horaActual.setMinutes(horaActual.getMinutes() + plantilla.Intervalo);
        }

        // devuelvo la lista de strings de horarios
        res.json(horariosDisponibles);

    } catch (err) {
        console.error("error en /api/turnos/disponibles:", err);
        res.status(500).json({ message: "error en el servidor." });
    } finally {
        if (connection) connection.release();
    }
});

// cancelar un turno 

app.put('/api/turnos/cancelar/:idturno', authMiddleware, async (req, res) => {
    
    // si llego aqui, mi token es valido.
    const idUsuarioLogueado = req.user.id;
    
    // 1. obtengo el id del turno de la url
    const { idturno } = req.params;

    let connection;
    try {
        connection = await db.getConnection();
        await connection.beginTransaction(); 

        // 2. necesito encontrar el 'idpaciente' de mi usuario logueado
        const [pacientes] = await connection.query(
            'SELECT IdPaciente FROM paciente WHERE id_usuario = ?',
            [idUsuarioLogueado]
        );
        
        if (pacientes.length === 0) {
            await connection.rollback();
            return res.status(404).json({ message: "perfil de paciente no encontrado." });
        }
        
        const pacienteId = pacientes[0].IdPaciente;

        // 3. ¡¡la consulta de seguridad!!
        // solo cambio el estado. no borro el paciente_id.
        // ¡¡esto cumple tu requisito 1 (guardar historial)!!
        const [resultadoUpdate] = await connection.query(
            `UPDATE turnos SET 
                estado = 'cancelado' 
            WHERE 
                turno_id = ? AND 
                paciente_id = ? AND
                estado IN ('reservado', 'confirmado')`, // solo puedo cancelar turnos pendientes
            [
                idturno,
                pacienteId 
            ]
        );

        // 4. verifico si la actualizacion funciono
        if (resultadoUpdate.affectedRows === 0) {
            await connection.rollback();
            return res.status(404).json({ message: "no se pudo cancelar el turno. puede que no exista, no le pertenezca o ya este atendido." });
        }

        // 5. si llego aqui, ¡el turno se cancelo!
        await connection.commit();

        res.status(200).json({ message: "turno cancelado exitosamente" });

    } catch (err) {
        if (connection) await connection.rollback();
        console.error("error en /api/turnos/cancelar:", err);
        res.status(500).json({ message: "error en el servidor al cancelar el turno." });
    } finally {
        if (connection) connection.release();
    }
});

//editar perfil
app.patch('/api/pacientes/mi-perfil', authMiddleware, async (req, res) => {
    
    // si llego aqui, mi token es valido
    const idUsuarioLogueado = req.user.id;
    
    // 1. obtengo los datos (opcionales) del body
    const { telefono, idObraSocial, contraseña } = req.body;

    // 2. reviso si no me enviaron nada
    if (!telefono && !idObraSocial && !contraseña) {
        return res.status(400).json({ message: "no se enviaron datos para actualizar." });
    }

    let connection;
    try {
        connection = await db.getConnection();
        await connection.beginTransaction(); 

        // 3. busco el idpaciente
        const [pacientes] = await connection.query(
            'SELECT IdPaciente FROM paciente WHERE id_usuario = ?',
            [idUsuarioLogueado]
        );
        const pacienteId = pacientes[0].IdPaciente;

        // 4. armo las actualizaciones dinamicamente
        
        // --- actualizacion en la tabla 'usuarios' ---
        let updatesUsuarios = [];
        let paramsUsuarios = [];
        
        if (telefono) {
            updatesUsuarios.push('telefono = ?');
            paramsUsuarios.push(telefono);
        }
        if (contraseña) {
            // ¡muy importante! encripto la nueva contraseña
            const salt = await bcrypt.genSalt(10);
            const hashedPassword = await bcrypt.hash(contraseña, salt);
            updatesUsuarios.push('contraseña = ?');
            paramsUsuarios.push(hashedPassword);
        }

        // --- actualizacion en la tabla 'paciente' ---
        let updatesPaciente = [];
        let paramsPaciente = [];
        
        if (telefono) {
            updatesPaciente.push('Telefono = ?');
            paramsPaciente.push(telefono);
        }
        if (idObraSocial) {
            updatesPaciente.push('IdObraSocial = ?');
            paramsPaciente.push(idObraSocial);
        }

        // 5. ejecuto las consultas solo si hay algo que actualizar
        if (updatesUsuarios.length > 0) {
            paramsUsuarios.push(idUsuarioLogueado); // añado el id para el where
            await connection.query(
                `UPDATE usuarios SET ${updatesUsuarios.join(', ')} WHERE idusuarios = ?`,
                paramsUsuarios
            );
        }
        
        if (updatesPaciente.length > 0) {
            paramsPaciente.push(pacienteId); // añado el id para el where
            await connection.query(
                `UPDATE paciente SET ${updatesPaciente.join(', ')} WHERE IdPaciente = ?`,
                paramsPaciente
            );
        }

        // 6. confirmo
        await connection.commit();

        res.status(200).json({ message: "perfil actualizado exitosamente" });

    } catch (err) {
        if (connection) await connection.rollback();
        console.error("error en patch /mi-perfil:", err);
        res.status(500).json({ message: "error en el servidor al actualizar el perfil." });
    } finally {
        if (connection) connection.release();
    }
});

//actualuzo o cargo la foto de perfil
app.post('/api/pacientes/mi-perfil/imagen', authMiddleware, upload.single('imgperfil'), async (req, res) => {
    
    // si llego aqui, el token es valido y la imagen ya se subio a la carpeta 'uploads/'
    const idUsuarioLogueado = req.user.id;
    
    // 1. verifico si multer me dio un archivo
    if (!req.file) {
        return res.status(400).json({ message: "no se envio ningun archivo de imagen." });
    }

    // 2. obtengo la ruta donde se guardo el archivo
    // ej: "uploads\10-1762428585250-424242-foto.jpg"
    // (en windows usa \, en linux /)
    const rutaImagen = req.file.path.replace(/\\/g, "/"); // normalizo a /

    try {
        // 3. guardo la ruta en la tabla 'usuarios'
        await db.query(
            'UPDATE usuarios SET imgperfil = ? WHERE idusuarios = ?',
            [rutaImagen, idUsuarioLogueado]
        );

        res.status(200).json({ 
            message: "imagen de perfil actualizada exitosamente",
            ruta: rutaImagen 
        });

    } catch (err) {
        console.error("error en post /mi-perfil/imagen:", err);
        res.status(500).json({ message: "error en el servidor al guardar la imagen." });
    }
});










// inicio el servidor en el puerto definido en las variables de entorno o el 3000 por defecto
const PORT = process.env.PORT || 3000;
app.listen(PORT, () => {
    console.log(`servidor app movil corriendo en http://localhost:${PORT}`);
});