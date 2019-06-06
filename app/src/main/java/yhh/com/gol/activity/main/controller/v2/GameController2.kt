package yhh.com.gol.activity.main.controller.v2

import android.os.HandlerThread
import android.os.Message
import yhh.com.gol.activity.main.controller.ConwayRule
import javax.inject.Inject

class GameController2 @Inject constructor(conwayRule: ConwayRule) {

    private val handlerThread = HandlerThread("GolMessageThread")

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

    fun sleep() {
        handler.sendMessage(Message.obtain()
            .also {
                it.what = MessageHandler.MSG_GAME_SLEEP
            }
        )
    }

    fun awake() {
        handler.sendMessage(Message.obtain()
            .also {
                it.what = MessageHandler.MSG_GAME_AWAKE
            }
        )
    }

    fun finish() {
        handler.sendMessage(Message.obtain()
            .also {
                it.what = MessageHandler.MSG_GAME_FINISH
            }
        )
        handlerThread.quitSafely()
    }

    fun randomAdd() {
        handler.sendMessage(Message.obtain()
            .also {
                it.what = MessageHandler.MSG_RANDOM_ADD
            }
        )
    }
}