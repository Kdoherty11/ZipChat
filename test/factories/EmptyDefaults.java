package factories;

import java.util.Collections;
import java.util.Map;

/**
 * Created by kevin on 6/23/15.
 */
public class EmptyDefaults extends FactoryDefaults{
    @Override
    public Map<String, Object> getDefaults() {
        return Collections.emptyMap();
    }
}
