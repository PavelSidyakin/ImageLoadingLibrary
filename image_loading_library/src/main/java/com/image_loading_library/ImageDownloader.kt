package com.image_loading_library

import android.graphics.Bitmap

interface ImageDownloader {

    fun load(url: String)

    fun cancel()

    var progressPlaceHolder: Bitmap?

    var errorPlaceHolder: Bitmap?

    var progressColor: Int

}