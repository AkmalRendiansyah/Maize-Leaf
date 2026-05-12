package com.akmal.maizeleaf.ui.artikel

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import androidx.datastore.preferences.preferencesDataStore
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager

import com.akmal.maizeleaf.api.ApiConfig
import com.akmal.maizeleaf.api.ApiService
import com.akmal.maizeleaf.data.UserPreference

import com.akmal.maizeleaf.databinding.FragmentArtikelBinding

import com.akmal.maizeleaf.ui.login.LoginActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton



private val Context.dataStore by preferencesDataStore(name = "session")

class ArtikelFragment : Fragment() {
    private var _binding: FragmentArtikelBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: ArtikelViewModel
    private lateinit var adapter: ArtikelAdapter
    private lateinit var userPreference: UserPreference
    private lateinit var apiService: ApiService

    private var userToken: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?

    ): View? {
        _binding = FragmentArtikelBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        userPreference = UserPreference.getInstance(requireContext().dataStore)
        apiService = ApiConfig.getApiService()
        viewModel = ArtikelViewModel(userPreference, apiService)

        setupRecyclerView()

        viewModel.getSession().observe(viewLifecycleOwner) { user ->
            val token = user.token
            userToken = token
            if (token.isNotEmpty()) {
                binding.tvNoLogin.visibility = View.GONE
                viewModel.getArtikel(token)
            } else {
                showNoLogin()
            }
        }

        viewModel.artikelList.observe(viewLifecycleOwner) { artikelList ->
            if (artikelList.isNullOrEmpty()) {
                showNoHistory()
            } else {
                binding.tvNoArtikel.visibility = View.GONE
                binding.rvArtikel.visibility = View.VISIBLE
                adapter.submitList(artikelList)
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
        adapter = ArtikelAdapter(
            onArtikelClick  = { item ->

                // Klik item bisa diisi jika dibutuhkan
            },
//            onPostingClick = { item ->
//                item.id?.let { historyId ->
//                    showDeleteConfirmationDialog(historyId)
//                }
//            }

        )
        binding.rvArtikel.layoutManager = LinearLayoutManager(requireContext())
        binding.rvArtikel.adapter = adapter
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
        binding.tvNoArtikel.visibility = View.VISIBLE
        binding.rvArtikel.visibility = View.GONE
    }
    private fun showNoLogin() {
        binding.tvNoLogin.visibility = View.VISIBLE
        binding.tvNoArtikel.visibility = View.GONE
        binding.rvArtikel.visibility = View.GONE
    }



}