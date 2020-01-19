package com.image_loading_library

import android.graphics.Bitmap
import android.widget.ImageView

interface ImageLoader {

    fun into(imageView: ImageView)

    fun load(url: String)

    fun cancel()

    var doOnSuccess: (() -> Unit)?

    var doOnFail: ((throwable: Throwable) -> Unit)?

    var progressPlaceHolder: Bitmap?

    var errorPlaceHolder: Bitmap?

    var progressColor: Int

}