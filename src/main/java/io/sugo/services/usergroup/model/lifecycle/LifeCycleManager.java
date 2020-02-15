package io.sugo.services.usergroup.model.lifecycle;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.metamx.common.logger.Logger;
import io.sugo.common.guice.annotations.Json;
import io.sugo.common.redis.RedisDataIOFetcher;
import io.sugo.common.redis.serderializer.UserGroupSerDeserializer;
import io.sugo.common.utils.ModelUtil;
import io.sugo.common.utils.UserGroupUtil;
import io.sugo.services.pathanalysis.dto.PathAnalysisDto;
import io.sugo.services.query.aggregator.Aggregation;
import io.sugo.services.query.dimension.Dimension;
import io.sugo.services.query.filter.AndFilter;
import io.sugo.services.query.filter.BetweenFilter;
import io.sugo.services.query.filter.FieldType;
import io.sugo.services.query.filter.NotNullFilter;
import io.sugo.services.query.result.DruidResult;
import io.sugo.services.usergroup.bean.lifecycle.*;
import io.sugo.services.usergroup.bean.rfm.DataBean;
import io.sugo.services.query.GroupByQuery;
import io.sugo.services.query.Query;
import io.sugo.services.query.ScanQuery;

import java.util.*;
import java.util.stream.Collectors;

import static io.sugo.services.query.dimension.Dimension.USER_ID;
import static io.sugo.services.usergroup.bean.lifecycle.StagesSpec.HISTORY_AGG_OUTPUT_NAME;
import static io.sugo.services.usergroup.model.rfm.QuantileModel.PERCENT_FORMAT;

public class LifeCycleManager {
    private static final Logger log = new Logger(LifeCycleManager.class);
    private ObjectMapper jsonMapper;
    @Inject
    public LifeCycleManager(@Json ObjectMapper mapper) {
        jsonMapper = mapper;
    }

