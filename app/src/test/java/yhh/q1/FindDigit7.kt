package yhh.q1

internal class FindDigit7 {

    companion object {
        private const val DIGIT = 7
    }

    /**
     * count(10^numberOfTenPower) = 9 * count(10^(numberOfTenPower-1)) + 10^(numberOfTenPower - 1)
     */
    fun count(number: Int): Int {
        if (number < DIGIT) return 0

        val numberOfTenPower = Math.log10(number.toDouble()).toInt()

        // something like 1000 or 100
        val numberOfDigit = Math.pow(10.0, numberOfTenPower.toDouble()).toInt()

        val firstDigit = number / numberOfDigit

        System.out.println("numberOfTenPower: $numberOfTenPower, numberOfDigit: $numberOfDigit, firstDigit: $firstDigit")

        // 10 ^ 0 => 0
        // 10 ^ 1 => 1
        // 10 ^ 2 => digitArray[1] * 9 + 10
        // 10 ^ 3 => digitArray[2] * 19 + 100
        val digitArray = IntArray(numberOfTenPower + 2)
        digitArray[0] = 0
        digitArray[1] = 1

        for (i in 2..numberOfTenPower) {
            digitArray[i] = digitArray[i - 1] * 9 + Math.pow(10.0, i - 1.0).toInt()
        }

        return when {
            firstDigit == DIGIT -> firstDigit * digitArray[numberOfTenPower] + (number % numberOfDigit) + 1
            firstDigit > DIGIT -> (firstDigit - 1) * digitArray[numberOfTenPower] + numberOfDigit + count(number % numberOfDigit)
            else -> firstDigit * digitArray[numberOfTenPower] + count(number % numberOfDigit)
        }
    }
}