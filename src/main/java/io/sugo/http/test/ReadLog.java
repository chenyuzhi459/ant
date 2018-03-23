package io.sugo.http.test;

import io.sugo.http.jersey.JerseyClientFactory;
import io.sugo.http.util.HttpMethod;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import java.io.*;

public class ReadLog {
    static String logPath = "/tmp/druid_log/overlord.log";
    static Client client = JerseyClientFactory.create();
    static HttpMethod method = new HttpMethod(client);
    private static WebTarget target;
    private static int lack;
    private static final String DATASOURCE = "com_r1aJyE6dM_project_HkPpBRg9f";

    public static void main(String[] args) throws IOException {
        readLog();
    }

    public static void readLog() throws IOException {

        FileInputStream fis = new FileInputStream(logPath);
        InputStreamReader isr = new InputStreamReader(fis);
        BufferedReader br = new BufferedReader(isr);

        String line = null;
        int count = 0;
        while((line = br.readLine()) != null) {
            if(line.contains("Logging action for task")) {
                processedMessage(line);
                count++;
            }
        }
//        System.out.println(count);
        System.out.println("lack:" +lack);
    }

    private static void processedMessage(String message) throws IOException {
        String str1 = message.split("Logging action for task\\[")[1];
        String[] strArr1 = str1.split("]: ");

        String taskId = strArr1[0];
        String ver, part, itvl;
        int produce = 0, consume = 0;

        String[] dataSegments = strArr1[1].split("DataSegment");

        // 获取 task 日志里面 merge records count
        int mergeRecordCount = getMergeRecordCount(taskId);

        for(int i=1; i<dataSegments.length; i++) {
//            System.out.println(dataSegments[i]);
            String[] indexInterval = dataSegments[i].split("index.zip}, interval=");
            String[] indexArr = indexInterval[0].split("/");
            ver = indexArr[indexArr.length-2];
            part = indexArr[indexArr.length-1];
            itvl = indexInterval[1].split(",")[0];

            int curProduce = getProduceCount(taskId, itvl, ver, part);

            System.out.println(taskId + "\t" + ver + "_" + part + "\t" + itvl + "\t" + curProduce );
            produce += curProduce;

            // 统计 kafka offset 情况
            if(i == dataSegments.length-1) {
                String[] offsetMaps = indexInterval[1].split("partitionOffsetMap=\\{");

                int startOffsetTotal = 0, endOffsetTotal = 0;

                String offsetMap1 = offsetMaps[1].split("}")[0];
                String[] startOffsets = offsetMap1.split(", ");

                for(String startOffset : startOffsets) {
                    startOffsetTotal += Integer.parseInt(startOffset.split("=")[1]);
                }


                String offsetMap2 = offsetMaps[2].split("}")[0];
                String[] endOffsets = offsetMap2.split(", ");
                for(String endOffset : endOffsets) {
                    endOffsetTotal += Integer.parseInt(endOffset.split("=")[1]);
                }

                System.out.println(offsetMap1 + "\t" + offsetMap2);
                System.out.println("---------------------------------------------------------------------------------------------------------------");

                consume = endOffsetTotal - startOffsetTotal;
            }
        }

        if(produce != consume) {
            System.out.println(taskId + "\t consume:" + consume + "\tproduce:" + produce + "\tmerge record count:" + mergeRecordCount);
            lack += consume - produce;
            System.out.println("==============================================================================================================");
        }
    }

    private static int getMergeRecordCount(String taskId) {
        StringBuffer buff = new StringBuffer();
        buff.append("http://192.168.0.225:8090/druid/indexer/v1/task/").append(taskId).append("/log");
        target = client.target(buff.toString());
        Response response = method.get(target);

        String logStr = response.readEntity(String.class);

        String[] processStrs = logStr.split("merge records count is :");

        int totalCount = 0;

        for(int i=1; i<processStrs.length; i++) {
            int curCount = Integer.parseInt(processStrs[i].split("\n")[0]);
            System.out.print(curCount + "\t");
            totalCount += curCount;
        }

        System.out.println();

        return totalCount;
    }

    private static int getProduceCount(String taskId, String itvl, String ver, String part) throws IOException {

        StringBuffer nodeQueryBuff = new StringBuffer();
        nodeQueryBuff.append("http://192.168.0.225:8081/druid/coordinator/v1/datasources/").append(DATASOURCE).append("/segments")
                .append("/").append(DATASOURCE).append("_")
                .append(itvl.replaceAll("/","_"))
                .append("_").append(ver.replaceAll("_",":"));
        if(!part.equals("0")) {
            nodeQueryBuff.append("_").append(part);
        }
//        System.out.println(nodeQueryBuff);
        Response nodeRes =  method.get(client.target(nodeQueryBuff.toString()));
        String nodeString = nodeRes.readEntity(String.class);
        if(nodeString.length() == 0) {
            return 0;
        }
        String node = nodeString.split(".sugo.net")[0].split("dev")[1];

        target = client.target("http://192.168.0." + node + ":8083/druid/v2");

        StringBuffer bodyString = new StringBuffer();
        bodyString.append("{\n" +
                "    \"queryType\": \"lucene_timeseries\",\n" +
                "    \"dataSource\": \"").append(DATASOURCE).append("\",\n" +
                "    \"intervals\":  {\n" +
                "    \t\"type\": \"segments\",\n" +
                "    \t\"segments\":[{\"itvl\":\"")
                .append(itvl)
                .append("\",\"ver\":\"").append(ver.replaceAll("_",":"))
                .append("\",\"part\":").append(part)
                .append("}]\t\n" +
                        "    },\n" +
                        "    \"granularity\": \"all\",\n" +
                        "    \"context\": {\n" +
                        "        \"timeout\": 180000,\n" +
                        "        \"useOffheap\": true,\n" +
                        "        \"groupByStrategy\": \"v2\"\n" +
                        "    },\n" +
                        "    \"aggregations\": [\n" +
                        "        {\n" +
                        "            \"name\": \"__VALUE__\",\n" +
                        "            \"type\": \"lucene_count\"\n" +
                        "        }\n" +
                        "    ]\n" +
                        "}");

        target = client.target("http://192.168.0." + node + ":8083/druid/v2");
//        System.out.println(bodyString);
//        System.out.println("http://192.168.0." + node + ":8083/druid/v2");
        Response response =  method.post(target, bodyString.toString());
        String resStr = response.readEntity(String.class);
        int returnNum = Integer.parseInt(resStr.split("\"__VALUE__\":")[1].split("}")[0]);
        return returnNum;
    }
}

