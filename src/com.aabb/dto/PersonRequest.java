package com.aabb.dto;

import com.oocourse.elevator2.Request;

public class PersonRequest extends Request {
    private int fromFloor;
    private int toFloor;
    private int personId;

    public PersonRequest(int fromFloor, int toFloor, int personId) {
        this.fromFloor = fromFloor;
        this.toFloor = toFloor;
        this.personId = personId;
    }

    public int getFromFloor() {
        return fromFloor;
    }

    public void setFromFloor(int fromFloor) {
        this.fromFloor = fromFloor;
    }

    public int getToFloor() {
        return toFloor;
    }

    public void setToFloor(int toFloor) {
        this.toFloor = toFloor;
    }

    public int getPersonId() {
        return personId;
    }

    public void setPersonId(int personId) {
        this.personId = personId;
    }
}