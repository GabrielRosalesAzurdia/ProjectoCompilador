package parser;

import lexer.Lexer;
import lexer.Token;
import lexer.TokenType;
import ast.*;

import java.util.ArrayList;
import java.util.List;

public class Parser {
    private List<Token> tokens;
    private int currentTokenIndex = 0;

    public Parser(Lexer lexer) {
        this.tokens = lexer.tokenize();
    }

    private Token currentToken() {
        if (currentTokenIndex >= tokens.size()) {
            return new Token(TokenType.EOF, ""); // Retornar un token especial en lugar de lanzar una excepción
        }
        return tokens.get(currentTokenIndex);
    }
    private void consume(TokenType type) {
        if (currentTokenIndex >= tokens.size()) {
            throw new RuntimeException("Error: Intento de consumir un token fuera del rango.");
        }
        if (currentToken().getType() == type) {
            currentTokenIndex++;
        } else {
            throw new RuntimeException("Unexpected token: " + currentToken());
        }
    }


    public Node parse() {
        return program();
    }

    private Node program() {
        List<Node> statements = new ArrayList<>();
        while (currentTokenIndex < tokens.size()) {
            if (currentTokenIndex >= tokens.size()) break; // Evita sobrepasar el límite
            statements.add(statement());
        }
        return new BlockStatement(statements);
    }

    private Node statement() {
        if (currentToken().getType() == TokenType.DATA_TYPE) {
            return variableDeclaration(); // Declaración de variable
        } else if (currentToken().getValue().equals("class")) {
            return classDeclaration(); // Declaración de clase
        } else if (currentToken().getValue().equals("if")) {
            return ifStatement(); // Declaración if
        } else if (currentToken().getValue().equals("while")) {
            return whileStatement(); // Declaración while
        } else if (currentToken().getValue().equals("for")) {
            return forStatement(); // Declaración for
        } else if (currentToken().getValue().equals("return")) {
            return returnStatement(); // Declaración return
        } else {
            return expressionStatement(); // 🚀 Nuevo: Manejo de expresiones sueltas
        }
    }


    private MethodDeclaration methodDeclaration() {
        String returnType = null;
        String methodName;

        // Puede empezar con tipo de dato o nombre de clase (para constructor)
        if (currentToken().getType() == TokenType.DATA_TYPE) {
            returnType = currentToken().getValue();
            consume(TokenType.DATA_TYPE);
        }

        methodName = currentToken().getValue(); // Nombre del método o constructor
        consume(TokenType.IDENTIFIER);

        consume(TokenType.OPERATOR); // '('

        List<VariableDeclaration> parameters = new ArrayList<>();
        while (currentToken().getType() != TokenType.OPERATOR || !currentToken().getValue().equals(")")) {
            String paramType = currentToken().getValue();
            consume(TokenType.DATA_TYPE);

            String paramName = currentToken().getValue();
            consume(TokenType.IDENTIFIER);

            parameters.add(new VariableDeclaration(paramType, paramName, null));

            if (currentToken().getValue().equals(",")) {
                consume(TokenType.OPERATOR);
            }
        }

        consume(TokenType.OPERATOR); // ')'
        BlockStatement body = blockStatement();

        return new MethodDeclaration(returnType, methodName, parameters, body);
    }

    private ClassDeclaration classDeclaration() {
        consume(TokenType.RESERVED); // Consumir 'class'
        String className = currentToken().getValue(); // Nombre de la clase
        consume(TokenType.IDENTIFIER); // Consumir el nombre de la clase

        List<VariableDeclaration> attributes = new ArrayList<>();
        List<MethodDeclaration> methods = new ArrayList<>();
        consume(TokenType.OPERATOR); // Consumir '{'

        while (currentToken().getType() != TokenType.OPERATOR || !currentToken().getValue().equals("}")) {

            // Soporte para modificadores como 'public'
            boolean hasModifier = false;
            if (currentToken().getType() == TokenType.RESERVED &&
                    (currentToken().getValue().equals("public") || currentToken().getValue().equals("private") || currentToken().getValue().equals("protected"))) {
                consume(TokenType.RESERVED); // Consumir el modificador
                hasModifier = true;
            }

            // Comprobamos si es un método, constructor o atributo
            if (currentToken().getType() == TokenType.DATA_TYPE || currentToken().getType() == TokenType.IDENTIFIER) {
                int tempIndex = currentTokenIndex;
                String first = currentToken().getValue();
                consume(currentToken().getType());

                if (currentToken().getType() == TokenType.IDENTIFIER) {
                    String second = currentToken().getValue();
                    consume(TokenType.IDENTIFIER);

                    if (currentToken().getValue().equals("(")) {
                        // Es un método
                        currentTokenIndex = tempIndex;
                        methods.add(methodDeclaration());
                    } else {
                        // Es un atributo
                        currentTokenIndex = tempIndex;
                        attributes.add(variableDeclaration());
                    }
                } else if (currentToken().getValue().equals("(")) {
                    // Es un constructor
                    currentTokenIndex = tempIndex;
                    methods.add(methodDeclaration()); // Constructor se trata igual que un método sin tipo de retorno
                } else {
                    throw new RuntimeException("Expected identifier or '(' after type/name in class body.");
                }

            } else {
                throw new RuntimeException("Unexpected token in class body: " + currentToken());
            }
        }


        consume(TokenType.OPERATOR); // Consumir '}'
        return new ClassDeclaration(className, attributes, methods);
    }

