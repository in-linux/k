package com.purbon.kafka.clients;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import org.apache.commons.lang.StringUtils;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.codehaus.jackson.map.ObjectMapper;

public class EventsProducer {

  private final String kafkaServers;
  private ObjectMapper mapper = new ObjectMapper();

  public EventsProducer(String kafkaServers) {
    this.kafkaServers = kafkaServers;
  }

  private Properties configure(String kafkaServers) {
    Properties props = new Properties();
    props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaServers);
    props.put(ProducerConfig.CLIENT_ID_CONFIG, "my-producer");
    props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.LongSerializer");
    props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
    return props;
  }

  public void load(List<CarEvent> events) throws IOException {

    KafkaProducer<Long, String> producer = new KafkaProducer<>(configure(kafkaServers));

    for(CarEvent event : events) {
      String value = mapper.writeValueAsString(event);
      Long k = event.getViewtime();
      ProducerRecord<Long, String> record = new ProducerRecord<>("my-topic", k, value);
      producer.send(record);
    }

    producer.flush();
  }

  public static void main(String[] args) throws Exception {

    String kafkaServers = "kafka:9092";
    if (args.length > 1)
      kafkaServers = args[1];

    EventsProducer producer = new EventsProducer(kafkaServers);
    String[] countries = new String[]{"DE", "UK"};

    int numberOfDocs = 100;
    ArrayList<CarEvent> docs = new ArrayList<CarEvent>();
    Random rand = new Random(System.currentTimeMillis());
    int total = 0;
    int count = 0;
    while(true) {
      for (int i = 0; i < numberOfDocs; i++) {
        System.out.println(i+" "+total);
        long viewTime = System.currentTimeMillis();
        String plateId = StringUtils.leftPad(String.valueOf(Math.abs(rand.nextLong()%100000000)), 8);
        String country = countries[rand.nextInt(countries.length)];
        String plate = plateId+"-"+country;
        CarEvent event = new CarEvent(viewTime, plate);
        docs.add(event);
        Thread.sleep(2);
      }
      producer.load(docs);
      total = total + numberOfDocs;
      System.out.println("Generated: "+total+ "docs");
      Thread.sleep(3000);
      count++;

    }
  }
}
