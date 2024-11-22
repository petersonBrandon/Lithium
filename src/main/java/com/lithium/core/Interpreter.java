package com.lithium.core;

import com.lithium.parser.Expr;
import com.lithium.lexer.TokenType;
import com.lithium.parser.Stmt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Interpreter {
    private final Map<String, Object> environment;
    private final Map<String, Stmt.Function> functions;
    private final LithiumTestHandler testHandler;

    public Interpreter(Map<String, Object> environment,
                       Map<String, Stmt.Function> functions,
                       LithiumTestHandler testHandler) {
        this.environment = environment;
        this.functions = functions;
        this.testHandler = testHandler;
    }

    public Object evaluate(Expr expression) {
        if (expression instanceof Expr.Literal literal) {
            return literal.value;
        }

        if (expression instanceof Expr.Variable variable) {
            return environment.get(variable.name.getLexeme());
        }

        if (expression instanceof Expr.Binary binary) {
            return evaluateBinaryExpression(binary);
        }

        if (expression instanceof Expr.Unary unary) {
            return evaluateUnaryExpression(unary);
        }

        if (expression instanceof Expr.Logical logical) {
            return evaluateLogicalExpression(logical);
        }

        if (expression instanceof Expr.Assign assign) {
            return evaluateAssignmentExpression(assign);
        }

        if (expression instanceof Expr.Call call) {
            return evaluateFunctionCall(call, testHandler);
        }

        if (expression instanceof Expr.Range range) {
            return evaluateRangeExpression(range);
        }

        if (expression instanceof Expr.Grouping grouping) {
            return evaluate(grouping.expression);
        }

        if (expression instanceof Expr.Get get) {
            return evaluateGetExpression(get);
        }

        if (expression instanceof Expr.Postfix postfix) {
            return evaluatePostfixExpression(postfix);
        }

        throw new RuntimeException("Unsupported expression type: " + expression.getClass());
    }

    private Object evaluateRangeExpression(Expr.Range range) {
        Object start = evaluate(range.start);
        Object end = evaluate(range.end);

        if (!(start instanceof Number) || !(end instanceof Number)) {
            throw new RuntimeException("Range bounds must be numbers");
        }

        // Return a range object or list depending on your needs
        return new Object[] {start, end};  // This is a placeholder implementation
    }

    private Object evaluateGetExpression(Expr.Get get) {
        Object object = evaluate(get.object);

        if (object == null) {
            throw new RuntimeException("Cannot access properties of null");
        }

        // Implementation depends on how you want to handle property access
        // This could involve reflection, a custom object system, or map access
        throw new RuntimeException("Property access not yet implemented");
    }

    private Object evaluatePostfixExpression(Expr.Postfix postfix) {
        Object operand = evaluate(postfix.operand);

        if (!(operand instanceof Number)) {
            throw new RuntimeException("Postfix operators can only be applied to numbers");
        }

        Number value = (Number) operand;

        // Store the original value for returning
        Number originalValue = value;

        switch (postfix.operator.getType()) {
            case INCREMENT -> {
                if (value instanceof Integer) {
                    value = value.intValue() + 1;
                } else {
                    value = value.doubleValue() + 1;
                }
            }
            case DECREMENT -> {
                if (value instanceof Integer) {
                    value = value.intValue() - 1;
                } else {
                    value = value.doubleValue() - 1;
                }
            }
            default -> throw new RuntimeException("Unknown postfix operator: " + postfix.operator.getType());
        }

        // If the operand is a variable, update its value
        if (postfix.operand instanceof Expr.Variable var) {
            environment.put(var.name.getLexeme(), value);
        }

        // Postfix operators return the original value
        return originalValue;
    }

    public void defineVariable(String name, Object value) {
        environment.put(name, value);
    }

    public Object getVariable(String name) {
        if (!environment.containsKey(name)) {
            throw new RuntimeException("Undefined variable '" + name + "'");
        }
        return environment.get(name);
    }

    public void assignVariable(String name, Object value) {
        if (!environment.containsKey(name)) {
            throw new RuntimeException("Cannot assign to undefined variable '" + name + "'");
        }
        environment.put(name, value);
    }

    private Object evaluateBinaryExpression(Expr.Binary binary) {
        Object left = evaluate(binary.left);
        Object right = evaluate(binary.right);

        return switch (binary.operator.getType()) {
            case PLUS -> addValues(left, right);
            case MINUS -> subtractValues(left, right);
            case MULTIPLY -> multiplyValues(left, right);
            case DIVIDE -> divideValues(left, right);
            case EQUALS_EQUALS -> isEqual(left, right);
            case NOT_EQUALS -> !isEqual(left, right);
            case LESS_THAN -> compareValues(left, right) < 0;
            case LESS_EQUALS -> compareValues(left, right) <= 0;
            case GREATER_THAN -> compareValues(left, right) > 0;
            case GREATER_EQUALS -> compareValues(left, right) >= 0;
            default -> throw new RuntimeException("Unsupported binary operator: " + binary.operator.getType());
        };
    }

    private Object evaluateUnaryExpression(Expr.Unary unary) {
        Object right = evaluate(unary.right);

        return switch (unary.operator.getType()) {
            case MINUS -> {
                if (right instanceof Integer) {
                    yield -((Integer) right);
                }
                yield -((Number) right).doubleValue();
            }
            case NOT -> !isTruthy(right);
            default -> throw new RuntimeException("Unsupported unary operator: " + unary.operator.getType());
        };
    }

    private Object evaluateLogicalExpression(Expr.Logical logical) {
        Object left = evaluate(logical.left);

        if (logical.operator.getType() == TokenType.OR) {
            if (isTruthy(left)) return left;
        } else if (logical.operator.getType() == TokenType.AND) {
            if (!isTruthy(left)) return left;
        }

        return evaluate(logical.right);
    }

    private Object evaluateAssignmentExpression(Expr.Assign assign) {
        Object value = evaluate(assign.value);
        String varName = assign.name.getLexeme();

        switch (assign.operator.getType()) {
            case EQUALS:
                environment.put(varName, value);
                break;
            case PLUS_EQUALS:
                environment.put(varName, addValues(environment.get(varName), value));
                break;
            case MINUS_EQUALS:
                environment.put(varName, subtractValues(environment.get(varName), value));
                break;
            case MULTIPLY_EQUALS:
                environment.put(varName, multiplyValues(environment.get(varName), value));
                break;
            case DIVIDE_EQUALS:
                environment.put(varName, divideValues(environment.get(varName), value));
                break;
        }

        return value;
    }

    private Object evaluateFunctionCall(Expr.Call call, LithiumTestHandler testHandler) {
        Object callee = evaluate(call.callee);

        // If it's a variable name, look up the function in the functions map
        if (call.callee instanceof Expr.Variable) {
            String functionName = ((Expr.Variable) call.callee).name.getLexeme();
            Stmt.Function function = functions.get(functionName);

            if (function != null) {
                // Evaluate all arguments
                List<Object> arguments = new ArrayList<>();
                for (Expr argument : call.arguments) {
                    arguments.add(evaluate(argument));
                }

                // Check arity
                if (arguments.size() != function.params.size()) {
                    throw new RuntimeException("Expected " +
                            function.params.size() + " arguments but got " +
                            arguments.size() + " in call to '" + functionName + "'");
                }

                // Create new scope for function execution
                Map<String, Object> functionScope = new HashMap<>(environment);

                // Bind parameters to arguments
                for (int i = 0; i < function.params.size(); i++) {
                    String paramName = function.params.get(i).getLexeme();
                    Object argValue = arguments.get(i);
                    functionScope.put(paramName, argValue);
                }

                // Create new interpreter with function scope
                Interpreter functionInterpreter = new Interpreter(functionScope, functions, testHandler);

                // Execute function body
                Object returnValue = null;
                boolean hasReturn = false;

                for (Stmt stmt : function.body) {
                    try {
                        Object result = functionInterpreter.executeStatement(stmt, testHandler);
                        if (stmt instanceof Stmt.Return) {
                            returnValue = result;
                            hasReturn = true;
                            break;
                        }
                        // Keep track of the last expression value as potential return
                        if (stmt instanceof Stmt.Expression) {
                            returnValue = result;
                        }

                        // Check for control flow flags
                        if (testHandler.isReturning() ||
                                testHandler.isBreaking() ||
                                testHandler.isContinuing()) {
                            break;
                        }
                    } catch (Exception e) {
                        throw new RuntimeException("Error in function '" +
                                functionName + "': " + e.getMessage());
                    }
                }

                // Clear any control flow flags before returning
                testHandler.clearControlFlowFlags();

                return returnValue;
            }
        }

        // Handle built-in functions or callable objects
        if (callee instanceof LithiumCallable) {
            List<Object> arguments = new ArrayList<>();
            for (Expr argument : call.arguments) {
                arguments.add(evaluate(argument));
            }

            LithiumCallable function = (LithiumCallable) callee;
            if (arguments.size() != function.arity()) {
                throw new RuntimeException("Expected " +
                        function.arity() + " arguments but got " +
                        arguments.size() + ".");
            }

            return function.call(this, arguments);
        }

        throw new RuntimeException("Can only call functions.");
    }

    public Object executeStatement(Stmt stmt, LithiumTestHandler testHandler) {
        if (stmt instanceof Stmt.Return returnStmt) {
            if (returnStmt.value != null) {
                return evaluate(returnStmt.value);
            }
            return null;
        }

        // Let the test handler process all other statement types
        testHandler.processStatement(stmt);
        return null;
    }

    private Object addValues(Object left, Object right) {
        if (left instanceof Number && right instanceof Number) {
            Number leftNum = (Number)left;
            Number rightNum = (Number)right;

            // If both are integers, return integer
            if (leftNum instanceof Integer && rightNum instanceof Integer) {
                return leftNum.intValue() + rightNum.intValue();
            }
            // Otherwise use double
            return leftNum.doubleValue() + rightNum.doubleValue();
        }
        if (left instanceof String || right instanceof String) {
            return String.valueOf(left) + String.valueOf(right);
        }
        throw new RuntimeException("Cannot add these types");
    }

    private Object subtractValues(Object left, Object right) {
        if (left instanceof Number && right instanceof Number) {
            Number leftNum = (Number)left;
            Number rightNum = (Number)right;

            if (leftNum instanceof Integer && rightNum instanceof Integer) {
                return leftNum.intValue() - rightNum.intValue();
            }
            return leftNum.doubleValue() - rightNum.doubleValue();
        }
        throw new RuntimeException("Cannot subtract non-numeric types");
    }

    private Object multiplyValues(Object left, Object right) {
        if (left instanceof Number && right instanceof Number) {
            Number leftNum = (Number)left;
            Number rightNum = (Number)right;

            if (leftNum instanceof Integer && rightNum instanceof Integer) {
                return leftNum.intValue() * rightNum.intValue();
            }
            return leftNum.doubleValue() * rightNum.doubleValue();
        }
        throw new RuntimeException("Cannot multiply non-numeric types");
    }

    private Object divideValues(Object left, Object right) {
        if (left instanceof Number && right instanceof Number) {
            Number leftNum = (Number)left;
            Number rightNum = (Number)right;

            double rightValue = rightNum.doubleValue();
            if (rightValue == 0) {
                throw new RuntimeException("Division by zero");
            }

            // Division always returns double to avoid loss of precision
            return leftNum.doubleValue() / rightValue;
        }
        throw new RuntimeException("Cannot divide non-numeric types");
    }

    private int compareValues(Object left, Object right) {
        if (left instanceof Number && right instanceof Number) {
            Number leftNum = (Number)left;
            Number rightNum = (Number)right;

            if (leftNum instanceof Integer && rightNum instanceof Integer) {
                return Integer.compare(leftNum.intValue(), rightNum.intValue());
            }
            return Double.compare(leftNum.doubleValue(), rightNum.doubleValue());
        }
        throw new RuntimeException("Cannot compare these types");
    }

    private boolean isTruthy(Object value) {
        return switch (value) {
            case null -> false;
            case Boolean b -> b;
            case Number number -> number.doubleValue() != 0;
            default -> true;
        };
    }

    private boolean isEqual(Object left, Object right) {
        if (left == null && right == null) return true;
        if (left == null) return false;
        return left.equals(right);
    }
}
