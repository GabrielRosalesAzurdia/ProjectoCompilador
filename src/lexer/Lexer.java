package lexer;

import symboltable.SymbolTable;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class Lexer {
    private SymbolTable symbolTable; // Tabla de símbolos
    private int currentScope = 0; // Ámbito actual (0 para global)
    private String input;
    private List<Token> tokens;

    private static final Pattern TOKEN_PATTERN = Pattern.compile(
            "\\s*(?:(\\d+)|" +              // Números
                    "(\"[^\"]*\")|" +                // Cadenas (entre comillas)
                    "(\\w+)|" +                     // Identificadores
                    "(==|!=|<=|>=|<|>|\\+|\\-|\\*|\\/|=|\\{|\\}|\\(|\\)|;|\\.|\\,)|" + // Operadores y símbolos
                    "(\\S))");                      // Cualquier otro carácter

    public Lexer(String input) {
        this.input = input;
        this.tokens = new ArrayList<>();
        this.symbolTable = new SymbolTable(); // Inicializar tabla de símbolos
    }

    private static final Set<String> RESERVED_WORDS = new HashSet<>(Arrays.asList(
            "if", "else", "for", "while", "do", "switch", "case", "break", "continue",
            "return", "public", "private", "protected", "class", "static", "void", "int",
            "float", "double", "char", "boolean", "String", "try", "catch", "finally"
    ));

    public List<Token> tokenize() {
        Matcher matcher = TOKEN_PATTERN.matcher(input);
        while (matcher.find()) {
            if (matcher.group(1) != null) { // Números
                String number = matcher.group(1);
                // No agregar a la tabla de símbolos
                tokens.add(new Token(TokenType.NUMBER, number));
            } else if (matcher.group(2) != null) { // Cadenas
                String stringValue = matcher.group(2);
                // No agregar a la tabla de símbolos
                tokens.add(new Token(TokenType.STRING, stringValue));
            } else if (matcher.group(3) != null) { // Identificadores o tipos de datos
                String identifier = matcher.group(3);
                if (RESERVED_WORDS.contains(identifier)) {
                    tokens.add(new Token(TokenType.RESERVED, identifier));
                } else if (isDataType(identifier)) {
                    tokens.add(new Token(TokenType.DATA_TYPE, identifier));

                    // Obtener el siguiente identificador (nombre de variable)
                    String nextIdentifier = getNextIdentifier(matcher);
                    if (nextIdentifier != null && !symbolTable.contains(nextIdentifier)) {
                        // Asignar el tipo de dato correspondiente
                        symbolTable.addSymbol(nextIdentifier, identifier, currentScope);
                    }
                } else {
                    // Solo agregar identificadores que no existan ya en la tabla
                    if (!symbolTable.contains(identifier)) {
                        // Aquí se debería asignar un tipo correcto si se está declarando
                        symbolTable.addSymbol(identifier, "unknown", currentScope);
                    }
                    tokens.add(new Token(TokenType.IDENTIFIER, identifier));
                }
            } else if (matcher.group(4) != null) { // Operadores
                String operator = matcher.group(4);
                if (operator.matches("==|!=|<=|>=|<|>")) {
                    tokens.add(new Token(TokenType.COMPARISON, operator));
                } else {
                    tokens.add(new Token(TokenType.OPERATOR, operator));
                }
            } else if (matcher.group(5) != null) { // Caracteres desconocidos
                tokens.add(new Token(TokenType.UNKNOWN, matcher.group(5)));
            }
        }
        return tokens;
    }

    private boolean isDataType(String identifier) {
        return identifier.matches("int|float|double|char|boolean|String");
    }

    private String getNextIdentifier(Matcher matcher) {
        // Obtener la posición actual del matcher
        int position = matcher.end(); // La posición después del tipo de dato
        String remainingInput = input.substring(position).trim(); // Recortar espacios

        // Crear un nuevo matcher para buscar el siguiente identificador
        Pattern identifierPattern = Pattern.compile("^\\s*(\\w+)"); // Busca un identificador (palabra)
        Matcher identifierMatcher = identifierPattern.matcher(remainingInput);

        if (identifierMatcher.find()) {
            // Si encontramos un identificador, devolverlo
            return identifierMatcher.group(1);
        }
        return null; // No se encontró un identificador
    }

    public SymbolTable getSymbolTable() {
        return symbolTable;
    }
}