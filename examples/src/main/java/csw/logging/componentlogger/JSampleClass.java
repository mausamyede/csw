package csw.logging.componentlogger;

import akka.actor.typed.javadsl.ActorContext;
import csw.command.client.internal.messages.ComponentMessage;
import csw.logging.javadsl.ILogger;
import csw.logging.javadsl.JLoggerFactory;
import csw.logging.scaladsl.LoggerFactory;

//#component-logger-class
public class JSampleClass {

    public JSampleClass(JLoggerFactory loggerFactory) {
        ILogger log = loggerFactory.getLogger(getClass());
    }
}
//#component-logger-class

//#component-logger-actor
class JSampleActor extends akka.actor.AbstractActor {

    public JSampleActor(JLoggerFactory loggerFactory) {

        //context() is available from akka.actor.AbstractActor
        ILogger log = loggerFactory.getLogger(context(), getClass());
    }

    @Override
    public Receive createReceive() {
        return null;
    }
}
//#component-logger-actor

//#component-logger-typed-actor
class JSampleTypedActor {

    public JSampleTypedActor(JLoggerFactory loggerFactory, ActorContext<ComponentMessage> ctx) {
        ILogger log = loggerFactory.getLogger(ctx, getClass());
    }
}
//#component-logger-typed-actor

class JSample {

    public void dummyMethod() {
        //#logger-factory-creation
        JLoggerFactory jLoggerFactory = new JLoggerFactory("my-component-name");

        // convert a java JLoggerFactory to scala LoggerFactory
        LoggerFactory loggerFactory = jLoggerFactory.asScala();
        //#logger-factory-creation
    }
}