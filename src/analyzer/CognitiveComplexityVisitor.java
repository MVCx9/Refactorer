package analyzer;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SwitchCase;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.WhileStatement;

public class CognitiveComplexityVisitor extends ASTVisitor {
    private int complexity = 0;
    private int nestingLevel = 0;

    // Cada vez que empieza un método, resetea los valores
    @Override
    public boolean visit(MethodDeclaration node) {
        nestingLevel = 0;
        return super.visit(node);
    }
    
    // Para manejar los case en los switch statements
    private boolean firstCaseInSwitch = false;

    // --- Estructuras de control ---

    @Override
    public boolean visit(IfStatement node) {
        complexity += 1 + nestingLevel;
        nestingLevel++;
        return super.visit(node);
    }
    @Override
    public void endVisit(IfStatement node) { nestingLevel--; }

    @Override
    public boolean visit(SwitchStatement node) {
        complexity += 1 + nestingLevel;
        nestingLevel++;
        firstCaseInSwitch = true; // Resetea el flag para este switch
        return super.visit(node);
    }
    @Override
    public void endVisit(SwitchStatement node) { nestingLevel--; }

    @Override
    public boolean visit(SwitchCase node) {
        if (firstCaseInSwitch) {
            firstCaseInSwitch = false; // El primer case no suma complejidad
        } else {
            complexity++; // Cada case adicional suma 1
        }
        return super.visit(node);
    }

    @Override
    public boolean visit(ForStatement node) {
        complexity += 1 + nestingLevel;
        nestingLevel++;
        return super.visit(node);
    }
    @Override
    public void endVisit(ForStatement node) { nestingLevel--; }

    @Override
    public boolean visit(EnhancedForStatement node) {
        complexity += 1 + nestingLevel;
        nestingLevel++;
        return super.visit(node);
    }
    @Override
    public void endVisit(EnhancedForStatement node) { nestingLevel--; }

    @Override
    public boolean visit(WhileStatement node) {
        complexity += 1 + nestingLevel;
        nestingLevel++;
        return super.visit(node);
    }
    @Override
    public void endVisit(WhileStatement node) { nestingLevel--; }

    @Override
    public boolean visit(DoStatement node) {
        complexity += 1 + nestingLevel;
        nestingLevel++;
        return super.visit(node);
    }
    @Override
    public void endVisit(DoStatement node) { nestingLevel--; }

    @Override
    public boolean visit(TryStatement node) {
        complexity += 1 + nestingLevel;
        nestingLevel++;
        return super.visit(node);
    }
    @Override
    public void endVisit(TryStatement node) { nestingLevel--; }

    // Los catch no suman complejidad en SonarQube, ya se cuenta por el try
    @Override
    public boolean visit(CatchClause node) {
        return super.visit(node);
    }

    // --- Operadores lógicos (&& y ||), cuenta todos los de la expresión ---
    @Override
    public boolean visit(InfixExpression node) {
        if (node.getOperator() == InfixExpression.Operator.CONDITIONAL_AND ||
            node.getOperator() == InfixExpression.Operator.CONDITIONAL_OR) {
            complexity += countLogicalOperators(node);
            // Importante: evitar doble conteo en subnodos
            return false;
        }
        return super.visit(node);
    }

    // Cuenta recursivamente todos los operadores && y || en la expresión
    private int countLogicalOperators(InfixExpression node) {
        int count = 0;
        if (node.getOperator() == InfixExpression.Operator.CONDITIONAL_AND ||
            node.getOperator() == InfixExpression.Operator.CONDITIONAL_OR) {
            count++;
        }
        if (node.getLeftOperand() instanceof InfixExpression) {
            count += countLogicalOperators((InfixExpression) node.getLeftOperand());
        }
        if (node.getRightOperand() instanceof InfixExpression) {
            count += countLogicalOperators((InfixExpression) node.getRightOperand());
        }
        for (Object extended : node.extendedOperands()) {
            if (extended instanceof InfixExpression) {
                count += countLogicalOperators((InfixExpression) extended);
            }
        }
        return count;
    }

    // --- Getter ---
    public int getComplexity() {
        return complexity;
    }
}