    /**demo
     * 若用户行为数据集为空：
     * 引入期：最近30天注册 并且 最近1年累计订单=0
     * 发展期：最近30天订单数>=1 并且 最近1年累计订单=1、2
     * 成熟期：最近30天订单数>=1 并且 最近1年累计订单>=3
     * 衰退期：最近30天订单数=0 并且 最近1年累计订单>=1
     * 流失期：最近60天订单数=0 并且 最近1年累计订单>=1
     *
     * 若用户行为数据集非空：
     * 引入期：最近30天注册 并且 最近1年累计订单=0
     * 发展期：最近30天行为记录数>=1 并且 最近1年累计订单=1、2
     * 成熟期：最近30天行为记录数>=1 并且 最近1年累计订单>=3
     * 衰退期：最近30天行为记录数=0 并且 最近1年累计订单>=1
     * 流失期：最近60天行为记录数=0 并且 最近1年累计订单>=1
     * @param requestBean
     */
    public List<StageResult> handle(LifeCycleRequestBean requestBean){
        DataSpec dataSpec = requestBean.getDataSpec();
        LifeCycleDimensions dimensions = requestBean.getDimensions();
        StagesSpec stagesSpec = requestBean.getStagesSpec();
        DataBean uindexDataBean = dataSpec.getUindexDataBean();
        ScanQuery uindexScanQuery = (ScanQuery)uindexDataBean.getQuery();
        List<String> uindexAllUser = ModelUtil.getUindexData(uindexScanQuery, uindexDataBean.getBroker(), jsonMapper);

        List<Stage> stages = stagesSpec.getStages();
        Stage stage1 = stages.get(0);
        List<Stage> sortedStage = stages.subList(1, stages.size());
        Preconditions.checkState(sortedStage.size() == 4);
        //交易数据
        DataBean tindexTradeDatabean = dataSpec.getTradeDataBean();
        //行为数据
        DataBean tindexBehaviorDataBean = dataSpec.getBehaviorDataBean();
        List<StageResult> results = new ArrayList<>();

        //计算一个周期内注册用户
        AndFilter registerFilter =
                buildRegisterFilter(stage1.getIntervalStart(), stage1.getIntervalEnd(), dimensions.getRegisterTimeKey());
        Object uindexOrignalFilter = uindexScanQuery.getFilter();
        if(uindexOrignalFilter != null){
            registerFilter.getFields().add(uindexOrignalFilter);
        }
        uindexScanQuery.setFilter(registerFilter);
        List<String> uindexRegisterUser = ModelUtil.getUindexData(uindexScanQuery, uindexDataBean.getBroker(), jsonMapper);
        Long totalUserCount = 0L;
        if(tindexBehaviorDataBean == null){
            String timeDimension = dimensions.getBuyTimeKey();
            //构造Tindex订单数据的 aggregator
            Set<Aggregation> aggs = sortedStage.stream()
                    .map(s -> s.buildStageAggregation(timeDimension))
                    .distinct().collect(Collectors.toSet());
            aggs.add(stagesSpec.buildHistoryAggregator(timeDimension));

            Query q = tindexTradeDatabean.getQuery();
            Preconditions.checkState(q instanceof GroupByQuery);
            GroupByQuery groupByQuery = (GroupByQuery) q;
            groupByQuery.setAggregations(aggs);
            rewriteGroupByDimensions(groupByQuery);
            List<Map<String, Object>> tindexTradeData =  ModelUtil.getTindexData(groupByQuery,
                    tindexTradeDatabean.getBroker(),
                    jsonMapper,
                    new TypeReference<List<DruidResult<Map<String, Object>>>>() {
                    });

            //处理stage1
            Set<String> firstStageUsers = handleFirstStage(stage1, uindexRegisterUser, tindexTradeData);
            //尽快释放uindexRegisterUser内存
            uindexRegisterUser.clear();
            results.add(writeStageDataToUserGroup(stage1, requestBean, firstStageUsers));
            totalUserCount += firstStageUsers.size();

            Set<String> stageUsers;
            for(Stage stage: sortedStage){
                stageUsers = handleCommonStage(stage, uindexAllUser,timeDimension, tindexTradeData);
                results.add(writeStageDataToUserGroup(stage, requestBean, stageUsers));
                totalUserCount += stageUsers.size();
            }
        } else {
            //处理tindexTradeDatabean
            String buyTimeKey = dimensions.getBuyTimeKey();
            //构造Tindex订单数据的 aggregator
            Set<Aggregation> tradeAggs = new HashSet<>();
            tradeAggs.add(stagesSpec.buildHistoryAggregator(buyTimeKey));

            Query tradeQuery = tindexTradeDatabean.getQuery();
            Preconditions.checkState(tradeQuery instanceof GroupByQuery);
            GroupByQuery tradeGroupByQuery = (GroupByQuery) tradeQuery;
            tradeGroupByQuery.setAggregations(tradeAggs);
            rewriteGroupByDimensions(tradeGroupByQuery);
            List<Map<String, Object>> tindexTradeData =  ModelUtil.getTindexData(tradeGroupByQuery,
                    tindexTradeDatabean.getBroker(),
                    jsonMapper,
                    new TypeReference<List<DruidResult<Map<String, Object>>>>() {
                    });

            //处理stage1
            Set<String> firstStageUsers = handleFirstStage(stage1, uindexRegisterUser, tindexTradeData);
            //尽快释放uindexRegisterUser内存
            uindexRegisterUser.clear();
            results.add(writeStageDataToUserGroup(stage1, requestBean, firstStageUsers));
            totalUserCount += firstStageUsers.size();

            //处理tindexBehaviorDataBean
            String behaviorTimeKey = dimensions.getBehaviorTimeKey();
            //构造Tinde行为数据的 aggregator
            Set<Aggregation> behaviorAggs = sortedStage.stream()
                    .map(s -> s.buildStageAggregation(behaviorTimeKey))
                    .collect(Collectors.toSet());
            Query behaviorQuery = tindexBehaviorDataBean.getQuery();
            Preconditions.checkState(behaviorQuery instanceof GroupByQuery);
            GroupByQuery behaviorGroupByQuery = (GroupByQuery) behaviorQuery;
            behaviorGroupByQuery.setAggregations(behaviorAggs);
            rewriteGroupByDimensions(behaviorGroupByQuery);
            List<Map<String, Object>> tindexBehaviorData =  ModelUtil.getTindexData(behaviorGroupByQuery,
                    tindexBehaviorDataBean.getBroker(),
                    jsonMapper,
                    new TypeReference<List<DruidResult<Map<String, Object>>>>() {
                    });

            Set<String> stageUsers;
            for(Stage stage: sortedStage){
                stageUsers = handleCommonStage2(stage, uindexAllUser,behaviorTimeKey, tindexTradeData, tindexBehaviorData);
                results.add(writeStageDataToUserGroup(stage, requestBean, stageUsers));
                totalUserCount += stageUsers.size();
            }

        }
        //避免0为被除数的问题
        totalUserCount = totalUserCount > 0 ? totalUserCount : 1;
        for(StageResult stageResult : results){
            stageResult.setUserPercent(Double.valueOf(PERCENT_FORMAT.format(stageResult.getUserCount() * 100.0d / totalUserCount)) + "%");
        }

        return results;

    }

