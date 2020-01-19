package com.image_loading_library

import com.image_loading_library.impl.di.DaggerImageLoaderComponent
import com.image_loading_library.impl.di.ImageLoaderComponent

class ImageLoadLibrary {

    private val imageLoaderComponent: ImageLoaderComponent by lazy { DaggerImageLoaderComponent.builder().build() }

    /**
     *  Creates [ImageLoader], which can be used for bind image to url.
     *
     *  @return [ImageLoader] instance
     */
    fun createImageLoader(): ImageLoader {
        return imageLoaderComponent.getImageDownloader()
    }

    /**
     * Initializes cache.
     * If is not called, cache is not used and all images are downloaded always.
     *
     * @param cacheDirectory absolute path to cache directory.
     * @param maxCacheSizeBytes maximum cache size in bytes.
     * @param maxNumberOfCachedItems maximum number of items in cache.
     *
     */
    fun initCache(cacheDirectory: String, maxCacheSizeBytes: Int = 10 * 1024 * 1024, maxNumberOfCachedItems: Int = 16) {
        imageLoaderComponent.getImageDownloadInteractor().apply {
            this.cacheDirectory = cacheDirectory
            this.maxCacheSizeBytes = maxCacheSizeBytes
            this.maxNumberOfCachedItems = maxNumberOfCachedItems
        }
    }

}