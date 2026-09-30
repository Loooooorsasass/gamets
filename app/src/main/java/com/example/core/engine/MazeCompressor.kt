package com.example.core.engine

import android.util.Base64
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.zip.Deflater
import java.util.zip.DeflaterOutputStream
import java.util.zip.InflaterInputStream

/**
 * Bộ công cụ nén và mã hóa mê cung hiệu năng cao (High-Performance Maze Compressor).
 * Nén bản đồ 1.000.000 byte xuống còn vài chục KB bằng Nibble Packing kết hợp ZLIB/RLE,
 * tối ưu tuyệt đối cho SQLite Room Database và truyền tải mạng.
 */
object MazeCompressor {

    /**
     * Gói mảng 1D ByteArray (mỗi ô 4 bit 0..15) thành mảng Byte rút gọn (2 ô trên 1 Byte).
     * Giảm trực tiếp 50% dung lượng RAM/Storage.
     */
    fun packNibbles(unpacked: ByteArray): ByteArray {
        val packedSize = (unpacked.size + 1) / 2
        val packed = ByteArray(packedSize)
        var pIdx = 0
        var i = 0
        while (i < unpacked.size) {
            val high = (unpacked[i].toInt() and 0x0F) shl 4
            val low = if (i + 1 < unpacked.size) (unpacked[i + 1].toInt() and 0x0F) else 0
            packed[pIdx++] = (high or low).toByte()
            i += 2
        }
        return packed
    }

    /**
     * Giải nén mảng Nibble packed trở lại mảng 1D ban đầu.
     */
    fun unpackNibbles(packed: ByteArray, originalLength: Int): ByteArray {
        val unpacked = ByteArray(originalLength)
        var uIdx = 0
        var i = 0
        while (i < packed.size && uIdx < originalLength) {
            val byteVal = packed[i].toInt() and 0xFF
            unpacked[uIdx++] = ((byteVal shr 4) and 0x0F).toByte()
            if (uIdx < originalLength) {
                unpacked[uIdx++] = (byteVal and 0x0F).toByte()
            }
            i++
        }
        return unpacked
    }

    /**
     * Nén mảng byte bằng thuật toán ZLIB Deflate (Tốc độ nén cao nhất).
     */
    fun compress(data: ByteArray): ByteArray {
        val baos = ByteArrayOutputStream()
        val deflater = Deflater(Deflater.BEST_SPEED)
        val dos = DeflaterOutputStream(baos, deflater)
        dos.write(data)
        dos.finish()
        dos.close()
        return baos.toByteArray()
    }

    /**
     * Giải nén mảng ZLIB Deflate trở lại mảng nguyên bản.
     */
    fun decompress(compressedData: ByteArray): ByteArray {
        val bais = ByteArrayInputStream(compressedData)
        val iis = InflaterInputStream(bais)
        val baos = ByteArrayOutputStream()
        val buffer = ByteArray(4096)
        var len: Int
        while (iis.read(buffer).also { len = it } != -1) {
            baos.write(buffer, 0, len)
        }
        iis.close()
        return baos.toByteArray()
    }

    /**
     * Nén toàn bộ cấu trúc mê cung thành chuỗi Base64 siêu gọn để chia sẻ qua mã code hoặc URL.
     */
    fun exportToBase64(grid: ByteArray, width: Int, height: Int, start: Int, end: Int): String {
        val packed = packNibbles(grid)
        val compressed = compress(packed)
        val header = "$width:$height:$start:$end:"
        val base64Body = Base64.encodeToString(compressed, Base64.NO_WRAP)
        return header + base64Body
    }

    /**
     * Khôi phục mê cung từ chuỗi Base64 chia sẻ.
     */
    fun importFromBase64(exportedStr: String): MazeResult? {
        return try {
            val parts = exportedStr.split(":", limit = 5)
            if (parts.size < 5) return null
            val width = parts[0].toInt()
            val height = parts[1].toInt()
            val start = parts[2].toInt()
            val end = parts[3].toInt()
            val base64Body = parts[4]

            val compressed = Base64.decode(base64Body, Base64.NO_WRAP)
            val packed = decompress(compressed)
            val grid = unpackNibbles(packed, width * height)

            MazeResult(
                grid = grid,
                startIndex = start,
                endIndex = end
            )
        } catch (_: Exception) {
            null
        }
    }
}
