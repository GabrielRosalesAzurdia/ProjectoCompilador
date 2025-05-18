package parser;

import ast.*;
import lexer.*;
import symboltable.*;

import java.util.*;

public class Parser {
    private final List<Token> tokens;
    private int current = 0;
    List<Symbol> parameters = new ArrayList<>();

    private SymbolTable symbolTable;
    private final List<String> semanticErrors = new ArrayList<>();

    public Parser(List<Token> tokens, SymbolTable initialTable) {
        this.tokens = tokens;
        this.symbolTable = initialTable;
    }

    public Node parse() {
        List<Node> statements = new ArrayList<>();
        while (!isAtEnd()) {
            Node stmt = parseStatement();
            if (stmt != null) {
                statements.add(stmt);
            }
        }
        return new BlockStatement(statements);
    }

    private Node parseStatement() {
        Token token = peek();
        if (token.getType() == TokenType.DATA_TYPE || (token.getType() == TokenType.RESERVED && token.getValue().equals("void"))) {
            // Lookahead para ver si es una función
            if (tokens.size() > current + 2 && tokens.get(current + 2).getValue().equals("(")) {
                return parseMethodDeclaration(); // ahora se permite globalmente
            } else {
                return parseDeclaration();
            }
        }
        else if (token.getType() == TokenType.DATA_TYPE) {
            return parseDeclaration();
        } else if (token.getType() == TokenType.IDENTIFIER) {
            return parseAssignmentOrCall();
        } else if (token.getType() == TokenType.RESERVED && token.getValue().equals("if")) {
            return parseIf();
        } else if (token.getType() == TokenType.RESERVED && token.getValue().equals("while")) {
            return parseWhile();
        } else if (token.getType() == TokenType.RESERVED && token.getValue().equals("for")) {
            return parseFor();
        } else if (token.getType() == TokenType.RESERVED && token.getValue().equals("return")) {
            return parseReturn();
        } else if (token.getType() == TokenType.RESERVED && token.getValue().equals("break")) {
            advance();
            match(TokenType.OPERATOR, ";");
            return new BreakStatement();
        } else if (token.getType() == TokenType.RESERVED && token.getValue().equals("continue")) {
            advance();
            match(TokenType.OPERATOR, ";");
            return new ContinueStatement();
        } else if (token.getType() == TokenType.RESERVED && token.getValue().equals("class")) {
            return parseClass();
        } else if (token.getType() == TokenType.OPERATOR && token.getValue().equals("{")) {
            enterScope();
            advance();
            return null;
        } else if (token.getType() == TokenType.OPERATOR && token.getValue().equals("}")) {
            exitScope();
            advance();
            return null;
        } else {
            advance();
            return null;
        }
    }

    private Node parseWhile() {
        advance(); // consume 'while'
        if (!match(TokenType.OPERATOR, "(")) {
            throw new RuntimeException("Error: Se esperaba '(' después de 'while'.");
        }
        Expression condition = parseExpression();
        if (!match(TokenType.OPERATOR, ")")) {
            throw new RuntimeException("Error: Se esperaba ')' después de la condición.");
        }
        BlockStatement body = (BlockStatement) parseBlock();
        return new WhileStatement(condition, body);
    }

    private Node parseFor() {
        advance(); // consume 'for'
        if (!match(TokenType.OPERATOR, "(")) {
            throw new RuntimeException("Error: Se esperaba '(' después de 'for'.");
        }

        VariableDeclaration initializer = null;
        if (!peek().getValue().equals(";")) {
            initializer = (VariableDeclaration) parseDeclaration();
        } else {
            match(TokenType.OPERATOR, ";");
        }

        Expression condition = null;
        if (!peek().getValue().equals(";")) {
            condition = parseExpression();
        }
        match(TokenType.OPERATOR, ";");

        Expression update = null;
        if (!peek().getValue().equals(")")) {
            update = parseExpression();
        }
        match(TokenType.OPERATOR, ")");

        BlockStatement body = (BlockStatement) parseBlock();

        return new ForStatement(initializer, condition, update, body);
    }

    private Node parseReturn() {
        advance(); // consume 'return'
        Expression value = null;
        if (!peek().getValue().equals(";")) {
            value = parseExpression();
        }
        match(TokenType.OPERATOR, ";");
        return new ReturnStatement(value);
    }

