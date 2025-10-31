package nl.han.ica.icss.transforms;

import nl.han.ica.icss.ast.*;
import nl.han.ica.icss.ast.literals.*;
import nl.han.ica.icss.ast.operations.AddOperation;
import nl.han.ica.icss.ast.operations.MultiplyOperation;
import nl.han.ica.icss.ast.operations.SubtractOperation;

import java.util.HashMap;
import java.util.LinkedList;

public class Evaluator implements Transform {

    private LinkedList<HashMap<String, Literal>> variableValues;

    public Evaluator() {
        variableValues = new LinkedList<>();
    }

    @Override
    public void apply(AST ast) {
        variableValues.clear();
        HashMap<String, Literal> variables = new HashMap<>();
        variableValues.addFirst(variables);
        applyStyleSheet(ast.root);
        variableValues.removeFirst();
    }

    private void applyStyleSheet(Stylesheet node) {
        for (ASTNode child:node.getChildren()) {
            if (child instanceof VariableAssignment){
                applyAssignment((VariableAssignment) child);
                variableValues.getFirst().put(((VariableAssignment) child).name.name, (Literal) ((VariableAssignment) child).expression);
            }
            if (child instanceof Stylerule) {
                applyStyleRule((Stylerule) child);
            }
        }
    }

    private void applyStyleRule(Stylerule node) {
        HashMap<String, Literal> styleRuleScope = new HashMap<>();
        variableValues.addFirst(styleRuleScope);
        for (ASTNode child : node.getChildren()) {
            applyBody(node, styleRuleScope, child);
        }
    }

    private void applyBody(Stylerule node, HashMap<String, Literal> styleRuleScope, ASTNode child) {
        if(child instanceof VariableAssignment){
            applyAssignment((VariableAssignment) child);
            styleRuleScope.put(((VariableAssignment) child).name.name, (Literal) ((VariableAssignment) child).expression);
        }
        if (child instanceof Declaration) {
            applyDeclaration((Declaration) child);
        }
        if (child instanceof IfClause){
            applyIfClause((IfClause) child, node);
        }
    }

    private void applyIfClause(IfClause child, Stylerule stylerule) {
        if(applyConditionalExpression(child.conditionalExpression)){
            applyIfClauseBody(child, stylerule);
        } else {
            if(child.elseClause != null){
                applyElseClause(child.elseClause, stylerule, child);
            }
        }
    }

    private void applyElseClause(ElseClause elseClause, Stylerule stylerule, IfClause ifClause) {
        HashMap<String, Literal> elseClauseScope = new HashMap<>();
        variableValues.addFirst(elseClauseScope);

        for (ASTNode node: elseClause.body) {
            applyBody(stylerule, elseClauseScope, node);
            stylerule.body.add(stylerule.body.indexOf(ifClause),node);
        }
        stylerule.body.remove(ifClause);
        variableValues.removeFirst();
    }

    private void applyAssignment(VariableAssignment node) {
        if(node.expression instanceof VariableReference){
            node.expression = getValueOfVariableReference((VariableReference) node.expression);
        } else {
            node.expression = evalExpression(node.expression);
        }
    }

    private void applyIfClauseBody(IfClause child, Stylerule stylerule) {
        HashMap<String, Literal> ifClauseScope = new HashMap<>();
        variableValues.addFirst(ifClauseScope);

        for (ASTNode node: child.body) {
            if(node instanceof VariableAssignment){
                applyAssignment((VariableAssignment) node);
                ifClauseScope.put(((VariableAssignment) node).name.name, (Literal) ((VariableAssignment) node).expression);
            }
            if (node instanceof ElseClause){
                variableValues.removeFirst();
                return;
            }
            stylerule.body.add(stylerule.body.indexOf(child),node);
            if(node instanceof Declaration){
                applyDeclaration((Declaration) node);
            }
            if(node instanceof IfClause) {
                applyIfClause((IfClause) node, stylerule);
            }
        }
        variableValues.removeFirst();
        stylerule.body.remove(child);
    }

    private boolean applyConditionalExpression(Expression child) {
        if(child instanceof VariableReference){
            return  ((BoolLiteral)getValueOfVariableReference((VariableReference) child)).value;
        }
        if(child instanceof BoolLiteral){
            return  ((BoolLiteral) child).value;
        }
        throw new IllegalArgumentException("Conditional expression not found");
    }

    private Literal getValueOfVariableReference(VariableReference child) {
        if(variableValues.size() >0){
            for(int i = 0; i < variableValues.size(); i++){
                if(variableValues.get(i).containsKey(child.name)){
                    return variableValues.get(i).get(child.name);
                }
            }
        }
        throw new IllegalArgumentException("Variable not defined");
    }

