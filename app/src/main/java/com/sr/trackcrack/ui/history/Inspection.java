package com.sr.trackcrack.ui.history;
public class Inspection {
    private String id;
    private String date;
    private int cracksFound;
    private boolean solved;

    public Inspection(String id, String date, int cracksFound, boolean solved) {
        this.id = id;
        this.date = date;
        this.cracksFound = cracksFound;
        this.solved = solved;
    }

    public String getId() {
        return id;
    }

    public String getDate() {
        return date;
    }

    public int getCracksFound() {
        return cracksFound;
    }

    public boolean isSolved() {
        return solved;
    }
}
