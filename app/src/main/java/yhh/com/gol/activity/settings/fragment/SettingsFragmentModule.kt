package yhh.com.gol.activity.settings.fragment

import android.content.Context
import dagger.Module
import dagger.Provides
import yhh.com.gol.libs.dagger2.PerFragment

@Module
class SettingsFragmentModule(private val view: SettingsFragment) {
    @Provides
    @PerFragment
    fun provideView() = view

    @Provides
    @PerFragment
    fun provideContext(): Context = view.requireContext()

    @Provides
    @PerFragment
    fun providePackageManager(context: Context) = context.packageManager
}