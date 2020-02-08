package io.sugo.services.usergroup.bean.rfm;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;

public class RFMParams {
    int r;
    int f;
    int m;
    @JsonCreator
    public RFMParams(
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

    @JsonProperty
    public int getR() {
        return r;
    }

    @JsonProperty
    public int getF() {
        return f;
    }

    @JsonProperty
    public int getM() {
        return m;
    }
}