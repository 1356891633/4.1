//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.aabb.dto;

public class MaintainRequest extends Request {
    private final int elevatorId;

    public MaintainRequest(int elevatorId) {
        this.elevatorId = elevatorId;
    }

    public int getElevatorId() {
        return this.elevatorId;
    }

    public String toString() {
        return String.format("MAINTAIN_ACCEPT-%d", this.elevatorId);
    }


}
