# App-Android-Aiep-U3

## Descripción del Proyecto

La aplicación móvil **"App-Android-Aiep-U3"** fue desarrollada para una empresa de distribución de alimentos que incorporó un nuevo servicio de entregas a domicilio.  
Su objetivo principal es **automatizar el cálculo de los costos de despacho**, gestionar **la ubicación GPS de los clientes** y **supervisar la cadena de frío** durante el transporte de productos sensibles, como carnes y mariscos.  

El sistema cuenta con **autenticación segura mediante Firebase Authentication**, y toda la información relevante se almacena en **Firebase Realtime Database**, garantizando disponibilidad y sincronización en tiempo real.  

---

## 🚀 Características Principales

- **Inicio de sesión seguro (SSO)** con **Firebase Authentication (Gmail)**.  
- **Gestión de usuarios** con registro y validación de credenciales.  
- **Cálculo automático del costo de despacho**, basado en reglas de negocio:
  - Compras sobre **$50.000 → despacho gratuito (radio ≤ 20 km)**  
  - Compras entre **$25.000 y $49.999 → $150 por kilómetro**  
  - Compras menores a **$25.000 → $300 por kilómetro**
- **Registro automático de ubicación GPS** del cliente al iniciar sesión.  
- **Supervisión de temperatura** del congelador (cadena de frío).  
- **Gestión colaborativa del proyecto en GitHub**, con historias de usuario y documentación.  

---

## 🧠 Tecnologías Utilizadas

| Tipo | Herramienta / Tecnología |
|------|---------------------------|
| Lenguaje | Kotlin |
| Entorno | Android Studio |
| Base de datos | Firebase Realtime Database |
| Autenticación | Firebase Authentication (Google SSO) |
| Control de versiones | Git / GitHub |
| Pruebas | Emuladores Android (Lollipop, Oreo) |

---
