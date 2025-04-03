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
        String returnType = currentToken().getValue(); // Tipo de retorno
        consume(TokenType.DATA_TYPE); // Consumir tipo de retorno

        String methodName = currentToken().getValue(); // Nombre del método
        consume(TokenType.IDENTIFIER); // Consumir nombre del método

        consume(TokenType.OPERATOR); // Consumir '('
        List<VariableDeclaration> parameters = new ArrayList<>();

        // Manejar parámetros (opcional)
        while (currentToken().getType() != TokenType.OPERATOR || !currentToken().getValue().equals(")")) {
            String paramType = currentToken().getValue(); // Tipo del parámetro
            consume(TokenType.DATA_TYPE); // Consumir tipo del parámetro

            String paramName = currentToken().getValue(); // Nombre del parámetro
            consume(TokenType.IDENTIFIER); // Consumir nombre del parámetro

            parameters.add(new VariableDeclaration(paramType, paramName, null)); // Agregar el parámetro
            if (currentToken().getValue().equals(",")) {
                consume(TokenType.OPERATOR); // Consumir ',' si hay más parámetros
            }
        }

        consume(TokenType.OPERATOR); // Consumir ')'
        BlockStatement body = blockStatement(); // Obtener el cuerpo del método

        return new MethodDeclaration(returnType, methodName, parameters, body); // Crear y devolver la declaración del método
    }

    private ClassDeclaration classDeclaration() {
        consume(TokenType.RESERVED); // Consumir 'class'
        String className = currentToken().getValue(); // Nombre de la clase
        consume(TokenType.IDENTIFIER); // Consumir el nombre de la clase

        // Aquí manejas la apertura de llaves y las declaraciones de métodos dentro de la clase.
        List<MethodDeclaration> methods = new ArrayList<>();
        consume(TokenType.OPERATOR); // Consumir '{'

        while (currentToken().getType() != TokenType.OPERATOR || !currentToken().getValue().equals("}")) {
            if (currentToken().getType() == TokenType.DATA_TYPE) {
                methods.add(methodDeclaration()); // Agregar métodos
            } else {
                throw new RuntimeException("Unexpected token in class body: " + currentToken());
            }
        }

        consume(TokenType.OPERATOR); // Consumir '}'
        return new ClassDeclaration(className, methods); // Crear y devolver la clase
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

        if (token.getType() == TokenType.IDENTIFIER) {
            String identifier = token.getValue();
            consume(TokenType.IDENTIFIER);

            // Si hay un '=', es una asignación
            if (currentToken().getType() == TokenType.OPERATOR && currentToken().getValue().equals("=")) {
                consume(TokenType.OPERATOR); // Consumir '='
                Expression value = expression(); // Obtener la expresión asignada
                return new Assignment(identifier, value);
            }

            return new VariableReference(identifier);
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