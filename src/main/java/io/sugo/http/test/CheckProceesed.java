package io.sugo.http.test;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.*;
import io.sugo.http.jersey.JerseyClientFactory;
import io.sugo.http.util.HttpMethod;
import org.joda.time.DateTime;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import java.sql.*;
import java.text.MessageFormat;
import java.util.*;

public class CheckProceesed {
    private static final Client client = JerseyClientFactory.create();
    private static final HttpMethod httpMethod = new HttpMethod(client);
    private static WebTarget target;

//    http://192.168.0.225:8090/druid/indexer/v1/task/lucene_index_kafka_stress_test_dataSource_101_a66702d777acbaf_adjfnllh/log

    public static void main(String[] args) throws SQLException {

        List<String> idList = new ArrayList<>();
        Set<String> times = new TreeSet<>();

        Connection c = null;
        try {
            Class.forName("org.postgresql.Driver");
            c = DriverManager.getConnection("jdbc:postgresql://192.168.0.224:15432/druid", "postgres", "123456");
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println(e.getClass().getName()+": "+e.getMessage());
            System.exit(0);
        }
        System.out.println("Opened database successfully");

        Statement stmt = c.createStatement();
        String sql = "select id from druid_tasks where datasource = 'com_r1aJyE6dM_project_HJSPKg2YG'";
        ResultSet rs = stmt.executeQuery(sql);
        while (rs.next()) {
            idList.add(rs.getString(1));
        }
        System.out.println("total task count:" + idList.size());

        rs.close();


//        String url = "http://192.168.0.225:8090/druid/indexer/v1/completeTasks/custom/list";
//        target = client.target(url);
//        String param = "{\"taskPageItem\":{\"offset\":0,\"size\":1000},\"taskSortItem\":{\"sortDimension\":\"created_date\",\"sortDirection\":\"DESC\"},\"taskId\":\"stress_test_dataSource_101\"}";
//        Response rep = httpMethod.post(target, param);
//        String entity = rep.readEntity(String.class);
//
//        JSONArray jsonArray = JSONObject.parseArray(entity);
//        System.out.println("total task count:" + jsonArray.size());
//
//        Iterator iterator = jsonArray.iterator();
//
//        while (iterator.hasNext()) {
//            JSONObject jsonObject = (JSONObject) iterator.next();
//            idList.add( (String) jsonObject.get("id"));
//        }


        long processed = 0l, thrown = 0l, unparse = 0l, consume = 0l, produce = 0l;
        Map<String, Long> pMap = new HashMap<>();
        Map<String, RangeMap<Long, String>> allRangeMap = new HashMap<>();
        Map<String, RangeSet<Long>> rangeSetMap = new HashMap<>();

        Iterator<String> idIterator = idList.iterator();
        while (idIterator.hasNext()) {
            String id = idIterator.next();

            StringBuffer buff = new StringBuffer();
            buff.append("http://192.168.0.225:8090/druid/indexer/v1/task/").append(id).append("/log");
            target = client.target(buff.toString());
            Response response = httpMethod.get(target);

            String logStr = response.readEntity(String.class);

            // processed:982,669,587, unparseable:0, thrownAway:0
            long curProcessed = 0l, curUnparse = 0l, curThrown = 0l;
            String[] strArr1 =  logStr.split("Finished read kafka, ");
            if(strArr1.length > 1) {
                String target = strArr1[1].split("\n")[0];
                String formatStr = target.replace("processed:","").replaceAll(" unparseable:","").replaceAll(" thrownAway:","");
                String[] nums = formatStr.split(",");
                curProcessed = Long.parseLong(nums[0]);
                curUnparse = Long.parseLong(nums[1]);
                curThrown = Long.parseLong(nums[2]);

                processed += curProcessed;
                unparse += curUnparse;
                thrown += curThrown;
            }

            Long curConsume = 0l, curProduce = 0l;
            // consume:976,091,219
//            String[] strArr2 = logStr.split("\"metric\":\"ingest/index/consume\",\"value\":");
//            if(strArr2.length > 1) {
//                String target = strArr2[strArr2.length-1].split(",")[0];
//                curConsume = Long.parseLong(target);
//                consume += curConsume;
//            }

            //produce:976,151,238
//            String[] strArr3 = logStr.split("\"metric\":\"ingest/index/produce\",\"value\":");
//            if(strArr3.length > 1) {
//                String target = strArr3[strArr3.length-1].split(",")[0];
//                curProduce = Long.parseLong(target);
//                produce += curProduce;
//            }

            //
            long totalProcessedCount = 0l;
            String[] strArr4 = logStr.split("endOffsets changed to ");
            if(strArr4.length > 1) {
                String endPartition = strArr4[strArr4.length-1].split("\n")[0].replaceAll("=",":");
                JSONObject endJson = JSONObject.parseObject(endPartition);

                String startPartition = logStr.split("\"partitionOffsetMap\" : ")[1].split("},\n")[0];
                JSONObject startJson = JSONObject.parseObject(startPartition);


                Iterator<String> keyIt = endJson.keySet().iterator();
                while(keyIt.hasNext()) {
                    String key = keyIt.next();
                    long endLoc = Long.valueOf(String.valueOf(endJson.get(key)));
                    long startLoc = Long.valueOf(String.valueOf(startJson.get(key)));

                    long processedCount = endLoc - startLoc;
                    if(processedCount > 0) {
                        totalProcessedCount += processedCount;
                        if(pMap.containsKey(key)) {
                            pMap.put(key, pMap.get(key) + processedCount);
                        } else{
                            pMap.put(key, processedCount);
                        }


                        // rangeMap
                        RangeMap<Long, String> rangeMap;
                        if(allRangeMap.containsKey(key)) {
                            rangeMap = allRangeMap.get(key);
                        } else {
                            rangeMap = TreeRangeMap.create();
                        }
                        rangeMap.put(Range.closedOpen(startLoc, endLoc), id);
                        allRangeMap.put(key, rangeMap);

                        // rangeSet
                        RangeSet<Long> rangeSet;
                        if(rangeSetMap.containsKey(key)) {
                            rangeSet = rangeSetMap.get(key);
                        } else {
                            rangeSet = TreeRangeSet.create();
                        }
                        rangeSet.add(Range.closedOpen(startLoc, endLoc));
                        rangeSetMap.put(key, rangeSet);
                    }
                }
            }

            // 如果 kafka offset 的偏移 不等于 task 处理的条数
//            if(totalProcessedCount != curProcessed + curThrown + curUnparse) {
//                System.out.println(id);
//            }
        }

        long processedKafka = 0l;
        for(Long current : pMap.values()) {
            processedKafka += current;
        }

        System.out.println(MessageFormat.format("processed:{0}, unparseable:{1}, thrownAway:{2}", processed, unparse, thrown));
        System.out.println(MessageFormat.format("consume:{0}, produce:{1}", consume, produce));
        System.out.println(MessageFormat.format("kafka offset processed:{0}", processedKafka));

        Map<String, Range<Long>> partitionOffsets = new HashMap<>();
        partitionOffsets.put("0", Range.closed(0l, 81026722l));
        partitionOffsets.put("1", Range.closed(0l, 83289549l));
        partitionOffsets.put("2", Range.closed(0l, 83289556l));
        partitionOffsets.put("3", Range.closed(0l, 81026720l));
        partitionOffsets.put("4", Range.closed(0l, 83289547l));
        partitionOffsets.put("5", Range.closed(0l, 83289555l));
        partitionOffsets.put("6", Range.closed(0l, 81026725l));
        partitionOffsets.put("7", Range.closed(0l, 83289547l));
        partitionOffsets.put("8", Range.closed(0l, 83289556l));
        partitionOffsets.put("9", Range.closed(0l, 81026716l));
        partitionOffsets.put("10", Range.closed(0l, 83289547l));
        partitionOffsets.put("11", Range.closed(0l, 83289556l));

//        for(String key : allRangeMap.keySet()) {
//            System.out.println(allRangeMap.get(key).subRangeMap(partitionOffsets.get(key)));
//        }
        long totalLack = 0l;

        for(String key : partitionOffsets.keySet()) {
            Set<Range<Long>> rangeSet = rangeSetMap.get(key).asRanges();
            Iterator<Range<Long>> rangeIt = rangeSet.iterator();

            Long partitionEndPoint =  partitionOffsets.get(key).upperEndpoint();
            long varLast = 0l;
            long varFirst = 0l;

            boolean first = true;
            while(rangeIt.hasNext()) {

                Range<Long> range = rangeIt.next();
                if(first) {
                    first = false;
                } else {
                    varFirst = range.lowerEndpoint();

                    totalLack += varFirst - varLast;
                }
                varLast = range.upperEndpoint();
                //
                if(partitionEndPoint != varLast ) {
                    getProblemTaskStartTime(times, varLast , key, stmt);
                }
            }
            totalLack += partitionEndPoint - varLast;
        }
        System.out.println(rangeSetMap);
        System.out.println("total lack: " + totalLack);
//        System.out.println(allRangeMap);

//        System.out.println("produce add lack: " +  (produce + totalLack));
//        System.out.println("consume add lack: " +  (consume + totalLack));
        System.out.println("kafka processed add lack:" + (processedKafka + totalLack));


        System.out.println(times);


        stmt.close();
        c.close();
    }

//        {0=[[0‥27881268), [29636286‥52187786), [54129915‥81026722)],
//        11=[[0‥83289556)],
//        1=[[0‥10213220), [12398572‥83289549)],
//        2=[[0‥74424979), [76659543‥83289556)],
//        3=[[0‥9407617), [10958016‥78789055)],
//        4=[[0‥39424650), [40881695‥83289547)],
//        5=[[0‥83289555)],
//        6=[[0‥27881275), [29636297‥52187789), [54129909‥81026725)],
//        7=[[0‥10213201), [12398567‥83289547)],
//        8=[[0‥74424876), [76659417‥83289556)],
//        9=[[0‥9407634), [10958025‥78789054)],
//        10=[[0‥39424718), [40881798‥83289547)]}

