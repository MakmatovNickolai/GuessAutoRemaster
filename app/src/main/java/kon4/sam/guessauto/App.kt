package kon4.sam.guessauto

import android.app.Application
import kon4.sam.guessauto.network.ApiClient


class App : Application() {
    companion object {
        val apiClient: ApiClient = ApiClient()
        var username = ""
        var score = 0
        const val LOG_TAG = "LOG_TAG"
        const val APP_PREFERENCES = "mysettings"
        const val APP_PREFERENCES_USERNAME = "user_name"
    }

}