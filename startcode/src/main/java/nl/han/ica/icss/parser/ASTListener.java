package nl.han.ica.icss.parser;

import nl.han.ica.datastructures.HANStackImpl;
import nl.han.ica.datastructures.IHANStack;
import nl.han.ica.icss.ast.*;
import nl.han.ica.icss.ast.literals.*;
import nl.han.ica.icss.ast.operations.*;
import nl.han.ica.icss.ast.selectors.*;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.ArrayList;
import java.util.EmptyStackException;

public class ASTListener extends ICSSBaseListener {

    private AST ast;

    private IHANStack<ASTNode> statementStack;  // VariableAssignment, Declaration, Stylerule
    private IHANStack<ASTNode> expressionStack; // Expressions & literals

    public ASTListener() {
        ast = new AST();
        statementStack = new HANStackImpl<>();
        expressionStack = new HANStackImpl<>();
        statementStack.push(ast.root); // root van AST
    }

    public AST getAST() {
        return ast;
    }

    // ---------- StyleRule ----------
    @Override
    public void enterStyleRule(ICSSParser.StyleRuleContext ctx) {
        Stylerule rule = new Stylerule();
        statementStack.peek().addChild(rule);
        statementStack.push(rule);
    }

    @Override
    public void exitStyleRule(ICSSParser.StyleRuleContext ctx) {
        statementStack.pop();
    }

    // ---------- Selector ----------
    @Override
    public void enterSelector(ICSSParser.SelectorContext ctx) {
        ASTNode selector;
        if (ctx.ID_IDENT() != null) {
            selector = new IdSelector(ctx.ID_IDENT().getText());
        } else if (ctx.CLASS_IDENT() != null) {
            selector = new ClassSelector(ctx.CLASS_IDENT().getText());
        } else {
            selector = new TagSelector(ctx.LOWER_IDENT().getText());
        }
        statementStack.peek().addChild(selector);
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
        statementStack.peek().addChild(decl);
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
        statementStack.peek().addChild(varAssign);
    }

    // ---------- Expressions ----------
    @Override
    public void exitPrimaryValue(ICSSParser.PrimaryValueContext ctx) {
        String text = ctx.getText();
        ASTNode node;

        if (text.startsWith("#") && text.length() == 7) {
            node = new ColorLiteral(text);
        } else if (text.endsWith("px")) {
            node = new PixelLiteral(text);
        } else if (text.endsWith("%")) {
            node = new PercentageLiteral(text);
        } else if (text.matches("[0-9]+")) {
            node = new ScalarLiteral(text);
        } else if (text.equals("TRUE") || text.equals("FALSE")) {
            node = new BoolLiteral(text);
        } else {
            node = new VariableReference(text);
        }

        expressionStack.push(node);
    }

    @Override
    public void exitExpression(ICSSParser.ExpressionContext ctx) {
        // check of ctx PLUS/MIN bestaat
        if (ctx.PLUS() != null || ctx.MIN() != null) {
            ASTNode right = expressionStack.pop();
            ASTNode left = expressionStack.pop();
            Expression op = (ctx.PLUS() != null) ? new AddOperation() : new SubtractOperation();
            op.addChild(left);
            op.addChild(right);
            expressionStack.push(op);
        }
        // anders expression -> term, niets doen
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
        // anders term -> primaryValue, niets doen
    }

    @Override
    public void enterIfStatement(ICSSParser.IfStatementContext ctx) {
        IfClause ifClause = new IfClause();
        statementStack.push(ifClause);
    }

    @Override
    public void exitIfStatement(ICSSParser.IfStatementContext ctx) {
        IfClause ifClause = (IfClause) statementStack.pop(); // top is IfClause

        // Condition
        Expression condition = (Expression) expressionStack.pop();
        ifClause.addChild(condition);

        // THEN-body
        ArrayList<ASTNode> thenBody = new ArrayList<>();
        try {
            while (true) {
                ASTNode node = statementStack.pop();
                if (node instanceof VariableAssignment || node instanceof Declaration ||
                        node instanceof Stylerule || node instanceof IfClause) {
                    thenBody.add(0, node);
                } else {
                    statementStack.push(node);
                    break;
                }
            }
        } catch (Exception ignored) { }

        for (ASTNode node : thenBody) ifClause.addChild(node);

        // ELSE-body
        if (ctx.ELSE() != null) {
            ElseClause elseClause = new ElseClause();
            ArrayList<ASTNode> elseBody = new ArrayList<>();
            try {
                while (true) {
                    ASTNode node = statementStack.pop();
                    if (node instanceof VariableAssignment || node instanceof Declaration ||
                            node instanceof Stylerule || node instanceof IfClause) {
                        elseBody.add(0, node);
                    } else {
                        statementStack.push(node);
                        break;
                    }
                }
            } catch (Exception ignored) { }

            for (ASTNode node : elseBody) elseClause.addChild(node);
            ifClause.addChild(elseClause);
        }

        // Voeg IfClause toe aan parent
        try {
            statementStack.peek().addChild(ifClause);
        } catch (Exception e) {
            ast.root.addChild(ifClause);
        }
    }


    // ---------- Optional overrides ----------
    @Override public void enterEveryRule(ParserRuleContext ctx) {}
    @Override public void exitEveryRule(ParserRuleContext ctx) {}
    @Override public void visitTerminal(TerminalNode node) {}
    @Override public void visitErrorNode(ErrorNode node) {}
}
