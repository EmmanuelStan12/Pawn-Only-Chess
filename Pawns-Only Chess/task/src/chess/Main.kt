package chess

const val WHITE_PAWN = "W"
const val BLACK_PAWN = "B"
const val EMPTY_CELL = " "
const val BOARD_DIMENSION = 8
const val DEMARCATION_LINE = "+---+---+---+---+---+---+---+---+"

val COLUMNS_INDEX_MAPPINGS = mapOf<Char, Int>(
        'a' to 0,
        'b' to 1,
        'c' to 2,
        'd' to 3,
        'e' to 4,
        'f' to 5,
        'g' to 6,
        'h' to 7
)

enum class PlayerAction {
    MOVEMENT,
    DIAGONAL_CAPTURE,
    EN_PASSANT_CAPTURE,
    INVALID_MOVEMENT,
    //BOARD_STATUS
}

val COLOR_NAME_MAPPING = mapOf<String, String>(
        "W" to "white",
        "B" to "black"
)

typealias REQUEST_AGAIN = Boolean
typealias PlayerColor = String

class UserInputResult(
        val fromColumnName: Char? = null,
        val fromColumnNo: Int? = null,
        val toColumnName: Char? = null,
        val toColumnNo: Int? = null,
        var isExit: Boolean = false,
        var isValidInput: Boolean = false
) {
    override fun toString(): String {
        return "UserInputResult(fromColumnName: $fromColumnName, toColumnName: $toColumnName, " +
                ("fromColumnNo: $fromColumnNo, toColumnNo: $toColumnNo" +
                        ", isExit: $isExit, isValidInput: $isValidInput)")
    }

    operator fun component1() : Char = this.fromColumnName!!
    operator fun component2() : Int = this.fromColumnNo!!
    operator fun component3() : Char = this.toColumnName!!
    operator fun component4() : Int = this.toColumnNo!!
}

fun executeCallbackActionsForPlayerMovements(
        playerAction: PlayerAction,
        board: List<MutableList<String>>,
        input: UserInputResult,
        enPassantBoard: List<MutableList<Boolean>>,
        color: String
): REQUEST_AGAIN {
    when (playerAction) {
        PlayerAction.MOVEMENT -> {
            return executePlayerPawnMovement(board, color, input, enPassantBoard)
        }
        PlayerAction.INVALID_MOVEMENT -> return true
        PlayerAction.DIAGONAL_CAPTURE -> {
            resetEnPassantBoard(enPassantBoard)
            return diagonalPawnCapture(
                    board, input, color
            )
        }
        PlayerAction.EN_PASSANT_CAPTURE -> {
            return enPassantPawnCapture(
                    board, enPassantBoard, input, color
            )
        }
    }
}

/**
 * createChessBoard - creates a chess board
 * @param n: the row and column dimension of the chess board (nxn)
 * Return: the chess board
 */
fun createChessBoard(n: Int): List<MutableList<String>> {
    //Side effect but necessary as it is what is to be returned
    val chessBoard = mutableListOf<MutableList<String>>()
    for (i in 1..n) {
        val row = mutableListOf<String>()
        for (j in 1..n) {
            when {
                (i == 2) -> row.add(WHITE_PAWN)
                (i == 7) -> row.add(BLACK_PAWN)
                else -> row.add(EMPTY_CELL)
            }
        }
        chessBoard.add(row)
    }
    return chessBoard.toList()
}

/**
 * createChessBoardForEnPassant - creates a chess board of booleans for checking en passant
 * @param n: the row and column dimension of the chess board (nxn)
 * Return: the chess board
 */
fun createChessBoardForEnPassant(n: Int): List<MutableList<Boolean>> {
    //Side effect but necessary as it is what is to be returned
    val chessBoard = mutableListOf<MutableList<Boolean>>()
    for (i in 1..n) {
        val row = mutableListOf<Boolean>()
        for (j in 1..n) {
            row.add(false)
        }
        chessBoard.add(row)
    }
    return chessBoard.toList()
}

/**
 * printChessBoard - prints the chess board in the console
 * @param board: the board to be printed
 */
fun printChessBoard(board: List<MutableList<String>>) {
    // declaring a variable but is necessary, even though it's a side effect
    var i = BOARD_DIMENSION
    for (row in board.reversed()) {
        println("  %s".format(DEMARCATION_LINE))
        print("%d ".format(i--))
        for (cell in row) {
            print("| %s ".format(cell))
        }
        println("|")
    }
    println("  %s".format(DEMARCATION_LINE))
    print("   ")
    for (j in 'a'..'h') {
        print(" %s  ".format(j))
    }
    println()
}

