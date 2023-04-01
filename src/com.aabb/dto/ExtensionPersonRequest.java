package com.aabb.dto;


import java.util.Comparator;

public class ExtensionPersonRequest extends Request implements Comparator<ExtensionPersonRequest> {

    private PersonRequest personRequest;


    public ExtensionPersonRequest(PersonRequest personRequest) {
        this.personRequest = personRequest;
    }

    public PersonRequest getPersonRequest() {
        return personRequest;
    }

    public void setPersonRequest(PersonRequest personRequest) {
        this.personRequest = personRequest;
    }


    @Override
    public int compare(ExtensionPersonRequest o1, ExtensionPersonRequest o2) {
        return (int)(o1.getRequestTime() - o2.getRequestTime());
    }
}
