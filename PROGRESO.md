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
- [x] **UI Sticky:** La barra superior (TopBar) y el logo permanecen siempre visibles durante el scroll para acceso inmediato al menú y navegación.
- [x] **Sistema de Logging (CircaLogger):**
    - Implementación de registros detallados guardados en `filesDir`.
    - Botón en menú lateral para enviar reporte de errores vía email a `myt8dolgj@gmail.com`.
- [x] **Easter Egg:** Activación del "Modo Fotosíntesis" tras 7 pulsaciones en el logo.🌻

---

### PASO 2: Home Screen (Monitorización y Sesiones)
- [x] **Sensores en Tiempo Real:**
    - Lectura de **Luminancia (Lux)** ambiental constante.
    - Localización GPS dinámica: Actualización automática de datos globales si el usuario se desplaza más de 1km.
- [x] **Datos Astronómicos y Climáticos:**
    - Hora, fecha, temperatura y estado del tiempo actual.
    - Horas exactas de salida y puesta de sol según ubicación.
    - Pronóstico de 7 días (Día, UVI Max, Temp Max, Clima).
- [x] **Brújula Solar Interactiva:**
    - Utiliza el magnetómetro y acelerómetro para orientar al usuario hacia el sol.
    - Indicadores visuales: "Gira a la derecha/izquierda" y confirmación "¡ESTÁS DE CARA AL SOL!".
- [x] **Gestión de Sesiones Sol/Luz Roja:**
    - **Sesión Solar:** Selección de exposición (10% a 100%), recordatorios de "Darse la vuelta" cada 10 min, cálculo de Vitamina D generada por minuto.
    - **Sesión RLT:** Ajustes de potencia (W) y distancia (cm) específicos para terapia de luz roja.
    - **Guardado Inteligente:** Posibilidad de descartar sesión o guardarla con selección del estado del tiempo manual para corregir discrepancias.
- [x] **Precisión UVI y MED:**
    - **Leyenda Dinámica:** La escala UVI muestra tiempos MED y consejos basados estrictamente en el **Fototipo de Fitzpatrick** del perfil.
    - **Colores Oficiales:** Sincronización cromática según la escala de la OMS (Verde, Amarillo, Naranja, Rojo, Púrpura).

---

### PASO 3: Historial y Proyecciones Avanzadas
- [x] **Cálculo de Vitamina D (Ciencia Aplicada):**
    - Algoritmo basado en: `[Nivel Base] + [Síntesis Solar] + [Suplementos] - [Decaimiento Biológico]`.
    - **Vida Media:** Se aplica una disminución biológica basada en una vida media de 15 días si no hay ingesta.
- [x] **Proyección a 30 días:** Gráfica Sparkline que estima niveles futuros basada en el comportamiento actual.
- [x] **Métricas de Salud y Físicas:**
    - Registro de Glucosa, Cetonas y cálculo automático de **GKI**.
    - Medidas corporales (Cuello, Cintura, etc.) con cálculo automático de **WHtR** y **% Grasa Corporal (Método Navy)**.
- [x] **Gestión de Datos:** Implementación de gestos **Swipe-to-Delete** para borrar cualquier entrada del historial (incluyendo tomas de suplementos).

---

### PASO 4: Perfil, Ajustes y Fidelización
- [x] **Perfil de Usuario:** Datos antropométricos, fecha de última analítica y configuración de suplementos.
- [x] **Suplementos Retroactivos:** Generación automática de tomas diarias desde la fecha de inicio indicada para poblar el historial fielmente.
- [x] **Sistema de Alertas:**
    - Alarmas sonoras al alcanzar el límite MED de seguridad.
    - Alertas de apertura/cierre de la ventana de Vitamina D (UVI > 3.0).
    - Avisos 10 min antes del amanecer/anochecer.
- [x] **Backups:** Sistema de exportación e importación CSV mediante `OpenDocument` para persistencia total sin nube.
- [x] **Sistema de Activación (Trial):**
    - Límite de 10 sesiones solares en modo gratuito.
    - Generación de User ID único y sistema de validación por Checksum (ASCII % 99).
- [x] **Menú Lateral:** Acceso a Historial, Perfil, Ajustes, Blog "Manifiesto Jota" y **Estado de Sensores** (Lux, GPS, Magnetómetro, Test de Audio).

---

## Errores Identificados y Soluciones Técnicas

| Error Encontrado | Solución Implementada |
| :--- | :--- |
| **Ángulo Solar Inexacto:** Desfase de ~22° en la elevación. | Migración a fórmulas astronómicas basadas en **Tiempo UTC**, eliminando errores de zona horaria local. |
| **UVI Discrepante:** Mostraba el máximo diario (7.9) en lugar del actual (4.0). | Cambio en la API para solicitar el **índice UVI instantáneo** en tiempo real. |
| **Pérdida de Sesiones Suplementos:** Días omitidos en el backfill automático. | Normalización de calendarios a `00:00:00.000` para asegurar un registro por día exacto. |
| **Fallo en Restauración CSV:** Error de permisos en Android 13+. | Implementación de `ActivityResultContracts.OpenDocument()` para acceso seguro a archivos. |
| **Brújula Estática:** No respondía al giro del usuario. | Creación de `OrientationManager` combinando magnetómetro y acelerómetro para rumbo (Heading) real. |
| **Consumo de Batería:** Sensores activos innecesariamente. | Implementación de `onCleared` y gestión de ciclos de vida para detener sensores al cerrar la app. |
| **Migración Room:** Crash al añadir campos de RLT y Suplementos. | Migración manual de base de datos a Versión 4 con `fallbackToDestructiveMigration` para estabilidad. |

---
**CircaLux v1.0.0** - Creado con IA por cu0uz en 2026.
