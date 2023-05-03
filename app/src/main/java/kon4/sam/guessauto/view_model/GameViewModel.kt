package kon4.sam.guessauto.view_model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kon4.sam.guessauto.App
import kon4.sam.guessauto.repository.UserRepository
import kon4.sam.guessauto.data.DBHelper
import kon4.sam.guessauto.data.JsonDbHelper
import kon4.sam.guessauto.util.Event
import kon4.sam.guessauto.util.SharedPrefsHelper
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GameViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val dbHelper: JsonDbHelper,
): ViewModel() {

    var currentPhotoIndex = 0
    var score = 0
    var attempt = 0
    var clickedButtonViewId = 0
    var nextImageLink:String
    lateinit var carImages: MutableList<String>
    lateinit var currentPhotoAutoName:String

    private var _showAdEvent = MutableLiveData<Event<Boolean>>()
    val showAdEvent: LiveData<Event<Boolean>> = _showAdEvent

    init {
        initAutoDb()
        nextImageLink = dbHelper.getAutoLink(carImages[currentPhotoIndex])
    }

    private fun initAutoDb() {
        dbHelper.reloadCarsFromFile()
        carImages = dbHelper.getAllAuto()
        carImages.shuffle()
    }

    fun updateAndGetNextImageLink(): String {
        nextImageLink =  dbHelper.getAutoLink(carImages[currentPhotoIndex])
        return nextImageLink
    }

    fun updateCurrentPhotoAutoName() {
        currentPhotoAutoName = carImages[currentPhotoIndex]
    }

    fun getAllCaptionsForAuto(): List<String> {
        return dbHelper.getAllCaptionsForAuto(currentPhotoAutoName)
    }

    fun updateUser() {
        viewModelScope.launch {
            userRepository.updateUser()
        }
    }

    val userCreated = userRepository.userCreated
    fun createUser(userName: String) {
        App.user.user_name = userName
        viewModelScope.launch {
            userRepository.createUser(App.user)
        }
    }

    fun isLastAttempt(): Boolean {
        return attempt == 3
    }

    fun showAd() {
        _showAdEvent.postValue(Event(true))
    }
}