package cinema;

import cinema.business.CinemaRoom;
import cinema.business.Error;
import cinema.business.PurchasedTicket;
import cinema.business.Seat;
import cinema.constants.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
public class CinemaRoomController {

    private CinemaRoom cinemaRoom;

    @Autowired
    public CinemaRoomController(CinemaRoom cinemaRoom) {
        this.cinemaRoom = cinemaRoom;
    }

    @GetMapping(value = "/seats")
    public CinemaRoom getCinemaRoomInfo() {
        return cinemaRoom;
    }

    @PostMapping(value = "/purchase")
    public ResponseEntity bookSeat(@RequestBody Seat purchasedSeat) {

        // Check for validity of row
        if (purchasedSeat.getRow() < 1 ||
                purchasedSeat.getRow() > Constants.NO_OF_ROWS ||
                purchasedSeat.getColumn() < 1 ||
                purchasedSeat.getColumn() > Constants.NO_OF_SEATS_PER_ROW) {
            return new ResponseEntity(new Error("The number of a row or a column is out of bounds!"),
                    HttpStatus.BAD_REQUEST);
        }

        // Check if ticket has been purchased
        for (PurchasedTicket ticket : cinemaRoom.getTaken_seats()) {
            if (ticket.getTicket().getRow() == purchasedSeat.getRow() &&
            ticket.getTicket().getColumn() == purchasedSeat.getColumn()) {
                return new ResponseEntity(new Error("The ticket has been already purchased!"), HttpStatus.BAD_REQUEST);
            }
        }

        // Remove ticket from available ticket list and return purchased ticket
        int purchasedIndex = (purchasedSeat.getRow() - 1) * Constants.NO_OF_ROWS +
                (purchasedSeat.getColumn() - 1) % Constants.NO_OF_SEATS_PER_ROW;

        // To find the right index we need to subtract from the size of taken_seats
        purchasedIndex -= cinemaRoom.getTaken_seats().size();

        Seat bookedSeat = cinemaRoom.getAvailable_seats().remove(purchasedIndex);
        PurchasedTicket purchasedTicket = new PurchasedTicket(UUID.randomUUID().toString(), bookedSeat);
        cinemaRoom.getTaken_seats().add(purchasedTicket);
        return new ResponseEntity(purchasedTicket, HttpStatus.OK);
    }

    @PostMapping(value = "/return")
    public ResponseEntity refund(@RequestBody PurchasedTicket purchasedTicket) {
        Map<String, Seat> seatForRefund = new HashMap<>(1);
        int index = -1;

        // Find booked ticket with token
        for (PurchasedTicket ticket : cinemaRoom.getTaken_seats()) {
            ++index;
            if (ticket.getToken().equals(purchasedTicket.getToken())) {
                seatForRefund.put("returned_ticket", ticket.getTicket());
                break;
            }
        }

        if (!seatForRefund.isEmpty() && index != -1) {
            cinemaRoom.getTaken_seats().remove(index);
            cinemaRoom.getAvailable_seats().add(seatForRefund.get("returned_ticket"));
            return new ResponseEntity(seatForRefund, HttpStatus.OK);
        } else {
            return new ResponseEntity(new Error("Wrong token!"), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping(value = "/stats")
    public ResponseEntity getStatistics(@RequestParam Map<String, String> params) {
        if (params.containsKey("password") && Objects.equals(params.get("password"), Constants.PASSWORD)) {
            Map<String, Integer> stats = new LinkedHashMap<>();

            int total_income = 0;

            // Find total income
            for (PurchasedTicket ticket : cinemaRoom.getTaken_seats()) {
                total_income += ticket.getTicket().getPrice();
            }

            stats.put("current_income", total_income);
            stats.put("number_of_available_seats", cinemaRoom.getAvailable_seats().size());
            stats.put("number_of_purchased_tickets", cinemaRoom.getTaken_seats().size());

            return new ResponseEntity(stats, HttpStatus.OK);
        } else {
            return new ResponseEntity(new Error("The password is wrong!"), HttpStatus.UNAUTHORIZED);
        }
    }
}
