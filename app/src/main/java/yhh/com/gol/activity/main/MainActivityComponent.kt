package yhh.com.gol.activity.main

import dagger.Component
import yhh.com.gol.libs.dagger2.PerActivity

@PerActivity
@Component(modules = [MainActivityModule::class])
interface MainActivityComponent {
    fun inject(view: MainActivity)
}