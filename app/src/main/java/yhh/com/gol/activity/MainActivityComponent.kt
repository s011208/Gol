package yhh.com.gol.activity

import dagger.Component
import yhh.com.gol.libs.dagger2.PerActiviy

@PerActiviy
@Component(modules = [MainActivityModule::class])
interface MainActivityComponent {
    fun inject(view: MainActivity)
}