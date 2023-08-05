package com.driver.services;


import com.driver.EntryDto.BookTicketEntryDto;
import com.driver.model.Passenger;
import com.driver.model.Ticket;
import com.driver.model.Train;
import com.driver.repository.PassengerRepository;
import com.driver.repository.TicketRepository;
import com.driver.repository.TrainRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

        Train train = trainRepository.findById(bookTicketEntryDto.getTrainId()).get();
        int seatBooked = 0;
        for(Ticket currTicket : train.getBookedTickets()){
            seatBooked += currTicket.getPassengersList().size();
        }
        if(seatBooked + bookTicketEntryDto.getNoOfSeats() > train.getNoOfSeats()){
            throw new Exception("Less tickets are available");
        }


        List<String> trainRoute = Arrays.asList(train.getRoute().split(", "));
        if ( !trainRoute.contains(bookTicketEntryDto.getFromStation().toString()) || !trainRoute.contains(bookTicketEntryDto.getToStation().toString()) ) {
            throw new Exception("Invalid stations");
        }


        Ticket ticket = new Ticket();
        ticket.setTrain(train);
        ticket.setFromStation(bookTicketEntryDto.getFromStation());
        ticket.setToStation(bookTicketEntryDto.getToStation());

        List<Passenger> passengerList = new ArrayList<>();
        for(Integer currPassengerId : bookTicketEntryDto.getPassengerIds()){
            passengerList.add(passengerRepository.findById(currPassengerId).get());
        }
        ticket.setPassengersList(passengerList);

        int totalFare = 0;
        int noOfStation = Math.abs(trainRoute.indexOf(bookTicketEntryDto.getFromStation().toString()) - trainRoute.indexOf(bookTicketEntryDto.getToStation().toString()));
        totalFare = (300 * noOfStation) * passengerList.size();
        ticket.setTotalFare(totalFare);


        Ticket savedTicket = ticketRepository.save(ticket);


        for(Passenger currPassenger : savedTicket.getPassengersList()) {
            currPassenger.getBookedTickets().add(savedTicket);
            passengerRepository.save(currPassenger);
        }


        train.getBookedTickets().add(savedTicket);
        trainRepository.save(train);

       return savedTicket.getTicketId();
    }
}
