package yhh.com.gol.activity.main.domain

import android.graphics.Bitmap

sealed class State {
    data class UpdateGameView(val bitmap: Bitmap) : State()

    data class InitGameView(val width: Int, val height: Int) : State()
}