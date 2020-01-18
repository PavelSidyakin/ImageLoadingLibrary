package com.image_loading_library.impl.utils.network

import com.image_loading_library.impl.model.DownloadProgress
import kotlinx.coroutines.flow.Flow

internal interface Downloader {

    suspend fun downloadFile(url: String): Flow<DownloadProgress>

}