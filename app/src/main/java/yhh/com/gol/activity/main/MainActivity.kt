package yhh.com.gol.activity.main

import android.graphics.Color
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.RelativeLayout
import androidx.annotation.VisibleForTesting
import androidx.appcompat.app.AppCompatActivity
import com.jakewharton.rxbinding3.view.clicks
import com.jakewharton.rxbinding3.view.globalLayouts
import com.jakewharton.rxbinding3.view.touches
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.view_debug_panel.*
import timber.log.Timber
import yhh.com.gol.R
import yhh.com.gol.activity.main.domain.State
import yhh.com.gol.view.GameView
import javax.inject.Inject
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity() {

    @field:[Inject]
    internal lateinit var presenter: MainActivityPresenter

    @VisibleForTesting
    internal var component: MainActivityComponent? = null

    internal lateinit var gameViewTouchIntent: Observable<MotionEvent>

    internal lateinit var tempViewLayoutIntent: Observable<Pair<Int, Int>>

    internal lateinit var gameViewLayoutIntent: Observable<Pair<Int, Int>>

    internal lateinit var startIntent: Observable<Unit>

    internal lateinit var pauseIntent: Observable<Unit>

    internal val onResumeIntent = PublishSubject.create<Unit>()

    internal val onPauseIntent = PublishSubject.create<Unit>()

    private var gameView: GameView? = null

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
                gameView?.update(state.bitmap)
            }
            is State.InitGameView -> {
                if (gameView != null) {
                    container.removeView(gameView)
                    gameView = null
                }

                gameView = GameView(this)
                gameView?.apply {
                    Timber.e("width: ${state.width}, height: ${state.height}")
                    setBackgroundColor(Color.BLACK)
                    val scale = 8f
                    container.addView(
                        gameView,
                        RelativeLayout.LayoutParams(
                            (state.width / scale).roundToInt(),
                            (state.height / scale).roundToInt()
                        )
                    )

                    pivotX = 0f
                    pivotY = 0f

                    scaleX = scale
                    scaleY = scale

                    gameViewTouchIntent =
                        touches().filter {
                            if (it.action == MotionEvent.ACTION_MOVE) return@filter it.x >= x && it.x <= x + width && it.y >= y && it.y <= y + height
                            else return@filter true
                        }
                    gameViewLayoutIntent = globalLayouts().map { Pair(width, height) }

                    (tempView.parent as ViewGroup).removeView(tempView)
                }
            }
            is State.ShowDebugPanel -> {
                if (debugPanelViewStub != null && debugPanelViewStub.parent != null) {
                    debugPanelViewStub.inflate()
                }
                debugPanelContainer.visibility = View.VISIBLE
            }
            is State.HideDebugPanel -> {
                if (debugPanelViewStub != null && debugPanelViewStub.parent != null) {
                    return
                }
                debugPanelContainer.visibility = View.INVISIBLE
            }
            is State.UpdateDebugMessage -> {
                if (debugPanelViewStub != null && debugPanelViewStub.parent != null) {
                    return
                }
                if (debugPanelContainer.visibility == View.INVISIBLE) return
                debugMessage.text = state.message
            }
        }
    }

    private fun initViews() {
        window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        gameView = GameView(this)
            .apply {
                setBackgroundColor(Color.BLACK)
            }

        tempViewLayoutIntent = tempView.globalLayouts().map { Pair(tempView.width, tempView.height) }
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

    override fun onResume() {
        super.onResume()
        onResumeIntent.onNext(Unit)
    }

    override fun onPause() {
        super.onPause()
        onPauseIntent.onNext(Unit)
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.destroy()
    }
}
