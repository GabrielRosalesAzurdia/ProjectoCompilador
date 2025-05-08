package lexer;

import symboltable.Symbol;
import symboltable.SymbolTable;

import java.util.*;
import java.util.regex.*;

public class Lexer {
    private SymbolTable symbolTable;
    private String input;
    private List<Token> tokens;

    private static final Pattern TOKEN_PATTERN = Pattern.compile(
            "\\s*(?:(\\d+)|" +
                    "(\"[^\"]*\")|" +
                    "(\\w+)|" +
                    "(==|!=|<=|>=|<|>|\\+|\\-|\\*|\\/|=|\\{|\\}|\\(|\\)|;|\\.|\\,)|" +
                    "(\\S))"
    );

    private static final Set<String> RESERVED_WORDS = new HashSet<>(Arrays.asList(
            "if", "else", "for", "while", "do", "switch", "case", "break", "continue",
            "return", "public", "private", "protected", "class", "static", "void", "try", "catch", "finally"
    ));

    private static final Set<String> DATA_TYPES = new HashSet<>(Arrays.asList(
            "int", "float", "double", "char", "boolean", "String", "void"
    ));

    public Lexer(String input) {
        this.input = input;
        this.tokens = new ArrayList<>();
        this.symbolTable = new SymbolTable(null);  // null = no padre, raíz del ámbito
        // Cargar símbolos predefinidos
        symbolTable.insert("System", new Symbol("System", "class", "global"));
        symbolTable.insert("println", new Symbol("println", "method", "global"));
    }

    public List<Token> tokenize() {
        Matcher matcher = TOKEN_PATTERN.matcher(input);

        while (matcher.find()) {
            if (matcher.group(1) != null) {
                tokens.add(new Token(TokenType.NUMBER, matcher.group(1)));
            } else if (matcher.group(2) != null) {
                tokens.add(new Token(TokenType.STRING, matcher.group(2)));
            } else if (matcher.group(3) != null) {
                String identifier = matcher.group(3);
                if (DATA_TYPES.contains(identifier)) {
                    tokens.add(new Token(TokenType.DATA_TYPE, identifier));
                } else if (RESERVED_WORDS.contains(identifier)) {
                    tokens.add(new Token(TokenType.RESERVED, identifier));
                } else {
                    tokens.add(new Token(TokenType.IDENTIFIER, identifier));
                }
            } else if (matcher.group(4) != null) {
                String operator = matcher.group(4);
                tokens.add(new Token(TokenType.OPERATOR, operator));
            } else if (matcher.group(5) != null) {
                tokens.add(new Token(TokenType.UNKNOWN, matcher.group(5)));
            }
        }
        return tokens;
    }

    public SymbolTable getSymbolTable() {
        return symbolTable;
    }
}
