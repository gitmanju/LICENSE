package com.dheemai.treecounter.data

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

object PdfExportService {

    private val fmt     = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
    private val dateFmt = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

    private const val PAGE_WIDTH  = 595
    private const val PAGE_HEIGHT = 842
    private const val MARGIN      = 48f
    private const val PHOTO_SIZE  = 80f   // thumbnail side length in points

    private val paintTitle    by lazy { Paint().apply { color = Color.rgb(46,125,50); textSize = 22f; isFakeBoldText = true } }
    private val paintSubtitle by lazy { Paint().apply { color = Color.DKGRAY; textSize = 11f } }
    private val paintSection  by lazy { Paint().apply { color = Color.rgb(46,125,50); textSize = 13f; isFakeBoldText = true } }
    private val paintBody     by lazy { Paint().apply { color = Color.BLACK; textSize = 11f } }
    private val paintBold     by lazy { Paint().apply { color = Color.BLACK; textSize = 11f; isFakeBoldText = true } }
    private val paintMuted    by lazy { Paint().apply { color = Color.GRAY; textSize = 10f } }
    private val paintDivider  by lazy { Paint().apply { color = Color.LTGRAY; strokeWidth = 1f } }
    private val paintHeaderBold by lazy { Paint().apply { color = Color.BLACK; textSize = 12f; isFakeBoldText = true } }

