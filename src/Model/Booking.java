package Model;

public class Booking {
    private String name;
    private String username;
    private String bookingId;
    private String teamName;
    private String date;
    private String time;
    private String groundNo;
    private String team1;
    private String team2;
    private String paymentStatus;
    private String status; // PENDING, ACCEPTED, DECLINED
    private String declineReason;

    public Booking(String name, String username, String bookingId, String date, String time, String groundNo,
            String team1, String team2, String paymentStatus, String status) {
        this.name = name;
        this.username = username;
        this.bookingId = bookingId;
        this.date = date;
        this.time = time;
        this.groundNo = groundNo;
        this.team1 = team1;
        this.team2 = team2;
        this.paymentStatus = paymentStatus;
        this.status = status;
        this.declineReason = "";
        this.teamName = team1; // Keeping for compatibility
    }

    // Getters and Setters

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getBookingId() {
        return bookingId;
    }

    public void setBookingId(String bookingId) {
        this.bookingId = bookingId;
    }

    public String getTeamName() {
        return teamName;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getGroundNo() {
        return groundNo;
    }

    public void setGroundNo(String groundNo) {
        this.groundNo = groundNo;
    }

    public String getTeam1() {
        return team1;
    }

    public void setTeam1(String team1) {
        this.team1 = team1;
    }

    public String getTeam2() {
        return team2;
    }

    public void setTeam2(String team2) {
        this.team2 = team2;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDeclineReason() {
        return declineReason;
    }

    public void setDeclineReason(String declineReason) {
        this.declineReason = declineReason;
    }
}
