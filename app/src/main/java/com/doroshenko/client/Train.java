package com.doroshenko.client;

public class Train {
    public  String Room;
    public String Group;
    public String Date;
    public String TimeStart;
    public String TimeEnd;
    public String Description;

    public Train(String room, String group, String date, String timeStart, String timeEnd, String description) {
        Room = room;
        Group = group;
        Date = date;
        TimeStart = timeStart;
        TimeEnd = timeEnd;
        Description = description;
    }

    public String getRoom() {
        return Room;
    }

    public String getGroup() {
        return Group;
    }

    public String getDate() {
        return Date;
    }

    public String getTimeStart() {
        return TimeStart;
    }

    public String getTimeEnd() {
        return TimeEnd;
    }

    public String getDescription() {
        return Description;
    }
}
