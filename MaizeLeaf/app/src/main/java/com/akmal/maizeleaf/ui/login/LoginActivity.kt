package com.akmal.maizeleaf.ui.login

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.akmal.maizeleaf.ErrorResponse
import com.akmal.maizeleaf.MainActivity
import com.akmal.maizeleaf.R
import com.akmal.maizeleaf.api.ApiConfig
import com.akmal.maizeleaf.api.ApiService
import com.akmal.maizeleaf.api.LoginResponse
import com.akmal.maizeleaf.data.UserModel
import com.akmal.maizeleaf.data.UserPreference
import com.akmal.maizeleaf.data.dataStore
import com.akmal.maizeleaf.databinding.ActivityLoginBinding
import com.akmal.maizeleaf.ui.camera.CameraActivity
import com.akmal.maizeleaf.ui.otp.VerifyOtpActivity
import com.akmal.maizeleaf.ui.register.RegisterActivity
import com.google.gson.Gson
import kotlinx.coroutines.launch
import retrofit2.HttpException

class LoginActivity : AppCompatActivity() {
    private lateinit var apiService: ApiService
    private lateinit var userPreference: UserPreference
    private lateinit var binding: ActivityLoginBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        setupView()
        setupAction()
        setupApiService()
        setupUserPreference()

        binding.tvRegister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
        binding.tvQuest.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }
    private fun setupApiService() {

        apiService = ApiConfig.getApiService()
    }
    private fun setupUserPreference() {
        userPreference = UserPreference.getInstance(dataStore)
    }
    private fun setupView() {
        @Suppress("DEPRECATION")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }
        supportActionBar?.hide()
    }
    private fun setupAction() {

        binding.loginButton.setOnClickListener {

            val email = binding.edLoginEmail.text.toString()
            val password = binding.edLoginPassword.text.toString()

            if (validateInput(email)) {
                loginUser( email, password)
            }
        }



    }

    private fun validateInput( email: String): Boolean {
        return when {
            email.isEmpty() -> {
                binding.edLoginEmail.error = "Email tidak boleh kosong"
                false
            }

            else -> true
        }
    }

    private fun loginUser( email: String, password: String) {
        lifecycleScope.launch {
            try {
                Log.d("LoginActivity", "Login data:  email=$email, password=$password")
                val response: LoginResponse = apiService.login( email, password)
                if (response.error == false) {
                    val token = response.loginResult?.token ?: ""
                    Log.d("LoginActivity", "Token berhasil diperoleh: $token")
                    val userModel = UserModel(
                        username = response.loginResult?.name ?: "",
                        email = email,
                        token = token,
                        isLogin = true
                    )
                    userPreference.saveSession(userModel)

                    Toast.makeText(this@LoginActivity, response.message, Toast.LENGTH_SHORT).show()
                    val intent = Intent(this@LoginActivity, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this@LoginActivity, response.message, Toast.LENGTH_SHORT).show()
                }
            } catch (e: HttpException) {
                //get error message
                val jsonInString = e.response()?.errorBody()?.string()
                val errorBody = Gson().fromJson(jsonInString, ErrorResponse::class.java)
                if (e.code() == 403) {
                    val userId = errorBody?.userId
                    Log.d("LoginActivity", "Akun belum terverifikasi, userId=$userId")

                    Toast.makeText(
                        this@LoginActivity,
                        errorBody?.message ?: "Akun belum diverifikasi",
                        Toast.LENGTH_SHORT
                    ).show()

                    val intent = Intent(this@LoginActivity, VerifyOtpActivity::class.java)
                    intent.putExtra(VerifyOtpActivity.EXTRA_USER_ID, userId)
                    intent.putExtra(VerifyOtpActivity.EXTRA_EMAIL, email)
                    intent.putExtra(VerifyOtpActivity.EXTRA_PASSWORD, password)
                    startActivity(intent)
                } else {
                    Toast.makeText(
                        this@LoginActivity,
                        "Error: ${errorBody?.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }
}