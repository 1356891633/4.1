package com.aabb;

import com.oocourse.elevator2.ElevatorInput;
import com.oocourse.elevator2.Request;
import com.oocourse.elevator2.TimableOutput;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author yellowgg
 * @since 1.1.0
 */
public class MainClass {

    public static void main(String[] args) throws IOException {
        TimableOutput.initStartTimestamp();

        ElevatorInput elevatorInput = new ElevatorInput(System.in);
        List<Elevator> elevators = new ArrayList<>();
        for (int i = 1; i <= 6; i++) {
            Elevator elevator = new Elevator(i);
            elevators.add(elevator);
        }

        ElevatorSchedule.setElevators(elevators);
        ElevatorSchedule.start();

        while (true) {
            Request request = elevatorInput.nextRequest();
            if (request == null) {
                break;
            } else {
                ElevatorSchedule.addRequest(request);
            }
        }
        elevatorInput.close();
    }


}
