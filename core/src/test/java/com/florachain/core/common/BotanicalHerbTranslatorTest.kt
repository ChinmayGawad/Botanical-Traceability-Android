package com.florachain.core.common

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BotanicalHerbTranslatorTest {

    @Test
    fun `getRegionalName returns Hindi translation for Ashwagandha`() {
        val result = BotanicalHerbTranslator.getRegionalName("ASHWAGANDHA", "hi")
        assertTrue(result.contains("अश्वगंधा"))
        assertTrue(result.contains("Ashwagandha Root Powder"))
    }

    @Test
    fun `getRegionalName returns Marathi translation for Tulsi`() {
        val result = BotanicalHerbTranslator.getRegionalName("TULSI", "mr")
        assertTrue(result.contains("कृष्ण तुळस"))
    }

    @Test
    fun `getRegionalName returns English and scientific taxon by default`() {
        val result = BotanicalHerbTranslator.getRegionalName("TURMERIC", "en")
        assertTrue(result.contains("Lakadong Turmeric"))
        assertTrue(result.contains("Curcuma longa"))
    }

    @Test
    fun `getRegionalName returns original query for unknown botanical name`() {
        val result = BotanicalHerbTranslator.getRegionalName("UNKNOWN_HERB_123", "en")
        assertEquals("UNKNOWN_HERB_123", result)
    }
}

