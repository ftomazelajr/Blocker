package com.fernando.blocker

object BlockList {

    // Domínios comuns de sites adultos (comparação por "contains")
    val domains = listOf(
        "pornhub", "xvideos", "xnxx", "redtube", "xhamster",
        "youporn", "brazzers", "xxx", "porn", "hentai",
        "onlyfans", "chaturbate", "livejasmin", "cam4",
        "spankbang", "tnaflix", "eporner", "tube8"
    )

    // Palavras-chave genéricas que também disparam o bloqueio
    // (útil para busca no Google/redes antes de abrir o site)
    val keywords = listOf(
        "pornografia", "conteúdo adulto +18", "nudes"
    )

    // Pacotes de navegadores que terão a URL inspecionada
    val browserPackages = setOf(
        "com.android.chrome",
        "org.mozilla.firefox",
        "com.brave.browser",
        "com.opera.browser",
        "com.sec.android.app.sbrowser",
        "com.microsoft.emmx",
        "com.duckduckgo.mobile.android",
        "com.android.browser"
    )

    fun matches(text: String?): Boolean {
        if (text.isNullOrBlank()) return false
        val lower = text.lowercase()
        return domains.any { lower.contains(it) } || keywords.any { lower.contains(it) }
    }
}
