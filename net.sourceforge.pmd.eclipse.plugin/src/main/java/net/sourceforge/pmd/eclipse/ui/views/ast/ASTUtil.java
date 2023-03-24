/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.eclipse.ui.views.ast;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import net.sourceforge.pmd.lang.ast.impl.AbstractNode;
import net.sourceforge.pmd.lang.ecmascript.ast.ASTName;
import net.sourceforge.pmd.lang.java.ast.ASTAnnotation;
import net.sourceforge.pmd.lang.java.ast.ASTClassOrInterfaceType;
import net.sourceforge.pmd.lang.java.ast.ASTFieldDeclaration;
import net.sourceforge.pmd.lang.java.ast.ASTFormalParameter;
import net.sourceforge.pmd.lang.java.ast.ASTLocalVariableDeclaration;
import net.sourceforge.pmd.lang.java.ast.ASTMethodDeclaration;
import net.sourceforge.pmd.lang.java.ast.ASTPrimitiveType;
import net.sourceforge.pmd.lang.java.ast.ASTType;
import net.sourceforge.pmd.lang.java.ast.ASTVariableDeclaratorId;
import net.sourceforge.pmd.lang.java.ast.AccessNode;
import net.sourceforge.pmd.lang.java.ast.JavaNode;

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
        ASTName name = annotation.getFirstChildOfType(ASTName.class);
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

    private static List<String> modifiersFor(AccessNode node) {
        List<String> modifiers = new ArrayList<>();
        if (node.isPublic()) {
            modifiers.add("public");
        } else {
            if (node.isProtected()) {
                modifiers.add("protected");
            } else {
                if (node.isPrivate()) {
                    modifiers.add("private");
                }
            }
        }

        if (node.isAbstract()) {
            modifiers.add("abstract");
        }
        if (node.isStatic()) {
            modifiers.add("static");
        }
        if (node.isFinal()) {
            modifiers.add("final");
        }
        if (node.isTransient()) {
            modifiers.add("transient");
        }
        if (node.isVolatile()) {
            modifiers.add("volatile");
        }
        if (node.isSynchronized()) {
            modifiers.add("synchronized");
        }
        if (node.isNative()) {
            modifiers.add("native");
        }
        if (node.isStrictfp()) {
            modifiers.add("strictfp");
        }
        return modifiers;
    }

    private static void addModifiers(AccessNode node, StringBuilder sb) {

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

        ASTType type = pmdField.getFirstChildOfType(ASTType.class);
        if (type != null) {
            sb.append(' ').append(type.getTypeImage());
        }

        sb.append(' ');
        boolean first = true;
        for (ASTVariableDeclaratorId id : pmdField) {
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
            JavaNode param = formalParam.getFirstDescendantOfType(ASTClassOrInterfaceType.class);
            if (param == null) {
                param = formalParam.getFirstDescendantOfType(ASTPrimitiveType.class);
            }
            if (param == null) {
                continue;
            }
            sb.append(param.getImage()).append(", ");
        }

        int length = sb.length();
        return length == 0 ? "" : sb.toString().substring(0, length - 2);
    }

    public static String returnType(ASTMethodDeclaration node) {
        ASTType resultType = node.getResultTypeNode();
        if (resultType.isVoid()) {
            return "void";
        }
        AbstractNode param = resultType.getFirstDescendantOfType(ASTClassOrInterfaceType.class);
        if (param == null) {
            param = resultType.getFirstDescendantOfType(ASTPrimitiveType.class);
        }
        return param.getImage();
    }

    public static String getLocalVarDeclarationLabel(ASTLocalVariableDeclaration node) {

        StringBuilder sb = new StringBuilder();
        addModifiers(node, sb);

        ASTType type = node.getTypeNode();
        sb.append(' ').append(type.getTypeImage());

        boolean first = true;
        for (ASTVariableDeclaratorId id : node) {
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
