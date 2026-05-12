package com.akmal.maizeleaf.ui.addPosting

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.akmal.maizeleaf.R
import com.akmal.maizeleaf.databinding.ActivityAddChatBinding
import com.akmal.maizeleaf.databinding.ActivityAddPostingBinding
import com.akmal.maizeleaf.databinding.ActivityPreviewBinding
import com.akmal.maizeleaf.ui.camera.UrlActivity

class AddPostingActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddPostingBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAddPostingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

    }
}