/**
 * getPlayers: gets the users names
 * Return: a pair of users to play the game
 */
fun getPlayers(): Pair<String, String> {
    println("First Player's name:")
    val firstPlayer = readln()
    println("Second Player's name:")
    val secondPlayer = readln()
    return Pair(firstPlayer, secondPlayer)
}

/**
 * startGame: starts the game to be played
 * @param board: the board for users
 * @param firstPlayer: the first player
 * @param secondPlayer: the second player
 */
fun startGame(
        board: List<MutableList<String>>,
        enPassantBoard: List<MutableList<Boolean>>,
        firstPlayer: String,
        secondPlayer: String
) {
    top_loop@ while (true) {
        val firstPlayerInput = actionUserTurn(firstPlayer)
        if (firstPlayerInput.isExit) {
            println("Bye!")
            break
        }
        val playerAction = analyzePlayerAction(board, firstPlayerInput)
        val firstPlayerAskAgain = executeCallbackActionsForPlayerMovements(
                playerAction,
                board,
                firstPlayerInput,
                enPassantBoard,
                "W"
        )
        if (!firstPlayerAskAgain) {
            when (checkWinner(board)) {
                BoardStatus.WHITE_WON -> {
                    println("White Wins!")
                    println("Bye!")
                    break@top_loop
                }
                BoardStatus.BLACK_WON -> {
                    println("Black Wins!")
                    println("Bye!")
                    break@top_loop
                }
                else -> Unit
            }
            when (checkDraw(board, color = "B")) {
                BoardStatus.IS_DRAW -> {
                    println("Stalemate!")
                    println("Bye!")
                    break@top_loop
                }
                else -> Unit
            }
        }
        if (firstPlayerAskAgain) {
            // println("Invalid Input")
            continue@top_loop
        }
        var secondPlayerAskAgain = true
        inner_loop@ while (secondPlayerAskAgain) {
            val secondPlayerInput = actionUserTurn(secondPlayer)
            if (secondPlayerInput.isExit) {
                println("Bye!")
                break@top_loop
            }
            val secondPlayerAction = analyzePlayerAction(board, secondPlayerInput)
            secondPlayerAskAgain = executeCallbackActionsForPlayerMovements(
                    secondPlayerAction,
                    board,
                    secondPlayerInput,
                    enPassantBoard,
                    "B"
            )
            if (!secondPlayerAskAgain) {
                when (checkWinner(board)) {
                    BoardStatus.WHITE_WON -> {
                        println("White Wins!")
                        println("Bye!")
                        break@top_loop
                    }
                    BoardStatus.BLACK_WON -> {
                        println("Black Wins!")
                        println("Bye!")
                        break@top_loop
                    }
                    else -> Unit
                }
                when (checkDraw(board, "W")) {
                    BoardStatus.IS_DRAW -> {
                        println("Stalemate!")
                        println("Bye!")
                        break@top_loop
                    }
                    else -> Unit
                }
            }
        }
    }
}

/**
 * analyzePlayerAction - checks the user action
 * @param board: player's chess board
 * @param input: user's input
 * @returns possible movement options
 */
fun analyzePlayerAction(
        board: List<MutableList<String>>,
        input: UserInputResult
): PlayerAction {
    val (fromColumnName, fromRowNo, toColumnName, toRowNo) = input
    val fromColumnIndex = COLUMNS_INDEX_MAPPINGS[fromColumnName]!!
    val toColumnIndex = COLUMNS_INDEX_MAPPINGS[toColumnName]!!
    val steps = toColumnIndex - fromColumnIndex
    if (fromColumnName == toColumnName) {
        return PlayerAction.MOVEMENT
    }
    val sourceRow = board[fromRowNo - 1]
    val destinationRow = board[toRowNo - 1]
    if (destinationRow[toColumnIndex] == " ") {
        return PlayerAction.EN_PASSANT_CAPTURE
    }
    if (destinationRow[toColumnIndex].isNotEmpty()) {
        return PlayerAction.DIAGONAL_CAPTURE
    }
    return PlayerAction.INVALID_MOVEMENT
}

/**
 * actionUserTurn: runs the required actions for each user's turn
 * @param player: the current player
 */
fun actionUserTurn(player: String): UserInputResult {
    println("$player's turn:")
    val input = readln()
    val userInputResult = analyzeUserInput(input)
    if (!userInputResult.isValidInput && !userInputResult.isExit) {
        return actionUserTurn(player)
    }
    return userInputResult
}

/**
 * analyzeUserInput: analyzes the user input
 * @param input: the input of the user
 */