    private StageResult writeStageDataToUserGroup(Stage stage, LifeCycleRequestBean requestBean, Set<String> data){
        RedisDataIOFetcher redisConfig = requestBean.getRedisConfig();
        UserGroupSerDeserializer userGroupSerDeserializer = new UserGroupSerDeserializer(redisConfig);
        String groupId = requestBean.getRequestId() + "%" + stage.getStageId() + "_" + stage.getName();
        int userCount;
        synchronized (redisConfig){
            redisConfig.setGroupId(groupId);
            userCount = UserGroupUtil.writeDataToRedis(userGroupSerDeserializer, data);
            log.info(String.format("write data to usergroup: %s finished.", groupId));
        }
        return new StageResult(stage.getStageId(), groupId, userCount);
    }

    public static class StageResult {
        private Integer stageId;
        private String groupId;
        private Integer userCount;
        private String userPercent;

        public StageResult(Integer stageId, String groupId, Integer userCount) {
            this.stageId = stageId;
            this.groupId = groupId;
            this.userCount = userCount;
        }

        @JsonProperty
        public Integer getStageId() {
            return stageId;
        }

        @JsonProperty
        public String getGroupId() {
            return groupId;
        }

        @JsonProperty
        public Integer getUserCount() {
            return userCount;
        }

        @JsonProperty
        public String getUserPercent() {
            return userPercent;
        }

        public void setUserPercent(String userPercent) {
            this.userPercent = userPercent;
        }
    }

    private Set<String> handleFirstStage(Stage stage, List<String> uindexUser, List<Map<String, Object>> tindexTradeData){
        Stage.RangeInfo historyRange = stage.getHistoryRange();
        long historyMax = historyRange.getMax() == null ? Long.MAX_VALUE  : historyRange.getMax();
        long historyMin = historyRange.getMin() == null ? Integer.MIN_VALUE  : historyRange.getMin();
        Set<String> stageUsers = tindexTradeData.stream().filter(m -> {
            long historyAgg = Long.parseLong(m.get(HISTORY_AGG_OUTPUT_NAME).toString());
            return historyMin <= historyAgg && historyAgg <= historyMax &&
                    uindexUser.contains(m.get(USER_ID).toString());

        }).map(m -> m.get(USER_ID).toString()).collect(Collectors.toSet());
        return stageUsers;
    }

