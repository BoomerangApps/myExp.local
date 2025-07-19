package com.expense.smsextractor.data

data class MonthYear(
    val year: Int,
    val month: Int,
    val monthName: String? = null
) {
    companion object {
        val DEFAULT: MonthYear = MonthYear(0, 0, "All")

        fun fromDateString(dateString: String): MonthYear? {
            // Assuming date format is "dd-MM-yyyy"
            try {
                val parts = dateString.split("-")
                if (parts.size == 3) {
                    val month = parts[1].toInt()
                    return MonthYear(
                        year = parts[2].toInt(),
                        month = month,
                        monthName = getMonthName(month)
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return null
        }

        private fun getMonthName(month: Int): String? {
            return when (month) {
                1 -> "Jan"
                2 -> "Feb"
                3 -> "Mar"
                4 -> "Apr"
                5 -> "May"
                6 -> "Jun"
                7 -> "Jul"
                8 -> "Aug"
                9 -> "Sep"
                10 -> "Oct"
                11 -> "Nov"
                12 -> "Dec"
                else -> null
            }
        }
    }
}