package main.jlox;

public class Interpreter implements Expr.Visitor<Object> {
    private Object evaluate(Expr expr) {
        return expr.accept(this);
    }

    @Override
    public Object visitBinaryExpr(Expr.Binary expr) {
        Object left = evaluate(expr.left);
        Object right = evaluate(expr.right);

        switch (expr.operator.type) {
            case MINUS:
                return (double) left - (double) right;
            case PLUS:
                if (left instanceof Double && right instanceof Double) {
                    return (double) left + (double) right;
                }

                if (left instanceof String && right instanceof String) {
                    return (String) left + (String) right;
                }
                break;
            case SLASH:
                return (double) left / (double) right;
            case STAR:
                return (double) left * (double) right;

            default:
                break;

        }

        // unreachable code
        return null;
    }

    @Override
    public Object visitGroupingExpr(Expr.Grouping expr) {
        return evaluate(expr.expression);
    }

    @Override
    public Object visitLiteralExpr(Expr.Literal expr) {
        return expr.value;
    }

    @Override
    public Object visitUnaryExpr(Expr.Unary expr) {
        Object right = evaluate(expr.right);

        switch (expr.operator.type) {
            case BANG:
                return !isTruthy(right);
            case MINUS:
                return -(double) right;

            default:
                break;
        }

        // unreachable code
        return null;
    }

    private boolean isTruthy(Object value) {
        return !isFalsey(value);
    }

    private boolean isFalsey(Object value) {
        return value == null || Boolean.FALSE.equals(value);
    }
}
