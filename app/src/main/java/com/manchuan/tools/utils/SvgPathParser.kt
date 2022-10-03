package com.manchuan.tools.utils

import android.graphics.Path
import android.graphics.PointF
import java.text.ParseException

/**
 * @author Felix.Liang
 */
object SvgPathParser {
    private const val TOKEN_ABSOLUTE_COMMAND = 1
    private const val TOKEN_RELATIVE_COMMAND = 2
    private const val TOKEN_VALUE = 3
    private const val TOKEN_EOF = 4
    private val sPoints = arrayOfNulls<PointF>(3)
    private var sCurrentToken = 0
    private val sCurrentPoint = PointF()
    private var sLength = 0
    private var sIndex = 0
    private var sPathString: String? = null

    init {
        for (i in sPoints.indices) {
            sPoints[i] = PointF()
        }
    }

    private fun transformX(x: Float): Float {
        return x
    }

    private fun transformY(y: Float): Float {
        return y
    }

    @JvmStatic
    @Throws(ParseException::class)
    fun parsePath(s: String?): Path {
        sCurrentPoint[0f] = 0f
        sCurrentPoint[Float.NaN] = Float.NaN
        sPathString = s
        sIndex = 0
        sLength = sPathString!!.length
        for (i in sPoints.indices) {
            sPoints[i]!![0f] = 0f
        }
        val tempPoint1 = sPoints[0]
        val tempPoint2 = sPoints[1]
        val tempPoint3 = sPoints[2]
        val p = Path()
        p.fillType = Path.FillType.WINDING
        var firstMove = true
        while (sIndex < sLength) {
            val command = consumeCommand()
            val relative = sCurrentToken == TOKEN_RELATIVE_COMMAND
            when (command) {
                'M', 'm' -> {

                    // move command
                    var firstPoint = true
                    while (advanceToNextToken() == TOKEN_VALUE) {
                        consumeAndTransformPoint(
                            tempPoint1,
                            relative && !sCurrentPoint.x.isNaN()
                        )
                        if (firstPoint) {
                            p.moveTo(tempPoint1!!.x, tempPoint1.y)
                            firstPoint = false
                            if (firstMove) {
                                sCurrentPoint.set(tempPoint1)
                                firstMove = false
                            }
                        } else {
                            p.lineTo(tempPoint1!!.x, tempPoint1.y)
                        }
                    }
                    sCurrentPoint.set(tempPoint1!!)
                }
                'C', 'c' -> {

                    // curve command
                    if (sCurrentPoint.x.isNaN()) {
                        throw ParseException("Relative commands require current point", sIndex)
                    }
                    while (advanceToNextToken() == TOKEN_VALUE) {
                        consumeAndTransformPoint(tempPoint1, relative)
                        consumeAndTransformPoint(tempPoint2, relative)
                        consumeAndTransformPoint(tempPoint3, relative)
                    }
                    p.cubicTo(
                        tempPoint1!!.x, tempPoint1.y, tempPoint2!!.x, tempPoint2.y, tempPoint3!!.x,
                        tempPoint3.y
                    )
                    sCurrentPoint.set(tempPoint3)
                }
                'S', 's' -> {

                    //smooth curve command
                    if (sCurrentPoint.x.isNaN()) {
                        throw ParseException("Relative commands require current point", sIndex)
                    }
                    while (advanceToNextToken() == TOKEN_VALUE) {
                        consumeAndTransformPoint(tempPoint1, relative)
                        consumeAndTransformPoint(tempPoint2, relative)
                    }
                    val a = (tempPoint2!!.x - sCurrentPoint.x) / (sCurrentPoint.y - tempPoint2.y)
                    val b = -1f
                    val c = ((sCurrentPoint.y + tempPoint2.y) / 2
                            + (sCurrentPoint.x - tempPoint2.x) / (sCurrentPoint.y - tempPoint2.y) * (sCurrentPoint.x + tempPoint2.x) / 2)
                    tempPoint3!!.x =
                        tempPoint1!!.x - 2 * a * (a * tempPoint1.x + b * tempPoint1.y + c) / (a * a + b * b)
                    tempPoint3.y =
                        tempPoint1.y - 2 * b * (a * tempPoint1.x + b * tempPoint1.y + c) / (a * a + b * b)
                    p.cubicTo(
                        tempPoint3.x,
                        tempPoint3.y,
                        tempPoint1.x,
                        tempPoint1.y,
                        tempPoint2.x,
                        tempPoint2.y
                    )
                    sCurrentPoint.set(tempPoint2)
                }
                'L', 'l' -> {

                    // line command
                    if (sCurrentPoint.x.isNaN()) {
                        throw ParseException("Relative commands require current point", sIndex)
                    }
                    while (advanceToNextToken() == TOKEN_VALUE) {
                        consumeAndTransformPoint(tempPoint1, relative)
                        p.lineTo(tempPoint1!!.x, tempPoint1.y)
                    }
                    sCurrentPoint.set(tempPoint1!!)
                }
                'H', 'h' -> {

                    // horizontal line command
                    if (sCurrentPoint.x.isNaN()) {
                        throw ParseException("Relative commands require current point", sIndex)
                    }
                    while (advanceToNextToken() == TOKEN_VALUE) {
                        var x = transformX(consumeValue())
                        if (relative) {
                            x += sCurrentPoint.x
                        }
                        tempPoint1!!.x = x
                        tempPoint1.y = sCurrentPoint.y
                        p.lineTo(tempPoint1.x, tempPoint1.y)
                    }
                    sCurrentPoint.set(tempPoint1!!)
                }
                'V', 'v' -> {

                    // vertical line command
                    if (sCurrentPoint.x.isNaN()) {
                        throw ParseException("Relative commands require current point", sIndex)
                    }
                    while (advanceToNextToken() == TOKEN_VALUE) {
                        var y = transformY(consumeValue())
                        if (relative) {
                            y += sCurrentPoint.y
                        }
                        tempPoint1!!.x = sCurrentPoint.x
                        tempPoint1.y = y
                        p.lineTo(tempPoint1.x, tempPoint1.y)
                    }
                    sCurrentPoint.set(tempPoint1!!)
                }
                'Z', 'z' -> {

                    // close command
                    p.close()
                }
            }
        }
        return p
    }