    private Node parseDeclaration() {
        String type = advance().getValue(); // tipo de dato
        Token identifier = advance(); // nombre de variable

        if (identifier.getType() == TokenType.IDENTIFIER) {
            if (symbolTable.containsInCurrentScope(identifier.getValue())) {
                semanticErrors.add("Error: Redefinición de variable '" + identifier.getValue() + "'");
            } else {
                symbolTable.insert(identifier.getValue(), new Symbol(identifier.getValue(), type, "variable"));
            }
        }

        Expression initializer = null;

        if (match(TokenType.OPERATOR, "=")) {
            initializer = parseExpression();
            String valueType = evaluateExpressionType(initializer);
            if (!type.equals(valueType)) {
                semanticErrors.add("Error: No se puede asignar un valor de tipo '" + valueType + "' a una variable de tipo '" + type + "'");
            }
        }

        // Consumir hasta el punto y coma
        while (!isAtEnd() && !peek().getValue().equals(";")) {
            advance();
        }
        if (!isAtEnd()) advance(); // consumir ;

        return new VariableDeclaration(type, identifier.getValue(), initializer);
    }

    private Node parseAssignmentOrCall() {
        Token identifier = advance();
        Symbol symbol = symbolTable.lookup(identifier.getValue());

        if (symbol == null) {
            semanticErrors.add("Error: Símbolo '" + identifier.getValue() + "' no declarado");
        }

        if (match(TokenType.OPERATOR, "(")) {
            // Llamada a función
            List<Expression> args = new ArrayList<>();
            if (!peek().getValue().equals(")")) {
                do {
                    args.add(parseExpression());
                } while (match(TokenType.OPERATOR, ","));
            }
            match(TokenType.OPERATOR, ")");
            match(TokenType.OPERATOR, ";");
            return new ExpressionStatement(new FunctionCall(identifier.getValue(), args));
        } else {
            // Solo referencia o asignación (puedes extender esto si soportas asignaciones reales)
            while (!isAtEnd() && !peek().getValue().equals(";")) {
                advance();
            }
            if (!isAtEnd()) advance(); // consumir ;
            return new ExpressionStatement(new VariableReference(identifier.getValue()));
        }
    }


    private Node parseIf() {
        advance(); // consume 'if'
        if (!match(TokenType.OPERATOR, "(")) {
            throw new RuntimeException("Error: Se esperaba '(' después de 'if'.");
        }

        Node condition = parseExpression();

        if (!match(TokenType.OPERATOR, ")")) {
            throw new RuntimeException("Error: Se esperaba ')' después de la condición.");
        }

        Node thenBranch = parseBlock();
        Node elseBranch = null;

        if (match(TokenType.RESERVED, "else")) {
            elseBranch = parseBlock();
        }

        return new IfStatement(condition, thenBranch, elseBranch);
    }

    private Node parseBlock() {
        if (match(TokenType.OPERATOR, "{")) {
            List<Node> statements = new ArrayList<>();
            enterScope();
            while (!isAtEnd() && !peek().getValue().equals("}")) {
                Node stmt = parseStatement();
                if (stmt != null) statements.add(stmt);
            }
            match(TokenType.OPERATOR, "}");
            exitScope();
            return new BlockStatement(statements);
        }
        throw new RuntimeException("Error: Se esperaba un bloque de código.");
    }

    private Node parseClass() {
        advance(); // consume "class"
        Token className = advance();

        symbolTable.insert(className.getValue(), new Symbol(className.getValue(), "class", "global"));

        if (!match(TokenType.OPERATOR, "{")) {
            throw new RuntimeException("Error: Se esperaba '{' después del nombre de la clase.");
        }

        enterScope();

        List<VariableDeclaration> fields = new ArrayList<>();
        List<MethodDeclaration> methods = new ArrayList<>();

        while (!isAtEnd() && !peek().getValue().equals("}")) {
            Token next = peek();
            if (next.getType() == TokenType.DATA_TYPE) {
                Token lookahead = tokens.get(current + 2);
                if (lookahead.getValue().equals("(")) {
                    methods.add(parseMethodDeclaration());
                } else {
                    VariableDeclaration field = (VariableDeclaration) parseDeclaration();
                    fields.add(field);
                }
            } else {
                advance();
            }
        }

        match(TokenType.OPERATOR, "}");
        exitScope();

        return new ClassDeclaration(className.getValue(), fields, methods);
    }

    private MethodDeclaration parseMethodDeclaration() {
        String returnType = advance().getValue();
        String methodName = advance().getValue();

        symbolTable.insert(methodName, new Symbol(methodName, returnType, "method"));

        if (!match(TokenType.OPERATOR, "(")) {
            throw new RuntimeException("Error: Se esperaba '(' en la declaración del método.");
        }

        List<VariableDeclaration> parameters = new ArrayList<>();
        enterScope();

        while (!peek().getValue().equals(")")) {
            String paramType = advance().getValue();
            String paramName = advance().getValue();

            parameters.add(new VariableDeclaration(paramType, paramName, null));
            symbolTable.insert(paramName, new Symbol(paramName, paramType, "parameter"));

            if (!peek().getValue().equals(")")) {
                match(TokenType.OPERATOR, ",");
            }
        }

        match(TokenType.OPERATOR, ")");

        BlockStatement body = (BlockStatement) parseBlock();
        exitScope();

        return new MethodDeclaration(returnType, methodName, parameters, body);
    }

