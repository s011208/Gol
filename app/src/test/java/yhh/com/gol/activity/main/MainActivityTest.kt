package yhh.com.gol.activity.main

import android.content.res.Resources
import android.widget.SeekBar
import android.widget.TextView
import dagger.Component
import dagger.Module
import dagger.Provides
import io.mockk.*
import kotlinx.android.synthetic.main.activity_main.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.android.controller.ActivityController
import org.robolectric.annotation.Config
import yhh.com.gol.R
import yhh.com.gol.activity.main.domain.State
import yhh.com.gol.libs.dagger2.PerActivity
import yhh.com.gol.util.TestUtilities
import javax.inject.Inject

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [21], manifest = Config.NONE)
class MainActivityTest {

    private lateinit var activity: MainActivity

    @field:[Inject]
    internal lateinit var presenter: MainActivityPresenter

    private lateinit var activityController: ActivityController<MainActivity>

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxUnitFun = true, relaxed = true)
        TestUtilities.initRxSchedulers()

        activityController = Robolectric.buildActivity(MainActivity::class.java)
        activityController.get().component = DaggerMainActivityTestComponent.create().also { it.inject(this) }
    }

    @Test
    fun create() {
        activityController.create()

        verify { presenter.create() }
    }

    @Test
    fun renderSwitchToStart() {
        activity = spyk(activityController.create().get())

        val controller: TextView = mockk(relaxed = true, relaxUnitFun = true)
        every { activity.controller } returns controller
        every { activity.findViewById(R.id.controller) as TextView } returns controller

        val resources: Resources = mockk(relaxed = true, relaxUnitFun = true)
        every { activity.resources } returns resources
        every { resources.getText(R.string.activity_main_start) } returns "123"

        activity.render(State.SwitchToStart)

        verify {
            resources.getText(R.string.activity_main_start)
        }
    }

    @Test
    fun renderSwitchToPause() {
        activity = spyk(activityController.create().get())

        val controller: TextView = mockk(relaxed = true, relaxUnitFun = true)
        every { activity.controller } returns controller
        every { activity.findViewById(R.id.controller) as TextView } returns controller

        val resources: Resources = mockk(relaxed = true, relaxUnitFun = true)
        every { activity.resources } returns resources
        every { resources.getText(R.string.activity_main_pause) } returns "123"

        activity.render(State.SwitchToPause)

        verify {
            resources.getText(R.string.activity_main_pause)
        }
    }

    @Test
    fun renderUpdateSeekBars() {
        activity = spyk(activityController.create().get())

        val frameRateSeekBar: SeekBar = mockk(relaxed = true, relaxUnitFun = true)
        every { activity.frameRateSeekBar } returns frameRateSeekBar
        every { activity.findViewById(R.id.frameRateSeekBar) as SeekBar } returns frameRateSeekBar

        val scaleSeekBar: SeekBar = mockk(relaxed = true, relaxUnitFun = true)
        every { activity.scaleSeekBar } returns scaleSeekBar
        every { activity.findViewById(R.id.scaleSeekBar) as SeekBar } returns scaleSeekBar

        activity.render(State.UpdateDefaultValue(10, 20))
        verify {
            frameRateSeekBar.progress = 10
            scaleSeekBar.progress = 20
        }
    }
}

@PerActivity
@Component(modules = [MainActivityTestModule::class])
interface MainActivityTestComponent : MainActivityComponent {
    fun inject(test: MainActivityTest)
}

@Module
class MainActivityTestModule {

    @Provides
    @PerActivity
    fun providePresenter(): MainActivityPresenter = mockk(relaxed = true, relaxUnitFun = true)
}