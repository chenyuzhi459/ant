package io.sugo.common.utils;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import com.metamx.common.logger.Logger;
import io.sugo.services.usergroup.bean.rfm.DataBean;
import io.sugo.services.usergroup.bean.rfm.RFMDimensions;
import io.sugo.services.usergroup.bean.rfm.TindexDataBean;
import io.sugo.services.usergroup.model.bean.ScanQueryResult;
import io.sugo.services.usergroup.model.rfm.RFMManager;
import io.sugo.services.usergroup.model.rfm.RFMModel;
import io.sugo.services.usergroup.query.GroupByQuery;
import io.sugo.services.usergroup.query.Query;
import io.sugo.services.usergroup.query.ScanQuery;
import redis.clients.jedis.ScanResult;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RFMUtil {
    private static final Logger log = new Logger(RFMUtil.class);
    private static final ObjectMapper jsonMapper = new ObjectMapper();


    public static Query rewriteTindexQuery(RFMDimensions rfmDimensions, TindexDataBean tindexDataBean) {
        Query query = tindexDataBean.getQuery();
        Preconditions.checkState(query instanceof GroupByQuery, "wtf! not found groupBy query for tindex.");
        GroupByQuery groupByQuery = (GroupByQuery) query;

        List<Dimension> dimensions = new ArrayList<>();

        Dimension dimension = new Dimension();
        dimension.setDimension(rfmDimensions.getUserIdKey());
        dimensions.add(dimension);
        groupByQuery.setDimensions(dimensions);

        List<Aggregation> aggregations = new ArrayList<>();
        Aggregation recency = new Aggregation();
        recency.setName("lastTime");
        recency.setType("lucene_dateMax");
        aggregations.add(recency);

        Aggregation frequency = new Aggregation();
        frequency.setName("frequency");
        frequency.setType("lucene_count");
        aggregations.add(frequency);

        Aggregation monetary = new Aggregation();
        monetary.setName("monetary");
        monetary.setType("lucene_doubleSum");
        aggregations.add(monetary);
        groupByQuery.setAggregations(aggregations);
        return query;
    }

    public static List<RFMModel> getTindexData(Query query, String broker){
        Preconditions.checkState(query instanceof  GroupByQuery, "only support groupBy query for tinexDataBean");
        String resultStr = "";
        String queryStr = null;
        long startQueryTime = System.currentTimeMillis();
        try {
            queryStr = jsonMapper.writeValueAsString(query);
            log.info("Begin to fetch RFM data from url: [%s], params: [%s].", broker, queryStr);
            resultStr = HttpClinetUtil.post(broker, queryStr).body().string();
        } catch (Exception e) {
            log.error("Query druid '%s' with parameter '%s' failed: ", broker, queryStr);
        }

        final List<RFMModel> rfmModelList = new ArrayList<>();
        try {
            JavaType javaType = jsonMapper.getTypeFactory().constructParametrizedType(List.class, ArrayList.class, RFMManager.DruidResult.class);
            List<RFMManager.DruidResult> druidResults = jsonMapper.readValue(resultStr, javaType);
            druidResults.forEach(druidResult -> {
                rfmModelList.add(druidResult.getEvent());
            });

            log.info("Fetch %d RFM data from druid %s in %d ms.", druidResults.size(), broker, System.currentTimeMillis() - startQueryTime);
        } catch (IOException e) {
            log.warn("Deserialize druid result to type [" + RFMManager.DruidResult.class.getName() +
                    "] list failed, details:" + e.getMessage());

            try {
                RFMManager.DruidError errorResult = jsonMapper.readValue(resultStr, RFMManager.DruidError.class);
                log.error("Fetch RFM data from druid failed: %s", errorResult.getError());
            } catch (IOException e1) {
                log.warn("Deserialize druid error result to type [" + RFMManager.DruidError.class.getName() +
                        "] failed, details:" + e.getMessage());
            }
        }

        return rfmModelList;
    }

    public static List<String> getUindexData(Query query, String broker){
        Preconditions.checkState(query instanceof ScanQuery, "only support scan query for uinexDataBean");
        String resultStr = "";
        String queryStr = null;
        long startQueryTime = System.currentTimeMillis();
        try {
            queryStr = jsonMapper.writeValueAsString(query);
            log.info("Begin to fetch RFM data from url: [%s], params: [%s].", broker, queryStr);
            resultStr = HttpClinetUtil.post(broker, queryStr).body().string();
        } catch (Exception e) {
            log.error("Query uindex '%s' with parameter '%s' failed: ", broker, queryStr);
        }

        final List<String> userList = new ArrayList<>();
        try {
            JavaType javaType = jsonMapper.getTypeFactory().constructParametrizedType(List.class, ArrayList.class, ScanQueryResult.class);
            List<ScanQueryResult> scamResults = jsonMapper.readValue(resultStr, javaType);

            scamResults.forEach(scanQueryResult -> {
                //scanQueryResult 只有两列数据： timestamp和用户ID
                List<String> columns  = scanQueryResult.getColumns();
                Preconditions.checkState(columns.size() == 2);
                int timestampIndex = scanQueryResult.getColumns().indexOf("timestamp");
                for(List l :  scanQueryResult.getEvents())
                userList.add(l.get(1-timestampIndex).toString());
            });

            log.info("Fetch %d RFM data from uindex %s in %d ms.", scamResults.size(), broker, System.currentTimeMillis() - startQueryTime);
        } catch (IOException e) {
            log.warn("Deserialize uindex result to type [" + RFMManager.DruidResult.class.getName() +
                    "] list failed, details:" + e.getMessage());

            try {
                RFMManager.DruidError errorResult = jsonMapper.readValue(resultStr, RFMManager.DruidError.class);
                log.error("Fetch RFM data from uindex failed: %s", errorResult.getError());
            } catch (IOException e1) {
                log.warn("Deserialize uindex error result to type [" + RFMManager.DruidError.class.getName() +
                        "] failed, details:" + e.getMessage());
            }
        }

        return userList;
    }

    private static class Dimension {
        String type = "default";
        String dimension;
        String outputName = "userId";

        @JsonProperty
        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        @JsonProperty
        public String getDimension() {
            return dimension;
        }

        public void setDimension(String dimension) {
            this.dimension = dimension;
        }

        @JsonProperty
        public String getOutputName() {
            return outputName;
        }

        public void setOutputName(String outputName) {
            this.outputName = outputName;
        }
    }

    private static class Aggregation {
        String name;
        String type;
        String fieldName = "";

        @JsonProperty
        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        @JsonProperty
        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        @JsonProperty
        public String getFieldName() {
            return fieldName;
        }

        public void setFieldName(String fieldName) {
            this.fieldName = fieldName;
        }
    }

}
