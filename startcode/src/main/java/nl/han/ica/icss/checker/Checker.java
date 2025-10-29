package nl.han.ica.icss.checker;

import nl.han.ica.icss.ast.*;
import nl.han.ica.icss.ast.literals.BoolLiteral;
import nl.han.ica.icss.ast.literals.ColorLiteral;
import nl.han.ica.icss.ast.literals.PixelLiteral;
import nl.han.ica.icss.ast.literals.ScalarLiteral;
import nl.han.ica.icss.ast.types.ExpressionType;

import java.util.HashMap;
import java.util.LinkedList;


public class Checker {

    private LinkedList<HashMap<String, ExpressionType>> variableTypes;

    public void check(AST ast) {
        if (ast == null || ast.root == null) return;

        variableTypes = new LinkedList<>();
        variableTypes.push(new HashMap<>());
        checkNode(ast.root);
        variableTypes.pop();
    }

    private void checkNode(ASTNode node) {
        if (node == null) return;

        // Determine if this node introduces a new scope (rules, if-blocks, else-blocks)
        boolean isNewScope = (node instanceof Stylerule) || (node instanceof IfClause) || (node instanceof ElseClause);
        if (isNewScope) variableTypes.push(new HashMap<>());

        // 1️⃣ Handle variable assignments
        if (node instanceof VariableAssignment) {
            VariableAssignment assign = (VariableAssignment) node;
            String varName = assign.name.name;
            ExpressionType type = getExpressionType(assign.expression);

            if (type == null) {
                assign.setError("Variable '" + varName + "' has invalid type");
            } else {
                variableTypes.peek().put(varName, type);
            }
        }

        // 2️⃣ Validate variable references
        if (node instanceof VariableReference) {
            VariableReference ref = (VariableReference) node;
            if (!isVariableDefined(ref.name)) {
                ref.setError("Variable '" + ref.name + "' used but not defined in this scope");
            }
        }

        // 3️⃣ Disallow colors in operations
        if (node instanceof Operation) {
            Operation op = (Operation) node;
            ExpressionType leftType = getExpressionType(op.lhs);
            ExpressionType rightType = getExpressionType(op.rhs);

            if (leftType == ExpressionType.COLOR || rightType == ExpressionType.COLOR) {
                op.setError("Cannot use colors in operations (+, -, *)");
            }
        }

        // 4️⃣ Property type validation
        if (node instanceof Declaration) {
            Declaration decl = (Declaration) node;

            if (decl.property != null && decl.expression != null) {
                ExpressionType valType = getExpressionType(decl.expression);
                ExpressionType expectedType = getExpectedType(decl.property.name);

                if (expectedType != null && valType != expectedType) {
                    decl.setError("Property '" + decl.property.name + "' cannot have value of type " + valType);
                }
            }
        }

        // 5️⃣ Recurse into children
        for (ASTNode child : node.getChildren()) {
            checkNode(child);
        }

        // 6️⃣ Pop scope if it was a new scope
        if (isNewScope) variableTypes.pop();
    }


    private ExpressionType getExpressionType(Expression expr) {
        if(expr instanceof ColorLiteral) return ExpressionType.COLOR;
        if(expr instanceof PixelLiteral) return ExpressionType.PIXEL;
        if(expr instanceof ScalarLiteral) return ExpressionType.SCALAR;
        if(expr instanceof BoolLiteral) return ExpressionType.BOOL;

        if(expr instanceof VariableReference) {
            return lookupVariableType(((VariableReference) expr).name);
        }

        if(expr instanceof Operation) {
            Operation op = (Operation) expr;
            ExpressionType leftType = getExpressionType(op.lhs);
            ExpressionType rightType = getExpressionType(op.rhs);

            if(leftType == null || rightType == null) return null; // unresolved yet
            if(leftType != rightType) return null; // type mismatch
            return leftType; // result type is the type of operands
        }

        return null;
    }


    private ExpressionType lookupVariableType(String name) {
        for (HashMap<String, ExpressionType> scope : variableTypes) {
            if (scope.containsKey(name)) return scope.get(name);
        }
        return null;
    }

    private boolean isVariableDefined(String name) {
        return lookupVariableType(name) != null;
    }

    private ExpressionType getExpectedType(String property) {
        switch (property) {
            case "width":
            case "height":
            case "padding":
            case "margin":
                return ExpressionType.PIXEL;
            case "color":
            case "background-color":
            case "border-color":
                return ExpressionType.COLOR;
            default:
                return null;
        }
    }
}