package calculator

val memory = mutableMapOf<String, Int>()
val opPrecedence = mapOf("(" to 3, "*" to 2, "/" to 2, "+" to 1, "-" to 1)

private fun String.infixToPostfixConverter(): String {  // convert to RPN (Reverse Polish Notation)
    val opStack = mutableListOf<String>()

    val infix = correct()                   // infix mutableList collections
    val postfix = mutableListOf<String>()                                   // postfix mutableList collections

    while (infix.isNotEmpty()) {

        // #1. Add operands (numbers and variables) to the result (postfix notation) as they arrive.
        if (infix.first()[0].isLetterOrDigit()) {
            if (infix.first().isValidPropertyName()) {                      // is valid property
                postfix.add(infix.first())
                infix.removeFirst()
            }
            if (infix.first()[0].isDigit()) {                               // begin with a digit thinking is a number
                postfix.add((infix.first().toIntOrNull()
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
            if (opStack.isEmpty()) throw Exception("Invalid expression. You have more right parenthesis")
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

private fun String.correct(): MutableList<String> { // replace all type of "+-.." or "-+.." with '+' or '-'
    return this
            .split(" ")
            .joinToString(" ") {
                if (it.length > 1 && it[0] in "-+" && it[1] in "-+")
                    if (it.count { it1 -> it1 == '-' } % 2 == 1)
                        " - "
                    else
                        " + "
                else it
                        .map { if (it !in "+-*/()") "$it" else " $it " }
                        .joinToString("")
                        .trim()
            }
            .split(" ")
            .toMutableList()
}

private fun String.pushToMemory() {
    val s = replace(" ", "")
    if (!s.substringBefore('=').isValidPropertyName()) throw Exception("Invalid identifier")
    memory[s.substringBefore('=')] = s.substringAfter('=').infixToPostfixConverter().evaluateRPN() }

private fun String.evaluateRPN(): Int {
    if (this.isValidPropertyName()) return memory[this] ?: throw Exception("Property $this not exist!")
    return this.toIntOrNull() ?: 0

    /* TODO Calculating the result
    When we have an expression in postfix notation, we can calculate it using another stack. To do that, scan the postfix expression from left to right:

    If the incoming element is a number, push it into the stack (the whole number, not a single digit!).

    If the incoming element is the name of a variable, push its value into the stack.
    If the incoming element is an operator, then pop twice to get two numbers and perform the operation; push the result on the stack.

    When the expression ends, the number on the top of the stack is a final result.*/
}

fun main() {
    do {
        val expr = readLine()!!
        when {
            expr == "" -> continue                  // empty line
            expr.indexOf('=') > 0 -> {         // memorise variable
                expr.pushToMemory()
                continue
            }
            memory.containsKey(expr) -> {           // print variable
                println(memory[expr])
                continue
            }
            expr == "/help" -> {                    // /help command
                println("The program calculates priority operations with recursive algorithm ")
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