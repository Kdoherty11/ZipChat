import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Provides;
import play.GlobalSettings;
import play.libs.ws.WSClient;
import play.libs.ws.WS;

/**
 * Created by kdoherty on 7/2/15.
 */
public class Global extends GlobalSettings {

    private static final Injector INJECTOR = Guice.createInjector(new AbstractModule() {
        @Override
        protected void configure() {

        }

        @Provides
        WSClient provideWsClient() {
            return WS.client();
        }


    });

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
