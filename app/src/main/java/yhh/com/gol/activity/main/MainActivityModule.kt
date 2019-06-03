package yhh.com.gol.activity.main

import dagger.Module
import dagger.Provides
import yhh.com.gol.libs.dagger2.PerActivity

@Module
class MainActivityModule(private val view: MainActivity) {

    @Provides
    @PerActivity
    fun provideView() = view

}