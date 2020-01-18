package com.image_loading_library.impl.domain

import com.image_loading_library.impl.model.DownloadProgress
import kotlinx.coroutines.flow.Flow

internal interface FileDownloader {

    suspend fun downloadFile(url: String): Flow<DownloadProgress>

}