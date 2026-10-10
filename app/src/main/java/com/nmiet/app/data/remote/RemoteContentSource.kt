package com.nmiet.app.data.remote

import com.nmiet.app.domain.model.ContentItem

interface RemoteContentSource {
    suspend fun fetchLatestContent(): Result<List<ContentItem>>
}
