package yhh.com.gol.activity.main

import android.graphics.Color
import android.os.Bundle
import android.view.MotionEvent
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.RelativeLayout
import androidx.annotation.VisibleForTesting
import androidx.appcompat.app.AppCompatActivity
import com.jakewharton.rxbinding3.view.clicks
import com.jakewharton.rxbinding3.view.globalLayouts
import com.jakewharton.rxbinding3.view.touches
import com.jakewharton.rxbinding3.widget.changes
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.activity_main.*
import timber.log.Timber
import yhh.com.gol.R
import yhh.com.gol.activity.main.domain.State
import yhh.com.gol.view.GameView
import javax.inject.Inject
import kotlin.math.roundToInt


class MainActivity : AppCompatActivity() {

    companion object {
        private const val BASE_SCALE = 4f
    }

    @field:[Inject]
    internal lateinit var presenter: MainActivityPresenter

    @VisibleForTesting
    internal var component: MainActivityComponent? = null

    internal lateinit var frameRateChangeIntent: Observable<Int>

    internal lateinit var scaleChangeIntent: Observable<Int>

    internal lateinit var gameViewTouchIntent: Observable<MotionEvent>

    internal lateinit var tempViewLayoutIntent: Observable<Pair<Int, Int>>

    internal lateinit var gameViewLayoutIntent: Observable<Pair<Int, Int>>

    internal lateinit var startIntent: Observable<Unit>

    internal lateinit var pauseIntent: Observable<Unit>

    internal lateinit var randomAddIntent: Observable<Unit>

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
            is State.ScaleGameView -> {
                gameView?.apply {
                    scaleX = state.scale.toFloat()
                    scaleY = state.scale.toFloat()
                }
            }
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
                    Timber.v("width: ${state.width}, height: ${state.height}")
                    setBackgroundColor(Color.BLACK)
                    val scale = BASE_SCALE

                    val gameViewWidth = (state.width / scale).roundToInt()
                    val gameViewHeight = (state.height / scale).roundToInt()

                    container.addView(
                        gameView,
                        RelativeLayout.LayoutParams(gameViewWidth, gameViewHeight)
                            .apply {
                                // lets put view in center
                                setMargins(
                                    state.width / 2 - gameViewWidth / 2,
                                    state.height / 2 - gameViewHeight / 2,
                                    0,
                                    0
                                )
                                translationZ = -1f
                            }
                    )

                    pivotX = gameViewWidth / 2f
                    pivotY = gameViewHeight / 2f

                    scaleX = scale
                    scaleY = scale

                    gameViewTouchIntent =
                        touches()
//                            .filter {
//                                if (it.action == MotionEvent.ACTION_MOVE) return@filter it.x >= x && it.x <= x + width && it.y >= y && it.y <= y + height
//                                else return@filter true
//                            }
                            .filter {
                                if (it.action == MotionEvent.ACTION_MOVE) return@filter it.x >= 0 && it.x <= width && it.y >= 0 && it.y <= height
                                else return@filter true
                            }
                    gameViewLayoutIntent = globalLayouts().map { Pair(width, height) }

                    (tempView.parent as ViewGroup).removeView(tempView)
                }
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
        randomAddIntent = randomAdd.clicks()
        frameRateChangeIntent = frameRateSeekBar.changes().skipInitialValue().map { if (it <= 0) 1 else it }
        scaleChangeIntent = scaleSeekBar.changes().skipInitialValue().map { it + BASE_SCALE.toInt() }
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
