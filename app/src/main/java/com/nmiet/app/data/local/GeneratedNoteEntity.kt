package com.nmiet.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.nmiet.app.domain.model.GeneratedNote

@Entity(tableName = "generated_notes")
data class GeneratedNoteEntity(
    @PrimaryKey
    val noteId: String,
    val topicId: String,
    val topicName: String,
    val titleOdia: String,
    val titleEnglish: String,
    val contentJson: String,
    val createdAt: Long = System.currentTimeMillis(),
    val isBookmarked: Boolean = false,
    val isOfflineGenerated: Boolean = true
) {
    fun toDomain(): GeneratedNote = GeneratedNote.fromJson(
        noteId = noteId,
        topicId = topicId,
        topicName = topicName,
        titleOdia = titleOdia,
        titleEnglish = titleEnglish,
        jsonString = contentJson,
        createdAt = createdAt,
        isBookmarked = isBookmarked
    )

    companion object {
        fun fromDomain(note: GeneratedNote): GeneratedNoteEntity = GeneratedNoteEntity(
            noteId = note.noteId,
            topicId = note.topicId,
            topicName = note.topicName,
            titleOdia = note.titleOdia,
            titleEnglish = note.titleEnglish,
            contentJson = note.toJson(),
            createdAt = note.createdAt,
            isBookmarked = note.isBookmarked,
            isOfflineGenerated = note.isOfflineGenerated
        )
    }
}
