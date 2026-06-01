package com.akmal.maizeleaf.ui.history

import android.util.Log
import androidx.lifecycle.*
import com.akmal.maizeleaf.api.ApiService
import com.akmal.maizeleaf.api.GetHistoryResponseItem
import com.akmal.maizeleaf.data.UserModel
import com.akmal.maizeleaf.data.UserPreference
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

class HistoryViewModel(
    private val userPreference: UserPreference,
    private val apiService: ApiService
) : ViewModel() {

    private val _historyList = MutableLiveData<List<GetHistoryResponseItem>>()
    val historyList: LiveData<List<GetHistoryResponseItem>> = _historyList
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    fun getSession(): LiveData<UserModel> = userPreference.getSession().asLiveData()

    fun logout() {
        viewModelScope.launch {
            userPreference.logout()
        }
    }

    fun getHistory(token: String) {
        _isLoading.value=true
        viewModelScope.launch {
            try {
                Log.d("HistoryViewModel", "Fetching history with token: $token")
                val bearerToken = "Bearer $token"
                val response = apiService.getHistory(bearerToken)
                _historyList.value = response.filterNotNull()
                Log.d("HistoryViewModel", "History fetched: ${response.size} items")
            } catch (e: IOException) {
                _errorMessage.value = "Tidak ada koneksi internet. Periksa jaringan Anda."
                _historyList.value = emptyList()
            }catch (e: Exception) {
                Log.e("HistoryViewModel", "Error fetching history", e)
                _historyList.value = emptyList()
            }finally {
                _isLoading.value=false
            }
        }
    }

    fun deleteHistory(token: String, historyId: Int, onResult: (Boolean) -> Unit) {
        _isLoading.value=true
        viewModelScope.launch {
            try {
                val bearerToken = "Bearer $token"
                val response = apiService.deleteHistory(bearerToken, historyId)

                if (response.isSuccessful) {

                    _historyList.value = _historyList.value?.filter { it.id != historyId }
                    onResult(true)
                    Log.d("HistoryViewModel", "History $historyId deleted successfully")
                } else {
                    onResult(false)
                    Log.e("HistoryViewModel", "Failed to delete history $historyId: ${response.code()}")
                }
            }catch (e: IOException) {
                _errorMessage.value = "Tidak ada koneksi internet. Periksa jaringan Anda."
            }catch (e: HttpException) {
                onResult(false)
                _errorMessage.value = "Terjadi kesalahan: ${e.message}"
                Log.e("HistoryViewModel", "HTTP error deleting history", e)
            } catch (e: Exception) {
                onResult(false)
                _errorMessage.value = "Terjadi kesalahan: ${e.message}"
                Log.e("HistoryViewModel", "Error deleting history", e)
            }finally {
                _isLoading.value=false
            }
        }
    }
    fun clearError() {
        _errorMessage.value = null
    }

}