fun analyzeUserInput(input: String): UserInputResult {
    // Side effect
    val regex = Regex("""([a-z])(\d)([a-z])(\d)""")
    if (regex.matches(input)) {
        val data = regex.find(input)?.destructured!!.toList()
        val fromColumnName = data[0][0]
        val fromColumnNo = data[1].toInt()
        val toColumnName = data[2][0]
        val toColumnNo = data[3].toInt()
        val result = UserInputResult(
                fromColumnName = fromColumnName,
                toColumnName = toColumnName,
                fromColumnNo = fromColumnNo,
                toColumnNo = toColumnNo
        )
        if (fromColumnName !in 'a'..'h' || toColumnName !in 'a'..'h') {
            println("Invalid Input")
            result.isExit = false
            result.isValidInput = false
            return result
        } else if (fromColumnNo !in 1..BOARD_DIMENSION || toColumnNo !in 1..BOARD_DIMENSION) {
            println("Invalid Input")
            result.isExit = false
            result.isValidInput = false
            return result
        }
        result.isExit = false
        result.isValidInput = true
        return result
    } else if (input == "exit") {
        return UserInputResult(isExit = true, isValidInput = true)
    }
    println("Invalid Input")
    return UserInputResult(isExit = false, isValidInput = false)
}

/**
 * executeUserCommand - executes moves made by a user on the board
 * @param board: the board to be evaluated
 * @param color: the player to be analyzed
 * @param input: the player's input
 */
fun executePlayerPawnMovement(
        board: List<MutableList<String>>,
        color: PlayerColor,
        input: UserInputResult,
        enPassantBoard: List<MutableList<Boolean>>
): REQUEST_AGAIN {
    val (fromColumnName, fromRowNo, toColumnName, toRowNo) = input
    val steps = toRowNo - fromRowNo
    //Check if the user is allowed to move in a direction or not
    if (color == "W") {
        // the no of steps must be positive and not negative, user can't move back
        if (steps < 0) {
            println("Invalid Input")
            return true
        }
    } else if (color == "B") {
        // the second player must not move back but can remain in the same position
        if (steps > 0) {
            println("Invalid Input")
            return true
        }
    }

    // check if they have the same column name
    if (fromColumnName != toColumnName) {
        println("Invalid Input")
        return true
    }
    val row = board[fromRowNo - 1]
    // no of steps to make, this varies from both users
    // if steps is negative then it's for the second player because
    // we need to move the pawn to the previous row not the next like player one
    // we need to check if the row no is 2 or 7 if it is then we allow steps of 2
    // and -2 respectively else we only allow steps of 1 and -1 respectively

    //We need to solve the zero error
    if (fromRowNo == 2 || fromRowNo == 7) {
        if (steps > 2 || steps < -2) {
            println("Invalid Input")
            return true
        }
    } else {
        if (steps > 1 || steps < -1) {
            println("Invalid Input")
            return true
        }
    }
    val fromColumnIndex = COLUMNS_INDEX_MAPPINGS[fromColumnName]!!
    //println("i: $i steps: $steps, fromColumnIndex: $fromColumnName - $fromColumnIndex color: " +
    //board[i][fromColumnIndex].trim())
    if (steps == 0 && row[fromColumnIndex] == " ") {
        println("No ${COLOR_NAME_MAPPING[color]} pawn at $fromColumnName$fromRowNo")
        return true
    } else if (steps == 0) {
        println("Invalid Input")
        return true
    }
    if (row[fromColumnIndex].trim().contains(color)) {
        if (board[fromRowNo - 1 + steps][fromColumnIndex] == " ") {
            row[fromColumnIndex] = " "
            board[fromRowNo - 1 + steps][fromColumnIndex] = color
            if (fromRowNo == 2 || fromRowNo == 7) {
                resetEnPassantBoard(enPassantBoard)
                checkPossibleEnPassantCapture(
                        board,
                        enPassantBoard,
                        toRowNo,
                        toColumnName,
                        color
                )
            }
        }
        else {
            println("Invalid Input")
            return true
        }
    } else {
        println("No ${COLOR_NAME_MAPPING[color]} pawn at $fromColumnName$fromRowNo")
        return true
    }
    printChessBoard(board)
    return false
}

/**
 * checkPossibleEnPassantCapture - checks for en passant capture
 * @param chessBoard: the chess board
 * @param enPassantBoard: the board for en passant
 * @param rowNo: the row of the movement
 * @param color: the player's color
 */
