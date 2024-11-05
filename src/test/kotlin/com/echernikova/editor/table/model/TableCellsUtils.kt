package com.echernikova.editor.table.model

const val UPDATED_VALUE = 1024
val underTest by lazy { TableDataController() }

fun fillTable() {
    underTest.apply {
        // A1 <- A2 <- A3
        setValueToCell(CellPointer.fromString("A3")!!, "0")
        setValueToCell(CellPointer.fromString("A1")!!, "=A3")
        setValueToCell(CellPointer.fromString("A2")!!, "=A1")

        // B1 <- B2 <-> B3 | Cycle!
        setValueToCell(CellPointer.fromString("B1")!!, "=0")
        setValueToCell(CellPointer.fromString("B2")!!, "=B1+B3")
        setValueToCell(CellPointer.fromString("B3")!!, "=B2")

        // C1 -> C1 | Cycle!
        setValueToCell(CellPointer.fromString("C1")!!, "=C1")

        // D1 -> D2 -> D3 -> D1 | Cycle!
        setValueToCell(CellPointer.fromString("D1")!!, "=D3")
        setValueToCell(CellPointer.fromString("D2")!!, "=D1")
        setValueToCell(CellPointer.fromString("D3")!!, "=D2")
    }
}

val A3 by lazy { underTest.getOrCreateCell(CellPointer.fromString("A3")!!) }
val A1 by lazy { underTest.getOrCreateCell(CellPointer.fromString("A1")!!) }
val A2 by lazy { underTest.getOrCreateCell(CellPointer.fromString("A2")!!) }

val B1 by lazy { underTest.getOrCreateCell(CellPointer.fromString("B1")!!) }
val B2 by lazy { underTest.getOrCreateCell(CellPointer.fromString("B2")!!) }
val B3 by lazy { underTest.getOrCreateCell(CellPointer.fromString("B3")!!) }

val C1 by lazy { underTest.getOrCreateCell(CellPointer.fromString("C1")!!) }

val D1 by lazy { underTest.getOrCreateCell(CellPointer.fromString("D1")!!) }
val D2 by lazy { underTest.getOrCreateCell(CellPointer.fromString("D2")!!) }
val D3 by lazy { underTest.getOrCreateCell(CellPointer.fromString("D3")!!) }
