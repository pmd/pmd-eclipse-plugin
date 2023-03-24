/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.eclipse.ui.views.ast;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.TextLayout;
import org.eclipse.swt.graphics.TextStyle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.TreeItem;

import net.sourceforge.pmd.lang.ast.impl.AbstractNode;
import net.sourceforge.pmd.lang.java.ast.ASTBooleanLiteral;
import net.sourceforge.pmd.lang.java.ast.JavadocComment;

/**
 * 
 * @author Brian Remedios
 */
public class ASTPainterHelper {
    private Font renderFont;
    private Font italicFont;
    private TextLayout textLayout;
    private TextStyle labelStyle;
    private TextStyle imageStyle;
    private TextStyle derivedStyle;

    public ASTPainterHelper(Display display) {

        textLayout = new TextLayout(display);

        // TODO take values from the font/color registries and then adapt to
        // changes
        renderFont = new Font(display, "Tahoma", 10, SWT.NORMAL);
        italicFont = new Font(display, "Tahoma", 10, SWT.ITALIC);
        labelStyle = new TextStyle(renderFont, display.getSystemColor(SWT.COLOR_BLACK), null);
        imageStyle = new TextStyle(renderFont, display.getSystemColor(SWT.COLOR_BLUE), null);
        derivedStyle = new TextStyle(italicFont, display.getSystemColor(SWT.COLOR_GRAY), null);
    }

    private String lineTextFor(ASTContentProvider.CommentNode comment) {

        StringBuilder sb = new StringBuilder();

        if (comment.getBeginLine() == comment.getEndLine()) {
            sb.append(comment.getBeginLine());
        } else {
            sb.append(comment.getBeginLine()).append('-').append(comment.getEndLine());
        }

        sb.append(' ');

        List<String> lines = CommentUtil.multiLinesIn(comment.getImage());
        String first = lines.get(0);
        if (StringUtils.isNotBlank(first)) {
            sb.append(first);
        }

        if (lines.size() == 1) {
            return sb.toString();
        } else {
            for (String line : lines) {
                if (StringUtils.isBlank(line)) {
                    continue;
                }
                sb.append('|').append(line);
            }
        }

        return sb.toString();
    }

    private TextLayout layoutFor(ASTContentProvider.CommentNode comment) {
        String label = comment.getClass().getSimpleName();
        int labelLength = label.length();

        String lineText = lineTextFor(comment);
        textLayout.setText(label + " " + lineText);
        textLayout.setStyle(derivedStyle, labelLength, labelLength + lineText.length());
        return textLayout;
    }

    private TextLayout layoutFor(JavadocComment javadoc) {
        String label = "@" + javadoc.getText();
        // int labelLength = label.length();

        textLayout.setText(label);
        // textLayout.setStyle(derivedStyle, labelLength, labelLength +
        // label.length());
        return textLayout;
    }

    private String textFor(AbstractNode node) {
        String txt = node.getImage();
        if (StringUtils.isNotBlank(txt)) {
            return txt;
        }

        // booleans don't have image values..convert them
        if (node instanceof ASTBooleanLiteral) {
            return Boolean.toString(((ASTBooleanLiteral) node).isTrue());
        }

        return null;
    }

    public TextLayout layoutFor(TreeItem item) {

        Object data = item.getData();
        if (data instanceof ASTContentProvider.CommentNode) {
            return layoutFor((ASTContentProvider.CommentNode) data);
        }

        if (data instanceof JavadocComment) {
            return layoutFor((JavadocComment) data);
        }

        AbstractNode node = (AbstractNode) data;
        String label = node.getXPathNodeName();

        TextStyle extraStyle = imageStyle;
        String extra = NodeImageDeriver.derivedTextFor(node);
        if (extra != null) {
            extraStyle = derivedStyle;
        } else {
            extra = textFor(node);
        }

        textLayout.setText(label + (extra == null ? "" : " " + extra));

        int labelLength = label.length();

        textLayout.setStyle(labelStyle, 0, labelLength);
        if (extra != null) {
            textLayout.setStyle(extraStyle, labelLength, labelLength + extra.length() + 1);
        }

        return textLayout;
    }

    public void dispose() {
        renderFont.dispose();
        italicFont.dispose();
    }
}
