/*
* Copyright 2014-2021 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
*/

package io.ktor.util.date

import kotlinx.datetime.*

/**
 * Build [Instant] parser using [pattern] string.
 *
 * Pattern string format:
 * | Unit     | pattern char | Description                                          |
 * | -------- | ------------ | ---------------------------------------------------- |
 * | Seconds  | s            | parse seconds 0 to 60                                |
 * | Minutes  | m            | parse minutes 0 to 60                                |
 * | Hours    | h            | parse hours 0 to 23                                  |
 * | Month    | M            | parse month from Jan to Dec (see [Month] for details)|
 * | Year     | Y            | parse year                                           |
 * | Any char | *            | Match any character                                  |
 */
public class GMTDateParser(private val pattern: String) {
    init {
        check(pattern.isNotEmpty()) { "Date parser pattern shouldn't be empty." }
    }

    /**
     * Parse [Instant] from [dateString] using [pattern].
     */
    public fun parse(dateString: String): Instant {
        val builder = GMTDateBuilder()

        var start = 0
        var current = pattern[start]
        var chunkStart = 0
        var index = 1

        try {
            while (index < pattern.length) {
                if (pattern[index] == current) {
                    index++
                    continue
                }

                val chunkEnd = chunkStart + index - start
                builder.handleToken(current, dateString.substring(chunkStart, chunkEnd))

                chunkStart = chunkEnd
                start = index
                current = pattern[index]

                index++
            }

            if (chunkStart < dateString.length) {
                builder.handleToken(current, dateString.substring(chunkStart))
            }
        } catch (_: Throwable) {
            throw InvalidDateStringException(dateString, chunkStart, pattern)
        }

        return builder.build()
    }

    private fun GMTDateBuilder.handleToken(
        type: Char,
        chunk: String
    ): Unit = when (type) {
        SECONDS -> {
            seconds = chunk.toInt()
        }
        MINUTES -> {
            minutes = chunk.toInt()
        }
        HOURS -> {
            hours = chunk.toInt()
        }
        DAY_OF_MONTH -> {
            dayOfMonth = chunk.toInt()
        }
        MONTH -> {
            month = Month.from(chunk)
        }
        YEAR -> {
            year = chunk.toInt()
        }
        ZONE ->
            check(chunk == "GMT")
        ANY -> Unit
        else -> {
            check(chunk.all { it == type })
        }
    }

    public companion object {
        public const val SECONDS: Char = 's'
        public const val MINUTES: Char = 'm'
        public const val HOURS: Char = 'h'

        public const val DAY_OF_MONTH: Char = 'd'
        public const val MONTH: Char = 'M'
        public const val YEAR: Char = 'Y'

        public const val ZONE: Char = 'z'

        public const val ANY: Char = '*'
    }
}

public class GMTDateBuilder {
    public var seconds: Int? = null
    public var minutes: Int? = null
    public var hours: Int? = null

    public var dayOfMonth: Int? = null
    public var month: Month? = null
    public var year: Int? = null

    public fun build(): Instant =
        LocalDateTime(
            second = seconds!!,
            minute = minutes!!,
            hour = hours!!,
            dayOfMonth = dayOfMonth!!,
            monthNumber = month!!.ordinal,
            year = year!!
        ).toInstant(TimeZone.UTC)
}

/**
 * Thrown when the date string doesn't the string pattern.
 */
public class InvalidDateStringException(
    data: String,
    at: Int,
    pattern: String
) : IllegalStateException("Failed to parse date string: \"${data}\" at index $at. Pattern: \"$pattern\"")
