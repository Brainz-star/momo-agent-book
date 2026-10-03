package com.momobook.agent.util

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.momobook.agent.data.DailySummary
import com.momobook.agent.data.Transaction
import java.io.File
import java.io.FileOutputStream
import java.util.Locale

object PdfReportGenerator {
    private const val PAGE_WIDTH = 595
    private const val PAGE_HEIGHT = 842
    private const val MARGIN = 36f
    private const val ROW_HEIGHT = 22f

    fun generate(
        context: Context,
        dateKey: String,
        summary: DailySummary,
        transactions: List<Transaction>
    ): File {
        val document = PdfDocument()
        var pageNumber = 1
        var page = document.startPage(PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create())
        var canvas = page.canvas
        var y = drawHeader(canvas, dateKey, summary)

        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            textSize = 8.5f
        }
        val bold = Paint(paint).apply { typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD) }

        y = drawTableHeader(canvas, y, bold)
        for (transaction in transactions) {
            if (y > PAGE_HEIGHT - 60f) {
                document.finishPage(page)
                pageNumber++
                page = document.startPage(PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create())
                canvas = page.canvas
                y = drawContinuationHeader(canvas, dateKey, pageNumber)
                y = drawTableHeader(canvas, y, bold)
            }
            y = drawTransactionRow(canvas, y, transaction, paint)
        }

        if (transactions.isNotEmpty() && y <= PAGE_HEIGHT - 60f) {
            drawFooterTotal(canvas, y, summary, bold)
        }

        document.finishPage(page)

        val reportsDir = File(context.cacheDir, "reports").apply { mkdirs() }
        val safeDate = dateKey.replace(Regex("[^0-9-]"), "-")
        val file = File(reportsDir, "MoMo-Agent-Book-Daily-Report-$safeDate.pdf")
        FileOutputStream(file).use { document.writeTo(it) }
        document.close()
        return file
    }

    fun uriFor(context: Context, file: File) =
        FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)

    private fun drawHeader(canvas: Canvas, dateKey: String, summary: DailySummary): Float {
        val title = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.BLACK
            textSize = 22f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val subtitle = Paint(Paint.ANTI_ALIAS_FLAG).apply { textSize = 11f }
        val label = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 9f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val value = Paint(Paint.ANTI_ALIAS_FLAG).apply { textSize = 10f }

        canvas.drawText("MoMo Agent Book", MARGIN, 52f, title)
        canvas.drawText("Daily Transaction Report", MARGIN, 70f, subtitle)
        canvas.drawText("Date: ${formatDateKey(dateKey)}", MARGIN, 88f, subtitle)

        val top = 108f
        val colWidth = (PAGE_WIDTH - 2 * MARGIN) / 2f
        val metrics = listOf(
            "Cash-In" to formatMoney(summary.cashIn),
            "Cash-Out" to formatMoney(summary.cashOut),
            "Total Value" to formatMoney(summary.totalValue),
            "Commission" to formatMoney(summary.commission),
            "Transactions" to summary.count.toString()
        )
        metrics.forEachIndexed { index, (name, amount) ->
            val col = index % 2
            val row = index / 2
            val x = MARGIN + col * colWidth
            val yy = top + row * 28f
            canvas.drawText(name, x, yy, label)
            canvas.drawText(amount, x, yy + 13f, value)
        }
        return top + 3 * 28f + 10f
    }

    private fun drawContinuationHeader(canvas: Canvas, dateKey: String, pageNumber: Int): Float {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 11f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        canvas.drawText("MoMo Agent Book — ${formatDateKey(dateKey)}", MARGIN, 38f, paint)
        val pagePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { textSize = 9f }
        canvas.drawText("Page $pageNumber", PAGE_WIDTH - MARGIN - 42f, 38f, pagePaint)
        return 60f
    }

    private fun drawTableHeader(canvas: Canvas, y: Float, paint: Paint): Float {
        val headerPaint = Paint(paint).apply { textSize = 8.5f }
        canvas.drawText("Time", 36f, y, headerPaint)
        canvas.drawText("Type", 78f, y, headerPaint)
        canvas.drawText("MoMo Number", 145f, y, headerPaint)
        canvas.drawText("Amount", 285f, y, headerPaint)
        canvas.drawText("Commission", 370f, y, headerPaint)
        canvas.drawText("Reference", 455f, y, headerPaint)
        canvas.drawLine(MARGIN, y + 5f, PAGE_WIDTH - MARGIN, y + 5f, Paint(paint))
        return y + 22f
    }

    private fun drawTransactionRow(canvas: Canvas, y: Float, transaction: Transaction, paint: Paint): Float {
        val rowPaint = Paint(paint).apply { textSize = 7.5f }
        canvas.drawText(formatTime(transaction.timestamp), 36f, y, rowPaint)
        canvas.drawText(transaction.type.label, 78f, y, rowPaint)
        canvas.drawText(maskMomoNumber(transaction.momoNumber), 145f, y, rowPaint)
        canvas.drawText(formatMoney(transaction.amount), 285f, y, rowPaint)
        canvas.drawText(formatMoney(transaction.commission), 370f, y, rowPaint)
        canvas.drawText(transaction.reference.take(18), 455f, y, rowPaint)
        canvas.drawLine(MARGIN, y + 6f, PAGE_WIDTH - MARGIN, y + 6f, Paint(rowPaint).apply { alpha = 45 })
        return y + ROW_HEIGHT
    }

    private fun drawFooterTotal(canvas: Canvas, y: Float, summary: DailySummary, paint: Paint) {
        val lineY = y + 2f
        canvas.drawLine(MARGIN, lineY, PAGE_WIDTH - MARGIN, lineY, paint)
        canvas.drawText("Daily total", 36f, lineY + 18f, paint)
        canvas.drawText(formatMoney(summary.totalValue), 285f, lineY + 18f, paint)
        canvas.drawText(formatMoney(summary.commission), 370f, lineY + 18f, paint)

        val footer = Paint(Paint.ANTI_ALIAS_FLAG).apply { textSize = 8f }
        canvas.drawText(
            "Generated by MoMo Agent Book • ${formatDateTime(System.currentTimeMillis())}",
            MARGIN,
            PAGE_HEIGHT - 24f,
            footer
        )
    }

    private fun maskMomoNumber(number: String): String {
        val normalized = number.trim()
        return if (normalized.length >= 7) {
            "${normalized.take(3)}***${normalized.takeLast(4)}"
        } else {
            normalized
        }
    }
}
