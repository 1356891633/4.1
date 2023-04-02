package com.aabb;


import com.aabb.status.ElevatorStatus;
import com.oocourse.elevator2.ElevatorRequest;
import com.oocourse.elevator2.MaintainRequest;
import com.oocourse.elevator2.PersonRequest;
import com.oocourse.elevator2.Request;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

public class ElevatorSchedule {

    private static final ConcurrentLinkedQueue<Request> requests = new ConcurrentLinkedQueue<>();

    private static List<Elevator> elevators; // 电梯列表


    public static void setElevators(List<Elevator> elevators) {
        ElevatorSchedule.elevators = elevators;
    }

    public static void schedule() {
        Request request = requests.poll();
        if (request == null) {
        } else if (request instanceof PersonRequest) {
            Elevator bestElevator = null;
            int minDistance = Integer.MAX_VALUE;
            List<Elevator> idleElevator = elevators.stream().filter(o -> o.getStatus().equals(ElevatorStatus.IDLE))
                    .collect(Collectors.toList());

            for (Elevator elevator : idleElevator) {
                // 如果电梯处于空闲状态，直接选择该电梯
                int distance = Math.abs(elevator.getCurrentFloor() - ((PersonRequest)request).getToFloor());
                if (minDistance > distance) {
                    bestElevator = elevator;
                    minDistance = distance;
                }
            }
            if (bestElevator == null) {
                for (Elevator elevator : elevators) {
                    Boolean up = ((PersonRequest)request).getToFloor() - ((PersonRequest)request).getFromFloor() > 0;
                    int distance = elevator.getDistance(((PersonRequest)request).getFromFloor(), up);
                    if (distance < minDistance) { // 选择距离最近的电梯
                        bestElevator = elevator;
                        minDistance = distance;
                    }
                }
            }
            bestElevator.addWaitPersonRequest((PersonRequest)request);
            synchronized (bestElevator) {
                bestElevator.notifyAll();
            }
        } else if (request instanceof ElevatorRequest) {
            Elevator elevator = new Elevator(((ElevatorRequest)request).getElevatorId());
            addElevator(elevator);
        } else if (request instanceof MaintainRequest) {
            Optional<Elevator> first = elevators.stream().filter(o -> o.getId() == ((MaintainRequest)request).getElevatorId()).findFirst();
            if (first.isPresent()) {
                Elevator elevator = first.get();
                elevator.setStatus(ElevatorStatus.MAINTAIN_START);
                synchronized (elevator) {
                    elevator.notifyAll();
                }
            }
        }


    }

    public static void addRequest(Request request) {
        requests.add(request);
        ElevatorSchedule.schedule();
    }

    public static void addElevator(Elevator elevator) {
        elevators.add(elevator);
        new Thread(elevator).start();
    }

    public static void start() {
        // 电梯开始运行
        for (Elevator elevator : elevators) {
            new Thread(elevator).start();
        }
    }

    public static void stop() {
        // 电梯开始运行
        for (Elevator elevator : elevators) {
            elevator.setCanRunning(false);
        }
    }

}


