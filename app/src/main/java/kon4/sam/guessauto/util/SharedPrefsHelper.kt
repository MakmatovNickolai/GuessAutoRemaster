package kon4.sam.guessauto.util

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kon4.sam.guessauto.data.model.Car
import kon4.sam.guessauto.data.model.User

import timber.log.Timber


class SharedPrefsHelper(context: Context) {
    private val sharedPrefs: SharedPreferences = context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)

    fun clearPref(name: String) {
       sharedPrefs.edit().remove(name).apply();
    }

    fun getStoredXmlDbVersion(): Int {
        return sharedPrefs.getInt(XML_DB_VERSION, 0)
    }

    fun saveXmlDbVersion(version: Int) {
        val editor = sharedPrefs.edit()
        editor.putInt(XML_DB_VERSION, version)
        editor.apply()
    }

    fun saveJsonAutoBase(cars: List<Car>) {
        val editor = sharedPrefs.edit()
        val typeToken = object : TypeToken<List<Car>>() {}.type
        val autobaseJson = Gson().toJson(cars, typeToken)
        editor.putString(AUTOBASE, autobaseJson);
        editor.apply()
    }
    fun getAutoBaseFromJson(): List<Car> {
        var result = emptyList<Car>()
        val autobaseJson: String? = sharedPrefs.getString(AUTOBASE, "0")
        if (autobaseJson != null && autobaseJson != "0") {
            try {
                val typeToken = object : TypeToken<List<Car>>() {}.type
                result = Gson().fromJson(autobaseJson, typeToken)
            } catch (e: Exception) {
                Timber.e(e)
            }
        }
        return result;
    }

    fun saveMyInfo(user: User) {
        val editor = sharedPrefs.edit()
        val typeToken = object : TypeToken<User>() {}.type
        val userJson = Gson().toJson(user, typeToken)
        editor.putString(USER, userJson);
        editor.apply()
    }

    fun getMyInfo(): User? {
        var result: User? = null
        val userJson: String? = sharedPrefs.getString(USER, "0")
        if (userJson != null && userJson != "0") {
            try {
                val typeToken = object : TypeToken<User>() {}.type
                result = Gson().fromJson(userJson, typeToken)
            } catch (e: Exception) {
                Timber.e(e)
            }
        }
        return result
    }

    companion object {
        const val PREFERENCES = "PREFERENCES"
        const val XML_DB_VERSION = "xml_db_version"
        const val AUTOBASE = "autobase"
        const val USER = "user"
    }
}