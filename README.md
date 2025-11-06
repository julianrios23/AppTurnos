# 🏥 App Móvil de Gestión de Turnos Médicos

[cite_start]Este proyecto propone el desarrollo de una **aplicación móvil nativa para Android** [cite: 26, 83, 145] [cite_start]destinada a la gestión de turnos médicos en una clínica con múltiples profesionales de la salud[cite: 26].

[cite_start]Su objetivo principal es facilitar a los **pacientes** [cite: 27, 54] [cite_start]la solicitud, consulta, modificación y cancelación de sus turnos desde su teléfono celular [cite: 27, 46][cite_start], optimizando la comunicación y reduciendo los errores administrativos[cite: 48].

[cite_start]El diseño de la aplicación está pensado para ser **simple, claro y accesible** [cite: 29, 120][cite_start], con especial atención a la usabilidad para adultos mayores o usuarios que son "poco dúctiles para el uso de la tecnología"[cite: 29].

---

## ✨ Alcance y Funcionalidades Principales (para Pacientes)

[cite_start]El sistema propuesto es una solución moderna [cite: 29, 35] [cite_start]que permite a los pacientes gestionar las siguientes actividades[cite: 67, 166]:

* **Gestión de Cuenta y Autenticación:**
    * [cite_start]Registro de nuevos pacientes mediante formulario[cite: 102].
    * [cite_start]Inicio de sesión con usuario/contraseña [cite: 103] [cite_start]o **autenticación biométrica (huella digital)**, si el dispositivo es compatible[cite: 104, 136].
    * [cite_start]Soporte de ingreso al agitar el dispositivo, que abre la aplicación de correo pre-armando un mensaje de asistencia[cite: 105, 191].
* [cite_start]**Gestión de Datos Personales:** Visualizar y modificar información básica como nombre, teléfono, y correo electrónico[cite: 106, 228].
* **Solicitud y Reserva de Turnos:**
    * [cite_start]Visualizar la lista de médicos disponibles y sus especialidades[cite: 68, 107].
    * [cite_start]Consultar los días y horarios habilitados para la atención[cite: 69, 108].
    * [cite_start]Reservar un turno seleccionando médico, día y horario[cite: 109].
* [cite_start]**Gestión de Turnos:** Cancelación de un turno previamente solicitado[cite: 70, 111].
* [cite_start]**Historial de Turnos:** Consulta de los turnos vigentes y los ya utilizados o cancelados[cite: 71, 112, 243].

---

## 🛠️ Arquitectura y Tecnologías

[cite_start]El diseño del sistema se basa en una arquitectura **Cliente-Servidor** [cite: 151][cite_start], donde la aplicación móvil actúa como cliente y la lógica de negocio reside en un servidor a través de una API REST[cite: 149, 150].

| Componente | Tecnología | Uso |
| :--- | :--- | :--- |
| **Desarrollo Móvil** | [cite_start]**Java** [cite: 50, 85, 145] [cite_start]y **Android Studio** [cite: 50, 86, 145] | Lenguaje de programación nativo y entorno de desarrollo para Android. |
| **Networking/API** | [cite_start]**Retrofit 2** [cite: 91] | Librería para gestionar las peticiones HTTP entre la aplicación y la API. |
| **Backend/API** | [cite_start]**API REST** [cite: 51, 90, 150] [cite_start]*hosteada* en **Microsoft Azure** [cite: 51] | Lógica de negocio y procesamiento de datos. |
| **Base de Datos** | [cite_start]**MySQL** [cite: 73, 150] | Almacenamiento centralizado para garantizar la consistencia e integridad de los datos. |
| **Formato de Datos** | [cite_start]**JSON** (JavaScript Object Notation) [cite: 92] | Utilizado para la comunicación e intercambio de datos entre la app y el servidor. |
| **Control de Versiones** | [cite_start]**Git** y **GitHub** [cite: 88] | Para el manejo del código fuente y la colaboración. |

---

## ⛔ No Contemplado (Límites del Proyecto)

[cite_start]En esta versión del proyecto (prototipo)[cite: 75]:

* [cite_start]**No hay funcionalidades para el personal médico o administrativo** (solo acceso de pacientes)[cite: 76].
* [cite_start]No incluye envío de recordatorios automáticos por correo electrónico o mensajes de texto[cite: 77].
* [cite_start]No se implementan módulos de facturación o pagos en línea[cite: 78].
* [cite_start]**Funcionamiento Offline:** La aplicación requerirá conexión a Internet para todas sus funciones principales[cite: 80].

---
### 🔗 Repositorio del Proyecto

* [cite_start]**App Móvil:** [https://github.com/julianrios23/AppTurnos](https://github.com/julianrios23/AppTurnos) [cite: 397]

