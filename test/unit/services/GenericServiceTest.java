package unit.services;

import daos.GenericDao;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import services.GenericService;
import services.impl.GenericServiceImpl;

import static org.fest.assertions.Assertions.assertEquals;
import static org.mockito.Matchers.refEq;
import static org.mockito.Mockito.verify;

/**
 * Created by kdoherty on 7/8/15.
 */
@RunWith(MockitoJUnitRunner.class)
public class GenericServiceTest<T> {

    private GenericService<T> genericService;

    @Mock
    private GenericDao<T> genericDao;

    @Before
    public void setUp() {
        genericService = new GenericServiceImpl<>(genericDao);
    }

    @Test
    public void testConstructorDoesNotAllowNullDao() {
        boolean nullPointerThrown = false;
        try {
            genericService = new GenericServiceImpl<>(null);
        } catch (NullPointerException npe) {
            nullPointerThrown = true;
        }
        assertEquals(nullPointerThrown).isTrue();
    }

    @Test
    public void saveCallsThroughToDao() {
        Object saveObj = new Object();

        genericService.save(saveObj);

        verify(genericDao).save(refEq(saveObj));
    }

    @Test
    public void findByIdCallsThroughToDao() {
        long id = 11;

        genericService.findById(id);

        verify(genericDao).findById(id);
    }

    @Test
    public void removeCallsThroughToDao() {
        Object removeObj = new Object();

        genericService.remove(removeObj);

        verify(genericDao).remove(refEq(removeObj));
    }

}
