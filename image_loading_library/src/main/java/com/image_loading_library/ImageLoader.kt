package com.image_loading_library

import android.R
import android.widget.ImageView
import com.image_loading_library.impl.di.DaggerImageLoaderComponent
import com.image_loading_library.impl.di.ImageLoaderComponent
import com.image_loading_library.impl.model.DownloadProgress
import com.image_loading_library.impl.utils.logs.log
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.lang.RuntimeException

object ImageLoader {

    internal var imageLoaderComponent: ImageLoaderComponent? = null
        private set

    fun into(imageView: ImageView): ImageDownloader {
        if (imageLoaderComponent == null) {
            imageLoaderComponent = DaggerImageLoaderComponent.builder().build()
        }
        return imageLoaderComponent?.getImageDownloader()?: throw RuntimeException("Component is not initialized")
    }

    fun release() {
        imageLoaderComponent = null
    }

    fun test() {
        if (imageLoaderComponent == null) {
            imageLoaderComponent = DaggerImageLoaderComponent.builder().build()
        }

        GlobalScope.launch {

            imageLoaderComponent?.getDownloader()!!.downloadFile("https://sun1-99.userapi.com/sQbsJVua4h-GtWgHVoQAeSXtRb1YipHTyl9d3w/Lln-df1VZfo.jpg")
                .collect { progress ->

                        log { i("test", "$progress")
                    }
                }




        }


    }
}