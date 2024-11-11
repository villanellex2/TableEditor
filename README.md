## Swing | Kotlin Desktop Application for Table Editing

### Overview

This is a Kotlin desktop application, designed for editing tables.

### Project Structure

#### Package `fileopening`

This package contains the "entry point" for the application (excluding `Main.kt`).

- **`FileOpeningFrame`**: Provides the UI for creating or opening table files. Supports opening existing files, creating new ones, and reopening the last saved table.
- **`FileOpeningFrameViewModel`**: Manages the interactions within `FileOpeningFrame`, handling file selection, opening `EditorFrame`, and error handling.

#### Package `editor`

Contains  logic for rendering and managing table interface, storing cell pointers, and tracking dependencies between them.

- **`editor.table.renderers`**: Renderers for headers, cells, and editors for JTable.
- **`editor.table.model`**:
    - **`EvaluatingTableModel`**: The central class for managing table data and dependencies. It stores `CellPointer` instances and dependencies between them in the instance of `TableDependenciesGraph`. This model tracks the state of each cell and updates dependent cells when something changes in the dependency graph. It uses topological sort to evaluate in which order cell should be evaluated after update.

#### Package `evaluator`

This package is responsible for evaluating "functions" inside cells, including parsing and interpreting various operators and functions.

- **`Evaluator`**: The main class that initiates expression evaluation from cell's raw string.
- **`Tokenizer`**: Tokenizes input strings into identifiable components like numbers, operators, and cell references.
- **`Parser`**: Builds a list of `Operands` from tokens to prepare for evaluation.

##### Supported Features

- **Data Types**: `Int`, `Double`, `String`, `Boolean` (true, false), `Cell links` (A1, F12), `Cell ranges` (A1:F12).
- **Binary Operators**: `||`, `&&`, `+`, `-`, `*`, `/`, `^`, `>`, `>=`, `<`, `<=`, `%`.
- **Unary Operators**: `+`, `-`.
- **Functions**:
    - **SUM**: `SUM(..varargs: numbers)`.
    - **AVERAGE**: `AVERAGE(..varargs: numbers)`.
    - **MAX**: `MAX(..varargs: numbers)`.
    - **MIN**: `MIN(..varargs: numbers)`.
    - **PRODUCT**: `PRODUCT(..varargs: numbers)`.
    - **IF**: `IF(condition: Boolean, trueValue: Any, falseValue: Any)`.
    - **VLOOKUP**: `VLOOKUP(value: Any, range: TableRange, columnIndex: Int)`.

Functions with 'vararg' types can accept any number of numeric arguments, cell links, or cell ranges. Arguments are separated by commas.

**Example Expressions**:
- Conditional function: `=IF(1 > 0, C1, C2)`.
- Arithmetic operation: `=1 + 5`.

#### Package `file`

Contains helpers for file I/O operations, enabling the application to save and read files.

- **FileHelpers**: Stores tables in chosen file. Currently supported only `.xls` and `.xlsx` extensions.
