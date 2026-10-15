package com.nmiet.app.data.local

import com.nmiet.app.data.remote.PilotCurriculumCatalog
import com.nmiet.app.domain.model.AppLanguage
import org.json.JSONArray
import org.json.JSONObject

/**
 * Intelligent Offline Curriculum Package Resolver.
 * Extracts accurate, spoken educational answers directly from downloaded Learning Packs
 * (including sections, key terms, equations, quiz explanations, and misconceptions).
 */
object OfflinePackageAnswerResolver {

    private val STOP_WORDS = setOf(
        "what", "when", "where", "which", "who", "why", "how", "explain",
        "about", "tell", "please", "with", "from", "this", "that", "the",
        "and", "is", "are", "was", "were", "does", "did", "define", "describe",
        "give", "me", "a", "an", "in", "of", "to", "for", "on", "kya", "hai",
        "batao", "ka", "ki", "ke", "can", "you", "details", "brief", "some",
        "କଣ", "କିପରି", "କର", "କୁହ", "ଅଟେ", "ଏହା", "ବିଷୟରେ"
    )

    fun resolveAnswer(
        query: String,
        language: AppLanguage,
        learningDao: LearningDao
    ): String {
        val trimmed = query.trim()
        if (trimmed.isBlank()) return getDynamicHelpfulGuide(language, emptyList())

        val lowerQuery = trimmed.lowercase()
        val queryTokens = lowerQuery
            .split(Regex("[\\s,?.!:;\"'()\\[\\]{}]+"))
            .filter { it.length > 2 && it !in STOP_WORDS }
            .toSet()

        // 1. Fetch available packs from local Room database
        val packs = try {
            learningDao.getAllPacksOnce()
        } catch (_: Throwable) {
            emptyList()
        }.ifEmpty {
            listOf(
                LearningPackEntity.fromDomain(PilotCurriculumCatalog.getPilotLearningPack()),
                LearningPackEntity.fromDomain(PilotCurriculumCatalog.getPilotClass10LearningPack()),
                LearningPackEntity.fromDomain(com.nmiet.app.data.local.entities.BrainPackSeed.createLearningPack())
            )
        }

        // 2. Check Key Terms definitions inside sectionsJson
        val termAnswer = findKeyTermMatch(packs, queryTokens, lowerQuery, language)
        if (termAnswer != null) return termAnswer

        // 3. Check Quiz explanations & misconception hints
        val quizAnswer = findQuizExplanationMatch(packs, queryTokens, lowerQuery, language)
        if (quizAnswer != null) return quizAnswer

        // 4. Check Equations & Formulas
        if (isFormulaQuery(lowerQuery)) {
            val formulaAnswer = findEquationMatch(packs, queryTokens, lowerQuery, language)
            if (formulaAnswer != null) return formulaAnswer
        }

        // 5. Check Section Title and Content details
        val sectionAnswer = findSectionContentMatch(packs, queryTokens, lowerQuery, language)
        if (sectionAnswer != null) return sectionAnswer

        // 6. Check Chapter / Topic Overview Summary
        val overviewAnswer = findPackOverviewMatch(packs, queryTokens, lowerQuery, language)
        if (overviewAnswer != null) return overviewAnswer

        // 7. Check Pilot Curriculum Catalog curated answers
        val catalogAnswer = PilotCurriculumCatalog.getLocalTutorResponse(trimmed)
        val catalogText = when (language) {
            AppLanguage.ODIA_ONLY -> catalogAnswer.first
            AppLanguage.HINDI_ONLY -> catalogAnswer.second
            else -> catalogAnswer.second
        }
        if (!catalogText.contains("please consult your syllabus", ignoreCase = true) &&
            !catalogText.contains("ସ୍ଥାନୀୟ ପାଠ୍ୟକ୍ରମ ଉପଲବ୍ଧ", ignoreCase = true)) {
            return cleanForSpeech(catalogText)
        }

        // 8. If query is outside current packs, provide a dynamic, informative curriculum guide
        return getDynamicHelpfulGuide(language, packs)
    }

    private fun isFormulaQuery(query: String): Boolean {
        return query.contains("formula") || query.contains("equation") ||
                query.contains("reaction") || query.contains("balance") ||
                query.contains("ସମୀକରଣ") || query.contains("समीकरण")
    }

