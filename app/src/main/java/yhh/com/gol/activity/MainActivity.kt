package yhh.com.gol.activity

import android.os.Bundle
import android.view.MotionEvent
import androidx.annotation.VisibleForTesting
import androidx.appcompat.app.AppCompatActivity
import com.jakewharton.rxbinding3.view.clicks
import com.jakewharton.rxbinding3.view.globalLayouts
import com.jakewharton.rxbinding3.view.touches
import io.reactivex.Observable
import kotlinx.android.synthetic.main.activity_main.*
import yhh.com.gol.R
import yhh.com.gol.activity.domain.State
import javax.inject.Inject

class MainActivity : AppCompatActivity() {

    @field:[Inject]
    internal lateinit var presenter: MainActivityPresenter

    @VisibleForTesting
    internal var component: MainActivityComponent? = null

    internal lateinit var gameViewTouchIntent: Observable<MotionEvent>

    internal lateinit var gameViewLayoutIntent: Observable<Pair<Int, Int>>

    internal lateinit var startIntent: Observable<Unit>

    internal lateinit var pauseIntent: Observable<Unit>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initViews()
        initInjections()
        presenter.create()
    }

    fun render(state: State) {
        when (state) {
            is State.UpdateGameView -> {
                gameView.update(state.bitmap)
            }
        }
    }

    private fun initViews() {
        gameViewTouchIntent = gameView.touches()
        gameViewLayoutIntent = gameView.globalLayouts().map { Pair(gameView.width, gameView.height) }
        startIntent = start.clicks()
        pauseIntent = pause.clicks()
    }

    private fun initInjections() {
        if (component == null) {
            component = DaggerMainActivityComponent.builder()
                .mainActivityModule(MainActivityModule(this))
                .build()
        }
        component?.inject(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.destroy()
    }
}
