package com.example.behavior_based_authentication;

public class ThreeDPoint {
    private long timestamp;
    private float x, y ,z;

    public ThreeDPoint(long timestamp, float x, float y, float z) {
        this.timestamp = timestamp;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public long getTimestamp() { return timestamp; }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getZ() {
        return z;
    }

    @Override
    public String toString() {
        return "ThreeDPoint{" +
                "timestamp=" + timestamp +
                ", x=" + x +
                ", y=" + y +
                ", z=" + z +
                '}';
    }
}
