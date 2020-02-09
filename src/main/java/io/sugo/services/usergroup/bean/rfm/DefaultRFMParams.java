package io.sugo.services.usergroup.bean.rfm;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;

public class DefaultRFMParams implements RFMParams<Integer> {
    int r;
    int f;
    int m;
    @JsonCreator
    public DefaultRFMParams(
            @JsonProperty("R")Integer R,
            @JsonProperty("F")Integer F,
            @JsonProperty("M")Integer M)
    {
        Preconditions.checkNotNull(R, "RFMParams[R]  can not be null.");
        Preconditions.checkNotNull(F, "RFMParams[F]  can not be null.");
        Preconditions.checkNotNull(M, "RFMParams[M]  can not be null.");
        this.r = R;
        this.f = F;
        this.m = M;
    }

    public Integer getR() {
        return r;
    }

    public Integer getF() {
        return f;
    }

    public Integer getM() {
        return m;
    }
}
