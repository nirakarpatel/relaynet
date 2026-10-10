package com.nmiet.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.nmiet.app.domain.model.Board
import com.nmiet.app.domain.model.KeyTerm
import com.nmiet.app.domain.model.LearningPack
import com.nmiet.app.domain.model.LessonData
import com.nmiet.app.domain.model.LessonSection
import com.nmiet.app.domain.model.QuizAttempt
import com.nmiet.app.domain.model.QuizQuestion
import com.nmiet.app.domain.model.TopicMastery
import com.nmiet.app.domain.model.TutorMessage
import com.nmiet.app.domain.model.TutorSender
import org.json.JSONArray
import org.json.JSONObject

@Entity(tableName = "learning_packs")
data class LearningPackEntity(
    @PrimaryKey
    val packId: String,
    val titleOdia: String,
    val titleEnglish: String,
    val boardName: String,
    val classGrade: Int,
    val subject: String,
    val chapterOdia: String,
    val chapterEnglish: String,
    val topicOdia: String,
    val topicEnglish: String,
    val lessonSummaryOdia: String,
    val lessonSummaryEnglish: String,
    val equationOrFormula: String?,
    val sectionsJson: String,
    val quizQuestionsJson: String,
    val checksumSha256: String,
    val isDownloaded: Boolean = true,
    val isReceivedViaMesh: Boolean = false,
    val peerSenderName: String? = null,
    val sourceAttribution: String
) {
    fun toDomain(): LearningPack {
        val board = try { Board.valueOf(boardName) } catch (_: Exception) { Board.ODISHA_STATE }
        val parsedSections = parseSectionsJson(sectionsJson)
        val parsedQuestions = parseQuestionsJson(quizQuestionsJson)

        return LearningPack(
            packId = packId,
            titleOdia = titleOdia,
            titleEnglish = titleEnglish,
            board = board,
            classGrade = classGrade,
            subject = subject,
            chapterOdia = chapterOdia,
            chapterEnglish = chapterEnglish,
            topicOdia = topicOdia,
            topicEnglish = topicEnglish,
            lesson = LessonData(
                summaryOdia = lessonSummaryOdia,
                summaryEnglish = lessonSummaryEnglish,
                equationOrFormula = equationOrFormula,
                sections = parsedSections
            ),
            quizQuestions = parsedQuestions,
            checksumSha256 = checksumSha256,
            isDownloaded = isDownloaded,
            isReceivedViaMesh = isReceivedViaMesh,
            peerSenderName = peerSenderName,
            sourceAttribution = sourceAttribution
        )
    }

    companion object {
        fun fromDomain(pack: LearningPack): LearningPackEntity {
            return LearningPackEntity(
                packId = pack.packId,
                titleOdia = pack.titleOdia,
                titleEnglish = pack.titleEnglish,
                boardName = pack.board.name,
                classGrade = pack.classGrade,
                subject = pack.subject,
                chapterOdia = pack.chapterOdia,
                chapterEnglish = pack.chapterEnglish,
                topicOdia = pack.topicOdia,
                topicEnglish = pack.topicEnglish,
                lessonSummaryOdia = pack.lesson.summaryOdia,
                lessonSummaryEnglish = pack.lesson.summaryEnglish,
                equationOrFormula = pack.lesson.equationOrFormula,
                sectionsJson = serializeSections(pack.lesson.sections),
                quizQuestionsJson = serializeQuestions(pack.quizQuestions),
                checksumSha256 = pack.checksumSha256,
                isDownloaded = pack.isDownloaded,
                isReceivedViaMesh = pack.isReceivedViaMesh,
                peerSenderName = pack.peerSenderName,
                sourceAttribution = pack.sourceAttribution
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

@Entity(tableName = "quiz_attempts")
data class QuizAttemptEntity(
    @PrimaryKey
    val attemptId: String,
    val topicId: String,
    val topicName: String,
    val scorePercent: Int,
    val correctCount: Int,
    val totalCount: Int,
    val weakConceptsCsv: String,
    val timestamp: Long,
    val isSynced: Boolean = false
) {
    fun toDomain(): QuizAttempt = QuizAttempt(
        id = attemptId,
        topicId = topicId,
        topicName = topicName,
        scorePercent = scorePercent,
        correctCount = correctCount,
        totalCount = totalCount,
        weakConcepts = if (weakConceptsCsv.isBlank()) emptyList() else weakConceptsCsv.split(","),
        timestamp = timestamp,
        isSynced = isSynced
    )
}

@Entity(tableName = "topic_mastery")
data class TopicMasteryEntity(
    @PrimaryKey
    val topicId: String,
    val topicTitleOdia: String,
    val topicTitleEnglish: String,
    val subject: String,
    val masteryScore: Int,
    val lastPracticedDate: Long,
    val attemptsCount: Int,
    val recommendedNextAction: String
) {
    fun toDomain(): TopicMastery = TopicMastery(
        topicId = topicId,
        topicTitleOdia = topicTitleOdia,
        topicTitleEnglish = topicTitleEnglish,
        subject = subject,
        masteryScore = masteryScore,
        lastPracticedDate = lastPracticedDate,
        attemptsCount = attemptsCount,
        recommendedNextAction = recommendedNextAction
    )
}

@Entity(tableName = "tutor_messages")
data class TutorMessageEntity(
    @PrimaryKey
    val messageId: String,
    val senderName: String,
    val textOdia: String,
    val textEnglish: String,
    val timestamp: Long,
    val isOfflineGrounded: Boolean,
    val sourceChapter: String?
) {
    fun toDomain(): TutorMessage = TutorMessage(
        id = messageId,
        sender = try { TutorSender.valueOf(senderName) } catch (_: Exception) { TutorSender.USER },
        textOdia = textOdia,
        textEnglish = textEnglish,
        timestamp = timestamp,
        isOfflineGrounded = isOfflineGrounded,
        sourceChapter = sourceChapter
    )
}
