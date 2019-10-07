package eu.stamp_project.utils.collector;

import eu.stamp_project.utils.collector.mongodb.MongodbCollector;
import eu.stamp_project.utils.program.InputConfiguration;
import eu.stamp_project.utils.smtp.EmailSender;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 03/10/19
 */
public class CollectorFactory {

    public static Collector build(InputConfiguration configuration, EmailSender emailSender) {
        switch (configuration.getCollector()) {
            case MongodbCollector:
                return buildMongodbCollector(configuration, emailSender);
            case NullCollector:
            default:
                return new NullCollector();
        }
    }

    private static Collector buildMongodbCollector(InputConfiguration configuration, EmailSender emailSender) {
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