    //处理既有行为数据又有交易数据的情况
    private Set<String> handleCommonStage2(Stage stage, List<String> uindexUser, String dimension,
                                           List<Map<String, Object>> tindexTradeData,
                                           List<Map<String, Object>> tindexBehaviorData){
        Stage.RangeInfo historyRange = stage.getHistoryRange();
        Stage.RangeInfo stageRange = stage.getStageRange();
        long historyMax = historyRange.getMax() == null ? Long.MAX_VALUE  : historyRange.getMax();
        long historyMin = historyRange.getMin() == null ? Integer.MIN_VALUE  : historyRange.getMin();

        long stageMax = stageRange.getMax() == null ? Long.MAX_VALUE  : stageRange.getMax();
        long stageMin = stageRange.getMin() == null ? Long.MIN_VALUE  : stageRange.getMin();
        Set<String> filteredTradeUser = tindexTradeData.stream().filter(m -> {
            long historyAgg = Long.parseLong(m.get(HISTORY_AGG_OUTPUT_NAME).toString());
            return historyMin <= historyAgg && historyAgg <= historyMax &&
                    uindexUser.contains(m.get(USER_ID).toString());

        }).map(m -> m.get(USER_ID).toString()).collect(Collectors.toSet());

        String stageAggName = stage.getAggregatorName(dimension);
        Set<String> stageUsers = tindexBehaviorData.stream().filter(m -> {
            long stageAgg = Long.parseLong(m.get(stageAggName).toString());
            return stageMin <= stageAgg && stageAgg <= stageMax &&
                    filteredTradeUser.contains(m.get(USER_ID).toString());

        }).map(m -> m.get(USER_ID).toString()).collect(Collectors.toSet());
        return stageUsers;
    }

    //处理仅有交易数据的情况
    private Set<String> handleCommonStage(Stage stage, List<String> uindexUser,
                                          String dimension, List<Map<String, Object>> tindexData){
        Stage.RangeInfo historyRange = stage.getHistoryRange();
        Stage.RangeInfo stageRange = stage.getStageRange();
        long historyMax = historyRange.getMax() == null ? Long.MAX_VALUE  : historyRange.getMax();
        long historyMin = historyRange.getMin() == null ? Integer.MIN_VALUE  : historyRange.getMin();

        long stageMax = stageRange.getMax() == null ? Long.MAX_VALUE  : stageRange.getMax();
        long stageMin = stageRange.getMin() == null ? Long.MIN_VALUE  : stageRange.getMin();
        String stageAggName = stage.getAggregatorName(dimension);
        Set<String> stageUsers = tindexData.stream().filter(m -> {
            long historyAgg = Long.parseLong(m.get(HISTORY_AGG_OUTPUT_NAME).toString());
            long stageAgg = Long.parseLong(m.get(stageAggName).toString());
            return historyMin <= historyAgg && historyAgg <= historyMax &&
                    stageMin <= stageAgg && stageAgg <= stageMax &&
                    uindexUser.contains(m.get(USER_ID).toString());

        }).map(m -> m.get(USER_ID).toString()).collect(Collectors.toSet());
        return stageUsers;
    }

    private void rewriteGroupByDimensions(GroupByQuery groupByQuery) {
        String groupByDimension = groupByQuery.getDimensions().get(0).toString();
        List<Dimension> dimensions = new ArrayList<>();

        Dimension dimension = new Dimension();
        dimension.setDimension(groupByDimension);
        dimensions.add(dimension);
        groupByQuery.setDimensions(dimensions);
    }

    private AndFilter buildRegisterFilter(String registerTimeStart, String registerTimeEnd ,String registerTimeKey){
        AndFilter andFilter = new AndFilter();
        List<FieldType> fieldTypes = andFilter.getFields();
        //非空过滤
        NotNullFilter notNullField = new NotNullFilter();
        notNullField.setDimension(registerTimeKey);
        fieldTypes.add(notNullField);

        //时间过滤
        BetweenFilter betweenField = new BetweenFilter();
        betweenField.setDimension(registerTimeKey);
        betweenField.setLower(registerTimeStart);
        betweenField.setUpper(registerTimeEnd);
        fieldTypes.add(betweenField);
        return andFilter;
    }
}
