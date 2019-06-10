package yhh.com.gol.activity.main

import android.view.MotionEvent
import androidx.annotation.VisibleForTesting
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import yhh.com.gol.activity.main.controller.v2.GameController2
import yhh.com.gol.activity.main.domain.State
import yhh.com.gol.activity.main.module.SharedPreferenceHelper
import yhh.com.gol.libs.dagger2.PerActivity
import javax.inject.Inject

@PerActivity
class MainActivityPresenter @Inject constructor(
    private val view: MainActivity,
    private val gameController: GameController2,
    private val sharedPreferenceHelper: SharedPreferenceHelper
) {

    private val compositeDisposable = CompositeDisposable()

    private var gameViewGlobalLayoutDisposable: Disposable? = null

    private var tempViewGlobalLayoutDisposable: Disposable? = null

    @VisibleForTesting
    internal var isPause = false

    fun create() {
        tempViewGlobalLayoutDisposable = view.tempViewLayoutIntent
            .subscribe {
                view.render(State.InitGameView(it.first, it.second))

                gameViewGlobalLayoutDisposable = view.gameViewLayoutIntent
                    .subscribe { pair ->
                        gameController.createBoard(pair.first, pair.second)

                        compositeDisposable += view.gameViewTouchIntent
                            .subscribe { motionEvent ->
                                if (motionEvent.action == MotionEvent.ACTION_MOVE || motionEvent.action == MotionEvent.ACTION_DOWN) {
                                    gameController.addGridPoint(motionEvent.x.toInt(), motionEvent.y.toInt())
                                } else if (motionEvent.action == MotionEvent.ACTION_CANCEL || motionEvent.action == MotionEvent.ACTION_UP) {
                                    gameController.mergeGridPoints()
                                }
                            }
                        gameViewGlobalLayoutDisposable?.dispose()
                        gameViewGlobalLayoutDisposable = null

                        compositeDisposable += Single.fromCallable {
                            return@fromCallable Pair(
                                sharedPreferenceHelper.getFrameRate(),
                                sharedPreferenceHelper.getScaleValue()
                            )
                        }
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(
                                { savedValues ->
                                    view.render(State.UpdateDefaultValue(savedValues.first, savedValues.second))

                                    compositeDisposable += view.frameRateChangeIntent
                                        .subscribe { frameRate ->
                                            gameController.setFrameRate(frameRate)
                                            sharedPreferenceHelper.setFrameRate(frameRate)
                                        }

                                    compositeDisposable += view.scaleChangeIntent
                                        .subscribe { scale ->
                                            view.render(State.ScaleGameView(scale))
                                            sharedPreferenceHelper.setScaleValue(scale - MainActivity.BASE_SCALE.toInt())
                                        }
                                },
                                { savedValueThrowable ->
                                    Timber.w(savedValueThrowable, "failed to init default value")
                                }
                            )
                    }

                tempViewGlobalLayoutDisposable?.dispose()
                tempViewGlobalLayoutDisposable = null
            }

        compositeDisposable += gameController.updateIntent
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                view.render(State.UpdateGameView(it))
            }

        compositeDisposable += view.controlIntent
            .subscribe {
                isPause = if (isPause) {
                    gameController.resume()
                    view.render(State.SwitchToPause)
                    false
                } else {
                    gameController.pause()
                    view.render(State.SwitchToStart)
                    true
                }
            }

        compositeDisposable += view.onPauseIntent
            .subscribe {
                gameController.sleep()
            }

        compositeDisposable += view.onResumeIntent
            .subscribe {
                gameController.awake()
            }

        compositeDisposable += view.randomAddIntent
            .subscribe {
                gameController.randomAdd()
            }

        compositeDisposable += view.clearIntent
            .subscribe {
                gameController.clear()
            }
    }

    fun destroy() {
        gameController.finish()

        if (!compositeDisposable.isDisposed) {
            compositeDisposable.dispose()
        }
        if (gameViewGlobalLayoutDisposable?.isDisposed == false) {
            gameViewGlobalLayoutDisposable?.dispose()
        }
    }
}