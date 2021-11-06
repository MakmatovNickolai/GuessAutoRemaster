package kon4.sam.guessauto.view_model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kon4.sam.guessauto.repository.UserRepository
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    val getMyInfoEvent = userRepository.getMyInfoEvent

    fun getMyInfo() {
        viewModelScope.launch {
            userRepository.getMyInfo()
        }
    }
}