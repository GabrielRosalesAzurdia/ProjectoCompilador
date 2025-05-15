import ast.Node;
import lexer.Lexer;
import lexer.Token;
import parser.Parser;
import symboltable.Symbol;
import symboltable.SymbolTable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CompilerGUI extends JFrame {

    private JTextArea lexerOutput;
    private JTextArea symbolTableOutput;
    private JTextArea parserOutput;
    private JTextArea semanticOutput;

    public CompilerGUI() {
        setTitle("Compilador Java");
        setSize(1000, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new GridLayout(1, 4));

        lexerOutput = new JTextArea();
        symbolTableOutput = new JTextArea();
        parserOutput = new JTextArea();
        semanticOutput = new JTextArea();

        add(createPanel("Analizador Léxico", lexerOutput));
        add(createPanel("Tabla de Símbolos", symbolTableOutput));
        add(createPanel("Analizador Sintáctico", parserOutput));
        add(createPanel("Analizador Semántico", semanticOutput));

        JMenuBar menuBar = new JMenuBar();
        JButton loadButton = new JButton("Cargar Archivo");
        loadButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loadFile();
            }
        });
        menuBar.add(loadButton);
        setJMenuBar(menuBar);

        setVisible(true);
    }

    private JPanel createPanel(String title, JTextArea textArea) {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder(title));
        textArea.setEditable(false);
        panel.add(new JScrollPane(textArea), BorderLayout.CENTER);
        return panel;
    }

    private void loadFile() {
        JFileChooser fileChooser = new JFileChooser();
        int returnValue = fileChooser.showOpenDialog(this);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            try (BufferedReader reader = new BufferedReader(new FileReader(fileChooser.getSelectedFile()))) {
                StringBuilder content = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    content.append(line).append("\n");
                }
                String code = content.toString();

                // Análisis léxico
                Lexer lexer = new Lexer(code);
                List<Token> tokens = lexer.tokenize();

                // Mostrar tokens
                StringBuilder lexerOutputBuilder = new StringBuilder();
                for (Token token : tokens) {
                    lexerOutputBuilder.append(token).append("\n");
                }
                lexerOutput.setText(lexerOutputBuilder.toString());

                // Obtener la tabla de símbolos
                SymbolTable symbolTable = lexer.getSymbolTable();

                // Análisis sintáctico y semántico
                Parser parser = new Parser(tokens, symbolTable);
                Node astRoot = parser.parse();

                if (astRoot != null) {
                    parserOutput.setText(astRoot.toString());
                } else {
                    parserOutput.setText("No se pudo generar el AST.");
                }

                SymbolTable root = parser.getGlobalSymbolTable();
                StringBuilder symbolTableBuilder = new StringBuilder();

                Set<String> seen = new HashSet<>();

                for (SymbolTable table : root.getAllTables()) {
                    for (Symbol symbol : table.getAllSymbols().values()) {
                        String entry = symbol.getName() + ": Type = " + symbol.getType() + ", Scope = " + symbol.getScope();
                        if (!seen.contains(entry)) {
                            seen.add(entry);
                            symbolTableBuilder.append(entry).append("\n");
                        }
                    }
                }

                symbolTableOutput.setText(symbolTableBuilder.toString());

                // Mostrar errores semánticos
                List<String> semanticErrors = parser.getSemanticErrors();
                if (semanticErrors.isEmpty()) {
                    semanticOutput.setText("No se encontraron errores semánticos.");
                } else {
                    StringBuilder semanticText = new StringBuilder();
                    for (String error : semanticErrors) {
                        semanticText.append(error).append("\n");
                    }
                    semanticOutput.setText(semanticText.toString());
                }

            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Error al cargar el archivo: " + ex.getMessage());
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error en el análisis: " + ex.getMessage());
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(CompilerGUI::new);
    }
}
