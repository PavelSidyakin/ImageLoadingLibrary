package com.image_loading_library.impl.model

internal sealed class DownloadProgress {

    /**
     * Indicates download process start.
     */
    object Start: DownloadProgress()

    /**
     * Indicates download process successfully completed.
     * @param bytes downloaded bytes.
     */
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

    /**
     * Indicates download process failed.
     * @param throwable exception with more information about the error.
     */
    data class Error(val throwable: Throwable): DownloadProgress()

    /**
     * Indicates download process progress.
     * @param progressPercent current progress in percent.
     */
    data class Progress(val progressPercent: Int): DownloadProgress()

}