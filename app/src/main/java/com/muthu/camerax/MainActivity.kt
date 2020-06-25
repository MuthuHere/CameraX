package com.muthu.camerax

import android.graphics.Matrix
import android.os.Bundle
import android.os.Environment
import android.util.DisplayMetrics
import android.util.Log.d
import android.util.Rational
import android.util.Size
import android.view.Surface
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import kotlinx.android.synthetic.main.content_main.*
import java.io.File
import kotlin.properties.Delegates

class MainActivity : AppCompatActivity() {


    private var lensFacing: CameraX.LensFacing by Delegates.notNull()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //setup back camera
        lensFacing = CameraX.LensFacing.BACK

        textureView.post {
            startCamera()
        }
// Every time the provided texture view changes, recompute layout
        textureView.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
            updateTransform()
        }

    }

    private fun startCamera() {
        val metrics = DisplayMetrics().also { textureView.display.getRealMetrics(it) }
        val screenSize = Size(metrics.widthPixels, metrics.heightPixels)
        val screenRatio = Rational(metrics.widthPixels, metrics.heightPixels)

        val imageCaptureConfig = ImageCaptureConfig.Builder()
            .apply {
                setLensFacing(lensFacing)
                setTargetAspectRatio(screenRatio)
                setTargetRotation(textureView.display.rotation)
                setCaptureMode(ImageCapture.CaptureMode.MAX_QUALITY)
            }.build()

        val imageCapture = ImageCapture(imageCaptureConfig)

        //preview
        val previewConfig = PreviewConfig.Builder().apply {
            setLensFacing(lensFacing)
            setTargetResolution(screenSize)
            setTargetAspectRatio(screenRatio)
            setTargetRotation(windowManager.defaultDisplay.rotation)
            setTargetRotation(textureView.display.rotation)
        }.build()

        val preview = Preview(previewConfig)
        preview.setOnPreviewOutputUpdateListener {
            textureView.surfaceTexture = it.surfaceTexture
            updateTransform()
        }
        val path = ":/storage/sdcard0/DCIM/"


        ivCapture.setOnClickListener {
//            val file =
//                File(Environment.getExternalStorageState() + "${path}${System.currentTimeMillis()}.jpg")

            val file = File(filesDir, "Camerax/${System.currentTimeMillis()}.jpg")
            if (!file.exists()) {
                file.mkdir()
            }

            imageCapture.takePicture(file, object : ImageCapture.OnImageSavedListener {
                override fun onImageSaved(file: File) {

                }

                override fun onError(
                    useCaseError: ImageCapture.UseCaseError,
                    message: String,
                    cause: Throwable?
                ) {
                    d("mmm ", message)
                }

            })
        }
        CameraX.bindToLifecycle(this, preview, imageCapture)

    }

    private fun updateTransform() {
        val matrix = Matrix()
        val centerX = textureView.width / 2f
        val centerY = textureView.height / 2f

        val rotationDegrees = when (textureView.display.rotation) {
            Surface.ROTATION_0 -> 0
            Surface.ROTATION_90 -> 90
            Surface.ROTATION_180 -> 180
            Surface.ROTATION_270 -> 270
            else -> return
        }
        matrix.postRotate(-rotationDegrees.toFloat(), centerX, centerY)
        textureView.setTransform(matrix)
    }
}