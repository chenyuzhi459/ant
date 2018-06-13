package io.sugo.kafka;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.sugo.http.Configure;
import io.sugo.kafka.factory.KafkaFactory;
import org.apache.commons.lang.StringUtils;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.TopicPartition;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import java.util.concurrent.ExecutionException;

public class KafkaHandler  {

  private static final Logger LOG = LogManager.getLogger(KafkaHandler.class);

//  public static Configure configure;



  private KafkaConsumer kafkaConsumer;


  public static KafkaHandler kafkaHandler = new KafkaHandler();

  public static KafkaHandler getKafkaHandler()  {

    return kafkaHandler;
  }

  private KafkaHandler()  {

  }

  public static void main(String[] args) throws ExecutionException {
    Configure.initConfigPath(args.length > 0 ? args[0] : "src/main/resources/config/");
    KafkaHandler api = new KafkaHandler();
//    api.printTopic();
//    api.printTopicPartition();

    api.printTopicPartition("com_H1bIzqK2SZ_project_HyAKaKC5Z");
//    api.printTopicPartitionOffset();
  }


  public void printTopicPartition(String topic) {
    try{
      KafkaConsumer consumer = KafkaFactory.getConsumer("[192.168.0.220:9092, 192.168.0.221:9092, 192.168.0.222:9092]");

      List<PartitionInfo> partitions = consumer.partitionsFor(topic);
      for (PartitionInfo info : partitions) {
        TopicPartition tp = new TopicPartition(info.topic(), info.partition());
        List<TopicPartition> topicPartitions = new ArrayList<>();
        topicPartitions.add(tp);
        consumer.assign(topicPartitions);
        consumer.seekToBeginning(topicPartitions);
        long startOffset = 0;
        for (TopicPartition topicPartition : topicPartitions) {
          startOffset = consumer.position(topicPartition);
        }
        consumer.seekToEnd(topicPartitions);
        for (TopicPartition topicPartition : topicPartitions) {
          long endOffset = consumer.position(topicPartition);
          System.out.println(String.format("Partition{topic:%s, partition:%d, start offset:%,d, end offset:%,d}",
                  info.topic(), topicPartition.partition(), startOffset, endOffset));
        }
      }
    }finally {

    }

  }

  public void printTopic() {
    try{
      KafkaConsumer consumer = KafkaFactory.getConsumer("[192.168.0.220:9092, 192.168.0.221:9092, 192.168.0.222:9092]");
      System.out.println("-------------printTopic---------------");
      Map<String, List<PartitionInfo>> topicMap = consumer.listTopics();
      for (Map.Entry<String, List<PartitionInfo>> entry : topicMap.entrySet()) {
        System.out.println(String.format("topic:%s, partitions:%d", entry.getKey(), entry.getValue().size()));
        for (PartitionInfo info : entry.getValue()) {
          System.out.println(String.format("PartitionInfo{partition:%d, inSyncReplicas:%s, leader:%s, replicas:%s}",
                  info.partition(), info.inSyncReplicas(), info.leader(), info.replicas()));
        }
        System.out.println();
      }
    }finally {

    }

  }

  public void printTopicPartition() {
    try{
      KafkaConsumer consumer = KafkaFactory.getConsumer("[192.168.0.220:9092, 192.168.0.221:9092, 192.168.0.222:9092]");
      System.out.println("-------------printTopicPartition---------------");
      Map<String, List<PartitionInfo>> topicMap = consumer.listTopics();
      for (String topic : topicMap.keySet()) {
        System.out.println("topic:" + topic);
        List<PartitionInfo> partitions = consumer.partitionsFor(topic);
        for (PartitionInfo info : partitions) {
          System.out.println(String.format("PartitionInfo{partition:%d, inSyncReplicas:%s, leader:%s, replicas:%s}",
                  info.partition(), info.inSyncReplicas(), info.leader(), info.replicas()));
        }
      }
    }finally {

    }

  }

  public Map getTopicPartitionOffset(String topic, List<Integer> partitionIds,String servers) {
    try {
      if(StringUtils.isBlank(servers)){
        servers = Configure.getConfigure().getProperty("kafka.properties","bootstrap.servers","192.168.0.223:9092,192.168.0.224:9092,192.168.0.225:9092");
      }
      String[] kafkaServers = servers.split(",");
      Arrays.sort(kafkaServers);
//      kafkaConsumer = KafkaFactory.getFactory(configure).getConsumer(Arrays.toString(kafkaServers));
//      consumerHandler = KafkaFactory.getFactory(configure).getConsumer(Arrays.toString(kafkaServers));
//      consumerHandler.lock();
      KafkaConsumer consumer = KafkaFactory.getConsumer(Arrays.toString(kafkaServers));
      synchronized (consumer){
        List<PartitionInfo> partitions = consumer.partitionsFor(topic);
        List<Map<String,Object>> retPartitions = Lists.newArrayList();

        for (PartitionInfo info : partitions) {
          if(!partitionIds.contains(info.partition())){
            continue;
          }
          TopicPartition tp = new TopicPartition(info.topic(), info.partition());
          List<TopicPartition> topicPartitions = new ArrayList<>();
          topicPartitions.add(tp);
          consumer.assign(topicPartitions);
          consumer.seekToBeginning(topicPartitions);
          long startOffset = 0;
          for (TopicPartition topicPartition : topicPartitions) {
            startOffset = consumer.position(topicPartition);
          }
          consumer.seekToEnd(topicPartitions);
          for (TopicPartition topicPartition : topicPartitions) {
            long endOffset = consumer.position(topicPartition);
            Map<String,Object> partitionMap = Maps.newLinkedHashMap();
            partitionMap.put("partition",topicPartition.partition());
            partitionMap.put("startOffset",startOffset);
            partitionMap.put("endOffset",endOffset);
            retPartitions.add(partitionMap);
          }

        }
        return ImmutableMap.of("topic",topic,"partitions",retPartitions);
      }

    } finally {
//      consumerHandler.clear();
    }

  }

