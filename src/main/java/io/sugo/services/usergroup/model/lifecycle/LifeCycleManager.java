package io.sugo.services.usergroup.model.lifecycle;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.metamx.common.logger.Logger;
import io.sugo.common.guice.annotations.Json;
import io.sugo.common.redis.RedisDataIOFetcher;
import io.sugo.common.redis.serderializer.UserGroupSerDeserializer;
import io.sugo.common.utils.RFMUtil;
import io.sugo.common.utils.UserGroupUtil;
import io.sugo.services.pathanalysis.dto.PathAnalysisDto;
import io.sugo.services.usergroup.bean.lifecycle.*;
import io.sugo.services.usergroup.bean.rfm.DataBean;
import io.sugo.services.usergroup.query.GroupByQuery;
import io.sugo.services.usergroup.query.Query;
import io.sugo.services.usergroup.query.ScanQuery;

import java.util.*;
import java.util.stream.Collectors;

import static io.sugo.common.utils.RFMUtil.Dimension.USER_ID;
import static io.sugo.services.usergroup.bean.lifecycle.StagesSpec.HISTORY_AGG_OUTPUT_NAME;

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
        DataSetSpec datasets = requestBean.getDatasets();
        LifeCycleDimensions dimensions = requestBean.getDimensions();
        StagesSpec stagesSpec = requestBean.getStages();
        DataBean uindexDataBean = datasets.getUindexDataBean();
        ScanQuery uindexScanQuery = (ScanQuery)uindexDataBean.getQuery();
        List<String> uindexAllUser = RFMUtil.getUindexData(uindexScanQuery, uindexDataBean.getBroker(), jsonMapper);

        List<Stage> stages = stagesSpec.getStages();
        Stage stage1 = stages.get(0);
        List<Stage> sortedStage = stages.subList(1, stages.size());
        Preconditions.checkState(sortedStage.size() == 4);
        //交易数据
        DataBean tindexTradeDatabean = datasets.getTradeDataBean();
        //行为数据
        DataBean tindexBehaviorDataBean = datasets.getBehaviorDataBean();
        List<StageResult> results = new ArrayList<>();

        //计算一个周期内注册用户
        PathAnalysisDto.FieldType registerFilter =
                buildRegisterFilter(stage1.getIntervalStart(), stage1.getIntervalEnd(), dimensions.getRegisterTimeKey());
        uindexScanQuery.setFilter(registerFilter);
        List<String> uindexRegisterUser = RFMUtil.getUindexData(uindexScanQuery, uindexDataBean.getBroker(), jsonMapper);
        if(tindexBehaviorDataBean == null){
            String timeDimension = dimensions.getBuyTimeKey();
            //构造Tindex订单数据的 aggregator
            List<RFMUtil.Aggregation> aggs = sortedStage.stream()
                    .map(s -> s.buildStageAggregation(timeDimension))
                    .distinct().collect(Collectors.toList());
            aggs.add(stagesSpec.buildHistoryAggregator(timeDimension));

            Query q = tindexTradeDatabean.getQuery();
            Preconditions.checkState(q instanceof GroupByQuery);
            GroupByQuery groupByQuery = (GroupByQuery) q;
            groupByQuery.setAggregations(aggs);
            rewriteGroupByDimensions(groupByQuery);
            List<Map<String, Object>> tindexTradeData =  RFMUtil.getTindexData(groupByQuery,
                    tindexTradeDatabean.getBroker(),
                    jsonMapper,
                    new TypeReference<List<RFMUtil.DruidResult<Map<String, Object>>>>() {
                    });

            //处理stage1
            Set<String> firstStageUsers = handleFirstStage(stage1, uindexRegisterUser, tindexTradeData);
            results.add(writeStageDataToUserGroup(stage1, requestBean, firstStageUsers));

            Set<String> stageUsers;
            for(Stage stage: sortedStage){
                stageUsers = handleCommonStage(stage, uindexAllUser,timeDimension, tindexTradeData);
                results.add(writeStageDataToUserGroup(stage, requestBean, stageUsers));
            }
        } else {
            //处理tindexTradeDatabean
            String buyTimeKey = dimensions.getBuyTimeKey();
            //构造Tindex订单数据的 aggregator
            List<RFMUtil.Aggregation> tradeAggs = new ArrayList<>();
            tradeAggs.add(stagesSpec.buildHistoryAggregator(buyTimeKey));

            Query tradeQuery = tindexTradeDatabean.getQuery();
            Preconditions.checkState(tradeQuery instanceof GroupByQuery);
            GroupByQuery tradeGroupByQuery = (GroupByQuery) tradeQuery;
            tradeGroupByQuery.setAggregations(tradeAggs);
            rewriteGroupByDimensions(tradeGroupByQuery);
            List<Map<String, Object>> tindexTradeData =  RFMUtil.getTindexData(tradeGroupByQuery,
                    tindexTradeDatabean.getBroker(),
                    jsonMapper,
                    new TypeReference<List<RFMUtil.DruidResult<Map<String, Object>>>>() {
                    });

            //处理stage1
            Set<String> firstStageUsers = handleFirstStage(stage1, uindexRegisterUser, tindexTradeData);
            results.add(writeStageDataToUserGroup(stage1, requestBean, firstStageUsers));

            //处理tindexBehaviorDataBean
            String behaviorTimeKey = dimensions.getBehaviorTimeKey();
            //构造Tinde行为数据的 aggregator
            List<RFMUtil.Aggregation> behaviorAggs = sortedStage.stream()
                    .map(s -> s.buildStageAggregation(behaviorTimeKey))
                    .distinct().collect(Collectors.toList());
            Query behaviorQuery = tindexBehaviorDataBean.getQuery();
            Preconditions.checkState(behaviorQuery instanceof GroupByQuery);
            GroupByQuery behaviorGroupByQuery = (GroupByQuery) behaviorQuery;
            behaviorGroupByQuery.setAggregations(tradeAggs);
            rewriteGroupByDimensions(behaviorGroupByQuery);
            List<Map<String, Object>> tindexBehaviorData =  RFMUtil.getTindexData(behaviorGroupByQuery,
                    tindexBehaviorDataBean.getBroker(),
                    jsonMapper,
                    new TypeReference<List<RFMUtil.DruidResult<Map<String, Object>>>>() {
                    });

            Set<String> stageUsers;
            for(Stage stage: sortedStage){
                stageUsers = handleCommonStage2(stage, uindexAllUser,behaviorTimeKey, tindexTradeData, tindexBehaviorData);
                results.add(writeStageDataToUserGroup(stage, requestBean, stageUsers));
            }

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

        public StageResult(Integer stageId, String groupId, Integer userCount) {
            this.stageId = stageId;
            this.groupId = groupId;
            this.userCount = userCount;
        }

        public Integer getStageId() {
            return stageId;
        }

        public String getGroupId() {
            return groupId;
        }

        public Integer getUserCount() {
            return userCount;
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
        List<RFMUtil.Dimension> dimensions = new ArrayList<>();

        RFMUtil.Dimension dimension = new RFMUtil.Dimension();
        dimension.setDimension(groupByDimension);
        dimensions.add(dimension);
        groupByQuery.setDimensions(dimensions);
    }

    private PathAnalysisDto.FieldType buildRegisterFilter(String registerTimeStart, String registerTimeEnd ,String registerTimeKey){
        PathAnalysisDto.AndFilter andFilter = new PathAnalysisDto.AndFilter();
        List<PathAnalysisDto.FieldType> fieldTypes = andFilter.getFields();
        //非空过滤
        PathAnalysisDto.NotNullField notNullField = new PathAnalysisDto.NotNullField();
        notNullField.setDimension(registerTimeKey);
        fieldTypes.add(notNullField);

        //时间过滤
        PathAnalysisDto.BetweenField betweenField = new PathAnalysisDto.BetweenField();
        betweenField.setDimension(registerTimeKey);
        betweenField.setLower(registerTimeStart);
        betweenField.setLower(registerTimeEnd);
        fieldTypes.add(betweenField);
        return andFilter;
    }
}
