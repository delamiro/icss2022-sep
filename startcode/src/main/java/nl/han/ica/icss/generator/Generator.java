package nl.han.ica.icss.generator;

import nl.han.ica.icss.ast.*;
import nl.han.ica.icss.ast.literals.*;
import nl.han.ica.icss.ast.selectors.*;

import java.util.ArrayList;

public class Generator {

    public String generate(AST ast) {
        if (ast == null || ast.root == null) return "";
        StringBuilder sb = new StringBuilder();

        for (ASTNode node : ast.root.getChildren()) {
            generateNode(node, sb, 0);
        }

        return sb.toString();
    }

    private void generateNode(ASTNode node, StringBuilder sb, int indentLevel) {
        String indent = "  ".repeat(indentLevel); // 2 spaces per level

        if (node instanceof Stylerule) {
            Stylerule rule = (Stylerule) node;

            // Collect selector names
            ArrayList<String> selectorNames = new ArrayList<>();
            for (Selector s : rule.selectors) {
                selectorNames.add(getSelectorName(s));
            }

            // Open block
            sb.append(indent).append(String.join(", ", selectorNames)).append(" {\n");

            // Declarations and nested rules
            for (ASTNode child : rule.getChildren()) {
                if (child instanceof Declaration) {
                    Declaration decl = (Declaration) child;
                    sb.append(indent).append("  ") // extra indent for declarations
                            .append(decl.property.name)
                            .append(": ")
                            .append(getLiteralValue(decl.expression))
                            .append(";\n");
                } else if (child instanceof Stylerule) {
                    generateNode(child, sb, indentLevel + 1);
                }
            }

            // Close block
            sb.append(indent).append("}\n\n");
        }
    }

    private String getLiteralValue(Expression expr) {
        if (expr instanceof PixelLiteral) return ((PixelLiteral) expr).value + "px";
        if (expr instanceof ColorLiteral) return ((ColorLiteral) expr).value;
        if (expr instanceof ScalarLiteral) return String.valueOf(((ScalarLiteral) expr).value);
        if (expr instanceof BoolLiteral) return String.valueOf(((BoolLiteral) expr).value).toUpperCase();
        return "";
    }

    private String getSelectorName(Selector s) {
        if (s instanceof IdSelector) return ((IdSelector) s).id;
        if (s instanceof ClassSelector) return ((ClassSelector) s).cls;
        if (s instanceof TagSelector) return ((TagSelector) s).tag;
        return "";
    }
}
