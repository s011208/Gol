package yhh.com.gol.activity.settings.fragment

import dagger.Component
import yhh.com.gol.libs.dagger2.PerFragment

@PerFragment
@Component(modules = [SettingsFragmentModule::class])
interface SettingsFragmentComponent {
    fun inject(view: SettingsFragment)
}