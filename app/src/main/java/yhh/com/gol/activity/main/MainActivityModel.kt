package yhh.com.gol.activity.main

import android.content.SharedPreferences
import io.reactivex.Single
import yhh.com.gol.activity.settings.fragment.SettingsFragment
import yhh.com.gol.libs.dagger2.PerActivity
import javax.inject.Inject

@PerActivity
class MainActivityModel @Inject constructor(private val sharedPreferences: SharedPreferences) {

    fun checkDebugView() = Single.fromCallable {
        return@fromCallable sharedPreferences.getBoolean(SettingsFragment.KEY_SHOW_DEBUG_PANEL, false)
    }
}