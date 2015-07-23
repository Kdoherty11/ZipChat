package factories;

/**
 * Created by kdoherty on 7/7/15.
 */
public interface ObjectMutator<T> {
    void apply(T t) throws IllegalAccessException, InstantiationException;
}
