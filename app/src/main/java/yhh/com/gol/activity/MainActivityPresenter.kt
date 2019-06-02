package yhh.com.gol.activity

import android.view.MotionEvent
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.plusAssign
import yhh.com.gol.activity.controller.GameController
import yhh.com.gol.activity.domain.State
import yhh.com.gol.libs.dagger2.PerActiviy
import javax.inject.Inject

@PerActiviy
class MainActivityPresenter @Inject constructor(
    private val view: MainActivity,
    private val gameController: GameController
) {

    private val compositeDisposable = CompositeDisposable()

    private var gameViewGlobalLayoutDisposable: Disposable? = null

    private var tempViewGlobalLayoutDisposable: Disposable? = null

    fun create() {
        tempViewGlobalLayoutDisposable = view.tempViewLayoutIntent
            .subscribe {
                view.render(State.InitGameView(it.first, it.second))

                gameViewGlobalLayoutDisposable = view.gameViewLayoutIntent
                    .subscribe { pair ->
                        gameController.createGrid(pair.first, pair.second)

                        compositeDisposable += view.gameViewTouchIntent
                            .subscribe { motionEvent ->
                                if (motionEvent.action == MotionEvent.ACTION_MOVE || motionEvent.action == MotionEvent.ACTION_DOWN) {
                                    gameController.addLifeAt(motionEvent.x.toInt(), motionEvent.y.toInt())
                                } else if (motionEvent.action == MotionEvent.ACTION_CANCEL || motionEvent.action == MotionEvent.ACTION_UP) {
                                    gameController.mergeLife()
                                }
                            }

                        gameViewGlobalLayoutDisposable?.dispose()
                        gameViewGlobalLayoutDisposable = null
                    }

                tempViewGlobalLayoutDisposable?.dispose()
                tempViewGlobalLayoutDisposable = null
            }

        compositeDisposable += gameController.updateIntent
            .subscribe {
                view.render(State.UpdateGameView(it))
            }

        compositeDisposable += view.startIntent
            .subscribe { gameController.start() }

        compositeDisposable += view.pauseIntent
            .subscribe { gameController.pause() }
    }

    fun destroy() {
        if (!compositeDisposable.isDisposed) {
            compositeDisposable.dispose()
        }
        if (gameViewGlobalLayoutDisposable?.isDisposed == false) {
            gameViewGlobalLayoutDisposable?.dispose()
        }
    }
}