package unit.validation.validators;

import org.junit.Test;
import utils.TestUtils;
import validation.Validator;
import validation.validators.Validators;

import java.lang.reflect.InvocationTargetException;
import java.util.Optional;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by kdoherty on 8/2/15.
 */
public class ValidatorsTest {

    @Test
    public void testConstructorIsPrivate() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        TestUtils.testConstructorIsPrivate(Validators.class);
    }

    @Test
    public void testRequiredValidator() {
        assertTrue(Validators.required().isValid(Optional.of("String")));
        assertFalse(Validators.required().isValid(Optional.empty()));
        assertFalse(Validators.required().getErrorMessage().isEmpty());
    }

    @Test
    public void testMinValidator() {
        Validator<Number> minValidator = Validators.min(0);
        assertTrue(minValidator.isValid(Optional.empty()));
        assertFalse(minValidator.isValid(Optional.of(-1)));
        assertTrue(minValidator.isValid(Optional.of(0)));
        assertTrue(minValidator.isValid(Optional.of(1)));
        assertFalse(minValidator.getErrorMessage().isEmpty());
    }

    @Test
    public void testWhiteListValidator() {
        Validator<String> whiteListValidator = Validators.whiteList("White", "List");
        assertTrue(whiteListValidator.isValid(Optional.empty()));
        assertTrue(whiteListValidator.isValid(Optional.of("White")));
        assertTrue(whiteListValidator.isValid(Optional.of("List")));
        assertFalse(whiteListValidator.isValid(Optional.of("NotValid")));
        assertFalse(whiteListValidator.isValid(Optional.of("white")));
        assertFalse(whiteListValidator.getErrorMessage().isEmpty());
    }

}
