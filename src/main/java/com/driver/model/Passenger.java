package com.driver.model;


import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "passenger")
public class Passenger {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int passengerId;

    private String name;

    private int age;

    //passengers and tickets are many to manny
    public Passenger() {
    }

    @ManyToMany
    @JoinColumn
    private List<Ticket> bookedTickets;

    public Passenger(int passengerId, String name, int age, List<Ticket> bookedTickets) {
        this.passengerId = passengerId;
        this.name = name;
        this.age = age;
        this.bookedTickets = bookedTickets;
    }

    public int getPassengerId() {
        return passengerId;
    }

    public void setPassengerId(int passengerId) {
        this.passengerId = passengerId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public List<Ticket> getBookedTickets() {
        return bookedTickets;
    }

    public void setBookedTickets(List<Ticket> bookedTickets) {
        this.bookedTickets = bookedTickets;
    }
}