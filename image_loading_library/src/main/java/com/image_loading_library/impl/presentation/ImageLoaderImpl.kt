package com.image_loading_library.impl.presentation

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.drawable.LayerDrawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.PathShape
import android.util.TypedValue
import android.widget.ImageView
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import com.image_loading_library.ImageLoader
import com.image_loading_library.R
import com.image_loading_library.impl.domain.ImageDownloadInteractor
import com.image_loading_library.impl.model.DownloadProgress
import com.image_loading_library.impl.utils.DispatcherProvider
import com.image_loading_library.impl.utils.logs.log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext
import kotlin.math.max

internal class ImageLoaderImpl

    @Inject
    constructor(
        private val imageDownloadInteractor: ImageDownloadInteractor,
        private val dispatcherProvider: DispatcherProvider
    ) : ImageLoader, CoroutineScope {

    override val coroutineContext: CoroutineContext = dispatcherProvider.io() + Job()

    override var progressPlaceHolder: Bitmap? = null
    override var errorPlaceHolder: Bitmap? = null
    override var progressColor: Int = Color.BLACK

    override var doOnFail: ((throwable: Throwable) -> Unit)? = null
    override var doOnSuccess: (() -> Unit)? = null

    private var imageView: ImageView? = null

    private var currentImageBitmap: Bitmap? = null // To prevent override the same bitmap

    override fun into(imageView: ImageView) {
        this.imageView = imageView
        imageView.setImageDrawable(imageView.context?.resources?.getDrawable(R.drawable.image_with_progress_layer_list, null)?.mutate())

        launch(dispatcherProvider.main()) {
            // Prevent loose of a progress if progressPlaceHolder is not set
            setTransparentImageBitmap()
        }
    }

    override fun load(url: String) {
        if (imageView == null) {
            throw RuntimeException("ImageView is not set. Call into() before call load()")
        }

        launch {
            imageDownloadInteractor.requestImage(url)
                .collect { progress ->

                    when(progress) {
                        is DownloadProgress.Start -> handleDownloadStart(progress)
                        is DownloadProgress.Progress -> handleDownloadProgress(progress)
                        is DownloadProgress.Success -> handleDownloadSuccess(progress)
                        is DownloadProgress.Error-> handleDownloadError(progress)
                    }
                }
        }
    }

    override fun cancel() {
        coroutineContext.cancel()
    }

    private suspend fun handleDownloadStart(start: DownloadProgress.Start) {
        log { i(TAG, "ImageDownloaderImpl.handleDownloadStart(). progress=$start, progressPlaceHolder=$progressPlaceHolder") }

        progressPlaceHolder?.let { setImageBitmap(it) }?:setTransparentImageBitmap()
    }

    private suspend fun handleDownloadProgress(progress: DownloadProgress.Progress) {
        log { i(TAG, "ImageDownloaderImpl.handleDownloadProgress(). progress = [${progress}]") }

        progressPlaceHolder?.let { setImageBitmap(it) }?:setTransparentImageBitmap()
        setProgress(progress.progressPercent)
    }

    private suspend fun handleDownloadError(error: DownloadProgress.Error) {
        log { i(TAG, "ImageDownloaderImpl.handleDownloadError(). error=$error errorPlaceHolder=$errorPlaceHolder") }

        errorPlaceHolder?.let { setImageBitmap(it) }?:setTransparentImageBitmap()
        setProgress(0)
        doOnFail?.invoke(error.throwable)
    }

    private suspend fun handleDownloadSuccess(success: DownloadProgress.Success) {
        log { i(TAG, "ImageDownloaderImpl.handleDownloadSuccess(). success = [${success.bytes.size} bytes]") }

        setProgress(0)
        setImageBitmap(BitmapFactory.decodeByteArray(success.bytes, 0, success.bytes.size))
        doOnSuccess?.invoke()
    }

    private suspend fun setProgress(progress: Int) {
        log { i(TAG, "ImageDownloaderImpl.setProgress(). progress = [${progress}]") }

        withContext(dispatcherProvider.main()) {
            val layerList = imageView?.drawable as LayerDrawable

            var imageWidth = imageView?.run { width } ?: 0
            var imageHeight = imageView?.run { height } ?: 0

            if (imageHeight == 0 || imageWidth == 0) {
                return@withContext
            }

            // Crop to square
            if (imageHeight < imageWidth) imageWidth = imageHeight
            if (imageWidth < imageHeight) imageHeight = imageWidth

            val dpSize = DEFAULT_CIRCLE_STROKE_WIDTH_DP
            val dm = imageView?.context?.resources?.displayMetrics
            var strokeWidth = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpSize.toFloat(), dm).toInt()

            if (strokeWidth <= 0) strokeWidth = 2

            // Make it even to draw arc on the image border
            if (strokeWidth  % 2 != 0) strokeWidth += 1

            val arcLeft = calcArcTopOrLeft(strokeWidth)
            val arcTop = calcArcTopOrLeft(strokeWidth)
            val arcRight = calcArcRightOrBottom(imageWidth, strokeWidth)
            val arcBottom = calcArcRightOrBottom(imageHeight, strokeWidth)

            val path = Path().apply {
                addArc(arcLeft, arcTop, arcRight, arcBottom, -90f, 360 * progress / 100f)
            }

            val pathShape = PathShape(path, imageWidth.toFloat(), imageHeight.toFloat())
            val shapeDrawable = ShapeDrawable(pathShape).apply {
                setBounds(0, 0, imageWidth, imageHeight)
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = strokeWidth.toFloat()
                paint.color = progressColor
            }

            layerList.setDrawableByLayerId(R.id.layer_list_item_progress_ring, shapeDrawable)
            layerList.invalidateSelf()
        }
    }

    private fun calcArcTopOrLeft(strokeWidth: Int): Float {
        return (strokeWidth / 2).toFloat()
    }

    private fun calcArcRightOrBottom(imageSideSize: Int, strokeWidth: Int): Float {
        return (imageSideSize - strokeWidth / 2).toFloat()
    }

    private suspend fun setImageBitmap(bitmap: Bitmap) {
        if (currentImageBitmap == bitmap) {
            return
        }
        currentImageBitmap = bitmap

        withContext(dispatcherProvider.main()) {
            val layerList = imageView?.drawable as LayerDrawable

            val resources = imageView?.context?.resources ?: return@withContext

            val croppedBitmap = cropToSquare(bitmap)
            val roundedDrawable = RoundedBitmapDrawableFactory.create(resources, croppedBitmap)
            roundedDrawable.setAntiAlias(true)
            roundedDrawable.cornerRadius = max(bitmap.width, bitmap.height) / 2.0f

            layerList.setDrawableByLayerId(R.id.layer_list_item_image, roundedDrawable)
            layerList.invalidateSelf()
        }
    }

    private suspend fun setTransparentImageBitmap() {
        setImageBitmap(Bitmap.createBitmap(4, 4, Bitmap.Config.ARGB_8888).apply { eraseColor(Color.TRANSPARENT) })
    }

    private fun cropToSquare(bitmap: Bitmap): Bitmap? {
        val width = bitmap.width
        val height = bitmap.height
        val newWidth = if (height > width) width else height
        val newHeight = if (height > width) height - (height - width) else height
        var cropW = (width - height) / 2
        cropW = if (cropW < 0) 0 else cropW
        var cropH = (height - width) / 2
        cropH = if (cropH < 0) 0 else cropH
        return Bitmap.createBitmap(bitmap, cropW, cropH, newWidth, newHeight)
    }

    companion object {
        private const val TAG = "ImageDownloader"
        private const val DEFAULT_CIRCLE_STROKE_WIDTH_DP = 10
    }

}
