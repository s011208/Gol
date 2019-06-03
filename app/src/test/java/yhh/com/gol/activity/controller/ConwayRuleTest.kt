package yhh.com.gol.activity.controller

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import yhh.com.gol.activity.main.controller.ConwayRule
import yhh.com.gol.activity.main.controller.GamePoint

class ConwayRuleTest {

    private lateinit var conwayRule: ConwayRule

    @Before
    fun setup() {
        conwayRule = ConwayRule()
    }

    @Test
    fun countNeighbors_return2() {
        val board = Array(3) { IntArray(3) }
        board[0][0] = 1
        board[0][1] = 1

        assertEquals(2, conwayRule.countNeighbor(board, 1, 1))
    }

    @Test
    fun countNeighbors_return0() {
        val board = Array(3) { IntArray(3) }

        assertEquals(0, conwayRule.countNeighbor(board, 1, 1))
    }

    @Test
    fun countNeighbors_return8() {
        val board = Array(3) { IntArray(3) }
        board[0][0] = 1
        board[0][1] = 1
        board[0][2] = 1
        board[1][0] = 1
        board[1][1] = 1
        board[1][2] = 1
        board[2][0] = 1
        board[2][1] = 1
        board[2][2] = 1

        assertEquals(8, conwayRule.countNeighbor(board, 1, 1))
    }

    @Test
    fun generateChangedLifeList5x10_listEquals() {
        val board = Array(5) { IntArray(10) }
        board[2][1] = 1
        board[2][2] = 1
        board[2][3] = 1

        val resultList = conwayRule.generateChangedLifeList(board)

        resultList.forEach {
            System.out.println(it)
        }

        assertEquals(4, resultList.size)
        assertTrue(resultList.contains(GamePoint(2, 1, false)))
        assertTrue(resultList.contains(GamePoint(2, 3, false)))
        assertTrue(resultList.contains(GamePoint(1, 2, true)))
        assertTrue(resultList.contains(GamePoint(3, 2, true)))
    }

    @Test
    fun generateChangedLifeList6x10_listEquals() {
        val board = Array(6) { IntArray(10) }
        board[2][2] = 1
        board[3][2] = 1
        board[4][2] = 1
        board[1][3] = 1
        board[2][3] = 1
        board[3][3] = 1

        val resultList = conwayRule.generateChangedLifeList(board)

        assertEquals(8, resultList.size)
        assertTrue(resultList.contains(GamePoint(2, 2, false)))
        assertTrue(resultList.contains(GamePoint(3, 1, true)))
        assertTrue(resultList.contains(GamePoint(2, 4, true)))
    }
}