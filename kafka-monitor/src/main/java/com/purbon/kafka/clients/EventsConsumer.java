package com.purbon.kafka.clients;

import java.io.IOException;
import java.time.Duration;
import java.util.Collections;
import java.util.Properties;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.codehaus.jackson.map.ObjectMapper;

public class EventsConsumer {


  private final String kafkaServers;
  private ObjectMapper mapper = new ObjectMapper();

  public EventsConsumer(String kafkaServers) {
    this.kafkaServers = kafkaServers;
  }



  private static Properties configure(String kafkaServers) {
    Properties props = new Properties();
    props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaServers);
    props.put(ConsumerConfig.GROUP_ID_CONFIG, "my-consumer-group");
    props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.LongDeserializer");
    props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
    return props;
  }

  public KafkaConsumer<Long, String> consumer() {
    return  new KafkaConsumer<>(configure(kafkaServers));
  }

  public CarEvent stringAsCarEvent(String jsonData) throws IOException {
   return  mapper.readValue(jsonData, CarEvent.class);
  }

  public static void main(String[] args) {

    String kafkaServers = "kafka:9092";
    if (args.length > 1)
      kafkaServers = args[1];

    KafkaConsumer<Long, String> consumer = new KafkaConsumer<>(configure(kafkaServers));
    consumer.subscribe(Collections.singletonList("my-topic"));

    while(true) {
      ConsumerRecords<Long, String> records = consumer.poll(Duration.ofMillis(1000));

      records.forEach(consumerRecord -> {
        System.out.println(consumerRecord.key()+" "+consumerRecord.value());
      });

      System.out.println("Records fetched: "+records.count());

      consumer.commitSync();
    }

  }
}
