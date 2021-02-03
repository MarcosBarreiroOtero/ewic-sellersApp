package es.ewic.sellers.model;

import java.io.Serializable;
import java.util.Calendar;

public class Entry implements Serializable {

    private int entryNumber;
    private Calendar start;
    private Calendar end;
    private long duration;
    private String description;
    private String shopName;
    private String clientName;

    public Entry(int entryNumber, Calendar start, Calendar end, long duration, String description, String shopName, String clientName) {
        this.entryNumber = entryNumber;
        this.start = start;
        this.end = end;
        this.duration = duration;
        this.description = description;
        this.shopName = shopName;
        this.clientName = clientName;
    }

    public int getEntryNumber() {
        return entryNumber;
    }

    public void setEntryNumber(int entryNumber) {
        this.entryNumber = entryNumber;
    }

    public Calendar getStart() {
        return start;
    }

    public void setStart(Calendar start) {
        this.start = start;
    }

    public Calendar getEnd() {
        return end;
    }

    public void setEnd(Calendar end) {
        this.end = end;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getShopName() {
        return shopName;
    }

    public void setShopName(String shopName) {
        this.shopName = shopName;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    @Override
    public String toString() {
        return "Entry{" +
                "entryNumber=" + entryNumber +
                ", start=" + start +
                ", end=" + end +
                ", duration=" + duration +
                ", description='" + description + '\'' +
                ", shopName='" + shopName + '\'' +
                ", clientName='" + clientName + '\'' +
                '}';
    }
}
