package com.image_loading_library.impl.utils.network

import com.image_loading_library.impl.model.DownloadProgress
import com.image_loading_library.impl.utils.DispatcherProvider
import com.image_loading_library.impl.utils.logs.log
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.internal.headersContentLength
import okhttp3.logging.HttpLoggingInterceptor
import java.io.InputStream
import javax.inject.Inject
import kotlin.math.min
import kotlin.math.roundToInt

internal class FileDownloaderImpl

    @Inject
    constructor(private val dispatcherProvider: DispatcherProvider): FileDownloader {

    private val loggingInterceptor = HttpLoggingInterceptor(object : HttpLoggingInterceptor.Logger {
        override fun log(message: String) {
            log { i(TAG, message) }
        }
    }).apply { level = HttpLoggingInterceptor.Level.BODY }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .build()

    override suspend fun downloadFile(url: String): Flow<DownloadProgress> {

        return withContext(dispatcherProvider.io()) {
            return@withContext flow {
                val request = Request.Builder()
                    .url(url)
                    .build()

                val response = okHttpClient.newCall(request).await()

                if (!response.isSuccessful) {
                    log { e(TAG, "DownloaderImpl.downloadFile(). Response failed") }
                    emit(DownloadProgress.Error(RuntimeException("Request failed")))
                    return@flow
                }

                val contentLength = response.headersContentLength()
                log { i(TAG, "DownloaderImpl.downloadFile(). contentLength=$contentLength") }

                emit(DownloadProgress.Start(contentLength))

                val data = ByteArray(contentLength.toInt())

                val bufferSize = calcBufferSize(contentLength)
                log { i(TAG, "DownloaderImpl.downloadFile(). bufferSize=$bufferSize") }

                val bytesStream: InputStream? = response.body?.byteStream()

                if (bytesStream == null) {
                    emit(DownloadProgress.Error(RuntimeException("Empty response")))
                    return@flow
                }

                var offset = 0
                do {
                    val currentRead = bytesStream.read(data, offset, min(bufferSize, contentLength.toInt() - offset - 1))
                    offset += currentRead
                    val progress = (offset * 100f / data.size)
                    log { i(TAG, "DownloaderImpl.downloadFile(). progress=$progress%") }
                    emit(DownloadProgress.Progress(progress.roundToInt()))
                } while (currentRead > 0)

                response.close()
                log { i(TAG, "DownloaderImpl.downloadFile(). downloaded=${data.size}") }
                emit(DownloadProgress.Success(data))
                bytesStream.close()
            }
        }
    }

    private fun calcBufferSize(contentLength: Long): Int {
        if (contentLength < MIN_BUFFER_SIZE_BYTES) {
            return MIN_BUFFER_SIZE_BYTES
        }

        if (contentLength > MAX_BUFFER_SIZE_BYTES) {
            return MAX_BUFFER_SIZE_BYTES
        }

        return (contentLength / 100L).toInt()
    }

    companion object {
        private const val MIN_BUFFER_SIZE_BYTES = 1024
        private const val MAX_BUFFER_SIZE_BYTES = 1024 * 1024

        private const val TAG = "Downloader"
    }
}