    // datasource total  990312960
    // kafka total 990423296

    private static void getProblemTaskStartTime(Set<String> times, Long offset, String partiton, Statement stmt) throws SQLException {
        StringBuffer buff = new StringBuffer();
        buff.append("select \"id\", \"created_date\" from druid_tasks where datasource = 'stress_test_dataSource_101' \n")
                .append("and  encode(payload::bytea,'escape') like '%")
                .append(String.format("\"partitionOffsetMap\":{\"%s\":%d", partiton, offset))
                .append("%'");

//        System.out.println(buff.toString());

        ResultSet rs = stmt.executeQuery(buff.toString());
        while (rs.next()) {
            DateTime dateTime = new DateTime(rs.getString("created_date"));
            times.add( "\n" + dateTime + "\t" + rs.getString("id") + "\t" + partiton + ":" + offset);
        }
    }
    //lucene_index_kafka_stress_test_dataSource_101_5bac36e2661b37a_flhkcdgp
    //lucene_index_kafka_stress_test_dataSource_101_5bac36e2661b37a_hbicccnb


}

//        2018年 03月 08日 星期四 14:21:58 CST stop middleManager
//        2018年 03月 08日 星期四 14:22:14 CST start middleManager
//        sleep
//        2018年 03月 08日 星期四 14:45:54 CST stop middleManager
//        2018年 03月 08日 星期四 14:46:10 CST start middleManager
//        sleep
//        2018年 03月 08日 星期四 15:17:22 CST stop middleManager
//        2018年 03月 08日 星期四 15:17:38 CST start middleManager
//        sleep
//        2018年 03月 08日 星期四 15:22:14 CST stop middleManager
//        2018年 03月 08日 星期四 15:22:30 CST start middleManager
//        sleep
//        2018年 03月 08日 星期四 15:46:10 CST stop middleManager
//        2018年 03月 08日 星期四 15:46:26 CST start middleManager
//        sleep
//        2018年 03月 08日 星期四 16:17:38 CST stop middleManager
//        2018年 03月 08日 星期四 16:17:54 CST start middleManager
//        sleep
//        2018年 03月 08日 星期四 16:22:30 CST stop middleManager
//        2018年 03月 08日 星期四 16:22:46 CST start middleManager
//        sleep
//        2018年 03月 08日 星期四 16:46:26 CST stop middleManager
//        2018年 03月 08日 星期四 16:46:42 CST start middleManager
//        sleep
//        2018年 03月 08日 星期四 17:17:54 CST stop middleManager
//        2018年 03月 08日 星期四 17:18:09 CST start middleManager
//        sleep
//        2018年 03月 08日 星期四 17:22:46 CST stop middleManager
//        2018年 03月 08日 星期四 17:23:01 CST start middleManager
//        sleep
//        2018年 03月 08日 星期四 17:46:42 CST stop middleManager
//        2018年 03月 08日 星期四 17:46:57 CST start middleManager

