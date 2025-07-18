/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.eclipse.ui.views.ast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;

import net.sourceforge.pmd.lang.ast.Node;
import net.sourceforge.pmd.lang.ast.impl.AbstractNode;
import net.sourceforge.pmd.lang.document.TextRegion;
import net.sourceforge.pmd.lang.java.ast.ASTCompilationUnit;
import net.sourceforge.pmd.lang.java.ast.ASTImportDeclaration;
import net.sourceforge.pmd.lang.java.ast.JavaComment;
import net.sourceforge.pmd.lang.java.ast.JavaNode;

/**
 * 
 * @author Brian Remedios
 */
public class ASTContentProvider implements ITreeContentProvider {

    private boolean includeImports;
    private boolean includeComments;
    // private Set<Class<?>> hiddenNodeTypes;

    private static final Comparator<Node> BY_LINE_NUMBER = new Comparator<Node>() {
        @Override
        public int compare(Node a, Node b) {
            return a.getBeginLine() - b.getBeginLine();
        }
    };

    public ASTContentProvider(boolean includeImportsFlag, boolean includeCommentsFlag) {
        this(Collections.<Class<?>>emptySet());

        includeImports = includeImportsFlag;
        includeComments = includeCommentsFlag;
    }

    public ASTContentProvider(Set<Class<?>> theHiddenNodeTypes) { // NOPMD: unused formal parameter TODO
        // hiddenNodeTypes = theHiddenNodeTypes;
    }

    public void includeImports(boolean flag) {
        includeImports = flag;
    }

    public void includeComments(boolean flag) {
        includeComments = flag;
    }

    @Override
    public void dispose() {
        // TODO
    }

    @Override
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        // TODO
    }

    static class CommentNode extends AbstractNode<CommentNode, JavaNode> {
        private final JavaComment comment;

        CommentNode(JavaComment comment) {
            this.comment = comment;
        }

        @Override
        public TextRegion getTextRegion() {
            return comment.getReportLocation().getRegionInFile();
        }

        @Override
        public String getXPathNodeName() {
            return "comment";
        }
    }

    private List<Node> withoutHiddenOnes(Object parent) {
        List<Node> kids = new ArrayList<>();

        if (includeComments && parent instanceof ASTCompilationUnit) {
            // if (!hiddenNodeTypes.contains(Comment.class)) {

            List<JavaComment> comments = ((ASTCompilationUnit) parent).getComments();


            kids.addAll(comments.stream().map(comment -> new CommentNode(comment)).collect(Collectors.toList()));
            // }
        }

        AbstractNode node = (AbstractNode) parent;
        int kidCount = node.getNumChildren();
        for (int i = 0; i < kidCount; i++) {
            Node kid = node.getChild(i);
            // if (hiddenNodeTypes.contains(kid.getClass())) continue;
            if (!includeImports && kid instanceof ASTImportDeclaration) {
                continue;
            }
            if (!includeComments && kid instanceof CommentNode) {
                continue;
            }
            kids.add(kid);
        }

        Collections.sort(kids, BY_LINE_NUMBER);

        return kids;
    }

    @Override
    public Object[] getElements(Object inputElement) {
        AbstractNode parent = (AbstractNode) inputElement;
        return withoutHiddenOnes(parent).toArray();
    }

    @Override
    public Object[] getChildren(Object parentElement) {
        AbstractNode parent = (AbstractNode) parentElement;
        return withoutHiddenOnes(parent).toArray();
    }

    @Override
    public Object getParent(Object element) {
        AbstractNode parent = (AbstractNode) element;
        return parent.getParent();
    }

    @Override
    public boolean hasChildren(Object element) {
        AbstractNode parent = (AbstractNode) element;
        return parent.getNumChildren() > 0;
    }

    public static void setupSorter(TableViewer viewer) {
        // TODO
    }
}
