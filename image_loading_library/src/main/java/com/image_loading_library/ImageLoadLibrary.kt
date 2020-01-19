package com.image_loading_library

import com.image_loading_library.impl.di.DaggerImageLoaderComponent
import com.image_loading_library.impl.di.ImageLoaderComponent

class ImageLoadLibrary {

    private var imageLoaderComponent: ImageLoaderComponent? = null

    fun createImageLoader(): ImageLoader {
        if (imageLoaderComponent == null) {
            imageLoaderComponent = DaggerImageLoaderComponent.builder().build()
        }

        return imageLoaderComponent?.getImageDownloader()?: throw RuntimeException("Component is not initialized")
    }

}