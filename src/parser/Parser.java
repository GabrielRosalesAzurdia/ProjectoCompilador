package parser;

import ast.*;
import lexer.*;
import symboltable.*;

import java.util.*;

public class Parser {
    private final List<Token> tokens;
    private int current = 0;

    private SymbolTable symbolTable; // Tabla activa
    private final List<String> semanticErrors = new ArrayList<>();

    public Parser(List<Token> tokens, SymbolTable initialTable) {
        this.tokens = tokens;
        this.symbolTable = initialTable;
    }

    // Cambiar el método parse para que devuelva un Node
    public Node parse() {
        List<Node> statements = new ArrayList<>();
        while (!isAtEnd()) {
            statements.add(parseStatement());
        }
        return new BlockStatement(statements); // Devolver un bloque que contenga todas las declaraciones
    }

    private Node parseStatement() {
        Token token = peek();

        if (token.getType() == TokenType.DATA_TYPE) {
            return parseDeclaration(); // Devuelvo el nodo de declaración
        } else if (token.getType() == TokenType.IDENTIFIER) {
            return parseAssignmentOrCall(); // Devuelvo el nodo de asignación o llamada
        } else if (token.getType() == TokenType.RESERVED && token.getValue().equals("if")) {
            return parseIf(); // Devuelvo el nodo de if
        } else if (token.getType() == TokenType.OPERATOR && token.getValue().equals("{")) {
            enterScope(); // Entrar en un nuevo ámbito
            advance(); // Consumir '{'
            return null; // No devolvemos un nodo para bloques vacíos
        } else if (token.getType() == TokenType.OPERATOR && token.getValue().equals("}")) {
            exitScope(); // Salir del ámbito
            advance(); // Consumir '}'
            return null; // No devolvemos un nodo para bloques vacíos
        } else {
            advance(); // Ignora otros tokens
            return null; // Si no se reconoce, devolvemos null
        }
    }

    private Node parseDeclaration() {
        String type = advance().getValue(); // Tipo
        Token identifier = advance();

        if (identifier.getType() == TokenType.IDENTIFIER) {
            // Verifica si la variable ya está en el ámbito actual
            if (symbolTable.containsInCurrentScope(identifier.getValue())) {
                semanticErrors.add("Error: Redefinición de variable '" + identifier.getValue() + "'");
                return null; // No continuar con la declaración
            } else {
                // Inserta la nueva variable en la tabla de símbolos
                symbolTable.insert(identifier.getValue(), new Symbol(identifier.getValue(), type, "local"));
            }
        }

        // Esperamos ";" u otra asignación
        while (!isAtEnd() && !peek().getValue().equals(";")) {
            advance();
        }
        if (!isAtEnd()) advance(); // Consume el ;

        return new VariableDeclaration(type, identifier.getValue(), null); // Devuelve la declaración de variable
    }

    private Node parseAssignmentOrCall() {
        Token identifier = advance();
        Symbol symbol = symbolTable.lookup(identifier.getValue());

        if (symbol == null) {
            semanticErrors.add("Error: Variable '" + identifier.getValue() + "' no declarada");
        }

        // Consume hasta el ;
        while (!isAtEnd() && !peek().getValue().equals(";")) {
            advance();
        }
        if (!isAtEnd()) advance(); // Consume el ;

        return new ExpressionStatement(new VariableReference(identifier.getValue())); // Devuelve la referencia
    }

    // Método para parsear expresiones primarias
    private Expression primaryExpression() {
        Token token = peek();

        if (token.getType() == TokenType.IDENTIFIER) {
            advance(); // Consumir el identificador
            return new VariableReference(token.getValue()); // Asegurarse de que sea un Expression
        } else if (token.getType() == TokenType.NUMBER) {
            advance(); // Consumir el número
            return new NumberLiteral(Integer.parseInt(token.getValue())); // Asegurarse de que sea un Expression
        } else if (token.getType() == TokenType.STRING) {
            advance(); // Consumir la cadena
            return new StringLiteral(token.getValue()); // Asegurarse de que sea un Expression
        } else {
            throw new RuntimeException("Unexpected token in expression: " + token);
        }
    }

    private Token previous() {
        return tokens.get(current - 1); // Devuelve el token anterior
    }

    private Node parseIf() {
        advance(); // Consumir 'if'

        // Asegúrate de que hay un paréntesis de apertura
        if (match(TokenType.OPERATOR, "(")) {
            Node condition = parseCondition(); // Definir la variable condition

            // Asegúrate de que hay un paréntesis de cierre
            if (!match(TokenType.OPERATOR, ")")) {
                throw new RuntimeException("Error: Se esperaba ')' después de la condición en 'if'.");
            }

            Node thenBranch = parseBlock(); // Parsear el bloque 'then'

            Node elseBranch = null;
            if (match(TokenType.RESERVED, "else")) {
                elseBranch = parseBlock(); // Parsear el bloque 'else' si existe
            }

            return new IfStatement(condition, thenBranch, elseBranch);
        }

        throw new RuntimeException("Error: Se esperaba un '(' después de 'if'.");
    }

    private Node parseCondition() {
        return expression(); // Puedes usar tu método de expresión para obtener la condición
    }

    private Node expression() {
        Expression left = primaryExpression(); // Obtener la expresión primaria

        while (true) {
            if (match(TokenType.OPERATOR, "<") || match(TokenType.OPERATOR, ">") ||
                    match(TokenType.OPERATOR, "==") || match(TokenType.OPERATOR, "<=") ||
                    match(TokenType.OPERATOR, ">=")) {

                String operator = previous().getValue();
                Expression right = primaryExpression();
                left = new BinaryExpression(left, operator, right);
            } else {
                break; // Salir del bucle si no hay más operadores
            }
        }

        return left; // Devolver la expresión
    }

    // Método para parsear un bloque
    private Node parseBlock() {
        if (match(TokenType.OPERATOR, "{")) {
            List<Node> statements = new ArrayList<>();
            while (!isAtEnd() && !peek().getValue().equals("}")) {
                statements.add(parseStatement());
            }
            advance(); // Consumir '}'
            return new BlockStatement(statements); // Devolver el bloque
        }
        throw new RuntimeException("Error: Se esperaba un bloque de código.");
    }

    private void enterScope() {
        symbolTable = new SymbolTable(symbolTable);
    }

    private void exitScope() {
        if (symbolTable.getParent() != null) {
            symbolTable = symbolTable.getParent();
        }
    }

    private boolean isAtEnd() {
        return current >= tokens.size();
    }

    private Token peek() {
        return tokens.get(current);
    }

    private Token advance() {
        if (!isAtEnd()) current++;
        return tokens.get(current - 1);
    }

    private boolean match(TokenType type, String value) {
        if (!isAtEnd() && peek().getType() == type && peek().getValue().equals(value)) {
            advance();
            return true;
        }
        return false;
    }

    public List<String> getSemanticErrors() {
        return semanticErrors;
    }

    public SymbolTable getSymbolTable() {
        return symbolTable;
    }
}