    // -----------------------
    // EXPRESIONES (con paréntesis y operadores)
    // -----------------------

    private Expression parseExpression() {
        return parseEquality();
    }

    private Expression parseEquality() {
        Expression expr = parseRelational();

        while (match(TokenType.OPERATOR, "==") || match(TokenType.OPERATOR, "!=")) {
            String operator = previous().getValue();
            Expression right = parseRelational();
            expr = new BinaryExpression(expr, operator, right);
        }

        return expr;
    }

    private Expression parseRelational() {
        Expression expr = parseAdditionSubtraction();

        while (match(TokenType.OPERATOR, "<") || match(TokenType.OPERATOR, ">") ||
                match(TokenType.OPERATOR, "<=") || match(TokenType.OPERATOR, ">=")) {
            String operator = previous().getValue();
            Expression right = parseAdditionSubtraction();
            expr = new BinaryExpression(expr, operator, right);
        }

        return expr;
    }

    private Expression parseAdditionSubtraction() {
        Expression expr = parseMultiplicationDivision();

        while (match(TokenType.OPERATOR, "+") || match(TokenType.OPERATOR, "-")) {
            String operator = previous().getValue();
            Expression right = parseMultiplicationDivision();
            expr = new BinaryExpression(expr, operator, right);
        }

        return expr;
    }

    private Expression parseMultiplicationDivision() {
        Expression expr = parseUnary();
        while (match(TokenType.OPERATOR, "*") || match(TokenType.OPERATOR, "/")) {
            String operator = previous().getValue();
            Expression right = parseUnary();
            expr = new BinaryExpression(expr, operator, right);
        }
        return expr;
    }

    private Expression parseUnary() {
        if (match(TokenType.OPERATOR, "-") || match(TokenType.OPERATOR, "!")) {
            String operator = previous().getValue();
            Expression right = parseUnary();
            return new UnaryExpression(operator, right);
        }

        return parsePostfix();
    }

    private Expression parsePostfix() {
        Expression expr = parsePrimary();

        while (match(TokenType.OPERATOR, "++") || match(TokenType.OPERATOR, "--")) {
            String operator = previous().getValue();
            expr = new PostfixExpression(expr, operator);  // debes crear esta clase
        }

        return expr;
    }

    private Expression parsePrimary() {
        Token token = peek();

        if (match(TokenType.OPERATOR, "(")) {
            Expression expr = parseExpression();
            if (!match(TokenType.OPERATOR, ")")) {
                throw new RuntimeException("Error: Se esperaba ')' en expresión.");
            }
            return expr;
        }

        if (token.getType() == TokenType.NUMBER) {
            advance();
            return new NumberLiteral(Integer.parseInt(token.getValue()));
        } else if (token.getType() == TokenType.STRING) {
            advance();
            return new StringLiteral(token.getValue());
        } else if (token.getType() == TokenType.IDENTIFIER) {
            advance();
            return new VariableReference(token.getValue());
        }

        throw new RuntimeException("Token inesperado en expresión: " + token);
    }

    // -----------------------
    // UTILIDADES
    // -----------------------

    private void enterScope() {
        SymbolTable newScope = new SymbolTable(symbolTable);
        symbolTable.addChild(newScope);
        symbolTable = newScope;
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

    private Token previous() {
        return tokens.get(current - 1);
    }

    private boolean match(TokenType type, String value) {
        if (!isAtEnd() && peek().getType() == type && peek().getValue().equals(value)) {
            advance();
            return true;
        }
        return false;
    }

    private String evaluateExpressionType(Expression expr) {
        if (expr instanceof NumberLiteral) {
            return "int";
        } else if (expr instanceof StringLiteral) {
            return "String";
        } else if (expr instanceof VariableReference) {
            VariableReference ref = (VariableReference) expr;
            Symbol symbol = symbolTable.lookup(ref.getName());
            if (symbol != null) return symbol.getType();
            else {
                semanticErrors.add("Error: Variable '" + ref.getName() + "' usada sin declarar.");
                return "unknown";
            }
        } else if (expr instanceof BinaryExpression) {
            BinaryExpression bin = (BinaryExpression) expr;
            String leftType = evaluateExpressionType(bin.getLeft());
            String rightType = evaluateExpressionType(bin.getRight());
            if (!leftType.equals(rightType)) {
                semanticErrors.add("Error: Tipos incompatibles en operación binaria: " + leftType + " y " + rightType);
            }
            return leftType;
        }
        return "unknown";
    }

    public List<String> getSemanticErrors() {
        return semanticErrors;
    }

    public SymbolTable getGlobalSymbolTable() {
        SymbolTable root = symbolTable;
        while (root.getParent() != null) {
            root = root.getParent();
        }
        return root;
    }
}
