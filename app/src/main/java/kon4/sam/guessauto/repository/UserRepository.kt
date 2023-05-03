package kon4.sam.guessauto.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kon4.sam.guessauto.App
import kon4.sam.guessauto.data.model.Car
import kon4.sam.guessauto.util.Event
import kon4.sam.guessauto.util.SharedPrefsHelper
import kon4.sam.guessauto.network.ApiService
import kon4.sam.guessauto.data.model.User
import kon4.sam.guessauto.util.RandomString
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import timber.log.Timber
import java.util.UUID
import javax.inject.Inject

class UserRepository @Inject constructor(
    private val apiService: ApiService,
    private val sharedPrefsHelper: SharedPrefsHelper
) {
    private var _userCreated = MutableLiveData<Event<String>>()
    val userCreated: LiveData<Event<String>> = _userCreated

    fun createUser(user: User) {
        apiService.createUser(user).enqueue(object : Callback<String> {
            override fun onFailure(call: Call<String>, t: Throwable) {
                Timber.e(t)
                _userCreated.postValue(Event(t.message.toString()))
            }

            override fun onResponse(call: Call<String>, response: Response<String>) {
                val res = response.body()!!
                _userCreated.postValue(Event(res))
                if (res == "OK") {

                }
            }
        })
    }

    private var _userUpdated = MutableLiveData<Event<String>>()
    val userUpdated: LiveData<Event<String>> = _userUpdated

    fun updateUser() {
        apiService.updateUser(App.user).enqueue(object : Callback<String> {
            override fun onFailure(call: Call<String>, t: Throwable) {
                Timber.e(t)
                _userUpdated.postValue(Event(t.message.toString()))
            }

            override fun onResponse(call: Call<String>, response: Response<String>) {
                val res = response.body()!!
                if (res == "OK") {

                }
                _userUpdated.postValue(Event(res))
            }
        })
    }

    private var _getMyInfoEvent = MutableLiveData<Event<String>>()
    val getMyInfoEvent: LiveData<Event<String>> = _getMyInfoEvent

    fun getMyInfo() {
        apiService.getMyInfo(App.user.device_id).enqueue(object : Callback<User?> {
            override fun onFailure(call: Call<User?>, t: Throwable) {
                Timber.e(t)
                _getMyInfoEvent.postValue(Event(t.message.toString()))
            }

            override fun onResponse(call: Call<User?>, response: Response<User?>) {
                val user = response.body()
                if (user != null) {
                    App.user = user
                    _getMyInfoEvent.postValue(Event("OK"))
                } else {
                    App.createNewUser()
                    createUser(App.user)
                    _getMyInfoEvent.postValue(Event("User is null"))
                }
                sharedPrefsHelper.saveMyInfo(App.user)
            }
        })
    }

    fun getAllCars() {
        apiService.getAllCars().enqueue(object : Callback<List<Car>> {
            override fun onFailure(call: Call<List<Car>>, t: Throwable) {
                Timber.e(t)
            }

            override fun onResponse(call: Call<List<Car>>, response: Response<List<Car>>) {
                val cars = response.body()
                if (cars != null) {
                    sharedPrefsHelper.saveJsonAutoBase(cars)
                } else {
                    Timber.e("No cars from server, can't play")
                }
            }
        })
    }
}