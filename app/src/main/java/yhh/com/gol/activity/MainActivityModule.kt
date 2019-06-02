package yhh.com.gol.activity

import dagger.Module
import dagger.Provides
import yhh.com.gol.libs.dagger2.PerActiviy

@Module
class MainActivityModule(private val view: MainActivity) {

    @Provides
    @PerActiviy
    fun provideView() = view

}