package com.akmal.maizeleaf.ui.komentar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.akmal.maizeleaf.api.ApiService
import com.akmal.maizeleaf.data.UserPreference

class KomentarViewModelFactory(
    private val userPreference: UserPreference,
    private val apiService: ApiService
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(KomentarViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return KomentarViewModel(userPreference, apiService) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}