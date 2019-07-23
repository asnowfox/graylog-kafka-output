package org.graylog.plugins.kafka;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.graylog2.plugin.Message;
import org.graylog2.plugin.configuration.Configuration;
import org.graylog2.plugin.configuration.ConfigurationRequest;
import org.graylog2.plugin.configuration.fields.*;
import org.graylog2.plugin.inputs.annotations.ConfigClass;
import org.graylog2.plugin.inputs.annotations.FactoryClass;
import org.graylog2.plugin.outputs.MessageOutput;
import org.graylog2.plugin.streams.Stream;
import org.joda.time.DateTime;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by fbalicchia on 14/05/17.
 */
public class KafkaOutput implements MessageOutput {

    private Logger LOG = LoggerFactory.getLogger(this.getClass());
    public static final String NAME = "Kafka Output";
    public static final String CK_BROKER_SERVER = "broker_server";
    public static final String CK_ACK = "ack";
    public static final String CK_RETRIES = "retries";
    public static final String CK_BATCH_SIZE = "batch_size";
    public static final String CK_LINGER_MS = "linger_ms";
    public static final String CK_BUFFER_MEMORY = "buffer_memory";
    public static final String CK_TOPIC = "TOPIC";

    private static final String CK_TLS_CLIENT_AUTH_TRUSTED_CERT_FILE = "tls_client_auth_cert_file";
    private static final String CK_TLS_CERT_FILE = "tls_cert_file";
    private static final String CK_TLS_KEY_FILE = "tls_key_file";
    private static final String CK_TLS_ENABLE = "tls_enable";
    private static final String CK_TLS_KEY_PASSWORD = "tls_key_password";
    private static final String CK_TLS_CLIENT_AUTH = "tls_client_auth";
    private static final String TLS_CLIENT_AUTH_DISABLED = "disabled";
    private static final String TLS_CLIENT_AUTH_OPTIONAL = "optional";
    private static final String TLS_CLIENT_AUTH_REQUIRED = "required";
    private static final Map<String, String> TLS_CLIENT_AUTH_OPTIONS = ImmutableMap.of(
            TLS_CLIENT_AUTH_DISABLED, TLS_CLIENT_AUTH_DISABLED,
            TLS_CLIENT_AUTH_OPTIONAL, TLS_CLIENT_AUTH_OPTIONAL,
            TLS_CLIENT_AUTH_REQUIRED, TLS_CLIENT_AUTH_REQUIRED);

    private static KafkaProducer<String, String> producer;
    private static Properties props;
    private final Configuration configuration;
    private final String topic;
    private final AtomicBoolean isRunning = new AtomicBoolean(false);

    @AssistedInject
    @Inject
    public KafkaOutput(@Assisted Stream stream , @Assisted Configuration configuration) {
        this.configuration = configuration;
        topic = configuration.getString(CK_TOPIC);
        props = new Properties();
        props.put("bootstrap.servers", configuration.getString(CK_BROKER_SERVER));
        props.put("acks", configuration.getString(CK_ACK));
        props.put("retries", configuration.getInt(CK_RETRIES));
        props.put("batch.size", configuration.getInt(CK_BATCH_SIZE));
        props.put("linger.ms", configuration.getInt(CK_LINGER_MS));
        props.put("buffer.memory", configuration.getInt(CK_BUFFER_MEMORY));
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        producer = new KafkaProducer<>(props);
        isRunning.getAndSet(true);
    }


    @Override
    public boolean isRunning() {
        return isRunning.get();
    }

    @Override
    public void write(Message message) throws Exception {

        JSONObject obj = new JSONObject(message.getFields());
        String finalObj = obj.remove("timestamp").toString();

        producer.send(new ProducerRecord<String, String>(topic, obj.toJSONString()), (recordMetaData, e) -> {

            if (e != null) {
                LOG.error("Error during publish to topic {} ", topic, e);
            } else {
//                LOG.info("Send message {} to  topic {} partition {} with offset {} ", message.getMessage(), topic, recordMetaData.partition(), recordMetaData.offset());
            }
        });

    }

