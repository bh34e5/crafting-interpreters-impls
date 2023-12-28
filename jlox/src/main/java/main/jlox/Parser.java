package main.jlox;

import java.io.Serial;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Parser {
    private static class ParseError extends RuntimeException {
        @Serial
        private static final long serialVersionUID = 1L;

        ParseError() {
            super();
        }
    }

    private final List<Token> tokens;
    private int current = 0;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    public List<Stmt> parse() {
        List<Stmt> statements = new ArrayList<>();
        while (!isAtEnd()) {
            statements.add(declaration());
        }

        return statements;
    }

    private Stmt declaration() {
        try {
            if (match(TokenType.VAR))
                return varDeclaration();

            return statement();
        } catch (ParseError error) {
            synchronize();
            return null;
        }
    }

    private Stmt varDeclaration() {
        Token name = consume(TokenType.IDENTIFIER, "Expect variable name.");

        Expr initializer = null;
        if (match(TokenType.EQUAL)) {
            initializer = expression();
        }

        consume(TokenType.SEMICOLON, "Expect ';' after variable declaration.");
        return new Stmt.Var(name, initializer);
    }

    private Stmt statement() {
        if (match(TokenType.FOR))
            return forStatement();

        if (match(TokenType.IF))
            return ifStatement();

        if (match(TokenType.PRINT))
            return printStatement();

        if (match(TokenType.WHILE))
            return whileStatement();

        if (match(TokenType.LEFT_BRACE))
            return new Stmt.Block(block());

        return expressionStatement();
    }

    private Stmt forStatement() {
        consume(TokenType.LEFT_PAREN, "Expect '(' after 'for'.");

        Stmt initializer;
        if (match(TokenType.SEMICOLON)) {
            initializer = null;
        } else if (match(TokenType.VAR)) {
            initializer = varDeclaration();
        } else {
            initializer = expressionStatement();
        }

        Expr condition = null;
        if (!check(TokenType.SEMICOLON)) {
            condition = expression();
        }
        consume(TokenType.SEMICOLON, "Expect ';' after loop condition.");

        Expr increment = null;
        if (!check(TokenType.RIGHT_PAREN)) {
            increment = expression();
        }
        consume(TokenType.RIGHT_PAREN, "Expect ')' after for clauses.");

        Stmt body = statement();
        if (increment != null) {
            body = new Stmt.Block(
                    Arrays.asList(
                            body,
                            new Stmt.Expression(increment)));
        }

        if (condition == null)
            condition = new Expr.Literal(true);

        body = new Stmt.While(
                condition,
                body);

        if (initializer != null) {
            body = new Stmt.Block(
                    Arrays.asList(
                            initializer,
                            body));
        }

        return body;
    }

    private Stmt ifStatement() {
        consume(TokenType.LEFT_PAREN, "Expect '(' after 'if'.");
        Expr condition = expression();
        consume(TokenType.RIGHT_PAREN, "Expect ')' after if condition.");

        Stmt thenBranch = statement();
        Stmt elseBranch = null;
        if (match(TokenType.ELSE)) {
            elseBranch = statement();
        }

        return new Stmt.If(condition, thenBranch, elseBranch);
    }

    private Stmt printStatement() {
        Expr value = expression();
        consume(TokenType.SEMICOLON, "Expect ';' after value.");
        return new Stmt.Print(value);
    }

    private Stmt whileStatement() {
        consume(TokenType.LEFT_PAREN, "Expect '(' after 'while'.");
        Expr condition = expression();
        consume(TokenType.RIGHT_PAREN, "Expect ')' after while condition.");
        Stmt body = statement();

        return new Stmt.While(condition, body);
    }

    private List<Stmt> block() {
        List<Stmt> statements = new ArrayList<>();

        while (!check(TokenType.RIGHT_BRACE) && !isAtEnd()) {
            statements.add(declaration());
        }

        consume(TokenType.RIGHT_BRACE, "Expect '}' after block.");
        return statements;
    }

    private Stmt expressionStatement() {
        Expr value = expression();
        consume(TokenType.SEMICOLON, "Expect ';' after expression.");
        return new Stmt.Expression(value);
    }

    private Expr expression() {
        return assignment();
    }

    public Expr assignment() {
        Expr expr = or();

        if (match(TokenType.EQUAL)) {
            Token equals = previous();
            Expr value = assignment();

            if (expr instanceof Expr.Variable) {
                Token name = ((Expr.Variable) expr).name;
                return new Expr.Assign(name, value);
            }

            error(equals, "Invalid assignment target.");
        }

        return expr;
    }

    private Expr or() {
        return parseLeftAssociativeLogicalOp(this::and, TokenType.OR);
    }

    private Expr and() {
        return parseLeftAssociativeLogicalOp(this::equality, TokenType.AND);
    }

    private Expr equality() {
        return parseLeftAssociativeBinaryOp(this::comparison, TokenType.BANG_EQUAL, TokenType.EQUAL_EQUAL);
    }

    private Expr comparison() {
        return parseLeftAssociativeBinaryOp(this::term, TokenType.GREATER, TokenType.GREATER_EQUAL, TokenType.LESS,
                TokenType.LESS_EQUAL);
    }

    private Expr term() {
        return parseLeftAssociativeBinaryOp(this::factor, TokenType.PLUS, TokenType.MINUS);
    }

    private Expr factor() {
        return parseLeftAssociativeBinaryOp(this::unary, TokenType.STAR, TokenType.SLASH);
    }

    private Expr unary() {
        if (match(TokenType.BANG, TokenType.MINUS)) {
            Token operator = previous();
            Expr right = unary();
            return new Expr.Unary(operator, right);
        }
        return primary();
    }

    private Expr primary() {
        if (match(TokenType.FALSE)) {
            return new Expr.Literal(false);
        }

        if (match(TokenType.TRUE)) {
            return new Expr.Literal(true);
        }

        if (match(TokenType.NIL)) {
            return new Expr.Literal(null);
        }

        if (match(TokenType.NUMBER, TokenType.STRING)) {
            return new Expr.Literal(previous().literal);
        }

        if (match(TokenType.IDENTIFIER)) {
            return new Expr.Variable(previous());
        }

        if (match(TokenType.LEFT_PAREN)) {
            Expr expr = expression();
            consume(TokenType.RIGHT_PAREN, "Expect ')' after expression.");
            return new Expr.Grouping(expr);
        }

        String expectedTokens = Stream.of(TokenType.FALSE, TokenType.TRUE, TokenType.NIL, TokenType.NUMBER,
                TokenType.STRING, TokenType.LEFT_PAREN).map(TokenType::toString).collect(Collectors.joining(", "));

        throw error(peek(), "Expect expression.");
    }

    private Token consume(TokenType type, String message) {
        if (check(type))
            return advance();

        throw error(peek(), message);
    }

    private ParseError error(Token token, String message) {
        Lox.error(token, message);
        return new ParseError();
    }

    private void synchronize() {
        advance();

        while (!isAtEnd()) {
            if (previous().type == TokenType.SEMICOLON)
                return;

            switch (peek().type) {
                case CLASS:
                case FUN:
                case VAR:
                case FOR:
                case IF:
                case WHILE:
                case PRINT:
                case RETURN:
                    return;

                default:
                    break;
            }
            advance();
        }
    }

    private Expr parseLeftAssociativeBinaryOp(Supplier<Expr> higherOpSupplier, TokenType... validTokens) {
        return parseLeftAssociativeOp(Expr.Binary::new, higherOpSupplier, validTokens);
    }

    private Expr parseLeftAssociativeLogicalOp(Supplier<Expr> higherOpSupplier, TokenType... validTokens) {
        return parseLeftAssociativeOp(Expr.Logical::new, higherOpSupplier, validTokens);
    }

    private <T extends Expr> Expr parseLeftAssociativeOp(BinaryExprConstructor<Expr, Token, Expr, T> constructor,
            Supplier<Expr> higherOpSupplier, TokenType... validTokens) {
        Expr expr = higherOpSupplier.get();

        while (match(validTokens)) {
            Token operator = previous();
            Expr right = higherOpSupplier.get();
            expr = constructor.construct(expr, operator, right);
        }

        return expr;
    }

    private boolean match(TokenType... types) {
        for (TokenType type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }
        return false;
    }

    private boolean check(TokenType type) {
        if (isAtEnd())
            return false;

        return peek().type == type;
    }

    private Token advance() {
        if (!isAtEnd())
            current++;

        return previous();
    }

    private boolean isAtEnd() {
        return peek().type == TokenType.EOF;
    }

    private Token peek() {
        return tokens.get(current);
    }

    private Token previous() {
        return tokens.get(current - 1);
    }

    @FunctionalInterface
    interface BinaryExprConstructor<L, O, R, E> {
        public E construct(L left, O op, R right);
    }
}
