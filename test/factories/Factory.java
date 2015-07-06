package factories;

import java.util.List;
import java.util.Set;

/**
 * Created by kdoherty on 7/5/15.
 */
public interface Factory<T> {

    T create(PropOverride... propOverrides) throws IllegalAccessException, InstantiationException;
    List<T> createList(int size, PropOverride... propOverrides) throws InstantiationException, IllegalAccessException;
    Set<T> createSet(int size, PropOverride... propOverrides) throws InstantiationException, IllegalAccessException;

}
