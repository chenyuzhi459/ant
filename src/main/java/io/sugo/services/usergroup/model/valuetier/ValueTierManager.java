package io.sugo.services.usergroup.model.valuetier;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import com.metamx.common.logger.Logger;
import io.sugo.common.guice.annotations.Json;
import io.sugo.common.redis.RedisDataIOFetcher;
import io.sugo.common.redis.serderializer.UserGroupSerDeserializer;
import io.sugo.common.utils.RFMUtil;
import io.sugo.common.utils.UserGroupUtil;
import io.sugo.services.pathanalysis.dto.PathAnalysisDto;
import io.sugo.services.usergroup.bean.lifecycle.FilterAggregation;
import io.sugo.services.usergroup.bean.rfm.DataBean;
import io.sugo.services.usergroup.bean.valuetier.ValueTier;
import io.sugo.services.usergroup.bean.valuetier.ValueTierRequestBean;
import io.sugo.services.usergroup.model.lifecycle.LifeCycleManager;
import io.sugo.services.usergroup.query.GroupByQuery;
import io.sugo.services.usergroup.query.Query;

import java.util.*;
import java.util.stream.Collectors;

import static io.sugo.services.usergroup.model.rfm.QuantileModel.PERCENT_FORMAT;

public class ValueTierManager {
    private static final Logger log = new Logger(ValueTierManager.class);
    private static final String AGGREGATOR_OUTPUT_NAME = "totalAmount";
    private ObjectMapper jsonMapper;
    @Inject
    public ValueTierManager(@Json ObjectMapper mapper) {
        jsonMapper = mapper;
    }

    public List<LifeCycleManager.StageResult>  handle(ValueTierRequestBean requestBean){
        List<DataBean> datasets = requestBean.getDatasets();
        DataBean uindexDataBean = datasets.stream().filter(s -> s.getType().equals(DataBean.UINDEX_TYPE))
                .findFirst().orElse(null);
        DataBean tindexDataBean = datasets.stream().filter(s -> s.getType().equals(DataBean.TINDEX_TYPE))
                .findFirst().orElse(null);
        List<ValueTier> valueTiers = requestBean.getParams();
        List<Double> percents = valueTiers.stream().map(ValueTier::getPercent).collect(Collectors.toList());
        Preconditions.checkState(percents.size() > 0);

        //将[0.5,0.3,0.2]转化为[0.5,0.8,1.0];
        List<Double> quantilePoins = new ArrayList<>(percents.size() - 1);
        quantilePoins.add(percents.get(0));
        for(int i = 1; i <= quantilePoins.size(); i++){
            quantilePoins.add(quantilePoins.get(i - 1) + percents.get(i));
        }
        List<String> uindexUser = RFMUtil.getUindexData(uindexDataBean.getQuery(), uindexDataBean.getBroker(), jsonMapper);
        Query tindexQuery = tindexDataBean.getQuery();
        Preconditions.checkState(tindexQuery instanceof GroupByQuery);
        this.rewriteTindexQuery((GroupByQuery)tindexQuery, requestBean.getDimensions().getBuyAmountKey());
        List<ValueTierResult> tindexData =  RFMUtil.getTindexData(tindexQuery,
                tindexDataBean.getBroker(),
                jsonMapper,
                new TypeReference<List<RFMUtil.DruidResult <ValueTierResult>>>() {
                });

        List<ValueTierResult> filterResult = tindexData.stream()
                .filter(r -> uindexUser.contains(r.getUserId())).collect(Collectors.toList());
        Map<Integer, Set<String>> groupData = this.doQuantile(filterResult, quantilePoins, valueTiers);
        Integer totalUser = groupData.values().stream().map(Set::size).reduce((a, b) -> a + b).orElse(0);
        List<LifeCycleManager.StageResult> results = new ArrayList<>();
        groupData.forEach((k, v) ->{
            ValueTier valueTier = valueTiers.stream().filter(s -> k.equals(s.getId())).findFirst().orElse(null);
            results.add(this.writeValueTierDataToUserGroup(valueTier, requestBean, v));
        });

        for(LifeCycleManager.StageResult stageResult : results){
            stageResult.setUserPercent(Double.valueOf(PERCENT_FORMAT.format(stageResult.getUserCount() * 100.0d / totalUser)) + "%");
        }
        return results;
    }

