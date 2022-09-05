package chess

enum class BoardStatus(val color: String? = null) {
    IS_DRAW, NONE, WHITE_WON, BLACK_WON
}

/**
 * checkWinner - if there is a winner
 * @param board: the chess board
 */
fun checkWinner(board: List<MutableList<String>>): BoardStatus {
    val firstRow = board[0]
    val lastRow = board[7]
    if ("B" in firstRow) {
        return BoardStatus.BLACK_WON
    }
    if ("W" in lastRow) {
        return BoardStatus.WHITE_WON
    }
    var boardString = ""
    for (row in board) {
        boardString += row.joinToString("").trim()
    }
    if ("W" in boardString && "B" !in boardString) {
        return BoardStatus.WHITE_WON
    }
    if ("B" in boardString && "W" !in boardString) {
        return BoardStatus.BLACK_WON
    }
    return BoardStatus.NONE
}

/**
 * checkDraw - checks if there is a draw on the board
 * @param board: the chess board
 * @return boardStatus: the current status of the board
 */
fun checkDraw(board: List<MutableList<String>>, color: String): BoardStatus {
    var canBMove = false
    var canWMove = false
    var bEntered = false
    outerLoop@ for (i in board.indices) {
        val row = board[i]
        innerLoop@ for (col in row.indices) {
            if (row[col] == "W") {
                if (i + 1 in 1 until board.size) {
                    val upCell = board[i + 1][col]
                    // the cell above it
                    if (upCell.trim().isEmpty()) {
                        canWMove = true
                    }
                    // the cell above it by the right
                    if (col + 1 < row.size) {
                        val rDiagonalCell = board[i + 1][col + 1]
                        if (rDiagonalCell.trim().isNotEmpty() && rDiagonalCell != "W") {
                            canWMove = true
                        }
                    }
                    // the cell above by the left
                    if (col - 1 > 0) {
                        val lDiagonalCell = board[i + 1][col - 1]
                        if (lDiagonalCell.trim().isNotEmpty() && lDiagonalCell != "W") {
                            canWMove = true
                        }
                    }
                }
            } else if (row[col] == "B") {
                if (i - 1 in 1 until board.size) {
                    val downCell = board[i - 1][col]
                    // the cell above it
                    if (downCell.trim().isEmpty()) {
                        canBMove = true
                    }
                    //the cell above by the right
                    if (col + 1 < row.size) {
                        val rDiagonalCell = board[i - 1][col + 1]
                        if (rDiagonalCell.trim().isNotEmpty() && rDiagonalCell != "B") {
                            canBMove = true
                        }
                    }
                    // the cell above by the left
                    if (col - 1 > 0) {
                        val lDiagonalCell = board[i - 1][col - 1]
                        if (lDiagonalCell.trim().isNotEmpty() && lDiagonalCell != "B") {
                            canBMove = true
                        }
                    }
                }
            }
        }
    }
    if (color == "W") {
        if (!canWMove) return BoardStatus.IS_DRAW
    }
    if (color == "B") {
        if (!canBMove) return BoardStatus.IS_DRAW
    }
    return BoardStatus.NONE
}

fun checkIfColorCanBeEliminated(board: List<MutableList<String>>, color: String): Boolean {
    for (row in board.indices) {
        for (col in board[row].indices) {
            // check for the color in the board
            if (board[row][col] == color) {
                // check of the row index is valid
                if (row - 1 in board.indices) {
                    // check of the col index is valid
                    if (col - 1 in board.indices) {
                        val lDiagonalColor = board[row - 1][col - 1]
                        if (lDiagonalColor == toggleColor(color)) {
                            return true
                        }
                    }
                    if (col + 1 in board.indices) {
                        val rDiagonalColor = board[row - 1][col + 1]
                        if (rDiagonalColor == toggleColor(color)) {
                            return true
                        }
                    }
                }
            }
        }
    }
    return false
}