package com.akmal.maizeleaf.ui.listPosting

import android.util.Log
import androidx.lifecycle.*
import com.akmal.maizeleaf.api.ApiService
import com.akmal.maizeleaf.api.GetAllPostingResponseItem
import com.akmal.maizeleaf.api.GetHistoryResponseItem
import com.akmal.maizeleaf.data.UserModel
import com.akmal.maizeleaf.data.UserPreference
import kotlinx.coroutines.launch
import retrofit2.HttpException

class PostingViewModel(
    private val userPreference: UserPreference,
    private val apiService: ApiService
) : ViewModel() {

    private val _postingList = MutableLiveData<List<GetAllPostingResponseItem>>()
    val postingList: LiveData<List<GetAllPostingResponseItem>> = _postingList

    fun getSession(): LiveData<UserModel> = userPreference.getSession().asLiveData()

    fun logout() {
        viewModelScope.launch {
            userPreference.logout()
        }
    }

    fun getPosting(token: String) {
        viewModelScope.launch {
            try {
                Log.d("PostingViewModel", "Fetching Posting with token: $token")
                val bearerToken = "Bearer $token"
                val response = apiService.getPosting(bearerToken)
                _postingList.value = response.filterNotNull()
                Log.d("PostingViewModel", "History fetched: ${response.size} items")
            } catch (e: Exception) {
                Log.e("PostingViewModel", "Error fetching history", e)
                _postingList.value = emptyList()
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