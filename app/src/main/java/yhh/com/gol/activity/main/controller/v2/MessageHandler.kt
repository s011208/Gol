package yhh.com.gol.activity.main.controller.v2

import android.os.Handler
import android.os.Looper
import android.os.Message
import timber.log.Timber

class MessageHandler(looper: Looper, private val gameRunner: GameRunner) : Handler(looper) {

    companion object {
        const val MSG_ADD_GRID_POINT = 0
        const val MSG_MERGE = 1
        const val MSG_SET_FRAME_RATE = 2
        const val MSG_CREATE_BOARD = 3
        const val MSG_GAME_PAUSE = 4
        const val MSG_GAME_RESUME = 5
        const val MSG_GAME_SLEEP = 6
        const val MSG_GAME_AWAKE = 7
        const val MSG_GAME_FINISH = 8
        const val MSG_RANDOM_ADD = 9
        const val MSG_CLEAR = 10
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
            MSG_GAME_SLEEP -> {
                gameRunner.sleep()
            }
            MSG_GAME_AWAKE -> {
                gameRunner.awake()
            }
            MSG_GAME_FINISH -> {
                gameRunner.finishGame()
            }
            MSG_RANDOM_ADD -> {
                gameRunner.randomAdd()
            }
            MSG_CLEAR -> {
                gameRunner.clear()
            }
        }
    }
}