package com.image_loading_library.impl.domain

import com.image_loading_library.impl.model.DownloadProgress
import kotlinx.coroutines.flow.Flow

internal interface ImageDownloadInteractor {

    /**
     * Requests image. Uses cache.
     *
     * @return Flow with download process. See [DownloadProgress] for more information.
     * Note: if image is found in cache, emits only [DownloadProgress.Success]
     */
    suspend fun requestImage(url: String): Flow<DownloadProgress>

    /**
     * Returns or sets cache directory.
     */
    var cacheDirectory: String

    /**
     * Returns or sets maximum cache size in bytes.
     */
    var maxCacheSizeBytes: Int

    /**
     * Returns or sets maximum number of items in cache.
     */
    var maxNumberOfCachedItems: Int

}