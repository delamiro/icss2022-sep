package nl.han.ica.icss.parser;

import nl.han.ica.datastructures.HANStackImpl;
import nl.han.ica.datastructures.IHANStack;
import nl.han.ica.icss.ast.*;
import nl.han.ica.icss.ast.literals.*;
import nl.han.ica.icss.ast.selectors.*;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.TerminalNode;

/**
 * ASTListener die ICSS AST opbouwt voor Level0.
 */
public class ASTListener extends ICSSBaseListener {

    private AST ast;
    private IHANStack<ASTNode> stack;

    public ASTListener() {
        ast = new AST();
        stack = new HANStackImpl<>();
        stack.push(ast.root);
    }

    public AST getAST() {
        return ast;
    }

    // --- Style rules ---
    @Override
    public void enterStyleRule(ICSSParser.StyleRuleContext ctx) {
        Stylerule rule = new Stylerule();
        stack.peek().addChild(rule);
        stack.push(rule);
    }

    @Override
    public void exitStyleRule(ICSSParser.StyleRuleContext ctx) {
        stack.pop();
    }

    // --- Selectors ---
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

        stack.peek().addChild(selector);
    }

    // --- Declarations ---
    @Override
    public void enterDeclaration(ICSSParser.DeclarationContext ctx) {
        Declaration declaration = new Declaration(ctx.LOWER_IDENT().getText());
        stack.peek().addChild(declaration);

        ASTNode valueNode = getValueNode(ctx.value());
        if (valueNode != null) {
            declaration.addChild(valueNode);
        }
    }

    // --- Variable assignments ---
    @Override
    public void enterVariableAssignment(ICSSParser.VariableAssignmentContext ctx) {
        VariableAssignment varAssign = new VariableAssignment();

        // Variable name
        VariableReference varName = new VariableReference(ctx.CAPITAL_IDENT().getText());
        varAssign.addChild(varName);

        // Variable value
        ASTNode valueNode = getValueNode(ctx.value());
        if (valueNode != null) {
            varAssign.addChild(valueNode);
        }

        // Attach the assignment to the AST
        stack.peek().addChild(varAssign);
    }

    // --- Helper method to convert ValueContext into ASTNode ---
    private ASTNode getValueNode(ICSSParser.ValueContext ctx) {
        if (ctx.COLOR() != null) {
            return new ColorLiteral(ctx.COLOR().getText());
        } else if (ctx.PIXELSIZE() != null) {
            return new PixelLiteral(ctx.PIXELSIZE().getText());
        } else if (ctx.PERCENTAGE() != null) {
            return new PercentageLiteral(ctx.PERCENTAGE().getText());
        } else if (ctx.SCALAR() != null) {
            return new ScalarLiteral(ctx.SCALAR().getText());
        } else if (ctx.TRUE() != null || ctx.FALSE() != null) {
            return new BoolLiteral(ctx.getText());
        } else if (ctx.LOWER_IDENT() != null) {
            return new VariableReference(ctx.LOWER_IDENT().getText());
        } else if (ctx.CAPITAL_IDENT() != null) {
            return new VariableReference(ctx.CAPITAL_IDENT().getText());
        }
        return null;
    }

    // Optional overrides (no changes needed)
    @Override
    public void enterEveryRule(ParserRuleContext ctx) { }
    @Override
    public void exitEveryRule(ParserRuleContext ctx) { }
    @Override
    public void visitTerminal(TerminalNode node) { }
    @Override
    public void visitErrorNode(ErrorNode node) { }

}
