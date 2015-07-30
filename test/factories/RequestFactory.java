package factories;

import models.entities.Request;

/**
 * Created by kdoherty on 7/30/15.
 */
public class RequestFactory extends GenericFactory<Request> {

    public enum Trait implements ObjectMutator<Request> {
        WITH_SENDER {
            @Override
            public void apply(Request request) throws IllegalAccessException, InstantiationException {
                request.sender = new UserFactory().create();
            }
        },
        WITH_RECEIVER {
            @Override
            public void apply(Request request) throws IllegalAccessException, InstantiationException {
                request.sender = new UserFactory().create();
            }
        },
        WITH_SENDER_AND_RECEIVER {
            @Override
            public void apply(Request request) throws IllegalAccessException, InstantiationException {
                WITH_SENDER.apply(request);
                WITH_RECEIVER.apply(request);
            }
        }
    }
    
    public RequestFactory() {
        super(Request.class);
    }
}
