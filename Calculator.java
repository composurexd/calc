package calc;

import java.util.*;

import static java.lang.Double.NaN;
import static java.lang.Double.parseDouble;
import static java.lang.Math.pow;


/*
 *   A calculator for rather simple arithmetic expressions
 *
 *   This is not the program, it's a class declaration (with methods) in it's
 *   own file (which must be named Calculator.java)
 *
 *   NOTE:
 *   - No negative numbers implemented
 */
public class Calculator {

    // Here are the only allowed instance variables!
    // Error messages (more on static later)
    final static String MISSING_OPERAND = "Missing or bad operand";
    final static String DIV_BY_ZERO = "Division with 0";
    final static String MISSING_OPERATOR = "Missing operator or parenthesis";
    final static String OP_NOT_FOUND = "Operator not found";

    // Definition of operators
    final static String OPERATORS = "+-*/^";

    // Method used in REPL
    double eval(String expr) {
        if (expr.length() == 0) {
            return NaN;
        }
        List<String> tokens = tokenize(expr);
        List<String> postfix = infix2Postfix(tokens);
        return evalPostfix(postfix);
    }

    // ------  Evaluate RPN expression -------------------

    double evalPostfix(List<String> postfix) {
        int count = 0;
        for (int i = 0; i < postfix.size(); i++) {
            String curStr = postfix.get(i);
            if (!isOperator(curStr)) {
                stack.push(curStr);
            } else if (stack.size() <= 1 && isOperator(curStr)) {
                return 0;
            }
            else {
                stack.push(Double.toString((applyOperator(curStr, Double.parseDouble(stack.pop()), Double.parseDouble(stack.pop())))));
                count++;
            }
        }
        if (count == 0 && stack.size() > 1) {
            return 0;
        }
        else {
            return Double.parseDouble(stack.pop());
        }
    }

    double applyOperator(String op, double d1, double d2) {
        switch (op) {
            case "+":
                return d1 + d2;
            case "-":
                return d2 - d1;
            case "*":
                return d1 * d2;
            case "/":
                if (d1 == 0) {
                    throw new IllegalArgumentException(DIV_BY_ZERO);
                }
                return d2 / d1;
            case "^":
                return pow(d2, d1);
        }
        throw new RuntimeException(OP_NOT_FOUND);
    }

    // ------- Infix 2 Postfix ------------------------
    Stack<String> stack = new Stack();
    List<String> outputString = new ArrayList();

    List<String> infix2Postfix(List<String> infix) {
        outputString.clear();
        int count = 0;
        stack.clear();
        for (int i = 0; i < infix.size(); i++) {
            String curStr = infix.get(i);
            if (isOpeningOperator(curStr)) {
                stack.push(curStr);
                count++;
                if (i == infix.size() - 1) {
                    outputString.clear();
                    outputString.add("0");
                    return outputString;
                }
            } else if (isClosingOperator(curStr)) {
                if ((i == infix.size() - 1) && count == 0) {
                    outputString.clear();
                    outputString.add("0");
                    return outputString;
                }
                handleClosingParenthesis();
                emptyStack(i, infix);
                count--;

            } else if (!isOperator(curStr)) {
                outputString.add(curStr);
                emptyStack(i, infix);

            } else if (((isOperator(curStr)) && ((getPrecedence(curStr)) < (stackPeekPrec())))) {
                while (!stack.isEmpty()) {
                    outputString.add(stack.pop());
                }
                stack.push(curStr);
                emptyStack(i, infix);
            } else if (((isOperator(curStr)) && ((getPrecedence(curStr)) == (stackPeekPrec())))) {
                if (getAssociativity(curStr) == Assoc.LEFT) {
                    outputString.add(stack.pop());
                    stack.push(curStr);
                    emptyStack(i, infix);
                }
                else if (getAssociativity(curStr) == Assoc.RIGHT) {
                    stack.push(curStr);
                    emptyStack(i, infix);
                }
            } else {
                stack.push(curStr);
                emptyStack(i, infix);
            }
        }
        if (count > 0) {
            outputString.clear();
            outputString.add("0");
            return outputString;
        }

        return outputString;
    }

    void emptyStack(int i, List<String> infix) {
        if (i == infix.size() - 1) {
            while (!stack.isEmpty()) {
                if ("()".contains(stack.peek())) {
                    stack.pop();
                } else {
                    outputString.add(stack.pop());
                }
            }
        }
    }
    int stackPeekPrec() {
        if (stack.isEmpty()) {
            return -1;
        } else{
            return getPrecedence(stack.peek());
        }
    }


    public void handleClosingParenthesis() {
        while (!isOpeningOperator(stack.peek())) {
            outputString.add(stack.pop());
        }
    }

    public boolean isClosingOperator(String operator) {
        return ")".equals(operator);
    }

    public boolean isOpeningOperator(String operator) {
        return "(".equals(operator);
    }

    public boolean isOperator(String op) {
        switch (op) {
            case "+":
                return true;
            case "-":
                return true;
            case "/":
                return true;
            case "*":
                return true;
            case "^":
                return true;
            default:
                return false;
        }
    }


    int getPrecedence(String op) {
        if ("+-".contains(op)) {
            return 2;
        } else if ("*/".contains(op)) {
            return 3;
        } else if ("^".contains(op)) {
            return 4;
        } else if (("()".contains(op))) {
            return -2;
        }
        else {
            throw new RuntimeException(OP_NOT_FOUND);
        }
    }

    Assoc getAssociativity(String op) {
        if ("+-*/".contains(op)) {
            return Assoc.LEFT;
        } else if ("^".contains(op)) {
            return Assoc.RIGHT;
        } else {
            throw new RuntimeException(OP_NOT_FOUND);
        }
    }

    enum Assoc {
        LEFT,
        RIGHT
    }

    // ---------- Tokenize -----------------------

    // List String (not char) because numbers (with many chars)

    List<String> tokenize(String expr) {
        // Here we use a LookAhead and LookBehind
        String delimiter = "((?<=[-(+*/^)])|(?=[-(+*/^)]))";
        if (!("+-/*^".contains(expr)) && "\\s".contains(expr)) {
            return Arrays.asList("0");
        }
        return Arrays.asList(expr.replaceAll("\\s", "").split(delimiter));
    }
}

