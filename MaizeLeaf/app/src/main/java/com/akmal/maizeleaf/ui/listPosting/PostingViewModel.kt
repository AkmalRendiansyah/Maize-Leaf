package com.akmal.maizeleaf.ui.listPosting

import android.util.Log
import androidx.lifecycle.*
import com.akmal.maizeleaf.api.ApiService
import com.akmal.maizeleaf.api.GetAllPostingResponseItem
import com.akmal.maizeleaf.api.GetHistoryResponseItem
import com.akmal.maizeleaf.api.GetMyPostingResponseItem
import com.akmal.maizeleaf.data.UserModel
import com.akmal.maizeleaf.data.UserPreference
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

class PostingViewModel(
    private val userPreference: UserPreference,
    private val apiService: ApiService
) : ViewModel() {
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _postingList = MutableLiveData<List<GetAllPostingResponseItem>>()
    val postingList: LiveData<List<GetAllPostingResponseItem>> = _postingList

    private val _myPostingList = MutableLiveData<List<GetMyPostingResponseItem>>()
    val myPostingList: LiveData<List<GetMyPostingResponseItem>> = _myPostingList

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    fun getSession(): LiveData<UserModel> = userPreference.getSession().asLiveData()

    fun logout() {
        viewModelScope.launch {
            userPreference.logout()
        }
    }

    fun getPosting(token: String) {

        viewModelScope.launch {
            _isLoading.value=true
            try {
                Log.d("PostingViewModel", "Fetching Posting with token: $token")
                val bearerToken = "Bearer $token"
                val response = apiService.getPosting(bearerToken)
                _postingList.value = response.filterNotNull()
                Log.d("PostingViewModel", "History fetched: ${response.size} items")
            } catch (e: IOException) {
                _errorMessage.value = "Tidak ada koneksi internet. Periksa jaringan Anda."
                _postingList.value = emptyList()
            }catch (e: Exception) {
                _errorMessage.value = "Terjadi kesalahan: ${e.message}"
                _postingList.value = emptyList()
            }finally {
                _isLoading.value=false
            }
        }
    }

    fun getMyPosting(token: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = apiService.getMyPosting("Bearer $token")
                _myPostingList.value = response.filterNotNull()
            } catch (e: IOException) {
                _errorMessage.value = "Tidak ada koneksi internet."
                _myPostingList.value = emptyList()
            } catch (e: Exception) {
                _errorMessage.value = "Terjadi kesalahan: ${e.message}"
                _myPostingList.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }


    fun clearError() {
        _errorMessage.value = null
    }

    fun deletePosting(token: String, postingId: Int, onResult: (Boolean) -> Unit) {
        _isLoading.value=true
        viewModelScope.launch {
            try {
                val bearerToken = "Bearer $token"
                val response = apiService.deletePosting(bearerToken, postingId)

                if (response.isSuccessful) {

                    _myPostingList.value = _myPostingList.value?.filter { it.id != postingId }
                    onResult(true)
                    Log.d("HistoryViewModel", "History $postingId deleted successfully")
                } else {
                    onResult(false)
                    Log.e("HistoryViewModel", "Failed to delete history $postingId: ${response.code()}")
                }
            } catch (e: IOException) {
                _errorMessage.value = "Tidak ada koneksi internet."
                _myPostingList.value = emptyList()
            } catch (e: Exception) {
                _errorMessage.value = "Terjadi kesalahan: ${e.message}"
                _myPostingList.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

}