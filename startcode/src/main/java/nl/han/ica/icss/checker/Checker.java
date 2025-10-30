package nl.han.ica.icss.checker;

import nl.han.ica.icss.ast.*;
import nl.han.ica.icss.ast.literals.*;
import nl.han.ica.icss.ast.operations.AddOperation;
import nl.han.ica.icss.ast.operations.MultiplyOperation;
import nl.han.ica.icss.ast.operations.SubtractOperation;
import nl.han.ica.icss.ast.types.ExpressionType;

import java.util.HashMap;
import java.util.LinkedList;


public class Checker {

    private LinkedList<HashMap<String, ExpressionType>> variableTypes;

    public void check(AST ast) {
        variableTypes = new LinkedList<>();
        HashMap<String, ExpressionType> scope = new HashMap<>();
        variableTypes.addFirst(scope);
        checkStylesheet(ast.root);
        variableTypes.removeFirst();
    }

    private ExpressionType getExpressionType(Expression expression) {
        if (expression instanceof PixelLiteral) {
            return ExpressionType.PIXEL;
        } else if (expression instanceof PercentageLiteral) {
            return ExpressionType.PERCENTAGE;
        } else if (expression instanceof ColorLiteral) {
            return ExpressionType.COLOR;
        } else if (expression instanceof BoolLiteral) {
            return ExpressionType.BOOL;
        } else if (expression instanceof ScalarLiteral) {
            return ExpressionType.SCALAR;
        } else if (expression instanceof Operation) {
            return checkExpression((Operation) expression);
        } else if (expression instanceof VariableReference) {
            return getVariableReferenceExpressionType((VariableReference) expression);
        } else {
            return ExpressionType.UNDEFINED;
        }
    }

    private void checkStylesheet(Stylesheet node) {
        for (ASTNode child : node.getChildren()) {
            if (child instanceof VariableAssignment) {
                checkVariableAssignment((VariableAssignment) child);
                variableTypes.getFirst().put(((VariableAssignment) child).name.name, getExpressionType(((VariableAssignment) child).expression));
            }
            if (child instanceof Stylerule) {
                checkStyleRule((Stylerule) child);
            }
        }
    }

    private void checkVariableAssignment(VariableAssignment child) {
        ExpressionType expressionType = getExpressionType(child.expression);
        if(expressionType == ExpressionType.SCALAR){
            child.setError("Variable assignment got assigned with invalid type: " + expressionType);
        }
    }

    private void checkStyleRule(Stylerule node) {
        HashMap<String, ExpressionType> styleRuleScope = new HashMap<>();
        variableTypes.addFirst(styleRuleScope);
        for (ASTNode child : node.getChildren()) {
            checkBody(child, styleRuleScope);
        }
        variableTypes.removeFirst();
    }


    private void checkBody(ASTNode node, HashMap<String, ExpressionType> scope) {
        if (node instanceof VariableAssignment) {
            checkVariableAssignment((VariableAssignment) node);
            scope.put(((VariableAssignment) node).name.name, getExpressionType(((VariableAssignment) node).expression));
        }

        if (node instanceof Declaration) {
            checkDeclaration((Declaration) node);
        }

    }

    private void checkDeclaration(Declaration node) {
        ExpressionType nodeType = getExpressionType(node.expression);
        if (node.property.name.equals("width") || node.property.name.equals("height")) {
            if (!(nodeType == ExpressionType.PERCENTAGE) && !(nodeType == ExpressionType.PIXEL)) {
                if(nodeType == ExpressionType.UNDEFINED){
                    node.setError("Property name '" + node.property.name + "' got assigned an invalid OPERATION or VARIABLE REFERENCE");
                } else {
                    node.expression.setError("Property name '" + node.property.name + "' got assigned an invalid type: " + nodeType);
                }
            }
        }

        if (node.property.name.equals("color") || node.property.name.equals("background-color")) {
            if (!( nodeType == ExpressionType.COLOR)) {
                if(nodeType == ExpressionType.UNDEFINED){
                    node.setError("Property name '" + node.property.name + "' got assigned an invalid OPERATION or VARIABLE REFERENCE");
                } else {
                    node.expression.setError("Property name: '" + node.property.name + "' got assigned an invalid type: " + nodeType);
                }
            }
        }
    }