    @Override
    public void write(List<Message> messages) throws Exception {
        messages.forEach(message -> {
            try {
                write(message);
            } catch (Exception e) {
                LOG.error("",e);
            }
        });
    }

    @Override
    public void stop() {
        producer.close();
        isRunning.compareAndSet(true,false);
    }

    @FactoryClass
    public interface Factory extends MessageOutput.Factory<KafkaOutput> {
        @Override
        KafkaOutput create(Stream stream, Configuration configuration);

        @Override
        Config getConfig();

        @Override
        Descriptor getDescriptor();
    }


    public static class Descriptor extends MessageOutput.Descriptor {
        public Descriptor() {
            super(NAME, false, "", "Output message send to Kafka");
        }
    }

    @ConfigClass
    public static class Config extends MessageOutput.Config {
        @Override
        public ConfigurationRequest getRequestedConfiguration() {
            ConfigurationRequest config = new ConfigurationRequest();

            config.addField(new TextField(
                    CK_BROKER_SERVER,
                    "Broker server",
                    "localhost:9092",
                    "A list host/port pair used to establishing the initial connection to the Kafka cluster",
                    ConfigurationField.Optional.NOT_OPTIONAL));
            config.addField(new TextField(
                    CK_TOPIC,
                    "Topic",
                    "graylog",
                    "Where data need to be send",
                    ConfigurationField.Optional.OPTIONAL));
            config.addField(new TextField(
                    CK_ACK,
                    "acks",
                    "all",
                    "Control under which request can be consider complete",
                    ConfigurationField.Optional.OPTIONAL));
            config.addField(new NumberField(
                    CK_RETRIES,
                    "retries",
                    0,
                    "Control producer semantics, If request fail can automatically retry",
                    ConfigurationField.Optional.OPTIONAL));
            config.addField(new NumberField(
                    CK_BATCH_SIZE,
                    "Batch size",
                    16384,
                    "",
                    ConfigurationField.Optional.NOT_OPTIONAL));

            config.addField(new NumberField(
                    CK_LINGER_MS,
                    "Linger ms",
                    1,
                    "",
                    ConfigurationField.Optional.NOT_OPTIONAL));

            config.addField(new NumberField(
                    CK_BUFFER_MEMORY,
                    "Buffer memory",
                    33554432,
                    "",
                    ConfigurationField.Optional.NOT_OPTIONAL));

            config.addField(
                    new TextField(
                            CK_TLS_CERT_FILE,
                            "TLS cert file",
                            "",
                            "Path to the TLS certificate file",
                            ConfigurationField.Optional.OPTIONAL
                    )
            );
            config.addField(
                    new TextField(
                            CK_TLS_KEY_FILE,
                            "TLS private key file",
                            "",
                            "Path to the TLS private key file",
                            ConfigurationField.Optional.OPTIONAL
                    )
            );
            config.addField(
                    new BooleanField(
                            CK_TLS_ENABLE,
                            "Enable TLS",
                            false,
                            "Accept TLS connections"
                    )
            );
            config.addField(
                    new TextField(
                            CK_TLS_KEY_PASSWORD,
                            "TLS key password",
                            "",
                            "The password for the encrypted key file.",
                            ConfigurationField.Optional.OPTIONAL,
                            TextField.Attribute.IS_PASSWORD
                    )
            );
            config.addField(
                    new DropdownField(
                            CK_TLS_CLIENT_AUTH,
                            "TLS client authentication",
                            TLS_CLIENT_AUTH_DISABLED,
                            TLS_CLIENT_AUTH_OPTIONS,
                            "Whether clients need to authenticate themselves in a TLS connection",
                            ConfigurationField.Optional.OPTIONAL
                    )
            );
            config.addField(
                    new TextField(
                            CK_TLS_CLIENT_AUTH_TRUSTED_CERT_FILE,
                            "TLS Client Auth Trusted Certs",
                            "",
                            "TLS Client Auth Trusted Certs  (File or Directory)",
                            ConfigurationField.Optional.OPTIONAL)
            );


            return config;

        }

    }


}
