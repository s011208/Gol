package yhh.com.gol.activity.domain

import android.graphics.Bitmap

sealed class State {
    data class UpdateGameView(val bitmap: Bitmap) : State()
}