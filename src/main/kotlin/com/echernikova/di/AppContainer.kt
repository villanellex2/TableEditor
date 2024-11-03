package com.echernikova.di

import com.echernikova.editor.EditorViewModel
import com.echernikova.editor.table.TableViewModel
import com.echernikova.editor.table.model.TableDataController
import com.echernikova.evaluator.core.Evaluator
import com.echernikova.evaluator.functions.*
import com.echernikova.evaluator.functions.Function
import com.echernikova.fileopening.FileOpeningFrameViewModel
import org.koin.core.context.GlobalContext.startKoin
import org.koin.core.context.GlobalContext.stopKoin
import org.koin.core.parameter.parametersOf
import org.koin.dsl.module
import java.io.File

val functionsModule = module {
    single<Function> { FunctionSum() }
    single<Function> { FunctionAverage() }
    single<Function> { FunctionProduct() }
    single<Function> { FunctionMax() }
    single<Function> { FunctionMin() }
    single<Function> { FunctionIf() }
}

val evaluatorModule = module {
    factory { TableDataController(get()) }
    single { Evaluator(getAll<Function>().associateBy { it.name }) }
}

val appModule = module {
    single { FileOpeningFrameViewModel() }

    factory { (initialData: Array<Array<Any?>>) -> TableViewModel(initialData, get()) }
    factory { (file: File, initialData: Array<Array<Any?>>) ->
        EditorViewModel(
            file,
            get { parametersOf(initialData, get()) }
        )
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
