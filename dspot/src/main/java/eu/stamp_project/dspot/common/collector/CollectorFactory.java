package eu.stamp_project.dspot.common.collector;

import eu.stamp_project.dspot.common.collector.mongodb.MongodbCollector;
import eu.stamp_project.dspot.common.configuration.UserInput;
import eu.stamp_project.dspot.common.collector.smtp.EmailSender;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 03/10/19
 */
public class CollectorFactory {

    public static Collector build(UserInput configuration, EmailSender emailSender) {
        switch (configuration.getCollector()) {
            case MongodbCollector:
                return buildMongodbCollector(configuration, emailSender);
            case NullCollector:
            default:
                return new NullCollector();
        }
    }

    private static Collector buildMongodbCollector(UserInput configuration, EmailSender emailSender) {
        return new MongodbCollector(
                configuration.getMongoUrl(),
                configuration.getMongoDbName(),
                configuration.getMongoColName(),
                configuration.getRepoSlug(),
                configuration.getRepoBranch(),
                configuration.isRestFul(),
                emailSender
        );
    }

}
