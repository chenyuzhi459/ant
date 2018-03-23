package io.sugo.http.test;


import io.sugo.http.jersey.JerseyClientFactory;
import io.sugo.http.util.HttpMethod;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import java.io.*;
import java.util.*;

public class CompareResult {
    private static Client client = JerseyClientFactory.create();
    private static HttpMethod httpMethod = new HttpMethod(client);
    private static WebTarget target;

    public static void main(String[] args) throws IOException {

        FileWriter fw = new FileWriter("kafka_data");


//        Map<String, String> kafkaRecords = new HashMap<>();

        getKafkaData(3,7426961, 9507021, fw);
        getKafkaData(9, 7426932, 9506983, fw);

        fw.close();

//        getDataSourceData("2018-03-13T00:00:00.000Z/2018-03-14T00:00:00.000Z", "2018-03-13T03:20:26.575Z", "50", kafkaRecords);
//        getDataSourceData("2018-03-13T00:00:00.000Z/2018-03-14T00:00:00.000Z", "2018-03-13T03:20:26.575Z", "51", kafkaRecords);

//        System.out.println("total kafka record count:" + kafkaRecords.size());
    }


    private static void getKafkaData(int partition, int startOffset, int endOffset, FileWriter fw ) throws IOException {
//        partitionOffsetMap={3=7426961, 9=7426932}
//        partitionOffsetMap={9=9506983, 3=9507021}

        //Kafka consumer configuration settings
        String topicName = "stress_test_001";
        Properties props = new Properties();

        props.put("bootstrap.servers", "192.168.0.223:9092,192.168.0.224:9092,192.168.0.225:9092");
        props.put("group.id", "partitiontest001");
        props.put("enable.auto.commit", "true");
        props.put("auto.commit.interval.ms", "1000");
        props.put("session.timeout.ms", "30000");
//        props.put("auto.offset.reset", "earliest");

        //要发送自定义对象，需要指定对象的反序列化类
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        KafkaConsumer<String, Object> consumer = new KafkaConsumer<>(props);

        TopicPartition p = new TopicPartition(topicName, partition);
        consumer.assign(Arrays.asList(p));
        consumer.seek(p, startOffset);

        int count = endOffset - startOffset;
        final String TAB = "\t";
        final String COLON = ":";
        final String NEWLINE = "\r\n";
        for(int i=0; i<count; i++) {
            ConsumerRecords<String, Object> records = consumer.poll(100);
            for (ConsumerRecord<String, Object> record : records){
                String result = record.toString();
                String offset = result.split("offset = ")[1].split(",")[0];
                String s1 = result.split("\"s\\|s1\":\"")[1].split("\"")[0];
//                String timeStamp = result.split("\"d\\|event_time\":")[1].split(",")[0];
//                System.out.println(offset + "-" +  s1 + "-" + timeStamp);
                StringBuffer buffer = new StringBuffer();
                buffer.append(s1).append(TAB).append(partition).append(COLON).append(offset).append(NEWLINE);
                fw.append(buffer.toString());
            }
            if(i % 1000 == 0) {
                System.out.println("consume kafka count:" + i);
            }
        }
    }



    private static void getDataSourceData(String itvl, String ver, String part, Map<String, String> kafkaRecords) {
//        lucene_index_kafka_stress_test_dataSource_200_477e86c797f42ea_chbgfljj

//        lucene_index_kafka_stress_test_dataSource_200_477e86c797f42ea_3=[stress_test_dataSource_200_2018-03-13T00:00:00.000Z_2018-03-14T00:00:00.000Z_2018-03-13T03:20:26.575Z_50
//  lucene_index_kafka_stress_test_dataSource_200_477e86c797f42ea_9=[stress_test_dataSource_200_2018-03-13T00:00:00.000Z_2018-03-14T00:00:00.000Z_2018-03-13T03:20:26.575Z_51

        String node = getNode(itvl, ver, part);

        String url = "http://192.168.0."+ node + ":8083/druid/v2";
        target = client.target(url);

        StringBuffer bodyBuff = new StringBuffer();
        bodyBuff.append("{\n" +
                "    \"queryType\": \"lucene_scan\",\n" +
                "    \"dataSource\": \"stress_test_dataSource_200\",\n" +
                "    \"resultFormat\": \"compactedList\",\n" +
                "    \"batchSize\": 1,\n" +
                "    \"columns\": [\n" +
                "        \"s1\"\n" +
                "    ],\n" +
                "    \"intervals\":  {\n" +
                "    \t\"type\": \"segments\",\n" +
                "    \t\"segments\":[{\"itvl\":\"")
                .append(itvl).append("\",\"ver\":\"")
                .append(ver).append("\",\"part\":")
                .append(part).append("}]\t\n" +
                        "    }\n" +
                        "}");




        Response response = httpMethod.post(target, bodyBuff.toString());
        InputStream stream = response.readEntity(InputStream.class);

        JsonObjectIterator iterator = new JsonObjectIterator(stream);

        while (iterator.hasNext()) {
            HashMap resultValue = iterator.next();
            if (resultValue != null) {
                 ArrayList events = (ArrayList) resultValue.get("events");
                 ArrayList values = (ArrayList) events.get(0);
//                 String timesstamp = values.get(0).toString();
                 String s1 = values.get(1).toString();
//                 System.out.println(timesstamp + "\t" + s1);
                if(kafkaRecords.containsKey(s1)) {
                    kafkaRecords.remove(s1);
                }
            }
        }
    }

    private static String getNode(String itvl, String ver, String part) {
        StringBuffer nodeQueryBuff = new StringBuffer();
        nodeQueryBuff.append("http://192.168.0.225:8081/druid/coordinator/v1/datasources/stress_test_dataSource_200/segments")
                .append("/stress_test_dataSource_200_")
                .append(itvl.replaceAll("/","_"))
                .append("_").append(ver.replaceAll("_",":"));
        if(!part.equals("0")) {
            nodeQueryBuff.append("_").append(part);
        }
        Response nodeRes =  httpMethod.get(client.target(nodeQueryBuff.toString()));
        String nodeString = nodeRes.readEntity(String.class);
        String node = nodeString.split(".sugo.net")[0].split("dev")[1];

        return node;
    }

}
