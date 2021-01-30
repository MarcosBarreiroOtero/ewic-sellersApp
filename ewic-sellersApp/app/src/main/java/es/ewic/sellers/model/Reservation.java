package es.ewic.sellers.model;

import java.io.Serializable;
import java.util.Calendar;

public class Reservation implements Serializable {

    private int idReservation;
    private Calendar date;
    private String state;
    private String remarks;
    private int nClients;
    private String idGoogleLoginClient;
    private int idShop;
    private String clientName;

    public Reservation(int idReservation, Calendar date, String state, String remarks, int nClients, String idGoogleLoginClient, int idShop, String clientName) {
        this.idReservation = idReservation;
        this.date = date;
        this.state = state;
        this.remarks = remarks;
        this.nClients = nClients;
        this.idGoogleLoginClient = idGoogleLoginClient;
        this.idShop = idShop;
        this.clientName = clientName;
    }

    public Reservation(Calendar date, String remarks, int nClients, String idGoogleLoginClient, int idShop) {
        this.date = date;
        this.remarks = remarks;
        this.nClients = nClients;
        this.idGoogleLoginClient = idGoogleLoginClient;
        this.idShop = idShop;
    }

    public int getIdReservation() {
        return idReservation;
    }

    public void setIdReservation(int idReservation) {
        this.idReservation = idReservation;
    }

    public Calendar getDate() {
        return date;
    }

    public void setDate(Calendar date) {
        this.date = date;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public int getnClients() {
        return nClients;
    }

    public void setnClients(int nClients) {
        this.nClients = nClients;
    }


    public String getIdGoogleLoginClient() {
        return idGoogleLoginClient;
    }

    public void setIdGoogleLoginClient(String idGoogleLoginClient) {
        this.idGoogleLoginClient = idGoogleLoginClient;
    }

    public int getIdShop() {
        return idShop;
    }

    public void setIdShop(int idShop) {
        this.idShop = idShop;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    @Override
    public String toString() {
        return "Reservation{" +
                "idReservation=" + idReservation +
                ", date=" + date +
                ", state='" + state + '\'' +
                ", remarks='" + remarks + '\'' +
                ", nClients=" + nClients +
                ", idGoogleLoginClient='" + idGoogleLoginClient + '\'' +
                ", idShop=" + idShop +
                ", clientName='" + clientName + '\'' +
                '}';
    }
}