fun checkPossibleEnPassantCapture(
        chessBoard: List<MutableList<String>>,
        enPassantBoard: List<MutableList<Boolean>>,
        rowNo: Int,
        columnName: Char,
        color: String
) {
    // after a double and first move, check for a possible en passant
    val row = chessBoard[rowNo - 1]
    val columnIndex = COLUMNS_INDEX_MAPPINGS[columnName]!!
    if (columnIndex + 1 < row.size) {
        if (row[columnIndex + 1] == toggleColor(color)) {
            enPassantBoard[rowNo - 1][columnIndex + 1] = true
        }
    }
    if (columnIndex - 1 > 0) {
        if (row[columnIndex - 1] == toggleColor(color)) {
            enPassantBoard[rowNo - 1][columnIndex + 1] = true
        }
    }
}

/**
 * resetEnPassantBoard - resets the board for en passant
 * @board board: the en passant boolean board
 */
fun resetEnPassantBoard(board: List<MutableList<Boolean>>) {
    for (row in board) {
        for (i in 0 until row.size) {
            row[i] = false
        }
    }
}

/**
 * @name: enPassantPawnCapture - execute a possible en passant move
 * @param board: the chess board
 * @param input: current user input
 * @param color: current user color
 * @return null
 */
fun enPassantPawnCapture(
        board: List<MutableList<String>>,
        enPassantBoard: List<MutableList<Boolean>>,
        input: UserInputResult,
        color: String
): REQUEST_AGAIN {
    // If the player moves to an empty cell check for en passant
    val (fromColumnName, fromRowNo, toColumnName, toRowNo) = input
    val fromColumnIndex = COLUMNS_INDEX_MAPPINGS[fromColumnName]!!
    val toColumnIndex = COLUMNS_INDEX_MAPPINGS[toColumnName]!!
    val enPassantRow = enPassantBoard[fromRowNo - 1]
    val doesHaveEnPassantAbilities = enPassantRow[fromColumnIndex]
    if (doesHaveEnPassantAbilities) {
        board[toRowNo - 1][toColumnIndex] = color
        board[fromRowNo - 1][fromColumnIndex] = " "
        // eliminate the player below the destination row that is row no - 2
        board[toRowNo - 2][toColumnIndex] = " "
        resetEnPassantBoard(enPassantBoard)
        printChessBoard(board)
        return false
    }
    println("Invalid Input")
    return true
}

/**
 * @name: diagonalPawnCapture captures a pawn diagonally
 * @param board: the chess board
 * @param input: current user input
 * @param color: current user color
 * @return null
 */
fun diagonalPawnCapture(
        board: List<MutableList<String>>,
        input: UserInputResult,
        color: String
): REQUEST_AGAIN {
    val (fromColumnName, fromRowNo, toColumnName, toRowNo) = input
    val steps = toRowNo - fromRowNo
    if (steps < -1 || steps > 1) {
        return true
    }
    if (color == "W") {
        // the no of steps must be positive and not negative, user can't move back
        if (steps < 0) {
            println("Invalid Input")
            return true
        }
    } else if (color == "B") {
        // the second player must not move back but can remain in the same position
        if (steps > 0) {
            println("Invalid Input")
            return true
        }
    }
    val row = board[fromRowNo - 1]
    val fromColumnIndex = COLUMNS_INDEX_MAPPINGS[fromColumnName]!!
    val toColumnIndex = COLUMNS_INDEX_MAPPINGS[toColumnName]!!
    if (board[toRowNo - 1][toColumnIndex] == color) {
        // Input is not valid
        //println("can't execute self")
        println("Invalid Input")
        return true
    } else if (board[toRowNo - 1][toColumnIndex].trim().isEmpty()) {
        // There is no pawn in the destination position
        println("Invalid Input")
        return true

    } else if (row[fromColumnIndex].trim().isEmpty()) {
        // There is no pawn in the source position
        //println("No pawn in the source position")
        println("Invalid Input")
        return true
    } else {
        // Time to remove the pawn from the destination position
        row[fromColumnIndex] = " "
        board[toRowNo - 1][toColumnIndex] = color
//        resetEnPassantBoard()
        printChessBoard(board)
        return false
    }
}

fun toggleColor(color: String) : String {
    val colors = mapOf<String, String>(
            "W" to "B",
            "B" to "W",
            " " to " "
    )
    return colors[color]!!
}

fun main() {
    // creates a chess board with board dimension
    val chessBoard = createChessBoard(BOARD_DIMENSION)
    // Creates chess board for en passant
    val chessBoardForEnPassant = createChessBoardForEnPassant(BOARD_DIMENSION)
    println("Pawns-Only Chess")
    // Get Player's names
    val (firstPlayer, secondPlayer) = getPlayers()
    // Prints a chess board
    printChessBoard(chessBoard)
    // Start the game
    startGame(chessBoard, chessBoardForEnPassant, firstPlayer, secondPlayer)
}