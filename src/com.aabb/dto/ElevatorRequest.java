package com.aabb.dto;


import com.aabb.Elevator;

public class ElevatorRequest extends Request {

    private Elevator elevator;


    public ElevatorRequest(Elevator elevator) {
        this.elevator = elevator;
    }

    public Elevator getElevator() {
        return elevator;
    }

    public void setElevator(Elevator elevator) {
        this.elevator = elevator;
    }


}
