package com.florachain.core.common

object BotanicalHerbTranslator {

    data class HerbNames(
        val english: String,
        val hindi: String,
        val marathi: String,
        val botanicalTaxon: String
    )

    private val translations = mapOf(
        "ASHWAGANDHA" to HerbNames(
            english = "Ashwagandha Root Powder",
            hindi = "अश्वगंधा चूर्ण",
            marathi = "अश्वगंधा पावडर",
            botanicalTaxon = "Withania somnifera"
        ),
        "TULSI" to HerbNames(
            english = "Krishna Tulsi Leaves",
            hindi = "कृष्णा तुलसी",
            marathi = "कृष्ण तुळस",
            botanicalTaxon = "Ocimum sanctum"
        ),
        "TURMERIC" to HerbNames(
            english = "Lakadong Turmeric",
            hindi = "लकाडोंग हल्दी",
            marathi = "लकाडोंग हळद",
            botanicalTaxon = "Curcuma longa"
        ),
        "BRAHMI" to HerbNames(
            english = "Bacopa Brahmi Extract",
            hindi = "ब्राह्मी अर्क",
            marathi = "ब्राह्मी अर्क",
            botanicalTaxon = "Bacopa monnieri"
        ),
        "SHATAVARI" to HerbNames(
            english = "Shatavari Root Extract",
            hindi = "शतावरी",
            marathi = "शतावरी मुळी",
            botanicalTaxon = "Asparagus racemosus"
        ),
        "NEEM" to HerbNames(
            english = "Organic Neem Leaf",
            hindi = "नीम पत्ती",
            marathi = "कडुनिंब पाने",
            botanicalTaxon = "Azadirachta indica"
        )
    )

    fun getRegionalName(query: String, languageCode: String = "en"): String {
        val key = translations.keys.find { query.contains(it, ignoreCase = true) } ?: return query
        val entry = translations[key] ?: return query
        return when (languageCode.lowercase()) {
            "hi" -> "${entry.hindi} (${entry.english})"
            "mr" -> "${entry.marathi} (${entry.english})"
            else -> "${entry.english} (${entry.botanicalTaxon})"
        }
    }
}
