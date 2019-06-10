package yhh.com.gol.activity.settings.fragment

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import yhh.com.gol.R
import yhh.com.gol.activity.settings.fragment.domain.State
import javax.inject.Inject

class SettingsFragment : PreferenceFragmentCompat() {
    companion object {
        const val KEY_VERSION_NAME = "version_name"
        const val KEY_VERSION_CODE = "version_code"
    }

    @field:[Inject]
    internal lateinit var presenter: SettingsFragmentPresenter

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.pref_settings)
        DaggerSettingsFragmentComponent.builder()
            .settingsFragmentModule(SettingsFragmentModule(this))
            .build().inject(this)
        presenter.create()
    }

    fun render(state: State) {
        when (state) {
            is State.UpdateVersionCode -> {
                findPreference(KEY_VERSION_CODE)?.summary = state.text
            }
            is State.UpdateVersionName -> {
                findPreference(KEY_VERSION_NAME)?.summary = state.text
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.destroy()
    }
}