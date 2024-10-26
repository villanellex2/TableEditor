package com.echernikova.di

import com.echernikova.editor.EditorViewModel
import com.echernikova.evaluator.functions.Function
import com.echernikova.editor.table.TableViewModel
import com.echernikova.evaluator.core.Context
import com.echernikova.evaluator.core.Evaluator
import com.echernikova.evaluator.functions.FunctionSum
import com.echernikova.fileopening.FileOpeningFrameViewModel
import org.koin.core.context.GlobalContext.startKoin
import org.koin.core.context.GlobalContext.stopKoin
import org.koin.core.parameter.parametersOf
import org.koin.dsl.module
import java.io.File

val functionsModule = module {
    single<Function> { FunctionSum() }
}

val evaluatorModule = module {
    single { Context(getAll<Function>().associateBy { it.name }) }
    single { Evaluator(get()) }
}

val appModule = module {
    single { FileOpeningFrameViewModel() }

    factory { (initialData: Array<Array<Any?>>) -> TableViewModel(initialData, get()) }
    factory { (file: File, initialData: Array<Array<Any?>>) ->
        EditorViewModel(
            file,
            get { parametersOf(initialData) })
    }
}

fun startDependencyInjection() {
    startKoin {
        modules(functionsModule, evaluatorModule, appModule)
    }
}

fun stopDependencyInjection() {
    stopKoin()
}
