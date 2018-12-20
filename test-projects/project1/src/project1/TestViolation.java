package project1;

import java.math.BigDecimal;

/* Expected 2 PMD Violations at line 10:
 * 1. Header comments are required (documentation/CommentRequired)
 * 2. Each class should declare at least one constructor
 *    (codestyle/AtLeastOneConstructor)
 */
public class TestViolation {

    // Expected 4 PMD Violations at line 15:
    // 1. Missing commented default access modifier
    //    (codestyle/CommentDefaultAccessModifier)
    // 2. Field comments are required
    //    (documentation/CommentRequired)
    // 3. Use explicit scoping instead of the default package private level
    //    (codestyle/DefaultPackage)
    // 4. Avoid creating BigDecimal with a decimal (float/double) literal. Use
    //    a String literal.
    //    (errorprone/AvoidDecimalLiteralsInBigDecimalConstructor)
    BigDecimal big = new BigDecimal(0.1); 

    BigDecimal big2 = new BigDecimal(0.2); // NOPMD: all violations suppressed!

    // Only the violation for AvoidDecimalLiteralsInBigDecimalConstructor
    // is suppressed. We have 3 violations left.
    @SuppressWarnings("PMD.AvoidDecimalLiteralsInBigDecimalConstructor")
    BigDecimal big3 = new BigDecimal(0.3);
}
