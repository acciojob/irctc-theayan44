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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class TrainService {

    @Autowired
    TrainRepository trainRepository;

    public Integer addTrain(AddTrainEntryDto trainEntryDto){

        //Add the train to the trainRepository
        //and route String logic to be taken from the Problem statement.
        //Save the train and return the trainId that is generated from the database.
        //Avoid using the lombok library

        String route = trainEntryDto.getStationRoute().toString();
        route = route.substring(1, route.length()-1);

        Train train = new Train();
        train.setRoute(route);
        train.setDepartureTime(trainEntryDto.getDepartureTime());
        train.setNoOfSeats(trainEntryDto.getNoOfSeats());
        train.setBookedTickets(new ArrayList<>());

        Train savedTrain = trainRepository.save(train);
        return savedTrain.getTrainId();
    }

    public Integer calculateAvailableSeats(SeatAvailabilityEntryDto seatAvailabilityEntryDto){

        //Calculate the total seats available
        //Suppose the route is A B C D
        //And there are 2 seats avaialble in total in the train
        //and 2 tickets are booked from A to C and B to D.
        //The seat is available only between A to C and A to B. If a seat is empty between 2 station it will be counted to our final ans
        //even if that seat is booked post the destStation or before the boardingStation
        //Inshort : a train has totalNo of seats and there are tickets from and to different locations
        //We need to find out the available seats between the given 2 stations.

        Train train = trainRepository.findById(seatAvailabilityEntryDto.getTrainId()).get();
        List<String> stationList = Arrays.asList(train.getRoute().split(", "));
        Integer seatBooked = 0;
        for(Ticket currTicket : train.getBookedTickets()){
            String ticketFromStation = currTicket.getFromStation().toString();
            String ticketToStation = currTicket.getToStation().toString();
            String fromStation = seatAvailabilityEntryDto.getFromStation().toString();
            String toStation = seatAvailabilityEntryDto.getToStation().toString();
            if(stationList.indexOf(ticketFromStation) <= stationList.indexOf(fromStation) && stationList.indexOf(ticketToStation) >= stationList.indexOf(toStation)){
                seatBooked += currTicket.getPassengersList().size();
            }
        }

        return (seatBooked < train.getNoOfSeats()) ? (train.getNoOfSeats() - seatBooked) : 0;
    }

    public Integer calculatePeopleBoardingAtAStation(Integer trainId,Station station) throws Exception{

        //We need to find out the number of people who will be boarding a train from a particular station
        //if the trainId is not passing through that station
        //throw new Exception("Train is not passing from this station");
        //  in a happy case we need to find out the number of such people.

        Train train = trainRepository.findById(trainId).get();
        List<String> stationList = Arrays.asList(train.getRoute().split(", "));
        if(!stationList.contains(station.toString())){
            throw new Exception("Train is not passing from this station");
        }

        Integer count = 0;
        for(Ticket currTicket : train.getBookedTickets()){
            if(currTicket.getFromStation() == station)
                count += currTicket.getPassengersList().size();
        }

        return count;
    }

    public Integer calculateOldestPersonTravelling(Integer trainId){

        //Throughout the journey of the train between any 2 stations
        //We need to find out the age of the oldest person that is travelling the train
        //If there are no people travelling in that train you can return 0

        Train train = trainRepository.findById(trainId).get();
        Integer maxAge = 0;
        for(Ticket currTicket : train.getBookedTickets()){
            for(Passenger currPassenger : currTicket.getPassengersList()){
                maxAge = Math.max(maxAge, currPassenger.getAge());
            }
        }

        return maxAge;
    }

    public List<Integer> trainsBetweenAGivenTime(Station station, LocalTime startTime, LocalTime endTime){

        //When you are at a particular station you need to find out the number of trains that will pass through a given station
        //between a particular time frame both start time and end time included.
        //You can assume that the date change doesn't need to be done ie the travel will certainly happen with the same date (More details
        //in problem statement)
        //You can also assume the seconds and milli seconds value will be 0 in a LocalTime format.

        List<Train> trainList = trainRepository.findAll();
        List<Integer> ansList = new ArrayList<>();
        for(Train currTrain : trainList){
            List<String> stationList = Arrays.asList(currTrain.getRoute().split(", "));
            if(stationList.contains(station.toString())){
                LocalTime departureTime = currTrain.getDepartureTime();
                int stationIndex = stationList.indexOf(station.toString());
                LocalTime trainTimeOnTheStation = departureTime.plusHours((long) stationIndex);
                if(trainTimeOnTheStation.compareTo(startTime) >= 0 && trainTimeOnTheStation.compareTo(endTime) <= 0){
                    ansList.add(currTrain.getTrainId());
                }
            }
        }

        return ansList;
    }

}
