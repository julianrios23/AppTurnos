// middleware/auth.js

const jwt = require('jsonwebtoken');

function authMiddleware(req, res, next) {
    // 1. Obtener el token de la cabecera (header)
    const authHeader = req.headers['authorization'];
    // El formato es "Bearer TOKEN_LARGO"
    const token = authHeader && authHeader.split(' ')[1]; 

    // 2. Si no hay token, no hay acceso
    if (token == null) {
        return res.status(401).json({ message: "Acceso denegado. No se proporcionó token." });
    }

    // 3. Verificar el token
    jwt.verify(token, process.env.JWT_SECRET, (err, user) => {
        if (err) {
            // El token no es válido o expiró
            return res.status(403).json({ message: "Token inválido." });
        }

        // 4. ¡Éxito! El token es válido.
        // Guardamos los datos del usuario (que venían en el token) en la request
        // para que el próximo endpoint los pueda usar.
        req.user = user;

        // 5. Dejamos pasar al usuario al endpoint que quería ver
        next();
    });
}

// Exportamos la función para que index.js pueda importarla
module.exports = authMiddleware;