package com.driver.services;


import com.driver.EntryDto.BookTicketEntryDto;
import com.driver.model.Passenger;
import com.driver.model.Station;
import com.driver.model.Ticket;
import com.driver.model.Train;
import com.driver.repository.PassengerRepository;
import com.driver.repository.TicketRepository;
import com.driver.repository.TrainRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class TicketService {

    @Autowired
    TicketRepository ticketRepository;

    @Autowired
    TrainRepository trainRepository;

    @Autowired
    PassengerRepository passengerRepository;


    public Integer bookTicket(BookTicketEntryDto bookTicketEntryDto)throws Exception{

        //Check for validity
        //Use bookedTickets List from the TrainRepository to get bookings done against that train
        // Incase the there are insufficient tickets
        // throw new Exception("Less tickets are available");
        //otherwise book the ticket, calculate the price and other details
        //Save the information in corresponding DB Tables
        //Fare System : Check problem statement
        //Incase the train doesn't pass through the requested stations
        //throw new Exception("Invalid stations");
        //Save the bookedTickets in the train Object
        //Also in the passenger Entity change the attribute bookedTickets by using the attribute bookingPersonId.
        //And the end return the ticketId that has come from db

        Optional<Train> optionalTrain =  trainRepository.findById(bookTicketEntryDto.getTrainId());
        if (!optionalTrain.isPresent()) throw new Exception("Invalid Train");
        Train train = optionalTrain.get();

        String fromStation = String.valueOf(bookTicketEntryDto.getFromStation());
        String toStation = String.valueOf(bookTicketEntryDto.getToStation());
        if (!train.getRoute().contains(fromStation) || !train.getRoute().contains(toStation)
                || train.getRoute().indexOf(fromStation) >= train.getRoute().indexOf(toStation))
            throw new Exception("Invalid stations");

        String[] route = train.getRoute().split(",");
        Map<String,Integer> stationIndexMap = new HashMap<>();
        for (int i = 0; i < route.length; i++) {
            stationIndexMap.put(route[i],i);
        }
        int bookedTickets[]=new int[route.length];
        for (Ticket ticket: train.getBookedTickets()) {
            bookedTickets[stationIndexMap.get(ticket.getFromStation().toString())] += ticket.getPassengersList().size();
            bookedTickets[stationIndexMap.get(ticket.getToStation().toString())] -= ticket.getPassengersList().size();
        }
        for (int i = 1; i < route.length; i++) {
            bookedTickets[i]+=bookedTickets[i-1];
        }

        Integer from = stationIndexMap.get(fromStation);
        Integer to = stationIndexMap.get(toStation);

        Integer bookedSeats = 0;
        for (int i=from; i<=to ; i++){
            bookedSeats=Math.max(bookedSeats,bookedTickets[i]);
        }
        if (bookTicketEntryDto.getNoOfSeats() > (train.getNoOfSeats()-bookedSeats)) throw new Exception("Less tickets are available");

        Optional<Passenger> optionalPassenger =  passengerRepository.findById(bookTicketEntryDto.getBookingPersonId());
        if (!optionalPassenger.isPresent()) throw new Exception("Invalid Passenger");
        Passenger passenger = optionalPassenger.get();

        List<Passenger> passengers = new ArrayList<>();
        for (Integer passId:bookTicketEntryDto.getPassengerIds()){
            passengers.add(passengerRepository.findById(passId).get());
        }
        Ticket ticket = new Ticket();
        ticket.setFromStation(bookTicketEntryDto.getFromStation());
        ticket.setToStation(bookTicketEntryDto.getToStation());
        ticket.setTrain(train);
        ticket.setTotalFare((to-from)*300*passengers.size());
        ticket.setPassengersList(passengers);

        for (Passenger p: ticket.getPassengersList()) {
            p.getBookedTickets().add(ticket);
        }
        Ticket savedTicket = ticketRepository.save(ticket);

        train.getBookedTickets().add(savedTicket);

        trainRepository.save(train);

        return savedTicket.getTicketId();

    }
}