    private fun advanceToNextToken(): Int {
        while (sIndex < sLength) {
            val c = sPathString!![sIndex]
            if (c in 'a'..'z') {
                return TOKEN_RELATIVE_COMMAND.also { sCurrentToken = it }
            } else if (c in 'A'..'Z') {
                return TOKEN_ABSOLUTE_COMMAND.also { sCurrentToken = it }
            } else if (c in '0'..'9' || c == '.' || c == '-') {
                return TOKEN_VALUE.also { sCurrentToken = it }
            }

            // skip unrecognized character
            ++sIndex
        }
        return TOKEN_EOF.also { sCurrentToken = it }
    }

    @Throws(ParseException::class)
    private fun consumeCommand(): Char {
        advanceToNextToken()
        if (sCurrentToken != TOKEN_RELATIVE_COMMAND && sCurrentToken != TOKEN_ABSOLUTE_COMMAND) {
            throw ParseException("Expected command", sIndex)
        }
        return sPathString!![sIndex++]
    }

    @Throws(ParseException::class)
    private fun consumeAndTransformPoint(out: PointF?, relative: Boolean) {
        out!!.x = transformX(consumeValue())
        out.y = transformY(consumeValue())
        if (relative) {
            out.x += sCurrentPoint.x
            out.y += sCurrentPoint.y
        }
    }

    @Throws(ParseException::class)
    private fun consumeValue(): Float {
        advanceToNextToken()
        if (sCurrentToken != TOKEN_VALUE) {
            throw ParseException("Expected value", sIndex)
        }
        var start = true
        var seenDot = false
        var index = sIndex
        while (index < sLength) {
            val c = sPathString!![index]
            if (c !in '0'..'9' && (c != '.' || seenDot) && (c != '-' || !start)) {
                // end of value
                break
            }
            if (c == '.') {
                seenDot = true
            }
            start = false
            ++index
        }
        if (index == sIndex) {
            throw ParseException("Expected value", sIndex)
        }
        val str = sPathString!!.substring(sIndex, index)
        return try {
            val value = str.toFloat()
            sIndex = index
            value
        } catch (e: NumberFormatException) {
            throw ParseException("Invalid float value '$str'.", sIndex)
        }
    }
}