    private ExpressionType checkExpression(Operation handSide) {
        ExpressionType type = ExpressionType.UNDEFINED;

        if (handSide instanceof AddOperation) {
            type = checkAddOperation((AddOperation) handSide);
        } else if (handSide instanceof SubtractOperation) {
            type = checkSubtractOperation((SubtractOperation) handSide);
        } else if (handSide instanceof MultiplyOperation) {
            type = checkMultiplyOperation((MultiplyOperation) handSide);
        }
        return type;
    }


    private ExpressionType checkMultiplyOperation(MultiplyOperation handSide) {

        ExpressionType lhs = getLhsExpressionType(handSide);
        ExpressionType rhs = getRhsExpressionType(handSide);

        if (((lhs == ExpressionType.PIXEL || lhs == ExpressionType.PERCENTAGE) && rhs == ExpressionType.SCALAR) || ((rhs == ExpressionType.PIXEL || rhs == ExpressionType.PERCENTAGE) && lhs == ExpressionType.SCALAR) || (lhs == ExpressionType.SCALAR && rhs == ExpressionType.SCALAR)) {
            if (rhs == ExpressionType.SCALAR) {
                return lhs;
            } else {
                return rhs;
            }
        }
        handSide.setError("Multiply operation got assigned with invalid types: " + lhs + " and " + rhs);
        return ExpressionType.UNDEFINED;
    }

    private ExpressionType getRhsExpressionType(Operation handSide) {
        ExpressionType rhs;
        if (handSide.rhs instanceof VariableReference) {
            rhs = getVariableReferenceExpressionType((VariableReference) handSide.rhs);
        } else {
            rhs = (handSide.rhs instanceof Operation) ? checkExpression((Operation) handSide.rhs) : getExpressionType(handSide.rhs);
        }
        return rhs;
    }

    private ExpressionType getLhsExpressionType(Operation handSide) {
        ExpressionType lhs;
        if (handSide.lhs instanceof VariableReference) {
            lhs = getVariableReferenceExpressionType((VariableReference) handSide.lhs);
        } else {
            lhs = (handSide.lhs instanceof Operation) ? checkExpression((Operation) handSide.lhs) : getExpressionType(handSide.lhs);
        }
        return lhs;
    }

    private ExpressionType checkSubtractOperation(SubtractOperation handSide) {
        ExpressionType lhs = getLhsExpressionType(handSide);
        ExpressionType rhs = getRhsExpressionType(handSide);

        if ((lhs == ExpressionType.PIXEL && rhs == ExpressionType.PIXEL) || (lhs == ExpressionType.PERCENTAGE && rhs == ExpressionType.PERCENTAGE) || (lhs == ExpressionType.SCALAR && rhs == ExpressionType.SCALAR)) {
            return lhs;
        }
        handSide.setError("Subtract operation got assigned with invalid types: " + lhs + " and " + rhs);
        return ExpressionType.UNDEFINED;
    }

    private ExpressionType checkAddOperation(AddOperation handSide) {
        ExpressionType lhs = getLhsExpressionType(handSide);
        ExpressionType rhs = getRhsExpressionType(handSide);

        if ((lhs == ExpressionType.PIXEL && rhs == ExpressionType.PIXEL) || (lhs == ExpressionType.PERCENTAGE && rhs == ExpressionType.PERCENTAGE) || (lhs == ExpressionType.SCALAR && rhs == ExpressionType.SCALAR)) {
            return lhs;
        }
        handSide.setError("Add operation got assigned with invalid types: " + lhs + " and " + rhs);
        return ExpressionType.UNDEFINED;
    }

    private ExpressionType getVariableReferenceExpressionType(VariableReference node) {
        for (int i = 0; i < variableTypes.size(); i++) {
            HashMap<String, ExpressionType> scope = variableTypes.get(i);

            if (scope.containsKey(node.name)) {
                return scope.get(node.name);
            }
        }

        node.setError("Variable reference: '" + node.name + "' is not defined in accessible scope");
        return ExpressionType.UNDEFINED;
    }
}