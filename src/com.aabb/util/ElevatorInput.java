package com.aabb.util;


import com.aabb.dto.*;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

public class ElevatorInput implements Closeable {

    private final Scanner scanner;

    public ElevatorInput(InputStream in) {
        this.scanner = new Scanner(in);
    }

    public void close() throws IOException {
        this.scanner.close();
    }

    public Request nextRequest() {
        while (this.scanner.hasNextLine()) {
            String line = this.scanner.nextLine();

            try {
                Passenger passenger = new Passenger(line);
                passenger.parse();
                double time = passenger.getTime();
                if (passenger.getMaintainElevatorId() != null) {
                    MaintainRequest sonMaintainRequest = new MaintainRequest(passenger.getMaintainElevatorId());
                    sonMaintainRequest.setRequestTime(time);
                    return sonMaintainRequest;
                } else if (passenger.getElevator() != null) {
                    ElevatorRequest elevatorRequest = new ElevatorRequest(passenger.getElevator());
                    elevatorRequest.setRequestTime(time);
                    return elevatorRequest;
                } else {
                    PersonRequest personRequest = new PersonRequest(passenger.getFromFloor(), passenger.getToFloor(),
                            passenger.getPassengerId());
                    ExtensionPersonRequest request = new ExtensionPersonRequest(personRequest);
                    request.setRequestTime(time);
                    return request;
                }

            } catch (Exception var3) {
                var3.printStackTrace(System.err);
            }
        }

        return null;
    }

}
