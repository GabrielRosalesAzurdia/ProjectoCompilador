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
import java.util.List;
import java.util.Map;

public class CompilerGUI extends JFrame {

    private JTextArea lexerOutput;
    private JTextArea symbolTableOutput;
    private JTextArea parserOutput;
    private JTextArea semanticOutput;

    public CompilerGUI() {
        setTitle("Compilador Java");
        setSize(800, 600);
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

        JButton loadButton = new JButton("Cargar Archivo");
        loadButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loadFile();
            }
        });

        add(loadButton);

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

                // Analiza léxico
                Lexer lexer = new Lexer(code);
                List<Token> tokens = lexer.tokenize();

                // Mostrar los tokens
                StringBuilder lexerOutputBuilder = new StringBuilder();
                for (Token token : tokens) {
                    lexerOutputBuilder.append(token).append("\n");
                }
                lexerOutput.setText(lexerOutputBuilder.toString());

                // Mostrar la tabla de símbolos
                SymbolTable symbolTable = lexer.getSymbolTable();
                StringBuilder symbolTableBuilder = new StringBuilder();
                for (Symbol symbol : symbolTable.getAllSymbols().values()) {
                    symbolTableBuilder.append(symbol.getName())
                            .append(": Type = ").append(symbol.getType())
                            .append(", Scope = ").append(symbol.getScope()).append("\n");
                }
                symbolTableOutput.setText(symbolTableBuilder.toString());

                // Analiza sintáctico
                Parser parser = new Parser(lexer); // Crear el parser
                Node astRoot = parser.parse(); // Analizar el código
                parserOutput.setText(astRoot.toString()); // Mostrar el AST como resultado

                // Aquí puedes integrar el analizador semántico si es necesario.
                semanticOutput.setText("Resultados del analizador semántico aquí...");
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Error al cargar el archivo: " + ex.getMessage());
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error en el análisis: " + ex.getMessage());
            }
        }
    }
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new CompilerGUI());
    }
}