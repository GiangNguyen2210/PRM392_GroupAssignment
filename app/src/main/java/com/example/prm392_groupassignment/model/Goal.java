package com.example.prm392_groupassignment.model;

import com.google.gson.annotations.SerializedName;

public class Goal {
    @SerializedName("goalId")
    private int goalId;

    @SerializedName("goalName")
    private String goalName;


    public Goal(int goalId, String goalName) {
        this.goalId = goalId;
        this.goalName = goalName;
    }

    public Goal(String goalName) {
        this.goalName = goalName;
    }

    // Getters
    public int getGoalId() {
        return goalId;
    }

    public String getGoalName() {
        return goalName;
    }

    public void setGoalId(int goalId) {
        this.goalId = goalId;
    }

    public void setGoalName(String goalName) {
        this.goalName = goalName;
    }

    // Override toString for easy logging/debugging (optional)
    @Override
    public String toString() {
        return "Goal{" +
                "goalId=" + goalId +
                ", goalName='" + goalName + '\'' +
                '}';
    }
}
