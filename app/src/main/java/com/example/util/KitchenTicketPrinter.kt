package com.example.util

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.os.Bundle
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.print.PageRange
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import android.print.PrintManager
import com.example.data.model.OrderItem
import java.io.FileOutputStream
import java.io.IOException

data class KitchenTicketData(
    val origin: String,
    val items: List<OrderItem>,
    val notes: String,
    val timestamp: Long = System.currentTimeMillis()
) {
    fun getItemsByCategory(): Map<String, List<OrderItem>> {
        return items.groupBy { it.category }
    }

    fun toFormattedText(): String {
        val sb = StringBuilder()
        sb.appendLine("================================")
        sb.appendLine("     🍕 PIZZAPOS - COCINA       ")
        sb.appendLine("================================")
        sb.appendLine("ORIGEN: $origin")
        sb.appendLine("FECHA:  ${CurrencyUtils.formatDate(timestamp)}")
        sb.appendLine("--------------------------------")
        sb.appendLine("ITEMS DE LA COMANDA:")
        getItemsByCategory().forEach { (category, categoryItems) ->
            sb.appendLine("\n[$category]")
            categoryItems.forEach { item ->
                sb.appendLine("  ${item.quantity}x ${item.name}")
            }
        }
        if (notes.isNotBlank()) {
            sb.appendLine("--------------------------------")
            sb.appendLine("OBSERVACIONES:")
            sb.appendLine("  $notes")
        }
        sb.appendLine("================================")
        return sb.toString()
    }
}

class ThermalKitchenTicketAdapter(
    private val ticket: KitchenTicketData
) : PrintDocumentAdapter() {

    // 72mm is ~204 PostScript points (72dpi), 80mm is ~226pt
    private val pageWidthPt = 204

    override fun onLayout(
        oldAttributes: PrintAttributes?,
        newAttributes: PrintAttributes?,
        cancellationSignal: CancellationSignal?,
        callback: LayoutResultCallback?,
        extras: Bundle?
    ) {
        if (cancellationSignal?.isCanceled == true) {
            callback?.onLayoutCancelled()
            return
        }

        val info = PrintDocumentInfo.Builder("Comanda_${ticket.origin.replace(" ", "_")}.pdf")
            .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
            .setPageCount(1)
            .build()

        callback?.onLayoutFinished(info, true)
    }

    override fun onWrite(
        pages: Array<out PageRange>?,
        destination: ParcelFileDescriptor?,
        cancellationSignal: CancellationSignal?,
        callback: WriteResultCallback?
    ) {
        if (cancellationSignal?.isCanceled == true) {
            callback?.onWriteCancelled()
            return
        }

        val document = PdfDocument()

        // Calculate dynamic page height based on lines
        val grouped = ticket.getItemsByCategory()
        var lineCount = 8 // header, origin, date, separators
        grouped.forEach { (_, items) ->
            lineCount += 2 + items.size // category header + items
        }
        if (ticket.notes.isNotBlank()) {
            lineCount += 3
        }
        val lineHeight = 14
        val pageHeightPt = maxOf(300, (lineCount * lineHeight) + 60)

        val pageInfo = PdfDocument.PageInfo.Builder(pageWidthPt, pageHeightPt, 1).create()
        val page = document.startPage(pageInfo)
        val canvas: Canvas = page.canvas

        val paintText = Paint().apply {
            color = Color.BLACK
            textSize = 9f
            typeface = Typeface.MONOSPACE
            isAntiAlias = true
        }

        val paintBold = Paint().apply {
            color = Color.BLACK
            textSize = 10f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            isAntiAlias = true
        }

        val paintTitle = Paint().apply {
            color = Color.BLACK
            textSize = 13f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            isAntiAlias = true
        }

        var y = 24f
        val left = 10f

        fun drawDivider(dash: Boolean = false) {
            val oldStyle = paintText.typeface
            val dividerText = if (dash) "----------------------------" else "============================"
            canvas.drawText(dividerText, left, y, paintText)
            y += lineHeight
        }

        // Header
        canvas.drawText("🍕 PIZZAPOS", left + 36f, y, paintTitle)
        y += lineHeight + 2
        canvas.drawText("** COMANDA DE COCINA **", left + 8f, y, paintBold)
        y += lineHeight
        drawDivider(false)

        // Metadata
        canvas.drawText("ORIGEN: ${ticket.origin}", left, y, paintBold)
        y += lineHeight
        canvas.drawText("FECHA:  ${CurrencyUtils.formatDate(ticket.timestamp)}", left, y, paintText)
        y += lineHeight
        drawDivider(true)

        // Items grouped by category
        grouped.forEach { (category, items) ->
            canvas.drawText("[${category.uppercase()}]", left, y, paintBold)
            y += lineHeight
            items.forEach { item ->
                canvas.drawText("  ${item.quantity}x ${item.name}", left, y, paintText)
                y += lineHeight
            }
            y += 4f
        }

        // Notes
        if (ticket.notes.isNotBlank()) {
            drawDivider(true)
            canvas.drawText("OBSERVACIONES:", left, y, paintBold)
            y += lineHeight
            // Wrap or draw note
            canvas.drawText("  ${ticket.notes}", left, y, paintText)
            y += lineHeight
        }

        drawDivider(false)
        canvas.drawText("*** FIN COMANDA ***", left + 30f, y, paintText)

        document.finishPage(page)

        try {
            destination?.fileDescriptor?.let { fd ->
                FileOutputStream(fd).use { out ->
                    document.writeTo(out)
                }
            }
            callback?.onWriteFinished(arrayOf(PageRange.ALL_PAGES))
        } catch (e: IOException) {
            callback?.onWriteFailed(e.message)
        } finally {
            document.close()
        }
    }
}

object KitchenTicketPrinter {
    fun printTicket(
        context: Context,
        origin: String,
        items: List<OrderItem>,
        notes: String
    ): Boolean {
        if (items.isEmpty()) return false
        val ticketData = KitchenTicketData(origin = origin, items = items, notes = notes)
        val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager ?: return false

        val jobName = "Comanda_${origin.replace(" ", "_")}"
        val adapter = ThermalKitchenTicketAdapter(ticketData)
        val attributes = PrintAttributes.Builder()
            .setMediaSize(PrintAttributes.MediaSize.ISO_A6) // Compact ticket size
            .build()

        printManager.print(jobName, adapter, attributes)
        return true
    }
}
