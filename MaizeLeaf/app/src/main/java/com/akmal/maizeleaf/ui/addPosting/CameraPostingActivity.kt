package com.akmal.maizeleaf.ui.addPosting

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.akmal.maizeleaf.R
import com.akmal.maizeleaf.createCustomTempFile
import com.akmal.maizeleaf.databinding.ActivityCameraBinding
import com.akmal.maizeleaf.databinding.ActivityCameraPostingBinding
import com.akmal.maizeleaf.ui.camera.PreviewActivity
import com.akmal.maizeleaf.ui.camera.UrlActivity

class CameraPostingActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCameraPostingBinding
    private lateinit var imageCapture: ImageCapture
    private lateinit var preview: Preview

    private val openGalleryLauncher =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            uri?.let {
                openPreviewActivity(it, false)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityCameraPostingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        startCamera()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets

        }
        binding.buttonOpenGallery.setOnClickListener {
            openGallery()
        }
        binding.buttonTakePicture.setOnClickListener {
            takePhoto()
        }

    }
    private fun takePhoto() {

        val photoFile = createCustomTempFile(this)

        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture.takePicture(
            outputOptions, ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    val savedUri = Uri.fromFile(photoFile)
                    openPreviewActivity(savedUri, true)
                }

                override fun onError(exception: ImageCaptureException) {
                    Toast.makeText(
                        applicationContext,
                        getString(R.string.error_capturing, exception.message),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }
    private fun openGallery() {
        openGalleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }
    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            preview = Preview.Builder()
                .build()

            imageCapture = ImageCapture.Builder()
                .build()

            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                this, CameraSelector.DEFAULT_BACK_CAMERA, preview, imageCapture
            )

            preview.surfaceProvider = binding.textureView.surfaceProvider
        }, ContextCompat.getMainExecutor(this))
    }




    private fun openPreviewActivity(uri: Uri, isFromCamera: Boolean) {
        val intent = Intent(this, PreviewPostingActivity::class.java)
        intent.putExtra("isFromCamera", isFromCamera)
        intent.putExtra("imageUri", uri.toString())
        startActivity(intent)
    }
}