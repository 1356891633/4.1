package com.aabb;


import com.aabb.status.ElevatorStatus;
import com.aabb.util.CommonUtil;
import com.oocourse.elevator2.PersonRequest;
import com.oocourse.elevator2.TimableOutput;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class Elevator implements Runnable {

    // private static final String ARRIVE_FORMAT = "[%.4f]ARRIVE-%d-%d %n";
    // private static final String CLOSE_FORMAT = "[%.4f]CLOSE-%d-%d %n";
    // private static final String OPEN_FORMAT = "[%.4f]OPEN-%d-%d %n";
    // private static final String IN_FORMAT = "[%.4f]IN-%d-%d-%d %n";
    // private static final String OUT_FORMAT = "[%.4f]OUT-%d-%d-%d %n";
    // private static final String MAINTAIN_ABLE_FORMAT = "[%.4f]MAINTAIN_ABLE-%d %n";

    private static final String ARRIVE_FORMAT = "ARRIVE-%s-%s";
    private static final String CLOSE_FORMAT = "CLOSE-%s-%s";
    private static final String OPEN_FORMAT = "OPEN-%s-%s";
    private static final String IN_FORMAT = "IN-%s-%s-%s";
    private static final String OUT_FORMAT = "OUT-%s-%s-%s";
    private static final String MAINTAIN_ABLE_FORMAT = "MAINTAIN_ABLE-%s";


    // 开门需要的时间，单位：毫秒
    private static final int OPEN_TIME = 200;
    // 关门需要的时间，单位：毫秒
    private static final int CLOSE_TIME = 200;
    private static final int MAX_FLOOR = 11;
    private volatile boolean canRunning = true;
    // 满载人数 默认为6
    private int maxPersonNum = 6;
    // 移动一层楼需要的时间，单位：毫秒 默认400 也就是0.4s
    private int moveTime = 400;
    // 电梯id
    private int id;
    // 电梯的状态
    private volatile ElevatorStatus elevatorStatus;
    // 电梯中的人数
    private int passengerNum;
    // 在电梯里的人
    private List<PersonRequest> inPerson;
    // 在等这部电梯的人
    private List<PersonRequest> waitPerson;
    // 当前电梯所在楼层
    private int currentFloor;
    public Elevator(int id) {
        this.id = id;
        this.currentFloor = 1;
        this.inPerson = new ArrayList<>();
        this.waitPerson = new ArrayList<>();
        this.elevatorStatus = ElevatorStatus.IDLE;
    }

    public void setCanRunning(boolean canRunning) {
        this.canRunning = canRunning;
    }

    @Override
    public void run() {
        while (canRunning) {
            // 维护开始
            if (getStatus() == ElevatorStatus.MAINTAIN_START) {
                // 将所有人在原地放下
                if (!CommonUtil.isEmptyCollection(inPerson)) {
                    Boolean openTheDoor = maintainOpenDoor();
                    getEveryoneOffElevator();
                    closeDoor(openTheDoor);
                    // 重新调度这些人
                    for (PersonRequest person : inPerson) {
                        ElevatorSchedule.addRequest(new PersonRequest(currentFloor, person.getToFloor(), person.getPersonId()));
                    }
                }
                // 如果有人在等这部电梯,则将他们重新调度
                if (!CommonUtil.isEmptyCollection(waitPerson)) {
                    for (PersonRequest person : waitPerson) {
                        ElevatorSchedule.addRequest(new PersonRequest(person.getFromFloor(), person.getToFloor(), person.getPersonId()));

                    }
                }

                // 开始维护
                TimableOutput.println(String.format(MAINTAIN_ABLE_FORMAT, id));

                // 将电梯退出系统
                canRunning = false;
                break;
            }

            if (inPerson.size() == 0 && waitPerson.size() == 0) {
                synchronized (this) {
                    try {
                        this.wait();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }


            // 上行
            if (getStatus() == ElevatorStatus.UP) {
                this.currentFloor++;
                try {
                    TimeUnit.MILLISECONDS.sleep(moveTime);
                    TimableOutput.println(String.format(ARRIVE_FORMAT, currentFloor, id));
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                // 开门
                Boolean openTheDoor = openDoor();
                // 下电梯
                outPassenger();
                // 上电梯
                inPersonRequest();

                // 关门
                closeDoor(openTheDoor);
            }

            // 下行
            if (getStatus() == ElevatorStatus.DOWN) {
                this.currentFloor--;
                try {
                    TimeUnit.MILLISECONDS.sleep(moveTime);
                    TimableOutput.println(String.format(ARRIVE_FORMAT, currentFloor, id));
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                // 开门
                Boolean openTheDoor = openDoor();
                // 下电梯
                outPassenger();
                // 上电梯
                inPersonRequest();
                // 关门
                closeDoor(openTheDoor);
            }

            // 空闲
            if (getStatus() == ElevatorStatus.IDLE) {
                if (!CommonUtil.isEmptyCollection(waitPerson)) {
                    // 取优先级最高的人
                    PersonRequest request = waitPerson.get(0);
                    // 出发楼层大于当前楼层 所以要上
                    int result = request.getFromFloor() - currentFloor;
                    if (result > 0) {
                        this.setStatus(ElevatorStatus.UP);
                    } else if (result == 0) {
                        if (request.getToFloor() - request.getFromFloor() > 0) {
                            this.setStatus(ElevatorStatus.UP);
                        } else {
                            this.setStatus(ElevatorStatus.DOWN);
                        }
                    } else {
                        this.setStatus(ElevatorStatus.DOWN);
                    }
                    Boolean openTheDoor = openDoor();
                    inPersonRequest();
                    closeDoor(openTheDoor);
                }
            }


        }
    }

    /**
     * 让所有人下电梯
     */
    private void getEveryoneOffElevator() {
        for (PersonRequest person : inPerson) {
            TimableOutput.println(String.format(OUT_FORMAT, person.getPersonId(), currentFloor, id));
        }
    }

    private void closeDoor(Boolean openTheDoor) {
        if (openTheDoor) {
            // 关门
            try {
                TimeUnit.MILLISECONDS.sleep(CLOSE_TIME);
                TimableOutput.println(String.format(CLOSE_FORMAT, currentFloor, id));
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private Boolean openDoor() {
        List<PersonRequest> remove = inPerson.stream()
                .filter(d -> d.getToFloor() == this.getCurrentFloor())
                .collect(Collectors.toList());

        List<PersonRequest> currentWaitPerson = waitPerson.stream()
                .filter(o -> o.getFromFloor() == this.currentFloor)
                .collect(Collectors.toList());
        // 没有人要上下
        if (CommonUtil.isEmptyCollection(remove) && CommonUtil.isEmptyCollection(currentWaitPerson)) {
            return false;
        }
        // 需要开门时 发现电梯里面没有人 则将状态设置为IDEL
        if (CommonUtil.isEmptyCollection(inPerson)) {
            setStatus(ElevatorStatus.IDLE);
        }
        // 开门
        try {
            TimeUnit.MILLISECONDS.sleep(OPEN_TIME);
            TimableOutput.println(String.format(OPEN_FORMAT, currentFloor, id));
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return true;
    }

    private Boolean maintainOpenDoor() {
        // 开门
        try {
            TimeUnit.MILLISECONDS.sleep(OPEN_TIME);
            TimableOutput.println(String.format(OPEN_FORMAT, currentFloor, id));
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return true;
    }


    // 上乘客
    public void inPersonRequest() {
        synchronized (this) {
            // 满载了 直接不需要加人
            if (this.passengerNum == maxPersonNum) {
                return;
            }

            List<PersonRequest> currentWaitPerson = waitPerson.stream()
                    .filter(o -> o.getFromFloor() == this.currentFloor)
                    .collect(Collectors.toList());
            if (CommonUtil.isEmptyCollection(currentWaitPerson)) {
                return;
            }

            if (this.getStatus().equals(ElevatorStatus.IDLE)) {
                PersonRequest request = waitPerson.get(0);
                boolean up = request.getToFloor() - currentFloor > 0;
                if (up) {
                    List<PersonRequest> upPerson = getUpPerson();
                    for (PersonRequest person : upPerson) {
                        TimableOutput.println(String.format(IN_FORMAT, person.getPersonId(), currentFloor, id));

                    }
                    inPerson.addAll(upPerson);
                    setStatus(ElevatorStatus.UP);
                } else {
                    List<PersonRequest> upPerson = getDownPerson();
                    for (PersonRequest person : upPerson) {
                        TimableOutput.println(String.format(IN_FORMAT, person.getPersonId(), currentFloor, id));
                    }
                    inPerson.addAll(upPerson);
                    setStatus(ElevatorStatus.DOWN);
                }

            }
            // 当前状态为向上 加向上的人
            if (this.getStatus().equals(ElevatorStatus.UP)) {
                // 到一楼后不能继续向下 如果有人要进来 则一定向上
                if (currentFloor == MAX_FLOOR) {
                    List<PersonRequest> downPerson = getDownPerson();
                    if (!CommonUtil.isEmptyCollection(downPerson)) {
                        inPerson.addAll(downPerson);
                        for (PersonRequest person : downPerson) {
                            TimableOutput.println(String.format(IN_FORMAT, person.getPersonId(), currentFloor, id));
                        }
                        this.setStatus(ElevatorStatus.DOWN);
                    }
                } else {
                    List<PersonRequest> upPerson = getUpPerson();
                    for (PersonRequest person : upPerson) {
                        TimableOutput.println(String.format(IN_FORMAT, person.getPersonId(), currentFloor, id));
                    }
                    inPerson.addAll(upPerson);
                }
            }
            // 当前状态为向下 加向下的人
            if (this.getStatus().equals(ElevatorStatus.DOWN)) {
                // 到一楼后不能继续向下 如果有人要进来 则一定向上
                if (currentFloor == 1) {
                    List<PersonRequest> upPerson = getUpPerson();
                    if (!CommonUtil.isEmptyCollection(upPerson)) {
                        for (PersonRequest person : upPerson) {
                            TimableOutput.println(String.format(IN_FORMAT, person.getPersonId(), currentFloor, id));
                        }
                        inPerson.addAll(upPerson);
                        this.setStatus(ElevatorStatus.UP);
                    }
                } else {
                    List<PersonRequest> downPerson = getDownPerson();
                    if (!CommonUtil.isEmptyCollection(downPerson)) {
                        for (PersonRequest person : downPerson) {
                            TimableOutput.println(String.format(IN_FORMAT, person.getPersonId(), currentFloor, id));
                        }
                        inPerson.addAll(downPerson);
                    }
                }
            }
        }
    }

    private List<PersonRequest> getDownPerson() {
        List<PersonRequest> downPerson = waitPerson.stream()
                .limit(maxPersonNum - passengerNum)
                .filter(o -> o.getFromFloor() == this.currentFloor)
                .collect(Collectors.toList());
        waitPerson = waitPerson.stream()
                .filter(o -> !downPerson.stream()
                        .allMatch(o2 -> o.getPersonId() == o2.getPersonId()))
                .collect(Collectors.toList());
        return downPerson;
    }

    private List<PersonRequest> getUpPerson() {
        List<PersonRequest> upPerson = waitPerson.stream()
                .filter(o -> o.getFromFloor() == this.currentFloor)
                .limit(maxPersonNum - passengerNum)
                .collect(Collectors.toList());
        waitPerson = waitPerson.stream()
                .filter(o -> !upPerson.stream()
                        .allMatch(o2 -> o.getPersonId() == o2.getPersonId()))
                .collect(Collectors.toList());
        return upPerson;
    }

//    public void addWaitPerson(PersonRequest personRequest) {
//        this.waitPerson.add(personRequest);
//    }

    // 判断当前楼层需要下的乘客下电梯
    public void outPassenger() {
        synchronized (this) {
            List<PersonRequest> remove = inPerson.stream()
                    .filter(d -> d.getToFloor() == this.getCurrentFloor())
                    .collect(Collectors.toList());

            if (remove.isEmpty()) {
                return;
            }
            for (PersonRequest person : remove) {
                TimableOutput.println(String.format(OUT_FORMAT, person.getPersonId(), currentFloor, id));
            }
            // 下电梯
            inPerson = inPerson.stream().filter(d -> d.getToFloor() != this.getCurrentFloor())
                    .collect(Collectors.toList());

            this.passengerNum = inPerson.size();
            if (passengerNum == 0) {
                setStatus(ElevatorStatus.IDLE);
            }

            // 到这肯定还有人在电梯上
            if (getStatus() == ElevatorStatus.UP && !needUp()) {
                setStatus(ElevatorStatus.DOWN);
            }
            if (getStatus() == ElevatorStatus.DOWN && !needDown()) {
                setStatus(ElevatorStatus.UP);
            }
        }
    }

    private boolean needUp() {
        for (PersonRequest person : inPerson) {
            if (currentFloor < person.getToFloor()) {
                return true;
            }
        }
        return false;
    }

    private boolean needDown() {
        for (PersonRequest person : inPerson) {
            if (currentFloor > person.getToFloor()) {
                return true;
            }
        }
        return false;
    }


    // 判断电梯是否满载
    public boolean isFull() {
        synchronized (this) {
            return inPerson.size() < maxPersonNum;
        }
    }

    public Boolean isUp() {
        synchronized (this) {
            return this.elevatorStatus == ElevatorStatus.UP;
        }
    }

    public Boolean isDown() {
        synchronized (this) {
            return this.elevatorStatus == ElevatorStatus.DOWN;
        }
    }

    public int getCurrentFloor() {
        synchronized (this) {
            return currentFloor;
        }
    }

    public void setCurrentFloor(int currentFloor) {
        this.currentFloor = currentFloor;
    }

    public ElevatorStatus getStatus() {
        synchronized (this) {
            return elevatorStatus;
        }
    }

    public void setStatus(ElevatorStatus elevatorStatus) {
        synchronized (this) {
            // 如果被维护了,就不能继续更新状态,以维护结束
            if (this.elevatorStatus == ElevatorStatus.MAINTAIN_START) {
                return;
            }
            this.elevatorStatus = elevatorStatus;
        }
    }

    /**
     * 根据出发楼层 和向上还是向下 来计算距离
     *
     * @param formFloor
     * @param up
     * @return 距离
     */

    public int getDistance(int formFloor, Boolean up) {
        int maxToFloor = 0;
        int minToFloor = 0;
        for (PersonRequest destination : this.inPerson) {
            if (maxToFloor <= destination.getToFloor()) {
                maxToFloor = destination.getToFloor();
            }
            if (minToFloor > destination.getToFloor()) {
                minToFloor = destination.getToFloor();
            }
        }
        // 电梯向上
        if (this.getStatus().equals(ElevatorStatus.UP)) {
            // 目标也上行
            if (up) {
                // 当前楼层在出发楼层下面
                if ((formFloor - currentFloor) >= 0) {
                    return formFloor - currentFloor;
                } else {
                    // 先到当前需要去的最大层 然后返回接人
                    return (maxToFloor - currentFloor) + (maxToFloor - formFloor);
                }
            } else {
                // 目标下行
                // 先到当前需要去的最大层 然后返回接人
                return (maxToFloor - currentFloor) + (maxToFloor - formFloor);
            }
        }
        // 向下
        if (this.getStatus().equals(ElevatorStatus.DOWN)) {
            if (up) {
                // 先到当前需要去的最小层 然后返回接人
                return (currentFloor - minToFloor) + (formFloor - minToFloor);
            } else {
                // 当前楼层在出发楼层上面 顺路接
                if ((currentFloor - formFloor) >= 0) {
                    return formFloor - currentFloor;
                } else {
                    // 先到当前需要去的最小层 然后返回接人
                    return (currentFloor - minToFloor) + (formFloor - minToFloor);
                }
            }
        }
        return Integer.MAX_VALUE;
    }

    public void addWaitPersonRequest(PersonRequest ex) {
        synchronized (this) {
            waitPerson.add(ex);
        }
    }

    /**
     * 设置移动一层的时间
     *
     * @param time 输入例子是0.2s 要转换
     */
    public void setMoveTime(double time) {
        this.moveTime = (int)(time * 1000);
    }

    public void setMaxPersonNum(int maxPersonNum) {
        this.maxPersonNum = maxPersonNum;
    }

    public int getId() {
        return id;
    }

}
