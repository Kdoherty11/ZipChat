package unit;

import org.junit.Ignore;
import org.junit.Test;
import validation.Validator;
import validation.validators.Validators;

import java.util.Optional;

import static org.fest.assertions.Assertions.assertEquals;

@Ignore
public class ValidatorsTest {

    @Test
    public void testRequiredValidator() {
        assertEquals(Validators.required().isValid(Optional.of("String"))).isTrue();
        assertEquals(Validators.required().isValid(Optional.empty())).isFalse();
        assertEquals(Validators.required().getErrorMessage()).isNotEmpty();
    }

    @Test
    public void testMinValidator() {
        Validator<Number> minValidator = Validators.min(0);
        assertEquals(minValidator.isValid(Optional.empty())).isTrue();
        assertEquals(minValidator.isValid(Optional.of(-1))).isFalse();
        assertEquals(minValidator.isValid(Optional.of(0))).isTrue();
        assertEquals(minValidator.isValid(Optional.of(1))).isTrue();
        assertEquals(minValidator.getErrorMessage()).isNotEmpty();
    }

    @Test
    public void testWhiteListValidator() {
        Validator<String> whiteListValidator = Validators.whiteList("White", "List");
        assertEquals(whiteListValidator.isValid(Optional.empty())).isTrue();
        assertEquals(whiteListValidator.isValid(Optional.of("White"))).isTrue();
        assertEquals(whiteListValidator.isValid(Optional.of("List"))).isTrue();
        assertEquals(whiteListValidator.isValid(Optional.of("NotValid"))).isFalse();
        assertEquals(whiteListValidator.isValid(Optional.of("white"))).isFalse();
        assertEquals(whiteListValidator.getErrorMessage()).isNotEmpty();
    }
}
