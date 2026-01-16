package Controller;

import Model.Booking;
import Model.BookingManager;
import java.util.LinkedList;

public class BookingController {
    private BookingManager bookingManager;

    public BookingController() {
        this.bookingManager = new BookingManager();
    }

    public LinkedList<Booking> getBookings() {
        return bookingManager.getBookings();
    }

    public boolean addBooking(String name, String username, String id, String date, String time,
            String groundNo, String team1, String team2, String paymentStatus, String status) {
        return bookingManager.addBooking(
                new Booking(name, username, id, date, time, groundNo, team1, team2, paymentStatus, status));
    }

    public boolean updateBookingStatus(String id, String status, String reason) {
        return bookingManager.updateBookingStatus(id, status, reason);
    }

    public boolean undoLastStatusChange() {
        return bookingManager.undoLastStatusChange();
    }

    public boolean canUndo() {
        return bookingManager.canUndo();
    }
    
    public LinkedList<Booking> getBookingsForDate(String date) {
        return bookingManager.getBookingsForDate(date);
    }

    public Booking getBookingById(String id) {
        return bookingManager.getBookingById(id);
    }
}
