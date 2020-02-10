package io.sugo.services.usergroup.model.rfm;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import com.metamx.common.logger.Logger;
import io.sugo.common.guice.annotations.Json;
import io.sugo.common.redis.RedisDataIOFetcher;
import io.sugo.common.redis.serderializer.UserGroupSerDeserializer;
import io.sugo.common.utils.RFMUtil;
import io.sugo.common.utils.UserGroupUtil;
import io.sugo.services.usergroup.bean.rfm.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 */
public class RFMManager {

    private static final Logger log = new Logger(RFMManager.class);
    private ObjectMapper jsonMapper;
    @Inject
    public RFMManager(@Json ObjectMapper mapper) {
        jsonMapper = mapper;
    }

    public QuantileModel handle(RFMRequestBean requestBean){
        RFMParams params = requestBean.getParams();
        QuantileModel quantileModel = null;
        if (params instanceof DefaultRFMParams){
            DefaultRFMParams defaultRFMParams = (DefaultRFMParams)params;
            quantileModel = this.getDefaultQuantileModel(requestBean, defaultRFMParams.getR(), defaultRFMParams.getF(), defaultRFMParams.getM());
        }else {
            CustomRFMParams customRFMParams = (CustomRFMParams)params;
            quantileModel =  this.getCustomizedQuantileModel(requestBean, customRFMParams.getR(), customRFMParams.getF(), customRFMParams.getM());
        }
        splitRFMToUserGroup(requestBean, quantileModel);

        return quantileModel;
    }

    public QuantileModel getDefaultQuantileModel(RFMRequestBean requestBean, int r, int f, int m) {
        List<RFMModel> rfmModelList = fetchModelData(requestBean);
        int dataSize = rfmModelList.size();
        if (r > dataSize) {
            log.warn("The total data size is: " + dataSize + ", but the 'R' parameter is: " +
                    r + ". 'R' must be not greater than data size.");
            return QuantileModel.emptyModel(r, f, m);
        }
        if (f > dataSize) {
            log.warn("The total data size is: " + dataSize + ", but the 'F' parameter is: " +
                    f + ". 'F' must be not greater than data size.");
            return QuantileModel.emptyModel(r, f, m);
        }
        if (m > dataSize) {
            log.warn("The total data size is: " + dataSize + ", but the 'M' parameter is: " +
                    m + ". 'M' must be not greater than data size.");
            return QuantileModel.emptyModel(r, f, m);
        }

        DefaultQuantileCalculator calculator = new DefaultQuantileCalculator(rfmModelList, r, f, m);
        QuantileModel quantileModel = calculator.calculate(requestBean.getRequestId());

        return quantileModel;
    }

    public QuantileModel getCustomizedQuantileModel(RFMRequestBean requestBean, List<Double> rqList, List<Double> fqList, List<Double> mqList) {
        List<RFMModel> rfmModelList = fetchModelData(requestBean);

        double[] rq = convertListParams(rqList);
        double[] fq = convertListParams(fqList);
        double[] mq = convertListParams(mqList);

        int dataSize = rfmModelList.size();
        if (rq.length + 1 > dataSize) {
            log.warn("The total data size is: " + dataSize + ", but the 'R' parameter is: " +
                    rq.length + 1 + ". 'R' must be not greater than data size.");
            return QuantileModel.emptyModel(rq, fq, mq);
        }
        if (fq.length + 1 > dataSize) {
            log.warn("The total data size is: " + dataSize + ", but the 'F' parameter is: " +
                    fq.length + 1 + ". 'F' must be not greater than data size.");
            return QuantileModel.emptyModel(rq, fq, mq);
        }
        if (mq.length + 1 > dataSize) {
            log.warn("The total data size is: " + dataSize + ", but the 'M' parameter is: " +
                    mq.length + 1 + ". 'M' must be not greater than data size.");
            return QuantileModel.emptyModel(rq, fq, mq);
        }

        CustomizedQuantileCalculator calculator = new CustomizedQuantileCalculator(rfmModelList, rq, fq, mq);
        QuantileModel quantileModel = calculator.calculate(requestBean.getRequestId());

        return quantileModel;
    }

    private double[] convertListParams(List<Double> list){
        double[] a = new double[list.size()];
        for(int i=0; i< list.size(); i++){
            a[i] = list.get(i);
        }
        return a;
    }

    private List<RFMModel> fetchModelData(RFMRequestBean requestBean){
        List<DataBean> dataSet = requestBean.getDataSet();
        Preconditions.checkState(dataSet.size() <=2, "dataSet size can not beyond 2" );
        DataBean tindexDataBean =  dataSet.stream().filter(d -> d.getType().equals(DataBean.TINDEX_TYPE))
                .findFirst().orElse(null);
        DataBean uindexDataBean =  dataSet.stream().filter(d -> d.getType().equals(DataBean.UINDEX_TYPE))
                .findFirst().orElse(null);
        Preconditions.checkNotNull(tindexDataBean, "can not found TindexDataBean from dataSet" );

        List<RFMModel> rfmModelList = RFMUtil.getTindexData(
                RFMUtil.rewriteTindexQuery(requestBean.getDimensions(), tindexDataBean),
                tindexDataBean.getBroker(),
                jsonMapper,
                new TypeReference<List<RFMUtil.DruidResult<RFMModel>>>() {});
        if(uindexDataBean != null){
            List<String> uindexData = RFMUtil.getUindexData(
                    uindexDataBean.getQuery(),
                    uindexDataBean.getBroker(),
                    jsonMapper);
            rfmModelList = rfmModelList.stream().filter(data -> uindexData.contains(data.getUserId())).collect(Collectors.toList());
        }
        return rfmModelList;
    }

    private void splitRFMToUserGroup(RFMRequestBean requestBean, QuantileModel quantileModel){
        RedisDataIOFetcher redisConfig = requestBean.getRedisConfig();
        UserGroupSerDeserializer userGroupSerDeserializer = new UserGroupSerDeserializer(redisConfig);
        List<String> userId = null;
        synchronized (redisConfig){
            for(RFMGroup group : quantileModel.getGroups()){
                redisConfig.setGroupId(group.getGroupId());
                userId = group.getUserIdList();
                UserGroupUtil.writeDataToRedis(userGroupSerDeserializer, ImmutableSet.copyOf(userId));
                //释放内存
                userId.clear();
                log.info(String.format("write data to usergroup: %s finished.", group.getGroupId()));

            }
        }
    }
}
