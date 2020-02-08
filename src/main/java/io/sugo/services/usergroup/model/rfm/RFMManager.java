package io.sugo.services.usergroup.model.rfm;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.metamx.common.logger.Logger;
import io.sugo.common.guice.annotations.Json;
import io.sugo.common.utils.RFMUtil;
import io.sugo.services.usergroup.bean.rfm.DataBean;
import io.sugo.services.usergroup.bean.rfm.TindexDataBean;
import io.sugo.services.usergroup.bean.rfm.UindexDataBean;
import io.sugo.services.usergroup.bean.rfm.RFMRequestBean;
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
        TindexDataBean tindexDataBean = (TindexDataBean) dataSet.stream().filter(d -> d.getType().equals(TindexDataBean.TYPE))
                .findFirst().orElse(null);
        UindexDataBean uindexDataBean = (UindexDataBean) dataSet.stream().filter(d -> d.getType().equals(UindexDataBean.TYPE))
                .findFirst().orElse(null);
        Preconditions.checkNotNull(tindexDataBean, "can not found TindexDataBean from dataSet" );

        List<RFMModel> rfmModelList = RFMUtil.getTindexData(
                RFMUtil.rewriteTindexQuery(requestBean.getDimensions(), tindexDataBean),
                tindexDataBean.getBroker(),
                jsonMapper);
        if(uindexDataBean != null){
            List<String> uindexData = RFMUtil.getUindexData(
                    uindexDataBean.getQuery(),
                    uindexDataBean.getBroker(),
                    jsonMapper);
            rfmModelList = rfmModelList.stream().filter(data -> uindexData.contains(data.getUserId())).collect(Collectors.toList());
        }
        return rfmModelList;
    }
}
