package es.ewic.sellers.model;

import java.io.Serializable;

public class ManualEntry implements Serializable {

    private int entryNumber;
    private String description;

    public ManualEntry(int entryNumber, String description) {
        this.entryNumber = entryNumber;
        this.description = description;
    }

    public int getEntryNumber() {
        return entryNumber;
    }

    public void setEntryNumber(int entryNumber) {
        this.entryNumber = entryNumber;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

}
