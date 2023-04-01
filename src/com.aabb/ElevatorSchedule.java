package com.aabb;


import com.aabb.dto.ElevatorRequest;
import com.aabb.dto.ExtensionPersonRequest;
import com.aabb.dto.MaintainRequest;
import com.aabb.dto.Request;
import com.aabb.status.ElevatorStatus;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

public class ElevatorSchedule {

    private ConcurrentLinkedQueue<Request> requests;

    private List<Elevator> elevators; // 电梯列表

    public ElevatorSchedule(List<Elevator> elevators) {
        this.elevators = elevators;
        this.requests = new ConcurrentLinkedQueue<>();
    }

    public void addElevator(Elevator elevator) {
        elevators.add(elevator);
        new Thread(elevator).start();
    }

    public void start() {
        // 电梯开始运行
        for (Elevator elevator : elevators) {
            new Thread(elevator).start();
        }
    }

    public void schedule() {
        Request request = requests.poll();
        if (request == null) {
        } else if (request instanceof ExtensionPersonRequest) {
            Elevator bestElevator = null;
            int minDistance = Integer.MAX_VALUE;
            List<Elevator> idleElevator = elevators.stream().filter(o -> o.getStatus().equals(ElevatorStatus.IDLE))
                    .collect(Collectors.toList());

            for (Elevator elevator : idleElevator) {
                // 如果电梯处于空闲状态，直接选择该电梯
                int distance = Math.abs(elevator.getCurrentFloor() - ((ExtensionPersonRequest)request).getPersonRequest().getToFloor());
                if (minDistance > distance) {
                    bestElevator = elevator;
                    minDistance = distance;
                }
            }
            if (bestElevator == null) {
                for (Elevator elevator : elevators) {
                    Boolean up = ((ExtensionPersonRequest)request).getPersonRequest().getToFloor() - ((ExtensionPersonRequest)request).getPersonRequest().getFromFloor() > 0;
                    int distance = elevator.getDistance(((ExtensionPersonRequest)request).getPersonRequest().getFromFloor(), up);
                    if (distance < minDistance) { // 选择距离最近的电梯
                        bestElevator = elevator;
                        minDistance = distance;
                    }
                }
            }
            bestElevator.addWaitPersonRequest(((ExtensionPersonRequest)request));
            synchronized (bestElevator) {
                bestElevator.notifyAll();
            }
        } else if (request instanceof ElevatorRequest) {
            addElevator(((ElevatorRequest)request).getElevator());
        } else if (request instanceof MaintainRequest) {
            Optional<Elevator> first = elevators.stream().filter(o -> o.getId() == ((MaintainRequest)request).getElevatorId()).findFirst();
            if (first.isPresent()) {
                Elevator elevator = first.get();
                elevator.setStatus(ElevatorStatus.MAINTAIN_START);
                elevator.startMaintain((MaintainRequest)request);
                synchronized (elevator) {
                    elevator.notifyAll();
                }
            }
        }


    }

    public void addRequest(Request request) {
        requests.add(request);
    }

}


