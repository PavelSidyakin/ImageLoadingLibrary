package com.image_loading_library.impl.domain

import java.io.OutputStream

internal interface ImageDownloadRepository {

    suspend fun downloadImage(url: String,
                              out: OutputStream,
                              progressCallback: (bytesTotal: Int, bytesDownloaded: Int) -> Unit)

}