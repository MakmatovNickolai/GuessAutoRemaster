package kon4.sam.guessauto.view_model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kon4.sam.guessauto.App
import kon4.sam.guessauto.data.model.User
import kon4.sam.guessauto.repository.UserRepository
import kon4.sam.guessauto.util.RandomString
import kon4.sam.guessauto.util.SharedPrefsHelper
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val sharedPrefsHelper: SharedPrefsHelper,
    private val userRepository: UserRepository
) : ViewModel() {
    fun getMyInfo() {
        val user = sharedPrefsHelper.getMyInfo()
        if (user!= null) {
            App.user = user
        } else {
            App.createNewUser()
        }
        viewModelScope.launch {
            userRepository.getMyInfo()
        }
    }

    fun reloadAllCars() {
        viewModelScope.launch {
            userRepository.getAllCars()
        }
    }
}