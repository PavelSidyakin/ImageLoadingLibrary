package com.image_loading_library.impl.data

import com.image_loading_library.impl.domain.ImageDownloadRepository
import com.image_loading_library.impl.domain.ImageDownloadStream
import com.image_loading_library.impl.utils.DispatcherProvider
import kotlinx.coroutines.withContext
import java.io.OutputStream
import javax.inject.Inject

internal class ImageDownloadRepositoryImpl
    @Inject
    constructor(private val dispatcherProvider: DispatcherProvider): ImageDownloadRepository {

    override suspend fun downloadImage(
        url: String,
        out: OutputStream, progressCallback: (bytesTotal: Int, bytesDownloaded: Int) -> Unit
    ) {

    }

}