    private fun findKeyTermMatch(
        packs: List<LearningPackEntity>,
        tokens: Set<String>,
        lowerQuery: String,
        language: AppLanguage
    ): String? {
        for (pack in packs) {
            try {
                val sectionsArray = JSONArray(pack.sectionsJson)
                for (i in 0 until sectionsArray.length()) {
                    val secObj = sectionsArray.getJSONObject(i)
                    if (!secObj.has("keyTerms")) continue
                    val termsArray = secObj.getJSONArray("keyTerms")
                    for (j in 0 until termsArray.length()) {
                        val tObj = termsArray.getJSONObject(j)
                        val termEng = tObj.optString("termEnglish", "")
                        val termOdia = tObj.optString("termOdia", "")
                        val termHindi = tObj.optString("termHindi", "")
                        val defEng = tObj.optString("defEng", "")
                        val defOdia = tObj.optString("defOdia", "")
                        val defHindi = tObj.optString("defHindi", "")

                        val engLower = termEng.lowercase()
                        val odiaLower = termOdia.lowercase()

                        val matches = lowerQuery.contains(engLower) ||
                                (engLower.isNotBlank() && engLower.split(" ").any { it in tokens && it.length > 3 }) ||
                                lowerQuery.contains(odiaLower)

                        if (matches) {
                            return when (language) {
                                AppLanguage.ODIA_ONLY -> {
                                    val t = termOdia.ifBlank { termEng }
                                    val d = defOdia.ifBlank { defEng }
                                    cleanForSpeech("$t: $d")
                                }
                                AppLanguage.HINDI_ONLY -> {
                                    val t = termHindi.ifBlank { termEng }
                                    val d = defHindi.ifBlank { defEng }
                                    cleanForSpeech("$t: $d")
                                }
                                else -> {
                                    cleanForSpeech("$termEng: $defEng")
                                }
                            }
                        }
                    }
                }
            } catch (_: Exception) {}
        }
        return null
    }

    private fun findQuizExplanationMatch(
        packs: List<LearningPackEntity>,
        tokens: Set<String>,
        lowerQuery: String,
        language: AppLanguage
    ): String? {
        for (pack in packs) {
            try {
                val questionsArray = JSONArray(pack.quizQuestionsJson)
                for (i in 0 until questionsArray.length()) {
                    val qObj = questionsArray.getJSONObject(i)
                    val qEng = qObj.optString("questionEnglish", "")
                    val qOdia = qObj.optString("questionOdia", "")
                    val expEng = qObj.optString("explanationEnglish", "")
                    val expOdia = qObj.optString("explanationOdia", "")
                    val conceptTag = qObj.optString("conceptTag", "").lowercase()
                    val miscEng = qObj.optString("misconceptionHintEnglish", "")
                    val miscoral = qObj.optString("misconceptionHintOdia", "")

                    val genericWords = setOf("brain", "science", "human", "chapter", "lesson", "plant", "plants", "body", "class")
                    val qTokens = qEng.lowercase().split(Regex("[\\s,?.!]+")).filter { it !in STOP_WORDS && it !in genericWords }
                    val overlap = tokens.intersect(qTokens.toSet())

                    val tagParts = conceptTag.split("_").filter { it !in genericWords }
                    val tagMatch = tagParts.isNotEmpty() && tagParts.any { it in tokens }
                    val highOverlap = overlap.size >= 2 || (overlap.size == 1 && tagMatch)

                    if (tagMatch || highOverlap) {
                        return when (language) {
                            AppLanguage.ODIA_ONLY -> cleanForSpeech(expOdia.ifBlank { expEng })
                            else -> cleanForSpeech(expEng.ifBlank { expOdia })
                        }
                    }
                }
            } catch (_: Exception) {}
        }
        return null
    }

    private fun findEquationMatch(
        packs: List<LearningPackEntity>,
        tokens: Set<String>,
        lowerQuery: String,
        language: AppLanguage
    ): String? {
        for (pack in packs) {
            val formula = pack.equationOrFormula
            if (formula.isNullOrBlank()) continue

            val packTokens = (pack.titleEnglish + " " + pack.topicEnglish).lowercase().split(" ")
            val matchesPack = tokens.any { it in packTokens } ||
                    lowerQuery.contains("photosynthesis") ||
                    lowerQuery.contains("chemical") ||
                    lowerQuery.contains("brain") ||
                    lowerQuery.contains("power") ||
                    lowerQuery.contains("watt")

            if (matchesPack) {
                val topic = if (language == AppLanguage.ODIA_ONLY) {
                    pack.topicOdia.ifBlank { pack.titleOdia }
                } else {
                    pack.topicEnglish.ifBlank { pack.titleEnglish }
                }
                return when (language) {
                    AppLanguage.ODIA_ONLY -> cleanForSpeech("$topic ର ମୁଖ୍ୟ ସମୀକରଣ ହେଉଛି: $formula")
                    else -> cleanForSpeech("The key formula for $topic is: $formula")
                }
            }
        }
        return null
    }

