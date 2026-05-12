package com.akmal.maizeleaf.ui.artikel

import android.util.Log
import androidx.lifecycle.*
import com.akmal.maizeleaf.api.ApiService
import com.akmal.maizeleaf.api.GetAllPostingResponseItem
import com.akmal.maizeleaf.api.GetArtikelResponseItem
import com.akmal.maizeleaf.api.GetHistoryResponseItem
import com.akmal.maizeleaf.data.UserModel
import com.akmal.maizeleaf.data.UserPreference
import kotlinx.coroutines.launch
import retrofit2.HttpException

class ArtikelViewModel(
    private val userPreference: UserPreference,
    private val apiService: ApiService
) : ViewModel() {

    private val _artikelList = MutableLiveData<List<GetArtikelResponseItem>>()
    val artikelList: LiveData<List<GetArtikelResponseItem>> = _artikelList

    fun getSession(): LiveData<UserModel> = userPreference.getSession().asLiveData()

    fun logout() {
        viewModelScope.launch {
            userPreference.logout()
        }
    }

    fun getArtikel(token: String) {
        viewModelScope.launch {
            try {
                Log.d("ArtikelViewModel", "Fetching Posting with token: $token")
                val bearerToken = "Bearer $token"
                val response = apiService.getArtikel(bearerToken)
                _artikelList.value = response.filterNotNull()
                Log.d("ArtikelViewModel", "History fetched: ${response.size} items")
            } catch (e: Exception) {
                Log.e("ArtikelViewModel", "Error fetching history", e)
                _artikelList.value = emptyList()
            }
        }
    }

//    fun deleteHistory(token: String, historyId: Int, onResult: (Boolean) -> Unit) {
//        viewModelScope.launch {
//            try {
//                val bearerToken = "Bearer $token"
//                val response = apiService.deleteHistory(bearerToken, historyId)
//
//                if (response.isSuccessful) {
//
//                    _historyList.value = _historyList.value?.filter { it.id != historyId }
//                    onResult(true)
//                    Log.d("HistoryViewModel", "History $historyId deleted successfully")
//                } else {
//                    onResult(false)
//                    Log.e("HistoryViewModel", "Failed to delete history $historyId: ${response.code()}")
//                }
//            } catch (e: HttpException) {
//                onResult(false)
//                Log.e("HistoryViewModel", "HTTP error deleting history", e)
//            } catch (e: Exception) {
//                onResult(false)
//                Log.e("HistoryViewModel", "Error deleting history", e)
//            }
//        }
//    }

}