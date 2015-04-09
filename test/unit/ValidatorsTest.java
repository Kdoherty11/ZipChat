package unit;

import org.junit.Test;
import validation.Validator;
import validation.Validators;

import java.util.Optional;

import static org.fest.assertions.Assertions.assertThat;

public class ValidatorsTest {

    @Test
    public void testRequiredValidator() {
        assertThat(Validators.required().isValid(Optional.of("String"))).isTrue();
        assertThat(Validators.required().isValid(Optional.empty())).isFalse();
        assertThat(Validators.required().getErrorMessage()).isNotEmpty();
    }

    @Test
    public void testMinValidator() {
        Validator<Number> minValidator = Validators.min(0);
        assertThat(minValidator.isValid(Optional.empty())).isTrue();
        assertThat(minValidator.isValid(Optional.of(-1))).isFalse();
        assertThat(minValidator.isValid(Optional.of(0))).isTrue();
        assertThat(minValidator.isValid(Optional.of(1))).isTrue();
        assertThat(minValidator.getErrorMessage()).isNotEmpty();
    }

    @Test
    public void testWhiteListValidator() {
        Validator<String> whiteListValidator = Validators.whiteList("White", "List");
        assertThat(whiteListValidator.isValid(Optional.empty())).isTrue();
        assertThat(whiteListValidator.isValid(Optional.of("White"))).isTrue();
        assertThat(whiteListValidator.isValid(Optional.of("List"))).isTrue();
        assertThat(whiteListValidator.isValid(Optional.of("NotValid"))).isFalse();
        assertThat(whiteListValidator.isValid(Optional.of("white"))).isFalse();
        assertThat(whiteListValidator.getErrorMessage()).isNotEmpty();
    }
}
