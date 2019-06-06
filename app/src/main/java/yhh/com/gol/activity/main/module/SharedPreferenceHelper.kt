package yhh.com.gol.activity.main.module

import android.content.Context
import javax.inject.Inject

class SharedPreferenceHelper @Inject constructor(context: Context) {

    companion object {
        private const val SHARED_PREFERENCE_NAME = "main_preference"

        private const val KEY_SCALE_SIZE = "key_scale_size"
        private const val KEY_FRAME_RATE = "key_frame_rate"
    }

    private val preference = context.getSharedPreferences(SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE)

    fun getScaleValue() = preference.getInt(KEY_SCALE_SIZE, 0)

    fun setScaleValue(value: Int) = preference.edit().putInt(KEY_SCALE_SIZE, value).apply()

    fun getFrameRate() = preference.getInt(KEY_FRAME_RATE, 30)

    fun setFrameRate(value: Int) = preference.edit().putInt(KEY_FRAME_RATE, value).apply()
}