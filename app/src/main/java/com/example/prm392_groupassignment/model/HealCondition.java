package com.example.prm392_groupassignment.model;

import com.example.prm392_groupassignment.activity.HealthConditionsActivity;
import com.google.gson.annotations.SerializedName;

public class HealCondition {
    @SerializedName("healthConditionId")
    private int healthConditionId;

    @SerializedName("healthConditionName")
    private String healthConditionName;

    @SerializedName("briefDescription")
    private String briefDescription;

    @SerializedName("healthConditionType")
    private String healthConditionType;

    public HealCondition(int healthConditionId, String healthConditionName, String briefDescription, String healthConditionType) {
        this.healthConditionId = healthConditionId;
        this.healthConditionName = healthConditionName;
        this.briefDescription = briefDescription;
        this.healthConditionType = healthConditionType;
    }

    public HealCondition(){}

    public int getHealthConditionId() {
        return healthConditionId;
    }

    public void setHealthConditionId(int healthConditionId) {
        this.healthConditionId = healthConditionId;
    }

    public String getHealthConditionName() {
        return healthConditionName;
    }

    public void setHealthConditionName(String healthConditionName) {
        this.healthConditionName = healthConditionName;
    }

    public String getBriefDescription() {
        return briefDescription;
    }

    public void setBriefDescription(String briefDescription) {
        this.briefDescription = briefDescription;
    }

    public String getHealthConditionType() {
        return healthConditionType;
    }

    public void setHealthConditionType(String healthConditionType) {
        this.healthConditionType = healthConditionType;
    }
}
