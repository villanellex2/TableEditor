package com.echernikova.evaluator.core.tokenizing

fun Char?.isWhiteSpace() = this != null && (this == ' ' || this == '\t' || this == '\n')

fun Char?.isAlphabetic() = this != null && (this in 'a'..'z' || this in 'A'..'Z' || this == '_')

fun Char?.isDigit() = this != null && this in '0'..'9'

fun Char?.isDecimal(nextChar: Char? = null) =
    this.isDigit() || when (this) {
        '.' -> nextChar.isDigit()
        else -> false
    }