package com.manchuan.tools.utils

import java.util.*
import kotlin.math.abs

class PasswordUtils {
    private val pwd_len = 0
    private val signal = 0

    //密码序列包含的字符类型个数
    val password: String
        get() {
            val maxChar = 82 //密码序列包含的字符类型个数
            var i: Int
            var count = 0
            val str = charArrayOf(
                'a',
                'A',
                'b',
                'B',
                'c',
                'C',
                'd',
                'D',
                'e',
                'E',
                'f',
                'F',
                'g',
                'G',
                'h',
                'H',
                'i',
                'I',
                'j',
                'J',
                'k',
                'K',
                'l',
                'm',
                'n',
                'o',
                'p',
                'q',
                'r',
                's',
                't',
                'u',
                'v',
                'w',
                'x',
                'X',
                'y',
                'Y',
                'z',
                'Z',
                '0',
                '1',
                '2',
                '3',
                '4',
                '5',
                '6',
                '7',
                '8',
                '9',
                '!',
                '#',
                '$',
                '%',
                '&',
                '(',
                ')',
                '*',
                '+',
                ',',
                '-',
                '.',
                '/',
                '\'',
                '"',
                ':',
                ';',
                '<',
                '=',
                '>',
                '?',
                '@',
                '[',
                '\\',
                ']',
                '^',
                '_',
                '`',
                '{',
                '|',
                '}',
                '~'
            )
            val pwd = StringBuilder("")
            val random = Random()
            when (signal) {
                0 -> while (count < pwd_len) {
                    i = abs(random.nextInt(maxChar))
                    if (str[i] >= 97.toChar() && str[i] <= 122.toChar()) {
                        pwd.append(str[i])
                        count++
                    }
                }
                1 -> while (count < pwd_len) {
                    i = abs(random.nextInt(maxChar))
                    if (str[i] >= 97.toChar() && str[i] <= 122.toChar() || str[i] <= 47.toChar() || (str[i] >= 58.toChar()
                                && str[i] <= 64.toChar()) || str[i] >= 91.toChar() || str[i] <= 96.toChar() && str[i] >= 123.toChar()
                    ) {
                        pwd.append(str[i])
                        count++
                    }
                }
                2 -> while (count < pwd_len) {
                    i = abs(random.nextInt(maxChar))
                    if (i >= 0 && i < str.size) {
                        if (str[i] >= 97.toChar() && str[i] <= 122.toChar() || str[i] >= 48.toChar() && str[i] <= 57.toChar()) {
                            pwd.append(str[i])
                            count++
                        }
                    }
                }
                3 -> while (count < pwd_len) {
                    i = abs(random.nextInt(maxChar))
                    if (i >= 0 && i < str.size) {
                        if (str[i] >= 97.toChar() && str[i] <= 122.toChar() || str[i] >= 48.toChar() && str[i] <= 57.toChar() || str[i] <= 47.toChar() || str[i] >= 58.toChar() && str[i] <= 64.toChar() || (str[i] >= 91.toChar()
                                    && str[i] <= 96.toChar()) || str[i] >= 123.toChar()
                        ) {
                            pwd.append(str[i])
                            count++
                        }
                    }
                }
                4 -> while (count < pwd_len) {
                    i = abs(random.nextInt(maxChar))
                    if (i >= 0 && i < str.size) {
                        if (str[i] >= 65.toChar() && str[i] <= 90.toChar() || str[i] >= 97.toChar() && str[i] <= 122.toChar()) {
                            pwd.append(str[i])
                            count++
                        }
                    }
                }
                5 -> while (count < pwd_len) {
                    i = abs(random.nextInt(maxChar))
                    if (i >= 0 && i < str.size) {
                        if (str[i] >= 65.toChar() && str[i] <= 90.toChar() || str[i] >= 97.toChar() && str[i] <= 122.toChar() || str[i] <= 47.toChar() || str[i] >= 58.toChar() && str[i] <= 64.toChar() || (str[i] >= 91.toChar()
                                    && str[i] <= 96.toChar()) || str[i] >= 123.toChar()
                        ) {
                            pwd.append(str[i])
                            count++
                        }
                    }
                }
                6 -> while (count < pwd_len) {
                    i = abs(random.nextInt(maxChar))
                    if (i >= 0 && i < str.size) {
                        if (str[i] >= 65.toChar() && str[i] <= 90.toChar() || str[i] >= 97.toChar() && str[i] <= 122.toChar() || str[i] >= 48.toChar() && str[i] <= 57.toChar()) {
                            pwd.append(str[i])
                            count++
                        }
                    }
                }
                7 -> while (count < pwd_len) {
                    i = abs(random.nextInt(maxChar))
                    if (i >= 0 && i < str.size) {
                        if (str[i] >= 65.toChar() && str[i] <= 90.toChar() || str[i] >= 97.toChar() && str[i] <= 122.toChar() || str[i] >= 48.toChar() && str[i] <= 57.toChar() || str[i] <= 47.toChar() || (str[i] >= 58.toChar()
                                    && str[i] <= 64.toChar()) || str[i] >= 91.toChar() && str[i] <= 96.toChar() || str[i] >= 123.toChar()
                        ) {
                            pwd.append(str[i])
                            count++
                        }
                    }
                }
                else -> {}
            }
            return pwd.toString()
        }
}