package com.driver.services;

import com.driver.EntryDto.AddTrainEntryDto;
import com.driver.EntryDto.SeatAvailabilityEntryDto;
import com.driver.model.Passenger;
import com.driver.model.Station;
import com.driver.model.Ticket;
import com.driver.model.Train;
import com.driver.repository.TrainRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.*;

@Service
public class TrainService {

    @Autowired
    TrainRepository trainRepository;

    public Integer addTrain(AddTrainEntryDto trainEntryDto) {

        //Add the train to the trainRepository
        //and route String logic to be taken from the Problem statement.
        //Save the train and return the trainId that is generated from the database.
        //Avoid using the lombok library
        Train train = new Train();
        train.setNoOfSeats(trainEntryDto.getNoOfSeats());
        train.setDepartureTime(trainEntryDto.getDepartureTime());

        StringBuilder route = new StringBuilder("");
        for (Station station : trainEntryDto.getStationRoute()) {
            route.append(station + ",");
        }
        route.deleteCharAt(route.length() - 1);
        train.setRoute(route.toString());

        Train savedTrain = trainRepository.save(train);
        return savedTrain.getTrainId();
    }

    public Integer calculateAvailableSeats(SeatAvailabilityEntryDto seatAvailabilityEntryDto) {

        //Calculate the total seats available
        //Suppose the route is A B C D
        //And there are 2 seats avaialble in total in the train
        //and 2 tickets are booked from A to C and B to D.
        //The seat is available only between A to C and A to B. If a seat is empty between 2 station it will be counted to our final ans
        //even if that seat is booked post the destStation or before the boardingStation
        //Inshort : a train has totalNo of seats and there are tickets from and to different locations
        //We need to find out the available seats between the given 2 stations.

        Optional<Train> optionalTrain = trainRepository.findById(seatAvailabilityEntryDto.getTrainId());
        if (!optionalTrain.isPresent()) return 0;
        Train train = optionalTrain.get();
        String[] route = train.getRoute().split(",");
        Map<String, Integer> stationIndexMap = new HashMap<>();
        for (int i = 0; i < route.length; i++) {
            stationIndexMap.put(route[i], i);
        }
        int bookedTickets[] = new int[route.length];
        for (Ticket ticket : train.getBookedTickets()) {
            bookedTickets[stationIndexMap.get(ticket.getFromStation().toString())] += ticket.getPassengersList().size();
            bookedTickets[stationIndexMap.get(ticket.getToStation().toString())] -= ticket.getPassengersList().size();
        }
        for (int i = 1; i < route.length; i++) {
            bookedTickets[i] += bookedTickets[i - 1];
        }

        Integer from = stationIndexMap.get(seatAvailabilityEntryDto.getFromStation().toString());
        Integer to = stationIndexMap.get(seatAvailabilityEntryDto.getToStation().toString());
        if (from == null || to == null || from >= to) return 0;
        Integer bookedSeats = 0;
        for (int i = from; i < to; i++) {
            bookedSeats = Math.max(bookedSeats, bookedTickets[i]);
        }
        return train.getNoOfSeats() - bookedSeats;
    }

    public Integer calculatePeopleBoardingAtAStation(Integer trainId, Station station) throws Exception {

        //We need to find out the number of people who will be boarding a train from a particular station
        //if the trainId is not passing through that station
        //throw new Exception("Train is not passing from this station");
        //  in a happy case we need to find out the number of such people.
        Optional<Train> optionalTrain = trainRepository.findById(trainId);
        if (!optionalTrain.isPresent()) return 0;
        Train train = optionalTrain.get();
        if (train.getRoute().indexOf("" + station) == -1) throw new Exception("Train is not passing from this station");
        Integer passengers = 0;
        for (Ticket ticket : train.getBookedTickets()) {
            if (ticket.getFromStation().equals(station)) passengers += ticket.getPassengersList().size();
        }
        return passengers;
    }

    public Integer calculateOldestPersonTravelling(Integer trainId) {

        //Throughout the journey of the train between any 2 stations
        //We need to find out the age of the oldest person that is travelling the train
        //If there are no people travelling in that train you can return 0
        Optional<Train> optionalTrain = trainRepository.findById(trainId);
        if (!optionalTrain.isPresent()) return 0;
        Train train = optionalTrain.get();

        Integer oldestAge = 0;
        for (Ticket ticket : train.getBookedTickets()) {
            for (Passenger passenger : ticket.getPassengersList()) {
                oldestAge = Math.max(oldestAge, passenger.getAge());
            }
        }
        return oldestAge;
    }

    public List<Integer> trainsBetweenAGivenTime(Station station, LocalTime startTime, LocalTime endTime) {

        //When you are at a particular station you need to find out the number of trains that will pass through a given station
        //between a particular time frame both start time and end time included.
        //You can assume that the date change doesn't need to be done ie the travel will certainly happen with the same date (More details
        //in problem statement)
        //You can also assume the seconds and milli seconds value will be 0 in a LocalTime format.

        List<Train> trains = trainRepository.findAll();
        List<Integer> trainsPassingAtGivenTime = new ArrayList<>();

        Integer sTime = startTime.getHour() * 60 + startTime.getMinute();
        Integer eTime = endTime.getHour() * 60 + endTime.getMinute();
        for (Train train : trains) {
            int dist = train.getRoute().indexOf(String.valueOf(station));
            if (dist != -1) {
                String[] route = train.getRoute().split(",");
                for (int i = 0; i < route.length; i++) {
                    if (route[i].equals(String.valueOf(station))) {
                        LocalTime departureTime = train.getDepartureTime();
                        Integer arrivalTime = departureTime.getHour() * 60 + departureTime.getMinute() + i * 60;

                        if (sTime <= arrivalTime && arrivalTime <= eTime)
                            trainsPassingAtGivenTime.add(train.getTrainId());
                    }
                }
            }
        }
        return trainsPassingAtGivenTime;
    }
}