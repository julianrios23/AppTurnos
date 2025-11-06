# 🏥 App Móvil de Gestión de Turnos Médicos

Este proyecto propone el desarrollo de una **aplicación móvil nativa para Android** destinada a la gestión de turnos médicos en una clínica con múltiples profesionales de la salud.

Su objetivo principal es facilitar a los **pacientes** la solicitud, consulta, modificación y cancelación de sus turnos desde su teléfono celular, optimizando la comunicación y reduciendo los errores administrativos.

El diseño de la aplicación está pensado para ser **simple, claro y accesible**, con especial atención a la usabilidad para adultos mayores o usuarios que son "poco dúctiles para el uso de la tecnología".

---

## ✨ Alcance y Funcionalidades Principales (para Pacientes)

El sistema propuesto es una solución moderna que permite a los pacientes gestionar las siguientes actividades:

* **Gestión de Cuenta y Autenticación:**
    * Registro de nuevos pacientes mediante formulario.
    * Inicio de sesión con usuario/contraseña o **autenticación biométrica (huella digital)**, si el dispositivo es compatible.
    * Soporte de ingreso al agitar el dispositivo, que abre la aplicación de correo pre-armando un mensaje de asistencia.
* **Gestión de Datos Personales:** Visualizar y modificar información básica como nombre, teléfono, y correo electrónico.
* **Solicitud y Reserva de Turnos:**
    * Visualizar la lista de médicos disponibles y sus especialidades.
    * Consultar los días y horarios habilitados para la atención.
    * Reservar un turno seleccionando médico, día y horario.
* **Gestión de Turnos:** Cancelación de un turno previamente solicitado.
* **Historial de Turnos:** Consulta de los turnos vigentes y los ya utilizados o cancelados.

---

## 🛠️ Arquitectura y Tecnologías

El diseño del sistema se basa en una arquitectura **Cliente-Servidor**, donde la aplicación móvil actúa como cliente y la lógica de negocio reside en un servidor a través de una API REST.

| Componente | Tecnología | Uso |
| :--- | :--- | :--- |
| **Desarrollo Móvil** | **Java** y **Android Studio** | Lenguaje de programación nativo y entorno de desarrollo para Android. |
| **Networking/API** | **Retrofit 2** | Librería para gestionar las peticiones HTTP entre la aplicación y la API. |
| **Backend/API** | **API REST** *hosteada* en **Microsoft Azure** | Lógica de negocio y procesamiento de datos. |
| **Base de Datos** | **MySQL** | Almacenamiento centralizado para garantizar la consistencia e integridad de los datos. |
| **Formato de Datos** | **JSON** (JavaScript Object Notation) | Utilizado para la comunicación e intercambio de datos entre la app y el servidor. |
| **Control de Versiones** | **Git** y **GitHub** | Para el manejo del código fuente y la colaboración. |

---

## ⛔ No Contemplado (Límites del Proyecto)

En esta versión del proyecto (prototipo):

* **No hay funcionalidades para el personal médico o administrativo** (solo acceso de pacientes).
* No incluye envío de recordatorios automáticos por correo electrónico o mensajes de texto.
* No se implementan módulos de facturación o pagos en línea.
* **Funcionamiento Offline:** La aplicación requerirá conexión a Internet para todas sus funciones principales.

---
### 🔗 Repositorio del Proyecto

* **App Móvil:** [https://github.com/julianrios23/AppTurnos](https://github.com/julianrios23/AppTurnos)
