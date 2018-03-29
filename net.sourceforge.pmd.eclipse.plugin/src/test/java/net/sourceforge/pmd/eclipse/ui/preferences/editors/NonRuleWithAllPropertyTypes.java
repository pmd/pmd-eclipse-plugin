
package net.sourceforge.pmd.eclipse.ui.preferences.editors;

import java.lang.reflect.Method;

import net.sourceforge.pmd.lang.java.rule.AbstractJavaRule;
import net.sourceforge.pmd.properties.BooleanMultiProperty;
import net.sourceforge.pmd.properties.BooleanProperty;
import net.sourceforge.pmd.properties.CharacterMultiProperty;
import net.sourceforge.pmd.properties.CharacterProperty;
import net.sourceforge.pmd.properties.EnumeratedMultiProperty;
import net.sourceforge.pmd.properties.EnumeratedProperty;
import net.sourceforge.pmd.properties.FileProperty;
import net.sourceforge.pmd.properties.FloatMultiProperty;
import net.sourceforge.pmd.properties.FloatProperty;
import net.sourceforge.pmd.properties.IntegerMultiProperty;
import net.sourceforge.pmd.properties.IntegerProperty;
import net.sourceforge.pmd.properties.LongMultiProperty;
import net.sourceforge.pmd.properties.LongProperty;
import net.sourceforge.pmd.properties.MethodMultiProperty;
import net.sourceforge.pmd.properties.MethodProperty;
import net.sourceforge.pmd.properties.StringMultiProperty;
import net.sourceforge.pmd.properties.StringProperty;
import net.sourceforge.pmd.properties.TypeMultiProperty;
import net.sourceforge.pmd.properties.TypeProperty;
import net.sourceforge.pmd.util.ClassUtil;

/**
 * A non-functional rule containing all property types. Used for testing UIs.
 * 
 * Steps required to use with Eclipse Plugin:
 * 
 * update your chosen ruleset xml file to include this 'rule' compile new PMD
 * jars copy both the pmd5.0.jar and pmd-test-5.0.jar to the eclipse-plugin/lib
 * directory update the /manifest.mf file to ensure it includes the
 * pmd-test-5.0.jar
 * 
 * @author Brian Remedios
 */
public class NonRuleWithAllPropertyTypes extends AbstractJavaRule {

    private static final Method STRING_LENGTH = ClassUtil.methodFor(String.class, "length", ClassUtil.EMPTY_CLASS_ARRAY);
    private static final Method STRING_TO_LOWER_CASE = ClassUtil.methodFor(String.class, "toLowerCase",
            ClassUtil.EMPTY_CLASS_ARRAY);

    // descriptors are public to enable us to write external tests
    public static final StringProperty SINGLE_STR = new StringProperty("singleStr", "String value", "hello world", 3.0f);
    public static final StringMultiProperty MULTI_STR = new StringMultiProperty("multiStr", "Multiple string values",
            new String[] { "hello", "world" }, 5.0f, '|');

    public static final IntegerProperty SINGLE_INT = new IntegerProperty("singleInt", "Single integer value", 1, 10, 8,
            3.0f);
    public static final IntegerMultiProperty MULTI_INT = new IntegerMultiProperty("multiInt", "Multiple integer values",
            0, 10, new Integer[] { 1, 2, 3, 4 }, 5.0f);

    public static final LongProperty SINGLE_LONG = new LongProperty("singleLong", "Single long value", 1L, 10L, 8L,
            3.0f);
    public static final LongMultiProperty MULTI_LONG = new LongMultiProperty("multiLong", "Multiple long values", 0L,
            10L, new Long[] { 1L, 2L, 3L, 4L }, 5.0f);

    public static final BooleanProperty SINGLE_BOOL = new BooleanProperty("singleBool", "Single boolean value", true,
            6.0f);
    public static final BooleanMultiProperty MULTI_BOOL = new BooleanMultiProperty("multiBool",
            "Multiple boolean values", new Boolean[] { true, false }, 5.0f);

    public static final CharacterProperty SINGLE_CHAR = new CharacterProperty("singleChar", "Single character", 'a',
            5.0f);
    public static final CharacterMultiProperty MULTI_CHAR = new CharacterMultiProperty("multiChar",
            "Multiple characters", new Character[] { 'a', 'e', 'i', 'o', 'u' }, 6.0f, '|');

    public static final FloatProperty SINGLE_FLOAT = new FloatProperty("singleFloat", "Single float value", 9f, 10f, .9f,
            5.0f);
    public static final FloatMultiProperty MULTI_FLOAT = new FloatMultiProperty("multiFloat", "Multiple float values",
            0f, 5f, new Float[] { 1f, 2f, 3f }, 6.0f);

    public static final TypeProperty SINGLE_TYPE = new TypeProperty("singleType", "Single type", String.class,
            new String[] { "java.lang" }, 5.0f);
    public static final TypeMultiProperty MULTI_TYPE = TypeMultiProperty.named("multiType")
            .desc("Multiple types")
            .defaultValues(Integer.class, Object.class)
            .legalPackages(new String[] { "java.lang" })
            .uiOrder(6.0f)
            .build();

    public static final MethodProperty SINGLE_METHOD = new MethodProperty("singleMethod", "Single method", STRING_LENGTH,
            new String[] { "java.lang" }, 5.0f);
    public static final MethodMultiProperty MULTI_METHOD = new MethodMultiProperty("multiMethod", "Multiple methods",
            new Method[] { STRING_LENGTH, STRING_TO_LOWER_CASE }, new String[] { "java.lang" }, 6.0f);

    public static final EnumeratedProperty<Class> ENUM_TYPE = new EnumeratedProperty<Class>("enumType",
            "Enumerated choices", new String[] { "String", "Object" }, new Class[] { String.class, Object.class }, 1,
            5.0f);
    public static final EnumeratedMultiProperty<Class> MULTI_ENUM_TYPE = new EnumeratedMultiProperty<Class>(
            "multiEnumType", "Multiple enumerated choices", new String[] { "String", "Object" },
            new Class[] { String.class, Object.class }, new int[] { 0, 1 }, 5.0f);

    public static final FileProperty SINGLE_FILE = new FileProperty("singleFile", "File value", null, 3.0f);

    public NonRuleWithAllPropertyTypes() {
        super();
        definePropertyDescriptor(SINGLE_STR);
        definePropertyDescriptor(MULTI_STR);
        definePropertyDescriptor(SINGLE_INT);
        definePropertyDescriptor(MULTI_INT);
        definePropertyDescriptor(SINGLE_LONG);
        definePropertyDescriptor(MULTI_LONG);
        definePropertyDescriptor(SINGLE_BOOL);
        definePropertyDescriptor(MULTI_BOOL);
        definePropertyDescriptor(SINGLE_CHAR);
        definePropertyDescriptor(MULTI_CHAR);
        definePropertyDescriptor(SINGLE_FLOAT);
        definePropertyDescriptor(MULTI_FLOAT);
        definePropertyDescriptor(SINGLE_TYPE);
        definePropertyDescriptor(MULTI_TYPE);
        definePropertyDescriptor(ENUM_TYPE);
        definePropertyDescriptor(SINGLE_METHOD);
        definePropertyDescriptor(MULTI_METHOD);
        definePropertyDescriptor(MULTI_ENUM_TYPE);
        definePropertyDescriptor(SINGLE_FILE);
    }
}
