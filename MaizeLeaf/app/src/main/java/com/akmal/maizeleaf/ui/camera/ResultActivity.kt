package com.akmal.maizeleaf.ui.camera

import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.akmal.maizeleaf.R
import com.akmal.maizeleaf.databinding.ActivityResultBinding
import com.akmal.maizeleaf.helper.ImageClassifierHelper

class ResultActivity : AppCompatActivity(),ImageClassifierHelper.ClassifierListener {
    private lateinit var binding: ActivityResultBinding
    private lateinit var classifierHelper: ImageClassifierHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val imageUri = Uri.parse(intent.getStringExtra(EXTRA_IMAGE_URI))
        classifierHelper = ImageClassifierHelper(context = this, classifierListener = this)
        imageUri?.let {
            Log.d("Image URI", "showImage: $it")
            binding.resultImage.setImageURI(it)
            classifierHelper.classifyStaticImage(it)
        }
    }
    override fun onResults(resultText: String) {
        binding.resultText.text = resultText  // Menampilkan hasil klasifikasi
    }
    override fun onError(error: String) {
        Log.e("Image Classification", error)
        binding.resultText.text = getString(R.string.error_message, error)
    }



    companion object {
        const val EXTRA_IMAGE_URI = "extra_image_uri"
        const val EXTRA_RESULT = "extra_result"
    }
}