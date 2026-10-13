package com.nmiet.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.nmiet.app.domain.model.AppLanguage
import com.nmiet.app.domain.model.Board
import com.nmiet.app.domain.model.KeyTerm
import com.nmiet.app.domain.model.LearnerProfile
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

@Entity(tableName = "learner_profile")
data class LearnerProfileEntity(
    @PrimaryKey
    val profileId: String = "primary_learner",
    val name: String,
    val board: String,
    val grade: Int,
    val languagePreference: String
) {
    fun toDomain(): LearnerProfile {
        val b = try { Board.valueOf(board) } catch (_: Exception) { Board.ODISHA_STATE }
        val lang = try { AppLanguage.valueOf(languagePreference) } catch (_: Exception) { AppLanguage.BILINGUAL }
        return LearnerProfile(
            studentId = profileId,
            name = name,
            classGrade = grade,
            board = b,
            preferredLanguage = lang
        )
    }

    companion object {
        fun fromDomain(profile: LearnerProfile): LearnerProfileEntity {
            return LearnerProfileEntity(
                profileId = profile.studentId,
                name = profile.name,
                board = profile.board.name,
                grade = profile.classGrade,
                languagePreference = profile.preferredLanguage.name
            )
        }
    }
}

@Entity(tableName = "learning_packs")
data class LearningPackEntity(
    @PrimaryKey
    val packId: String,
    val title: String, // e.g., "Chapter 4: Photosynthesis (ଆଲୋକ ଶ୍ଳେଷଣ)"
    val board: String,
    val grade: Int,
    val odiaContentJson: String,
    val englishContentJson: String,
    val sha256Hash: String,
    val isCached: Boolean = true,
    val titleOdia: String = "",
    val titleEnglish: String = "",
    val subject: String = "",
    val chapterOdia: String = "",
    val chapterEnglish: String = "",
    val topicOdia: String = "",
    val topicEnglish: String = "",
    val equationOrFormula: String? = null,
    val sectionsJson: String = "[]",
    val quizQuestionsJson: String = "[]",
    val sizeBytes: Long = 1887436L, // 1.8 MB footprint
    val isReceivedViaMesh: Boolean = false,
    val peerSenderName: String? = null,
    val sourceAttribution: String = "Odisha State Board (BSE) & NCERT"
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
            chapterOdia = chapterOdia,
            chapterEnglish = chapterEnglish,
            topicOdia = topicOdia,
            topicEnglish = topicEnglish,
            lesson = LessonData(
                summaryOdia = odiaContentJson,
                summaryEnglish = englishContentJson,
                equationOrFormula = equationOrFormula,
                sections = parsedSections
            ),
            quizQuestions = parsedQuestions,
            sizeBytes = sizeBytes,
            checksumSha256 = sha256Hash,
            isDownloaded = isCached,
            isReceivedViaMesh = isReceivedViaMesh,
            peerSenderName = peerSenderName,
            sourceAttribution = sourceAttribution
        )
    }

    companion object {
        fun fromDomain(pack: LearningPack): LearningPackEntity {
            return LearningPackEntity(
                packId = pack.packId,
                title = pack.titleEnglish,
                board = pack.board.name,
                grade = pack.classGrade,
                odiaContentJson = pack.lesson.summaryOdia,
                englishContentJson = pack.lesson.summaryEnglish,
                sha256Hash = pack.checksumSha256,
                isCached = pack.isDownloaded,
                titleOdia = pack.titleOdia,
                titleEnglish = pack.titleEnglish,
                subject = pack.subject,
                chapterOdia = pack.chapterOdia,
                chapterEnglish = pack.chapterEnglish,
                topicOdia = pack.topicOdia,
                topicEnglish = pack.topicEnglish,
                equationOrFormula = pack.lesson.equationOrFormula,
                sectionsJson = serializeSections(pack.lesson.sections),
                quizQuestionsJson = serializeQuestions(pack.quizQuestions),
                sizeBytes = pack.sizeBytes,
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
    val packId: String,
    val score: Int, // 0 to 100 percentage score
    val answersJson: String,
    val misconceptionTagsJson: String,
    val isSynced: Boolean = false,
    val topicName: String = "",
    val correctCount: Int = 0,
    val totalCount: Int = 0,
    val timestamp: Long = System.currentTimeMillis()
) {
    fun toDomain(): QuizAttempt = QuizAttempt(
        id = attemptId,
        topicId = packId,
        topicName = if (topicName.isNotBlank()) topicName else "Chapter 4: Photosynthesis",
        scorePercent = score,
        correctCount = correctCount,
        totalCount = totalCount,
        weakConcepts = parseTags(misconceptionTagsJson),
        timestamp = timestamp,
        isSynced = isSynced
    )

    companion object {
        fun parseTags(jsonStr: String): List<String> {
            if (jsonStr.isBlank()) return emptyList()
            return try {
                val array = JSONArray(jsonStr)
                val list = mutableListOf<String>()
                for (i in 0 until array.length()) list.add(array.getString(i))
                list
            } catch (_: Exception) {
                jsonStr.split(",").filter { it.isNotBlank() }
            }
        }
    }
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
