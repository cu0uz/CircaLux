package com.example.circalux.util

object ScientificKnowledgeBase {
    
    data class Tip(val title: String, val content: String, val source: String = "Evidencia Científica")

    val tips = mapOf(
        "vitamina_d" to Tip(
            "Producción de Vitamina D",
            "La vitamina D3 se sintetiza en la piel cuando el colesterol (7-dehidrocolesterol) absorbe la radiación UVB (290-315 nm). El UVI debe ser superior a 3.0 para que este proceso sea eficiente.",
            "Holick, M. F. (2007). Vitamin D deficiency. NEJM."
        ),
        "luz_roja" to Tip(
            "Fotobiomodulación (RLT)",
            "La luz roja (630-670nm) e infrarroja cercana (810-850nm) estimula la citocromo c oxidasa en las mitocondrias, aumentando la producción de ATP (energía celular) y reduciendo el estrés oxidativo.",
            "Hamblin, M. R. (2017). Mechanisms and applications of the anti-inflammatory effects of photobiomodulation."
        ),
        "med" to Tip(
            "Dosis Eritematosa Mínima (MED)",
            "El MED es la cantidad de radiación UV que causa un enrojecimiento leve de la piel. Varía según el fototipo de Fitzpatrick. Exceder el 1 MED regularmente aumenta el riesgo de daño acumulativo.",
            "Fitzpatrick, T. B. (1988). The validity and practicality of sun-reactive skin types I through VI."
        ),
        "exposicion_piel" to Tip(
            "Superficie de Exposición",
            "Exponer una mayor superficie corporal (ej. 50% vs 10%) permite generar la misma cantidad de Vitamina D en una fracción del tiempo, minimizando el daño por radiación en áreas específicas.",
            "Webb, A. R. (2006). Who, what, where and when—influences on cutaneous vitamin D synthesis."
        ),
        "ritmo_circadiano" to Tip(
            "Anclaje Circadiano",
            "La exposición a la luz brillante de la mañana (lux > 1000) ayuda a suprimir la melatonina y sincronizar el reloj maestro (núcleo supraquiasmático), mejorando la calidad del sueño nocturno.",
            "Duffy, J. F., & Czeisler, C. A. (2009). Effect of Light on Human Circadian Physiology."
        ),
        "luminancia" to Tip(
            "Intensidad Lumínica (Lux)",
            "La luminancia mide la cantidad de luz que llega a una superficie. Para regular el ritmo circadiano, se recomienda una exposición de al menos 1,000 lux durante 30 minutos por la mañana. En un día soleado, el exterior puede alcanzar más de 100,000 lux.",
            "Brown, T. M. (2020). Using Light to Synchronize Human Circadian Rhythms."
        ),
        "amanecer_atardecer" to Tip(
            "Espectro Solar y Ritmos",
            "Durante el amanecer y el atardecer, la atmósfera filtra las ondas cortas (UV/Azul), dejando un predominio de Luz Roja e Infrarroja Cercana. Esta luz prepara el cuerpo para el día o la noche, mitigando el daño oxidativo.",
            "Mead, M. N. (2008). Benefits of Sunlight: A Bright Spot for Human Health."
        ),
        "angulo_solar" to Tip(
            "Ley de Beer-Lambert y UV",
            "El ángulo de elevación solar determina la distancia que recorren los fotones a través de la atmósfera. Por debajo de 30-35°, la capa de ozono absorbe casi todos los fotones UVB, imposibilitando la síntesis de Vitamina D.",
            "Engelsen, O. (2010). The Evolution of Vitamin D Synthesis Efficiency."
        ),
        "brujula_solar" to Tip(
            "Acimut y Orientación",
            "El acimut solar indica la posición del sol en el plano horizontal respecto al norte. Orientarse directamente hacia el sol (acimut 0° de diferencia) maximiza la irradiancia recibida por centímetro cuadrado de piel.",
            "Holick, M. F. (2011). Vitamin D: A Millenium Perspective."
        )
    )

    fun getTip(key: String): Tip? = tips[key]
}
