package com.akmal.maizeleaf.ui.camera

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.akmal.maizeleaf.R
import com.akmal.maizeleaf.databinding.ActivityPreviewBinding
import com.akmal.maizeleaf.helper.ImageClassifierHelper
import com.bumptech.glide.Glide
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.net.URL
import kotlin.concurrent.thread

class PreviewActivity : AppCompatActivity(), ImageClassifierHelper.ClassifierListener {

    private lateinit var binding: ActivityPreviewBinding
    private var currentImageUri: Uri? = null
    private var isFromCamera: Boolean = false
    private var progressDialog: AlertDialog? = null
    private lateinit var imageClassifierHelper: ImageClassifierHelper


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPreviewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()

        imageClassifierHelper = ImageClassifierHelper(
            context = this,
            classifierListener = this
        )

        isFromCamera = intent.getBooleanExtra("isFromCamera", false)
        val imageUriString = intent.getStringExtra("imageUri")
        val imageUrlString = intent.getStringExtra("imageUrl")

        if (imageUriString != null) {
            currentImageUri = Uri.parse(imageUriString)
            binding.previewImageView.setImageURI(currentImageUri)
        } else if (imageUrlString != null) {
            Glide.with(this)
                .load(imageUrlString)
                .into(binding.previewImageView)
        }

        if (isFromCamera) {
            binding.previewImageView.scaleType = ImageView.ScaleType.CENTER_CROP
        } else {
            binding.previewImageView.scaleType = ImageView.ScaleType.FIT_CENTER
        }

        binding.buttonRetake.setOnClickListener {
            val intent = Intent(this, CameraActivity::class.java)
            startActivity(intent)
        }

        binding.buttonTakePicture.setOnClickListener {
            when {
                currentImageUri != null -> analyzeImage(currentImageUri!!)
                imageUrlString != null -> analyzeImageFromUrl(imageUrlString)
                else -> Toast.makeText(this, getString(R.string.image_classifier_failed), Toast.LENGTH_SHORT).show()
            }
        }

        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }


    private fun analyzeImage(uri: Uri) {
        try {
            showProgressDialog(getString(R.string.analisis_ulang))
            imageClassifierHelper.classifyStaticImage(uri)
        } catch (e: Exception) {
            hideProgressDialog()
            Toast.makeText(
                this,
                getString(R.string.error_message, e.message), Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun analyzeImageFromUrl(imageUrl: String) {
        showProgressDialog(getString(R.string.analisis_ulang))

        lifecycleScope.launch {
            try {
                val uri = withContext(Dispatchers.IO) {
                    val inputStream = URL(imageUrl).openStream()
                    val tempFile = File.createTempFile("image", ".jpg", cacheDir)
                    tempFile.outputStream().use { output ->
                        inputStream.copyTo(output)
                    }
                    inputStream.close()
                    Uri.fromFile(tempFile)
                }

                currentImageUri = uri
                imageClassifierHelper.classifyStaticImage(uri)

            } catch (e: Exception) {
                Toast.makeText(
                    this@PreviewActivity,
                    "Gagal memuat gambar dari URL: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            } finally {
                hideProgressDialog()
            }
        }
    }

    override fun onResults(resultText: String, confidence: Float) {
        hideProgressDialog()

        if (confidence < 0.80f) {
            showLowConfidenceDialog()
            return
        }

        val intent = Intent(this, ResultActivity::class.java)
        intent.putExtra("resultText", resultText)
        intent.putExtra("imageUri", currentImageUri?.toString())
        intent.putExtra("imageUrl", intent.getStringExtra("imageUrl"))
        startActivity(intent)
    }

    override fun onError(error: String) {
        hideProgressDialog()
        Toast.makeText(this, "Error klasifikasi: $error", Toast.LENGTH_SHORT).show()
    }


    private fun showProgressDialog(message: String) {
        if (progressDialog == null) {
            val builder = AlertDialog.Builder(this)
            builder.setView(R.layout.progress_dialog)
            builder.setCancelable(false)
            builder.setMessage(message)
            progressDialog = builder.create()
        }
        progressDialog?.show()
    }


    private fun hideProgressDialog() {
        progressDialog?.dismiss()
    }

    private fun showLowConfidenceDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_low_detection, null)
        val alertDialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        val tryAgainButton = dialogView.findViewById<Button>(R.id.dialog_try_again_button)
        tryAgainButton.setOnClickListener {
            alertDialog.dismiss()
        }

        alertDialog.show()
    }
}
