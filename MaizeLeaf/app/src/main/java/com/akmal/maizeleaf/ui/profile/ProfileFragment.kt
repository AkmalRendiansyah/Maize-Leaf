package com.akmal.maizeleaf.ui.profile

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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ProfileFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ProfileFragment : Fragment() {
     private var _binding: FragmentProfileBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private lateinit var userPreference: UserPreference
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        userPreference = UserPreference.getInstance(requireContext().dataStore)
        observeUserData()
        return binding.root
    }
    private fun observeUserData() {
        viewLifecycleOwner.lifecycleScope.launch {
            val user = userPreference.getSession().first()
            binding.nameTextView.text = user.username
            binding.emailTextView.text = user.email
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}