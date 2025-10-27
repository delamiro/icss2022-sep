package nl.han.ica.icss.parser;

import nl.han.ica.datastructures.HANStackImpl;
import nl.han.ica.datastructures.IHANStack;
import nl.han.ica.icss.ast.*;
import nl.han.ica.icss.ast.literals.*;
import nl.han.ica.icss.ast.operations.*;
import nl.han.ica.icss.ast.selectors.*;

import java.util.ArrayList;

public class ASTListener extends ICSSBaseListener {

    private AST ast;

    private IHANStack<ASTNode> statementStack;        // parent nodes
    private IHANStack<ASTNode> expressionStack;       // literals and expressions
    private IHANStack<ArrayList<ASTNode>> bodyStack;  // collects statements inside blocks

    public ASTListener() {
        ast = new AST();
        statementStack = new HANStackImpl<>();
        expressionStack = new HANStackImpl<>();
        bodyStack = new HANStackImpl<>();
        statementStack.push(ast.root);
    }

    public AST getAST() {
        return ast;
    }

    // ---------- StyleRule ----------
    @Override
    public void enterStyleRule(ICSSParser.StyleRuleContext ctx) {
        Stylerule rule = new Stylerule();
        if (!statementStack.isEmpty()) statementStack.peek().addChild(rule);
        statementStack.push(rule);
        bodyStack.push(new ArrayList<>());
    }

    @Override
    public void exitStyleRule(ICSSParser.StyleRuleContext ctx) {
        Stylerule rule = (Stylerule) statementStack.pop();
        if (!bodyStack.isEmpty()) {
            for (ASTNode stmt : bodyStack.pop()) rule.addChild(stmt);
        }
    }

    // ---------- Selector ----------
    @Override
    public void enterSelector(ICSSParser.SelectorContext ctx) {
        ASTNode selector;
        if (ctx.ID_IDENT() != null) selector = new IdSelector(ctx.ID_IDENT().getText());
        else if (ctx.CLASS_IDENT() != null) selector = new ClassSelector(ctx.CLASS_IDENT().getText());
        else selector = new TagSelector(ctx.LOWER_IDENT().getText());

        if (!statementStack.isEmpty()) statementStack.peek().addChild(selector);
    }

    // ---------- Declaration ----------
    @Override
    public void enterDeclaration(ICSSParser.DeclarationContext ctx) {
        Declaration decl = new Declaration(ctx.LOWER_IDENT().getText());
        statementStack.push(decl);
    }

    @Override
    public void exitDeclaration(ICSSParser.DeclarationContext ctx) {
        ASTNode valueNode = expressionStack.pop();
        Declaration decl = (Declaration) statementStack.pop();
        decl.addChild(valueNode);

        if (!bodyStack.isEmpty()) bodyStack.peek().add(decl);
        else if (!statementStack.isEmpty()) statementStack.peek().addChild(decl);
    }

    // ---------- VariableAssignment ----------
    @Override
    public void enterVariableAssignment(ICSSParser.VariableAssignmentContext ctx) {
        VariableAssignment varAssign = new VariableAssignment();
        varAssign.addChild(new VariableReference(ctx.CAPITAL_IDENT().getText()));
        statementStack.push(varAssign);
    }

    @Override
    public void exitVariableAssignment(ICSSParser.VariableAssignmentContext ctx) {
        ASTNode valueNode = expressionStack.pop();
        VariableAssignment varAssign = (VariableAssignment) statementStack.pop();
        varAssign.addChild(valueNode);

        if (!bodyStack.isEmpty()) bodyStack.peek().add(varAssign);
        else if (!statementStack.isEmpty()) statementStack.peek().addChild(varAssign);
    }

    // ---------- Expressions ----------
    @Override
    public void exitPrimaryValue(ICSSParser.PrimaryValueContext ctx) {
        String text = ctx.getText();
        ASTNode node;
        if (text.startsWith("#") && text.length() == 7) node = new ColorLiteral(text);
        else if (text.endsWith("px")) node = new PixelLiteral(text);
        else if (text.endsWith("%")) node = new PercentageLiteral(text);
        else if (text.matches("[0-9]+")) node = new ScalarLiteral(text);
        else if (text.equals("TRUE") || text.equals("FALSE")) node = new BoolLiteral(text);
        else node = new VariableReference(text);

        expressionStack.push(node);
    }

    @Override
    public void exitExpression(ICSSParser.ExpressionContext ctx) {
        if (ctx.PLUS() != null || ctx.MIN() != null) {
            ASTNode right = expressionStack.pop();
            ASTNode left = expressionStack.pop();
            Expression op = (ctx.PLUS() != null) ? new AddOperation() : new SubtractOperation();
            op.addChild(left);
            op.addChild(right);
            expressionStack.push(op);
        }
    }

    @Override
    public void exitTerm(ICSSParser.TermContext ctx) {
        if (ctx.MUL() != null) {
            ASTNode right = expressionStack.pop();
            ASTNode left = expressionStack.pop();
            MultiplyOperation mul = new MultiplyOperation();
            mul.addChild(left);
            mul.addChild(right);
            expressionStack.push(mul);
        }
    }

    // ---------- IfStatement ----------
    @Override
    public void enterIfStatement(ICSSParser.IfStatementContext ctx) {
        IfClause ifNode = new IfClause();
        statementStack.push(ifNode);
        bodyStack.push(new ArrayList<>()); // collect IF body
    }

    @Override
    public void exitIfStatement(ICSSParser.IfStatementContext ctx) {
        IfClause ifNode = (IfClause) statementStack.pop();
        ifNode.body = bodyStack.pop();
        ifNode.conditionalExpression = new VariableReference(ctx.expression().getText());

        if (!bodyStack.isEmpty()) bodyStack.peek().add(ifNode);
        else if (!statementStack.isEmpty()) statementStack.peek().addChild(ifNode);
    }

    // ---------- ElseBlock ----------
    @Override
    public void enterElseBlock(ICSSParser.ElseBlockContext ctx) {
        bodyStack.push(new ArrayList<>()); // collect ELSE body
    }

    @Override
    public void exitElseBlock(ICSSParser.ElseBlockContext ctx) {
        ElseClause elseNode = new ElseClause();
        elseNode.body = bodyStack.pop();

        // Attach ELSE to its parent IF
        IfClause ifNode = (IfClause) statementStack.peek();
        ifNode.elseClause = elseNode;
    }
}
