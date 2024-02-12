package com.doroshenko.client;

public class Training {
    public Integer RoomId;
    public String Group;
    public String Date;
    public String TimeStart;
    public String TimeEnd;
    public String Description;

    public Training(Integer room, String group, String date, String timeStart, String timeEnd, String description) {
        RoomId = room;
        Group = group;
        Date = date;
        TimeStart = timeStart;
        TimeEnd = timeEnd;
        Description = description;
    }

    public Integer getRoomId() {
        return RoomId;
    }

    public void setRoomId(Integer roomId) {
        RoomId = roomId;
    }

    public String getGroup() {
        return Group;
    }

    public void setGroup(String group) {
        Group = group;
    }

    public String getDate() {
        return Date;
    }

    public void setDate(String date) {
        Date = date;
    }

    public String getTimeStart() {
        return TimeStart;
    }

    public void setTimeStart(String timeStart) {
        TimeStart = timeStart;
    }

    public String getTimeEnd() {
        return TimeEnd;
    }

    public void setTimeEnd(String timeEnd) {
        TimeEnd = timeEnd;
    }

    public String getDescription() {
        return Description;
    }

    public void setDescription(String description) {
        Description = description;
    }
}
