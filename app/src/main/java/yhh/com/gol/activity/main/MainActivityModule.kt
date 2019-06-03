package yhh.com.gol.activity.main

import android.content.Context
import androidx.preference.PreferenceManager
import dagger.Module
import dagger.Provides
import yhh.com.gol.libs.dagger2.PerActivity

@Module
class MainActivityModule(private val view: MainActivity) {

    @Provides
    @PerActivity
    fun provideView() = view

    @Provides
    @PerActivity
    fun provideContext(): Context = view

    @Provides
    @PerActivity
    fun providePreferenceManager(context: Context) = PreferenceManager.getDefaultSharedPreferences(context)

}