package com.nmiet.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.nmiet.app.domain.model.AppLanguage
import com.nmiet.app.domain.model.Board
import com.nmiet.app.domain.model.ContentOrigin
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
                grade = profile.gradeInt,
                languagePreference = profile.preferredLanguage.name
            )
        }
    }
}

// LearningPackEntity is defined in LearningPackEntity.kt

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
    val textHindi: String = "",
    val timestamp: Long,
    val isOfflineGrounded: Boolean,
    val sourceChapter: String?,
    val isStudyRelated: Boolean = true,
    val isCloudAI: Boolean = false,
    val sourceTagsJson: String = "[]",
    val isNemotronReasoning: Boolean = false
) {
    fun toDomain(): TutorMessage = TutorMessage(
        id = messageId,
        sender = try { TutorSender.valueOf(senderName) } catch (_: Exception) { TutorSender.USER },
        textOdia = textOdia,
        textEnglish = textEnglish,
        textHindi = textHindi,
        timestamp = timestamp,
        isOfflineGrounded = isOfflineGrounded,
        sourceChapter = sourceChapter,
        isStudyRelated = isStudyRelated,
        isCloudAI = isCloudAI,
        isNemotronReasoning = isNemotronReasoning,
        sourceTags = QuizAttemptEntity.parseTags(sourceTagsJson)
    )

    companion object {
        fun fromDomain(msg: TutorMessage): TutorMessageEntity = TutorMessageEntity(
            messageId = msg.id,
            senderName = msg.sender.name,
            textOdia = msg.textOdia,
            textEnglish = msg.textEnglish,
            textHindi = msg.textHindi,
            timestamp = msg.timestamp,
            isOfflineGrounded = msg.isOfflineGrounded,
            sourceChapter = msg.sourceChapter,
            isStudyRelated = msg.isStudyRelated,
            isCloudAI = msg.isCloudAI,
            isNemotronReasoning = msg.isNemotronReasoning,
            sourceTagsJson = if (msg.sourceTags.isEmpty()) "[]" else JSONArray(msg.sourceTags).toString()
        )
    }
}
