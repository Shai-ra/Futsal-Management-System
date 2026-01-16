/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Controller;

/**
 *
 * @author WELCOME
 */
import View.Admin;
import Model.*;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

public class AdminController {
    private Admin view;
    private UserController userController;
    private TeamController teamController;
    private PlayerController playerController;
    private BookingController bookingController;

    public AdminController(Admin view) {
        this.view = view;
        this.userController = new UserController();
        this.teamController = new TeamController();
        this.playerController = new PlayerController();
        this.bookingController = new BookingController();
    }

    public void initController() {
        // Team Management Listeners
        view.getTMAddBtn().addActionListener(e -> addTeam());
        view.getTMUpdateBtn().addActionListener(e -> updateTeam());
        view.getTMDeleteBtn().addActionListener(e -> deleteTeam());
        view.getTMReadBtn().addActionListener(e -> loadTeamData());

        // Player Management Listeners
        view.getPMAddBtn().addActionListener(e -> addPlayer());
        view.getPMUpdateBtn().addActionListener(e -> updatePlayer());
        view.getPMDeleteBtn().addActionListener(e -> deletePlayer());
        view.getPMReadBtn().addActionListener(e -> loadPlayerData());
        view.getUploadPhotoBtn().addActionListener(e -> handlePhotoUpload());

        // Search and Sort Listeners
        // Real-time Search Listeners
        view.getTMSearchTextField().addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent e) {
                searchTeams();
            }
        });
        view.getPMSearchTextField().addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent e) {
                searchPlayers();
            }
        });

        view.getTMSortByComboBox().addActionListener(e -> sortTeams());
        view.getPMSortByComboBox().addActionListener(e -> sortPlayers());

        // Booking Table Listener
        view.getBookingTable().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                handleBookingTableClick(e);
            }
        });
        
        // Undo Button Listener
        view.getUndoButton().addActionListener(e -> handleUndo());

        // Table Selection Listeners
        view.getTeamTable().getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = view.getTeamTable().getSelectedRow();
                if (selectedRow >= 0) {
                    // Populate fields
                    String teamId = view.getTeamTable().getValueAt(selectedRow, 0).toString();
                    teamController.loadTeamFromTable(teamId, view.getTeamIDTextField(),
                            view.getTMTeamNameTextField(), view.getManagerTextField(), view.getNoPlayerTextField());
                }
            }
        });

        view.getPlayerTable().getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = view.getPlayerTable().getSelectedRow();
                if (selectedRow >= 0) {
                    String playerId = view.getPlayerTable().getValueAt(selectedRow, 0).toString();
                    // Note: loadPlayerFromTable needs all these fields
                    playerController.loadPlayerFromTable(playerId, view.getPlayerIDTextField(),
                            view.getPlayerNameTextField(), view.getPlayerAgeTextField(),
                            view.getPlayerJerseyNoTextField(), view.getPlayerPositionTextField(),
                            view.getPMTeamNameTextField(), view.getPhotoLabel());
                    // view.currentPhotoFileName = ... (view doesn't expose this easily, maybe skip
                    // or handle in controller)
                    // Controller handles photo via UploadPhoto button client property usually.
                }
            }
        });

        // Load initial data
        loadTeamData();
        loadPlayerData();
        loadBookingData();

        // Login/Signup Listeners
        view.getLoginBtn().addActionListener(e -> handleLogin());
        view.getSignUpBtn().addActionListener(e -> handleRegister());
        
        updateUndoButtonState();
    }

    // Team Management Logic
    private void addTeam() {
        boolean success = teamController.addTeam(
                view.getTeamIDTextField().getText(),
                view.getTMTeamNameTextField().getText(),
                view.getManagerTextField().getText(),
                view.getNoPlayerTextField().getText());
        if (success) {
            view.addRecentActivity("New Team Added: " + view.getTMTeamNameTextField().getText());
            loadTeamData();
            view.clearTeamFields();
            view.updateDashboardStats();
        }
    }

    private void updateTeam() {
        boolean success = teamController.updateTeam(
                view.getTeamIDTextField().getText(),
                view.getTMTeamNameTextField().getText(),
                view.getManagerTextField().getText(),
                view.getNoPlayerTextField().getText());
        if (success) {
            view.addRecentActivity("Team Updated: " + view.getTMTeamNameTextField().getText());
            loadTeamData();
            view.clearTeamFields();
            view.updateDashboardStats();
        }
    }

    private void deleteTeam() {
        boolean success = teamController.deleteTeam(view.getTeamIDTextField().getText());
        if (success) {
            view.addRecentActivity("Team Deleted: ID " + view.getTeamIDTextField().getText());
            loadTeamData();
            view.clearTeamFields();
            view.updateDashboardStats();
        }
    }

    public void loadTeamData() {
        teamController.loadTeamsToTable((DefaultTableModel) view.getTeamTable().getModel());
        view.addRecentActivity("Team Data Loaded");
    }

    private void searchTeams() {
        String query = view.getTMSearchTextField().getText();
        // Defaulting to "Team Name" for partial matches as requested
        teamController.searchTeams("Team Name", query, (DefaultTableModel) view.getTeamTable().getModel());
    }

    private void sortTeams() {
        String criteria = (String) view.getTMSortByComboBox().getSelectedItem();
        teamController.sortTeams(criteria, (DefaultTableModel) view.getTeamTable().getModel());
    }

    // Player Management Logic
    private void addPlayer() {
        String photoPath = (String) view.getUploadPhotoBtn().getClientProperty("selectedPhoto");
        boolean success = playerController.addPlayer(
                view.getPlayerIDTextField().getText(),
                view.getPlayerNameTextField().getText(),
                view.getPlayerAgeTextField().getText(),
                view.getPlayerJerseyNoTextField().getText(),
                view.getPlayerPositionTextField().getText(),
                view.getPMTeamNameTextField().getText(),
                photoPath);
        if (success) {
            view.addRecentActivity("New Player Added: " + view.getPlayerNameTextField().getText());
            loadPlayerData();
            view.clearPlayerFields();
            view.updateDashboardStats();
        }
    }

    private void updatePlayer() {
        String newPhotoPath = (String) view.getUploadPhotoBtn().getClientProperty("selectedPhoto");
        String photoPath = newPhotoPath;

        if (photoPath == null || photoPath.isEmpty()) {
            int selectedRow = view.getPlayerTable().getSelectedRow();
            if (selectedRow >= 0) {
                Object photoValue = view.getPlayerTable().getValueAt(selectedRow, 6);
                if (photoValue != null) {
                    photoPath = photoValue.toString();
                }
            }
        }

        boolean success = playerController.updatePlayer(
                view.getPlayerIDTextField().getText(),
                view.getPlayerNameTextField().getText(),
                view.getPlayerAgeTextField().getText(),
                view.getPlayerJerseyNoTextField().getText(),
                view.getPlayerPositionTextField().getText(),
                view.getPMTeamNameTextField().getText(),
                photoPath);
        if (success) {
            view.addRecentActivity("Player Updated: " + view.getPlayerNameTextField().getText());
            loadPlayerData();
            view.clearPlayerFields();
            view.updateDashboardStats();
        }
    }

    private void deletePlayer() {
        boolean success = playerController.deletePlayer(view.getPlayerIDTextField().getText());
        if (success) {
            view.addRecentActivity("Player Deleted: ID " + view.getPlayerIDTextField().getText());
            loadPlayerData();
            view.clearPlayerFields();
            view.updateDashboardStats();
        }
    }

    public void loadPlayerData() {
        playerController.loadPlayersToTable((DefaultTableModel) view.getPlayerTable().getModel());
        view.addRecentActivity("Player Data Loaded");
    }

    private void searchPlayers() {
        String query = view.getPMSearchTextField().getText();
        playerController.searchPlayers("Player Name", query, (DefaultTableModel) view.getPlayerTable().getModel());
    }

    private void sortPlayers() {
        String criteria = (String) view.getPMSortByComboBox().getSelectedItem();
        playerController.sortPlayers(criteria, (DefaultTableModel) view.getPlayerTable().getModel());
    }

    // Photo Upload
    private void handlePhotoUpload() {
        String photoPath = playerController.uploadPhoto();
        if (photoPath != null && !photoPath.isEmpty()) {
            view.getUploadPhotoBtn().putClientProperty("selectedPhoto", photoPath);
            playerController.displayPhoto(photoPath, view.getPhotoLabel());
            JOptionPane.showMessageDialog(view, "Photo selected successfully!");
        }
    }

    // Login/Register Logic
    public void handleLogin() {
        String username = view.getLoginUsernameField().getText();
        String password = new String(view.getLoginPasswordField().getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(view, "Please enter username and password.", "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        User user = userController.loginUser(username, password);

        if (user != null) {
            JOptionPane.showMessageDialog(view, "Login Successful! Welcome " + user.getFirstName());
            view.setWelcomeMessage(user.getFirstName());
            view.setIsAdmin(true);

            // Switch to Dashboard
            java.awt.CardLayout cardLayout = (java.awt.CardLayout) view.getMainPanel().getLayout();
            cardLayout.show(view.getMainPanel(), "card3");

            // Update Dashboard Stats immediately
            view.updateDashboardStats();
            view.updateUpcomingMatch();
            view.highlightBookedDates();
        } else {
            JOptionPane.showMessageDialog(view, "Invalid Username or Password.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void handleRegister() {
        String username = view.getSignUpUsernameField().getText();
        String password = new String(view.getSignUpPasswordField().getPassword());
        String retypePassword = new String(view.getSignUpRetypePwField().getPassword());
        String firstName = view.getSignUpFirstNameField().getText();
        String lastName = view.getSignUpLastNameField().getText();

        String dob = view.getDateOfBirth();

        if (password.equals(retypePassword)) {
            boolean success = userController.registerUser(username, password, retypePassword, firstName, lastName, dob);
            if (success) {
                JOptionPane.showMessageDialog(view, "Registration Successful! Please Login.");
                // Switch to Login card
                java.awt.CardLayout cardLayout = (java.awt.CardLayout) view.getMainPanel().getLayout();
                cardLayout.show(view.getMainPanel(), "card1");
            } else {
                JOptionPane.showMessageDialog(view, "Registration Failed. Username may exist.", "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(view, "Passwords do not match!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Booking Logic
    public void loadBookingData() {
        DefaultTableModel model = (DefaultTableModel) view.getBookingTable().getModel();
        model.setRowCount(0);
        for (Booking b : bookingController.getBookings()) {
            if (b.getStatus().equals("PENDING")) {
                model.addRow(new Object[] {
                        b.getName(), b.getUsername(), b.getBookingId(), b.getDate(),
                        b.getTime(), b.getGroundNo(), b.getTeam1(), b.getTeam2(),
                        b.getPaymentStatus(), ""
                });
            }
        }
    }

    private void handleBookingTableClick(MouseEvent e) {
        JTable table = view.getBookingTable();
        int column = table.getColumnModel().getColumnIndexAtX(e.getX());
        int row = e.getY() / table.getRowHeight();

        if (row < table.getRowCount() && row >= 0 && column == 9) {
            java.awt.Rectangle rect = table.getCellRect(row, column, true);
            int xRel = e.getX() - rect.x;
            String bookingId = table.getValueAt(row, 2).toString();

            if (xRel < rect.width / 2) {
                handleAcceptBooking(bookingId);
            } else {
                handleDeclineBooking(bookingId);
            }
        }
    }

    private void handleAcceptBooking(String bookingId) {
        if (bookingController.updateBookingStatus(bookingId, "ACCEPTED", "")) {
            view.addRecentActivity("Booking Accepted: ID " + bookingId);
            JOptionPane.showMessageDialog(view, "Booking Accepted!");
            loadBookingData();
            view.updateDashboardStats();
            view.updateUpcomingMatch();
            view.highlightBookedDates();
            
            // Enable undo button
            updateUndoButtonState(); 
        }else {
            JOptionPane.showMessageDialog(view, 
                "Failed to accept booking!", 
                "Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleDeclineBooking(String bookingId) {
        String reason = JOptionPane.showInputDialog(view, "Enter reason for declining:");
        if (reason != null && !reason.trim().isEmpty()) {
            if (bookingController.updateBookingStatus(bookingId, "DECLINED", reason)) {
                view.addRecentActivity("Booking Declined: ID " + bookingId + " (" + reason + ")");
                JOptionPane.showMessageDialog(view, "Booking Declined.");
                loadBookingData();
                view.updateDashboardStats();
                view.updateUpcomingMatch();
                view.highlightBookedDates();
                // Enable undo button
                updateUndoButtonState();
            }else {
                JOptionPane.showMessageDialog(view, 
                    "Failed to decline booking!", 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void handleUndo() {
        if (bookingController.undoLastStatusChange()) {
            JOptionPane.showMessageDialog(view, 
                "Successfully undone the last booking status change!", 
                "Undo Successful", 
                JOptionPane.INFORMATION_MESSAGE);
            
            // Refresh the booking table
            loadBookingData();
            
            // Update undo button state
            updateUndoButtonState();
            
            // Update dashboard stats
            view.updateDashboardStats();
            view.updateUpcomingMatch();
            view.highlightBookedDates();
            
            // Log activity
            view.addRecentActivity("Undid last booking status change");
        } else {
            JOptionPane.showMessageDialog(view, 
                "Nothing to undo!", 
                "Undo Failed", 
                JOptionPane.WARNING_MESSAGE);
        }
    }

    
    private void updateUndoButtonState() {
    view.getUndoButton().setEnabled(bookingController.canUndo());
    }
    
    // Getters for controllers if needed
    public TeamController getTeamController() {
        return teamController;
    }

    public PlayerController getPlayerController() {
        return playerController;
    }

    public BookingController getBookingController() {
        return bookingController;
    }
}