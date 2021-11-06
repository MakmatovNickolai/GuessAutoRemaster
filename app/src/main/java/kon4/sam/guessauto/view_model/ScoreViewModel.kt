package kon4.sam.guessauto.view_model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kon4.sam.guessauto.network.ApiService
import kon4.sam.guessauto.network.model.User
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class ScoreViewModel @Inject constructor(
    private val apiService: ApiService
) :ViewModel() {

    private var _getAllScoreCompleted = MutableLiveData<List<User>?>()
    val getAllScoreCompleted: LiveData<List<User>?> = _getAllScoreCompleted
    var page = 1
    private var isLoadingUsers = false

    fun getInitialTopUsers() {
        if (!isLoadingUsers) {
            page = 1
            isLoadingUsers = true
            getTopScoreUsersByPage()
        }
    }

    fun getScoreUsersNextPage() {
        if (!isLoadingUsers) {
            page += 1
            isLoadingUsers = true
            getTopScoreUsersByPage()
        }
    }

    private fun getTopScoreUsersByPage() {
        viewModelScope.launch {
            apiService.getTopScoreUsersByPage(page).enqueue(object :
                Callback<List<User>> {
                override fun onFailure(call: Call<List<User>>, t: Throwable) {
                    Timber.e(t)
                    isLoadingUsers = false
                    _getAllScoreCompleted.postValue(null)
                }

                override fun onResponse(call: Call<List<User>>, response: Response<List<User>>) {
                    val listRes = response.body()
                    isLoadingUsers = false
                    _getAllScoreCompleted.postValue(listRes)
                }
            })
        }
    }
}