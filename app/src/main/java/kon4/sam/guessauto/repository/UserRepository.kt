package kon4.sam.guessauto.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kon4.sam.guessauto.App
import kon4.sam.guessauto.util.Event
import kon4.sam.guessauto.util.SharedPrefsHelper
import kon4.sam.guessauto.network.ApiService
import kon4.sam.guessauto.network.model.User
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import timber.log.Timber
import javax.inject.Inject

class UserRepository @Inject constructor(
    private val apiService: ApiService,
    private val sharedPrefsHelper: SharedPrefsHelper
) {

    private var _userScoreUpdated = MutableLiveData<Event<Boolean>>()
    val userScoreUpdated: LiveData<Event<Boolean>> = _userScoreUpdated

    fun sendUserScore(score: Int) {
        apiService.setScore(App.user.user_name, score.toString(), App.deviceId).enqueue(object :
            Callback<String> {
            override fun onFailure(call: Call<String>, t: Throwable) {
                Timber.e(t)
                _userScoreUpdated.postValue(Event(false))
            }

            override fun onResponse(call: Call<String>, response: Response<String>) {
                val res = response.body()
                if (res == "OK") {
                    sharedPrefsHelper.saveUserScoreToPrefs(score)
                    App.user.score = score
                    _userScoreUpdated.postValue(Event(true))
                }
            }
        })
    }

    private var _userInitiated = MutableLiveData<Event<String>>()
    val userInitiated: LiveData<Event<String>> = _userInitiated

    fun initiateUser(userName: String) {
        apiService.setNewUser(userName, App.deviceId).enqueue(object : Callback<String> {
            override fun onFailure(call: Call<String>, t: Throwable) {
                Timber.e(t)
                _userInitiated.postValue(Event(t.message.toString()))
            }

            override fun onResponse(call: Call<String>, response: Response<String>) {
                val res = response.body()!!
                _userInitiated.postValue(Event(res))
                if (res == "OK") {
                    App.user.user_name = userName
                    sharedPrefsHelper.saveUserNameToPrefs(userName)
                }
            }
        })
    }

    private var _userNameSet = MutableLiveData<Event<String>>()
    val userNameSet: LiveData<Event<String>> = _userNameSet

    fun setNewUser(userName: String) {
        apiService.setNewUser(userName, App.deviceId).enqueue(object : Callback<String> {
                override fun onFailure(call: Call<String>, t: Throwable) {
                    Timber.e(t)
                    _userNameSet.postValue(Event(t.message.toString()))
                }

                override fun onResponse(call: Call<String>, response: Response<String>) {
                    val res = response.body()!!
                    if (res == "OK") {
                        App.user.user_name = userName
                        sharedPrefsHelper.saveUserNameToPrefs(userName)
                    }
                    _userNameSet.postValue(Event(res))
                }
            })
    }

    fun updateNewUser(userName: String) {
        apiService.updateUserName(App.user.user_name, userName, App.deviceId).enqueue(object : Callback<String> {
                override fun onFailure(call: Call<String>, t: Throwable) {
                    Timber.e(t)
                    _userNameSet.postValue(Event(t.message.toString()))
                }

                override fun onResponse(call: Call<String>, response: Response<String>) {
                    val res = response.body()!!
                    if (res == "OK") {
                        App.user.user_name = userName
                        sharedPrefsHelper.saveUserNameToPrefs(userName)
                    }
                    _userNameSet.postValue(Event(res))
                }
            })
    }

    private var _userImageSet = MutableLiveData<Event<String>>()
    val userImageSet: LiveData<Event<String>> = _userImageSet

    fun setUserImage(url: String?) {
        apiService.setUserImage(App.user.user_name, url, App.deviceId).enqueue(object : Callback<String> {
            override fun onFailure(call: Call<String>, t: Throwable) {
                Timber.e(t)
                _userImageSet.postValue(Event(t.message.toString()))
            }

            override fun onResponse(call: Call<String>, response: Response<String>) {
                val res = response.body()!!
                if (res == "OK") {
                    sharedPrefsHelper.saveUserUrlToPrefs(url)
                    App.user.url = url
                    _userImageSet.postValue(Event("OK"))
                } else {
                    _userImageSet.postValue(Event(res))
                }
            }
        })
    }

    private var _getMyInfoEvent = MutableLiveData<Event<String>>()
    val getMyInfoEvent: LiveData<Event<String>> = _getMyInfoEvent

    fun getMyInfo() {
        apiService.getMyInfo(App.deviceId).enqueue(object : Callback<User?> {
            override fun onFailure(call: Call<User?>, t: Throwable) {
                Timber.e(t)
                _getMyInfoEvent.postValue(Event(t.message.toString()))
            }

            override fun onResponse(call: Call<User?>, response: Response<User?>) {
                val user = response.body()
                if (user != null) {
                    if (sharedPrefsHelper.getUserNameFromPrefs() == null) {
                        sharedPrefsHelper.saveUserNameToPrefs(user.user_name)
                        sharedPrefsHelper.saveUserScoreToPrefs(user.score)
                        sharedPrefsHelper.saveUserUrlToPrefs(user.url)
                    }
                    App.user = user.toDatabaseModel()
                    _getMyInfoEvent.postValue(Event("OK"))
                } else {
                    _getMyInfoEvent.postValue(Event("User is null"))
                }
            }
        })
    }
}