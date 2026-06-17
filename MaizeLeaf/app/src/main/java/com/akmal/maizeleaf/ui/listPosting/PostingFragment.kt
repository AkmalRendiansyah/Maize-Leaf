package com.akmal.maizeleaf.ui.listPosting

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.datastore.preferences.preferencesDataStore
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.akmal.maizeleaf.R
import com.akmal.maizeleaf.api.ApiConfig
import com.akmal.maizeleaf.api.ApiService
import com.akmal.maizeleaf.api.GetAllPostingResponseItem
import com.akmal.maizeleaf.api.GetMyPostingResponseItem
import com.akmal.maizeleaf.data.UserPreference
import com.akmal.maizeleaf.databinding.ActivityAddChatBinding
import com.akmal.maizeleaf.databinding.FragmentHistoryBinding
import com.akmal.maizeleaf.databinding.FragmentPostingBinding
import com.akmal.maizeleaf.ui.addPosting.CameraPostingActivity
import com.akmal.maizeleaf.ui.history.HistoryAdapter
import com.akmal.maizeleaf.ui.history.HistoryViewModel
import com.akmal.maizeleaf.ui.login.LoginActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout


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
    override fun onResume() {
        super.onResume()
        userToken?.let { token ->
            when (binding.tabFilter.selectedTabPosition) {
                0 -> viewModel.getPosting(token)
                1 -> viewModel.getMyPosting(token)
            }
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        userPreference = UserPreference.getInstance(requireContext().dataStore)
        apiService = ApiConfig.getApiService()
        viewModel = PostingViewModel(userPreference, apiService)

        setupRecyclerView()
        setupScrollListener()
        binding.tabFilter.addTab(binding.tabFilter.newTab().setText("Semua Postingan"))
        binding.tabFilter.addTab(binding.tabFilter.newTab().setText("Postingan Saya"))


        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            setLoading(isLoading)
        }
        viewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            message?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                viewModel.clearError()
            }
        }

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

//        viewModel.postingList.observe(viewLifecycleOwner) { postingList ->
//            if (postingList.isNullOrEmpty()) {
//                showNoHistory()
//            } else {
//                binding.tvNoPosting.visibility = View.GONE
//                binding.rvPosting.visibility = View.VISIBLE
//                adapter.submitList(postingList)
//            }
//        }


        viewModel.postingList.observe(viewLifecycleOwner) { list ->
            if (binding.tabFilter.selectedTabPosition == 0) {
                renderAllPosting(list)
            }
        }

        viewModel.myPostingList.observe(viewLifecycleOwner) { list ->
            if (binding.tabFilter.selectedTabPosition == 1) {
                renderMyPosting(list)
            }
        }


        binding.tabFilter.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> {
                        adapter.setShowDelete(false)
                        viewModel.resetPagination()                        // reset
                        userToken?.let { viewModel.getPosting(it) }       // fetch ulang dari page 1
                    }
                    1 -> {
                        adapter.setShowDelete(true)
                        viewModel.resetPagination()
                        userToken?.let { viewModel.getMyPosting(it) }
                    }
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })


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

    private fun setupScrollListener() {
        binding.rvPosting.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy <= 0) return
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val lastVisible = layoutManager.findLastVisibleItemPosition()
                val total = layoutManager.itemCount


                if (lastVisible >= total - 2 && !viewModel.isLoadingMore) {
                    when (binding.tabFilter.selectedTabPosition) {
                        0 -> userToken?.let { viewModel.getPosting(it, loadMore = true) }
                        1 -> userToken?.let { viewModel.getMyPosting(it, loadMore = true) }
                    }
                }
            }
        })
    }

    private fun renderAllPosting(list: List<GetAllPostingResponseItem>?) {
        if (list.isNullOrEmpty()) showNoHistory()
        else {
            binding.tvNoPosting.visibility = View.GONE
            binding.rvPosting.visibility = View.VISIBLE
            adapter.submitList(list)
        }
    }

    private fun renderMyPosting(list: List<GetMyPostingResponseItem>?) {
        if (list.isNullOrEmpty()) showNoHistory()
        else {
            binding.tvNoPosting.visibility = View.GONE
            binding.rvPosting.visibility = View.VISIBLE
            // Map ke GetAllPostingResponseItem karena adapter pakai tipe itu
            val mapped = list.map {
                GetAllPostingResponseItem(
                    id           = it.id,
                    deskripsi    = it.deskripsi,
                    gambar       = it.gambar,
                    username     = it.username,
                    jumlahKomentar = it.jumlahKomentar,
                    createdAt    = it.createdAt
                )
            }
            adapter.submitList(mapped)
        }
    }



    private fun setupRecyclerView() {
        adapter = PostingAdapter(
            onPostingClick = { item ->  },
            onDeleteClick  = { item ->
                item.id?.let { showDeleteConfirmationDialog(it) }
            }

        )
        binding.rvPosting.layoutManager = LinearLayoutManager(requireContext())
        binding.rvPosting.adapter = adapter
    }



    private fun showDeleteConfirmationDialog(postingId: Int) {
        val context = requireContext()
        val builder = androidx.appcompat.app.AlertDialog.Builder(context)
        builder.setTitle(getString(R.string.delete_alert))
            .setMessage(getString(R.string.are_you_sure_delete))
            .setPositiveButton(getString(R.string.yes)) { _, _ ->
                userToken?.let { token ->
                    viewModel.deletePosting(token, postingId) { success ->
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
        binding.tvNoPosting.visibility = View.VISIBLE
        binding.rvPosting.visibility = View.GONE
    }
    private fun showNoLogin() {
        binding.tvNoLogin.visibility = View.VISIBLE
        binding.tvNoPosting.visibility = View.GONE
        binding.rvPosting.visibility = View.GONE
    }
    private fun setLoading(isLoading: Boolean) {
        binding.loadingOverlay.visibility = if (isLoading) View.VISIBLE else View.GONE
    }


}