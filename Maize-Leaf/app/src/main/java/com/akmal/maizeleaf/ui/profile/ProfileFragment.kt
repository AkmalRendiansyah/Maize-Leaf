package com.akmal.maizeleaf.ui.profile

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.akmal.maizeleaf.R
import com.akmal.maizeleaf.data.UserPreference
import com.akmal.maizeleaf.data.dataStore
import com.akmal.maizeleaf.databinding.FragmentHomeBinding
import com.akmal.maizeleaf.databinding.FragmentProfileBinding
import com.akmal.maizeleaf.helper.LoadingAnimator
import com.akmal.maizeleaf.ui.login.LoginActivity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.IOException


class ProfileFragment : Fragment() {
     private var _binding: FragmentProfileBinding? = null
    private lateinit var loadingAnimator: LoadingAnimator
    private val binding get() = _binding!!
    private lateinit var userPreference: UserPreference
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        userPreference = UserPreference.getInstance(requireContext().dataStore)
        binding.logoutButton.setOnClickListener {
            logout()
        }
        loadingAnimator = LoadingAnimator(binding.loadingRing, binding.loadingLogo)
//        binding.tvNoLogin.setOnClickListener {
//            val intent = Intent(requireContext(), LoginActivity::class.java)
//            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
//            startActivity(intent)
//        }

        observeUserData()

        return binding.root
    }
    private fun observeUserData() {
        setLoading(true)
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val user = userPreference.getSession().first()
                if (user.token.isNotEmpty()) {
                    binding.nameTextView.text = user.username
                    binding.emailTextView.text = user.email
                } else {
                    showNoLogin()
                }
            } catch (e: IOException) {
                Toast.makeText(
                    requireContext(),
                    "Tidak ada koneksi internet. Periksa jaringan Anda.",
                    Toast.LENGTH_SHORT
                ).show()
            } finally {
                setLoading(false)
            }
        }
    }

    private fun showNoLogin() {
        binding.layoutNoLogin.visibility = View.VISIBLE

        binding.logoutButton.visibility = View.VISIBLE
        binding.logoutButton.text = getString(R.string.login)
    }
    private fun logout() {
        lifecycleScope.launch {
            userPreference.logout()
            val intent = Intent(requireContext(), LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    private fun setLoading(isLoading: Boolean) {
        binding.loadingOverlay.visibility = if (isLoading) View.VISIBLE else View.GONE
        if (isLoading) {
            loadingAnimator.start()
        } else {
            loadingAnimator.stop()
        }
    }
}