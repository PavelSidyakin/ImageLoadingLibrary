package com.image_loading_library.impl.model

internal sealed class DownloadProgress {

    data class Start(val totalBytes: Long): DownloadProgress()

    data class Success(val bytes: ByteArray): DownloadProgress()

    data class Error(val throwable: Throwable): DownloadProgress()

    data class Progress(val progressPercent: Int): DownloadProgress()

}