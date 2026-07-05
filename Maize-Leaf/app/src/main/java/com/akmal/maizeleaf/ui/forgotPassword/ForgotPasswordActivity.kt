package com.akmal.maizeleaf.ui.forgotPassword

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
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
import com.akmal.maizeleaf.api.ForgotPasswordResponse
import com.akmal.maizeleaf.api.LoginResponse
import com.akmal.maizeleaf.api.RegisterResponse
import com.akmal.maizeleaf.data.UserModel
import com.akmal.maizeleaf.data.UserPreference
import com.akmal.maizeleaf.data.dataStore
import com.akmal.maizeleaf.databinding.ActivityForgotPasswordBinding
import com.akmal.maizeleaf.databinding.ActivityLoginBinding
import com.akmal.maizeleaf.helper.LoadingAnimator
import com.akmal.maizeleaf.ui.login.LoginActivity
import com.akmal.maizeleaf.ui.otp.VerifyOtpActivity
import com.akmal.maizeleaf.ui.register.RegisterActivity
import com.google.gson.Gson
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

class ForgotPasswordActivity : AppCompatActivity() {
    private lateinit var apiService: ApiService
    private lateinit var userPreference: UserPreference
    private lateinit var binding: ActivityForgotPasswordBinding
    private lateinit var loadingAnimator: LoadingAnimator
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityForgotPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        binding.tvBackToLogin.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
        setupView()
        setupAction()
        setupApiService()
        setupUserPreference()
        loadingAnimator = LoadingAnimator(binding.loadingRing, binding.loadingLogo)
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

        binding.btnResetPassword.setOnClickListener {

            val email = binding.edLoginEmail.text.toString()

            if (validateInput(email)) {
                forgotPasswordUser( email)
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
    private fun setLoading(isLoading: Boolean) {
        binding.loadingOverlay.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnResetPassword.isEnabled = !isLoading

        if (isLoading) {
            loadingAnimator.start()
        } else {
            loadingAnimator.stop()
        }
    }

    private fun forgotPasswordUser(email: String) {
        lifecycleScope.launch {
            setLoading(true)
            try {
                val response: ForgotPasswordResponse = apiService.forgotPassword(email)
                if (response.error == false) {
                    Toast.makeText(this@ForgotPasswordActivity, response.message, Toast.LENGTH_SHORT).show()
                    val intent = Intent(this@ForgotPasswordActivity, VerifyOtpActivity::class.java)
                    intent.putExtra(VerifyOtpActivity.EXTRA_OTP_TYPE, VerifyOtpActivity.OtpType.FORGOT_PASSWORD.name)
                    intent.putExtra(VerifyOtpActivity.EXTRA_USER_ID_FORGOT, response.userId)
                    intent.putExtra(VerifyOtpActivity.EXTRA_EMAIL_FORGOT, email)
                    startActivity(intent)
                    Log.d("VerifyOtpForgotActivity", "USER ID = ${response.userId}, EMAIL = $email")
                    finish()
                } else {
                    Toast.makeText(this@ForgotPasswordActivity, response.message, Toast.LENGTH_SHORT).show()
                }
            } catch (e: HttpException) {
                //get error message
                val jsonInString = e.response()?.errorBody()?.string()
                val errorBody = Gson().fromJson(jsonInString, ErrorResponse::class.java)
                val errorMessage = errorBody.message
                Toast.makeText(this@ForgotPasswordActivity, "Error: $errorMessage", Toast.LENGTH_SHORT).show()
            }catch (e: IOException) {
                Toast.makeText(
                    this@ForgotPasswordActivity,
                    "Tidak ada koneksi internet. Periksa jaringan Anda.",
                    Toast.LENGTH_SHORT
                ).show()
            }finally {
                setLoading(false)
            }
        }
    }
}