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
import com.akmal.maizeleaf.R
import com.akmal.maizeleaf.api.ApiConfig
import com.akmal.maizeleaf.api.ApiService
import com.akmal.maizeleaf.api.ForgotPasswordResponse
import com.akmal.maizeleaf.api.ResetPasswordResponse
import com.akmal.maizeleaf.data.UserPreference
import com.akmal.maizeleaf.data.dataStore
import com.akmal.maizeleaf.databinding.ActivityForgotPasswordBinding
import com.akmal.maizeleaf.databinding.ActivityNewPasswordBinding
import com.akmal.maizeleaf.helper.LoadingAnimator
import com.akmal.maizeleaf.ui.login.LoginActivity
import com.akmal.maizeleaf.ui.otp.VerifyOtpActivity
import com.google.gson.Gson
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import kotlin.jvm.java

class NewPasswordActivity : AppCompatActivity() {
    companion object {
        const val EXTRA_USER_ID = "extra_user_id"
        const val EXTRA_RESET_TOKEN = "extra_reset_token"
    }
    private lateinit var apiService: ApiService
    private lateinit var userPreference: UserPreference
    private lateinit var binding: ActivityNewPasswordBinding
    private lateinit var loadingAnimator: LoadingAnimator
    private var userId: Int = -1
    private var resetToken: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityNewPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        userId = intent.getIntExtra(EXTRA_USER_ID, -1)
        resetToken = intent.getStringExtra(EXTRA_RESET_TOKEN)
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

            val password = binding.passwordEditText.text.toString()
            if (resetToken.isNullOrEmpty()) {
                Toast.makeText(this, "Reset token tidak valid, silakan ulangi proses", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (validateInput(password)) {
                passwordUser( resetToken!!,password)
            }
        }

    }

    private fun validateInput( password: String): Boolean {
        return when {
            password.isEmpty() -> {
                binding.passwordEditText.error = "Password tidak boleh kosong"
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

    private fun passwordUser(tokenreset: String,password: String) {
        lifecycleScope.launch {
            setLoading(true)
            try {
                val response: ResetPasswordResponse = apiService.resetPassword(tokenreset,password)
                if (response.error == false) {
                    Toast.makeText(this@NewPasswordActivity, response.message, Toast.LENGTH_SHORT).show()
                    val intent = Intent(this@NewPasswordActivity, LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this@NewPasswordActivity, response.message, Toast.LENGTH_SHORT).show()
                }
            } catch (e: HttpException) {
                //get error message
                val jsonInString = e.response()?.errorBody()?.string()
                val errorBody = Gson().fromJson(jsonInString, ErrorResponse::class.java)
                val errorMessage = errorBody.message
                Toast.makeText(this@NewPasswordActivity, "Error: $errorMessage", Toast.LENGTH_SHORT).show()
            }catch (e: IOException) {
                Toast.makeText(
                    this@NewPasswordActivity,
                    "Tidak ada koneksi internet. Periksa jaringan Anda.",
                    Toast.LENGTH_SHORT
                ).show()
            }finally {
                setLoading(false)
            }
        }
    }
}