package org.graylog.plugins.kafka;

import org.graylog2.plugin.PluginModule;

import java.util.Set;

/**
 * Extend the PluginModule abstract class here to add you plugin to the system.
 */
public class KafkaModule extends PluginModule {
    /**
     * Returns all configuration beans required by this plugin.
     * <p>
     * Implementing this method is optional. The default method returns an empty {@link Set}.
     */

    @Override
    protected void configure() {
        addTransport("Kafka in-out plugin", KafkaTransport.class);
        addMessageOutput(KafkaOutput.class,KafkaOutput.Factory.class);
    }
}
