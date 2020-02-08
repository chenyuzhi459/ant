package io.sugo.services.usergroup.bean.rfm;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;

import java.util.List;

public class CustomRFMParams implements RFMParams<List<Double>>{
    List<Double> rq;
    List<Double> fq;
    List<Double> mq;

    @JsonCreator
    public CustomRFMParams(
            @JsonProperty("R")List<Double> rq,
            @JsonProperty("F")List<Double> fq,
            @JsonProperty("M")List<Double> mq)
    {
        Preconditions.checkNotNull(rq, "RFMParams[R]  can not be null.");
        Preconditions.checkNotNull(fq, "RFMParams[F]  can not be null.");
        Preconditions.checkNotNull(mq, "RFMParams[M]  can not be null.");
        this.rq = rq;
        this.fq = fq;
        this.mq = mq;
    }

    @JsonProperty
    public List<Double> getR() {
        return rq;
    }

    @JsonProperty
    public List<Double> getF() {
        return fq;
    }

    @JsonProperty
    public List<Double> getM() {
        return mq;
    }

}
