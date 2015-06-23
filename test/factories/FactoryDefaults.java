package factories;

import com.github.javafaker.Faker;

import java.util.Map;

/**
 * Created by kevin on 6/23/15.
 */
public abstract class FactoryDefaults {

    protected Faker faker = new Faker();

    public abstract Map<String, Object> getDefaults();
}
