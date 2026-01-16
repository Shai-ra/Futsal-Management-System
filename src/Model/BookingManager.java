package Model;

import java.util.LinkedList;
import java.util.Stack;

public class BookingManager {
    private LinkedList<Booking> bookings;
    private Stack<BookingStateMemento> undoStack;
    
    // Inner class to store booking state for undo
    private static class BookingStateMemento {
        private final String bookingId;
        private final String previousStatus;
        private final String previousDeclineReason;

        public BookingStateMemento(String bookingId, String previousStatus, String previousDeclineReason) {
            this.bookingId = bookingId;
            this.previousStatus = previousStatus;
            this.previousDeclineReason = previousDeclineReason;
        }

        public String getBookingId() {
            return bookingId;
        }

        public String getPreviousStatus() {
            return previousStatus;
        }

        public String getPreviousDeclineReason() {
            return previousDeclineReason;
        }
    }
    
    
    public BookingManager() {
        this.bookings = new LinkedList<>();
        this.undoStack = new Stack<>();
        prepareInitialBookingData();
        
    }

    private void prepareInitialBookingData() {
        bookings.add(new Booking("Shaira Gurung", "shaira.admin", "B001", "2026-01-10", "10:00 AM", "G1",
                "Manchester City", "Liverpool", "PAID", "PENDING"));
        bookings.add(new Booking("John Doe", "john.doe", "B002", "2026-01-12", "02:00 PM", "G2", "Real Madrid",
                "Barcelona", "PENDING", "PENDING"));
        bookings.add(new Booking("Jane Smith", "jane.smith", "B003", "2026-01-15", "04:30 PM", "G1",
                "FC Barcelona", "PSG", "PAID", "PENDING"));
        bookings.add(new Booking("Robert Brown", "robert.b", "B004", "2026-01-20", "09:00 AM", "G3",
                "Bayern Munich", "Arsenal", "PAID", "PENDING"));
        bookings.add(new Booking("Emily Davis", "emily.d", "B005", "2026-01-22", "11:00 AM", "G2", "Thunder",
                "Bolts", "PAID", "PENDING"));
        bookings.add(new Booking("Michael Wilson", "michael.w", "B006", "2026-01-25", "01:00 PM", "G1",
                "Lightning", "Storm", "PENDING", "PENDING"));
        bookings.add(new Booking("Sarah Miller", "sarah.m", "B007", "2026-01-28", "03:00 PM", "G3", "Volta",
                "Static", "PAID", "PENDING"));
    }

    public boolean addBooking(Booking booking) {
        for (Booking b : bookings) {
            if (b.getBookingId().equals(booking.getBookingId())) {
                return false;
            }
        }
        bookings.add(booking);
        return true;
    }

    public LinkedList<Booking> getBookings() {
        return bookings;
    }

    public boolean updateBookingStatus(String bookingId, String status, String reason) {
        for (Booking b : bookings) {
            if (b.getBookingId().equals(bookingId)) {
                // Save current state before changing
                BookingStateMemento memento = new BookingStateMemento(
                    bookingId, 
                    b.getStatus(), 
                    b.getDeclineReason()
                );
                undoStack.push(memento);
                
                // Update status 
                b.setStatus(status);
                b.setDeclineReason(reason);
                return true;
            }
        }
        return false;
    }
    
    public boolean undoLastStatusChange() {
        if (undoStack.isEmpty()) {
            return false;
        }
    BookingStateMemento memento = undoStack.pop();
    
    for (Booking b : bookings) {
            if (b.getBookingId().equals(memento.getBookingId())) {
                b.setStatus(memento.getPreviousStatus());
                b.setDeclineReason(memento.getPreviousDeclineReason());
                return true;
            }
        }
        
        return false;
    }

    public boolean canUndo() {
        return !undoStack.isEmpty();
    }

    public Booking getBookingById(String id) {
        for (Booking b : bookings) {
            if (b.getBookingId().equals(id)) {
                return b;
            }
        }
        return null;
    }

    public LinkedList<Booking> getBookingsForDate(String date) {
        LinkedList<Booking> list = new LinkedList<>();
        for (Booking b : bookings) {
            if (b.getDate().equals(date) && b.getStatus().equals("ACCEPTED")) {
                list.add(b);
            }
        }
        return list;
    }
}
