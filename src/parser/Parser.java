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

        if (token.getType() == TokenType.DATA_TYPE) {
            return parseDeclaration();
        } else if (token.getType() == TokenType.IDENTIFIER) {
            return parseAssignmentOrCall();
        } else if (token.getType() == TokenType.RESERVED && token.getValue().equals("if")) {
            return parseIf();
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

    private Node parseDeclaration() {
        String type = advance().getValue();
        Token identifier = advance();

        if (identifier.getType() == TokenType.IDENTIFIER) {
            if (symbolTable.containsInCurrentScope(identifier.getValue())) {
                semanticErrors.add("Error: Redefinición de variable '" + identifier.getValue() + "'");
                return null;
            } else {
                symbolTable.insert(identifier.getValue(), new Symbol(identifier.getValue(), type, "local"));
            }
        }

        while (!isAtEnd() && !peek().getValue().equals(";")) {
            advance();
        }
        if (!isAtEnd()) advance();

        return new VariableDeclaration(type, identifier.getValue(), null);
    }

    private Node parseAssignmentOrCall() {
        Token identifier = advance();
        Symbol symbol = symbolTable.lookup(identifier.getValue());

        if (symbol == null) {
            semanticErrors.add("Error: Variable '" + identifier.getValue() + "' no declarada");
        }

        while (!isAtEnd() && !peek().getValue().equals(";")) {
            advance();
        }
        if (!isAtEnd()) advance();

        return new ExpressionStatement(new VariableReference(identifier.getValue()));
    }

    private Expression primaryExpression() {
        Token token = peek();

        if (token.getType() == TokenType.IDENTIFIER) {
            advance();
            return new VariableReference(token.getValue());
        } else if (token.getType() == TokenType.NUMBER) {
            advance();
            return new NumberLiteral(Integer.parseInt(token.getValue()));
        } else if (token.getType() == TokenType.STRING) {
            advance();
            return new StringLiteral(token.getValue());
        } else {
            throw new RuntimeException("Unexpected token in expression: " + token);
        }
    }

    private Node parseIf() {
        advance();

        if (match(TokenType.OPERATOR, "(")) {
            Node condition = parseCondition();

            if (!match(TokenType.OPERATOR, ")")) {
                throw new RuntimeException("Error: Se esperaba ')' después de la condición en 'if'.");
            }

            Node thenBranch = parseBlock();

            Node elseBranch = null;
            if (match(TokenType.RESERVED, "else")) {
                elseBranch = parseBlock();
            }

            return new IfStatement(condition, thenBranch, elseBranch);
        }

        throw new RuntimeException("Error: Se esperaba un '(' después de 'if'.");
    }

    private Node parseCondition() {
        return expression();
    }

    private Node expression() {
        Expression left = primaryExpression();

        while (true) {
            if (match(TokenType.OPERATOR, "<") || match(TokenType.OPERATOR, ">") ||
                    match(TokenType.OPERATOR, "==") || match(TokenType.OPERATOR, "<=") ||
                    match(TokenType.OPERATOR, ">=")) {

                String operator = previous().getValue();
                Expression right = primaryExpression();
                left = new BinaryExpression(left, operator, right);
            } else {
                break;
            }
        }

        return left;
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
        advance();
        Token className = advance();

        if (!match(TokenType.OPERATOR, "{")) {
            throw new RuntimeException("Error: Se esperaba '{' después del nombre de la clase.");
        }

        List<VariableDeclaration> fields = new ArrayList<>();
        List<MethodDeclaration> methods = new ArrayList<>();

        while (!isAtEnd() && !peek().getValue().equals("}")) {
            Token next = peek();
            if (next.getType() == TokenType.DATA_TYPE) {
                Token lookahead = tokens.get(current + 2);
                if (lookahead.getValue().equals("(")) {
                    methods.add(parseMethodDeclaration());
                } else {
                    fields.add((VariableDeclaration) parseDeclaration());
                }
            } else {
                advance();
            }
        }

        match(TokenType.OPERATOR, "}");

        return new ClassDeclaration(className.getValue(), fields, methods);
    }

    private MethodDeclaration parseMethodDeclaration() {
        String returnType = advance().getValue();
        String methodName = advance().getValue();

        if (!match(TokenType.OPERATOR, "(")) {
            throw new RuntimeException("Error: Se esperaba '(' en la declaración del método.");
        }

        List<VariableDeclaration> parameters = new ArrayList<>();

        while (!peek().getValue().equals(")")) {
            String paramType = advance().getValue();
            String paramName = advance().getValue();
            parameters.add(new VariableDeclaration(paramType, paramName, null));

            if (!peek().getValue().equals(")")) {
                match(TokenType.OPERATOR, ",");
            }
        }

        match(TokenType.OPERATOR, ")");

        BlockStatement body = (BlockStatement) parseBlock();

        return new MethodDeclaration(returnType, methodName, parameters, body);
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

    public List<String> getSemanticErrors() {
        return semanticErrors;
    }

    public SymbolTable getSymbolTable() {
        return symbolTable;
    }
}
