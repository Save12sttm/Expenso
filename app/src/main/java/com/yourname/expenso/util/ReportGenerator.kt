package com.yourname.expenso.util

import android.content.Context
import android.os.Environment
import com.yourname.expenso.model.Transaction
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReportGenerator @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val displayDateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

    fun generateCSVReport(
        transactions: List<Transaction>,
        startDate: Long,
        endDate: Long
    ): String {
        val filteredTransactions = transactions.filter { 
            it.date >= startDate && it.date <= endDate 
        }

        val fileName = "expenso_report_${dateFormat.format(Date())}.csv"
        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val file = File(downloadsDir, fileName)

        try {
            FileWriter(file).use { writer ->
                // CSV Header
                writer.append("Date,Title,Amount,Type,Category\n")
                
                // CSV Data
                filteredTransactions.forEach { transaction ->
                    writer.append("${displayDateFormat.format(Date(transaction.date))},")
                    writer.append("\"${transaction.title}\",")
                    writer.append("${transaction.amount},")
                    writer.append("${transaction.type},")
                    writer.append("General\n") // Placeholder category
                }
            }
            return "Report saved as $fileName in Downloads folder"
        } catch (e: Exception) {
            return "Error generating report: ${e.message}"
        }
    }

    fun generateSummaryReport(
        transactions: List<Transaction>,
        startDate: Long,
        endDate: Long
    ): String {
        val filteredTransactions = transactions.filter { 
            it.date >= startDate && it.date <= endDate 
        }

        val income = filteredTransactions.filter { it.type == "Income" }.sumOf { it.amount }
        val expense = filteredTransactions.filter { it.type == "Expense" }.sumOf { it.amount }
        val balance = income - expense

        val categoryBreakdown = filteredTransactions
            .filter { it.type == "Expense" }
            .groupBy { "General" } // Placeholder until category system is integrated
            .mapValues { it.value.sumOf { transaction -> transaction.amount } }

        val startDateStr = displayDateFormat.format(Date(startDate))
        val endDateStr = displayDateFormat.format(Date(endDate))

        return """
        📊 Financial Report ($startDateStr - $endDateStr)
        
        💰 Summary:
        • Total Income: ₹${String.format("%.2f", income)}
        • Total Expenses: ₹${String.format("%.2f", expense)}
        • Net Balance: ₹${String.format("%.2f", balance)}
        
        📈 Expense Breakdown:
        ${categoryBreakdown.entries.joinToString("\n") { 
            "• ${it.key}: ₹${String.format("%.2f", it.value)}"
        }}
        
        📝 Transaction Count: ${filteredTransactions.size}
        """.trimIndent()
    }
}