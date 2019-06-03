package yhh.com.gol.activity.main

import android.view.MotionEvent
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import yhh.com.gol.activity.main.controller.GameController
import yhh.com.gol.activity.main.domain.State
import yhh.com.gol.libs.dagger2.PerActivity
import javax.inject.Inject

@PerActivity
class MainActivityPresenter @Inject constructor(
    private val view: MainActivity,
    private val gameController: GameController,
    private val model: MainActivityModel
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
                        fun initGameController(pair: Pair<Int, Int>) {
                            gameController.createGrid(pair.first, pair.second)

                            compositeDisposable += view.gameViewTouchIntent
                                .subscribe { motionEvent ->
                                    if (motionEvent.action == MotionEvent.ACTION_MOVE || motionEvent.action == MotionEvent.ACTION_DOWN) {
                                        gameController.addLifeAt(motionEvent.x.toInt(), motionEvent.y.toInt())
                                    } else if (motionEvent.action == MotionEvent.ACTION_CANCEL || motionEvent.action == MotionEvent.ACTION_UP) {
                                        gameController.mergeLife()
                                    }
                                }
                        }
                        initGameController(pair)
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
            .subscribe { gameController.pause = false }

        compositeDisposable += view.pauseIntent
            .subscribe { gameController.pause = true }

        compositeDisposable += view.onPauseIntent
            .subscribe {
                gameController.stopRendering()
            }

        compositeDisposable += view.onResumeIntent
            .subscribe {
                gameController.startRendering()

                model.checkDebugView()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                        { showDebugPanel ->
                            Timber.v("showDebugPanel: $showDebugPanel")
                            if (showDebugPanel) {
                                view.render(State.ShowDebugPanel)
                            } else {
                                view.render(State.HideDebugPanel)
                            }
                        },
                        {
                            Timber.w(it, "failed to check debug panel visibility")
                        }
                    )
            }

        compositeDisposable += gameController.debugMessageIntent
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { view.render(State.UpdateDebugMessage(it)) }
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