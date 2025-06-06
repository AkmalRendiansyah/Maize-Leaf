package com.akmal.maizeleaf.ui.register

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
import com.akmal.maizeleaf.R
import com.akmal.maizeleaf.api.ApiConfig
import com.akmal.maizeleaf.api.ApiService
import com.akmal.maizeleaf.api.RegisterResponse
import com.akmal.maizeleaf.data.UserModel
import com.akmal.maizeleaf.data.UserPreference
import com.akmal.maizeleaf.data.dataStore
import com.akmal.maizeleaf.databinding.ActivityRegisterBinding
import com.akmal.maizeleaf.ui.login.LoginActivity
import com.google.gson.Gson
import kotlinx.coroutines.launch
import retrofit2.HttpException

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private lateinit var apiService: ApiService
    private lateinit var userPreference: UserPreference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        binding.tvLogin.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
        setupView()
        setupAction()
        setupApiService()
        setupUserPreference()
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

        binding.signupButton.setOnClickListener {
            val name = binding.edRegisterName.text.toString()
            val email = binding.emailEditText.text.toString()
            val password = binding.passwordEditText.text.toString()

            if (validateInput(name, email)) {
                registerUser(name, email, password)
            }
        }

    }

    private fun validateInput(name: String, email: String): Boolean {
        return when {
            name.isEmpty() -> {
                binding.edRegisterName.error = "Nama tidak boleh kosong"
                false
            }
            email.isEmpty() -> {
                binding.emailEditText.error = "Email tidak boleh kosong"
                false
            }

            else -> true
        }
    }


    private fun registerUser(name: String, email: String, password: String) {
        lifecycleScope.launch {
            try {
                Log.d("SignupActivity", "Register data: name=$name, email=$email, password=$password")
                val response: RegisterResponse = apiService.register(name, email, password)
                if (response.error == false) {

                    val userModel = UserModel(
                        username = name,
                        email = email,
                        token = "TOKEN",
                        isLogin = true
                    )
                    userPreference.saveSession(userModel)

                    Toast.makeText(this@RegisterActivity, response.message, Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this@RegisterActivity, response.message, Toast.LENGTH_SHORT).show()
                }
            } catch (e: HttpException) {
                //get error message
                val jsonInString = e.response()?.errorBody()?.string()
                val errorBody = Gson().fromJson(jsonInString, ErrorResponse::class.java)
                val errorMessage = errorBody.message
                Toast.makeText(this@RegisterActivity, "Error: $errorMessage", Toast.LENGTH_SHORT).show()
            }
        }
    }


}