package com.aabb;

import com.aabb.dto.Request;
import com.aabb.util.ElevatorInput;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author yellowgg
 * @since 1.1.0
 */
public class MainClass {

    public static void main(String[] args) throws IOException {
        ElevatorInput elevatorInput = new ElevatorInput(System.in);
        List<Elevator> elevators = new ArrayList<>();
        for (int i = 1; i <= 6; i++) {
            Elevator elevator = new Elevator(i);
            elevators.add(elevator);
        }
        ElevatorSchedule levatorSchedule = new ElevatorSchedule(elevators);
        levatorSchedule.start();
        ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);
        while (true) {
            Request request = elevatorInput.nextRequest();
            if (request == null) {
                break;
            }
            scheduledExecutorService.schedule(() -> {
                levatorSchedule.addRequest(request);
                levatorSchedule.schedule();
            }, (long)request.getRequestTime() * 1000L, TimeUnit.MILLISECONDS);
        }
        elevatorInput.close();
    }


}
