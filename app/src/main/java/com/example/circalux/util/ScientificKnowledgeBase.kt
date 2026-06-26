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
        )
    )

    fun getTip(key: String): Tip? = tips[key]
}
