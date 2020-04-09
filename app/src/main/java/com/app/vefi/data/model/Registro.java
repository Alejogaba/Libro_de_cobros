package com.app.vefi.data.model;

import java.util.Comparator;

public class Registro {
    private int day;
    private int month;
    private int year;
    private String descripcion;
    private float valor;
    private String registroId;

    public Registro() {
    }

    public Registro(int day, String descripcion,int month,String registroId, float valor, int year) {
        this.setDay(day);
        this.setMonth(month);
        this.setYear(year);
        this.setDescripcion(descripcion);
        this.setValor(valor);
        this.setRegistroId(registroId);
    }

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public float getValor() {
        return valor;
    }

    public void setValor(float valor) {
        this.valor = valor;
    }

    public String getRegistroId() {
        return registroId;
    }

    public void setRegistroId(String registroId) {
        this.registroId = registroId;
    }


    @Override
    public String toString() {
        return  getDay() + "   " + getDescripcion() + "   "+
                "   " + getValor();
    }


}