    fun exportAndShare(context: Context, trees: List<Tree>, ownerName: String, plotName: String) {
        val file = generate(context, trees, ownerName, plotName)
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
        }
        context.startActivity(intent)
    }

    private fun generate(context: Context, trees: List<Tree>, ownerName: String, plotName: String): File {
        val document = PdfDocument()
        var pageNumber = 1

        // ── Output 1: Tree list with photos ───────────────────────────────────
        var page = document.startPage(PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create())
        var canvas = page.canvas
        var y = MARGIN

        fun finishAndNewPage() {
            canvas.drawText("TreeSpecNMap  •  Page $pageNumber", MARGIN, PAGE_HEIGHT - MARGIN, paintMuted)
            document.finishPage(page)
            pageNumber++
            page = document.startPage(PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create())
            canvas = page.canvas
            y = MARGIN
        }

        fun newPageIfNeeded(space: Float) {
            if (y + space > PAGE_HEIGHT - MARGIN - 20f) finishAndNewPage()
        }

        fun divider() {
            canvas.drawLine(MARGIN, y, PAGE_WIDTH - MARGIN, y, paintDivider)
            y += 10f
        }

        // Header
        canvas.drawText("Tree Counter Report", MARGIN, y, paintTitle)
        y += 28f
        canvas.drawText("Owner:", MARGIN, y, paintSubtitle)
        canvas.drawText(ownerName.ifBlank { "—" }, MARGIN + 48f, y, paintHeaderBold)
        y += 18f
        canvas.drawText("Farm Plot:", MARGIN, y, paintSubtitle)
        canvas.drawText(plotName.ifBlank { "—" }, MARGIN + 70f, y, paintHeaderBold)
        y += 18f
        canvas.drawText(
            "Generated: ${dateFmt.format(Date())}   |   Total trees: ${trees.size}",
            MARGIN, y, paintSubtitle
        )
        y += 20f
        divider()
        y += 8f

        // Tree entries
        val textRight = PAGE_WIDTH - MARGIN - PHOTO_SIZE - 12f  // text stays left of photo column

        trees.forEachIndexed { index, tree ->
            val notation = "T${index + 1}"

            // Estimate block height: base lines + notes + photo
            val noteLines = if (tree.notes.isNotBlank()) {
                val words = tree.notes.split(" ")
                var line = ""; var count = 1
                for (word in words) {
                    val test = if (line.isEmpty()) word else "$line $word"
                    if (paintBody.measureText(test) > textRight - MARGIN - 70f) { count++; line = word } else line = test
                }
                count
            } else 0
            val hasPhoto = tree.photoPath != null && File(tree.photoPath).exists()
            val textHeight = 20f + 20f + 20f + 20f + (noteLines * 18f)  // section + 3 fields + notes
            val blockHeight = maxOf(textHeight, if (hasPhoto) PHOTO_SIZE + 8f else 0f) + 16f

            newPageIfNeeded(blockHeight)

            val blockTop = y

            // T-notation + species
            canvas.drawText("$notation  —  ${tree.species}", MARGIN, y, paintSection)
            y += 20f

            if (tree.additionalNames.isNotBlank()) {
                val names = tree.additionalNames.split(",").filter { it.isNotBlank() }.joinToString(", ")
                canvas.drawText("Also known as:", MARGIN, y, paintBold)
                canvas.drawText(names, MARGIN + 100f, y, paintBody)
                y += 18f
            }

            canvas.drawText("Location:", MARGIN, y, paintBold)
            canvas.drawText(
                "Lat %.7f   Lon %.7f".format(tree.latitude, tree.longitude),
                MARGIN + 70f, y, paintBody
            )
            y += 18f

            canvas.drawText("Logged:", MARGIN, y, paintBold)
            canvas.drawText(fmt.format(Date(tree.timestamp)), MARGIN + 70f, y, paintBody)
            y += 18f

            if (tree.notes.isNotBlank()) {
                canvas.drawText("Notes:", MARGIN, y, paintBold)
                var line = ""
                for (word in tree.notes.split(" ")) {
                    val test = if (line.isEmpty()) word else "$line $word"
                    if (paintBody.measureText(test) > textRight - MARGIN - 70f) {
                        canvas.drawText(line, MARGIN + 70f, y, paintBody)
                        y += 18f
                        line = word
                    } else line = test
                }
                if (line.isNotEmpty()) {
                    canvas.drawText(line, MARGIN + 70f, y, paintBody)
                    y += 18f
                }
            }

            // Photo thumbnail on the right
            if (hasPhoto) {
                try {
                    val opts = BitmapFactory.Options().apply { inSampleSize = 4 }
                    val bmp = BitmapFactory.decodeFile(tree.photoPath, opts)
                    if (bmp != null) {
                        val photoLeft = PAGE_WIDTH - MARGIN - PHOTO_SIZE
                        val photoTop = blockTop - 4f
                        val src = Rect(0, 0, bmp.width, bmp.height)
                        val dst = RectF(photoLeft, photoTop, photoLeft + PHOTO_SIZE, photoTop + PHOTO_SIZE)
                        canvas.drawBitmap(bmp, src, dst, null)
                        bmp.recycle()
                    }
                } catch (_: Exception) {}
            }

            y = maxOf(y, if (hasPhoto) blockTop - 4f + PHOTO_SIZE + 4f else y)
            y += 8f
            divider()
            y += 4f
        }

        canvas.drawText("TreeSpecNMap  •  Page $pageNumber", MARGIN, PAGE_HEIGHT - MARGIN, paintMuted)
        document.finishPage(page)

        // ── Output 2: Grid map (T-notation) ───────────────────────────────────
        if (trees.isNotEmpty()) {
            pageNumber++
            val mapPage = document.startPage(
                PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
            )
            drawMapPage(mapPage.canvas, trees, pageNumber)
            document.finishPage(mapPage)
        }

        // Save
        val pdfDir = File(context.filesDir, "pdfs").also { it.mkdirs() }
        val file = File(pdfDir, "tree_report_${System.currentTimeMillis()}.pdf")
        FileOutputStream(file).use { document.writeTo(it) }
        document.close()
        return file
    }

    private fun drawMapPage(canvas: Canvas, trees: List<Tree>, pageNum: Int) {
        val pTitle  = Paint().apply { color = Color.rgb(46,125,50); textSize = 16f; isFakeBoldText = true }
        val pAxis   = Paint().apply { color = Color.DKGRAY; textSize = 7f }
        val pAxisR  = Paint().apply { color = Color.DKGRAY; textSize = 7f; textAlign = Paint.Align.RIGHT }
        val pGrid   = Paint().apply { color = Color.rgb(220,220,220); strokeWidth = 0.5f }
        val pBorder = Paint().apply { color = Color.DKGRAY; strokeWidth = 1.5f; style = Paint.Style.STROKE }
        val pFill   = Paint().apply { color = Color.rgb(46,125,50); style = Paint.Style.FILL }
        val pRing   = Paint().apply { color = Color.WHITE; style = Paint.Style.FILL }
        val pNum    = Paint().apply { color = Color.WHITE; textSize = 7f; isFakeBoldText = true; textAlign = Paint.Align.CENTER }
        val pMuted  = Paint().apply { color = Color.GRAY; textSize = 9f }

        // Use canvas (manual) positions if all trees have been placed, otherwise GPS
        val useCanvas = trees.all { it.canvasX >= 0f }

        val mapLeft   = if (useCanvas) MARGIN else MARGIN + 60f
        val mapRight  = PAGE_WIDTH - MARGIN - 10f
        val mapTop    = MARGIN + 35f
        val mapBottom = PAGE_HEIGHT - MARGIN - 20f
        val mapW = mapRight - mapLeft
        val mapH = mapBottom - mapTop

        val title = if (useCanvas) "Tree Layout Map (manual positions)" else "Tree Location Map (GPS)"
        canvas.drawText(title, MARGIN, MARGIN, pTitle)
        canvas.drawText(
            "Generated: ${dateFmt.format(Date())}   |   ${trees.size} tree${if (trees.size != 1) "s" else ""}",
            MARGIN, MARGIN + 16f, pMuted
        )

        // Grid lines (always draw)
        for (i in 0..5) {
            val x = mapLeft + mapW * i / 5
            canvas.drawLine(x, mapTop, x, mapBottom, pGrid)
        }
        for (i in 0..5) {
            val yLine = mapTop + mapH * i / 5
            canvas.drawLine(mapLeft, yLine, mapRight, yLine, pGrid)
        }

        // Axis labels and titles — only for GPS mode
        if (!useCanvas) {
            val rawMinLat = trees.minOf { it.latitude }
            val rawMaxLat = trees.maxOf { it.latitude }
            val rawMinLon = trees.minOf { it.longitude }
            val rawMaxLon = trees.maxOf { it.longitude }
            val latSpan = if (rawMaxLat - rawMinLat < 0.00005) 0.00005 else rawMaxLat - rawMinLat
            val lonSpan = if (rawMaxLon - rawMinLon < 0.00005) 0.00005 else rawMaxLon - rawMinLon
            val minLat = rawMinLat - latSpan * 0.40
            val maxLat = rawMaxLat + latSpan * 0.40
            val minLon = rawMinLon - lonSpan * 0.40
            val maxLon = rawMaxLon + lonSpan * 0.40

            for (i in 0..5) {
                val x = mapLeft + mapW * i / 5
                val lon = minLon + (maxLon - minLon) * i / 5
                canvas.drawText("%.7f".format(lon), x, mapBottom + 10f, pAxis.also { it.textAlign = Paint.Align.CENTER })
            }
            for (i in 0..5) {
                val yLine = mapTop + mapH * i / 5
                val lat = maxLat - (maxLat - minLat) * i / 5
                canvas.drawText("%.7f".format(lat), mapLeft - 4f, yLine + 3f, pAxisR)
            }

            val pAxisTitle = Paint().apply { color = Color.DKGRAY; textSize = 9f; textAlign = Paint.Align.CENTER }
            canvas.drawText("Longitude", (mapLeft + mapRight) / 2, mapBottom + 24f, pAxisTitle)
            canvas.save()
            canvas.rotate(-90f, MARGIN - 5f, (mapTop + mapBottom) / 2)
            canvas.drawText("Latitude", MARGIN - 5f, (mapTop + mapBottom) / 2, pAxisTitle)
            canvas.restore()
        }

        canvas.drawRect(mapLeft, mapTop, mapRight, mapBottom, pBorder)

        // Compute dot positions
        fun dotXY(tree: Tree): Pair<Float, Float> {
            return if (useCanvas) {
                Pair(mapLeft + tree.canvasX * mapW, mapTop + tree.canvasY * mapH)
            } else {
                val rawMinLat = trees.minOf { it.latitude }
                val rawMaxLat = trees.maxOf { it.latitude }
                val rawMinLon = trees.minOf { it.longitude }
                val rawMaxLon = trees.maxOf { it.longitude }
                val latSpan = if (rawMaxLat - rawMinLat < 0.00005) 0.00005 else rawMaxLat - rawMinLat
                val lonSpan = if (rawMaxLon - rawMinLon < 0.00005) 0.00005 else rawMaxLon - rawMinLon
                val minLat = rawMinLat - latSpan * 0.40
                val maxLat = rawMaxLat + latSpan * 0.40
                val minLon = rawMinLon - lonSpan * 0.40
                val maxLon = rawMaxLon + lonSpan * 0.40
                Pair(
                    mapLeft + ((tree.longitude - minLon) / (maxLon - minLon) * mapW).toFloat(),
                    mapBottom - ((tree.latitude - minLat) / (maxLat - minLat) * mapH).toFloat()
                )
            }
        }

        // North arrow
        val nx = mapRight - 18f
        val ny = mapTop + 30f
        val arrowPath = Path().apply {
            moveTo(nx, ny - 14f); lineTo(nx - 5f, ny); lineTo(nx, ny - 4f); lineTo(nx + 5f, ny); close()
        }
        canvas.drawPath(arrowPath, pFill)
        val pN = Paint().apply { color = Color.rgb(46,125,50); textSize = 10f; isFakeBoldText = true; textAlign = Paint.Align.CENTER }
        canvas.drawText("N", nx, ny + 12f, pN)

        // Tree dots
        val DOT_R = 10f
        val rawPos = trees.map { dotXY(it) }

        // Spread overlapping dots (GPS mode only — canvas positions won't overlap)
        val OVERLAP = DOT_R * 2f
        val slotIndex = IntArray(trees.size) { 0 }
        val slotTotal = IntArray(trees.size) { 1 }
        if (!useCanvas) {
            for (i in trees.indices) {
                var slot = 0
                for (j in 0 until i) {
                    val dx = rawPos[j].first - rawPos[i].first
                    val dy = rawPos[j].second - rawPos[i].second
                    if (dx * dx + dy * dy < OVERLAP * OVERLAP) slot++
                }
                slotIndex[i] = slot
            }
            for (i in trees.indices) {
                for (j in trees.indices) {
                    if (i == j) continue
                    val dx = rawPos[j].first - rawPos[i].first
                    val dy = rawPos[j].second - rawPos[i].second
                    if (dx * dx + dy * dy < OVERLAP * OVERLAP) slotTotal[i]++
                }
            }
        }

        val spreadOffsets = arrayOf(
            Pair(0f, 0f),
            Pair(-DOT_R * 1.4f, -DOT_R * 1.4f), Pair(DOT_R * 1.4f, -DOT_R * 1.4f),
            Pair(-DOT_R * 1.4f,  DOT_R * 1.4f), Pair(DOT_R * 1.4f,  DOT_R * 1.4f),
            Pair(-DOT_R * 2.2f, 0f), Pair(DOT_R * 2.2f, 0f),
            Pair(0f, -DOT_R * 2.2f), Pair(0f,  DOT_R * 2.2f)
        )

        trees.forEachIndexed { index, _ ->
            val notation = "T${index + 1}"
            val base = rawPos[index]
            val slot = slotIndex[index]
            val total = slotTotal[index]
            val offset = if (!useCanvas && total > 1 && slot < spreadOffsets.size) spreadOffsets[slot] else Pair(0f, 0f)
            val tx = base.first + offset.first
            val ty = base.second + offset.second
            canvas.drawCircle(tx, ty, DOT_R, pRing)
            canvas.drawCircle(tx, ty, DOT_R - 2f, pFill)
            canvas.drawText(notation, tx, ty + 2.5f, pNum)
        }

        canvas.drawText("TreeSpecNMap  •  Page $pageNum", MARGIN, PAGE_HEIGHT - MARGIN, pMuted)
    }
}
