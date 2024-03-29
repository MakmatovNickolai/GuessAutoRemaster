package kon4.sam.guessauto.view_model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amplifyframework.core.Amplify
import dagger.hilt.android.lifecycle.HiltViewModel
import kon4.sam.guessauto.App
import kon4.sam.guessauto.repository.UserRepository
import kon4.sam.guessauto.util.Event
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File
import java.io.FileNotFoundException
import java.util.*
import javax.inject.Inject

@HiltViewModel
class UserViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    companion object {
        const val bucketUrl = "https://guessauto.s3.eu-north-1.amazonaws.com/public/"
    }

    val userName = MutableLiveData<String>()
    val url = MutableLiveData<String?>()

    init {
        userName.value = App.user.user_name ?: ""
        url.value = App.user.picture_url
    }

    fun updateUserName() {
        App.user.user_name = userName.value!!.trim();
        Timber.d(App.user.toString())
        viewModelScope.launch {
            userRepository.updateUser()
        }
    }

    val userUpdated = userRepository.userUpdated
    fun setUserImage() {
        App.user.picture_url = url.value!!
        viewModelScope.launch {
            userRepository.updateUser()
        }
    }

    private var _uploadToS3RequestFinished = MutableLiveData<Event<String?>>()
    val uploadToS3RequestFinished: LiveData<Event<String?>> = _uploadToS3RequestFinished

    fun uploadImageToS3(path: String) {
        try {
            val selectedImageFile = File(path)
            val uniqueId = UUID.randomUUID().toString()

            Amplify.Storage.uploadFile(
                "$uniqueId.jpg",
                selectedImageFile,
                { result ->
                    url.postValue(bucketUrl + result.key)
                    _uploadToS3RequestFinished.postValue(Event("OK"))
                },
                { error ->
                    Timber.e(error)
                    _uploadToS3RequestFinished.postValue(Event(error.message))
                }
            )
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
            Timber.e(e)
        }
    }

    fun removeImageForUser() {
        App.user.picture_url = null
        viewModelScope.launch {
            userRepository.updateUser()
        }
    }

    private var _deleteFromS3RequestFinished = MutableLiveData<Event<String?>>()
    val deleteFromS3RequestFinished: LiveData<Event<String?>> = _deleteFromS3RequestFinished

    fun removeImageFromS3() {
        val key = url.value!!.removePrefix(bucketUrl)
        Amplify.Storage.remove(key,
            {
                _deleteFromS3RequestFinished.postValue(Event("OK"))
            },
            { error ->
                Timber.e(error)
                _deleteFromS3RequestFinished.postValue(Event(error.message))
            })
    }
}