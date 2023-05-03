package kon4.sam.guessauto.data

import kon4.sam.guessauto.data.model.Car
import kon4.sam.guessauto.util.SharedPrefsHelper
import timber.log.Timber
import javax.inject.Inject

class JsonDbHelper @Inject constructor(
    private val sharedPrefsHelper: SharedPrefsHelper,
    private val dbHelper: DBHelper) {

    private lateinit var cars: List<Car>

    fun reloadCarsFromFile() {
        val carsFromJson = sharedPrefsHelper.getAutoBaseFromJson();
        if (carsFromJson.isEmpty()) {
            val carsFromXml = dbHelper.cars
            if (carsFromXml.isNotEmpty()) {
                cars = carsFromXml
            } else {
                Timber.e("No cars in xml and json")
            }
        } else {
            cars = sharedPrefsHelper.getAutoBaseFromJson()
        }
    }

    fun getAllCaptionsForAuto(autoName: String) : MutableList<String> {
        val captions = cars.first { it.name == autoName }.similar_cars
        val res = captions.split(',').toMutableList()
        res.shuffle()
        return res
    }

    fun getAutoLink(autoName:String) : String {
        return cars.first { it.name == autoName }.picture_url
    }

    fun getAllAuto(): MutableList<String> {
        return cars.map { it.name }.toMutableList()
    }
}