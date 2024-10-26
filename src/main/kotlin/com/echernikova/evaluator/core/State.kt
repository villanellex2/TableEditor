package com.echernikova.evaluator.core

import com.echernikova.evaluator.core.tokenizing.Token

open class State<T: Any>(val source: Array<T>) {
    var index: Int = 0
        private set

    fun isAtEnd() = index >= source.size

    fun current() = if (isAtEnd()) null else source[index]

    fun next() = if (index + 1 >= source.size) null else source[index + 1]

    fun forward(): Int = index++

}

class TokenizerState(source: CharArray): State<Char>(source.toTypedArray()) {
    var tokens = mutableListOf<Token>()

    fun part(from: Int, to: Int) = source.copyOfRange(from, to).joinToString("")
}

class ParsingState(tokens: List<Token>): State<Token>(tokens.toTypedArray())
