package com.example.desafioevento.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class PeopleEventoDTO implements Serializable {

    @SerializedName("nome")
    private String nome;

    @SerializedName("email")
    private String email;

    @SerializedName("eventId")
    private String eventId;

    @SerializedName("body")
    private String text;
    public PeopleEventoDTO(){

    }
    public PeopleEventoDTO(String nome, String email, String eventId) {
        this.nome = nome;
        this.email = email;
        this.eventId = eventId;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }
}
