package com.image_loading_library

import com.image_loading_library.impl.di.DaggerImageLoaderComponent
import com.image_loading_library.impl.di.ImageLoaderComponent

class ImageLoadLibrary {

    private val imageLoaderComponent: ImageLoaderComponent by lazy { DaggerImageLoaderComponent.builder().build() }

    fun createImageLoader(): ImageLoader {
        return imageLoaderComponent.getImageDownloader()
    }

    fun initCache(cacheDirectory: String, maxCacheSizeBytes: Int = 10 * 1024 * 1024, maxNumberOfCachedItems: Int = 16) {
        imageLoaderComponent.getImageDownloadInteractor().apply {
            this.cacheDirectory = cacheDirectory
            this.maxCacheSizeBytes = maxCacheSizeBytes
            this.maxNumberOfCachedItems = maxNumberOfCachedItems
        }
    }

}