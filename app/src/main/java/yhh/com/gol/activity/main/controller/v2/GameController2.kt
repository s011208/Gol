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

    init {
        handlerThread.start()
        handler = MessageHandler(handlerThread.looper, conwayRule)
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
}

private class MessageHandler(looper: Looper, conwayRule: ConwayRule) : Handler(looper) {

    companion object {
        const val MSG_ADD_GRID_POINT = 0
        const val MSG_MERGE = 1
        const val MSG_SET_FRAME_RATE = 2
        const val MSG_CREATE_BOARD = 3
    }

    private val gameRunner = GameRunner(conwayRule)

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
        }
    }
}

private class GameRunner(private val conwayRule: ConwayRule) : Thread() {
    private val newPointList = ArrayList<Point>()

    private var canMergeNewPointList = false

    private var frameRate = 60

    private var newBoardWidth = -1

    private var newBoardHeight = -1

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
            newBoardHeight: Int
        ) {
            Timber.w("calculate")

            if (newBoardHeight > 0 && newBoardWidth > 0) {
                if (!::board.isInitialized || board.size != newBoardWidth || board[0].size != newBoardHeight) {
                    Timber.i("create new board with w: $newBoardWidth, h: $newBoardHeight")
                    board = Array(newBoardWidth) { IntArray(newBoardHeight) }
                }
            }
        }

        while (true) {
            if (updateIntent.hasComplete() || !updateIntent.hasObservers()) {
                Timber.v("updateIntent.hasComplete(): ${updateIntent.hasComplete()}, updateIntent.hasObservers(): ${updateIntent.hasObservers()}, ignore")
                sleep(1000L / frameRate)
                continue
            }

            val newPointList = ArrayList<Point>()
            val frameRate: Int
            val canMergeNewPointList: Boolean
            val newBoardWidth: Int
            val newBoardHeight: Int
            synchronized(this) {
                Timber.i("prepare to synchronized")

                frameRate = this.frameRate
                canMergeNewPointList = this.canMergeNewPointList
                newPointList.addAll(this.newPointList)
                newBoardWidth = this.newBoardWidth
                newBoardHeight = this.newBoardHeight

                if (canMergeNewPointList) {
                    this.newPointList.clear()
                    this.canMergeNewPointList = false
                }
            }

            calculate(newPointList, canMergeNewPointList, newBoardWidth, newBoardHeight)

            sleep(1000)
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
}

//private data class GridPoint(val x: Int, val y: Int, var times: Int = 0, var isAlive: Boolean)