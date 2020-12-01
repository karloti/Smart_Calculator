package calculator

import java.math.BigInteger

val memory = mutableMapOf<String, BigInteger>()
val opPrecedence = mapOf(
        "(" to 4,
        "^" to 3,
        "*" to 2,
        "/" to 2,
        "+" to 1,
        "-" to 1,
        ")" to 0,
)

private fun String.infixToPostfixConverter(): String {  // convert to RPN (Reverse Polish Notation)
    val opStack = mutableListOf<String>()

    val infix = correct() ?: throw Exception("Invalid expression")                  // infix mutableList collections
    val postfix = mutableListOf<String>()                                   // postfix mutableList collections

    while (infix.isNotEmpty()) {

        // #1. Add operands (numbers and variables) to the result (postfix notation) as they arrive.
        if (infix.first()[0].isLetterOrDigit())
            when {
                infix.first().isValidPropertyName() -> {                      // is valid property
                    postfix.add(infix.first())
                    infix.removeFirst()
                }
                infix.first()[0].isDigit() -> {                               // begin with a digit thinking is a number
                    postfix.add((infix.first().toBigIntegerOrNull()
                            ?: throw Exception("Invalid expression ${infix.first()}")).toString())
                    infix.removeFirst()
                }
            }

        if (infix.isEmpty()) break

        if (opPrecedence.containsKey(infix.first())) {

            // #2. If the stack is empty or contains a left parenthesis on top, push the incoming operator on the stack.
            if (opStack.isEmpty() || (opStack.lastOrNull() ?: "") == "(") {
                opStack.add(infix.first())
                infix.removeFirst()
                continue
            }

            // #3. If the incoming operator has higher precedence than the top of the stack, push it on the stack.
            if (opStack.isNotEmpty() && opPrecedence[infix.first()]!! > opPrecedence[opStack.last()]!!) {
                opStack.add(infix.first())
                infix.removeFirst()
                continue
            }

            // #4. If the incoming operator has lower or equal precedence than or to the top of the stack,
            // pop the stack and add operators to the result until you see an operator that has a smaller
            // precedence or a left parenthesis on the top of the stack; then add the incoming operator to the stack.
            while (opStack.isNotEmpty() && opStack.last() != "(" &&
                    opPrecedence[infix.first()]!! <= opPrecedence[opStack.last()]!!) {
                postfix.add(opStack.last())
                opStack.removeLast()
                continue
            }

            // #5. If the incoming element is a left parenthesis, push it on the stack.
            if (infix.first() == "(") {
                opStack.add(infix.first())
                infix.removeFirst()
                continue
            }
        }

        // #6. If the incoming element is a right parenthesis, pop the stack and add operators to the result
        // until you see a left parenthesis. Discard the pair of parentheses.
        if (infix.first() == ")") {
            while (opStack.isNotEmpty() && opStack.last() != "(") {
                postfix.add(opStack.last())
                opStack.removeLast()
            }
            if (opStack.isEmpty()) throw Exception("Invalid expression")
            opStack.removeLast()
            infix.removeFirst()
        }
    }

    while (opStack.isNotEmpty()) {
        postfix.add(opStack.last())
        opStack.removeLast()
        continue
    }
    return postfix.joinToString(" ")
}

private fun String.isValidPropertyName(): Boolean {      // when property is not valid return FALSE
    this.forEach { if (!it.isLetter()) return false }
    return true
}

private fun String.correct(): MutableList<String>? { // replace all type of "+-.." or "-+.." with '+' or '-'
    if (this == "") return null

    var s = ""
    var tmp = ""
    var i = 0

    while (i < this.lastIndex) {
        var j = i
        var minusCount = 0

        if (this[i] in "-+" && this[i + 1] in "-+")
            while (this[j] in "-+")
                if (this[j++] == '-') minusCount++

        tmp += when {
            minusCount == 0 -> this[i]
            minusCount % 2 == 1 -> '-'
            else -> '+'
        }

        i = j + 1

        if (
                tmp.length > 1 &&
                (tmp[tmp.lastIndex] in "^*/+-") &&
                (tmp[tmp.lastIndex - 1] in "^*/+-")
        ) return null

    }

    if (i == this.lastIndex) tmp += this.last()

    for (ch in tmp) {
        if (opPrecedence.containsKey("$ch")) s += " $ch " else if (ch != ' ') s += ch
    }
    while (s.contains("  ")) s = s.replace("  ", " ")
    return s.trim().split(" ").toMutableList()
}


private fun String.pushToMemory() {
    val s = replace(" ", "")
    if (!s.substringBefore('=').isValidPropertyName()) throw Exception("Invalid identifier")
    memory[s.substringBefore('=')] = s.substringAfter('=').infixToPostfixConverter().evaluateRPN()
}

private fun String.evaluateRPN(): BigInteger {

    if (this.isValidPropertyName())
        return memory[this] ?: throw Exception("Unknown variable")

    try {
        return this.toBigInteger()
    } catch (e: Exception) {
    }

    val postfix = this.split(" ").toMutableList()
    val stack = mutableListOf<String>()

    while (postfix.isNotEmpty()) {

        //  1. If the incoming element is a number, push it into the stack (the whole number, not a single digit!).
        if (postfix.first().toBigIntegerOrNull() != null) {
            stack.add(postfix.first())
            postfix.removeFirst()
            continue
        }

        //  2. If the incoming element is the name of a variable, push its value into the stack.
        if (memory[postfix.first()] != null) {
            stack.add(memory[postfix.first()].toString())
            postfix.removeFirst()
            continue
        }

        //  3. If the incoming element is an operator, then pop twice to get two numbers and perform the operation;
        //  push the result on the stack.
        if (opPrecedence.containsKey(postfix.first())) {

            val b: BigInteger
            if (stack.isNotEmpty()) {
                b = stack.last().toBigInteger()
                stack.removeLast()
            } else throw Exception("Invalid expression")

            val a: BigInteger
            if (stack.isNotEmpty()) {
                a = stack.last().toBigInteger()
                stack.removeLast()
            } else a = "0".toBigInteger()

            val result: BigInteger by lazy {
                when (postfix.first()) {
                    "+" -> a + b
                    "-" -> a - b
                    "/" -> a / b
                    "*" -> a * b
                    "^" -> a.pow(b.toInt())
                    else -> throw Exception("Invalid expression")
                }
            }
            stack.add(result.toString())
            postfix.removeFirst()
            continue
        }
    }

    //  4. When the expression ends, the number on the top of the stack is a final result.
    return stack.last().toBigInteger()
}

fun main() {
    do {
        val expr = readLine()!!
        when {
            expr == "" -> continue                  // empty line

            expr.indexOf('=') > 0 -> {         // memorise variable
                try {
                    expr.pushToMemory()
                } catch (e: Exception) {
                    println(e.message)
                }
                continue
            }

            memory.containsKey(expr) -> {           // print variable
                println(memory[expr])
                continue
            }
            expr == "/help" -> {                    // /help command
                println("The program calculates priority operations with RNP (Reverse Polish Notation) algorithm")
                continue
            }
            expr == "/exit" -> break                // exit command
            expr[0] == '/' -> {
                println("Unknown command")
                continue
            }
            else ->
                try {                               // intent to calculate expresion
                    println(expr.infixToPostfixConverter().evaluateRPN())
                } catch (e: Exception) {
                    println(e.message)
                }
        }
    } while (true)
    println("Bye!")
    println(memory)
}