package csw.time;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.typed.ActorContext;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.Adapter;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import csw.time.api.models.UTCTime;
import csw.time.client.api.Cancellable;
import csw.time.client.api.TimeServiceScheduler;
import csw.time.client.TimeServiceSchedulerFactory;

import java.time.Duration;

public class JSchedulerExamples {

    private ActorContext<UTCTime> ctx;
    private UTCTime utcTime = UTCTime.now();
    TimeServiceScheduler scheduler;

    public JSchedulerExamples() {
        //#create-scheduler
        // create time service scheduler using the factory method
        ActorSystem actorSystem = Adapter.toUntyped(ctx.asJava().getSystem());
        TimeServiceScheduler scheduler = TimeServiceSchedulerFactory.make(actorSystem);
        //#create-scheduler

        this.scheduler = scheduler;
    }


    void scheduleOnce() {
        UTCTime utcTime = UTCTime.now();

        // #schedule-once
        Runnable task = () -> {/* do something*/};
        scheduler.scheduleOnce(utcTime, task);
        // #schedule-once
    }

    // #schedule-once-with-actorRef
    class SchedulingHandler extends AbstractBehavior<UTCTime> {
        @Override
        public Receive<UTCTime> createReceive() {
            // handle the message to execute the task on scheduled time
            return null;
        }
    }

    Cancellable schedule() {
        Behavior<UTCTime> behavior = Behaviors.setup(ctx -> new SchedulingHandler());
        ActorRef actorRef = Adapter.toUntyped(ctx.asJava().spawnAnonymous(behavior));

        return scheduler.scheduleOnce(utcTime, actorRef, UTCTime.now());
    }
    // #schedule-once-with-actorRef

    void schedulePeriodically() {
        // #schedule-periodically
        // #schedule-periodically-with-startTime
        Runnable task = () -> {/* do something*/};
        // #schedule-periodically-with-startTime
        scheduler.schedulePeriodically(Duration.ofMillis(50), task);
        // #schedule-periodically

        Runnable runnable = () -> {/* do something*/};
        // #schedule-periodically-with-startTime
        scheduler.schedulePeriodically(utcTime, Duration.ofMillis(50), task);
        // #schedule-periodically-with-startTime
    }

}
