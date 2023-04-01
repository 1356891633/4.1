package com.aabb.util;

import com.aabb.Elevator;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Passenger {

    String inputString = "";

    private int fromFloor;
    private int toFloor;
    private int passengerId;
    private boolean isUp;
    private double time;
    private Integer maintainElevatorId;
    private Elevator elevator;


    public Passenger(String inputString) {
        this.inputString = inputString;
    }

    private void parseTime() {
        Pattern pattern = Pattern.compile("(?<=\\[).+?(?=\\])");
        Matcher m = pattern.matcher(this.inputString);
        while (m.find()) {
            this.time = Double.parseDouble(m.group(0));
        }
    }

    private void parseMaintain() {
        if (inputString.contains("MAINTAIN-Elevator")) {
            String[] split = inputString.split("-");
            // 标记维护电梯
            this.maintainElevatorId = Integer.parseInt(split[2]);
        }
    }

    private void parseAddElevator() {
        if (inputString.contains("ADD-Elevator")) {
            String[] split = inputString.split("-");
            // 创建电梯
            elevator = new Elevator(Integer.parseInt(split[2]));
            elevator.setMoveTime(Double.parseDouble(split[5]));
            elevator.setCurrentFloor(Integer.parseInt(split[3]));
            elevator.setMaxPersonNum(Integer.parseInt(split[4]));

        }
    }

    private void parsePassengerId() {
        Pattern pattern = Pattern.compile("(?<=\\]).+?(?=\\-)");
        Matcher m = pattern.matcher(this.inputString);
        while (m.find()) {
            this.passengerId = Integer.parseInt(this.inputString.substring(m.start(), m.end()));
        }
    }


    private void parseFromFloor() {  //[9.6]1-FROM-4-TO-5
        Pattern pattern = Pattern.compile("(?<=\\-).+?(?=\\-)");
        Matcher m = pattern.matcher(this.inputString);
        int cnt = 0;
        while (m.find()) {
            cnt++;
            if (cnt == 2) {
                this.fromFloor = Integer.parseInt(this.inputString.substring(m.start(), m.end()));
            }
        }
    }

    public void parseToFloor() {
        this.toFloor = Integer.parseInt(this.inputString.substring(this.inputString.lastIndexOf('-') + 1));
    }

    private void isUp() {
        this.isUp = this.fromFloor > this.toFloor;
    }

    public void parse() {
        this.parseTime();
        this.parseMaintain();
        this.parseAddElevator();
        if (maintainElevatorId != null || elevator != null) {
            return;
        }
        this.parsePassengerId();
        this.parseTime();
        this.parseFromFloor();
        this.parseToFloor();
        this.isUp();
    }

    public void putout() {
        System.out.println(this.inputString);
        System.out.println(this.time);
        System.out.println(this.passengerId);
        System.out.println(this.fromFloor);
        System.out.println(this.toFloor);
        System.out.println(this.isUp);
    }

    public String getInputString() {
        return inputString;
    }

    public int getFromFloor() {
        return fromFloor;
    }

    public int getToFloor() {
        return toFloor;
    }

    public int getPassengerId() {
        return passengerId;
    }

    public double getTime() {
        return time;
    }

    public Integer getMaintainElevatorId() {
        return maintainElevatorId;
    }

    public Elevator getElevator() {
        return elevator;
    }
}