    private Map<Integer, Set<String>> doQuantile(List<ValueTierResult> data, List<Double> quantilePoins, List<ValueTier> valueTiers){
        Double sumAmount = data.stream().map(ValueTierResult::getTotalAmount).reduce(Double::sum)
                .orElse(null);
        Double accumulator = 0.0;
        int index = 0;
        for(ValueTierResult r : data){
            accumulator = r.getTotalAmount() + accumulator;
            r.setGroupId(valueTiers.get(index).getId());
            if((accumulator / sumAmount) > quantilePoins.get(index)){
                index++;
            }
        }
        Map<Integer, Set<String>> groups = data.stream().collect(Collectors.toMap(
                ValueTierResult::getGroupId,
                v -> Collections.singleton(v.getUserId()),
                (v1, v2) -> {
                    Set<String> mergeVals = new HashSet<>();
                    mergeVals.addAll(v1);
                    mergeVals.addAll(v2);
                    return mergeVals;
                })
        );

        return groups;
    }

    private LifeCycleManager.StageResult writeValueTierDataToUserGroup(ValueTier valueTier, ValueTierRequestBean requestBean, Set<String> groupData) {
        RedisDataIOFetcher redisConfig = requestBean.getRedisConfig();
        UserGroupSerDeserializer userGroupSerDeserializer = new UserGroupSerDeserializer(redisConfig);
        String groupId = requestBean.getRequestId() + "%" + valueTier.getId() + "_" + valueTier.getName();
        int userCount;
        synchronized (redisConfig){
            redisConfig.setGroupId(groupId);
            userCount = UserGroupUtil.writeDataToRedis(userGroupSerDeserializer, groupData);
            log.info(String.format("write data to usergroup: %s finished.", groupId));
        }
        return new LifeCycleManager.StageResult(valueTier.getId(), groupId, userCount);
    }

    private void rewriteTindexQuery(GroupByQuery query, String buyAmountKey){
        //重写dimension, 设置outputName
        String groupByDimension = query.getDimensions().get(0).toString();
        List<RFMUtil.Dimension> dimensions = new ArrayList<>();
        RFMUtil.Dimension dimension = new RFMUtil.Dimension();
        dimension.setDimension(groupByDimension);
        dimensions.add(dimension);
        query.setDimensions(dimensions);

        //设置指标
        RFMUtil.Aggregation countAgg = new RFMUtil.Aggregation();
        countAgg.setName(AGGREGATOR_OUTPUT_NAME);
        countAgg.setType("lucene_count");
        //非null过滤
        PathAnalysisDto.NotNullField notNullField = new PathAnalysisDto.NotNullField();
        notNullField.setDimension(buyAmountKey);
        FilterAggregation filterAggregation = new FilterAggregation(countAgg, notNullField);
        query.setAggregations(Collections.singleton(filterAggregation));

        Map<String, String> orderSepec = ImmutableMap.of("dimension", buyAmountKey, "direction", "descending");
        Map<String, Object> limitSpec = ImmutableMap.of("type", "default",
                "columns", ImmutableList.of(orderSepec));
        query.setLimitSpec(limitSpec);

    }

    @JsonInclude(JsonInclude.Include.NON_NULL) //设置不打印null属性值
    private class ValueTierResult {
        private String userId;
        private Double totalAmount;
        private Integer groupId;

        @JsonCreator
        public ValueTierResult(@JsonProperty("userId") String userId,
                               @JsonProperty("totalAmount") Double totalAmount) {
            this.userId = userId;
            this.totalAmount = totalAmount;
        }

        public String getUserId() {
            return userId;
        }

        public Double getTotalAmount() {
            return totalAmount;
        }

        public Integer getGroupId() {
            return groupId;
        }

        public void setGroupId(Integer groupId) {
            this.groupId = groupId;
        }
    }
}
