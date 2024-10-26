package com.echernikova

import com.echernikova.di.startDependencyInjection
import com.echernikova.fileopening.FileOpeningFrame
import com.echernikova.fileopening.FileOpeningFrameViewModel
import org.koin.java.KoinJavaComponent.getKoin
import javax.swing.*

fun main() {
    SwingUtilities.invokeLater {
        startDependencyInjection()

        FileOpeningFrame(getKoin().get<FileOpeningFrameViewModel>()).isVisible = true
    }
}
