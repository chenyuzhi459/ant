package io.sugo.common.utils;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.metamx.common.logger.Logger;
import io.sugo.services.usergroup.bean.rfm.DataBean;
import io.sugo.services.usergroup.bean.rfm.RFMDimensions;
import io.sugo.services.usergroup.bean.rfm.ScanQueryResult;
import io.sugo.services.usergroup.model.rfm.RFMModel;
import io.sugo.services.usergroup.query.GroupByQuery;
import io.sugo.services.usergroup.query.Query;
import io.sugo.services.usergroup.query.ScanQuery;
import org.apache.logging.log4j.core.util.Throwables;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class RFMUtil {
    private static final Logger log = new Logger(RFMUtil.class);


    public static Query rewriteTindexQuery(RFMDimensions rfmDimensions, DataBean tindexDataBean) {
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
        recency.setFieldName(rfmDimensions.getBuyTimeKey());
        aggregations.add(recency);

        Aggregation frequency = new Aggregation();
        frequency.setName("frequency");
        frequency.setType("lucene_count");
        aggregations.add(frequency);

        Aggregation monetary = new Aggregation();
        monetary.setName("monetary");
        monetary.setType("lucene_doubleSum");
        monetary.setFieldName(rfmDimensions.getBuyAmountKey());
        aggregations.add(monetary);
        groupByQuery.setAggregations(aggregations);
        return query;
    }

    public static <T> List<T>  getTindexData(Query query, String broker, ObjectMapper jsonMapper, TypeReference<List<DruidResult<T>>> type){
        Preconditions.checkState(query instanceof  GroupByQuery, "only support groupBy query for tinexDataBean");
        String resultStr = "";
        String queryStr = null;
        long startQueryTime = System.currentTimeMillis();
        String url = String.format("http://%s/druid/v2",broker);
        try {
            queryStr = jsonMapper.writeValueAsString(query);
            log.info("Begin to fetch RFM data from url: [%s], params: [%s].", url, queryStr);
            resultStr = HttpClinetUtil.post(url, queryStr).body().string();
        } catch (Exception e) {
            log.error(e,"Query druid '%s' with parameter '%s' failed: errMsg : %s", url, queryStr, e.getMessage());
            Throwables.rethrow(e);
        }
        final List<T> rfmModelList = new ArrayList<>();
        try {
            List<DruidResult<T>> druidResults = jsonMapper.readValue(resultStr, type);
            druidResults.forEach(druidResult -> {
                rfmModelList.add(druidResult.getEvent());
            });

            log.info("Fetch %d RFM data from druid %s in %d ms.", druidResults.size(), broker, System.currentTimeMillis() - startQueryTime);
        } catch (IOException e) {
            log.error(e, "Deserialize druid result to type [" + type.getType() +
                    "] list failed, result details:" + resultStr);
            Throwables.rethrow(e);

        }

        return rfmModelList;
    }

    public static List<String> getUindexData(Query query, String broker,  ObjectMapper jsonMapper){
        Preconditions.checkState(query instanceof ScanQuery, "only support scan query for uinexDataBean");
        String resultStr = "";
        String queryStr = null;
        long startQueryTime = System.currentTimeMillis();
        String url = String.format("http://%s/druid/v2",broker);
        try {
            queryStr = jsonMapper.writeValueAsString(query);
            log.info("Begin to fetch RFM data from url: [%s], params: [%s].", url, queryStr);
            resultStr = HttpClinetUtil.post(url, queryStr).body().string();
        } catch (Exception e) {
            log.error(e,"Query uindex '%s' with parameter '%s' failed: ", url, queryStr);
            Throwables.rethrow(e);
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
            log.error(e,"Deserialize uindex result to type [" + ScanQueryResult.class.getName() +
                    "] list failed, details:" + e.getMessage());
            Throwables.rethrow(e);

        }

        return userList;
    }

    public static class Dimension {
        public static final String USER_ID = "userId";
        String type = "default";
        String dimension;
        String outputName = USER_ID;

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

    public static class Aggregation {
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

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Aggregation)) return false;
            Aggregation that = (Aggregation) o;
            return Objects.equals(getName(), that.getName()) &&
                    Objects.equals(getType(), that.getType()) &&
                    Objects.equals(getFieldName(), that.getFieldName());
        }

        @Override
        public int hashCode() {
            return Objects.hash(getName(), getType(), getFieldName());
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DruidResult<T> {
        T event;

        @JsonCreator
        public DruidResult(
                @JsonProperty("event") T event) {
            this.event = event;
        }

        public T getEvent() {
            return event;
        }
    }

}