    private VariableDeclaration variableDeclaration() {
        String type = currentToken().getValue();
        consume(TokenType.DATA_TYPE); // Consumir tipo de dato

        String identifier = currentToken().getValue();
        consume(TokenType.IDENTIFIER); // Consumir identificador

        Expression initializer = null;
        if (currentToken().getType() == TokenType.OPERATOR && currentToken().getValue().equals("=")) {
            consume(TokenType.OPERATOR); // Consumir '='
            initializer = expression(); // Obtener la expresión
        }

        consume(TokenType.SEMICOLON); // Consumir ';'
        return new VariableDeclaration(type, identifier, initializer);
    }

    private Node expressionStatement() {
        Expression expr = expression();  // Evaluar expresión
        consume(TokenType.SEMICOLON);   // Consumir ';'
        return new ExpressionStatement(expr);
    }

    private Node ifStatement() {
        consume(TokenType.RESERVED); // Consumir 'if'
        consume(TokenType.OPERATOR); // Consumir '('

        Expression condition = expression(); // Obtener la condición

        // Evitar que la condición sea una asignación
        if (condition instanceof Assignment) {
            throw new RuntimeException("Syntax error: Assignment '=' is not allowed in if conditions. Use '==' instead.");
        }

        consume(TokenType.OPERATOR); // Consumir ')'

        BlockStatement thenBranch = (BlockStatement) blockStatement(); // Cuerpo del if

        BlockStatement elseBranch = null;
        if (currentToken().getValue().equals("else")) {
            consume(TokenType.RESERVED); // Consumir 'else'
            elseBranch = (BlockStatement) blockStatement(); // Cuerpo del else
        }

        return new IfStatement(condition, thenBranch, elseBranch);
    }



    private Node whileStatement() {
        consume(TokenType.IDENTIFIER); // Consumir 'while'
        consume(TokenType.OPERATOR); // Consumir '('
        Expression condition = expression(); // Obtener la condición
        consume(TokenType.OPERATOR); // Consumir ')'

        BlockStatement body = (BlockStatement) blockStatement(); // Cuerpo del while
        return new WhileStatement(condition, body);
    }

    private Node forStatement() {
        consume(TokenType.IDENTIFIER); // Consumir 'for'
        consume(TokenType.OPERATOR); // Consumir '('

        VariableDeclaration initializer = variableDeclaration(); // Inicialización
        Expression condition = expression(); // Condición
        consume(TokenType.OPERATOR); // Consumir ';'
        Expression update = expression(); // Actualización

        consume(TokenType.OPERATOR); // Consumir ')'
        BlockStatement body = (BlockStatement) blockStatement(); // Cuerpo del for

        return new ForStatement(initializer, condition, update, body);
    }

    private Node returnStatement() {
        consume(TokenType.IDENTIFIER); // Consumir 'return'
        Expression value = expression(); // Valor a retornar
        consume(TokenType.SEMICOLON); // Consumir ';'
        return new ReturnStatement(value);
    }

    private BlockStatement blockStatement() {
        List<Node> statements = new ArrayList<>();
        consume(TokenType.OPERATOR); // Consumir '{'
        while (currentTokenIndex < tokens.size()) {
            if (currentToken().getType() == TokenType.OPERATOR && currentToken().getValue().equals("}")) {
                break; // Salir del bucle si encontramos el cierre del bloque
            }
            statements.add(statement());
        }
        consume(TokenType.OPERATOR); // Consumir '}'
        return new BlockStatement(statements);
    }

    private Expression expression() {
        Expression left = primaryExpression(); // Obtener el primer operando

        // Si el siguiente token es un operador de comparación, construir una expresión binaria
        if (currentToken().getType() == TokenType.COMPARISON) {
            String operator = currentToken().getValue(); // Obtener operador (==, <, >, etc.)
            consume(TokenType.COMPARISON); // Consumir el operador

            Expression right = primaryExpression(); // Obtener el segundo operando
            return new BinaryExpression(left, operator, right); // Crear la expresión binaria
        }

        return left; // Si no hay operador de comparación, devolver la expresión simple
    }

    private Expression primaryExpression() {
        Token token = currentToken();

        Expression expr;

        if (token.getType() == TokenType.IDENTIFIER || token.getValue().equals("this")) {
            String identifier = token.getValue();
            consume(token.getType()); // Consumir 'IDENTIFIER' o 'this'
            expr = new VariableReference(identifier);

            // Soportar acceso a campos con '.'
            while (currentToken().getType() == TokenType.OPERATOR && currentToken().getValue().equals(".")) {
                consume(TokenType.OPERATOR); // Consumir '.'

                String fieldName = currentToken().getValue();
                consume(TokenType.IDENTIFIER); // Consumir el identificador
                expr = new FieldAccess(expr, fieldName);
            }

            // Soportar asignaciones
            if (currentToken().getType() == TokenType.OPERATOR && currentToken().getValue().equals("=")) {
                consume(TokenType.OPERATOR); // Consumir '='
                Expression value = expression(); // Obtener la expresión asignada
                return new AssignmentExpression(expr, value);
            }

            return expr;
        } else if (token.getType() == TokenType.NUMBER) {
            consume(TokenType.NUMBER);
            return new NumberLiteral(Integer.parseInt(token.getValue()));
        } else if (token.getType() == TokenType.STRING) {
            consume(TokenType.STRING);
            return new StringLiteral(token.getValue());
        } else {
            throw new RuntimeException("Unexpected token in expression: " + token);
        }
    }


}