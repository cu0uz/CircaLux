# CircaLux ☀️

CircaLux es una aplicación Android nativa diseñada para optimizar el ritmo circadiano y monitorizar la síntesis de Vitamina D a través de sesiones solares y de Terapia de Luz Roja (RLT).

La aplicación está construida con **Jetpack Compose**, **Kotlin** y **Room Database**, siguiendo las prácticas modernas de desarrollo en Android.

---

## ✨ Características Principales

- **Monitorización Solar en Tiempo Real**: Índice UV (UVI), Luminancia (Lux) y Elevación Solar.
- **Base de Conocimiento Científico**: Diálogos expandibles con explicaciones técnicas y referencias bibliográficas sobre fotobiología y ritmos circadianos.
- **Brújula Solar Interactiva**: Ayuda a orientar el cuerpo hacia el sol (acimut) para maximizar la irradiancia.
- **Seguimiento de Sesiones**: Registro detallado de sesiones de Sol y Luz Roja (RLT).
- **Estimación de Vitamina D**: Algoritmo basado en el fototipo de Fitzpatrick, área de exposición y niveles de radiación.
- **Salud Metabólica**: Registro de métricas de salud como Glucosa, Cetonas e Índice GKI.
- **Composición Corporal**: Seguimiento de medidas antropométricas con cálculo automático de % de Grasa Corporal (Método Navy) y WHtR.
- **Privacidad Total**: Todos los datos se almacenan localmente en una base de datos Room; sin sincronización en la nube ni rastreadores.

---

## 🚀 Sistema de Activación y Soporte

CircaLux ofrece un modelo de **Donación (Ko-fi)** para desbloquear la versión completa:
- La versión gratuita permite usar todas las herramientas de monitorización y registro de salud.
- Las sesiones activas de Sol y Luz Roja están limitadas a 10 usos en la versión trial.
- La activación se realiza mediante una donación de **1€** a través de [Ko-fi](https://ko-fi.com/cu0uz), incluyendo el **USER ID** generado por la app.

---

## 🛠 Guía de Compilación

Este es un proyecto **Android Nativo**. No utiliza frameworks híbridos como Capacitor o Ionic.

### Requisitos
- **Android Studio** (Hedgehog o superior).
- **JDK 17** o superior.
- **Android SDK** (API 34/35+).

### Instrucciones
1. **Clonar el repositorio:**
   ```bash
   git clone https://github.com/cu0uz/CircaLux
   cd CircaLux
   ```
2. **Permisos de ejecución:**
   ```bash
   chmod +x gradlew
   ```
3. **Compilar APK de Debug:**
   ```bash
   ./gradlew assembleDebug
   ```

---

## 🔒 Privacidad e Independencia

CircaLux es **Google Play Services Free**:
- Utiliza la **API Nativa de Localización** de Android en lugar de los servicios de Google.
- No contiene Firebase, Google Analytics ni SDKs de rastreo propietarios.
- Compatible con ROMs de-googled (como GrapheneOS) y apta para F-Droid.

---

**CircaLux v1.0.0** - Created with AI by cu0uz in 2026.
