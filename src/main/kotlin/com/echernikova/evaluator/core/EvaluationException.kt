package com.echernikova.evaluator.core

open class EvaluationException(message: String): Exception(message)

class TokenizerException(message: String) : EvaluationException(message)

class ParsingException(message: String): EvaluationException(message)
