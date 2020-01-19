package com.image_loading_library.impl.domain

import com.image_loading_library.impl.model.DownloadProgress
import kotlinx.coroutines.flow.Flow

internal interface FileDownloader {

    /**
     * Downloads file from provided url.
     *
     * @return Flow with download process. See [DownloadProgress] for more information
     */
    suspend fun downloadFile(url: String): Flow<DownloadProgress>

}