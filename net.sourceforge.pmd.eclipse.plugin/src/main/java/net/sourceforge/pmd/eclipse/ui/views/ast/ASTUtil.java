/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.eclipse.ui.views.ast;

import static net.sourceforge.pmd.util.AssertionUtil.shouldNotReachHere;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import net.sourceforge.pmd.lang.ecmascript.ast.ASTName;
import net.sourceforge.pmd.lang.java.ast.ASTAmbiguousName;
import net.sourceforge.pmd.lang.java.ast.ASTAnnotation;
import net.sourceforge.pmd.lang.java.ast.ASTArrayType;
import net.sourceforge.pmd.lang.java.ast.ASTClassType;
import net.sourceforge.pmd.lang.java.ast.ASTFieldDeclaration;
import net.sourceforge.pmd.lang.java.ast.ASTFormalParameter;
import net.sourceforge.pmd.lang.java.ast.ASTIntersectionType;
import net.sourceforge.pmd.lang.java.ast.ASTLocalVariableDeclaration;
import net.sourceforge.pmd.lang.java.ast.ASTMethodDeclaration;
import net.sourceforge.pmd.lang.java.ast.ASTPrimitiveType;
import net.sourceforge.pmd.lang.java.ast.ASTReferenceType;
import net.sourceforge.pmd.lang.java.ast.ASTType;
import net.sourceforge.pmd.lang.java.ast.ASTTypeArguments;
import net.sourceforge.pmd.lang.java.ast.ASTUnionType;
import net.sourceforge.pmd.lang.java.ast.ASTVariableId;
import net.sourceforge.pmd.lang.java.ast.ASTVoidType;
import net.sourceforge.pmd.lang.java.ast.ASTWildcardType;
import net.sourceforge.pmd.lang.java.ast.JModifier;
import net.sourceforge.pmd.lang.java.ast.ModifierOwner;
import net.sourceforge.pmd.lang.java.ast.ModifierOwner.Visibility;

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
            sb.append(' ');
            prettyPrintType(sb, type);
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
            prettyPrintType(sb, typeNode).append(", ");
        }

        int length = sb.length();
        return length == 0 ? "" : sb.toString().substring(0, length - 2);
    }

    public static String returnType(ASTMethodDeclaration node) {
        ASTType resultType = node.getResultTypeNode();
        return prettyPrintType(resultType);
    }

    public static String getLocalVarDeclarationLabel(ASTLocalVariableDeclaration node) {

        StringBuilder sb = new StringBuilder();
        addModifiers(node, sb);

        ASTType type = node.getTypeNode();
        sb.append(' ');
        prettyPrintType(sb, type);

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

    private static String prettyPrintType(ASTType type) {
        StringBuilder sb = new StringBuilder();
        prettyPrintType(sb, type);
        return sb.toString();
    }

    private static StringBuilder prettyPrintType(StringBuilder sb, ASTType type) {
        if (type instanceof ASTPrimitiveType) {
            sb.append(((ASTPrimitiveType) type).getKind().getSimpleName());
        } else if (type instanceof ASTClassType) {
            ASTClassType classT = (ASTClassType) type;
            sb.append(classT.getSimpleName());

            ASTTypeArguments targs = classT.getTypeArguments();
            if (targs != null) {
                sb.append("<");
                sb.append(targs.toList().stream().map(ASTUtil::prettyPrintType).collect(Collectors.joining(", ")));
                sb.append(">");
            }
        } else if (type instanceof ASTArrayType) {
            sb.append(prettyPrintType(((ASTArrayType) type).getElementType()));
            int depth = ((ASTArrayType) type).getArrayDepth();
            for (int i = 0; i < depth; i++) {
                sb.append("[]");
            }
        } else if (type instanceof ASTVoidType) {
            sb.append("void");
        } else if (type instanceof ASTWildcardType) {
            sb.append("?");
            ASTReferenceType bound = ((ASTWildcardType) type).getTypeBoundNode();
            if (bound != null) {
                sb.append(((ASTWildcardType) type).isLowerBound() ? " super " : " extends ");
                sb.append(prettyPrintType(bound));
            }
        } else if (type instanceof ASTUnionType) {
            sb.append(((ASTUnionType) type).getComponents().toList().stream().map(ASTUtil::prettyPrintType).collect(Collectors.joining(" | ")));
        } else if (type instanceof ASTIntersectionType) {
            sb.append(((ASTIntersectionType) type).getComponents().toList().stream().map(ASTUtil::prettyPrintType).collect(Collectors.joining(" & ")));
        } else if (type instanceof ASTAmbiguousName) {
            sb.append(((ASTAmbiguousName) type).getName());
        } else {
            throw shouldNotReachHere("Unhandled type? " + type);
        }
        return sb;
    }
}
