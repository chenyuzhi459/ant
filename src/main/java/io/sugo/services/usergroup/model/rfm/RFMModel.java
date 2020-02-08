package io.sugo.services.usergroup.model.rfm;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import org.joda.time.DateTime;
import org.joda.time.Days;

import java.util.Calendar;
import java.util.Date;

/**
 */
public class RFMModel {

    public static final int R = 0;
    public static final int F = 1;
    public static final int M = 2;

    private String userId;

    private int recency;  //由lastTime计算而来

    private int frequency;
    private double monetary;

    @JsonCreator
    public RFMModel(
        @JsonProperty("userId")String userId,
        @JsonProperty("lastTime")Date lastTime,
        @JsonProperty("frequency")Integer frequency,
        @JsonProperty("monetary")Double monetary)
    {
        Preconditions.checkArgument(
            userId != null && lastTime != null && frequency != null && monetary != null,
            "there is null value for rfmData:[userId=%s, lastTime=%s, frequency=%s, monetary=%s]",
            userId, lastTime, frequency, monetary);
        this.userId = userId;
        this.lastTime = lastTime;
        this.frequency = frequency;
        this.monetary = monetary;
    }

    /**
     * Last purchase time
     */
    private Date lastTime;

    private String rLabel;
    private String fLabel;
    private String mLabel;

    public String getGroup() {
        return rLabel + ":" + fLabel + ":" + mLabel;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public int getRecency() {
        if (recency == 0) {
            recency = Days.daysBetween(new DateTime(lastTime), DateTime.now()).getDays();
        }
        return recency;
    }

    public void setRecency(int recency) {
        this.recency = recency;
    }

    public int getFrequency() {
        return frequency;
    }

    public void setFrequency(int frequency) {
        this.frequency = frequency;
    }

    public double getMonetary() {
        return monetary;
    }

    public void setMonetary(double monetary) {
        this.monetary = monetary;
    }

    public String getrLabel() {
        return rLabel;
    }

    public Date getLastTime() {
        return lastTime;
    }

    public void setLastTime(Date lastTime) {
        this.lastTime = lastTime;
    }

    public RFMModel setrLabel(String rLabel) {
        this.rLabel = rLabel;
        return this;
    }

    public String getfLabel() {
        return fLabel;
    }

    public RFMModel setfLabel(String fLabel) {
        this.fLabel = fLabel;
        return this;
    }

    public String getmLabel() {
        return mLabel;
    }

    public RFMModel setmLabel(String mLabel) {
        this.mLabel = mLabel;
        return this;
    }
}
