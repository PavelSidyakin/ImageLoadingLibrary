package com.image_loading_library.impl.presentation

import android.widget.ImageView
import com.image_loading_library.ImageDownloader

internal interface ImageDownloaderInternal: ImageDownloader {

    fun into(imageView: ImageView)

}