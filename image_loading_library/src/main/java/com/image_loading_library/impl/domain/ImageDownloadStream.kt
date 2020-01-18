package com.image_loading_library.impl.domain

import com.image_loading_library.ImageLoader
import com.image_loading_library.impl.utils.DispatcherProvider
import kotlinx.coroutines.withContext
import java.io.InputStream

internal class ImageDownloadStream(val size: Int, private val inputStream: InputStream) {

    private lateinit var dispatcherProvider: DispatcherProvider

    init {
        ImageLoader.imageLoaderComponent?.inject(this)
    }

    suspend fun nextBytes(): ByteArray {
        return withContext(dispatcherProvider.io()) {
            inputStream.readBytes()
        }

    }
}