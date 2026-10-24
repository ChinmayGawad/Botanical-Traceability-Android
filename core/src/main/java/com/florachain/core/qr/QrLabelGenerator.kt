package com.florachain.core.qr

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import androidx.print.PrintHelper
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import java.util.EnumMap

object QrLabelGenerator {

    /**
     * Generates a raw monochrome QR Code Bitmap.
     */
    fun generateQrBitmap(
        content: String,
        width: Int = 512,
        height: Int = 512
    ): Bitmap {
        val hints = EnumMap<EncodeHintType, Any>(EncodeHintType::class.java).apply {
            put(EncodeHintType.MARGIN, 1)
            put(EncodeHintType.CHARACTER_SET, "UTF-8")
        }

        val writer = QRCodeWriter()
        val bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, width, height, hints)

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val darkColor = Color.rgb(6, 78, 59) // Deep Forest Green (#064E3B)
        val lightColor = Color.WHITE

        for (x in 0 until width) {
            for (y in 0 until height) {
                bitmap.setPixel(x, y, if (bitMatrix[x, y]) darkColor else lightColor)
            }
        }
        return bitmap
    }

    /**
     * Generates a complete print-ready packaging label sticker with botanical monograph,
     * batch identifier, and verification badge.
     */
    fun generatePrintableLabel(
        batchId: String,
        productName: String,
        botanicalName: String,
        category: String,
        verificationUrl: String,
        labelType: LabelType = LabelType.CARTON
    ): Bitmap {
        val (labelWidth, labelHeight) = when (labelType) {
            LabelType.JAR -> Pair(600, 320)
            LabelType.CARTON -> Pair(800, 480)
            LabelType.PALLET -> Pair(1200, 720)
        }

        val bitmap = Bitmap.createBitmap(labelWidth, labelHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // 1. Clean white background with subtle rounded border
        val bgPaint = Paint().apply {
            color = Color.WHITE
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        canvas.drawRoundRect(RectF(0f, 0f, labelWidth.toFloat(), labelHeight.toFloat()), 24f, 24f, bgPaint)

        val borderPaint = Paint().apply {
            color = Color.rgb(16, 185, 129) // Emerald
            style = Paint.Style.STROKE
            strokeWidth = 6f
            isAntiAlias = true
        }
        canvas.drawRoundRect(RectF(3f, 3f, labelWidth.toFloat() - 3f, labelHeight.toFloat() - 3f), 24f, 24f, borderPaint)

        // 2. Generate and draw QR Code
        val qrSize = (labelHeight * 0.72f).toInt()
        val qrBitmap = generateQrBitmap(verificationUrl, qrSize, qrSize)
        val qrLeft = labelWidth - qrSize - 32f
        val qrTop = (labelHeight - qrSize) / 2f
        canvas.drawBitmap(qrBitmap, qrLeft, qrTop, null)

        // 3. Draw Brand & Botanical Typography
        val titlePaint = Paint().apply {
            color = Color.rgb(15, 23, 42) // Dark Slate
            textSize = labelHeight * 0.085f
            isFakeBoldText = true
            isAntiAlias = true
        }

        val subtitlePaint = Paint().apply {
            color = Color.rgb(16, 185, 129) // Emerald
            textSize = labelHeight * 0.055f
            isAntiAlias = true
        }

        val metaPaint = Paint().apply {
            color = Color.rgb(100, 116, 139) // Slate muted
            textSize = labelHeight * 0.048f
            isAntiAlias = true
        }

        val monoPaint = Paint().apply {
            color = Color.rgb(6, 78, 59)
            textSize = labelHeight * 0.052f
            isFakeBoldText = true
            isAntiAlias = true
        }

        var textY = 64f
        canvas.drawText("FLORACHAIN AUTHENTIC", 40f, textY, subtitlePaint)

        textY += labelHeight * 0.12f
        val safeTitle = if (productName.length > 24) productName.take(22) + "..." else productName
        canvas.drawText(safeTitle, 40f, textY, titlePaint)

        textY += labelHeight * 0.08f
        canvas.drawText(botanicalName, 40f, textY, subtitlePaint)

        textY += labelHeight * 0.10f
        canvas.drawText("Batch ID: $batchId", 40f, textY, monoPaint)

        textY += labelHeight * 0.07f
        canvas.drawText("Category: $category • Polygon Amoy", 40f, textY, metaPaint)

        textY += labelHeight * 0.09f
        canvas.drawText("Scan with phone camera to verify provenance", 40f, textY, metaPaint)

        return bitmap
    }

    /**
     * Sends the bitmap to the native Android Print Spooler.
     */
    fun printLabel(context: Context, jobName: String, bitmap: Bitmap) {
        val printHelper = PrintHelper(context).apply {
            scaleMode = PrintHelper.SCALE_MODE_FIT
        }
        printHelper.printBitmap(jobName, bitmap)
    }

    enum class LabelType {
        JAR,
        CARTON,
        PALLET
    }
}
