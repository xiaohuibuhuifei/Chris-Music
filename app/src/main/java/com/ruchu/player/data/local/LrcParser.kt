package com.ruchu.player.data.local

import com.ruchu.player.data.model.LyricLine
import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.charset.Charset
import java.nio.charset.CodingErrorAction

object LrcParser {

    private val timestampPattern = Regex("\\[(\\d{2}):(\\d{2})([.:]\\d{2,3})?]")
    private val metadataPattern = Regex("^\\[[a-zA-Z#]+:.*]$")

    fun parse(inputStream: InputStream): List<LyricLine> {
        val rawBytes = inputStream.readBytes()
        return parseText(decodeText(rawBytes))
    }

    fun parseText(text: String): List<LyricLine> {
        val lines = mutableListOf<LyricLine>()

        for (line in text.lines()) {
            val trimmed = line.trim()
            if (trimmed.isEmpty() || metadataPattern.matches(trimmed)) continue

            val timestamps = timestampPattern.findAll(trimmed).toList()
            if (timestamps.isEmpty()) continue

            val content = trimmed.substring(timestamps.last().range.last + 1).trim()
            if (content.isBlank()) continue

            for (timestampMatch in timestamps) {
                val (min, sec, msGroup) = timestampMatch.destructured
                val minutes = min.toLongOrNull() ?: 0L
                val seconds = sec.toLongOrNull() ?: 0L
                val milliseconds = if (msGroup.isNotEmpty()) {
                    val msText = msGroup.removePrefix(".").removePrefix(":")
                    if (msText.length == 2) {
                        (msText.toLongOrNull() ?: 0L) * 10
                    } else {
                        msText.toLongOrNull() ?: 0L
                    }
                } else {
                    0L
                }

                lines.add(
                    LyricLine(
                        timestamp = minutes * 60_000 + seconds * 1000 + milliseconds,
                        text = content
                    )
                )
            }
        }

        return lines.sortedBy { it.timestamp }
    }

    private fun decodeText(rawBytes: ByteArray): String {
        val utf8Decoder = Charsets.UTF_8.newDecoder()
            .onMalformedInput(CodingErrorAction.REPORT)
            .onUnmappableCharacter(CodingErrorAction.REPORT)

        val text = runCatching {
            utf8Decoder.decode(ByteBuffer.wrap(rawBytes)).toString()
        }.getOrElse {
            String(rawBytes, Charset.forName("GBK"))
        }

        return text.removePrefix("\uFEFF")
    }
}
