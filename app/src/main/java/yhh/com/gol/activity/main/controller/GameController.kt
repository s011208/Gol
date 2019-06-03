package yhh.com.gol.activity.main.controller

import android.graphics.*
import io.reactivex.Completable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import timber.log.Timber
import javax.inject.Inject

class GameController @Inject constructor() {

    companion object {
        private const val DRAW_BITMAP_STEP = 3000
    }

    internal val updateIntent = PublishSubject.create<Bitmap>()

    internal val debugMessageIntent = PublishSubject.create<String>()

    private val conwayRule = ConwayRule()

    private val tempNewLifeList = ArrayList<Point>()

    private val backgroundNewLifeList = ArrayList<Point>()

    private lateinit var board: Array<IntArray>

    private lateinit var boardBitmap: Bitmap

    private val canvas = Canvas()

    var pause = true

    private var disposable: Disposable? = null

    private val livePaint = Paint().apply {
        style = Paint.Style.FILL
        color = Color.WHITE
        strokeWidth = 1f
    }

    private val diePaint = Paint().apply {
        style = Paint.Style.FILL
        color = Color.BLACK
        strokeWidth = 1f
    }

    private fun render() {
        Timber.i("render")
        if (disposable != null) disposable?.dispose()
        disposable = Completable.create { emitter ->
            while (!emitter.isDisposed) {
                if (!updateIntent.hasObservers() || updateIntent.hasComplete()) {
                    Timber.v("has observers: ${updateIntent.hasObservers()}, has completed: ${updateIntent.hasComplete()}")
                    Thread.sleep(16)
                    continue
                }
                var copyNewLifeListTime = System.currentTimeMillis()
                synchronized(backgroundNewLifeList) {
                    if (backgroundNewLifeList.isNotEmpty()) {
                        backgroundNewLifeList.forEach {
                            board[it.x][it.y] = 1
                        }
                        backgroundNewLifeList.clear()
                    }
                }
                copyNewLifeListTime = System.currentTimeMillis() - copyNewLifeListTime
                val timeSpend = System.currentTimeMillis()
                if (!::boardBitmap.isInitialized) {
                    boardBitmap = Bitmap.createBitmap(board.size, board[0].size, Bitmap.Config.ARGB_8888)
                }
                val list: ArrayList<GamePoint>
                var conwayRuleTime: Long
                if (!pause) {
                    conwayRuleTime = System.currentTimeMillis()
                    list = conwayRule.generateChangedLifeList(board)
                    conwayRuleTime = System.currentTimeMillis() - conwayRuleTime
                } else {
                    list = ArrayList()
                    conwayRuleTime = 0
                }

                var drawingTime = System.currentTimeMillis()
                canvas.setBitmap(boardBitmap)
                if (list.isNotEmpty()) {
                    // FIXME Fatal signal 11 (SIGSEGV), code 1 (SEGV_MAPERR), fault addr 0x1493b3a4 in tid 1487 (GameController), pid 1462 (yhh.com.gol)
                    if (list.size >= 20000 && false) {
                        // normally it spends less than single thread when list.size > 20000 ?
                        val completableList = ArrayList<Completable>()
                        for (index in 0 until list.size step DRAW_BITMAP_STEP) {
                            completableList.add(Completable.fromAction {
                                val subList = list.subList(index, Math.min(index + DRAW_BITMAP_STEP, list.size))
                                subList.forEach {
                                    if (it.isAlive) {
                                        canvas.drawPoint(it.x.toFloat(), it.y.toFloat(), livePaint)
                                    } else {
                                        canvas.drawPoint(it.x.toFloat(), it.y.toFloat(), diePaint)
                                    }
                                }
                            }.subscribeOn(Schedulers.io()))
                        }
                        Completable.merge(completableList).blockingAwait()
                    } else {
                        list.forEach {
                            if (it.isAlive) {
                                canvas.drawPoint(it.x.toFloat(), it.y.toFloat(), livePaint)
                            } else {
                                canvas.drawPoint(it.x.toFloat(), it.y.toFloat(), diePaint)
                            }
                        }
                    }
                }
                val newLifeList: ArrayList<Point>
                synchronized(tempNewLifeList) {
                    newLifeList = ArrayList(tempNewLifeList)
                }
                if (newLifeList.isNotEmpty()) {
                    newLifeList.forEach {
                        canvas.drawPoint(it.x.toFloat(), it.y.toFloat(), livePaint)
                    }
                }

                canvas.setBitmap(null)
                updateIntent.onNext(boardBitmap)
                drawingTime = System.currentTimeMillis() - drawingTime

                if (list.isNotEmpty()) {
                    debugMessageIntent.onNext("total(${System.currentTimeMillis() - timeSpend}), logic($conwayRuleTime), drawing($drawingTime), copy($copyNewLifeListTime), update(${list.size})")
                }
            }
        }
            .subscribeOn(Schedulers.io())
            .doOnDispose { Timber.i("stop render") }
            .subscribe()
    }

    fun createGrid(width: Int, height: Int) {
        Timber.v("createGrid, columnCount: $width, rowCount: $height")
        if (width < 0 || height < 0) {
            throw IllegalArgumentException("arguments cannot be negative")
        }
        board = Array(width) { IntArray(height) }
        render()
    }

    fun addLifeAt(x: Int, y: Int) {
        Timber.v("addLifeAt, x: $x, y: $y")
        synchronized(tempNewLifeList) {
            tempNewLifeList.add(Point(x, y))
        }
    }

    fun mergeLife() {
        Timber.v("merge")

        if (!::board.isInitialized) {
            throw IllegalArgumentException("board needs be created first")
        }
        // FIXME ??
        synchronized(backgroundNewLifeList) {
            synchronized(tempNewLifeList) {
                backgroundNewLifeList.addAll(tempNewLifeList)
                tempNewLifeList.clear()
            }
        }
    }

    fun stopRendering() {
        disposable?.dispose()
    }

    fun startRendering() {
        if (!::board.isInitialized) return
        render()
    }
}