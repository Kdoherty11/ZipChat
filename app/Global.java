import com.google.inject.Guice;
import com.google.inject.Injector;
import play.GlobalSettings;

/**
 * Created by kdoherty on 7/2/15.
 */
public class Global extends GlobalSettings {

    private static final Injector INJECTOR = createInjector();

    @Override
    public <T> T getControllerInstance(Class<T> controllerClazz) {
//        if (controllerClazz.getSuperclass() == Action.class) {
//            return null;
//        }

        return INJECTOR.getInstance(controllerClazz);
    }
    private static Injector createInjector() {
        return Guice.createInjector();
    }

}