    private void applyDeclaration(Declaration node) {
        if(node.expression instanceof VariableReference){
            node.expression = getValueOfVariableReference((VariableReference) node.expression);
        } else {
            node.expression = evalExpression(node.expression);
        }
    }

    private Expression evalExpression(Expression expression) {
        if (expression instanceof Operation) {
            if (expression instanceof AddOperation) {
                expression = evalAdd((AddOperation) expression);
            }
            if (expression instanceof SubtractOperation) {
                expression = evalSubtract((SubtractOperation) expression);
            }
            if (expression instanceof MultiplyOperation) {
                expression = evalMul((MultiplyOperation) expression);
            }
        }
        return expression;
    }

    private Literal evalMul(MultiplyOperation node) {

        Literal lhs = evalLhsLiteral(node);
        Literal rhs = evalRhsLiteral(node);

        if (lhs instanceof ScalarLiteral && rhs instanceof ScalarLiteral) {
            return new ScalarLiteral(((ScalarLiteral) lhs).value * ((ScalarLiteral) rhs).value);
        }

        if (lhs instanceof PixelLiteral && rhs instanceof ScalarLiteral) {
            return new PixelLiteral(((PixelLiteral) lhs).value * ((ScalarLiteral) rhs).value);
        }

        if (lhs instanceof ScalarLiteral && rhs instanceof PixelLiteral) {
            return new PixelLiteral(((ScalarLiteral) lhs).value * ((PixelLiteral) rhs).value);
        }

        if (lhs instanceof PercentageLiteral && rhs instanceof ScalarLiteral) {
            return new PercentageLiteral(((PercentageLiteral) lhs).value * ((ScalarLiteral) rhs).value);
        }

        if (lhs instanceof ScalarLiteral && rhs instanceof PercentageLiteral) {
            return new PercentageLiteral(((ScalarLiteral) lhs).value * ((PercentageLiteral) rhs).value);
        }

        return null;
    }


    private Literal evalSubtract(SubtractOperation node) {

        Literal lhs = evalLhsLiteral(node);
        Literal rhs = evalRhsLiteral(node);

        if (lhs instanceof ScalarLiteral && rhs instanceof ScalarLiteral) {
            return new ScalarLiteral(((ScalarLiteral) lhs).value - ((ScalarLiteral) rhs).value);
        }

        if (lhs instanceof PixelLiteral && rhs instanceof PixelLiteral) {
            return new PixelLiteral(((PixelLiteral) lhs).value - ((PixelLiteral) rhs).value);
        }

        if (lhs instanceof PercentageLiteral && rhs instanceof PercentageLiteral) {
            return new PercentageLiteral(((PercentageLiteral) lhs).value - ((PercentageLiteral) rhs).value);
        }

        return null;
    }


    private Literal evalAdd(AddOperation node) {

        Literal lhs = evalLhsLiteral(node);
        Literal rhs = evalRhsLiteral(node);

        if (lhs instanceof ScalarLiteral && rhs instanceof ScalarLiteral) {
            return new ScalarLiteral(((ScalarLiteral) lhs).value + ((ScalarLiteral) rhs).value);
        }

        if (lhs instanceof PixelLiteral && rhs instanceof PixelLiteral) {
            return new PixelLiteral(((PixelLiteral) lhs).value + ((PixelLiteral) rhs).value);
        }

        if (lhs instanceof PercentageLiteral && rhs instanceof PercentageLiteral) {
            return new PercentageLiteral(((PercentageLiteral) lhs).value + ((PercentageLiteral) rhs).value);
        }
        return null;
    }

    private Literal evalLhsLiteral(Operation node) {
        Literal lhs;
        if(node.lhs instanceof VariableReference){
            lhs = getValueOfVariableReference((VariableReference) node.lhs);
        } else {
            lhs = (node.lhs instanceof Operation) ? evalOperation((Operation) node.lhs) : (Literal) node.lhs;
        }
        return lhs;
    }

    private Literal evalRhsLiteral(Operation node) {
        Literal rhs;
        if(node.rhs instanceof VariableReference){
            rhs = getValueOfVariableReference((VariableReference) node.rhs);
        } else {
            rhs = (node.rhs instanceof Operation) ? evalOperation((Operation) node.rhs) : (Literal) node.rhs;
        }
        return rhs;
    }

    private Literal evalOperation(Operation node) {
        Literal result = null;

        if (node instanceof AddOperation) {
            result = evalAdd((AddOperation) node);
        } else if (node instanceof SubtractOperation) {
            result = evalSubtract((SubtractOperation) node);
        } else if (node instanceof MultiplyOperation) {
            result = evalMul((MultiplyOperation) node);
        }
        return result;
    }
}