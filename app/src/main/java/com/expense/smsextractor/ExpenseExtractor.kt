package com.expense.smsextractor

import android.content.Context
import android.provider.Telephony
import android.util.Log
import java.text.SimpleDateFormat
import java.util.*

data class Expense(
    val amount: Double,
    val description: String,
    val date: String,
    val timestamp: Long,
    val sender: String
)

class ExpenseExtractor {
    companion object {
        private const val TAG = "ExpenseExtractorLog"
        private val expensePatterns = listOf(
            "(?:Rs|INR)\\s*(\\d{1,3}(?:,\\d{3})*(?:\\.\\d+)?|\\d+(?:\\.\\d+)?)",
            "₹\\s*(\\d+(?:\\.\\d+)?)",
            "\\$(\\d+(?:\\.\\d+)?)"
        )

        private val otpKeywords = listOf(
            "otp",
            "one time password",
            "verification code",
            "security code"
        )

        fun extractFromSms(context: Context): List<Expense> {
            Log.d(TAG, "Starting SMS extraction...")
            val contentResolver = context.contentResolver
            val cursor = contentResolver.query(
                Telephony.Sms.Inbox.CONTENT_URI,
                null,
                null,
                null,
                Telephony.Sms.DEFAULT_SORT_ORDER
            )

            val expenses = mutableListOf<Expense>()
            val seenMessages = mutableSetOf<String>()

            cursor?.use { c ->
                Log.d(TAG, "Found ${c.count} SMS messages in inbox.")
                val dateIndex = c.getColumnIndexOrThrow(Telephony.Sms.DATE)
                val bodyIndex = c.getColumnIndexOrThrow(Telephony.Sms.BODY)
                val addressIndex = c.getColumnIndexOrThrow(Telephony.Sms.ADDRESS)

                while (c.moveToNext()) {
                    val date = c.getLong(dateIndex)
                    val body = c.getString(bodyIndex)
                    val sender = c.getString(addressIndex)

                    if (isOtpMessage(body)) {
                        continue
                    }

                    val amount = extractAmount(body)
                    if (amount == null) {
                        continue
                    }

                    val expenseDate = Date(date)
                    val formattedDate = SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault()).format(expenseDate)
                    val description = extractDescription(body)

                    val expense = Expense(
                        amount = amount,
                        description = description,
                        date = formattedDate,
                        timestamp = expenseDate.time,
                        sender = sender
                    )

                    val messageKey = "${expense.amount}${expense.description}${expense.date}"
                    if (!seenMessages.contains(messageKey)) {
                        expenses.add(expense)
                        seenMessages.add(messageKey)
                    }
                }
            }

            Log.d(TAG, "Finished SMS extraction. Found ${expenses.size} total expenses.")
            return expenses
        }

        private fun isOtpMessage(message: String): Boolean {
            return otpKeywords.any { message.lowercase().contains(it) }
        }

        private fun extractAmount(message: String): Double? {
            for (pattern in expensePatterns) {
                val regex = Regex(pattern, RegexOption.IGNORE_CASE)
                val match = regex.find(message)
                if (match != null) {
                    return match.groupValues[1].replace(",", "").toDoubleOrNull()
                }
            }
            return null
        }

        private fun extractDescription(message: String): String {
            var description = message
            expensePatterns.forEach { pattern ->
                description = description.replace(Regex(pattern, RegexOption.IGNORE_CASE), "")
            }
            return description.trim()
        }
    }
}
