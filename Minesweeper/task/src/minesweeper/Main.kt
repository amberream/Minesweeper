package minesweeper

import kotlin.random.Random

fun main() {
    val mineSweeper = MineSweeper()
    mineSweeper.playGame()
}

const val FIELD_SIZE = 9
const val MARKED = "*"
const val UNMARKED = "."
const val EXPLODED = "X"
const val EXPLORED = "/"
const val FREE = "free"
const val MINE = "mine"

/**
 * This is the classic Minesweeper game played from the command line.
 */
class MineSweeper {

    private val mineField: MutableList<MutableList<Cell>> = mutableListOf()

    fun playGame() {
        print("How many mines do you want on the field? ")
        val mines = readln().toInt()

        initField()
        placeMines(mines)
        addMineCounts()

        var lost = false

        while (!isValid()) {
            printField()
            print("Set/unset mine marks or claim a cell as free (x y [mine to mark/unmark] or [free to explore]): ")
            val coordinates = readln().split(" ")
            val x = coordinates[1].toInt() - 1
            val y = coordinates[0].toInt() - 1
            val command = coordinates[2]
            if (mineField[x][y].explored){
                // ignore
            } else if (command == MINE && mineField[x][y].marked) {
                mineField[x][y].marked = false
                printField()
            } else if (command == MINE && !mineField[x][y].marked) {
                mineField[x][y].marked = true
                printField()
            } else if (command == FREE && mineField[x][y].mine) {
                println("You stepped on a mine and failed!")
                mineField[x][y].explored = true
                printField()
                lost = true
                break
            } else if (command == FREE && !mineField[x][y].mine) {
                explore(x, y)
            }
        }
        if (!lost) {
            println("Congratulations! You found all the mines!")
        }
    }

    private fun initField() {
        for (row in 0 until FIELD_SIZE) {
            val column = mutableListOf<Cell>()
            mineField.add(column)
            // start with all "." to begin with, then replace with mines later
            for (col in 0 until FIELD_SIZE) {
                column.add(Cell())
            }
        }
    }

    private fun placeMines(mines: Int) {
        var minesPlaced = 0
        while (minesPlaced < mines) {
            val x = Random.nextInt(0, FIELD_SIZE)
            val y = Random.nextInt(0, FIELD_SIZE)
            if (!mineField[x][y].mine) {
                mineField[x][y].mine = true
                minesPlaced++
            }
        }
    }

    /**
     * Counts the number of mines surrounding the cell
     */
    private fun addMineCounts() {
        for (row in 0 until FIELD_SIZE) {
            for (col in 0 until FIELD_SIZE) {
                if (mineField[row][col].mine) {
                    continue
                }
                var mineCount = 0
                // check all surrounding cells clockwise starting with the upper left corner
                if (row > 0 && col > 0 && mineField[row-1][col-1].mine) {
                    mineCount++
                }
                if (row > 0 && mineField[row-1][col].mine) {
                    mineCount++
                }
                if (row > 0 && col < FIELD_SIZE - 1 && mineField[row-1][col+1].mine) {
                    mineCount++
                }
                if (col < FIELD_SIZE - 1 && mineField[row][col + 1].mine) {
                    mineCount++
                }
                if (row < FIELD_SIZE - 1 && col < FIELD_SIZE - 1 && mineField[row + 1][col + 1].mine) {
                    mineCount++
                }
                if (row < FIELD_SIZE - 1 && mineField[row + 1][col].mine) {
                    mineCount++
                }
                if (row < FIELD_SIZE - 1 && col > 0 && mineField[row + 1][col - 1].mine) {
                    mineCount++
                }
                if (col > 0 && mineField[row][col - 1].mine) {
                    mineCount++
                }

                mineField[row][col].count = mineCount
            }
        }
    }

    /**
     * 1. If the cell is empty and has no mines around, all the surrounding cells, including the marked ones,
     * can be explored, and it should be done automatically. Also, if next to the explored cell
     * there is another empty one with no mines around, all the surrounding cells should be explored as well,
     * and so on, until no more can be explored automatically.
     * 2. If a cell is empty and has mines around it, only that cell is explored, revealing a number of
     * mines around it.
     *
     * We only call explore on cells we know are not mines
     */
    private fun explore(x: Int, y: Int) {
        if (mineField[x][y].explored){
            // already explored this cell, move on
            return
        }
        mineField[x][y].explored = true
        if (mineField[x][y].count == 0) {
            // explore all surrounding cells clockwise starting with the upper left corner
            if (y > 0 && x > 0) {
                explore(x-1, y-1)
            }
            if (y > 0) {
                explore(x, y-1)
            }
            if (y > 0 && x < FIELD_SIZE - 1) {
                explore(x+1, y-1)
            }
            if (x < FIELD_SIZE - 1) {
                explore(x+1, y)
            }
            if (y < FIELD_SIZE - 1 && x < FIELD_SIZE - 1 ) {
                explore(x+1, y+1)
            }
            if (y < FIELD_SIZE - 1) {
                explore(x, y+1)
            }
            if (y < FIELD_SIZE - 1 && x > 0) {
                explore(x-1, y+1)
            }
            if (x > 0) {
                explore(x-1, y)
            }
        }
    }

    private fun printField() {
        println(" |123456789|")
        println("-|---------|")
        for (i in mineField.indices){
            println("${i + 1}|${mineField[i].joinToString("")}|")
        }
        println("-|---------|")
    }

    /**
     * Checks if all mines have been correctly marked
     */
    private fun isValid() : Boolean{
        for (row in mineField){
            for (cell in row) {
                if (cell.mine && !cell.marked) return false
                if (!cell.mine && cell.marked) return false
            }
        }
        return true
    }
}

class Cell {
    var explored = false
    var marked = false
    var mine = false
    var count = 0

    override fun toString(): String {
        if (explored && mine) {
            return EXPLODED
        } else if (explored && count > 0) {
            return count.toString()
        } else if (explored && count == 0) {
            return EXPLORED
        } else if (marked) {
            return MARKED
        } else {
            return UNMARKED
        }
    }
}
