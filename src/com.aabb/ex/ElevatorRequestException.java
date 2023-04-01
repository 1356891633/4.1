package com.aabb.ex;

public class ElevatorRequestException extends Exception {
    private final String original;

    public ElevatorRequestException(String original) {
        super(String.format("Elevator request parse failed! - \"%s\"", original));
        this.original = original;
    }

    public String getOriginal() {
        return this.original;
    }
}
