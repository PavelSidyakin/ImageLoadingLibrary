package com.image_loading_library

import android.graphics.Bitmap

interface ImageDownloader {

    fun load(url: String)

    var progressPlaceHolder: Bitmap?

    var errorPlaceHolder: Bitmap?

}