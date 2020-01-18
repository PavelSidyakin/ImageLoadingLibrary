package com.image_loading_library.impl.domain

import com.image_loading_library.impl.model.DownloadProgress
import kotlinx.coroutines.flow.Flow

internal interface ImageDownloadInteractor {

    suspend fun requestImage(url: String): Flow<DownloadProgress>

}