    private fun findSectionContentMatch(
        packs: List<LearningPackEntity>,
        tokens: Set<String>,
        lowerQuery: String,
        language: AppLanguage
    ): String? {
        var bestMatchText: String? = null
        var maxScore = 0

        for (pack in packs) {
            try {
                val sectionsArray = JSONArray(pack.sectionsJson)
                for (i in 0 until sectionsArray.length()) {
                    val secObj = sectionsArray.getJSONObject(i)
                    val titleEng = secObj.optString("titleEnglish", "")
                    val titleOdia = secObj.optString("titleOdia", "")
                    val contentEng = secObj.optString("contentEnglish", "")
                    val contentOdia = secObj.optString("contentOdia", "")

                    val textToScore = (titleEng + " " + contentEng).lowercase()
                    val matchCount = tokens.count { textToScore.contains(it) }

                    if (matchCount > maxScore && matchCount >= 1) {
                        maxScore = matchCount
                        val content = if (language == AppLanguage.ODIA_ONLY) {
                            contentOdia.ifBlank { contentEng }
                        } else {
                            contentEng.ifBlank { contentOdia }
                        }
                        val sentences = content.split("\n")
                            .flatMap { it.split(Regex("(?<=[.!?])\\s+")) }
                            .map { it.trim().removePrefix("•").trim() }
                            .filter { it.isNotBlank() && it.length > 15 }

                        val topSentence = sentences.maxByOrNull { sentence ->
                            val sLower = sentence.lowercase()
                            tokens.count { sLower.contains(it) }
                        } ?: content.take(200)

                        bestMatchText = if (topSentence.endsWith(".")) topSentence else "$topSentence."
                    }
                }
            } catch (_: Exception) {}
        }

        return bestMatchText?.let { cleanForSpeech(it) }
    }

    private fun findPackOverviewMatch(
        packs: List<LearningPackEntity>,
        tokens: Set<String>,
        lowerQuery: String,
        language: AppLanguage
    ): String? {
        for (pack in packs) {
            val titleWords = (pack.titleEnglish + " " + pack.topicEnglish).lowercase().split(" ")
            val matches = tokens.any { it in titleWords } ||
                    (pack.titleEnglish.isNotBlank() && lowerQuery.contains(pack.titleEnglish.lowercase())) ||
                    (pack.titleOdia.isNotBlank() && lowerQuery.contains(pack.titleOdia))

            if (matches) {
                val summary = if (language == AppLanguage.ODIA_ONLY) {
                    pack.odiaContentJson.ifBlank { pack.englishContentJson }
                } else {
                    pack.englishContentJson.ifBlank { pack.odiaContentJson }
                }
                if (summary.isNotBlank()) {
                    // Extract first 2 sentences for concise audio speech
                    val shortSummary = summary.split(".").filter { it.isNotBlank() }.take(2).joinToString(".") + "."
                    return cleanForSpeech(shortSummary)
                }
            }
        }
        return null
    }

    private fun getDynamicHelpfulGuide(language: AppLanguage, packs: List<LearningPackEntity>): String {
        val topicNames = packs.mapNotNull { it.topicEnglish.ifBlank { it.titleEnglish }.ifBlank { null } }
            .distinct()
            .take(3)
            .joinToString(", ")

        val availableText = if (topicNames.isNotBlank()) topicNames else "Photosynthesis, Brain Anatomy, and Chemical Reactions"

        return when (language) {
            AppLanguage.ODIA_ONLY -> {
                "ଅଫଲାଇନ୍ ମୋଡ଼ରେ ଆପଣଙ୍କ ପାଖରେ $availableText ବିଷୟରେ ପାଠ୍ୟକ୍ରମ ପ୍ୟାକେଜ୍ ଉପଲବ୍ଧ ଅଛି। ଆପଣ ଏହି ବିଷୟଗୁଡ଼ିକ ଉପରେ ପ୍ରଶ୍ନ ପଚାରିପାରିବେ କିମ୍ବା ଅନ୍ୟାନ୍ୟ ବିଷୟ ପାଇଁ ଇଣ୍ଟରନେଟ୍ ସଂଯୋଗ କରି କ୍ଲାଉଡ୍ AI ବ୍ୟବହାର କରିପାରିବେ।"
            }
            AppLanguage.HINDI_ONLY -> {
                "ऑफ़लाइन आपके पास $availableText के पाठ्यक्रम पैकेज उपलब्ध हैं। आप इन विषयों पर प्रश्न पूछ सकते हैं, या किसी अन्य विषय के लिए इंटरनेट से क्लाउड AI का उपयोग कर सकते हैं।"
            }
            else -> {
                "Offline, your downloaded packages cover $availableText. You can ask questions about those curriculum topics, or connect to the internet to query Cloud AI on any subject."
            }
        }
    }

    private fun cleanForSpeech(text: String): String {
        return text
            .replace("**", "")
            .replace("*", "")
            .replace("#", "")
            .replace("`", "")
            .replace("•", "")
            .replace("_", "")
            .replace(Regex("(?m)^\\s*[-*]\\s+"), "")
            .replace(Regex("\\s+"), " ")
            .trim()
    }
}
