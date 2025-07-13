package com.akmal.maizeleaf.ui.history

import android.content.Context
import android.content.Intent
import androidx.datastore.preferences.preferencesDataStore
import androidx.fragment.app.Fragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.akmal.maizeleaf.R
import com.akmal.maizeleaf.api.ApiConfig
import com.akmal.maizeleaf.api.ApiService
import com.akmal.maizeleaf.databinding.FragmentHistoryBinding
import com.akmal.maizeleaf.data.UserPreference
import com.akmal.maizeleaf.ui.login.LoginActivity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

private val Context.dataStore by preferencesDataStore(name = "session")

class HistoryFragment : Fragment() {

    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: HistoryViewModel
    private lateinit var adapter: HistoryAdapter
    private lateinit var userPreference: UserPreference
    private lateinit var apiService: ApiService

    private var userToken: String? = null  // Simpan token

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        userPreference = UserPreference.getInstance(requireContext().dataStore)
        apiService = ApiConfig.getApiService()
        viewModel = HistoryViewModel(userPreference, apiService)

        setupRecyclerView()

        viewModel.getSession().observe(viewLifecycleOwner) { user ->
            val token = user.token
            userToken = token
            if (token.isNotEmpty()) {
                binding.tvNoLogin.visibility = View.GONE
                viewModel.getHistory(token)
            } else {
                showNoLogin()
            }
        }

        viewModel.historyList.observe(viewLifecycleOwner) { historyList ->
            if (historyList.isNullOrEmpty()) {
                showNoHistory()
            } else {
                binding.tvNoHistory.visibility = View.GONE
                binding.rvHistory.visibility = View.VISIBLE
                adapter.submitList(historyList)
            }
        }
        binding.tvNoLogin.setOnClickListener {
            val intent = Intent(requireContext(), LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            requireActivity().finish()
        }

    }

    private fun setupRecyclerView() {
        adapter = HistoryAdapter(
            onHistoryClick = { item ->
                // Klik item bisa diisi jika dibutuhkan
            },
            onDeleteClick = { item ->
                item.id?.let { historyId ->
                    showDeleteConfirmationDialog(historyId)
                }
            }

        )
        binding.rvHistory.layoutManager = LinearLayoutManager(requireContext())
        binding.rvHistory.adapter = adapter
    }

    private fun showDeleteConfirmationDialog(historyId: Int) {
        val context = requireContext()
        val builder = androidx.appcompat.app.AlertDialog.Builder(context)
        builder.setTitle(getString(R.string.delete_alert))
            .setMessage(getString(R.string.are_you_sure_delete))
            .setPositiveButton(getString(R.string.yes)) { _, _ ->
                userToken?.let { token ->
                    viewModel.deleteHistory(token, historyId) { success ->
                        if (!success) {
                        }
                    }
                }
            }
            .setNegativeButton(getString(R.string.no)) { dialog, _ ->
                dialog.dismiss()
            }
        builder.create().show()
    }

    private fun showNoHistory() {
        binding.tvNoHistory.visibility = View.VISIBLE
        binding.rvHistory.visibility = View.GONE
    }
    private fun showNoLogin() {
        binding.tvNoLogin.visibility = View.VISIBLE
        binding.tvNoHistory.visibility = View.GONE
        binding.rvHistory.visibility = View.GONE
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
