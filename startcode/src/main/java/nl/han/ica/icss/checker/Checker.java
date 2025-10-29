package nl.han.ica.icss.checker;

import nl.han.ica.datastructures.IHANLinkedList;
import nl.han.ica.icss.ast.*;
import nl.han.ica.icss.ast.literals.ColorLiteral;
import nl.han.ica.icss.ast.operations.AddOperation;
import nl.han.ica.icss.ast.operations.MultiplyOperation;
import nl.han.ica.icss.ast.operations.SubtractOperation;
import nl.han.ica.icss.ast.types.ExpressionType;

import java.util.HashMap;
import java.util.LinkedList;


public class Checker {

    // Stack van scopes: elke scope is een map van variabelen en hun types
    private LinkedList<HashMap<String, ExpressionType>> variableTypes;

    public void check(AST ast) {
        if(ast == null || ast.root == null) return;

        // Begin met een lege scope
        variableTypes = new LinkedList<>();
        variableTypes.push(new HashMap<>());

        // Start met recursief checken vanaf de root
        checkNode(ast.root);

        // Scope aan het einde opruimen (optioneel hier, omdat we klaar zijn)
        variableTypes.pop();
    }

    private void checkNode(ASTNode node) {
        if(node == null) return;

        // 1️⃣ Undefined variable check
        if(node instanceof VariableReference) {
            VariableReference refNode = (VariableReference) node;
            String varName = refNode.name;
            if(!isVariableDefined(varName)) {
                refNode.setError("Variable '" + varName + "' used but not defined");
            }
        }

        // 2️⃣ Check kleuren in operaties (alle subklassen van Operation)
        if(node instanceof Operation) {
            Operation op = (Operation) node;
            checkColorInOperation(op);
        }

        // 3️⃣ Recursief check alle children
        for(ASTNode child : node.getChildren()) {
            checkNode(child);
        }
    }

    // Hulpmethode om kleuren in een operatie te controleren
    private void checkColorInOperation(Operation op) {
        Expression left = op.lhs;
        Expression right = op.rhs;

        if(left instanceof ColorLiteral || right instanceof ColorLiteral) {
            op.setError("Cannot use colors in operations (+, -, *)");
        }
    }



    private boolean isVariableDefined(String name) {
        // Check alle scopes van laatste naar eerste
        for(HashMap<String, ExpressionType> scope : variableTypes) {
            if(scope.containsKey(name)) return true;
        }
        return false;
    }
}
