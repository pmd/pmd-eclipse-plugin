/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.eclipse.ui.views.ast;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import net.sourceforge.pmd.lang.ecmascript.ast.ASTName;
import net.sourceforge.pmd.lang.java.ast.ASTAnnotation;
import net.sourceforge.pmd.lang.java.ast.ASTFieldDeclaration;
import net.sourceforge.pmd.lang.java.ast.ASTFormalParameter;
import net.sourceforge.pmd.lang.java.ast.ASTLocalVariableDeclaration;
import net.sourceforge.pmd.lang.java.ast.ASTMethodDeclaration;
import net.sourceforge.pmd.lang.java.ast.ASTType;
import net.sourceforge.pmd.lang.java.ast.ASTVariableId;
import net.sourceforge.pmd.lang.java.ast.JModifier;
import net.sourceforge.pmd.lang.java.ast.ModifierOwner;
import net.sourceforge.pmd.lang.java.ast.ModifierOwner.Visibility;
import net.sourceforge.pmd.lang.java.ast.internal.PrettyPrintingUtil;

/**
 * 
 * @author Brian Remedios
 */
public final class ASTUtil {

    public static final Comparator<ASTMethodDeclaration> METHOD_COMPARATOR = new Comparator<ASTMethodDeclaration>() {
        @Override
        public int compare(ASTMethodDeclaration m1, ASTMethodDeclaration m2) {
            return m1.getName().compareTo(m2.getName());
        }
    };

    private ASTUtil() {
    }

    public static String getAnnotationLabel(ASTAnnotation annotation) {
        ASTName name = annotation.firstChild(ASTName.class);
        return name == null ? "??" : name.getImage();
    }

    public static String getMethodLabel(ASTMethodDeclaration pmdMethod, boolean includeModifiers) {
        String returnType = returnType(pmdMethod);

        StringBuilder sb = new StringBuilder();

        if (includeModifiers) {
            addModifiers(pmdMethod, sb);
            sb.append(' ');
        }

        sb.append(pmdMethod.getName());
        sb.append('(').append(parameterTypes(pmdMethod)).append(')');
        if (returnType == null) {
            return sb.toString();
        }

        sb.append(" : ").append(returnType);
        return sb.toString();
    }

    private static List<String> modifiersFor(ModifierOwner node) {
        List<String> modifiers = new ArrayList<>();
        if (node.hasVisibility(Visibility.V_PUBLIC)) {
            modifiers.add("public");
        } else {
            if (node.hasVisibility(Visibility.V_PROTECTED)) {
                modifiers.add("protected");
            } else {
                if (node.hasVisibility(Visibility.V_PRIVATE)) {
                    modifiers.add("private");
                }
            }
        }

        if (node.hasModifiers(JModifier.ABSTRACT)) {
            modifiers.add("abstract");
        }
        if (node.hasModifiers(JModifier.STATIC)) {
            modifiers.add("static");
        }
        if (node.hasModifiers(JModifier.FINAL)) {
            modifiers.add("final");
        }
        if (node.hasModifiers(JModifier.TRANSIENT)) {
            modifiers.add("transient");
        }
        if (node.hasModifiers(JModifier.VOLATILE)) {
            modifiers.add("volatile");
        }
        if (node.hasModifiers(JModifier.SYNCHRONIZED)) {
            modifiers.add("synchronized");
        }
        if (node.hasModifiers(JModifier.NATIVE)) {
            modifiers.add("native");
        }
        if (node.hasModifiers(JModifier.STRICTFP)) {
            modifiers.add("strictfp");
        }
        return modifiers;
    }

    private static void addModifiers(ModifierOwner node, StringBuilder sb) {

        List<String> modifiers = modifiersFor(node);
        if (modifiers.isEmpty()) {
            return;
        }

        sb.append(modifiers.get(0));
        for (int i = 1; i < modifiers.size(); i++) {
            sb.append(' ').append(modifiers.get(i));
        }
    }

    public static String getFieldLabel(ASTFieldDeclaration pmdField) {

        StringBuilder sb = new StringBuilder();
        addModifiers(pmdField, sb);

        ASTType type = pmdField.firstChild(ASTType.class);
        if (type != null) {
            sb.append(' ').append(PrettyPrintingUtil.prettyPrintType(type));
        }

        sb.append(' ');
        boolean first = true;
        for (ASTVariableId id : pmdField) {
            if (!first) {
                sb.append(", ");
            }
            first = false;
            sb.append(id.getName());
        }

        return sb.toString();
    }

    public static String parameterTypes(ASTMethodDeclaration node) {

        StringBuilder sb = new StringBuilder();

        for (ASTFormalParameter formalParam : node.getFormalParameters()) {
            ASTType typeNode = formalParam.getTypeNode();
            sb.append(PrettyPrintingUtil.prettyPrintType(typeNode)).append(", ");
        }

        int length = sb.length();
        return length == 0 ? "" : sb.toString().substring(0, length - 2);
    }

    public static String returnType(ASTMethodDeclaration node) {
        ASTType resultType = node.getResultTypeNode();
        return PrettyPrintingUtil.prettyPrintType(resultType);
    }

    public static String getLocalVarDeclarationLabel(ASTLocalVariableDeclaration node) {

        StringBuilder sb = new StringBuilder();
        addModifiers(node, sb);

        ASTType type = node.getTypeNode();
        sb.append(' ').append(PrettyPrintingUtil.prettyPrintType(type));

        boolean first = true;
        for (ASTVariableId id : node) {
            if (!first) {
                sb.append(", ");
            }
            first = false;
            sb.append(id.getName());
            if (id.hasArrayType()) {
                sb.append("[]");
            }
        }

        return sb.toString();
    }
}
