package io.sugo.services.usergroup.bean.valuetier;

import io.sugo.services.usergroup.bean.ModelRequest;
import io.sugo.services.usergroup.bean.rfm.DataBean;
import io.sugo.services.usergroup.bean.rfm.RFMDimensions;

import java.util.Comparator;
import java.util.List;

public class ValueTierRequestBean implements ModelRequest {
    public static final String TYPE = "valueTier";
    private List<DataBean> datasets;
    private RFMDimensions dimensions;
    private List<ValueTier> params;

    public ValueTierRequestBean(List<DataBean> datasets, RFMDimensions dimensions, List<ValueTier> params) {
        this.datasets = datasets;
        this.dimensions = dimensions;
        params.sort(Comparator.comparing(ValueTier::getId));
        this.params = params;
    }

    public List<DataBean> getDatasets() {
        return datasets;
    }

    public RFMDimensions getDimensions() {
        return dimensions;
    }

    public List<ValueTier> getParams() {
        return params;
    }

    @Override
    public String getRequestId() {
        return null;
    }

    @Override
    public String getCallbackUrl() {
        return null;
    }

    @Override
    public String getType() {
        return null;
    }
}
