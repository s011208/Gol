package yhh.q1

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class FindDigit7Test {

    private lateinit var instance: FindDigit7

    @Before
    fun setup() {
        instance = FindDigit7()
    }

    @Test
    fun givenNegative_return0() {
        assertEquals(0, instance.count(-10))
    }

    @Test
    fun given0_return0() {
        assertEquals(0, instance.count(0))
    }

    @Test
    fun given6_return0() {
        assertEquals(0, instance.count(6))
    }

    @Test
    fun given7_return1() {
        assertEquals(1, instance.count(7))
    }

    @Test
    fun given17_return2() {
        assertEquals(2,  instance.count(17))
    }

    @Test
    fun given70_return8() {
        assertEquals(8,  instance.count(70))
    }

    @Test
    fun given100_return19() {
        assertEquals(19,  instance.count(100))
    }

    @Test
    fun given200_return38() {
        assertEquals(38,  instance.count(200))
    }

    @Test
    fun given699_return133() {
        assertEquals(133,  instance.count(699))
    }

    @Test
    fun given800_return233() {
        assertEquals(233,  instance.count(800))
    }

    @Test
    fun given1000_return271() {
        assertEquals(271,  instance.count(1000))
    }
}