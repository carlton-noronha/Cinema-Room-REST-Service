package cinema.business;

import cinema.constants.Constants;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Data
@Component
public class CinemaRoom {
    private int total_rows;
    private int total_columns;
    private List<Seat> available_seats;

    @JsonIgnore
    private List<PurchasedTicket> taken_seats;

    public CinemaRoom() {
        this.total_rows = Constants.NO_OF_ROWS;
        this.total_columns = Constants.NO_OF_SEATS_PER_ROW;
        this.available_seats = new ArrayList<>();
        this.taken_seats = new ArrayList<>();
        initializeSeatingStatus();
    }

    private void initializeSeatingStatus() {
        for(int row = 1; row <= total_rows; ++row) {
            for (int column = 1; column <= total_columns; ++column) {
                if (row <= 4) {
                    available_seats.add(new Seat(row, column, 10));
                } else {
                    available_seats.add(new Seat(row, column, 8));
                }
            }
        }
    }

}
