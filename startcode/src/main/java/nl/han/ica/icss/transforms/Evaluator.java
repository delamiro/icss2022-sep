package nl.han.ica.icss.transforms;

import nl.han.ica.icss.ast.*;
import nl.han.ica.icss.ast.literals.*;
import nl.han.ica.icss.ast.operations.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

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
        evaluateNode(ast.root);
        variableValues.pop();
    }

    private void evaluateNode(ASTNode node) {
        if (node == null) return;

        // 1️⃣ Variable assignment
        if (node instanceof VariableAssignment) {
            VariableAssignment assign = (VariableAssignment) node;
            Literal value = evaluateExpression(assign.expression);
            assign.expression = value;
            variableValues.peek().put(assign.name.name, value);
        }

        // 2️⃣ IfClause: evaluate condition and recursively evaluate the chosen body
        if (node instanceof IfClause) {
            IfClause ifNode = (IfClause) node;
            Literal condLit = evaluateExpression(ifNode.conditionalExpression);

            ArrayList<ASTNode> bodyToApply = new ArrayList<>();

            if (condLit instanceof BoolLiteral) {
                if (((BoolLiteral) condLit).value) {
                    bodyToApply.addAll(ifNode.body);
                } else if (ifNode.elseClause != null) {
                    bodyToApply.addAll(ifNode.elseClause.body);
                }
            }

            // Evaluate all statements in the chosen body immediately
            for (ASTNode stmt : bodyToApply) {
                evaluateNode(stmt);
            }

            return; // Skip further processing for this IfClause
        }

        // 3️⃣ Declaration: evaluate its expression
        if (node instanceof Declaration) {
            Declaration decl = (Declaration) node;
            if (decl.expression != null) {
                Literal lit = evaluateExpression(decl.expression);
                decl.expression = lit;
            }
        }

        // 4️⃣ Recursively evaluate children
        ArrayList<ASTNode> childrenCopy = new ArrayList<>(node.getChildren());
        for (ASTNode child : childrenCopy) {
            evaluateNode(child);
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

            return null; // type mismatch
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
