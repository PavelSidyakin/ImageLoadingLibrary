package com.image_loading_library.impl.domain

import com.image_loading_library.impl.model.DownloadProgress
import com.image_loading_library.impl.utils.logs.log
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

internal class ImageDownloadInteractorImpl

    @Inject
    constructor(
        private val fileDownloader: FileDownloader,
        private val fileDownloadCacheRepository: FileDownloadCacheRepository
    ): ImageDownloadInteractor {

    override var cacheDirectory: String = ""

    override var maxCacheSizeBytes: Int = 0
    override var maxNumberOfCachedItems: Int = 0

    override suspend fun requestImage(url: String): Flow<DownloadProgress> {
        log { i(TAG, "ImageDownloadInteractorImpl.requestImage(). url = [${url}]") }

        fileDownloadCacheRepository.init(cacheDirectory)

        if (fileDownloadCacheRepository.isInited()) {
            log { i(TAG, "ImageDownloadInteractorImpl.requestImage(). Try to find in cache...") }
            val cachedImage = fileDownloadCacheRepository.findInCache(url)

            log { i(TAG, "ImageDownloadInteractorImpl.requestImage(). Image is${if (cachedImage != null) " " else " not"}found in cache") }

            if (cachedImage != null) {
                return flow {
                    emit(DownloadProgress.Success(cachedImage))
                }
            }
        }

        return fileDownloader.downloadFile(url).onEach { progress ->
            if (progress is DownloadProgress.Success) {
                if (fileDownloadCacheRepository.isInited()) {
                    putInCache(url, progress.bytes)
                }
            }
        }
    }

    private suspend fun putInCache(url: String, imageBytes: ByteArray) {
        log { i(TAG, "ImageDownloadInteractorImpl.putInCache(). url = [${url}], imageSize = [${imageBytes.size}]") }

        if (maxCacheSizeBytes <= 0 || maxNumberOfCachedItems <= 0) {
            return
        }

        if (maxCacheSizeBytes < imageBytes.size) {
            return
        }

        while (!cacheSpaceAvailable(imageBytes.size) && fileDownloadCacheRepository.getItemCount() > 0) {
            log { i(TAG, "ImageDownloadInteractorImpl.putInCache(). Cache space is not available. Free space...") }
            fileDownloadCacheRepository.removeOldestItem()
        }

        log { i(TAG, "ImageDownloadInteractorImpl.putInCache(). Putting in cache...") }
        fileDownloadCacheRepository.putInCache(url, imageBytes)
    }

    private suspend fun cacheSpaceAvailable(newFileSize: Int): Boolean {
        log { i(TAG, "ImageDownloadInteractorImpl.cacheSpaceAvailable(). newFileSize = [${newFileSize}]") }
        log { i(TAG, "ImageDownloadInteractorImpl.cacheSpaceAvailable(). maxCacheSizeBytes = [${maxCacheSizeBytes}]") }
        log { i(TAG, "ImageDownloadInteractorImpl.cacheSpaceAvailable(). maxNumberOfCachedItems = [${maxNumberOfCachedItems}]") }

        val cacheSize = fileDownloadCacheRepository.getCacheSize()
        val cacheItemCount = fileDownloadCacheRepository.getItemCount()

        log { i(TAG, "ImageDownloadInteractorImpl.cacheSpaceAvailable(). cacheSize = [${cacheSize}]") }
        log { i(TAG, "ImageDownloadInteractorImpl.cacheSpaceAvailable(). cacheItemCount = [${cacheItemCount}]") }

        return cacheSize + newFileSize <= maxCacheSizeBytes
                && cacheItemCount + 1 <= maxNumberOfCachedItems
    }

    companion object {
        private const val TAG="ImageDownloadInteractor"
    }

}