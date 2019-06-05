package yhh.com.gol.activity.main.controller.v1

import androidx.annotation.VisibleForTesting
import timber.log.Timber
import javax.inject.Inject

class ConwayRule @Inject constructor() {

    companion object {
        private const val BORN = 3
        private const val SURVIVE_UP = 3
        private const val SURVIVE_BOTTOM = 2
    }

    private var runningTimes = 0L
    private var totalTimes = 0L

    fun generateChangedLifeList(board: Array<IntArray>): ArrayList<GamePoint> {
        if (board.isEmpty()) return ArrayList()
        val time = System.currentTimeMillis()
        val rtn = ArrayList<GamePoint>()

        val algorithmTime = System.currentTimeMillis()
        for (row in 0 until board.size) {
            for (column in 0 until board[row].size) {
                val neighbors = countNeighbor(board, row, column)
                if (board[row][column] == 1) {
                    if (neighbors in SURVIVE_BOTTOM..SURVIVE_UP) {
                        continue
                    } else {
                        rtn.add(GamePoint(row, column, false))
                    }
                } else {
                    if (neighbors == BORN) {
                        rtn.add(GamePoint(row, column, true))
                    }
                }
            }
        }
        Timber.v("algorithmTime: ${System.currentTimeMillis() - algorithmTime}")

        rtn.forEach {
            board[it.x][it.y] = if (it.isAlive) 1 else 0
        }
        ++runningTimes
        totalTimes += System.currentTimeMillis() - time
        Timber.v("result size: ${rtn.size}, average time: ${totalTimes / runningTimes}")
        return rtn
    }

    @VisibleForTesting
    fun countNeighbor(board: Array<IntArray>, rawIndex: Int, columnIndex: Int): Int {
        var neighbors = 0
        for (row in rawIndex - 1..rawIndex + 1) {
            for (column in columnIndex - 1..columnIndex + 1) {
                if (row >= 0 && row < board.size && column >= 0 && column < board[0].size) {
                    neighbors += board[row][column]
                }
            }
        }
        return neighbors - board[rawIndex][columnIndex]
    }
}