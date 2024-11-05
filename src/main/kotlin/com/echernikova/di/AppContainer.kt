package com.echernikova.di

import com.echernikova.editor.table.TableViewModel
import com.echernikova.evaluator.core.Evaluator
import com.echernikova.evaluator.functions.*
import com.echernikova.fileopening.FileOpeningFrameViewModel
import org.koin.core.context.GlobalContext.startKoin
import org.koin.core.context.GlobalContext.stopKoin
import org.koin.dsl.module

val evaluatorModule = module {
    single {
        Evaluator(listOf(
            FunctionSum(),
            FunctionMin(),
            FunctionMax(),
            FunctionAverage(),
            FunctionProduct(),
            FunctionIf(),
            FunctionVLookUp(),
        ).associateBy { it.name })
    }
}

val appModule = module {
    single { FileOpeningFrameViewModel() }
    factory { (initialData: Array<Array<Any?>>) -> TableViewModel(initialData, get()) }
}

fun startDependencyInjection() {
    startKoin {
        modules(evaluatorModule, appModule)
    }
}

fun stopDependencyInjection() {
    stopKoin()
}
