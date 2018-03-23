package io.sugo.http.test;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;

import java.util.Arrays;
import java.util.Properties;

public class KafkaConsume {
    private static final String SERVERS = "192.168.0.223:9092,192.168.0.224:9092,192.168.0.225:9092";
    private static final String TOPIC_NAME = "nanhang_test_015";
    private static final int PARTITION = 0;
    private static final int START_OFFSET = 70000000;
    private static final int RECORD_COUNT = 100;

    public static void main(String[] args) throws Exception {

        //Kafka consumer configuration settings
        Properties props = new Properties();

        props.put("bootstrap.servers", SERVERS);
        props.put("group.id", "partitiontest001");
        props.put("enable.auto.commit", "true");
        props.put("auto.commit.interval.ms", "1000");
        props.put("session.timeout.ms", "30000");
//        props.put("auto.offset.reset", "earliest");

        //要发送自定义对象，需要指定对象的反序列化类
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        KafkaConsumer<String, Object> consumer = new KafkaConsumer<>(props);

        TopicPartition p = new TopicPartition(TOPIC_NAME, PARTITION);
        consumer.assign(Arrays.asList(p));
//        consumer.seekToBeginning(Arrays.asList(p));//不改变当前offset
       consumer.seek(p, START_OFFSET);
//        consumer.seek(p, 5);

        for(int i=0; i<RECORD_COUNT; i++) {
            ConsumerRecords<String, Object> records = consumer.poll(1000);
            for (ConsumerRecord<String, Object> record : records){
                System.out.println(record.toString());
            }
        }

    }
}
