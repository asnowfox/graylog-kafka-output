package org.graylog.plugins.kafka;

import org.graylog2.plugin.PluginMetaData;
import org.graylog2.plugin.ServerStatus;
import org.graylog2.plugin.Version;

import java.net.URI;
import java.util.Collections;
import java.util.Set;

/**
 * Implement the PluginMetaData interface here.
 */
public class KafkaMetaData implements PluginMetaData {
    private static final String PLUGIN_PROPERTIES = "org.graylog.plugins.graylog-plugin-kafka/graylog-plugin.properties";

    @Override
    public String getUniqueId() {
        return "org.graylog.plugins.kafka.KafkaPlugin";
    }

    @Override
    public String getName() {
        return "KafkaPlugin";
    }

    @Override
    public String getAuthor() {
        return "fbalicchia <fbalicchia@gmail.com>";
    }

    @Override
    public URI getURL() {
        return URI.create("https://github.com/fbalicchia/graylog-plugin-kafka-inout.git");
    }

    @Override
    public Version getVersion() {
        return Version.fromPluginProperties(getClass(), PLUGIN_PROPERTIES, "version", Version.from(0, 0, 0, "unknown"));
    }

    @Override
    public String getDescription() {
        // TODO Insert correct plugin description
        return "Description of Kafka plugin";
    }

    @Override
    public Version getRequiredVersion() {
        return Version.fromPluginProperties(getClass(), PLUGIN_PROPERTIES, "graylog.version", Version.from(0, 0, 0, "unknown"));
    }

    @Override
    public Set<ServerStatus.Capability> getRequiredCapabilities() {
        return Collections.emptySet();
    }
}
