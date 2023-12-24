package main.jlox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Scanner {
    private final String source;
    private final List<Token> tokens = new ArrayList<>();
    private int start = 0;
    private int current = 0;
    private int line = 1;

    private static final Map<String, TokenType> RESERVED_KEYWORDS_MAP;

    static {
        RESERVED_KEYWORDS_MAP = new HashMap<>();
        RESERVED_KEYWORDS_MAP.put("and", TokenType.AND);
        RESERVED_KEYWORDS_MAP.put("class", TokenType.CLASS);
        RESERVED_KEYWORDS_MAP.put("else", TokenType.ELSE);
        RESERVED_KEYWORDS_MAP.put("false", TokenType.FALSE);
        RESERVED_KEYWORDS_MAP.put("for", TokenType.FOR);
        RESERVED_KEYWORDS_MAP.put("fun", TokenType.FUN);
        RESERVED_KEYWORDS_MAP.put("if", TokenType.IF);
        RESERVED_KEYWORDS_MAP.put("nil", TokenType.NIL);
        RESERVED_KEYWORDS_MAP.put("or", TokenType.OR);
        RESERVED_KEYWORDS_MAP.put("print", TokenType.PRINT);
        RESERVED_KEYWORDS_MAP.put("return", TokenType.RETURN);
        RESERVED_KEYWORDS_MAP.put("super", TokenType.SUPER);
        RESERVED_KEYWORDS_MAP.put("this", TokenType.THIS);
        RESERVED_KEYWORDS_MAP.put("true", TokenType.TRUE);
        RESERVED_KEYWORDS_MAP.put("var", TokenType.VAR);
        RESERVED_KEYWORDS_MAP.put("while", TokenType.WHILE);
    }

    public Scanner(String source) {
        this.source = source;
    }

    public List<Token> scanTokens() {
        while (!isAtEnd()) {
            start = current;
            scanToken();
        }

        tokens.add(new Token(TokenType.EOF, "", null, line));
        return tokens;
    }

    private void scanToken() {
        char c = advance();
        switch (c) {
            case '(':
                addToken(TokenType.LEFT_PAREN);
                break;
            case ')':
                addToken(TokenType.RIGHT_PAREN);
                break;
            case '{':
                addToken(TokenType.LEFT_BRACE);
                break;
            case '}':
                addToken(TokenType.RIGHT_BRACE);
                break;
            case ',':
                addToken(TokenType.COMMA);
                break;
            case '.':
                addToken(TokenType.DOT);
                break;
            case '-':
                addToken(TokenType.MINUS);
                break;
            case '+':
                addToken(TokenType.PLUS);
                break;
            case ';':
                addToken(TokenType.SEMICOLON);
                break;
            case '*':
                addToken(TokenType.STAR);
                break;
            case '!':
                addToken(match('=') ? TokenType.BANG_EQUAL : TokenType.BANG);
                break;
            case '=':
                addToken(match('=') ? TokenType.EQUAL_EQUAL : TokenType.EQUAL);
                break;
            case '<':
                addToken(match('=') ? TokenType.LESS_EQUAL : TokenType.LESS);
                break;
            case '>':
                addToken(match('=') ? TokenType.GREATER_EQUAL : TokenType.GREATER);
                break;
            case '/':
                if (match('/')) {
                    while (peek() != '\n' && !isAtEnd())
                        advance();
                } else {
                    addToken(TokenType.SLASH);
                }
                break;
            case ' ':
            case '\r':
            case '\t':
                // ignore whitespace
                break;
            case '\n':
                // increment line count, then ignore
                line++;
                break;
            case '"':
                string();
                break;
            default:
                if (isDigit(c)) {
                    number();
                } else if (isAlpha(c)) {
                    identifier();
                } else {
                    Lox.error(line, "Unexpected character.");
                }
                break;
        }
    }

    private void string() {
        while (peek() != '"' && !isAtEnd()) {
            if (peek() == '\n')
                line++;
            advance();
        }

        if (isAtEnd()) {
            Lox.error(line, "Unterminated string.");
            return;
        }

        // the closing '"'.
        advance();

        // trim surrounding quotes
        String value = source.substring(start + 1, current - 1);
        addToken(TokenType.STRING, value);
    }

    private void number() {
        while (isDigit(peek()))
            advance();

        // look for fractional part, signaled by '.'
        if (peek() == '.' && isDigit(peekNext())) {
            // consume the '.''
            advance();

            // read the rest of the digits
            while (isDigit(peek()))
                advance();
        }

        addToken(TokenType.NUMBER,
                Double.parseDouble(source.substring(start, current)));
    }

    private void identifier() {
        while (isAlphaNumeric(peek()))
            advance();

        String text = source.substring(start, current);
        TokenType type = RESERVED_KEYWORDS_MAP.get(text);
        if (type == null) {
            type = TokenType.IDENTIFIER;
        }

        addToken(type);
    }

    private boolean isAtEnd() {
        return current >= source.length();
    }

    private char advance() {
        return source.charAt(current++);
    }

    private void addToken(TokenType type) {
        addToken(type, null);
    }

    private void addToken(TokenType type, Object literal) {
        String text = source.substring(start, current);
        tokens.add(new Token(type, text, literal, line));
    }

    private boolean match(char expected) {
        if (isAtEnd())
            return false;
        if (source.charAt(current) != expected)
            return false;

        current++;
        return true;
    }

    private char peek() {
        if (isAtEnd())
            return '\0';

        return source.charAt(current);
    }

    private char peekNext() {
        if (current + 1 >= source.length())
            return '\0';
        return source.charAt(current + 1);
    }

    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z')
                || (c >= 'A' && c <= 'Z')
                || c == '_';
    }

    private boolean isAlphaNumeric(char c) {
        return isAlpha(c) || isDigit(c);
    }
}
