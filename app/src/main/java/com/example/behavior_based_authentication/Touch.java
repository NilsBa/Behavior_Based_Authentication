package com.example.behavior_based_authentication;
import android.os.Parcel;
import android.os.Parcelable;

public class Touch implements Parcelable {
    private float x;
    private float y;
    private float pressure;
    private float fingersize;
    private long downtime;
    private long eventtime;

    public Touch(float x, float y, float pressure, float fingersize, long downtime, long eventtime) {
        this.x = x;
        this.y = y;
        this.pressure = pressure;
        this.fingersize = fingersize;
        this.downtime = downtime;
        this.eventtime = eventtime;
    }

    protected Touch(Parcel in) {
        x = in.readFloat();
        y = in.readFloat();
        pressure = in.readFloat();
        fingersize = in.readFloat();
        downtime = in.readLong();
        eventtime = in.readLong();
    }

    public static final Creator<Touch> CREATOR = new Creator<Touch>() {
        @Override
        public Touch createFromParcel(Parcel in) {
            return new Touch(in);
        }

        @Override
        public Touch[] newArray(int size) {
            return new Touch[size];
        }
    };

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getPressure() {
        return pressure;
    }

    public float getFingersize() {
        return fingersize;
    }

    public long getDowntime() {
        return downtime;
    }

    public long getEventtime() {
        return eventtime;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeFloat(x);
        dest.writeFloat(y);
        dest.writeFloat(pressure);
        dest.writeFloat(fingersize);
        dest.writeLong(downtime);
        dest.writeLong(eventtime);
    }
}