  public void printTopicPartitionOffset() {
    try{
      KafkaConsumer consumer = KafkaFactory.getConsumer("[192.168.0.220:9092, 192.168.0.221:9092]");
      Map<String, List<PartitionInfo>> topicMap = consumer.listTopics();
      for (Map.Entry<String, List<PartitionInfo>> entry : topicMap.entrySet()) {
        System.out.println(String.format("topic:%s, partitions:%d", entry.getKey(), entry.getValue().size()));

        for (PartitionInfo info : entry.getValue()) {
          TopicPartition tp = new TopicPartition(entry.getKey(), info.partition());
          List<TopicPartition> partitions = new ArrayList<>();
          partitions.add(tp);   //只有一个partition,为何还要用list?
          consumer.assign(partitions);
          consumer.seekToBeginning(partitions);
          long startOffset = 0;
          for (TopicPartition partition : partitions) {
            startOffset = consumer.position(partition);
          }
          consumer.seekToEnd(partitions);
          for (TopicPartition partition : partitions) {
            long endOffset = consumer.position(partition);
            System.out.println(String.format("Partition{topic:%s, partition:%d, start offset:%,d, end offset:%,d}",
                    entry.getKey(), partition.partition(), startOffset, endOffset));
          }
        }

        System.out.println();
      }

    }finally {

    }

  }




  //  private void assignPartitions() {
  //    List<TopicPartition> topicPartitions = new ArrayList<>();
  //    topicPartitions.add(new TopicPartition(topic, partition));
  //    consumerHandler.assign(topicPartitions);
  //    logger.info("Thread:" + threadNum + " Seeking partition[" + partition + "] to offset[" + offset + "].");
  //    consumerHandler.seek(new TopicPartition(topic, partition), offset);
  //  }

  //  public void loadToFile() throws IOException {
  //    logger.info("Thread:" + threadNum + " begin to load data from kafka to file");
  //    logger.info("Thread:" + threadNum + " topic:" + topic + ", partition:" + partition);
  //    boolean reading = true;
  //
  //    ConsumerRecords<byte[], byte[]> records = ConsumerRecords.empty();
  //    File path = new File(dataPath);
  //    if (!path.exists()) {
  //      path.mkdirs();
  //    }
  //    FileOutputStream fos = null;
  //    BufferedOutputStream bos = null;
  //    ObjectOutputStream oos = null;
  //
  //    int recordCount = 0;
  //    long start = System.currentTimeMillis();
  //    int logIndex = 1;
  //    long dataSize = 0;
  //    long refresh = new DateTime().plusMinutes(1).getMillis();
  //    try {
  //      currentFile = new File(path, "data-" + fileIndex++);
  //      fos = new FileOutputStream(currentFile);
  //      bos = new BufferedOutputStream(fos);
  //
  //      long lastRecordCount = 0;
  //      boolean waiting = true;
  //      while (reading) {
  //        records = consumerHandler.poll(POLL_TIMEOUT);
  //        for (ConsumerRecord<byte[], byte[]> record : records) {
  //          recordCount++;
  //          offsetMap.put(record.partition(), record.offset());
  //          final byte[] valueBytes = record.value();
  //          if (valueBytes == null) {
  //            throw new RuntimeException("null value");
  //          }
  //          bos.create(valueBytes, 0, valueBytes.length);
  //          bos.create(LINE);
  //          if (currentFile.length() > MAX_FILE_LEN) {
  //            bos.close();
  //            dataSize += currentFile.length();
  //            currentFile = new File(path, "data-" + fileIndex++);
  //            fos = new FileOutputStream(currentFile);
  //            bos = new BufferedOutputStream(fos);
  //          }
  //        }
  //        if (System.currentTimeMillis() > refresh) {
  //          logger.info("Thread:" + threadNum + " " + logIndex++ + " record count:" + recordCount + ", add record:" + (recordCount - lastRecordCount) + ", partition offsets:" + offsetMap);
  //          bos.flush();
  //          refresh = new DateTime().plusMinutes(1).getMillis();
  //          if (lastRecordCount == recordCount) {
  //            refresh = new DateTime().plusSeconds(10).getMillis();
  //            if (waiting) {
  //              waiting = false;
  //            } else {
  //              logger.info("Thread:" + threadNum + " no record for 10s, stop reading");
  //              break;
  //            }
  //          }
  //          lastRecordCount = recordCount;
  //        }
  //      }
  //    } finally {
  //      if (bos != null) {
  //        bos.close();
  //      }
  //      dataSize += currentFile.length();
  //      logger.info("Thread:" + threadNum + " total record count:" + recordCount + ", files:" + fileIndex
  //          + ", data size:" + String.format("%,d", dataSize)
  //          + ", spend time:" + ((System.currentTimeMillis() - start) / 1000) + ", partition offsets:" + offsetMap);
  //      if (consumerHandler != null) {
  //        consumerHandler.close();
  //      }
  //      latch.countDown();
  //    }
  //  }



}
