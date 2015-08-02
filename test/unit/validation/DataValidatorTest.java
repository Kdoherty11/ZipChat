package unit.validation;

import com.google.common.collect.HashMultimap;
import org.junit.Test;
import validation.DataValidator;
import validation.FieldValidator;

import static org.mockito.Mockito.*;

/**
 * Created by kdoherty on 8/2/15.
 */
public class DataValidatorTest {

    @Test
    public void hasErrorsCachesFieldErrors() {
        FieldValidator fieldValidator = mock(FieldValidator.class);
        when(fieldValidator.getErrors()).thenReturn(HashMultimap.create());

        DataValidator dataValidator = new DataValidator(fieldValidator);
        dataValidator.hasErrors();
        dataValidator.hasErrors();

        verify(fieldValidator, times(1)).getErrors();
    }

    @Test(expected = IllegalStateException.class)
    public void hasErrorsMustBeCalledBeforeErrorsAsJson() {
        DataValidator dataValidator = new DataValidator();
        dataValidator.errorsAsJson();
    }
}
