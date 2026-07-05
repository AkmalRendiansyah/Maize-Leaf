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

    private var allPage          = 1
    private var allIsLastPage    = false
    private var allIsLoadingMore = false

    private var myPage           = 1
    private var myIsLastPage     = false
    private var myIsLoadingMore  = false

    val isLoadingMore: Boolean
        get() = allIsLoadingMore || myIsLoadingMore

    fun getSession(): LiveData<UserModel> = userPreference.getSession().asLiveData()

    fun logout() {
        viewModelScope.launch {
            userPreference.logout()
        }
    }


    fun getPosting(token: String, loadMore: Boolean = false) {
        if (allIsLoadingMore) return
        if (loadMore && allIsLastPage) return
        val page = if (loadMore) allPage + 1 else 1
        if (!loadMore) {
            allPage = 1
            allIsLastPage = false
        }

        viewModelScope.launch {
            if (loadMore) allIsLoadingMore = true
            else _isLoading.value = true
            try {
                Log.d("PostingViewModel", "Fetching Posting with token: $token")
                val bearerToken = "Bearer $token"
                val response = apiService.getPosting(bearerToken, page )
                val current  = if (loadMore) _postingList.value.orEmpty() else emptyList()
                _postingList.value = current + (response.data?.filterNotNull() ?: emptyList())
                allPage       = response.page ?: page
                allIsLastPage = response.hasNext != true

            } catch (e: IOException) {
                _errorMessage.value = "Tidak ada koneksi internet. Periksa jaringan Anda."
                if (!loadMore) _postingList.value = emptyList()
            }catch (e: Exception) {
                _errorMessage.value = "Terjadi kesalahan: ${e.message}"
                if (!loadMore) _postingList.value = emptyList()
            }finally {
                if (loadMore) allIsLoadingMore = false else _isLoading.value = false
            }
        }
    }

    fun getMyPosting(token: String, loadMore: Boolean = false) {
        if (myIsLoadingMore) return
        if (loadMore && myIsLastPage) return
        val page = if (loadMore) myPage + 1 else 1
        if (!loadMore) {
            myPage = 1
            myIsLastPage = false
        }

        viewModelScope.launch {
            if (loadMore) myIsLoadingMore = true else _isLoading.value = true
            try {
                Log.d("PostingViewModel", "Fetching Posting with token: $token")
                val bearerToken = "Bearer $token"
                val response = apiService.getMyPosting(bearerToken, page )
                val current  = if (loadMore) _myPostingList.value.orEmpty() else emptyList()
                _myPostingList.value = current + (response.data?.filterNotNull() ?: emptyList())
                myPage       = response.page ?: page
                myIsLastPage = response.hasNext != true
            } catch (e: IOException) {
                _errorMessage.value = "Tidak ada koneksi internet. Periksa jaringan Anda."
                if (!loadMore) _myPostingList.value = emptyList()
            }catch (e: Exception) {
                _errorMessage.value = "Terjadi kesalahan: ${e.message}"
                if (!loadMore) _myPostingList.value = emptyList()
            }finally {
                if (loadMore) allIsLoadingMore = false else _isLoading.value = false
            }
        }
    }

    fun resetPagination() {
        allPage = 1;  allIsLastPage = false;  allIsLoadingMore = false
        myPage  = 1;  myIsLastPage  = false;  myIsLoadingMore  = false
        _postingList.value   = emptyList()
        _myPostingList.value = emptyList()

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