package nl.han.ica.icss.generator;

import nl.han.ica.icss.ast.*;
import nl.han.ica.icss.ast.literals.*;
import nl.han.ica.icss.ast.operations.AddOperation;
import nl.han.ica.icss.ast.operations.MultiplyOperation;
import nl.han.ica.icss.ast.operations.SubtractOperation;
import nl.han.ica.icss.ast.selectors.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Generator {

    public String generate(AST ast) {
        if (ast == null || ast.root == null) return "";
        StringBuilder sb = new StringBuilder();
        for (ASTNode child : ast.root.getChildren()) {
            generateNode(child, sb, 0);
        }
        return sb.toString();
    }

    private void generateNode(ASTNode node, StringBuilder sb, int indentLevel) {
        String indent = "    ".repeat(indentLevel);

        if (node instanceof Stylerule) {
            Stylerule rule = (Stylerule) node;

            // Collect selector names
            List<String> selectorNames = new ArrayList<>();
            for (ASTNode selNode : rule.getChildren()) {
                if (selNode instanceof Selector) {
                    selectorNames.add(getSelectorName((Selector) selNode));
                }
            }

            // Only generate rule if it has selectors
            if (!selectorNames.isEmpty()) {
                sb.append(String.join(", ", selectorNames));
                sb.append(" {\n");

                // Declarations
                for (ASTNode child : rule.getChildren()) {
                    if (child instanceof Declaration) {
                        generateNode(child, sb, indentLevel + 1);
                    }
                }

                sb.append("}\n\n");
            }
        } else if (node instanceof Declaration) {
            Declaration decl = (Declaration) node;
            sb.append(indent)
                    .append(decl.property.name)
                    .append(": ")
                    .append(generateExpression(decl.expression))
                    .append(";\n");
        }
        // You can extend here if you want nested rules or other AST nodes
    }

    private String generateExpression(Expression expr) {
        if (expr instanceof PixelLiteral) {
            return ((PixelLiteral) expr).value + "px";
        } else if (expr instanceof ColorLiteral) {
            return ((ColorLiteral) expr).value;
        } else if (expr instanceof ScalarLiteral) {
            return Integer.toString(((ScalarLiteral) expr).value);
        } else if (expr instanceof BoolLiteral) {
            return ((BoolLiteral) expr).value ? "TRUE" : "FALSE";
        } else if (expr instanceof VariableReference) {
            return ((VariableReference) expr).name;
        } else if (expr instanceof AddOperation) {
            return generateExpression(((AddOperation) expr).lhs) + " + " +
                    generateExpression(((AddOperation) expr).rhs);
        } else if (expr instanceof SubtractOperation) {
            return generateExpression(((SubtractOperation) expr).lhs) + " - " +
                    generateExpression(((SubtractOperation) expr).rhs);
        } else if (expr instanceof MultiplyOperation) {
            return generateExpression(((MultiplyOperation) expr).lhs) + " * " +
                    generateExpression(((MultiplyOperation) expr).rhs);
        }
        return "";
    }

    private String getSelectorName(Selector s) {
        if (s instanceof IdSelector) return "#" + ((IdSelector) s).id;
        if (s instanceof ClassSelector) return "." + ((ClassSelector) s).cls;
        if (s instanceof TagSelector) return ((TagSelector) s).tag;
        return "";
    }
}
