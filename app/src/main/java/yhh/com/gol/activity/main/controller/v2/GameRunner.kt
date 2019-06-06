package yhh.com.gol.activity.main.controller.v2

import android.graphics.*
import io.reactivex.subjects.PublishSubject
import timber.log.Timber
import yhh.com.gol.activity.main.controller.ConwayRule
import yhh.com.gol.activity.main.controller.GamePoint

@Suppress("RemoveRedundantQualifierName", "PLATFORM_CLASS_MAPPED_TO_KOTLIN")
class GameRunner(private val conwayRule: ConwayRule) : Thread() {

    companion object {
        private const val TAG_LOCK = "Lock"
    }

    init {
        priority = MAX_PRIORITY
    }

    // sync members
    private val newPointList = HashSet<Point>()

    private var canMergeNewPointList = false

    private var frameRate = 60

    private var newBoardWidth = -1

    private var newBoardHeight = -1

    private var pause = false

    // normal members
    private lateinit var board: Array<IntArray>

    internal val updateIntent = PublishSubject.create<Bitmap>()

    private lateinit var boardBitmap: Bitmap

    private val canvas = Canvas()

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

    private var remainingFrameTime = 0L

    private val lock = Any()

    private var isSleep = false

    private var previousFrameRate = 0

    private var isFinished = false

    override fun run() {
        loop@ while (!isFinished) {
            synchronized(lock) {
                if (isSleep) {
                    Timber.tag(TAG_LOCK).v("prepare to lock")
                    try {
                        (lock as java.lang.Object).wait()
                    } catch (e: InterruptedException) {
                    }
                }
            }
            if (isFinished) {
                Timber.tag(TAG_LOCK).i("Finish game")
                break@loop
            }
            if (!updateIntent.hasObservers()) {
                Timber.v("updateIntent.hasObservers(): ${updateIntent.hasObservers()}, ignore")
                sleep(1000L)
                continue
            }

            val newPointList = ArrayList<Point>()
            val frameRate: Int
            val canMergeNewPointList: Boolean
            val newBoardWidth: Int
            val newBoardHeight: Int
            val pause: Boolean

            var totalTime = System.currentTimeMillis()

            // init function variable
            synchronized(this) {

                frameRate = this.frameRate
                canMergeNewPointList = this.canMergeNewPointList
                newPointList.addAll(this.newPointList)
                newBoardWidth = this.newBoardWidth
                newBoardHeight = this.newBoardHeight
                pause = this.pause

                if (canMergeNewPointList) {
                    this.newPointList.clear()
                    this.canMergeNewPointList = false
                }
                if (frameRate != previousFrameRate) {
                    // attempt to reset remainingFrameTime when frame rate updated
                    remainingFrameTime = 0L
                }
                previousFrameRate = frameRate
            }

            // create board if need
            if (newBoardHeight > 0 && newBoardWidth > 0) {
                if (!::board.isInitialized || board.size != newBoardWidth || board[0].size != newBoardHeight) {
                    Timber.i("create new board with w: $newBoardWidth, h: $newBoardHeight")
                    board = Array(newBoardWidth) { IntArray(newBoardHeight) }
                    if (::boardBitmap.isInitialized) {
                        boardBitmap.recycle()
                    }
                    boardBitmap = Bitmap.createBitmap(board.size, board[0].size, Bitmap.Config.RGB_565)
                    Timber.i("create new bitmap: $boardBitmap")
                }
            }

            if (::board.isInitialized) {
                var hasUpdate = false

                canvas.setBitmap(boardBitmap)

                // copy new point if need
                if (canMergeNewPointList) {
                    newPointList.forEach {
                        board[it.x][it.y] = 1
                    }
                    newPointList.clear()
                }

                // generate next generation list
                var gameResult: ArrayList<GamePoint>? = null

                if (remainingFrameTime <= 0) {
                    gameResult =
                        if (!pause) {
                            conwayRule.generateChangedLifeList(board)
                        } else {
                            ArrayList()
                        }
                    remainingFrameTime = (1000 / frameRate).toLong()
                }

                // draw result
                if (!gameResult.isNullOrEmpty()) {
                    hasUpdate = true
                }
                gameResult?.forEach {
                    if (it.isAlive) {
                        canvas.drawPoint(it.x.toFloat(), it.y.toFloat(), livePaint)
                    } else {
                        canvas.drawPoint(it.x.toFloat(), it.y.toFloat(), diePaint)
                    }
                }

                // draw new live (not merged yet)
                if (newPointList.isNotEmpty()) {
                    hasUpdate = true
                }
                newPointList.forEach {
                    canvas.drawPoint(it.x.toFloat(), it.y.toFloat(), livePaint)
                }

                canvas.setBitmap(null)

                if (hasUpdate) {
                    updateIntent.onNext(Bitmap.createBitmap(boardBitmap))
                }
            }

            totalTime = System.currentTimeMillis() - totalTime
            remainingFrameTime -= totalTime
        }

        Timber.i("Game finished")
    }

    fun sleep() {
        synchronized(lock) {
            Timber.tag(TAG_LOCK).i("request lock")
            isSleep = true
        }
    }

    fun awake() {
        synchronized(lock) {
            Timber.tag(TAG_LOCK).i("request unlock")
            (lock as java.lang.Object).notifyAll()
            isSleep = false
        }
    }

    fun addNewPoint(x: Int, y: Int) {
        synchronized(this) {
            newPointList.add(Point(x, y))
        }
    }

    fun merge() {
        synchronized(this) {
            canMergeNewPointList = true
        }
    }

    fun setFrameRate(rate: Int) {
        synchronized(this) {
            frameRate = rate
        }
    }

    fun createBoard(width: Int, height: Int) {
        synchronized(this) {
            newBoardWidth = width
            newBoardHeight = height
        }
    }

    fun pauseGame() {
        synchronized(this) {
            pause = true
        }
    }

    fun resumeGame() {
        synchronized(this) {
            pause = false
            if (newPointList.isNotEmpty()) {
                canMergeNewPointList = true
            }
        }
    }

    fun finishGame() {
        isFinished = true
        interrupt()
    }

    fun randomAdd() {
        if (newBoardWidth <= 0 || newBoardHeight <= 0) {
            return
        }
        val localWidth = newBoardWidth
        val localHeight = newBoardHeight
        Thread(
            Runnable {
                val result = HashSet<Point>()
                for (x in 0 until localWidth) {
                    for (y in 0 until localHeight) {
                        if ((Math.random() * 10000).toInt() % 3 == 0) {
                            result.add(Point(x, y))
                        }
                    }
                }
                synchronized(this@GameRunner) {
                    newPointList.addAll(result)
                    if (!pause) canMergeNewPointList = true
                }
            }
        ).start()
    }
}
