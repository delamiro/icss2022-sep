package nl.han.ica.icss.generator;

import nl.han.ica.icss.ast.*;
import nl.han.ica.icss.ast.literals.*;
import nl.han.ica.icss.ast.selectors.*;

public class Generator {

    public String generate(AST ast) {
        return generateStylesheet(ast.root);
    }

    private String generateStylesheet(Stylesheet node) {
        String result = "";
        for (ASTNode child : node.getChildren()) {
            if (child instanceof Stylerule) {
                result += generateStyleRule((Stylerule) child);
                result += "\n";
            }
        }
        return result;
    }

    private String generateStyleRule(Stylerule stylerule) {
        String result = "";
        result += stylerule.selectors.get(0) + " {\n";
        for (ASTNode child : stylerule.body) {
            result += "  ";
            result += generateDeclaration((Declaration) child);
            result += ";";
            result += "\n";
        }
        result += "}";
        return result;
    }

    private String generateDeclaration(Declaration node) {
        String result = "";
        if (node.expression instanceof ColorLiteral) {
            ColorLiteral l = (ColorLiteral) node.expression;
            result += node.property.name + ": " + l.value;
        }

        if (node.expression instanceof PixelLiteral) {
            PixelLiteral l = (PixelLiteral) node.expression;
            result += node.property.name + ": " + l.value + "px";
        }

        if (node.expression instanceof PercentageLiteral) {
            PercentageLiteral l = (PercentageLiteral) node.expression;
            result += node.property.name + ": " + l.value + "%";
        }
        return result;
    }
}
