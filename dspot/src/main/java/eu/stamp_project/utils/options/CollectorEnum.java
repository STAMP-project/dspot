package eu.stamp_project.utils.options;

import eu.stamp_project.utils.collector.DspotInformationCollector;
import eu.stamp_project.utils.collector.mongodb.MongodbCollector;
import eu.stamp_project.utils.collector.NullCollector;

// Pick the correct collector based on JSAP input.
public enum CollectorEnum {
    NullCollector {
        @Override
        public DspotInformationCollector getCollector() {
            return new NullCollector();
        }
    },
    MongodbCollector {
        @Override
        public DspotInformationCollector getCollector() {
            return new MongodbCollector();
        }
    };

    public abstract DspotInformationCollector getCollector();
}
