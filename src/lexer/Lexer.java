package lexer;

import symboltable.SymbolTable;

import java.util.*;
import java.util.regex.*;

public class Lexer {
    private SymbolTable symbolTable;
    private int currentScope = 0; // Ámbito actual
    private String input;
    private List<Token> tokens;

    private static final Pattern TOKEN_PATTERN = Pattern.compile(
            "\\s*(?:(\\d+)|" +              // Números
                    "(\"[^\"]*\")|" +           // Cadenas
                    "(\\w+)|" +                      // Identificadores
                    "(==|!=|<=|>=|<|>|\\+|\\-|\\*|\\/|=|\\{|\\}|\\(|\\)|;|\\.|\\,)|" + // Operadores y símbolos
                    "(\\S))"                        // Caracteres desconocidos
    );

    private static final Set<String> RESERVED_WORDS = new HashSet<>(Arrays.asList(
            "if", "else", "for", "while", "do", "switch", "case", "break", "continue",
            "return", "public", "private", "protected", "class", "static", "void", "int",
            "float", "double", "char", "boolean", "String", "try", "catch", "finally"
    ));

    public Lexer(String input) {
        this.input = input;
        this.tokens = new ArrayList<>();
        this.symbolTable = new SymbolTable();
        // Agregar identificadores comunes
        symbolTable.addSymbol("System", "class", currentScope);
        symbolTable.addSymbol("println", "method", currentScope);
    }

    public List<Token> tokenize() {
        Matcher matcher = TOKEN_PATTERN.matcher(input);
        String lastDataType = null; // Almacena el último tipo de dato encontrado
        boolean expectIdentifier = false; // Indica si esperamos un identificador después de un tipo de dato

        while (matcher.find()) {
            if (matcher.group(1) != null) { // Números
                tokens.add(new Token(TokenType.NUMBER, matcher.group(1)));
            } else if (matcher.group(2) != null) { // Cadenas
                tokens.add(new Token(TokenType.STRING, matcher.group(2)));
            } else if (matcher.group(3) != null) { // Identificadores y palabras clave
                String identifier = matcher.group(3);

                if (RESERVED_WORDS.contains(identifier)) {
                    tokens.add(new Token(TokenType.RESERVED, identifier));

                    // Manejo de tipos de datos
                    if (isDataType(identifier)) {
                        lastDataType = identifier;
                        expectIdentifier = true;
                    } else if (identifier.equals("class")) {
                        // Incrementar el ámbito al declarar una clase
                        currentScope++;
                    }
                } else {
                    if (expectIdentifier) {
                        // Guardar en la tabla de símbolos con el tipo detectado
                        symbolTable.addSymbol(identifier, lastDataType, currentScope);
                        expectIdentifier = false;
                    } else {
                        if (!symbolTable.contains(identifier)) {
                            symbolTable.addSymbol(identifier, "unknown", currentScope);
                        }
                    }
                    tokens.add(new Token(TokenType.IDENTIFIER, identifier));
                }
            } else if (matcher.group(4) != null) { // Operadores y símbolos
                String operator = matcher.group(4);
                if (operator.equals(";")) {
                    tokens.add(new Token(TokenType.SEMICOLON, operator)); // Registra como SEMICOLON
                } else {
                    tokens.add(operator.matches("==|!=|<=|>=|<|>") ? new Token(TokenType.COMPARISON, operator) : new Token(TokenType.OPERATOR, operator));
                }

                // Manejo de asignaciones
                if (operator.equals("=")) {
                    lastDataType = null; // Resetear tipo al asignar
                }
            }else if (matcher.group(5) != null) { // Caracteres desconocidos
                tokens.add(new Token(TokenType.UNKNOWN, matcher.group(5)));
            }
        }

        // Decrementar el ámbito después de procesar el bloque
        currentScope--;

        return tokens;
    }

    private boolean isDataType(String identifier) {
        return identifier.matches("int|float|double|char|boolean|String");
    }

    public SymbolTable getSymbolTable() {
        return symbolTable;
    }
}