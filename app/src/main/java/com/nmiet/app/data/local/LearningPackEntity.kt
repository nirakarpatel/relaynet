package com.nmiet.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.nmiet.app.domain.model.Board
import com.nmiet.app.domain.model.ContentOrigin
import com.nmiet.app.domain.model.KeyTerm
import com.nmiet.app.domain.model.LearningPack
import com.nmiet.app.domain.model.LessonData
import com.nmiet.app.domain.model.LessonSection
import com.nmiet.app.domain.model.QuizQuestion
import org.json.JSONArray
import org.json.JSONObject

@Entity(tableName = "learning_packs")
data class LearningPackEntity(
    @PrimaryKey
    val packId: String,
    val titleOdia: String,
    val titleEnglish: String,
    val subject: String,
    val chapterNumber: Int = 4,
    val origin: ContentOrigin = ContentOrigin.CLOUD_DOWNLOADED,
    val senderPeerName: String? = null,
    val receivedTimestamp: Long = System.currentTimeMillis(),
    val sha256Checksum: String = "",
    val isCachedLocally: Boolean = true,
    val board: String = "ODISHA_STATE",
    val grade: Int = 9,
    val title: String = "",
    val odiaContentJson: String = "",
    val englishContentJson: String = "",
    val chapterOdia: String = "",
    val chapterEnglish: String = "",
    val topicOdia: String = "",
    val topicEnglish: String = "",
    val equationOrFormula: String? = null,
    val sectionsJson: String = "[]",
    val quizQuestionsJson: String = "[]",
    val sizeBytes: Long = 1887436L,
    val sourceAttribution: String = "Odisha State Board (BSE) & NCERT",
    val diagramLocalPath: String? = null
) {
    fun toDomain(): LearningPack {
        val boardEnum = try { Board.valueOf(board) } catch (_: Exception) { Board.ODISHA_STATE }
        val parsedSections = parseSectionsJson(sectionsJson)
        val parsedQuestions = parseQuestionsJson(quizQuestionsJson)

        return LearningPack(
            packId = packId,
            titleOdia = if (titleOdia.isNotBlank()) titleOdia else "Chapter 4: Photosynthesis (ଆଲୋକ ଶ୍ଳେଷଣ)",
            titleEnglish = if (titleEnglish.isNotBlank()) titleEnglish else title,
            board = boardEnum,
            classGrade = grade,
            subject = if (subject.isNotBlank()) subject else "Science (ବିଜ୍ଞାନ)",
            chapterNumber = chapterNumber,
            chapterOdia = chapterOdia,
            chapterEnglish = chapterEnglish,
            topicOdia = topicOdia,
            topicEnglish = topicEnglish,
            origin = origin,
            receivedTimestamp = receivedTimestamp,
            lesson = LessonData(
                summaryOdia = odiaContentJson,
                summaryEnglish = englishContentJson,
                equationOrFormula = equationOrFormula,
                sections = parsedSections
            ),
            quizQuestions = parsedQuestions,
            sizeBytes = sizeBytes,
            checksumSha256 = sha256Checksum.ifEmpty { "b8a923fc817293b8cae29471f08d1354890bf230a108b98e1fae9cf19a3b6f98" },
            isDownloaded = isCachedLocally,
            isReceivedViaMesh = (origin == ContentOrigin.P2P_RECEIVED || origin == ContentOrigin.P2P_SENT),
            peerSenderName = senderPeerName,
            sourceAttribution = sourceAttribution,
            diagramLocalPath = diagramLocalPath
        )
    }

    companion object {
        fun fromDomain(pack: LearningPack): LearningPackEntity {
            return LearningPackEntity(
                packId = pack.packId,
                titleOdia = pack.titleOdia,
                titleEnglish = pack.titleEnglish,
                subject = pack.subject,
                chapterNumber = pack.chapterNumber,
                origin = pack.origin,
                senderPeerName = pack.peerSenderName,
                receivedTimestamp = pack.receivedTimestamp,
                sha256Checksum = pack.checksumSha256,
                isCachedLocally = pack.isDownloaded,
                board = pack.board.name,
                grade = pack.classGrade,
                title = pack.titleEnglish,
                odiaContentJson = pack.lesson.summaryOdia,
                englishContentJson = pack.lesson.summaryEnglish,
                chapterOdia = pack.chapterOdia,
                chapterEnglish = pack.chapterEnglish,
                topicOdia = pack.topicOdia,
                topicEnglish = pack.topicEnglish,
                equationOrFormula = pack.lesson.equationOrFormula,
                sectionsJson = serializeSections(pack.lesson.sections),
                quizQuestionsJson = serializeQuestions(pack.quizQuestions),
                sizeBytes = pack.sizeBytes,
                sourceAttribution = pack.sourceAttribution,
                diagramLocalPath = pack.diagramLocalPath
            )
        }

        private fun serializeSections(sections: List<LessonSection>): String {
            val array = JSONArray()
            sections.forEach { sec ->
                val obj = JSONObject().apply {
                    put("titleOdia", sec.titleOdia)
                    put("titleEnglish", sec.titleEnglish)
                    put("contentOdia", sec.contentOdia)
                    put("contentEnglish", sec.contentEnglish)
                    val termsArray = JSONArray()
                    sec.keyTerms.forEach { term ->
                        termsArray.put(JSONObject().apply {
                            put("termOdia", term.termOdia)
                            put("termEnglish", term.termEnglish)
                            put("defOdia", term.definitionOdia)
                            put("defEng", term.definitionEnglish)
                        })
                    }
                    put("keyTerms", termsArray)
                }
                array.put(obj)
            }
            return array.toString()
        }

        private fun parseSectionsJson(jsonStr: String): List<LessonSection> {
            val list = mutableListOf<LessonSection>()
            try {
                val array = JSONArray(jsonStr)
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    val terms = mutableListOf<KeyTerm>()
                    if (obj.has("keyTerms")) {
                        val tArray = obj.getJSONArray("keyTerms")
                        for (j in 0 until tArray.length()) {
                            val tObj = tArray.getJSONObject(j)
                            terms.add(
                                KeyTerm(
                                    termOdia = tObj.getString("termOdia"),
                                    termEnglish = tObj.getString("termEnglish"),
                                    definitionOdia = tObj.getString("defOdia"),
                                    definitionEnglish = tObj.getString("defEng")
                                )
                            )
                        }
                    }
                    list.add(
                        LessonSection(
                            titleOdia = obj.getString("titleOdia"),
                            titleEnglish = obj.getString("titleEnglish"),
                            contentOdia = obj.getString("contentOdia"),
                            contentEnglish = obj.getString("contentEnglish"),
                            keyTerms = terms
                        )
                    )
                }
            } catch (_: Exception) {}
            return list
        }

        private fun serializeQuestions(questions: List<QuizQuestion>): String {
            val array = JSONArray()
            questions.forEach { q ->
                val obj = JSONObject().apply {
                    put("id", q.id)
                    put("questionOdia", q.questionOdia)
                    put("questionEnglish", q.questionEnglish)
                    put("optionsOdia", JSONArray(q.optionsOdia))
                    put("optionsEnglish", JSONArray(q.optionsEnglish))
                    put("correctIndex", q.correctIndex)
                    put("explanationOdia", q.explanationOdia)
                    put("explanationEnglish", q.explanationEnglish)
                    put("conceptTag", q.conceptTag)
                    put("misconceptionHintOdia", q.misconceptionHintOdia)
                    put("misconceptionHintEnglish", q.misconceptionHintEnglish)
                }
                array.put(obj)
            }
            return array.toString()
        }

        private fun parseQuestionsJson(jsonStr: String): List<QuizQuestion> {
            val list = mutableListOf<QuizQuestion>()
            try {
                val array = JSONArray(jsonStr)
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    val optOdia = mutableListOf<String>()
                    val oOdia = obj.getJSONArray("optionsOdia")
                    for (k in 0 until oOdia.length()) optOdia.add(oOdia.getString(k))

                    val optEng = mutableListOf<String>()
                    val oEng = obj.getJSONArray("optionsEnglish")
                    for (k in 0 until oEng.length()) optEng.add(oEng.getString(k))

                    list.add(
                        QuizQuestion(
                            id = obj.getString("id"),
                            questionOdia = obj.getString("questionOdia"),
                            questionEnglish = obj.getString("questionEnglish"),
                            optionsOdia = optOdia,
                            optionsEnglish = optEng,
                            correctIndex = obj.getInt("correctIndex"),
                            explanationOdia = obj.getString("explanationOdia"),
                            explanationEnglish = obj.getString("explanationEnglish"),
                            conceptTag = obj.getString("conceptTag"),
                            misconceptionHintOdia = obj.getString("misconceptionHintOdia"),
                            misconceptionHintEnglish = obj.getString("misconceptionHintEnglish")
                        )
                    )
                }
            } catch (_: Exception) {}
            return list
        }
    }
}
