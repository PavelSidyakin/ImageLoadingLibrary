package com.image_loading_library.impl.data

import android.util.Base64
import com.image_loading_library.impl.domain.FileDownloadCacheRepository
import com.image_loading_library.impl.utils.DispatcherProvider
import com.image_loading_library.impl.utils.logs.log
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.zip.GZIPOutputStream
import javax.inject.Inject
import kotlin.text.Charsets.UTF_8

internal class FileDownloadCacheRepositoryImpl

    @Inject
    constructor(
        private val dispatcherProvider: DispatcherProvider
    ): FileDownloadCacheRepository {

    private var cacheDirectory: String = ""

    override fun init(cacheDirectory: String) {
        this.cacheDirectory = cacheDirectory
        File(cacheDirectory).mkdirs()
    }

    override suspend fun putInCache(url: String, imageBytes: ByteArray) {
        withContext(dispatcherProvider.io()) {
            File(makePathForUrl(url)).writeBytes(imageBytes)
        }
    }

    override suspend fun findInCache(url: String): ByteArray? {
        return doInCacheDir { cacheDir ->
            cacheDir.walk().find { file -> file.name.endsWith(encodeUrlForFileName(url)) }?.readBytes()
        }
    }

    override suspend fun getItemCount(): Int {
        return doInCacheDir { cacheDir ->
            cacheDir.listFiles()?.size ?: 0
        }
    }

    override suspend fun getCacheSize(): Long {
        return doInCacheDir { cacheDir ->
            var dirSize = 0L
            cacheDir.walk().forEach { file ->
                dirSize += file.length()
            }

            dirSize
        }
    }

    override suspend fun invalidateCache() {
        doInCacheDir { cacheDir ->
            cacheDir.walk().forEach { file ->
                file.delete()
            }
        }
    }

    override suspend fun removeOldestItem() {
        doInCacheDir { cacheDir ->
            cacheDir.walk().sortedBy { file -> file.name }.first().delete()
        }
    }

    private suspend fun <R> doInCacheDir(block: (cacheDir: File) -> R): R {
        if (cacheDirectory.isBlank()) {
            throw RuntimeException("doInCacheDir() Cache dir is not set. Call init() first.")
        }

        return withContext(dispatcherProvider.io()) {
            block(File(cacheDirectory))
        }
    }

    private fun makePathForUrl(url: String): String {
        if (cacheDirectory.isBlank()) {
            throw RuntimeException("makePathForUrl() Cache dir is not set. Call init() first.")
        }

        return  File(cacheDirectory).resolve(makeFileNameForUrl(url)).toString()
    }

    override fun isInited(): Boolean {
        return cacheDirectory.isNotBlank()
    }

    private fun makeFileNameForUrl(url: String): String {
        return "${System.currentTimeMillis()}_${encodeUrlForFileName(url)}"
    }

    private fun encodeUrlForFileName(url: String): String {
        return Base64.encodeToString(zip(url), Base64.URL_SAFE)
    }

    private fun zip(str: String): ByteArray {
        val bos = ByteArrayOutputStream()
        GZIPOutputStream(bos).bufferedWriter(UTF_8).use { it.write(str) }
        return bos.toByteArray()
    }

    companion object {
        private const val TAG = "FileCacheRepo"
    }

}