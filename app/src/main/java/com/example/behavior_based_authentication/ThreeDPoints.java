package com.example.behavior_based_authentication;

import java.util.ArrayList;

public class ThreeDPoints {
    private ArrayList<ThreeDPoint> threeDPoints = new ArrayList<>();

    public void Add3DPoint (ThreeDPoint threeDPoint) {
        threeDPoints.add(threeDPoint);
    }

    public void clearList () {
        threeDPoints.clear();
    }

    public ArrayList<ThreeDPoint> getThreeDPoints() {
        return threeDPoints;
    }
}

