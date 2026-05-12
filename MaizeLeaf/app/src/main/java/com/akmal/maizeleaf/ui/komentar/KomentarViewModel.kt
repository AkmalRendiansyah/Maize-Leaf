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

class KomentarViewModel(
    private val userPreference: UserPreference,
    private val apiService: ApiService
) : ViewModel() {

    private val _komentarList = MutableLiveData<List<GetKomentarResponseItem>>()
    val komentarList: LiveData<List<GetKomentarResponseItem>> = _komentarList

    fun getSession(): LiveData<UserModel> = userPreference.getSession().asLiveData()

    fun logout() {
        viewModelScope.launch {
            userPreference.logout()
        }
    }

    fun getKomentar(token: String,idPosting: Int) {
        viewModelScope.launch {
            try {
                Log.d("KomentarViewModel", "Fetching history with token: $token")
                val bearerToken = "Bearer $token"
                val response = apiService.getKomentarById(bearerToken,idPosting)
                _komentarList.value = response.filterNotNull()
                Log.d("KomentarViewModel", "History fetched: ${response.size} items")
            } catch (e: Exception) {
                Log.e("KomentarViewModel", "Error fetching history", e)
                _komentarList.value = emptyList()
            }
        }
    }

    fun kirimKomentar(token: String, idPosting: Int, komentar: String) {
        viewModelScope.launch {
            try {
                val bearerToken = "Bearer $token"
                apiService.postKomentar(bearerToken, idPosting, komentar) // sesuaikan nama endpoint-mu
                getKomentar(token, idPosting) // refresh list setelah kirim
            } catch (e: Exception) {
                Log.e("KomentarViewModel", "Error posting komentar", e)
            }
        }
    }



}
