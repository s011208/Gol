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

    private var radius = 1

    private val compositeDisposable = CompositeDisposable()

    private var gameViewGlobalLayoutDisposable: Disposable? = null

    fun create() {
        gameViewGlobalLayoutDisposable = view.gameViewLayoutIntent
            .subscribe {
                gameController.createGrid(it.first / radius, it.second / radius)
                gameViewGlobalLayoutDisposable?.dispose()
                gameViewGlobalLayoutDisposable = null
            }

        compositeDisposable += view.gameViewTouchIntent
            .subscribe {
                if (it.action == MotionEvent.ACTION_MOVE || it.action == MotionEvent.ACTION_DOWN) {
                    gameController.addLifeAt(it.x.toInt() / radius, it.y.toInt() / radius)
                } else if (it.action == MotionEvent.ACTION_CANCEL || it.action == MotionEvent.ACTION_UP) {
                    gameController.mergeLife()
                }
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