package com.nyora.linux.ui

// ── Friendly source subtitle ────────────────────────────────────────────────
// Sources carry an internal parser/engine name (e.g. "MadaraParser") — users
// should only ever see the reader's LANGUAGE, never that developer jargon.
private val LANG_NAMES = mapOf(
    "en" to "English", "es" to "Spanish", "pt" to "Portuguese", "fr" to "French",
    "de" to "German", "it" to "Italian", "ru" to "Russian", "id" to "Indonesian",
    "ar" to "Arabic", "tr" to "Turkish", "pl" to "Polish", "vi" to "Vietnamese",
    "th" to "Thai", "ja" to "Japanese", "ko" to "Korean", "zh" to "Chinese",
    "uk" to "Ukrainian", "fa" to "Persian", "nl" to "Dutch", "multi" to "Multi-language",
)

fun languageLabel(lang: String?): String {
    val key = (lang ?: "").lowercase().trim()
    if (key.isBlank()) return "Manga"
    return LANG_NAMES[key] ?: LANG_NAMES[key.take(2)] ?: key.uppercase()
}

fun sourceSubtitle(lang: String?, isNsfw: Boolean): String =
    languageLabel(lang) + if (isNsfw) " · 18+" else ""
