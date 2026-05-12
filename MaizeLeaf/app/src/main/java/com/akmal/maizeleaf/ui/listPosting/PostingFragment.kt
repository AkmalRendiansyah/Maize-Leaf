package com.akmal.maizeleaf.ui.listPosting

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.datastore.preferences.preferencesDataStore
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.akmal.maizeleaf.R
import com.akmal.maizeleaf.api.ApiConfig
import com.akmal.maizeleaf.api.ApiService
import com.akmal.maizeleaf.data.UserPreference
import com.akmal.maizeleaf.databinding.ActivityAddChatBinding
import com.akmal.maizeleaf.databinding.FragmentHistoryBinding
import com.akmal.maizeleaf.databinding.FragmentPostingBinding
import com.akmal.maizeleaf.ui.addPosting.CameraPostingActivity
import com.akmal.maizeleaf.ui.history.HistoryAdapter
import com.akmal.maizeleaf.ui.history.HistoryViewModel
import com.akmal.maizeleaf.ui.login.LoginActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton



private val Context.dataStore by preferencesDataStore(name = "session")

class PostingFragment : Fragment() {
    private var _binding: FragmentPostingBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: PostingViewModel
    private lateinit var adapter: PostingAdapter
    private lateinit var userPreference: UserPreference
    private lateinit var apiService: ApiService

    private var userToken: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?

    ): View? {
        _binding = FragmentPostingBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        userPreference = UserPreference.getInstance(requireContext().dataStore)
        apiService = ApiConfig.getApiService()
        viewModel = PostingViewModel(userPreference, apiService)

        setupRecyclerView()

        viewModel.getSession().observe(viewLifecycleOwner) { user ->
            val token = user.token
            userToken = token
            if (token.isNotEmpty()) {
                binding.tvNoLogin.visibility = View.GONE
                viewModel.getPosting(token)
            } else {
                showNoLogin()
            }
        }

        viewModel.postingList.observe(viewLifecycleOwner) { postingList ->
            if (postingList.isNullOrEmpty()) {
                showNoHistory()
            } else {
                binding.tvNoPosting.visibility = View.GONE
                binding.rvPosting.visibility = View.VISIBLE
                adapter.submitList(postingList)
            }
        }
        binding.tvNoLogin.setOnClickListener {
            val intent = Intent(requireContext(), LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            requireActivity().finish()
        }


        val addChat = view.findViewById<FloatingActionButton>(R.id.komentar_add)
        addChat.setOnClickListener {
            val intent = Intent(requireContext(), CameraPostingActivity::class.java)
            startActivity(intent)
        }
    }


    private fun setupRecyclerView() {
        adapter = PostingAdapter(
            onPostingClick  = { item ->

                // Klik item bisa diisi jika dibutuhkan
            },
//            onPostingClick = { item ->
//                item.id?.let { historyId ->
//                    showDeleteConfirmationDialog(historyId)
//                }
//            }

        )
        binding.rvPosting.layoutManager = LinearLayoutManager(requireContext())
        binding.rvPosting.adapter = adapter
    }

//    private fun showDeleteConfirmationDialog(historyId: Int) {
//        val context = requireContext()
//        val builder = androidx.appcompat.app.AlertDialog.Builder(context)
//        builder.setTitle(getString(R.string.delete_alert))
//            .setMessage(getString(R.string.are_you_sure_delete))
//            .setPositiveButton(getString(R.string.yes)) { _, _ ->
//                userToken?.let { token ->
//                    viewModel.deleteHistory(token, historyId) { success ->
//                        if (!success) {
//                        }
//                    }
//                }
//            }
//            .setNegativeButton(getString(R.string.no)) { dialog, _ ->
//                dialog.dismiss()
//            }
//        builder.create().show()
//    }

    private fun showNoHistory() {
        binding.tvNoPosting.visibility = View.VISIBLE
        binding.rvPosting.visibility = View.GONE
    }
    private fun showNoLogin() {
        binding.tvNoLogin.visibility = View.VISIBLE
        binding.tvNoPosting.visibility = View.GONE
        binding.rvPosting.visibility = View.GONE
    }



}