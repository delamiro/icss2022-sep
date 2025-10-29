package nl.han.ica.icss.transforms;

import nl.han.ica.icss.ast.*;
import nl.han.ica.icss.ast.literals.*;
import nl.han.ica.icss.ast.operations.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.ListIterator;

public class Evaluator implements Transform {

    private LinkedList<HashMap<String, Literal>> variableValues;

    public Evaluator() {
        variableValues = new LinkedList<>();
    }

    @Override
    public void apply(AST ast) {
        if (ast == null || ast.root == null) return;

        variableValues.clear();
        variableValues.push(new HashMap<>());

        // Start evaluation from the root
        evaluateChildren(ast.root.getChildren());

        variableValues.pop();
    }

    private void evaluateChildren(ArrayList<ASTNode> children) {
        ListIterator<ASTNode> iter = children.listIterator();
        while (iter.hasNext()) {
            ASTNode node = iter.next();

            if (node instanceof VariableAssignment) {
                VariableAssignment assign = (VariableAssignment) node;
                Literal value = evaluateExpression(assign.expression);
                assign.expression = value;
                variableValues.peek().put(assign.name.name, value);
            }
            else if (node instanceof IfClause) {
                IfClause ifNode = (IfClause) node;
                Literal condLit = evaluateExpression(ifNode.conditionalExpression);
                ArrayList<ASTNode> replacement = new ArrayList<>();

                if (condLit instanceof BoolLiteral) {
                    if (((BoolLiteral) condLit).value) {
                        replacement.addAll(ifNode.body);
                    } else if (ifNode.elseClause != null) {
                        replacement.addAll(ifNode.elseClause.body);
                    }
                }

                // Remove the IfClause
                iter.remove();

                // Add all chosen nodes individually
                for (ASTNode n : replacement) {
                    iter.add(n);
                }

                // Evaluate newly inserted nodes
                evaluateChildren(replacement);
            }

            else if (node instanceof Declaration) {
                Declaration decl = (Declaration) node;
                if (decl.expression != null) {
                    Literal lit = evaluateExpression(decl.expression);
                    decl.expression = lit;
                }
            }
            else {
                // Recurse on other node types
                evaluateChildren(node.getChildren());
            }
        }
    }

    private Literal evaluateExpression(Expression expr) {
        if (expr instanceof Literal) return (Literal) expr;

        if (expr instanceof VariableReference) {
            return lookupVariable(((VariableReference) expr).name);
        }

        if (expr instanceof Operation) {
            Operation op = (Operation) expr;
            Literal left = evaluateExpression(op.lhs);
            Literal right = evaluateExpression(op.rhs);

            if (left == null || right == null) return null;

            // Pixel operations
            if (left instanceof PixelLiteral && right instanceof PixelLiteral) {
                int lv = ((PixelLiteral) left).value;
                int rv = ((PixelLiteral) right).value;
                if (op instanceof AddOperation) return new PixelLiteral(lv + rv);
                if (op instanceof SubtractOperation) return new PixelLiteral(lv - rv);
                if (op instanceof MultiplyOperation) return new PixelLiteral(lv * rv);
            }

            // Scalar operations
            if (left instanceof ScalarLiteral && right instanceof ScalarLiteral) {
                int lv = ((ScalarLiteral) left).value;
                int rv = ((ScalarLiteral) right).value;
                if (op instanceof AddOperation) return new ScalarLiteral(lv + rv);
                if (op instanceof SubtractOperation) return new ScalarLiteral(lv - rv);
                if (op instanceof MultiplyOperation) return new ScalarLiteral(lv * rv);
            }

            // Boolean operations
            if (left instanceof BoolLiteral && right instanceof BoolLiteral) {
                boolean lv = ((BoolLiteral) left).value;
                boolean rv = ((BoolLiteral) right).value;
                if (op instanceof AddOperation) return new BoolLiteral(lv || rv);
                if (op instanceof MultiplyOperation) return new BoolLiteral(lv && rv);
                if (op instanceof SubtractOperation) return new BoolLiteral(lv != rv);
            }

            return null;
        }

        return null;
    }

    private Literal lookupVariable(String name) {
        for (HashMap<String, Literal> scope : variableValues) {
            if (scope.containsKey(name)) return scope.get(name);
        }
        return null;
    }
}
