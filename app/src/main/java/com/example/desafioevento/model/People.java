package com.example.desafioevento.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class People implements Serializable {
    //{ "eventId": "1", "name": "Otávio", "email": "otavio_souza@..." }
    @SerializedName("eventId")
    private String eventId;

    @SerializedName("name")
    private String name;

    @SerializedName("email")
    private String email;

    public People(){

    }
    public People(String eventId, String name, String email) {
        this.eventId = eventId;
        this.name = name;
        this.email = email;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
