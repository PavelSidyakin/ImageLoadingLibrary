package com.image_loading_library

import android.widget.ImageView
import com.image_loading_library.impl.di.DaggerImageLoaderComponent
import com.image_loading_library.impl.di.ImageLoaderComponent

object ImageLoader {

    private var imageLoaderComponent: ImageLoaderComponent? = null

    fun into(imageView: ImageView): ImageDownloader {
        if (imageLoaderComponent == null) {
            imageLoaderComponent = DaggerImageLoaderComponent.builder().build()
        }
        return imageLoaderComponent?.getImageDownloaderInternal()
            .apply { this?.into(imageView) }
            ?: throw RuntimeException("Component is not initialized")
    }

    fun release() {
        imageLoaderComponent = null
    }


}