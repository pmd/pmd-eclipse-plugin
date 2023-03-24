/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.eclipse.ui.views.ast;

import java.util.HashMap;
import java.util.Map;

import net.sourceforge.pmd.lang.ast.Node;
import net.sourceforge.pmd.lang.java.ast.ASTAnnotation;
import net.sourceforge.pmd.lang.java.ast.ASTClassOrInterfaceType;
import net.sourceforge.pmd.lang.java.ast.ASTCompilationUnit;
import net.sourceforge.pmd.lang.java.ast.ASTFieldDeclaration;
import net.sourceforge.pmd.lang.java.ast.ASTImportDeclaration;
import net.sourceforge.pmd.lang.java.ast.ASTLocalVariableDeclaration;
import net.sourceforge.pmd.lang.java.ast.ASTMethodDeclaration;
import net.sourceforge.pmd.lang.java.ast.ASTThrowStatement;
import net.sourceforge.pmd.lang.java.ast.JavaComment;

/**
 * For nodes higher in the tree that don't have any identifying information we
 * can walk their children and derive some in Java-like form. The idea is to
 * help keep the number of child nodes that need to be visible to a minimum.
 * 
 * @author Brian Remedios
 */
public class NodeImageDeriver {

    private static NodeImageDeriver compilationUnitDeriver = new NodeImageDeriver(ASTCompilationUnit.class) {
        @Override
        public String deriveFrom(Node node) {
            dumpComments((ASTCompilationUnit) node);
            return "Comments: " + ((ASTCompilationUnit) node).getComments().size();
        }
    };

    private static NodeImageDeriver importDeriver = new NodeImageDeriver(ASTImportDeclaration.class) {
        @Override
        public String deriveFrom(Node node) {
            // TODO show package name as well?
            return ((ASTImportDeclaration) node).getImportedName();
        }
    };

    private static NodeImageDeriver methodDeclarationDeriver = new NodeImageDeriver(ASTMethodDeclaration.class) {
        @Override
        public String deriveFrom(Node node) {
            return ASTUtil.getMethodLabel((ASTMethodDeclaration) node, true);
        }
    };

    private static NodeImageDeriver throwStatementDeriver = new NodeImageDeriver(ASTThrowStatement.class) {
        @Override
        public String deriveFrom(Node node) {
            final ASTClassOrInterfaceType t = node.getFirstDescendantOfType(ASTClassOrInterfaceType.class);
            return t == null ? null : t.getImage();
        }
    };

    private static NodeImageDeriver fieldDeclarationDeriver = new NodeImageDeriver(ASTFieldDeclaration.class) {
        @Override
        public String deriveFrom(Node node) {
            return ASTUtil.getFieldLabel((ASTFieldDeclaration) node);
        }
    };

    private static NodeImageDeriver localVariableDeclarationDeriver = new NodeImageDeriver(
            ASTLocalVariableDeclaration.class) {
        @Override
        public String deriveFrom(Node node) {
            return ASTUtil.getLocalVarDeclarationLabel((ASTLocalVariableDeclaration) node);
        }
    };

    private static NodeImageDeriver annotationDeriver = new NodeImageDeriver(ASTAnnotation.class) {
        @Override
        public String deriveFrom(Node node) {
            return ASTUtil.getAnnotationLabel((ASTAnnotation) node);
        }
    };

    private static final NodeImageDeriver[] ALL_DERIVERS = new NodeImageDeriver[] { importDeriver,
        methodDeclarationDeriver, localVariableDeclarationDeriver, fieldDeclarationDeriver, annotationDeriver,
        compilationUnitDeriver, throwStatementDeriver };

    private static final Map<Class<?>, NodeImageDeriver> DERIVERS_BY_TYPE = new HashMap<Class<?>, NodeImageDeriver>(
            NodeImageDeriver.ALL_DERIVERS.length);

    public final Class<?> target;

    public NodeImageDeriver(Class<?> theASTClass) {
        target = theASTClass;
    }

    public String deriveFrom(Node node) {
        return null; // failed to implement!
    }

    private static void dumpComments(ASTCompilationUnit node) {
        for (JavaComment comment : node.getComments()) {
            System.out.println(comment.getClass().getName());
            System.out.println(comment.getText());
        }
    }

    static {
        for (NodeImageDeriver deriver : NodeImageDeriver.ALL_DERIVERS) {
            DERIVERS_BY_TYPE.put(deriver.target, deriver);
        }
    }

    public static String derivedTextFor(Node node) {
        NodeImageDeriver deriver = DERIVERS_BY_TYPE.get(node.getClass());
        return deriver == null ? null : deriver.deriveFrom(node);
    }
}
