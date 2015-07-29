package utils;

import play.Application;
import play.ApplicationLoader;
import play.Environment;
import play.Mode;
import play.inject.guice.GuiceApplicationLoader;
import play.test.WithApplication;

import java.io.File;

/**
 * Created by kdoherty on 7/29/15.
 */
public class WithProductionApplication extends WithApplication {

    @Override
    protected Application provideApplication() {
        return new GuiceApplicationLoader().builder(new ApplicationLoader.Context(
                new Environment(
                        new File("."),
                        Environment.class.getClassLoader(),
                        Mode.PROD)))
                .build();
    }

}
