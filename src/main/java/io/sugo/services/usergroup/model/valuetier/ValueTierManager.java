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
import io.sugo.common.utils.RFMUtil;
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

public class ValueTierManager {
    private static final Logger log = new Logger(LifeCycleManager.class);
    private static final String AGGREGATOR_OUTPUT_NAME = "totalAmount";
    private ObjectMapper jsonMapper;
    @Inject
    public ValueTierManager(@Json ObjectMapper mapper) {
        jsonMapper = mapper;
    }

    private void handle(ValueTierRequestBean requestBean){
        List<DataBean> datasets = requestBean.getDatasets();
        DataBean uindexDataBean = datasets.stream().filter(s -> s.getType().equals(DataBean.UINDEX_TYPE))
                .findFirst().orElse(null);
        DataBean tindexDataBean = datasets.stream().filter(s -> s.getType().equals(DataBean.TINDEX_TYPE))
                .findFirst().orElse(null);
        List<ValueTier> valueTiers = requestBean.getParams();
        List<Double> percents = valueTiers.stream().map(ValueTier::getPercent).collect(Collectors.toList());
        Preconditions.checkState(percents.size() > 0);
        List<Double> quantilePoins = new ArrayList<>(percents.size() - 1);

        //将[0.5,0.3,0.2]转化为[0.5,0.8];
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
        private Integer totalAmount;
        private Integer groupId;

        @JsonCreator
        public ValueTierResult(@JsonProperty("userId") String userId,
                               @JsonProperty("totalAmount") Integer totalAmount) {
            this.userId = userId;
            this.totalAmount = totalAmount;
        }

        public String getUserId() {
            return userId;
        }

        public Integer getTotalAmount() {
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
