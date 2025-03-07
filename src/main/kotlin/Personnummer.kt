package personnummer

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

/**
 * Class used to validate Swedish personal identity numbers.
 *
 * @author Simon Sawert
 */

class Personnummer {
    companion object {
        val PNR_REGEX = "^(\\d{2})?(\\d{2})(\\d{2})(\\d{2})([-+]?)?((?!000)\\d{3})(\\d?)$".toRegex()
        val DATE_TIME_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("yy-MM-dd")
        const val ASCII_REDUCE = 48

        /**
         * Validates if a given string is a valid Swedish personal identity number.
         *
         * @return true or false.
         */
        fun valid(pnr: String): Boolean {
            return Personnummer().valid(pnr)
        }

        /**
         * Validates if a given long value is a valid Swedish personal identity number.
         *
         * @return true or false.
         */
        fun valid(pnr: Long): Boolean {
            return valid("$pnr")
        }
    }

    /**
     * Validates if a given string is a valid Swedish personal identity number.
     * @return true or false.
     */
    fun valid(pnr: String): Boolean {
        val matchResult = PNR_REGEX.find(pnr) ?: return false

        val year: Int
        val month: Int
        val day: Int
        val control: Int
        val number: Int

        try {
            val y = parseYear(matchResult.groupValues[2])

            year = y.toInt()
            month = matchResult.groupValues[3].toInt()
            day = matchResult.groupValues[4].toInt()
            control = matchResult.groupValues[7].toInt()
            number = matchResult.groupValues[6].toInt()
        } catch (e: NumberFormatException) {
            return false
        }

        val luhnValue: Int = luhn("%02d%02d%02d%03d0".format(year, month, day, number))

        return (luhnValue == control) && validDate(year, month, day)
    }

    private fun parseYear(year : String) : String = if (year.length == 4) year.substring(2) else year

    /**
     * Validates if a given long value is a valid Swedish personal identity number.
     * @return true or false.
     */
    fun valid(pnr: Long): Boolean {
        return valid("$pnr")
    }

    /**
     * Calculate the luhn check sum, see https://en.wikipedia.org/wiki/Luhn_algorithm
     * @return the checksum as integer.
     */
    private fun luhn(value: String): Int {
        var sum: Int = 0
        var temp: Int

        for (i in 0 until value.length - 1) {
            temp = value[i].code - ASCII_REDUCE

            if (i % 2 == 0) {
                temp *= 2

                if (temp > 9) {
                    temp -= 9
                }
            }

            sum += temp
        }

        val controlNumber = 10 - sum % 10
        return if (controlNumber == 10) 0 else controlNumber
    }

    /**
     * Combine year (from two digits), month and day to see if it's a valid date. Also supports co-ordination numbers
     * by taking the remainder of modulus 60.
     * @return true or false.
     */
    private fun validDate(year: Int, month: Int, day: Int): Boolean {
        val finalDay = if (day > 60) day - 60 else day

        try {
            LocalDate.parse("%02d-%02d-%02d".format(year, month, finalDay), DATE_TIME_FORMATTER)
        } catch (e: DateTimeParseException) {
            return false
        }

        return true
    }
}
