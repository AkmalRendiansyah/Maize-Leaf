package com.akmal.maizeleaf.ui.profile

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import com.akmal.maizeleaf.R
import com.akmal.maizeleaf.data.UserPreference
import com.akmal.maizeleaf.data.dataStore
import com.akmal.maizeleaf.databinding.FragmentHomeBinding
import com.akmal.maizeleaf.databinding.FragmentProfileBinding
import com.akmal.maizeleaf.ui.login.LoginActivity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch


class ProfileFragment : Fragment() {
     private var _binding: FragmentProfileBinding? = null


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
        binding.tvNoLogin.setOnClickListener {
            val intent = Intent(requireContext(), LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

        observeUserData()
        return binding.root
    }
    private fun observeUserData() {
        viewLifecycleOwner.lifecycleScope.launch {
            val user = userPreference.getSession().first()
            if (user.token.isNotEmpty()) {

                binding.nameTextView.text = user.username
                binding.emailTextView.text = user.email

            } else {

            showNoLogin()
        }
        }
    }
    private fun showNoLogin() {
        binding.tvNoLogin.visibility = View.VISIBLE
        binding.nameTextView.visibility = View.GONE
        binding.emailTextView.visibility = View.GONE
        binding.logoutButton.visibility = View.GONE
        binding.nameLabel.visibility = View.GONE
        binding.emailLabel.visibility = View.GONE
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
}