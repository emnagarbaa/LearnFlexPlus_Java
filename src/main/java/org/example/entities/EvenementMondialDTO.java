package org.example.entities;

public class EvenementMondialDTO {
    private String id;
    private String title;
    private String category;
    private String country;
    private String start;
    private String end;
    private int phqAttendance;

    public String getId()            { return id; }
    public String getTitle()         { return title; }
    public String getCategory()      { return category; }
    public String getCountry()       { return country; }
    public String getStart()         { return start; }
    public String getEnd()           { return end; }
    public int    getPhqAttendance() { return phqAttendance; }
}