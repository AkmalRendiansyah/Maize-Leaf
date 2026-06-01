package com.akmal.maizeleaf.ui.komentar

import android.util.Log
import androidx.lifecycle.*
import com.akmal.maizeleaf.api.ApiService
import com.akmal.maizeleaf.api.GetHistoryResponseItem
import com.akmal.maizeleaf.api.GetKomentarResponseItem
import com.akmal.maizeleaf.data.UserModel
import com.akmal.maizeleaf.data.UserPreference
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

class KomentarViewModel(
    private val userPreference: UserPreference,
    private val apiService: ApiService
) : ViewModel() {
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _komentarList = MutableLiveData<List<GetKomentarResponseItem>>()
    val komentarList: LiveData<List<GetKomentarResponseItem>> = _komentarList

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    fun getSession(): LiveData<UserModel> = userPreference.getSession().asLiveData()

    fun logout() {
        viewModelScope.launch {
            userPreference.logout()
        }
    }


    fun getKomentar(token: String,idPosting: Int) {
        _isLoading.value =true
        viewModelScope.launch {
            try {
                Log.d("KomentarViewModel", "Fetching history with token: $token")
                val bearerToken = "Bearer $token"
                val response = apiService.getKomentarById(bearerToken,idPosting)
                _komentarList.value = response.filterNotNull()
                Log.d("KomentarViewModel", "History fetched: ${response.size} items")
            } catch (e: IOException) {
                _errorMessage.value = "Tidak ada koneksi internet. Periksa jaringan Anda."
                _komentarList.value = emptyList()
            }catch (e: Exception) {
                _errorMessage.value = "Terjadi kesalahan: ${e.message}"
                _komentarList.value = emptyList()
            }finally {
                _isLoading.value=false
            }
        }
    }

    fun kirimKomentar(token: String, idPosting: Int, komentar: String) {
        _isLoading.value =true
        viewModelScope.launch {
            try {
                val bearerToken = "Bearer $token"
                apiService.postKomentar(bearerToken, idPosting, komentar) // sesuaikan nama endpoint-mu
                getKomentar(token, idPosting) // refresh list setelah kirim
            } catch (e: IOException) {
                _errorMessage.value = "Tidak ada koneksi internet. Periksa jaringan Anda."

            }catch (e: Exception) {
                _errorMessage.value = "Terjadi kesalahan: ${e.message}"

            }finally {
                _isLoading.value=false
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }



}
