package com.nmiet.app.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import com.nmiet.app.domain.model.AppLanguage
import kotlinx.coroutines.flow.StateFlow

data class MultilingualString(
    val english: String,
    val odia: String,
    val hindi: String
) {
    fun getPrimary(language: AppLanguage): String = when (language) {
        AppLanguage.ENGLISH_ONLY -> english
        AppLanguage.ODIA_ONLY -> odia
        AppLanguage.HINDI_ONLY -> hindi
        AppLanguage.BILINGUAL_EN_HI -> english
        AppLanguage.BILINGUAL_EN_OR -> english
    }

    fun getSecondary(language: AppLanguage): String? = when (language) {
        AppLanguage.BILINGUAL_EN_HI -> hindi
        AppLanguage.BILINGUAL_EN_OR -> odia
        else -> null
    }

    fun resolve(language: AppLanguage): String = when (language) {
        AppLanguage.ENGLISH_ONLY -> english
        AppLanguage.ODIA_ONLY -> odia
        AppLanguage.HINDI_ONLY -> hindi
        AppLanguage.BILINGUAL_EN_HI -> "$english ($hindi)"
        AppLanguage.BILINGUAL_EN_OR -> "$english ($odia)"
    }
}

@Composable
fun LocalizedText(
    text: MultilingualString,
    currentLanguageFlow: StateFlow<AppLanguage>,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.bodyMedium,
    secondaryStyle: TextStyle = MaterialTheme.typography.bodySmall.copy(
        color = MaterialTheme.colorScheme.onSurfaceVariant
    ),
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip
) {
    val language by currentLanguageFlow.collectAsState()
    LocalizedText(
        text = text,
        language = language,
        modifier = modifier,
        style = style,
        secondaryStyle = secondaryStyle,
        maxLines = maxLines,
        overflow = overflow
    )
}

@Composable
fun LocalizedText(
    text: MultilingualString,
    language: AppLanguage,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.bodyMedium,
    secondaryStyle: TextStyle = MaterialTheme.typography.bodySmall.copy(
        color = MaterialTheme.colorScheme.onSurfaceVariant
    ),
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip
) {
    val primaryText = text.getPrimary(language)
    val secondaryText = text.getSecondary(language)

    if (secondaryText == null) {
        Text(
            text = primaryText,
            style = style,
            modifier = modifier,
            maxLines = maxLines,
            overflow = overflow
        )
    } else {
        Column(modifier = modifier) {
            Text(
                text = primaryText,
                style = style,
                maxLines = maxLines,
                overflow = overflow
            )
            Text(
                text = secondaryText,
                style = secondaryStyle,
                maxLines = maxLines,
                overflow = overflow
            )
        }
    }
}
