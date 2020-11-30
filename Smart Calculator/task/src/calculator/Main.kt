package calculator

val memory = mutableMapOf<String, Int>()

fun main() {
    do {
        val expr = readLine()!!
        when {
            expr == "" -> continue
            expr.indexOf('=') > 0 -> {
                pushToMemory(expr)
                continue
            }
            memory.containsKey(expr) -> {
                println(memory[expr])
                continue
            }
            expr == "/help" -> {
                println("The program calculates priority operations with recursive algorithm ")
                continue
            }
            expr == "/exit" -> break
            expr[0] == '/' -> {
                println("Unknown command")
                continue
            }
            else -> try {
                println(eval(expr)!!.toInt())
            } catch (e: Exception) {
                println("Invalid expression")
            }
        }
    } while (true)
    println("Bye!")
    println(memory)
}

private fun correct(expr: String): String { // replace all type of "+-.." or "-+.." with '+' or '-'
    return expr
            .split(" ")
            .joinToString("") {
                if (it.length > 1 && it[0] in "-+" && it[1] in "-+")
                    if (it.count { it1 -> it1 == '-' } % 2 == 1) "-" else "+" else it
            }
            .map { if (it in "+-*/") " $it " else it }
            .joinToString("")
            .trim()
}

fun eval(exprList: String): Double? {
    return eval(correct(exprList).split(" "))
}

private fun eval(exprList: List<String>): Double? {
    try {
        if (exprList.size == 1)                                                 // if only one parameter
            return exprList[0].toDoubleOrNull()
                    ?: if (!checkIdentifier(exprList[0])) {
                        println("Invalid identifier")
                        return null
                    } else
                        memory[exprList[0]]?.toDouble() ?: throw Exception("Unknown variable")

        if (memory[exprList[0]] != null)                                        // if first element is a variable
            return eval(listOf(memory[exprList[0]].toString()).plus(exprList.drop(1)))

        when (exprList[1]) {
            "*" -> return if (exprList.size == 3)                                   // high priority operations
                (exprList[0].toDouble() * (eval(exprList[2])
                        ?: throw Exception("Invalid expression ${exprList[2]}"))) else
                eval(listOf((eval(exprList.take(3))
                        ?: throw Exception("Invalid expression ${exprList.take(3).joinToString("")}"))
                        .toString()).plus(exprList.drop(3)))

            "/" -> return if (exprList.size == 3)                                   // high priority operations
                (exprList[0].toDouble() / (eval(exprList[2])
                        ?: throw Exception("Invalid expression ${exprList[2]}"))) else
                eval(listOf((eval(exprList.take(3))
                        ?: throw Exception("Invalid expression ${exprList.take(3).joinToString("")}"))
                        .toString()).plus(exprList.drop(3)))

            "+" -> return exprList[0].toDouble() + (eval(exprList.drop(2))             // low priority operations
                    ?: throw Exception("Invalid expression ${exprList.drop(2).joinToString("")}"))

            "-" -> return exprList[0].toDouble() - (eval(exprList                         // low priority operations
                    .drop(2)
                    .map {                         // inverse sign example "a - b - c" == "a - (b + c)"
                        when (it) {
                            "-" -> "+"
                            "+" -> "-"
                            else -> it
                        }
                    }
                    .toList())
                    ?: throw Exception("Invalid expression ${exprList.drop(2).joinToString("")}"))
        }
    } catch (e: Exception) {
        println(e.message)
        return null
    }
    return null
}

fun pushToMemory(expr: String): Boolean {
    val s = expr.replace(" ", "")
    if (!checkIdentifier(s.substringBefore('='))) {
        println("Invalid identifier")
        return false
    }
    try {
        memory[s.substringBefore('=')] = eval(correct(s.substringAfter('=')))!!.toInt()
    } catch (e: Exception) {
        return false
    }
    return true
}

fun checkIdentifier(s: String): Boolean {
    s.forEach {
        if (it.toUpperCase() !in ('A'..'Z')) return false
    }
    return true
}
