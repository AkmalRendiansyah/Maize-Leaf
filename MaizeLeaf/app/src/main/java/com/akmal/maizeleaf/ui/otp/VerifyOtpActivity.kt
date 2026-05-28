package com.akmal.maizeleaf.ui.otp

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.widget.EditText
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
import com.akmal.maizeleaf.data.UserModel
import com.akmal.maizeleaf.data.UserPreference
import com.akmal.maizeleaf.data.dataStore
import com.akmal.maizeleaf.databinding.ActivityVerifyOtpBinding
import com.google.gson.Gson
import kotlinx.coroutines.launch
import retrofit2.HttpException

class VerifyOtpActivity : AppCompatActivity() {

    private lateinit var binding: ActivityVerifyOtpBinding
    private lateinit var otpBoxes: List<EditText>
    private lateinit var userPreference: UserPreference
    private var countDownTimer: CountDownTimer? = null
    private var userId: Int = -1

    companion object {
        const val EXTRA_USER_ID = "extra_user_id"
        const val EXTRA_EMAIL   = "extra_email"
        const val EXTRA_PASSWORD = "extra_password"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityVerifyOtpBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        supportActionBar?.hide()
        userPreference = UserPreference.getInstance(dataStore)

        userId = intent.getIntExtra(EXTRA_USER_ID, -1)
        val email = intent.getStringExtra(EXTRA_EMAIL)
        Log.d("VerifyOtpActivity", "USER ID = $userId, EMAIL = $email")

        email?.let { binding.tvEmail.text = "\n$it" }

        otpBoxes = listOf(
            binding.etOtp1, binding.etOtp2, binding.etOtp3,
            binding.etOtp4, binding.etOtp5, binding.etOtp6
        )

        setupOtpInputs()
        startResendCountdown()

        binding.btnVerify.setOnClickListener {
            val otp = otpBoxes.joinToString("") { it.text.toString() }.toInt()
            verifyOtp(otp)
        }

        binding.tvResendOtp.setOnClickListener {
            resendOtp()
        }
    }

    private fun setupOtpInputs() {
        otpBoxes.forEachIndexed { index, editText ->
            editText.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    if (s?.length == 1) {
                        if (index < otpBoxes.size - 1) otpBoxes[index + 1].requestFocus()
                        else editText.clearFocus()
                    }
                    updateVerifyButton()
                }
            })

            editText.setOnKeyListener { _, keyCode, event ->
                if (keyCode == KeyEvent.KEYCODE_DEL &&
                    event.action == KeyEvent.ACTION_DOWN &&
                    editText.text.isEmpty()
                ) {
                    if (index > 0) {
                        otpBoxes[index - 1].apply {
                            requestFocus()
                            text?.clear()
                        }
                    }
                    true
                } else false
            }
        }
    }

    private fun updateVerifyButton() {
        binding.btnVerify.isEnabled = otpBoxes.all { it.text.length == 1 }
    }

    private fun verifyOtp(otp: Int) {
        if (userId == -1) {
            Toast.makeText(this, "User ID tidak valid", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                // userId dikirim sebagai String sesuai signature ApiService
                val response = ApiConfig.getApiService().verifyOtp(
                    userId = userId,
                    otp    = otp
                )
                if (response.error == false) {
                    Toast.makeText(
                        this@VerifyOtpActivity,
                        response.message ?: "Akun berhasil diverifikasi",
                        Toast.LENGTH_SHORT
                    ).show()
                    val token = response.token
                    Log.d("LoginActivity", "Token berhasil diperoleh: $token")
                    val email = intent.getStringExtra(EXTRA_EMAIL) ?: ""
                    val userModel = UserModel(
                        username =  "",
                        email = email,
                        token = token.toString(),
                        isLogin = true
                    )
                    userPreference.saveSession(userModel)
                    val intent = Intent(this@VerifyOtpActivity, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()// kembali ke LoginActivity
                } else {
                    Toast.makeText(this@VerifyOtpActivity, response.message, Toast.LENGTH_SHORT).show()
                    clearOtpBoxes()
                }
            } catch (e: HttpException) {
                val json      = e.response()?.errorBody()?.string()
                val errorBody = Gson().fromJson(json, ErrorResponse::class.java)
                Toast.makeText(
                    this@VerifyOtpActivity,
                    "Error: ${errorBody?.message}",
                    Toast.LENGTH_SHORT
                ).show()
                clearOtpBoxes()
            }
        }
    }

    private fun resendOtp() {
        if (userId == -1) {
            Toast.makeText(this, "User ID tidak valid", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                val response = ApiConfig.getApiService().resendOtp(
                    userId = userId
                )
                if (response.error == false) {
                    clearOtpBoxes()
                    Toast.makeText(
                        this@VerifyOtpActivity,
                        response.message ?: "Kode OTP baru telah dikirim",
                        Toast.LENGTH_SHORT
                    ).show()
                    startResendCountdown()
                } else {
                    Toast.makeText(this@VerifyOtpActivity, response.message, Toast.LENGTH_SHORT).show()
                }
            } catch (e: HttpException) {
                val json      = e.response()?.errorBody()?.string()
                val errorBody = Gson().fromJson(json, ErrorResponse::class.java)
                Toast.makeText(
                    this@VerifyOtpActivity,
                    "Error: ${errorBody?.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun clearOtpBoxes() {
        otpBoxes.forEach { it.text?.clear() }
        otpBoxes.first().requestFocus()
        updateVerifyButton()
    }

    private fun startResendCountdown() {
        binding.resendContainer.visibility       = View.VISIBLE
        binding.resendButtonContainer.visibility = View.GONE

        countDownTimer?.cancel()
        countDownTimer = object : CountDownTimer(30_000L, 1_000L) {
            override fun onTick(millisUntilFinished: Long) {
                binding.tvCountdown.text = "${millisUntilFinished / 1000}s"
            }
            override fun onFinish() {
                binding.resendContainer.visibility       = View.GONE
                binding.resendButtonContainer.visibility = View.VISIBLE
            }
        }.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        countDownTimer?.cancel()
    }
}