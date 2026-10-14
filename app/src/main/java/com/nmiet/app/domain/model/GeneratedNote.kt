package com.nmiet.app.domain.model

import org.json.JSONArray
import org.json.JSONObject

data class NoteConceptPoint(
    val pointOdia: String,
    val pointEnglish: String
)

data class GeneratedNote(
    val noteId: String,
    val topicId: String,
    val topicName: String,
    val titleOdia: String,
    val titleEnglish: String,
    val summaryOdia: String,
    val summaryEnglish: String,
    val keyConcepts: List<NoteConceptPoint>,
    val formulasEquations: List<String>,
    val examTips: List<String>,
    val createdAt: Long = System.currentTimeMillis(),
    val isBookmarked: Boolean = false,
    val isOfflineGenerated: Boolean = true
) {
    fun toJson(): String {
        val root = JSONObject().apply {
            put("note_id", noteId)
            put("topic_id", topicId)
            put("topic_name", topicName)
            put("title_odia", titleOdia)
            put("title_english", titleEnglish)
            put("summary_odia", summaryOdia)
            put("summary_english", summaryEnglish)
            put("is_offline_generated", isOfflineGenerated)

            val conceptsArr = JSONArray()
            keyConcepts.forEach { point ->
                val pObj = JSONObject().apply {
                    put("point_odia", point.pointOdia)
                    put("point_english", point.pointEnglish)
                }
                conceptsArr.put(pObj)
            }
            put("key_concepts", conceptsArr)

            val formulasArr = JSONArray()
            formulasEquations.forEach { formulasArr.put(it) }
            put("formulas_equations", formulasArr)

            val tipsArr = JSONArray()
            examTips.forEach { tipsArr.put(it) }
            put("exam_tips", tipsArr)
        }
        return root.toString()
    }

    companion object {
        fun fromJson(
            noteId: String,
            topicId: String,
            topicName: String,
            titleOdia: String,
            titleEnglish: String,
            jsonString: String,
            createdAt: Long,
            isBookmarked: Boolean
        ): GeneratedNote {
            return try {
                val json = JSONObject(jsonString)
                val summaryOdia = json.optString("summary_odia", json.optString("summary", ""))
                val summaryEng = json.optString("summary_english", json.optString("summary", ""))

                val concepts = mutableListOf<NoteConceptPoint>()
                val conceptsArr = json.optJSONArray("key_concepts")
                if (conceptsArr != null) {
                    for (i in 0 until conceptsArr.length()) {
                        val obj = conceptsArr.getJSONObject(i)
                        concepts.add(
                            NoteConceptPoint(
                                pointOdia = obj.optString("point_odia", ""),
                                pointEnglish = obj.optString("point_english", "")
                            )
                        )
                    }
                }

                val formulas = mutableListOf<String>()
                val formulasArr = json.optJSONArray("formulas_equations")
                if (formulasArr != null) {
                    for (i in 0 until formulasArr.length()) {
                        formulas.add(formulasArr.getString(i))
                    }
                }

                val tips = mutableListOf<String>()
                val tipsArr = json.optJSONArray("exam_tips")
                if (tipsArr != null) {
                    for (i in 0 until tipsArr.length()) {
                        tips.add(tipsArr.getString(i))
                    }
                }

                GeneratedNote(
                    noteId = noteId,
                    topicId = topicId,
                    topicName = topicName,
                    titleOdia = titleOdia.ifBlank { json.optString("title_odia", "ଆଲୋକ ଶ୍ଳେଷଣ ସାରାଂଶ") },
                    titleEnglish = titleEnglish.ifBlank { json.optString("title_english", "Photosynthesis Key Revision Notes") },
                    summaryOdia = summaryOdia,
                    summaryEnglish = summaryEng,
                    keyConcepts = concepts,
                    formulasEquations = formulas,
                    examTips = tips,
                    createdAt = createdAt,
                    isBookmarked = isBookmarked,
                    isOfflineGenerated = json.optBoolean("is_offline_generated", true)
                )
            } catch (_: Exception) {
                // Fallback default note
                GeneratedNote(
                    noteId = noteId,
                    topicId = topicId,
                    topicName = topicName,
                    titleOdia = titleOdia,
                    titleEnglish = titleEnglish,
                    summaryOdia = "ଆଲୋକ ଶ୍ଳେଷଣ ପ୍ରକ୍ରିୟା ଦ୍ୱାରା ସବୁଜ ଉଦ୍ଭିଦ ସୂର୍ଯ୍ୟାଲୋକ ବ୍ୟବହାର କରି ଶର୍କରା ପ୍ରସ୍ତୁତ କରେ।",
                    summaryEnglish = "Photosynthesis enables green plants to synthesize glucose using solar energy and chlorophyll.",
                    keyConcepts = emptyList(),
                    formulasEquations = listOf("6CO₂ + 12H₂O ──> C₆H₁₂O₆ + 6O₂ + 6H₂O"),
                    examTips = listOf("Always mention photolysis of water when asked about oxygen source."),
                    createdAt = createdAt,
                    isBookmarked = isBookmarked,
                    isOfflineGenerated = true
                )
            }
        }

        fun createDemoPhotosynthesisNote(): GeneratedNote {
            return GeneratedNote(
                noteId = "note_photosynthesis_demo_01",
                topicId = "relaynet_class9_science_photosynthesis_v1",
                topicName = "Chapter 4: Photosynthesis (ଆଲୋକ ଶ୍ଳେଷଣ)",
                titleOdia = "ଆଲୋକ ଶ୍ଳେଷଣ ମୁଖ୍ୟ ସାରାଂଶ ଓ ପରୀକ୍ଷା ଟିପ୍ସ",
                titleEnglish = "Photosynthesis Key Revision Notes & Exam Tips",
                summaryOdia = "ଆଲୋକ ଶ୍ଳେଷଣ ହେଉଛି ଏକ ଜୈବ-ରାସାୟନିକ ପ୍ରକ୍ରିୟା ଯେଉଁଥିରେ ସୂର୍ଯ୍ୟାଲୋକ ଏବଂ କ୍ଲୋରୋଫିଲ ସାହାଯ୍ୟରେ ଶର୍କରା ଉତ୍ପନ୍ନ ହୁଏ।",
                summaryEnglish = "Photosynthesis is the biochemical process by which green plants convert light energy into chemical energy, generating glucose and oxygen from water and CO₂.",
                keyConcepts = listOf(
                    NoteConceptPoint(
                        pointOdia = "ଆଲୋକ ପ୍ରକ୍ରିୟା ଥାଇଲାକଏଡ୍ (Thylakoid) ରେ ସଂଘଠିତ ହୋଇ ATP ଓ NADPH ଉତ୍ପନ୍ନ କରେ।",
                        pointEnglish = "Light-dependent reactions occur in the thylakoids producing ATP and NADPH via water photolysis."
                    ),
                    NoteConceptPoint(
                        pointOdia = "ଅନ୍ଧକାର ପ୍ରକ୍ରିୟା ଷ୍ଟ୍ରୋମା (Stroma) ରେ କାର୍ବନ ଡାଇଅକ୍ସାଇଡ୍ ସ୍ଥିରୀକରଣ କରେ।",
                        pointEnglish = "Dark reactions (Calvin Cycle) take place in the stroma to fix CO₂ into glucose."
                    )
                ),
                formulasEquations = listOf(
                    "6CO₂ + 12H₂O ──[Light / Chlorophyll]──> C₆H₁₂O₆ + 6O₂ + 6H₂O",
                    "2H₂O ──[Photolysis]──> 4H⁺ + 4e⁻ + O₂ ↑"
                ),
                examTips = listOf(
                    "Oxygen released during photosynthesis comes entirely from water (H₂O), NOT from carbon dioxide (CO₂).",
                    "Chlorophyll absorbs Blue and Red wavelengths most strongly, reflecting Green light."
                ),
                createdAt = System.currentTimeMillis(),
                isBookmarked = true,
                isOfflineGenerated = true
            )
        }
    }
}
