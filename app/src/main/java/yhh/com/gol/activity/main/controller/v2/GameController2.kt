package yhh.com.gol.activity.main.controller.v2

import android.graphics.*
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.os.Message
import io.reactivex.subjects.PublishSubject
import timber.log.Timber
import yhh.com.gol.activity.main.controller.v1.ConwayRule
import javax.inject.Inject

class GameController2 @Inject constructor(conwayRule: ConwayRule) {

    private val handlerThread = HandlerThread("GolMessengeThread")

    private val handler: MessageHandler

    private val gameRunner = GameRunner(conwayRule)

    internal val updateIntent = gameRunner.updateIntent

    init {
        handlerThread.start()
        handler = MessageHandler(handlerThread.looper, gameRunner)
    }

    fun mergeGridPoints() {
        handler.sendMessage(Message.obtain()
            .also {
                it.what = MessageHandler.MSG_MERGE
            }
        )
    }

    fun addGridPoint(x: Int, y: Int) {
        handler.sendMessage(Message.obtain()
            .also {
                it.what = MessageHandler.MSG_ADD_GRID_POINT
                it.arg1 = x
                it.arg2 = y
            }
        )
    }

    fun setFrameRate(rate: Int) {
        handler.sendMessage(Message.obtain()
            .also {
                it.what = MessageHandler.MSG_SET_FRAME_RATE
                it.arg1 = rate
            }
        )
    }

    fun createBoard(width: Int, height: Int) {
        handler.sendMessage(Message.obtain()
            .also {
                it.what = MessageHandler.MSG_CREATE_BOARD
                it.arg1 = width
                it.arg2 = height
            }
        )
    }

    fun pause() {
        handler.sendMessage(Message.obtain()
            .also {
                it.what = MessageHandler.MSG_GAME_PAUSE
            }
        )
    }

    fun resume() {
        handler.sendMessage(Message.obtain()
            .also {
                it.what = MessageHandler.MSG_GAME_RESUME
            }
        )
    }
}

private class MessageHandler(looper: Looper, private val gameRunner: GameRunner) : Handler(looper) {

    companion object {
        const val MSG_ADD_GRID_POINT = 0
        const val MSG_MERGE = 1
        const val MSG_SET_FRAME_RATE = 2
        const val MSG_CREATE_BOARD = 3
        const val MSG_GAME_PAUSE = 4
        const val MSG_GAME_RESUME = 5
    }

    init {
        gameRunner.start()
    }

    override fun handleMessage(msg: Message) {
        super.handleMessage(msg)
        Timber.v("handleMessage, what: ${msg.what}")
        when (msg.what) {
            MSG_ADD_GRID_POINT -> {
                gameRunner.addNewPoint(msg.arg1, msg.arg2)
            }
            MSG_MERGE -> {
                gameRunner.merge()
            }
            MSG_SET_FRAME_RATE -> {
                gameRunner.setFrameRate(msg.arg1)
            }
            MSG_CREATE_BOARD -> {
                gameRunner.createBoard(msg.arg1, msg.arg2)
            }
            MSG_GAME_RESUME -> {
                gameRunner.resumeGame()
            }
            MSG_GAME_PAUSE -> {
                gameRunner.pauseGame()
            }
        }
    }
}

private class GameRunner(private val conwayRule: ConwayRule) : Thread() {
    // sync members
    private val newPointList = ArrayList<Point>()

    private var canMergeNewPointList = false

    private var frameRate = 30

    private var newBoardWidth = -1

    private var newBoardHeight = -1

    private var pause = false

    // normal members
    private lateinit var board: Array<IntArray>

    internal val updateIntent = PublishSubject.create<Bitmap>()

    internal val debugMessageIntent = PublishSubject.create<String>()

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

    override fun run() {

        fun calculate(
            newPointList: ArrayList<Point>,
            canMergeNewPointList: Boolean,
            newBoardWidth: Int,
            newBoardHeight: Int,
            pause: Boolean
        ) {
            Timber.w("calculate")

            if (newBoardHeight > 0 && newBoardWidth > 0) {
                if (!::board.isInitialized || board.size != newBoardWidth || board[0].size != newBoardHeight) {
                    Timber.i("create new board with w: $newBoardWidth, h: $newBoardHeight")
                    board = Array(newBoardWidth) { IntArray(newBoardHeight) }
                    if (::boardBitmap.isInitialized) {
                        boardBitmap.recycle()
                    }
                    Timber.i("create new bitmap")
                    boardBitmap = Bitmap.createBitmap(board.size, board[0].size, Bitmap.Config.ARGB_8888)
                }
            }

            if (::board.isInitialized) {

                if (canMergeNewPointList) {
                    newPointList.forEach {
                        board[it.x][it.y] = 1
                    }
                    newPointList.clear()
                }

                val gameResult =
                    if (!pause) {
                        conwayRule.generateChangedLifeList(board)
                    } else {
                        ArrayList()
                    }

                canvas.setBitmap(boardBitmap)

                gameResult.forEach {
                    if (it.isAlive) {
                        canvas.drawPoint(it.x.toFloat(), it.y.toFloat(), livePaint)
                    } else {
                        canvas.drawPoint(it.x.toFloat(), it.y.toFloat(), diePaint)
                    }
                }

                newPointList.forEach {
                    canvas.drawPoint(it.x.toFloat(), it.y.toFloat(), livePaint)
                }
                canvas.setBitmap(null)
                updateIntent.onNext(boardBitmap)
            }
        }

        while (true) {
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

            val startTime = System.currentTimeMillis()

            synchronized(this) {
                Timber.i("prepare to synchronized")

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
            }

            calculate(newPointList, canMergeNewPointList, newBoardWidth, newBoardHeight, pause)

            val timeSpent = System.currentTimeMillis() - startTime
            val expectTime = 1000 / frameRate

            Timber.v("timeSpent: $timeSpent")
            if (timeSpent < expectTime) {
                Timber.v("sleep ${expectTime - timeSpent}")
                sleep(expectTime - timeSpent)
            }
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
        }
    }
}