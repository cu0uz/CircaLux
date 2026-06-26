# Registro de Avances - CircaLux (v1.0.0)

## Estado del Proyecto: Finalización de la Fase 4.3 - Refinamiento Total

Este documento detalla todas las funcionalidades implementadas, las mejoras de diseño y las soluciones técnicas a los problemas encontrados durante el desarrollo de **CircaLux**.

---

### PASO 1: Identidad Visual, Estructura y Diagnóstico
- [x] **Nombre de la App:** Circalux (Circa: Ciclo/Circadiano, Lux: Luz medida).
- [x] **Logo Dinámico (Reloj Circadiano):**
    - Anillo de 24 segmentos representando las 24 horas del día.
    - **Gradiente:** Azul marino (#0B1F3A) a las 00h, Violeta (#5A4FCF) madrugada, Naranja (#FF8A3D) amanecer, Amarillo (#FFD84A) mediodía.
    - **Horizonte:** Apertura del 15% en la base para simbolizar la conexión con el mundo real.
    - **Trayectoria:** Curva minimalista (sonrisa) flotando bajo el anillo.
    - **Punto de Momento Actual:** Indicador con efecto *glow* que recorre el anillo en tiempo real.
- [x] **Icono de App Nativo:** Recreación del logo como icono adaptativo Android con soporte para iconos con tema (Monochrome).
- [x] **UI Sticky:** La barra superior (TopBar) y el logo permanecen siempre visibles durante el scroll para acceso inmediato al menú y navegación.
- [x] **Sistema de Logging (CircaLogger):**
    - Implementación de registros detallados guardados en `filesDir`.
    - Botón en menú lateral para enviar reporte de errores vía email a `myt8dolgj@gmail.com`.
- [x] **Easter Egg:** Activación del "Modo Fotosíntesis" tras 7 pulsaciones en el logo.🌻

---

### PASO 2: Home Screen (Monitorización y Sesiones)
- [x] **Sensores en Tiempo Real:**
    - Lectura de **Luminancia (Lux)** ambiental constante con UI optimizada.
    - Localización GPS dinámica: Actualización automática de datos globales y visualización de coordenadas en Home.
- [x] **Datos Astronómicos y Climáticos:**
    - Hora, fecha, temperatura y estado del tiempo actual.
    - Horas exactas de salida y puesta de sol según ubicación.
    - Pronóstico de 7 días (Día, UVI Max, Temp Max, Clima).
- [x] **Brújula Solar Interactiva:**
    - Utiliza el magnetómetro y acelerómetro para orientar al usuario hacia el sol.
    - Indicadores visuales y confirmación "¡ESTÁS DE CARA AL SOL!".
- [x] **Gestión de Sesiones Sol/Luz Roja:**
    - **Sesión Solar:** Selección de exposición mediante etiquetas descriptivas (Cara/Manos, Camiseta, Bañador, etc.), recordatorios de cambio de posición cada 10 min.
    - **Sesión RLT:** Ajustes de potencia (W) y distancia (cm) específicos para terapia de luz roja.
    - **Guardado Inteligente:** Diálogos de confirmación y selección de estado del tiempo manual.
- [x] **Precisión UVI y MED:**
    - **Solar Elevation Guard:** Validación del UVI basada en la elevación solar real para evitar lecturas falsas al atardecer/amanecer.
    - **Colores Oficiales:** Sincronización cromática según la escala de la OMS.

---

### PASO 3: Historial y Proyecciones Avanzadas
- [x] **Cálculo de Vitamina D (Ciencia Aplicada):**
    - Algoritmo basado en: `[Nivel Base] + [Síntesis Solar] + [Suplementos] - [Decaimiento Biológico]`.
- [x] **Proyección a 30 días:** Gráfica Sparkline que estima niveles futuros basada en el comportamiento actual.
- [x] **Métricas de Salud y Físicas:**
    - Registro de Glucosa, Cetonas y medidas corporales.
    - **Fechas Retroactivas:** Posibilidad de elegir la fecha exacta al añadir métricas o medidas manualmente.
- [x] **Gestión de Datos:** Implementación de gestos **Swipe-to-Delete** con confirmación visual vía Snackbar.

---

### PASO 4: Perfil, Ajustes y Fidelización
- [x] **Perfil de Usuario:** Datos antropométricos, gestión de suplementos y fototipo Fitzpatrick.
- [x] **Acerca de (About):** Nueva sección con descargo de responsabilidad legal y base científica del proyecto.
- [x] **Sistema de Feedback:** Snackbars de confirmación en todas las acciones de guardado y borrado.
- [x] **Backups:** Sistema de exportación e importación CSV con confirmación de estado.
- [x] **Blog "Manifiesto Jota":** WebView optimizado con carga rápida, caché y scroll fluido mediante aceleración por hardware.
- [x] **Sistema de Activación (Trial):** Límite de 10 sesiones solares en modo gratuito con validación por Checksum.

---

## Errores Identificados y Soluciones Técnicas

| Error Encontrado | Solución Implementada |
| :--- | :--- |
| **Ángulo Solar Inexacto:** Desfase de ~22° en la elevación. | Migración a fórmulas astronómicas basadas en **Tiempo UTC**, eliminando errores de zona horaria local. |
| **UVI Discrepante:** Mostraba el máximo diario (7.9) en lugar del actual (4.0). | Cambio en la API para solicitar el **índice UVI instantáneo** en tiempo real. |
| **UVI Sticking:** Valores altos (4.9) al atardecer cuando debería ser < 3. | Implementación de **Solar Elevation Guard**: Validación cruzada del UVI vs elevación solar real (sin(elev)^1.5) para forzar 0.0 tras el ocaso. |
| **Notificaciones Descompasadas:** Avisos de UVI alto al despertar la app por la mañana. | Implementación de **Stale Data Protection**: El monitoreo ahora refresca datos secuencialmente y descarta lecturas con más de 1 hora de antigüedad. |
| **Pérdida de Sesiones Suplementos:** Días omitidos en el backfill automático. | Normalización de calendarios a `00:00:00.000` para asegurar un registro por día exacto. |
| **Fallo en Restauración CSV:** Error de permisos en Android 13+. | Implementación de `ActivityResultContracts.OpenDocument()` para acceso seguro a archivos. |
| **Brújula Estática:** No respondía al giro del usuario. | Creación de `OrientationManager` combinando magnetómetro y acelerómetro para rumbo (Heading) real. |
| **Consumo de Batería:** Sensores activos innecesariamente. | Implementación de `onCleared` y gestión de ciclos de vida para detener sensores al cerrar la app. |
| **WebView Lento/Trabado:** Scroll dificultoso en el blog. | Activación de **DOM Storage** y **Aceleración por Hardware** para renderizado fluido. |
| **Icono de Inicio Genérico:** No mostraba el logo de la app. | Recreación del logo en formato **Vectorial Nativo (Adaptive Icon)** con soporte para temas de sistema. |

---
**CircaLux v1.0.0** - Creado con IA por cu0uz en 2026.
