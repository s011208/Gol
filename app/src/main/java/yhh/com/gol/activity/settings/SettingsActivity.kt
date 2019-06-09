package yhh.com.gol.activity.settings

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import yhh.com.gol.R
import yhh.com.gol.activity.settings.fragment.SettingsFragment

class SettingsActivity: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.container, SettingsFragment())
            .commitAllowingStateLoss()
    }
}