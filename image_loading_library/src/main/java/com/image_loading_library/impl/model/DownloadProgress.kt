package com.image_loading_library.impl.model

internal sealed class DownloadProgress {

    object Start: DownloadProgress()

    data class GotSize(val totalBytes: Int): DownloadProgress()

    data class Success(val bytes: ByteArray): DownloadProgress() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Success

            if (!bytes.contentEquals(other.bytes)) return false

            return true
        }

        override fun hashCode(): Int {
            return bytes.contentHashCode()
        }
    }

    data class Error(val throwable: Throwable): DownloadProgress()

    data class Progress(val progressPercent: Int): DownloadProgress()

}