package com.image_loading_library.impl.domain

import com.image_loading_library.impl.model.DownloadProgress
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

internal class ImageDownloadInteractorImpl

    @Inject
    constructor(
        private val fileDownloader: FileDownloader
    ): ImageDownloadInteractor {

    override suspend fun requestImage(url: String): Flow<DownloadProgress> {
        return fileDownloader.downloadFile(url)
    }
}