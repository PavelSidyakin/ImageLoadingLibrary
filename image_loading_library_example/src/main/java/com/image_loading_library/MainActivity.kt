package com.image_loading_library

import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    private val testImageUrls = listOf(
        // Big
        "https://images.unsplash.com/photo-1538086092015-4102b173037a?ixlib=rb-1.2.1&q=85&fm=jpg&crop=entropy&cs=srgb&dl=matteo-grassi-iWD-XMEk9-4-unsplash.jpg",
        "https://images.unsplash.com/photo-1547888413-5c8a8871d4bd?ixlib=rb-1.2.1&q=85&fm=jpg&crop=entropy&cs=srgb&dl=olivia-spink-8ulylIUfTuQ-unsplash.jpg",

        // Medium
        "https://sun9-51.userapi.com/c543103/v543103268/7a95/LR3N1cOvXZg.jpg",
        "https://sun9-21.userapi.com/c638028/v638028293/50c9e/ensRvNkE_1Y.jpg",

        // Small
        "https://cdn1.iconfinder.com/data/icons/animal-flat-2/128/animal_fox-forest-256.png",
        "https://www.redfox.bz/img/products/anydvdhd-200.png",
        "https://cdn.shopify.com/s/files/1/0016/5173/6636/t/2/assets/favicon-32x32.png"
    )

    private var currentImageIndex = 0
    private var currentImageLoader: ImageLoader? = null
    private val imageLoadLibrary: ImageLoadLibrary = ImageLoadLibrary()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        imageLoadLibrary.initCache(application.cacheDir.absolutePath)

        loadNextImage()

        main_activity_button_next_image.setOnClickListener {
            loadNextImage()
        }
    }

    private fun loadNextImage() {
        currentImageLoader?.cancel()

        currentImageLoader = imageLoadLibrary.createImageLoader().apply {
            into(main_activity_image_with_progress)
            progressPlaceHolder = getBitmapFromVectorDrawable(R.drawable.progress_placeholder)
            errorPlaceHolder = getBitmapFromVectorDrawable(R.drawable.error_placeholder)
            progressColor = resources.getColor(android.R.color.holo_green_dark)
            doOnFail = { currentImageLoader = null }
            doOnSuccess = { currentImageLoader = null }
            load(testImageUrls[currentImageIndex++])
        }
        if (currentImageIndex > testImageUrls.lastIndex) {
            currentImageIndex = 0
        }
    }

    private fun getBitmapFromVectorDrawable(drawableId: Int): Bitmap? {
        val drawable = ContextCompat.getDrawable(this, drawableId)
        return drawable?.let {

            val bitmap = Bitmap.createBitmap(
                drawable.intrinsicWidth,
                drawable.intrinsicHeight, Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
            bitmap
        }
    }
}
