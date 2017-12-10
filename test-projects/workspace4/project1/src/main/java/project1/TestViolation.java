package project1;

import java.math.BigDecimal;

public class TestViolation {

    // Expected PMD Violations at line 11:
    // 1. Avoid creating BigDecimal with a decimal (float/double) literal. Use
    //    a String literal.
    //    (errorprone/AvoidDecimalLiteralsInBigDecimalConstructor)
    BigDecimal big = new BigDecimal(0.1); 

    BigDecimal big2 = new BigDecimal(0.2); // NOPMD: all violations suppressed!

    @SuppressWarnings("PMD.AvoidDecimalLiteralsInBigDecimalConstructor")
    BigDecimal big3 = new BigDecimal(0.3);
}
