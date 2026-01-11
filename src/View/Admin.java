/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package View;

/**
 *
 * @author WELCOME
 */
import java.awt.CardLayout;
import javax.swing.JOptionPane;
import java.util.LinkedList;
import java.text.SimpleDateFormat;
import javax.swing.*;
import Controller.*;
import Model.*;
import java.io.File;
import javax.imageio.ImageIO;
import datechooser.model.multiple.PeriodSet;
import datechooser.model.multiple.Period;
import java.util.Calendar;

public class Admin extends javax.swing.JFrame {

    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(Admin.class.getName());
    private UserController userController;
    private TeamController teamController;
    private PlayerController playerController;
    private BookingController bookingController;
    private boolean isAdmin = false;
    private javax.swing.JLabel photoLabel;
    private String lastModifiedBookingId = null;

    // Creates new form AdminLogin
    public Admin() {
        initComponents();
        userController = new UserController();
        teamController = new TeamController();
        playerController = new PlayerController();
        bookingController = new BookingController();

        // Setup photo handling
        setupPhotoHandling();

        // Load initial data to tables
        loadTeamData();
        loadPlayerData();

        // Setup table selection listeners
        setupTableListeners();

        // Setup photo column renderer and click listener
        setupPhotoColumnRenderer();
        setupPhotoClickListener();

        // Add action listeners to buttons
        addActionListenersToTeamButtons();
        addActionListenersToPlayerButtons();

        // Load initial data for bookings
        setupBookingTableActions();
        loadBookingData();
        highlightBookedDates();

        // Dashboard fixes
        RecentActivitesTextArea.setEditable(false);
        updateDashboardStats();
        updateUpcomingMatch();
        setupSearchSortUndoListeners();
        setupAnnouncementListeners();
    }

    // In Admin.java (add these methods)

    public JTextField getLoginUsernameField() {
        return LoginUsername;
    }

    public JPasswordField getLoginPasswordField() {
        return LoginPassword;
    }

    public JToggleButton getLoginBtn() {
        return LoginBtn;
    }

    public JTextField getSignUpUsernameField() {
        return SignUpUsername;
    }

    public JPasswordField getSignUpPasswordField() {
        return SignUpPassword;
    }

    public JPasswordField getSignUpRetypePwField() {
        return SignUpRetypePw;
    }

    public JTextField getSignUpFirstNameField() {
        return SignUpFirstName;
    }

    public JTextField getSignUpLastNameField() {
        return SignUpLastName;
    }

    public JToggleButton getSignUpBtn() {
        return SignUpBtn;
    }

    public JLabel getLoginSignUpLabel() {
        return LoginSignUp;
    }

    public JPanel getMainPanel() {
        return jPanel1;
    }

    public void setWelcomeMessage(String firstName) {
        jLabel6.setText("Welcome Back " + firstName + "!");
    }

    private void handleNavigation(String cardName) {
        // Restricted cards for admins only
        java.util.List<String> adminRestrictedCards = java.util.Arrays.asList("card3", "card4", "card5", "card6",
                "card7", "card8");

        if (adminRestrictedCards.contains(cardName) && !isAdmin) {
            JOptionPane.showMessageDialog(this, "Access Denied: Not an Admin", "Restriction",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        CardLayout cardLayout = (CardLayout) jPanel1.getLayout();
        cardLayout.show(jPanel1, cardName);
    }

    private void setupBookingTableActions() {
        // Add a column for actions if not already present
        javax.swing.table.DefaultTableModel model = (javax.swing.table.DefaultTableModel) BookingTable.getModel();
        model.setColumnIdentifiers(new String[] {
                "Name", "Username", "Booking ID", "Date", "Time", "Ground no", "Team 1", "Team 2",
                "Payment Status", "Action"
        });

        // Action column is at index 9
        BookingTable.getColumnModel().getColumn(9).setCellRenderer(new javax.swing.table.TableCellRenderer() {
            @Override
            public java.awt.Component getTableCellRendererComponent(javax.swing.JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                javax.swing.JPanel panel = new javax.swing.JPanel();
                panel.setLayout(new javax.swing.BoxLayout(panel, javax.swing.BoxLayout.Y_AXIS));

                javax.swing.JButton acceptBtn = new javax.swing.JButton("Accept");
                javax.swing.JButton declineBtn = new javax.swing.JButton("Decline");

                acceptBtn.setAlignmentX(java.awt.Component.CENTER_ALIGNMENT);
                declineBtn.setAlignmentX(java.awt.Component.CENTER_ALIGNMENT);

                panel.add(javax.swing.Box.createVerticalGlue());
                panel.add(acceptBtn);
                panel.add(javax.swing.Box.createVerticalStrut(5));
                panel.add(declineBtn);
                panel.add(javax.swing.Box.createVerticalGlue());

                return panel;
            }
        });

        BookingTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                int column = BookingTable.getColumnModel().getColumnIndexAtX(e.getX());
                int row = e.getY() / BookingTable.getRowHeight();

                if (row < BookingTable.getRowCount() && row >= 0 && column == 9) {
                    java.awt.Rectangle rect = BookingTable.getCellRect(row, column, true);
                    int xRel = e.getX() - rect.x;

                    String bookingId = BookingTable.getValueAt(row, 2).toString(); // Booking ID is at index 2

                    if (xRel < rect.width / 2) {
                        // Accept clicked
                        handleAcceptBooking(bookingId);
                    } else {
                        // Decline clicked
                        handleDeclineBooking(bookingId);
                    }
                }
            }
        });

        // Increase row height to accommodate stacked buttons
        BookingTable.setRowHeight(60);
    }

    private void handleAcceptBooking(String bookingId) {
        Booking b = bookingController.getBookingById(bookingId);
        if (bookingController.updateBookingStatus(bookingId, "ACCEPTED", "")) {
            JOptionPane.showMessageDialog(this, "Booking Accepted!");
            if (b != null) {
                RecentActivitesTextArea.append("Team " + b.getTeamName() + " accepted\n");
            }
            lastModifiedBookingId = bookingId;
            loadBookingData();
            highlightBookedDates();
            updateDashboardStats();
            updateUpcomingMatch();
        }
    }

    private void handleDeclineBooking(String bookingId) {
        Booking b = bookingController.getBookingById(bookingId);
        String reason = JOptionPane.showInputDialog(this, "Enter reason for declining:");
        if (reason != null && !reason.trim().isEmpty()) {
            if (bookingController.updateBookingStatus(bookingId, "DECLINED", reason)) {
                JOptionPane.showMessageDialog(this, "Booking Declined.");
                if (b != null) {
                    RecentActivitesTextArea.append("Team " + b.getTeamName() + " declined\n");
                }
                lastModifiedBookingId = bookingId;
                loadBookingData();
                highlightBookedDates();
                updateDashboardStats();
                updateUpcomingMatch();
            }
        }
    }

    private void setupSearchSortUndoListeners() {
        // Team Search (jLabel32 is the actual search icon for Teams)
        jLabel32.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                handleTeamSearch();
            }

            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                jLabel32.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
            }
        });

        // Player Search
        PMSearchBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                handlePlayerSearch();
            }

            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                PMSearchBtn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
            }
        });

        // Team Sort
        TMSortByComboBox.addActionListener(e -> handleTeamSort());

        // Player Sort
        PMSortByComboBox.addActionListener(e -> handlePlayerSort());

        // Undo
        jButton1.addActionListener(e -> handleUndo());
    }

    private void handleTeamSearch() {
        String criteria = (String) TMSearchComboBox.getSelectedItem();
        String query = TMSearchTextField.getText();
        teamController.searchTeams(criteria, query, (javax.swing.table.DefaultTableModel) TeamTable.getModel());
    }

    private void handlePlayerSearch() {
        String criteria = (String) PMSearchComboBox.getSelectedItem();
        String query = PMSearchTextField.getText();
        playerController.searchPlayers(criteria, query, (javax.swing.table.DefaultTableModel) PlayerTable.getModel());
    }

    private void handleTeamSort() {
        String criteria = (String) TMSortByComboBox.getSelectedItem();
        teamController.sortTeams(criteria, (javax.swing.table.DefaultTableModel) TeamTable.getModel());
    }

    private void handlePlayerSort() {
        String criteria = (String) PMSortByComboBox.getSelectedItem();
        playerController.sortPlayers(criteria, (javax.swing.table.DefaultTableModel) PlayerTable.getModel());
    }

    private void handleUndo() {
        if (lastModifiedBookingId == null) {
            JOptionPane.showMessageDialog(this, "No action to undo!");
            return;
        }

        if (bookingController.updateBookingStatus(lastModifiedBookingId, "PENDING", "")) {
            JOptionPane.showMessageDialog(this, "Action undone! Booking is now PENDING.");
            RecentActivitesTextArea.append("Undo last action for " + lastModifiedBookingId + "\n");
            lastModifiedBookingId = null;
            loadBookingData();
            highlightBookedDates();
            updateDashboardStats();
            updateUpcomingMatch();
        }
    }

    private void setupAnnouncementListeners() {
        // Announcement 1 (jPanel26 / cross1)
        cross1.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                jPanel26.setVisible(false);
            }

            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                cross1.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
            }
        });

        // Announcement 2 (jPanel28 / cross2)
        cross2.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                jPanel28.setVisible(false);
            }

            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                cross2.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
            }
        });

        // Announcement 3 (jPanel3 / cross3)
        cross3.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                jPanel3.setVisible(false);
            }

            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                cross3.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
            }
        });
    }

    private void highlightBookedDates() {
        try {
            LinkedList<Booking> allBookings = bookingController.getBookings();
            PeriodSet periodSet = new PeriodSet();

            for (Booking booking : allBookings) {
                if ("ACCEPTED".equals(booking.getStatus())) {
                    String dateStr = booking.getDate();
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                    java.util.Date date = sdf.parse(dateStr);
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(date);
                    periodSet.add(new Period(cal));
                }
            }

            dateChooserPanel1.setSelection(periodSet);
        } catch (Exception e) {
            System.err.println("Error highlighting booked dates: " + e.getMessage());
        }
    }

    private void loadBookingData() {
        javax.swing.table.DefaultTableModel model = (javax.swing.table.DefaultTableModel) BookingTable.getModel();
        model.setRowCount(0);

        for (Booking b : bookingController.getBookings()) {
            if (b.getStatus().equals("PENDING")) {
                model.addRow(new Object[] {
                        b.getName(),
                        b.getUsername(),
                        b.getBookingId(),
                        b.getDate(),
                        b.getTime(),
                        b.getGroundNo(),
                        b.getTeam1(),
                        b.getTeam2(),
                        b.getPaymentStatus(),
                        "" // Action column placeholder
                });
            }
        }
    }

    private int countPendingBookings() {
        int count = 0;
        for (Booking b : bookingController.getBookings()) {
            if ("PENDING".equals(b.getStatus())) {
                count++;
            }
        }
        return count;
    }

    private int countAcceptedBookings() {
        int count = 0;
        for (Booking b : bookingController.getBookings()) {
            if ("ACCEPTED".equals(b.getStatus())) {
                count++;
            }
        }
        return count;
    }

    private void updateDashboardStats() {
        if (jLabel47 != null)
            jLabel47.setText(String.valueOf(teamController.getTeamCount())); // Total Teams
        if (jLabel49 != null)
            jLabel49.setText(String.valueOf(playerController.getPlayerCount())); // Total Players
        if (jLabel48 != null)
            jLabel48.setText(String.valueOf(countPendingBookings())); // Total Bookings (Pending)
        if (jLabel50 != null)
            jLabel50.setText(String.valueOf(countAcceptedBookings())); // Total Matches
    }

    private void updateUpcomingMatch() {
        LinkedList<Booking> accepted = new LinkedList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String todayStr = sdf.format(new java.util.Date());

        for (Booking b : bookingController.getBookings()) {
            if ("ACCEPTED".equals(b.getStatus())) {
                if (b.getDate().compareTo(todayStr) >= 0) {
                    accepted.add(b);
                }
            }
        }

        // Sort by date then time
        accepted.sort((b1, b2) -> {
            int dateComp = b1.getDate().compareTo(b2.getDate());
            if (dateComp != 0)
                return dateComp;
            return b1.getTime().compareTo(b2.getTime());
        });

        if (!accepted.isEmpty() && jLabel42 != null) {
            Booking next = accepted.getFirst();
            jLabel42.setText(next.getTeam1());
            jLabel44.setText(next.getTeam2());
            jLabel46.setText(next.getDate());
            jLabel45.setText(next.getTime());
        } else if (jLabel42 != null) {
            jLabel42.setText("None");
            jLabel44.setText("None");
            jLabel46.setText("N/A");
            jLabel45.setText("N/A");
        }
    }

    private String getDateFromChooser() {
        try {
            java.util.Calendar selectedCalendar = DateOfBirth.getSelectedDate();
            if (selectedCalendar != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                // Calendar has a getTime() method that returns Date
                return sdf.format(selectedCalendar.getTime());
            }
        } catch (Exception e) {
            // e.printStackTrace();
        }
        return "";
    }

    private void setupUploadPhotoButton() {
        UploadPhoto.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                uploadPhotoButtonClicked();
            }
        });
    }

    // In Admin.java - add this method
    private void setupPhotoHandling() {
        // Create a photo label if not exists
        photoLabel = new javax.swing.JLabel();
        photoLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        photoLabel.setVerticalAlignment(javax.swing.SwingConstants.CENTER);
        photoLabel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        photoLabel.setPreferredSize(new java.awt.Dimension(150, 150));

        // Add photo label to the panel (adjust coordinates based on your layout)
        jPanel9.add(photoLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(350, 470, 150, 150));

        // Modify UploadPhoto button action
        UploadPhoto.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                handlePhotoUpload();
            }
        });
    }

    private void handlePhotoUpload() {
        String photoPath = playerController.uploadPhoto();

        if (photoPath != null && !photoPath.isEmpty()) {
            // Store the photo path temporarily (not in LinkedList yet)
            UploadPhoto.putClientProperty("selectedPhoto", photoPath);

            // Display the photo preview
            playerController.displayPhoto(photoPath, photoLabel);

            JOptionPane.showMessageDialog(this,
                    "Photo selected successfully!\n" +
                            "Click 'Add' or 'Update' to save with player.",
                    "Success", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void addActionListenerToUploadButton() {
        UploadPhoto.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                uploadPhotoActionPerformed(evt);
            }
        });
    }

    private void uploadPhotoButtonClicked() {
        String uploadedFileName = playerController.uploadPhoto();

        if (uploadedFileName != null && !uploadedFileName.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Photo uploaded successfully!",
                    "Success", JOptionPane.INFORMATION_MESSAGE);

            // // Display the photo preview
            // playerController.displayPhoto(uploadedFileName, photoPreviewLabel);

            // Store the filename
            UploadPhoto.putClientProperty("lastUploadedPhoto", uploadedFileName);

            // Update label to show filename
            jLabel34.setText("Photo: " + uploadedFileName);
        }
    }

    private void uploadPhotoActionPerformed(java.awt.event.ActionEvent evt) {
        // Call the controller's uploadPhoto method
        String photoFileName = playerController.uploadPhoto();

        if (photoFileName != null) {
            // Display the photo in the label
            playerController.displayPhoto(photoFileName, photoLabel);

            // Show success message
            JOptionPane.showMessageDialog(this,
                    "Photo uploaded successfully!\n" +
                            "File: " + photoFileName,
                    "Success", JOptionPane.INFORMATION_MESSAGE);

            currentPhotoFileName = photoFileName;
        }
    }

    // Add this class variable to store the current photo filename
    private String currentPhotoFileName = "";

    public void clearDateChooser() {
        DateOfBirth.setSelectedDate(null);
    }

    private void loadUsersFromFile() {

    }
    // private void saveUsersToFile() {
    // }

    private void loadTeamData() {
        teamController.loadTeamsToTable((javax.swing.table.DefaultTableModel) TeamTable.getModel());
    }

    private void loadPlayerData() {
        playerController.loadPlayersToTable((javax.swing.table.DefaultTableModel) PlayerTable.getModel());
    }

    private void clearTeamFields() {
        TeamIDTextField.setText("");
        TMTeamNameTextField.setText("");
        ManagerTextField.setText("");
        NoPlayerTextField.setText("");
    }

    private void clearPlayerFields() {
        PlayerIDTextField.setText("");
        PlayerNameTextField.setText("");
        PlayerAgeTextField.setText("");
        PlayerJerseyNoTextField.setText("");
        PlayerPositionTextField.setText("");
        PMTeamNameTextField.setText("");

        // Clear photo display
        if (photoLabel != null) {
            photoLabel.setIcon(null);
            photoLabel.setText("No Photo");
        }

        // Clear stored photo selection
        UploadPhoto.putClientProperty("selectedPhoto", null);
    }

    private void setupTableListeners() {
        // Team table selection listener
        javax.swing.event.ListSelectionListener teamTableListener = new javax.swing.event.ListSelectionListener() {
            @Override
            public void valueChanged(javax.swing.event.ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    int selectedRow = TeamTable.getSelectedRow();
                    if (selectedRow >= 0) {
                        String teamId = TeamTable.getValueAt(selectedRow, 0).toString();
                        teamController.loadTeamFromTable(teamId, TeamIDTextField, TMTeamNameTextField,
                                ManagerTextField, NoPlayerTextField);
                    }
                }
            }
        };
        TeamTable.getSelectionModel().addListSelectionListener(teamTableListener);

        // Player table selection listener
        javax.swing.event.ListSelectionListener playerTableListener = new javax.swing.event.ListSelectionListener() {
            @Override
            public void valueChanged(javax.swing.event.ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    int selectedRow = PlayerTable.getSelectedRow();
                    if (selectedRow >= 0) {
                        String playerId = PlayerTable.getValueAt(selectedRow, 0).toString();
                        playerController.loadPlayerFromTable(playerId, PlayerIDTextField, PlayerNameTextField,
                                PlayerAgeTextField, PlayerJerseyNoTextField,
                                PlayerPositionTextField, PMTeamNameTextField, photoLabel);
                        currentPhotoFileName = PlayerTable.getValueAt(selectedRow, 6).toString();
                    }
                }
            }
        };
        PlayerTable.getSelectionModel().addListSelectionListener(playerTableListener);
    }

    private void setupPhotoColumnRenderer() {
        // Create a custom renderer for the Photo column (column 6)
        PlayerTable.getColumnModel().getColumn(6).setCellRenderer(new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public java.awt.Component getTableCellRendererComponent(javax.swing.JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {

                // Call the parent method to get default styling
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                // Set text to "View Photo" if photo exists, otherwise "No Photo"
                if (value != null && !value.toString().isEmpty()) {
                    setText("View Photo");
                    setForeground(java.awt.Color.BLUE);
                    setToolTipText("Click to view photo");
                } else {
                    setText("No Photo");
                    setForeground(java.awt.Color.GRAY);
                    setToolTipText("No photo available");
                }

                // Center the text
                setHorizontalAlignment(javax.swing.SwingConstants.CENTER);

                return this;
            }
        });
    }

    private void setupPhotoClickListener() {
        PlayerTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                int row = PlayerTable.rowAtPoint(evt.getPoint());
                int col = PlayerTable.columnAtPoint(evt.getPoint());

                // Check if clicked on the Photo column (column 6)
                if (row >= 0 && col == 6) {
                    String playerId = PlayerTable.getValueAt(row, 0).toString();
                    showPlayerPhoto(playerId);
                }
            }
        });
    }

    private void showPlayerPhoto(String playerId) {
        // Find the player
        for (Player player : playerController.getPlayers()) {
            if (player.getPlayerId().equals(playerId)) {
                String photoFileName = player.getPhotoPath();

                if (photoFileName != null && !photoFileName.isEmpty()) {
                    File photoFile = new File("player_photos/" + photoFileName);

                    if (photoFile.exists()) {
                        try {
                            // Create a dialog to show the photo
                            javax.swing.JDialog photoDialog = new javax.swing.JDialog(this,
                                    "Photo: " + player.getPlayerName(), true);
                            photoDialog.setLayout(new java.awt.BorderLayout());

                            // Create a label to display the photo
                            javax.swing.JLabel photoLabel = new javax.swing.JLabel();
                            photoLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);

                            // Load and scale the image
                            java.awt.Image originalImage = javax.imageio.ImageIO.read(photoFile);
                            java.awt.Image scaledImage = originalImage.getScaledInstance(
                                    400, 400, java.awt.Image.SCALE_SMOOTH);
                            photoLabel.setIcon(new javax.swing.ImageIcon(scaledImage));

                            // Add player info
                            javax.swing.JLabel infoLabel = new javax.swing.JLabel(
                                    "<html><center><b>" + player.getPlayerName() + "</b><br>" +
                                            "Team: " + player.getTeamName() + "<br>" +
                                            "Position: " + player.getPosition() + "<br>" +
                                            "Jersey: #" + player.getJerseyNo() + "</center></html>");
                            infoLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
                            infoLabel.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10));

                            // Add close button
                            javax.swing.JButton closeButton = new javax.swing.JButton("Close");
                            closeButton.addActionListener(e -> photoDialog.dispose());

                            javax.swing.JPanel buttonPanel = new javax.swing.JPanel();
                            buttonPanel.add(closeButton);

                            // Add components
                            photoDialog.add(infoLabel, java.awt.BorderLayout.NORTH);
                            photoDialog.add(new javax.swing.JScrollPane(photoLabel), java.awt.BorderLayout.CENTER);
                            photoDialog.add(buttonPanel, java.awt.BorderLayout.SOUTH);

                            // Set dialog size and show
                            photoDialog.setSize(450, 550);
                            photoDialog.setLocationRelativeTo(this);
                            photoDialog.setVisible(true);

                        } catch (Exception e) {
                            javax.swing.JOptionPane.showMessageDialog(this,
                                    "Error loading photo: " + e.getMessage(),
                                    "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
                        }
                    } else {
                        javax.swing.JOptionPane.showMessageDialog(this,
                                "Photo file not found!",
                                "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
                    }
                } else {
                    javax.swing.JOptionPane.showMessageDialog(this,
                            "No photo available for this player",
                            "Information", javax.swing.JOptionPane.INFORMATION_MESSAGE);
                }
                break;
            }
        }
    }

    private void addActionListenersToTeamButtons() {
        // Add Team Button
        TMAddBtn.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                boolean success = teamController.addTeam(
                        TeamIDTextField.getText(),
                        TMTeamNameTextField.getText(),
                        ManagerTextField.getText(),
                        NoPlayerTextField.getText());

                if (success) {
                    loadTeamData();
                    clearTeamFields();
                    updateDashboardStats();
                }
            }
        });

        // Update Team Button
        TMUpdateBtn.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                boolean success = teamController.updateTeam(
                        TeamIDTextField.getText(),
                        TMTeamNameTextField.getText(),
                        ManagerTextField.getText(),
                        NoPlayerTextField.getText());

                if (success) {
                    loadTeamData();
                    clearTeamFields();
                    updateDashboardStats();
                }
            }
        });

        // Delete Team Button
        TMDeleteBtn.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                boolean success = teamController.deleteTeam(TeamIDTextField.getText());

                if (success) {
                    loadTeamData();
                    clearTeamFields();
                    updateDashboardStats();
                }
            }
        });

        // Read Team Button
        TMReadBtn.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loadTeamData();
            }
        });
    }

    private void addActionListenersToPlayerButtons() {
        // Add Player Button
        PMAddBtn.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                // Get the selected photo path
                String photoPath = (String) UploadPhoto.getClientProperty("selectedPhoto");

                boolean success = playerController.addPlayer(
                        PlayerIDTextField.getText(),
                        PlayerNameTextField.getText(),
                        PlayerAgeTextField.getText(),
                        PlayerJerseyNoTextField.getText(),
                        PlayerPositionTextField.getText(),
                        PMTeamNameTextField.getText(),
                        photoPath // Pass the photo path separately
                );

                if (success) {
                    loadPlayerData();
                    clearPlayerFields();
                    UploadPhoto.putClientProperty("selectedPhoto", null);
                    photoLabel.setIcon(null);
                    photoLabel.setText("No Photo");
                    updateDashboardStats();
                }
            }
        });

        // Update Player Button
        PMUpdateBtn.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                // Get the newly selected photo path
                String newPhotoPath = (String) UploadPhoto.getClientProperty("selectedPhoto");
                String photoPath = newPhotoPath;

                // If no new photo was selected, keep the existing photo
                if (photoPath == null || photoPath.isEmpty()) {
                    int selectedRow = PlayerTable.getSelectedRow();
                    if (selectedRow >= 0) {
                        // Get existing photo path from the table
                        Object photoValue = PlayerTable.getValueAt(selectedRow, 6);
                        if (photoValue != null) {
                            photoPath = photoValue.toString();
                        }
                    }
                }

                boolean success = playerController.updatePlayer(
                        PlayerIDTextField.getText(),
                        PlayerNameTextField.getText(),
                        PlayerAgeTextField.getText(),
                        PlayerJerseyNoTextField.getText(),
                        PlayerPositionTextField.getText(),
                        PMTeamNameTextField.getText(),
                        photoPath);

                if (success) {
                    loadPlayerData();
                    clearPlayerFields();
                    UploadPhoto.putClientProperty("selectedPhoto", null);
                    photoLabel.setIcon(null);
                    photoLabel.setText("No Photo");
                    updateDashboardStats();
                }
            }
        });

        // Delete Player Button
        PMDeleteBtn.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                boolean success = playerController.deletePlayer(PlayerIDTextField.getText());

                if (success) {
                    loadPlayerData();
                    clearPlayerFields();
                    UploadPhoto.putClientProperty("selectedPhoto", null);
                    photoLabel.setIcon(null);
                    photoLabel.setText("No Photo");
                    updateDashboardStats();
                }
            }
        });

        // Read Player Button
        PMReadBtn.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loadPlayerData();
            }
        });
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated
    // <editor-fold defaultstate="collapsed" desc="Generated
    // <editor-fold defaultstate="collapsed" desc="Generated
    // <editor-fold defaultstate="collapsed" desc="Generated
    // <editor-fold defaultstate="collapsed" desc="Generated
    // <editor-fold defaultstate="collapsed" desc="Generated
    // <editor-fold defaultstate="collapsed" desc="Generated
    // <editor-fold defaultstate="collapsed" desc="Generated
    // <editor-fold defaultstate="collapsed" desc="Generated
    // <editor-fold defaultstate="collapsed" desc="Generated
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        Login = new javax.swing.JPanel();
        LoginSignUp = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        LoginUsername = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        LoginPassword = new javax.swing.JPasswordField();
        LoginBtn = new javax.swing.JToggleButton();
        jLabel5 = new javax.swing.JLabel();
        LoginBg = new javax.swing.JLabel();
        SignUp = new javax.swing.JPanel();
        SignupLogin = new javax.swing.JLabel();
        jLabel51 = new javax.swing.JLabel();
        jLabel23 = new javax.swing.JLabel();
        jLabel24 = new javax.swing.JLabel();
        jLabel26 = new javax.swing.JLabel();
        jLabel27 = new javax.swing.JLabel();
        jLabel25 = new javax.swing.JLabel();
        jLabel28 = new javax.swing.JLabel();
        jLabel29 = new javax.swing.JLabel();
        SignUpRetypePw = new javax.swing.JPasswordField();
        SignUpFirstName = new javax.swing.JTextField();
        SignUpLastName = new javax.swing.JTextField();
        SignUpUsername = new javax.swing.JTextField();
        SignUpPassword = new javax.swing.JPasswordField();
        SignUpBtn = new javax.swing.JToggleButton();
        DateOfBirth = new datechooser.beans.DateChooserCombo();
        LoginBg1 = new javax.swing.JLabel();
        AdminDashboard = new javax.swing.JPanel();
        Nav1 = new javax.swing.JPanel();
        jPanel5 = new javax.swing.JPanel();
        Title1 = new javax.swing.JLabel();
        DashBoardBtn1 = new javax.swing.JLabel();
        ManageTeamBtn1 = new javax.swing.JLabel();
        ManagePlayerBtn1 = new javax.swing.JLabel();
        BookingBtn1 = new javax.swing.JLabel();
        ScheduleBtn1 = new javax.swing.JLabel();
        AnnouncementBtn1 = new javax.swing.JLabel();
        Main1 = new javax.swing.JPanel();
        jPanel6 = new javax.swing.JPanel();
        jLabel6 = new javax.swing.JLabel();
        ProfileBtn = new javax.swing.JLabel();
        jLabel17 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        RecentActivitesTextArea = new javax.swing.JTextArea();
        jLabel35 = new javax.swing.JLabel();
        jPanel4 = new javax.swing.JPanel();
        jLabel37 = new javax.swing.JLabel();
        jLabel42 = new javax.swing.JLabel();
        jLabel43 = new javax.swing.JLabel();
        jLabel44 = new javax.swing.JLabel();
        jLabel45 = new javax.swing.JLabel();
        jLabel46 = new javax.swing.JLabel();
        jPanel13 = new javax.swing.JPanel();
        jLabel38 = new javax.swing.JLabel();
        jLabel47 = new javax.swing.JLabel();
        jPanel15 = new javax.swing.JPanel();
        jLabel39 = new javax.swing.JLabel();
        jLabel48 = new javax.swing.JLabel();
        jPanel17 = new javax.swing.JPanel();
        jLabel40 = new javax.swing.JLabel();
        jLabel49 = new javax.swing.JLabel();
        jPanel24 = new javax.swing.JPanel();
        jLabel41 = new javax.swing.JLabel();
        jLabel50 = new javax.swing.JLabel();
        TeamManagement = new javax.swing.JPanel();
        Nav2 = new javax.swing.JPanel();
        jPanel19 = new javax.swing.JPanel();
        Title2 = new javax.swing.JLabel();
        DashBoardBtn2 = new javax.swing.JLabel();
        ManageTeamBtn2 = new javax.swing.JLabel();
        ManagePlayerBtn2 = new javax.swing.JLabel();
        BookingBtn2 = new javax.swing.JLabel();
        ScheduleBtn2 = new javax.swing.JLabel();
        AnnouncementBtn2 = new javax.swing.JLabel();
        Main2 = new javax.swing.JPanel();
        jPanel10 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        TMTeamNameTextField = new javax.swing.JTextField();
        ManagerTextField = new javax.swing.JTextField();
        TeamIDTextField = new javax.swing.JTextField();
        NoPlayerTextField = new javax.swing.JTextField();
        jLabel11 = new javax.swing.JLabel();
        TMSearchTextField = new javax.swing.JTextField();
        jLabel12 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        TeamTable = new javax.swing.JTable();
        TMAddBtn = new javax.swing.JButton();
        TMUpdateBtn = new javax.swing.JButton();
        TMDeleteBtn = new javax.swing.JButton();
        TMReadBtn = new javax.swing.JButton();
        TMSortByComboBox = new javax.swing.JComboBox<>();
        TMSearchComboBox = new javax.swing.JComboBox<>();
        jLabel32 = new javax.swing.JLabel();
        jLabel30 = new javax.swing.JLabel();
        TMSearchBtn = new javax.swing.JLabel();
        jLabel53 = new javax.swing.JLabel();
        PlayerManagement = new javax.swing.JPanel();
        Nav3 = new javax.swing.JPanel();
        jPanel7 = new javax.swing.JPanel();
        Title3 = new javax.swing.JLabel();
        DashBoardBtn3 = new javax.swing.JLabel();
        ManageTeamBtn3 = new javax.swing.JLabel();
        ManagePlayerBtn3 = new javax.swing.JLabel();
        BookingBtn3 = new javax.swing.JLabel();
        ScheduleBtn3 = new javax.swing.JLabel();
        AnnouncementBtn3 = new javax.swing.JLabel();
        Main3 = new javax.swing.JPanel();
        jPanel9 = new javax.swing.JPanel();
        jLabel13 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        PlayerNameTextField = new javax.swing.JTextField();
        jLabel15 = new javax.swing.JLabel();
        PlayerIDTextField = new javax.swing.JTextField();
        jLabel16 = new javax.swing.JLabel();
        PlayerAgeTextField = new javax.swing.JTextField();
        jLabel18 = new javax.swing.JLabel();
        PlayerJerseyNoTextField = new javax.swing.JTextField();
        jLabel19 = new javax.swing.JLabel();
        PlayerPositionTextField = new javax.swing.JTextField();
        jLabel20 = new javax.swing.JLabel();
        jLabel21 = new javax.swing.JLabel();
        PMSearchTextField = new javax.swing.JTextField();
        PMSortByComboBox = new javax.swing.JComboBox<>();
        jScrollBar1 = new javax.swing.JScrollBar();
        jScrollPane2 = new javax.swing.JScrollPane();
        PlayerTable = new javax.swing.JTable();
        jLabel22 = new javax.swing.JLabel();
        PMTeamNameTextField = new javax.swing.JTextField();
        PMAddBtn = new javax.swing.JButton();
        PMUpdateBtn = new javax.swing.JButton();
        PMDeleteBtn = new javax.swing.JButton();
        PMReadBtn = new javax.swing.JButton();
        PMSearchComboBox = new javax.swing.JComboBox<>();
        PMSearchBtn = new javax.swing.JLabel();
        jLabel31 = new javax.swing.JLabel();
        UploadPhoto = new javax.swing.JButton();
        jLabel34 = new javax.swing.JLabel();
        jLabel33 = new javax.swing.JLabel();
        jLabel54 = new javax.swing.JLabel();
        BookingManagement = new javax.swing.JPanel();
        Nav4 = new javax.swing.JPanel();
        jPanel20 = new javax.swing.JPanel();
        Title4 = new javax.swing.JLabel();
        DashBoardBtn4 = new javax.swing.JLabel();
        ManageTeamBtn4 = new javax.swing.JLabel();
        ManagePlayerBtn4 = new javax.swing.JLabel();
        BookingBtn4 = new javax.swing.JLabel();
        ScheduleBtn4 = new javax.swing.JLabel();
        AnnouncementBtn4 = new javax.swing.JLabel();
        Main4 = new javax.swing.JPanel();
        jPanel12 = new javax.swing.JPanel();
        jLabel36 = new javax.swing.JLabel();
        jScrollPane4 = new javax.swing.JScrollPane();
        BookingTable = new javax.swing.JTable();
        jLabel55 = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();
        ScheduleManagement = new javax.swing.JPanel();
        Nav5 = new javax.swing.JPanel();
        jPanel8 = new javax.swing.JPanel();
        Title5 = new javax.swing.JLabel();
        DashBoardBtn5 = new javax.swing.JLabel();
        ManageTeamBtn5 = new javax.swing.JLabel();
        ManagePlayerBtn5 = new javax.swing.JLabel();
        BookingBtn5 = new javax.swing.JLabel();
        ScheduleBtn5 = new javax.swing.JLabel();
        AnnouncementBtn5 = new javax.swing.JLabel();
        Main5 = new javax.swing.JPanel();
        jPanel14 = new javax.swing.JPanel();
        dateChooserPanel1 = new datechooser.beans.DateChooserPanel();
        jLabel52 = new javax.swing.JLabel();
        jLabel56 = new javax.swing.JLabel();
        AnnouncementManagement = new javax.swing.JPanel();
        Nav6 = new javax.swing.JPanel();
        jPanel11 = new javax.swing.JPanel();
        Title6 = new javax.swing.JLabel();
        DashBoardBtn6 = new javax.swing.JLabel();
        ManageTeamBtn6 = new javax.swing.JLabel();
        ManagePlayerBtn6 = new javax.swing.JLabel();
        BookingBtn6 = new javax.swing.JLabel();
        ScheduleBtn6 = new javax.swing.JLabel();
        AnnouncementBtn6 = new javax.swing.JLabel();
        Main6 = new javax.swing.JPanel();
        jPanel16 = new javax.swing.JPanel();
        jLabel58 = new javax.swing.JLabel();
        jLabel57 = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        cross3 = new javax.swing.JLabel();
        jLabel59 = new javax.swing.JLabel();
        jPanel27 = new javax.swing.JPanel();
        jLabel64 = new javax.swing.JLabel();
        jLabel65 = new javax.swing.JLabel();
        jPanel26 = new javax.swing.JPanel();
        cross1 = new javax.swing.JLabel();
        jLabel63 = new javax.swing.JLabel();
        jPanel28 = new javax.swing.JPanel();
        cross2 = new javax.swing.JLabel();
        jLabel67 = new javax.swing.JLabel();
        jPanel29 = new javax.swing.JPanel();
        jLabel68 = new javax.swing.JLabel();
        jLabel69 = new javax.swing.JLabel();
        ProfileSettings = new javax.swing.JPanel();
        Nav7 = new javax.swing.JPanel();
        jPanel21 = new javax.swing.JPanel();
        Title7 = new javax.swing.JLabel();
        DashBoardBtn7 = new javax.swing.JLabel();
        ManageTeamBtn7 = new javax.swing.JLabel();
        ManagePlayerBtn7 = new javax.swing.JLabel();
        BookingBtn7 = new javax.swing.JLabel();
        ScheduleBtn7 = new javax.swing.JLabel();
        AnnouncementBtn7 = new javax.swing.JLabel();
        Main7 = new javax.swing.JPanel();
        jPanel18 = new javax.swing.JPanel();
        LogOutBtn = new javax.swing.JLabel();
        jLabel60 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setMinimumSize(new java.awt.Dimension(1311, 720));
        setSize(new java.awt.Dimension(1310, 720));
        getContentPane().setLayout(null);

        jPanel1.setOpaque(false);
        jPanel1.setLayout(new java.awt.CardLayout());

        Login.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        LoginSignUp.setFont(new java.awt.Font("Serif", 0, 18)); // NOI18N
        LoginSignUp.setForeground(new java.awt.Color(51, 102, 0));
        LoginSignUp.setText("Sign Up");
        Login.add(LoginSignUp, new org.netbeans.lib.awtextra.AbsoluteConstraints(940, 500, -1, -1));
        LoginSignUp.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                System.out.print("Button clicked");
                // Get the CardLayout from jPanel1
                CardLayout cardLayout = (CardLayout) jPanel1.getLayout();

                // Switch to the AdminDashboard card
                cardLayout.show(jPanel1, "card2");
            }

            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                LoginSignUp.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
            }
        });

        jLabel2.setFont(new java.awt.Font("SimSun", 0, 48)); // NOI18N
        jLabel2.setText("Login");
        Login.add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(860, 80, -1, -1));

        jLabel4.setFont(new java.awt.Font("Serif", 0, 18)); // NOI18N
        jLabel4.setText("Username");
        Login.add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(850, 180, -1, -1));

        LoginUsername.setFont(new java.awt.Font("Serif", 0, 24)); // NOI18N
        LoginUsername.setBorder(new javax.swing.border.MatteBorder(null));
        LoginUsername.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                LoginUsernameActionPerformed(evt);
            }
        });
        Login.add(LoginUsername, new org.netbeans.lib.awtextra.AbsoluteConstraints(850, 210, 320, 57));

        jLabel3.setFont(new java.awt.Font("Serif", 0, 18)); // NOI18N
        jLabel3.setText("Password");
        Login.add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(850, 290, -1, -1));

        LoginPassword.setFont(new java.awt.Font("Serif", 0, 24)); // NOI18N
        LoginPassword.setBorder(new javax.swing.border.MatteBorder(null));
        LoginPassword.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                LoginPasswordActionPerformed(evt);
            }
        });
        Login.add(LoginPassword, new org.netbeans.lib.awtextra.AbsoluteConstraints(850, 320, 320, 60));

        LoginBtn.setBackground(new java.awt.Color(51, 51, 51));
        LoginBtn.setFont(new java.awt.Font("Serif", 0, 18)); // NOI18N
        LoginBtn.setForeground(new java.awt.Color(204, 204, 204));
        LoginBtn.setText("Login");
        LoginBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                LoginBtnActionPerformed(evt);
            }
        });
        Login.add(LoginBtn, new org.netbeans.lib.awtextra.AbsoluteConstraints(950, 420, 112, 38));

        jLabel5.setFont(new java.awt.Font("Serif", 0, 18)); // NOI18N
        jLabel5.setText("New User ?");
        Login.add(jLabel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(850, 500, -1, -1));

        LoginBg.setIcon(new javax.swing.ImageIcon(getClass().getResource("/View/Scoccer.jpeg"))); // NOI18N
        Login.add(LoginBg, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, -1, -1));

        jPanel1.add(Login, "card1");

        SignUp.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        SignupLogin.setFont(new java.awt.Font("Serif", 0, 18)); // NOI18N
        SignupLogin.setForeground(new java.awt.Color(51, 102, 0));
        SignupLogin.setText("Login");
        SignUp.add(SignupLogin, new org.netbeans.lib.awtextra.AbsoluteConstraints(1070, 610, -1, -1));
        SignupLogin.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                System.out.print("Button clicked");
                // Get the CardLayout from jPanel1
                CardLayout cardLayout = (CardLayout) jPanel1.getLayout();

                // Switch to the AdminDashboard card
                cardLayout.show(jPanel1, "card1");
            }

            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                SignupLogin.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
            }
        });

        jLabel51.setFont(new java.awt.Font("Serif", 0, 18)); // NOI18N
        jLabel51.setText("Already have an Account?");
        SignUp.add(jLabel51, new org.netbeans.lib.awtextra.AbsoluteConstraints(870, 610, -1, -1));

        jLabel23.setFont(new java.awt.Font("SimSun", 0, 48)); // NOI18N
        jLabel23.setText("Sign Up");
        SignUp.add(jLabel23, new org.netbeans.lib.awtextra.AbsoluteConstraints(850, 40, -1, -1));

        jLabel24.setFont(new java.awt.Font("Serif", 0, 18)); // NOI18N
        jLabel24.setText("Username");
        SignUp.add(jLabel24, new org.netbeans.lib.awtextra.AbsoluteConstraints(850, 320, -1, -1));

        jLabel26.setFont(new java.awt.Font("Serif", 0, 18)); // NOI18N
        jLabel26.setText("First Name");
        SignUp.add(jLabel26, new org.netbeans.lib.awtextra.AbsoluteConstraints(850, 100, -1, -1));

        jLabel27.setFont(new java.awt.Font("Serif", 0, 18)); // NOI18N
        jLabel27.setText("Last Name");
        SignUp.add(jLabel27, new org.netbeans.lib.awtextra.AbsoluteConstraints(850, 170, -1, -1));

        jLabel25.setFont(new java.awt.Font("Serif", 0, 18)); // NOI18N
        jLabel25.setText("Retype Password");
        SignUp.add(jLabel25, new org.netbeans.lib.awtextra.AbsoluteConstraints(850, 460, -1, -1));

        jLabel28.setFont(new java.awt.Font("Serif", 0, 18)); // NOI18N
        jLabel28.setText("Date of Birth");
        SignUp.add(jLabel28, new org.netbeans.lib.awtextra.AbsoluteConstraints(850, 250, -1, -1));

        jLabel29.setFont(new java.awt.Font("Serif", 0, 18)); // NOI18N
        jLabel29.setText("Password");
        SignUp.add(jLabel29, new org.netbeans.lib.awtextra.AbsoluteConstraints(850, 390, -1, -1));

        SignUpRetypePw.setFont(new java.awt.Font("Serif", 0, 24)); // NOI18N
        SignUpRetypePw.setBorder(new javax.swing.border.MatteBorder(null));
        SignUpRetypePw.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                SignUpRetypePwActionPerformed(evt);
            }
        });
        SignUp.add(SignUpRetypePw, new org.netbeans.lib.awtextra.AbsoluteConstraints(850, 490, 320, 40));

        SignUpFirstName.setFont(new java.awt.Font("Serif", 0, 24)); // NOI18N
        SignUpFirstName.setBorder(new javax.swing.border.MatteBorder(null));
        SignUpFirstName.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                SignUpFirstNameActionPerformed(evt);
            }
        });
        SignUp.add(SignUpFirstName, new org.netbeans.lib.awtextra.AbsoluteConstraints(850, 130, 320, 40));

        SignUpLastName.setFont(new java.awt.Font("Serif", 0, 24)); // NOI18N
        SignUpLastName.setBorder(new javax.swing.border.MatteBorder(null));
        SignUpLastName.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                SignUpLastNameActionPerformed(evt);
            }
        });
        SignUp.add(SignUpLastName, new org.netbeans.lib.awtextra.AbsoluteConstraints(850, 200, 320, 40));

        SignUpUsername.setFont(new java.awt.Font("Serif", 0, 24)); // NOI18N
        SignUpUsername.setBorder(new javax.swing.border.MatteBorder(null));
        SignUpUsername.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                SignUpUsernameActionPerformed(evt);
            }
        });
        SignUp.add(SignUpUsername, new org.netbeans.lib.awtextra.AbsoluteConstraints(850, 350, 320, 40));

        SignUpPassword.setFont(new java.awt.Font("Serif", 0, 24)); // NOI18N
        SignUpPassword.setBorder(new javax.swing.border.MatteBorder(null));
        SignUpPassword.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                SignUpPasswordActionPerformed(evt);
            }
        });
        SignUp.add(SignUpPassword, new org.netbeans.lib.awtextra.AbsoluteConstraints(850, 420, 320, 40));

        SignUpBtn.setBackground(new java.awt.Color(51, 51, 51));
        SignUpBtn.setFont(new java.awt.Font("Serif", 0, 18)); // NOI18N
        SignUpBtn.setForeground(new java.awt.Color(204, 204, 204));
        SignUpBtn.setText("Sign Up");
        SignUpBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                SignUpBtnActionPerformed(evt);
            }
        });
        SignUp.add(SignUpBtn, new org.netbeans.lib.awtextra.AbsoluteConstraints(950, 540, 112, 38));

        DateOfBirth.setCurrentView(new datechooser.view.appearance.AppearancesList("Light",
            new datechooser.view.appearance.ViewAppearance("custom",
                new datechooser.view.appearance.swing.SwingCellAppearance(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 12),
                    new java.awt.Color(0, 0, 0),
                    new java.awt.Color(0, 0, 255),
                    false,
                    true,
                    new datechooser.view.appearance.swing.ButtonPainter()),
                new datechooser.view.appearance.swing.SwingCellAppearance(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 12),
                    new java.awt.Color(0, 0, 0),
                    new java.awt.Color(0, 0, 255),
                    true,
                    true,
                    new datechooser.view.appearance.swing.ButtonPainter()),
                new datechooser.view.appearance.swing.SwingCellAppearance(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 12),
                    new java.awt.Color(0, 0, 255),
                    new java.awt.Color(0, 0, 255),
                    false,
                    true,
                    new datechooser.view.appearance.swing.ButtonPainter()),
                new datechooser.view.appearance.swing.SwingCellAppearance(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 12),
                    new java.awt.Color(128, 128, 128),
                    new java.awt.Color(0, 0, 255),
                    false,
                    true,
                    new datechooser.view.appearance.swing.LabelPainter()),
                new datechooser.view.appearance.swing.SwingCellAppearance(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 12),
                    new java.awt.Color(0, 0, 0),
                    new java.awt.Color(0, 0, 255),
                    false,
                    true,
                    new datechooser.view.appearance.swing.LabelPainter()),
                new datechooser.view.appearance.swing.SwingCellAppearance(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 12),
                    new java.awt.Color(0, 0, 0),
                    new java.awt.Color(255, 0, 0),
                    false,
                    false,
                    new datechooser.view.appearance.swing.ButtonPainter()),
                (datechooser.view.BackRenderer)null,
                false,
                true)));
    DateOfBirth.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 1));
    DateOfBirth.setShowOneMonth(true);
    SignUp.add(DateOfBirth, new org.netbeans.lib.awtextra.AbsoluteConstraints(850, 280, 320, 40));

    LoginBg1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/View/Scoccer.jpeg"))); // NOI18N
    SignUp.add(LoginBg1, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, -1, -1));

    jPanel1.add(SignUp, "card2");

    AdminDashboard.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

    Nav1.setBackground(new java.awt.Color(0, 102, 102));
    Nav1.setMinimumSize(new java.awt.Dimension(225, 720));
    Nav1.setLayout(new java.awt.CardLayout());

    jPanel5.setBackground(new java.awt.Color(0, 102, 102));
    jPanel5.setMinimumSize(new java.awt.Dimension(220, 720));

    Title1.setFont(new java.awt.Font("Serif", 0, 48)); // NOI18N
    Title1.setForeground(new java.awt.Color(0, 51, 51));
    Title1.setText("volta.");

    DashBoardBtn1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
    DashBoardBtn1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/View/ClickedDashboard.png"))); // NOI18N

    ManageTeamBtn1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
    ManageTeamBtn1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/View/UnclickedManageTeam.png"))); // NOI18N

    ManagePlayerBtn1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
    ManagePlayerBtn1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/View/UnclickedManagePlayer.png"))); // NOI18N

    BookingBtn1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
    BookingBtn1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/View/UnclickedBooking.png"))); // NOI18N

    ScheduleBtn1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
    ScheduleBtn1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/View/UnclickedSchedule.png"))); // NOI18N

    AnnouncementBtn1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
    AnnouncementBtn1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/View/UnclickedAnnouncement.png"))); // NOI18N

    javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
    jPanel5.setLayout(jPanel5Layout);
    jPanel5Layout.setHorizontalGroup(
        jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(jPanel5Layout.createSequentialGroup()
            .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel5Layout.createSequentialGroup()
                    .addGap(118, 118, 118)
                    .addComponent(Title1, javax.swing.GroupLayout.PREFERRED_SIZE, 108, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGroup(jPanel5Layout.createSequentialGroup()
                    .addGap(6, 6, 6)
                    .addComponent(DashBoardBtn1, javax.swing.GroupLayout.PREFERRED_SIZE, 220, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGroup(jPanel5Layout.createSequentialGroup()
                    .addGap(6, 6, 6)
                    .addComponent(ManageTeamBtn1, javax.swing.GroupLayout.PREFERRED_SIZE, 220, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGroup(jPanel5Layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(ManagePlayerBtn1, javax.swing.GroupLayout.PREFERRED_SIZE, 216, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGroup(jPanel5Layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(BookingBtn1, javax.swing.GroupLayout.PREFERRED_SIZE, 218, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGroup(jPanel5Layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(ScheduleBtn1, javax.swing.GroupLayout.PREFERRED_SIZE, 218, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGroup(jPanel5Layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(AnnouncementBtn1, javax.swing.GroupLayout.PREFERRED_SIZE, 218, javax.swing.GroupLayout.PREFERRED_SIZE)))
            .addContainerGap(14, Short.MAX_VALUE))
    );
    jPanel5Layout.setVerticalGroup(
        jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(jPanel5Layout.createSequentialGroup()
            .addGap(33, 33, 33)
            .addComponent(Title1, javax.swing.GroupLayout.PREFERRED_SIZE, 58, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(89, 89, 89)
            .addComponent(DashBoardBtn1)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(ManageTeamBtn1)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
            .addComponent(ManagePlayerBtn1)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
            .addComponent(BookingBtn1)
            .addGap(12, 12, 12)
            .addComponent(ScheduleBtn1, javax.swing.GroupLayout.PREFERRED_SIZE, 66, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
            .addComponent(AnnouncementBtn1, javax.swing.GroupLayout.PREFERRED_SIZE, 51, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addContainerGap())
    );

    // Code adding the component to the parent container - not shown here

    DashBoardBtn1.addMouseListener(new java.awt.event.MouseAdapter() {
        @Override
        public void mouseClicked(java.awt.event.MouseEvent e) {
            System.out.print("Button clicked");
            // Get the CardLayout from jPanel1
            CardLayout cardLayout = (CardLayout) jPanel1.getLayout();

            // Switch to the AdminDashboard card
            cardLayout.show(jPanel1, "card3");
        }

        @Override
        public void mouseEntered(java.awt.event.MouseEvent e) {
            DashBoardBtn1.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        }
    });
    ManageTeamBtn1.addMouseListener(new java.awt.event.MouseAdapter() {
        @Override
        public void mouseClicked(java.awt.event.MouseEvent e) {
            System.out.print("Button clicked");
            // Get the CardLayout from jPanel1
            CardLayout cardLayout = (CardLayout) jPanel1.getLayout();

            // Switch to the AdminDashboard card
            cardLayout.show(jPanel1, "card4");
        }

        @Override
        public void mouseEntered(java.awt.event.MouseEvent e) {
            ManageTeamBtn1.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        }
    });
    ManagePlayerBtn1.addMouseListener(new java.awt.event.MouseAdapter() {
        @Override
        public void mouseClicked(java.awt.event.MouseEvent e) {
            System.out.print("Button clicked");
            // Get the CardLayout from jPanel1
            CardLayout cardLayout = (CardLayout) jPanel1.getLayout();

            // Switch to the AdminDashboard card
            cardLayout.show(jPanel1, "card5");
        }

        @Override
        public void mouseEntered(java.awt.event.MouseEvent e) {
            ManagePlayerBtn1.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        }
    });
    BookingBtn1.addMouseListener(new java.awt.event.MouseAdapter() {
        @Override
        public void mouseClicked(java.awt.event.MouseEvent e) {
            System.out.print("Button clicked");
            // Get the CardLayout from jPanel1
            CardLayout cardLayout = (CardLayout) jPanel1.getLayout();

            // Switch to the AdminDashboard card
            cardLayout.show(jPanel1, "card6");
        }

        @Override
        public void mouseEntered(java.awt.event.MouseEvent e) {
            BookingBtn1.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        }
    });
    ScheduleBtn1.addMouseListener(new java.awt.event.MouseAdapter() {
        @Override
        public void mouseClicked(java.awt.event.MouseEvent e) {
            System.out.print("Button clicked");
            // Get the CardLayout from jPanel1
            CardLayout cardLayout = (CardLayout) jPanel1.getLayout();

            // Switch to the AdminDashboard card
            cardLayout.show(jPanel1, "card7");
        }

        @Override
        public void mouseEntered(java.awt.event.MouseEvent e) {
            ScheduleBtn1.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        }
    });
    AnnouncementBtn1.addMouseListener(new java.awt.event.MouseAdapter() {
        @Override
        public void mouseClicked(java.awt.event.MouseEvent e) {
            System.out.print("Button clicked");
            // Get the CardLayout from jPanel1
            CardLayout cardLayout = (CardLayout) jPanel1.getLayout();

            // Switch to the AdminDashboard card
            cardLayout.show(jPanel1, "card8");
        }

        @Override
        public void mouseEntered(java.awt.event.MouseEvent e) {
            AnnouncementBtn1.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        }
    });

    Nav1.add(jPanel5, "card2");

    AdminDashboard.add(Nav1, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 240, 720));

    Main1.setLayout(new java.awt.CardLayout());

    jPanel6.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

    jLabel6.setBackground(new java.awt.Color(0, 153, 204));
    jLabel6.setFont(new java.awt.Font("Serif", 0, 36)); // NOI18N
    jLabel6.setForeground(new java.awt.Color(0, 153, 153));
    jLabel6.setText("Welcome Back Shaira!");
    jPanel6.add(jLabel6, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 120, -1, 56));

    ProfileBtn.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
    ProfileBtn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/View/ProfileIcon.png"))); // NOI18N
    ProfileBtn.setText(".");
    jPanel6.add(ProfileBtn, new org.netbeans.lib.awtextra.AbsoluteConstraints(930, 100, 80, 60));
    ProfileBtn.addMouseListener(new java.awt.event.MouseAdapter() {
        @Override
        public void mouseClicked(java.awt.event.MouseEvent e) {
            System.out.print("Button clicked");
            // Get the CardLayout from jPanel1
            CardLayout cardLayout = (CardLayout) jPanel1.getLayout();

            // Switch to the AdminDashboard card
            cardLayout.show(jPanel1, "card9");
        }

        @Override
        public void mouseEntered(java.awt.event.MouseEvent e) {
            ProfileBtn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        }
    });

    jLabel17.setIcon(new javax.swing.ImageIcon(getClass().getResource("/View/DashBoardBg.jpg"))); // NOI18N
    jPanel6.add(jLabel17, new org.netbeans.lib.awtextra.AbsoluteConstraints(-10, 0, 1080, 180));

    jPanel2.setBackground(new java.awt.Color(153, 153, 153));

    RecentActivitesTextArea.setColumns(20);
    RecentActivitesTextArea.setRows(5);
    jScrollPane3.setViewportView(RecentActivitesTextArea);

    jLabel35.setFont(new java.awt.Font("Serif", 1, 24)); // NOI18N
    jLabel35.setForeground(new java.awt.Color(102, 102, 102));
    jLabel35.setText("Recent Activities");

    javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
    jPanel2.setLayout(jPanel2Layout);
    jPanel2Layout.setHorizontalGroup(
        jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(jPanel2Layout.createSequentialGroup()
            .addGap(22, 22, 22)
            .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(jLabel35)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 330, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addContainerGap(48, Short.MAX_VALUE))
    );
    jPanel2Layout.setVerticalGroup(
        jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
            .addContainerGap(12, Short.MAX_VALUE)
            .addComponent(jLabel35)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
            .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 170, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(14, 14, 14))
    );

    jPanel6.add(jPanel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(90, 230, 400, 240));

    jPanel4.setBackground(new java.awt.Color(153, 153, 153));

    jLabel37.setFont(new java.awt.Font("Serif", 1, 24)); // NOI18N
    jLabel37.setForeground(new java.awt.Color(102, 102, 102));
    jLabel37.setText("Upcoming Match");

    jLabel42.setFont(new java.awt.Font("Serif", 1, 24)); // NOI18N
    jLabel42.setForeground(new java.awt.Color(68, 66, 66));
    jLabel42.setText("Team1");

    jLabel43.setFont(new java.awt.Font("Serif", 1, 24)); // NOI18N
    jLabel43.setForeground(new java.awt.Color(68, 66, 66));
    jLabel43.setText("VS");

    jLabel44.setFont(new java.awt.Font("Serif", 1, 24)); // NOI18N
    jLabel44.setForeground(new java.awt.Color(68, 66, 66));
    jLabel44.setText("Team2");

    jLabel45.setFont(new java.awt.Font("Serif", 1, 24)); // NOI18N
    jLabel45.setForeground(new java.awt.Color(68, 66, 66));
    jLabel45.setText("Time");

    jLabel46.setFont(new java.awt.Font("Serif", 1, 24)); // NOI18N
    jLabel46.setForeground(new java.awt.Color(68, 66, 66));
    jLabel46.setText("Date");

    javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
    jPanel4.setLayout(jPanel4Layout);
    jPanel4Layout.setHorizontalGroup(
        jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(jPanel4Layout.createSequentialGroup()
            .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel4Layout.createSequentialGroup()
                    .addGap(166, 166, 166)
                    .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jLabel42)
                        .addComponent(jLabel44)
                        .addGroup(jPanel4Layout.createSequentialGroup()
                            .addGap(19, 19, 19)
                            .addComponent(jLabel43))))
                .addGroup(jPanel4Layout.createSequentialGroup()
                    .addGap(22, 22, 22)
                    .addComponent(jLabel37)))
            .addContainerGap(224, Short.MAX_VALUE))
        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
            .addGap(0, 0, Short.MAX_VALUE)
            .addComponent(jLabel45)
            .addGap(63, 63, 63))
        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGap(32, 32, 32)
                .addComponent(jLabel46)
                .addContainerGap(380, Short.MAX_VALUE)))
    );
    jPanel4Layout.setVerticalGroup(
        jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
            .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jLabel37)
            .addGap(18, 18, 18)
            .addComponent(jLabel42)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
            .addComponent(jLabel43)
            .addGap(9, 9, 9)
            .addComponent(jLabel44)
            .addGap(26, 26, 26)
            .addComponent(jLabel45)
            .addGap(22, 22, 22))
        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                .addContainerGap(201, Short.MAX_VALUE)
                .addComponent(jLabel46)
                .addGap(20, 20, 20)))
    );

    jPanel6.add(jPanel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(527, 230, 460, 240));

    jPanel13.setBackground(new java.awt.Color(153, 153, 153));

    jLabel38.setFont(new java.awt.Font("Serif", 1, 24)); // NOI18N
    jLabel38.setForeground(new java.awt.Color(102, 102, 102));
    jLabel38.setText("Total Teams");

    jLabel47.setFont(new java.awt.Font("Serif", 0, 80)); // NOI18N
    jLabel47.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
    jLabel47.setText("0");

    javax.swing.GroupLayout jPanel13Layout = new javax.swing.GroupLayout(jPanel13);
    jPanel13.setLayout(jPanel13Layout);
    jPanel13Layout.setHorizontalGroup(
        jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(jPanel13Layout.createSequentialGroup()
            .addGroup(jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel13Layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(jLabel38))
                .addGroup(jPanel13Layout.createSequentialGroup()
                    .addGap(69, 69, 69)
                    .addComponent(jLabel47, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)))
            .addContainerGap(67, Short.MAX_VALUE))
    );
    jPanel13Layout.setVerticalGroup(
        jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(jPanel13Layout.createSequentialGroup()
            .addContainerGap()
            .addComponent(jLabel38)
            .addGap(18, 18, 18)
            .addComponent(jLabel47, javax.swing.GroupLayout.PREFERRED_SIZE, 68, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addContainerGap(36, Short.MAX_VALUE))
    );

    jPanel6.add(jPanel13, new org.netbeans.lib.awtextra.AbsoluteConstraints(90, 500, 200, 160));

    jPanel15.setBackground(new java.awt.Color(153, 153, 153));

    jLabel39.setFont(new java.awt.Font("Serif", 1, 24)); // NOI18N
    jLabel39.setForeground(new java.awt.Color(102, 102, 102));
    jLabel39.setText("Total Bookings");

    jLabel48.setFont(new java.awt.Font("Serif", 0, 80)); // NOI18N
    jLabel48.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
    jLabel48.setText("0");

    javax.swing.GroupLayout jPanel15Layout = new javax.swing.GroupLayout(jPanel15);
    jPanel15.setLayout(jPanel15Layout);
    jPanel15Layout.setHorizontalGroup(
        jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(jPanel15Layout.createSequentialGroup()
            .addGroup(jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel15Layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(jLabel39))
                .addGroup(jPanel15Layout.createSequentialGroup()
                    .addGap(82, 82, 82)
                    .addComponent(jLabel48, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)))
            .addContainerGap(67, Short.MAX_VALUE))
    );
    jPanel15Layout.setVerticalGroup(
        jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(jPanel15Layout.createSequentialGroup()
            .addContainerGap()
            .addComponent(jLabel39)
            .addGap(18, 18, 18)
            .addComponent(jLabel48, javax.swing.GroupLayout.PREFERRED_SIZE, 68, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addContainerGap(36, Short.MAX_VALUE))
    );

    jPanel6.add(jPanel15, new org.netbeans.lib.awtextra.AbsoluteConstraints(310, 500, -1, -1));

    jPanel17.setBackground(new java.awt.Color(153, 153, 153));

    jLabel40.setFont(new java.awt.Font("Serif", 1, 24)); // NOI18N
    jLabel40.setForeground(new java.awt.Color(102, 102, 102));
    jLabel40.setText("Total Players");

    jLabel49.setFont(new java.awt.Font("Serif", 0, 80)); // NOI18N
    jLabel49.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
    jLabel49.setText("0");

    javax.swing.GroupLayout jPanel17Layout = new javax.swing.GroupLayout(jPanel17);
    jPanel17.setLayout(jPanel17Layout);
    jPanel17Layout.setHorizontalGroup(
        jPanel17Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(jPanel17Layout.createSequentialGroup()
            .addContainerGap()
            .addComponent(jLabel40)
            .addContainerGap(67, Short.MAX_VALUE))
        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel17Layout.createSequentialGroup()
            .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jLabel49, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(78, 78, 78))
    );
    jPanel17Layout.setVerticalGroup(
        jPanel17Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(jPanel17Layout.createSequentialGroup()
            .addContainerGap()
            .addComponent(jLabel40)
            .addGap(18, 18, 18)
            .addComponent(jLabel49, javax.swing.GroupLayout.PREFERRED_SIZE, 68, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addContainerGap(36, Short.MAX_VALUE))
    );

    jPanel6.add(jPanel17, new org.netbeans.lib.awtextra.AbsoluteConstraints(550, 500, -1, -1));

    jPanel24.setBackground(new java.awt.Color(153, 153, 153));

    jLabel41.setFont(new java.awt.Font("Serif", 1, 24)); // NOI18N
    jLabel41.setForeground(new java.awt.Color(102, 102, 102));
    jLabel41.setText("Total Matches");

    jLabel50.setFont(new java.awt.Font("Serif", 0, 80)); // NOI18N
    jLabel50.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
    jLabel50.setText("0");

    javax.swing.GroupLayout jPanel24Layout = new javax.swing.GroupLayout(jPanel24);
    jPanel24.setLayout(jPanel24Layout);
    jPanel24Layout.setHorizontalGroup(
        jPanel24Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(jPanel24Layout.createSequentialGroup()
            .addGroup(jPanel24Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel24Layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(jLabel41))
                .addGroup(jPanel24Layout.createSequentialGroup()
                    .addGap(78, 78, 78)
                    .addComponent(jLabel50, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)))
            .addContainerGap(67, Short.MAX_VALUE))
    );
    jPanel24Layout.setVerticalGroup(
        jPanel24Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(jPanel24Layout.createSequentialGroup()
            .addContainerGap()
            .addComponent(jLabel41)
            .addGap(18, 18, 18)
            .addComponent(jLabel50, javax.swing.GroupLayout.PREFERRED_SIZE, 68, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addContainerGap(36, Short.MAX_VALUE))
    );

    jPanel6.add(jPanel24, new org.netbeans.lib.awtextra.AbsoluteConstraints(770, 500, -1, -1));

    Main1.add(jPanel6, "card2");

    AdminDashboard.add(Main1, new org.netbeans.lib.awtextra.AbsoluteConstraints(240, 0, 1070, 720));

    jPanel1.add(AdminDashboard, "card3");
    AdminDashboard.getAccessibleContext().setAccessibleParent(AdminDashboard);

    TeamManagement.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

    Nav2.setBackground(new java.awt.Color(0, 102, 102));
    Nav2.setMinimumSize(new java.awt.Dimension(225, 720));
    Nav2.setLayout(new java.awt.CardLayout());

    jPanel19.setBackground(new java.awt.Color(0, 102, 102));
    jPanel19.setMinimumSize(new java.awt.Dimension(220, 720));

    Title2.setFont(new java.awt.Font("Serif", 0, 48)); // NOI18N
    Title2.setForeground(new java.awt.Color(0, 51, 51));
    Title2.setText("volta.");

    DashBoardBtn2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
    DashBoardBtn2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/View/UnclickedDashboard.png"))); // NOI18N

    ManageTeamBtn2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
    ManageTeamBtn2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/View/ClickedManageTeam.png"))); // NOI18N

    ManagePlayerBtn2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
    ManagePlayerBtn2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/View/UnclickedManagePlayer.png"))); // NOI18N

    BookingBtn2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
    BookingBtn2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/View/UnclickedBooking.png"))); // NOI18N

    ScheduleBtn2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
    ScheduleBtn2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/View/UnclickedSchedule.png"))); // NOI18N

    AnnouncementBtn2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
    AnnouncementBtn2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/View/UnclickedAnnouncement.png"))); // NOI18N

    javax.swing.GroupLayout jPanel19Layout = new javax.swing.GroupLayout(jPanel19);
    jPanel19.setLayout(jPanel19Layout);
    jPanel19Layout.setHorizontalGroup(
        jPanel19Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(jPanel19Layout.createSequentialGroup()
            .addGroup(jPanel19Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel19Layout.createSequentialGroup()
                    .addGap(118, 118, 118)
                    .addComponent(Title2, javax.swing.GroupLayout.PREFERRED_SIZE, 108, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGroup(jPanel19Layout.createSequentialGroup()
                    .addGap(6, 6, 6)
                    .addComponent(DashBoardBtn2, javax.swing.GroupLayout.PREFERRED_SIZE, 220, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGroup(jPanel19Layout.createSequentialGroup()
                    .addGap(6, 6, 6)
                    .addComponent(ManageTeamBtn2, javax.swing.GroupLayout.PREFERRED_SIZE, 220, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGroup(jPanel19Layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(ManagePlayerBtn2, javax.swing.GroupLayout.PREFERRED_SIZE, 216, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGroup(jPanel19Layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(BookingBtn2, javax.swing.GroupLayout.PREFERRED_SIZE, 218, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGroup(jPanel19Layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(ScheduleBtn2, javax.swing.GroupLayout.PREFERRED_SIZE, 218, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGroup(jPanel19Layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(AnnouncementBtn2, javax.swing.GroupLayout.PREFERRED_SIZE, 218, javax.swing.GroupLayout.PREFERRED_SIZE)))
            .addContainerGap(14, Short.MAX_VALUE))
    );
    jPanel19Layout.setVerticalGroup(
        jPanel19Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(jPanel19Layout.createSequentialGroup()
            .addGap(33, 33, 33)
            .addComponent(Title2, javax.swing.GroupLayout.PREFERRED_SIZE, 58, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(89, 89, 89)
            .addComponent(DashBoardBtn2)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(ManageTeamBtn2)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
            .addComponent(ManagePlayerBtn2)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
            .addComponent(BookingBtn2)
            .addGap(12, 12, 12)
            .addComponent(ScheduleBtn2, javax.swing.GroupLayout.PREFERRED_SIZE, 66, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
            .addComponent(AnnouncementBtn2, javax.swing.GroupLayout.PREFERRED_SIZE, 51, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addContainerGap())
    );

    // Code adding the component to the parent container - not shown here

    DashBoardBtn2.addMouseListener(new java.awt.event.MouseAdapter() {
        @Override
        public void mouseClicked(java.awt.event.MouseEvent e) {
            System.out.print("Button clicked");
            // Get the CardLayout from jPanel1
            CardLayout cardLayout = (CardLayout) jPanel1.getLayout();

            // Switch to the AdminDashboard card
            cardLayout.show(jPanel1, "card3");
        }

        @Override
        public void mouseEntered(java.awt.event.MouseEvent e) {
            DashBoardBtn2.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        }
    });
    ManageTeamBtn2.addMouseListener(new java.awt.event.MouseAdapter() {
        @Override
        public void mouseClicked(java.awt.event.MouseEvent e) {
            System.out.print("Button clicked");
            // Get the CardLayout from jPanel1
            CardLayout cardLayout = (CardLayout) jPanel1.getLayout();

            // Switch to the AdminDashboard card
            cardLayout.show(jPanel1, "card4");
        }

        @Override
        public void mouseEntered(java.awt.event.MouseEvent e) {
            ManageTeamBtn2.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        }
    });
    ManagePlayerBtn2.addMouseListener(new java.awt.event.MouseAdapter() {
        @Override
        public void mouseClicked(java.awt.event.MouseEvent e) {
            System.out.print("Button clicked");
            // Get the CardLayout from jPanel1
            CardLayout cardLayout = (CardLayout) jPanel1.getLayout();

            // Switch to the AdminDashboard card
            cardLayout.show(jPanel1, "card5");
        }

        @Override
        public void mouseEntered(java.awt.event.MouseEvent e) {
            ManagePlayerBtn2.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        }
    });
    BookingBtn2.addMouseListener(new java.awt.event.MouseAdapter() {
        @Override
        public void mouseClicked(java.awt.event.MouseEvent e) {
            System.out.print("Button clicked");
            // Get the CardLayout from jPanel1
            CardLayout cardLayout = (CardLayout) jPanel1.getLayout();

            // Switch to the AdminDashboard card
            cardLayout.show(jPanel1, "card6");
        }

        @Override
        public void mouseEntered(java.awt.event.MouseEvent e) {
            BookingBtn2.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        }
    });
    ScheduleBtn2.addMouseListener(new java.awt.event.MouseAdapter() {
        @Override
        public void mouseClicked(java.awt.event.MouseEvent e) {
            System.out.print("Button clicked");
            // Get the CardLayout from jPanel1
            CardLayout cardLayout = (CardLayout) jPanel1.getLayout();

            // Switch to the AdminDashboard card
            cardLayout.show(jPanel1, "card7");
        }

        @Override
        public void mouseEntered(java.awt.event.MouseEvent e) {
            ScheduleBtn2.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        }
    });
    AnnouncementBtn2.addMouseListener(new java.awt.event.MouseAdapter() {
        @Override
        public void mouseClicked(java.awt.event.MouseEvent e) {
            System.out.print("Button clicked");
            // Get the CardLayout from jPanel1
            CardLayout cardLayout = (CardLayout) jPanel1.getLayout();

            // Switch to the AdminDashboard card
            cardLayout.show(jPanel1, "card8");
        }

        @Override
        public void mouseEntered(java.awt.event.MouseEvent e) {
            AnnouncementBtn2.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        }
    });

    Nav2.add(jPanel19, "card2");

    TeamManagement.add(Nav2, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 240, 720));

    Main2.setLayout(new java.awt.CardLayout());

    jPanel10.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

    jLabel1.setFont(new java.awt.Font("Serif", 0, 50)); // NOI18N
    jLabel1.setForeground(new java.awt.Color(0, 153, 153));
    jLabel1.setText("Team Management");
    jLabel1.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);
    jPanel10.add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 100, 417, 81));

    jLabel7.setFont(new java.awt.Font("Sylfaen", 0, 18)); // NOI18N
    jLabel7.setText("Team Name");
    jPanel10.add(jLabel7, new org.netbeans.lib.awtextra.AbsoluteConstraints(70, 300, -1, -1));

    jLabel8.setFont(new java.awt.Font("Sylfaen", 0, 18)); // NOI18N
    jLabel8.setText("Team ID");
    jPanel10.add(jLabel8, new org.netbeans.lib.awtextra.AbsoluteConstraints(70, 240, -1, -1));

    jLabel9.setFont(new java.awt.Font("Sylfaen", 0, 18)); // NOI18N
    jLabel9.setText("No. Player");
    jPanel10.add(jLabel9, new org.netbeans.lib.awtextra.AbsoluteConstraints(70, 420, -1, -1));

    jLabel10.setFont(new java.awt.Font("Sylfaen", 0, 18)); // NOI18N
    jLabel10.setText("Manager");
    jPanel10.add(jLabel10, new org.netbeans.lib.awtextra.AbsoluteConstraints(70, 360, -1, -1));

    TMTeamNameTextField.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
    TMTeamNameTextField.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            TMTeamNameTextFieldActionPerformed(evt);
        }
    });
    jPanel10.add(TMTeamNameTextField, new org.netbeans.lib.awtextra.AbsoluteConstraints(190, 290, 252, 36));

    ManagerTextField.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
    ManagerTextField.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            ManagerTextFieldActionPerformed(evt);
        }
    });
    jPanel10.add(ManagerTextField, new org.netbeans.lib.awtextra.AbsoluteConstraints(190, 350, 252, 36));

    TeamIDTextField.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
    jPanel10.add(TeamIDTextField, new org.netbeans.lib.awtextra.AbsoluteConstraints(190, 230, 252, 36));

    NoPlayerTextField.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
    jPanel10.add(NoPlayerTextField, new org.netbeans.lib.awtextra.AbsoluteConstraints(190, 410, 252, 36));

    jLabel11.setFont(new java.awt.Font("Sylfaen", 0, 18)); // NOI18N
    jLabel11.setText("Search");
    jPanel10.add(jLabel11, new org.netbeans.lib.awtextra.AbsoluteConstraints(560, 240, -1, 39));

    TMSearchTextField.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
    TMSearchTextField.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            TMSearchTextFieldActionPerformed(evt);
        }
    });
    jPanel10.add(TMSearchTextField, new org.netbeans.lib.awtextra.AbsoluteConstraints(750, 240, 136, 39));

    jLabel12.setFont(new java.awt.Font("Sylfaen", 0, 18)); // NOI18N
    jLabel12.setText("Sort By");
    jPanel10.add(jLabel12, new org.netbeans.lib.awtextra.AbsoluteConstraints(560, 300, -1, 39));

    TeamTable.setModel(new javax.swing.table.DefaultTableModel(
        new Object [][] {
            {null, null, null, null},
            {null, null, null, null},
            {null, null, null, null},
            {null, null, null, null},
            {null, null, null, null},
            {null, null, null, null},
            {null, null, null, null},
            {null, null, null, null},
            {null, null, null, null},
            {null, null, null, null},
            {null, null, null, null},
            {null, null, null, null},
            {null, null, null, null},
            {null, null, null, null},
            {null, null, null, null},
            {null, null, null, null},
            {null, null, null, null}
        },
        new String [] {
            "Team ID", "Team Name", "Manager", "No. Player"
        }
    ));
    jScrollPane1.setViewportView(TeamTable);

    jPanel10.add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(550, 400, 420, 200));

    TMAddBtn.setText("Add");
    jPanel10.add(TMAddBtn, new org.netbeans.lib.awtextra.AbsoluteConstraints(110, 490, 127, 41));

    TMUpdateBtn.setText("Update");
    jPanel10.add(TMUpdateBtn, new org.netbeans.lib.awtextra.AbsoluteConstraints(300, 490, 127, 41));

    TMDeleteBtn.setText("Delete");
    jPanel10.add(TMDeleteBtn, new org.netbeans.lib.awtextra.AbsoluteConstraints(110, 560, 127, 41));

    TMReadBtn.setText("Read");
    jPanel10.add(TMReadBtn, new org.netbeans.lib.awtextra.AbsoluteConstraints(300, 560, 127, 41));

    TMSortByComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Team ID", "No. Player" }));
    jPanel10.add(TMSortByComboBox, new org.netbeans.lib.awtextra.AbsoluteConstraints(640, 300, 100, 40));

    TMSearchComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Team ID", "Team Name", "Manager", "No. Player" }));
    jPanel10.add(TMSearchComboBox, new org.netbeans.lib.awtextra.AbsoluteConstraints(640, 240, -1, 37));

    jLabel32.setIcon(new javax.swing.ImageIcon(getClass().getResource("/View/search.png"))); // NOI18N
    jPanel10.add(jLabel32, new org.netbeans.lib.awtextra.AbsoluteConstraints(900, 240, -1, -1));

    jLabel30.setIcon(new javax.swing.ImageIcon(getClass().getResource("/View/bg2.png"))); // NOI18N
    jPanel10.add(jLabel30, new org.netbeans.lib.awtextra.AbsoluteConstraints(530, 200, -1, -1));

    TMSearchBtn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/View/bg2.png"))); // NOI18N
    jPanel10.add(TMSearchBtn, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 200, -1, -1));

    jLabel53.setIcon(new javax.swing.ImageIcon(getClass().getResource("/View/DashBoardBg.jpg"))); // NOI18N
    jPanel10.add(jLabel53, new org.netbeans.lib.awtextra.AbsoluteConstraints(-10, 0, 1080, 180));

    Main2.add(jPanel10, "card2");

    TeamManagement.add(Main2, new org.netbeans.lib.awtextra.AbsoluteConstraints(240, 0, 1070, 720));

    jPanel1.add(TeamManagement, "card4");

    PlayerManagement.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

    Nav3.setBackground(new java.awt.Color(0, 102, 102));
    Nav3.setMinimumSize(new java.awt.Dimension(225, 720));
    Nav3.setLayout(new java.awt.CardLayout());

    jPanel7.setBackground(new java.awt.Color(0, 102, 102));
    jPanel7.setMinimumSize(new java.awt.Dimension(220, 720));

    Title3.setFont(new java.awt.Font("Serif", 0, 48)); // NOI18N
    Title3.setForeground(new java.awt.Color(0, 51, 51));
    Title3.setText("volta.");

    DashBoardBtn3.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
    DashBoardBtn3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/View/UnclickedDashboard.png"))); // NOI18N

    ManageTeamBtn3.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
    ManageTeamBtn3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/View/UnclickedManageTeam.png"))); // NOI18N

    ManagePlayerBtn3.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
    ManagePlayerBtn3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/View/ClickedManagePlayer.png"))); // NOI18N

    BookingBtn3.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
    BookingBtn3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/View/UnclickedBooking.png"))); // NOI18N

    ScheduleBtn3.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
    ScheduleBtn3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/View/UnclickedSchedule.png"))); // NOI18N

    AnnouncementBtn3.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
    AnnouncementBtn3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/View/UnclickedAnnouncement.png"))); // NOI18N

    javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
    jPanel7.setLayout(jPanel7Layout);
    jPanel7Layout.setHorizontalGroup(
        jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(jPanel7Layout.createSequentialGroup()
            .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel7Layout.createSequentialGroup()
                    .addGap(118, 118, 118)
                    .addComponent(Title3, javax.swing.GroupLayout.PREFERRED_SIZE, 108, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGroup(jPanel7Layout.createSequentialGroup()
                    .addGap(6, 6, 6)
                    .addComponent(DashBoardBtn3, javax.swing.GroupLayout.PREFERRED_SIZE, 220, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGroup(jPanel7Layout.createSequentialGroup()
                    .addGap(6, 6, 6)
                    .addComponent(ManageTeamBtn3, javax.swing.GroupLayout.PREFERRED_SIZE, 220, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGroup(jPanel7Layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(ManagePlayerBtn3, javax.swing.GroupLayout.PREFERRED_SIZE, 216, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGroup(jPanel7Layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(BookingBtn3, javax.swing.GroupLayout.PREFERRED_SIZE, 218, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGroup(jPanel7Layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(ScheduleBtn3, javax.swing.GroupLayout.PREFERRED_SIZE, 218, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGroup(jPanel7Layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(AnnouncementBtn3, javax.swing.GroupLayout.PREFERRED_SIZE, 218, javax.swing.GroupLayout.PREFERRED_SIZE)))
            .addContainerGap(14, Short.MAX_VALUE))
    );
    jPanel7Layout.setVerticalGroup(
        jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(jPanel7Layout.createSequentialGroup()
            .addGap(33, 33, 33)
            .addComponent(Title3, javax.swing.GroupLayout.PREFERRED_SIZE, 58, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(89, 89, 89)
            .addComponent(DashBoardBtn3)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(ManageTeamBtn3)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
            .addComponent(ManagePlayerBtn3)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
            .addComponent(BookingBtn3)
            .addGap(12, 12, 12)
            .addComponent(ScheduleBtn3, javax.swing.GroupLayout.PREFERRED_SIZE, 66, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
            .addComponent(AnnouncementBtn3, javax.swing.GroupLayout.PREFERRED_SIZE, 51, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addContainerGap())
    );

    // Code adding the component to the parent container - not shown here

    DashBoardBtn3.addMouseListener(new java.awt.event.MouseAdapter() {
        @Override
        public void mouseClicked(java.awt.event.MouseEvent e) {
            System.out.print("Button clicked");
            // Get the CardLayout from jPanel1
            CardLayout cardLayout = (CardLayout) jPanel1.getLayout();

            // Switch to the AdminDashboard card
            cardLayout.show(jPanel1, "card3");
        }

        @Override
        public void mouseEntered(java.awt.event.MouseEvent e) {
            DashBoardBtn3.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        }
    });
    ManageTeamBtn3.addMouseListener(new java.awt.event.MouseAdapter() {
        @Override
        public void mouseClicked(java.awt.event.MouseEvent e) {
            System.out.print("Button clicked");
            // Get the CardLayout from jPanel1
            CardLayout cardLayout = (CardLayout) jPanel1.getLayout();

            // Switch to the AdminDashboard card
            cardLayout.show(jPanel1, "card4");
        }

        @Override
        public void mouseEntered(java.awt.event.MouseEvent e) {
            ManageTeamBtn3.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        }
    });
    ManagePlayerBtn3.addMouseListener(new java.awt.event.MouseAdapter() {
        @Override
        public void mouseClicked(java.awt.event.MouseEvent e) {
            System.out.print("Button clicked");
            // Get the CardLayout from jPanel1
            CardLayout cardLayout = (CardLayout) jPanel1.getLayout();

            // Switch to the AdminDashboard card
            cardLayout.show(jPanel1, "card5");
        }

        @Override
        public void mouseEntered(java.awt.event.MouseEvent e) {
            ManagePlayerBtn3.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        }
    });
    BookingBtn3.addMouseListener(new java.awt.event.MouseAdapter() {
        @Override
        public void mouseClicked(java.awt.event.MouseEvent e) {
            System.out.print("Button clicked");
            // Get the CardLayout from jPanel1
            CardLayout cardLayout = (CardLayout) jPanel1.getLayout();

            // Switch to the AdminDashboard card
            cardLayout.show(jPanel1, "card6");
        }

        @Override
        public void mouseEntered(java.awt.event.MouseEvent e) {
            BookingBtn3.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        }
    });
    ScheduleBtn3.addMouseListener(new java.awt.event.MouseAdapter() {
        @Override
        public void mouseClicked(java.awt.event.MouseEvent e) {
            System.out.print("Button clicked");
            // Get the CardLayout from jPanel1
            CardLayout cardLayout = (CardLayout) jPanel1.getLayout();

            // Switch to the AdminDashboard card
            cardLayout.show(jPanel1, "card7");
        }

        @Override
        public void mouseEntered(java.awt.event.MouseEvent e) {
            ScheduleBtn3.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        }
    });
    AnnouncementBtn3.addMouseListener(new java.awt.event.MouseAdapter() {
        @Override
        public void mouseClicked(java.awt.event.MouseEvent e) {
            System.out.print("Button clicked");
            // Get the CardLayout from jPanel1
            CardLayout cardLayout = (CardLayout) jPanel1.getLayout();

            // Switch to the AdminDashboard card
            cardLayout.show(jPanel1, "card8");
        }

        @Override
        public void mouseEntered(java.awt.event.MouseEvent e) {
            AnnouncementBtn3.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        }
    });

    Nav3.add(jPanel7, "card2");

    PlayerManagement.add(Nav3, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 240, 720));

    Main3.setLayout(new java.awt.CardLayout());

    jPanel9.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

    jLabel13.setFont(new java.awt.Font("Serif", 0, 50)); // NOI18N
    jLabel13.setForeground(new java.awt.Color(0, 153, 153));
    jLabel13.setText("Player Management");
    jLabel13.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);
    jPanel9.add(jLabel13, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 90, 417, 81));

    jLabel14.setFont(new java.awt.Font("Sylfaen", 0, 18)); // NOI18N
    jLabel14.setText("Player ID");
    jPanel9.add(jLabel14, new org.netbeans.lib.awtextra.AbsoluteConstraints(70, 220, -1, -1));

    PlayerNameTextField.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
    jPanel9.add(PlayerNameTextField, new org.netbeans.lib.awtextra.AbsoluteConstraints(210, 270, 252, 36));

    jLabel15.setFont(new java.awt.Font("Sylfaen", 0, 18)); // NOI18N
    jLabel15.setText("Team Name");
    jPanel9.add(jLabel15, new org.netbeans.lib.awtextra.AbsoluteConstraints(70, 340, -1, -1));

    PlayerIDTextField.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
    jPanel9.add(PlayerIDTextField, new org.netbeans.lib.awtextra.AbsoluteConstraints(210, 210, 252, 36));

    jLabel16.setFont(new java.awt.Font("Sylfaen", 0, 18)); // NOI18N
    jLabel16.setText("Age");
    jPanel9.add(jLabel16, new org.netbeans.lib.awtextra.AbsoluteConstraints(70, 400, -1, -1));

    PlayerAgeTextField.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
    jPanel9.add(PlayerAgeTextField, new org.netbeans.lib.awtextra.AbsoluteConstraints(210, 390, 86, 36));

    jLabel18.setFont(new java.awt.Font("Sylfaen", 0, 18)); // NOI18N
    jLabel18.setText("Jersey No");
    jPanel9.add(jLabel18, new org.netbeans.lib.awtextra.AbsoluteConstraints(70, 460, -1, -1));

    PlayerJerseyNoTextField.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
    jPanel9.add(PlayerJerseyNoTextField, new org.netbeans.lib.awtextra.AbsoluteConstraints(210, 450, 86, 36));

    jLabel19.setFont(new java.awt.Font("Sylfaen", 0, 18)); // NOI18N
    jLabel19.setText("Position");
    jPanel9.add(jLabel19, new org.netbeans.lib.awtextra.AbsoluteConstraints(70, 510, -1, -1));

    PlayerPositionTextField.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
    jPanel9.add(PlayerPositionTextField, new org.netbeans.lib.awtextra.AbsoluteConstraints(210, 500, 86, 36));

    jLabel20.setFont(new java.awt.Font("Sylfaen", 0, 18)); // NOI18N
    jLabel20.setText("Search");
    jPanel9.add(jLabel20, new org.netbeans.lib.awtextra.AbsoluteConstraints(580, 220, -1, -1));

    jLabel21.setFont(new java.awt.Font("Sylfaen", 0, 18)); // NOI18N
    jLabel21.setText("Sort By");
    jPanel9.add(jLabel21, new org.netbeans.lib.awtextra.AbsoluteConstraints(580, 280, -1, -1));

    PMSearchTextField.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
    jPanel9.add(PMSearchTextField, new org.netbeans.lib.awtextra.AbsoluteConstraints(770, 210, 151, 36));

    PMSortByComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Player ID", "Age", "Jersey" }));
    PMSortByComboBox.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            PMSortByComboBoxActionPerformed(evt);
        }
    });
    jPanel9.add(PMSortByComboBox, new org.netbeans.lib.awtextra.AbsoluteConstraints(660, 270, 92, 40));
    jPanel9.add(jScrollBar1, new org.netbeans.lib.awtextra.AbsoluteConstraints(1060, 0, -1, 714));

    PlayerTable.setModel(new javax.swing.table.DefaultTableModel(
        new Object [][] {
            {null, null, null, null, null, null, null},
            {null, null, null, null, null, null, null},
            {null, null, null, null, null, null, null},
            {null, null, null, null, null, null, null},
            {null, null, null, null, null, null, null},
            {null, null, null, null, null, null, null},
            {null, null, null, null, null, null, null},
            {null, null, null, null, null, null, null},
            {null, null, null, null, null, null, null},
            {null, null, null, null, null, null, null},
            {null, null, null, null, null, null, null},
            {null, null, null, null, null, null, null},
            {null, null, null, null, null, null, null}
        },
        new String [] {
            "Player ID", "Player Name", "Age", "Jersey", "Position", "Team", "Photo"
        }
    ) {
        Class[] types = new Class [] {
            java.lang.Integer.class, java.lang.String.class, java.lang.Integer.class, java.lang.Integer.class, java.lang.String.class, java.lang.String.class, java.lang.Object.class
        };

        public Class getColumnClass(int columnIndex) {
            return types [columnIndex];
        }
    });
    jScrollPane2.setViewportView(PlayerTable);

    jPanel9.add(jScrollPane2, new org.netbeans.lib.awtextra.AbsoluteConstraints(570, 370, 400, 240));

    jLabel22.setFont(new java.awt.Font("Sylfaen", 0, 18)); // NOI18N
    jLabel22.setText("Player Name");
    jPanel9.add(jLabel22, new org.netbeans.lib.awtextra.AbsoluteConstraints(70, 280, -1, -1));

    PMTeamNameTextField.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
    jPanel9.add(PMTeamNameTextField, new org.netbeans.lib.awtextra.AbsoluteConstraints(210, 330, 252, 36));

    PMAddBtn.setText("Add");
    jPanel9.add(PMAddBtn, new org.netbeans.lib.awtextra.AbsoluteConstraints(120, 560, 127, 41));

    PMUpdateBtn.setText("Update");
    PMUpdateBtn.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            PMUpdateBtnActionPerformed(evt);
        }
    });
    jPanel9.add(PMUpdateBtn, new org.netbeans.lib.awtextra.AbsoluteConstraints(290, 560, 127, 41));

    PMDeleteBtn.setText("Delete");
    jPanel9.add(PMDeleteBtn, new org.netbeans.lib.awtextra.AbsoluteConstraints(120, 620, 127, 41));

    PMReadBtn.setText("Read");
    jPanel9.add(PMReadBtn, new org.netbeans.lib.awtextra.AbsoluteConstraints(290, 620, 127, 41));

    PMSearchComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Player ID", "Player Name", "Age", "Jersey", "Position", "Team" }));
    PMSearchComboBox.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            PMSearchComboBoxActionPerformed(evt);
        }
    });
    jPanel9.add(PMSearchComboBox, new org.netbeans.lib.awtextra.AbsoluteConstraints(660, 210, 92, 39));

    PMSearchBtn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/View/search.png"))); // NOI18N
    jPanel9.add(PMSearchBtn, new org.netbeans.lib.awtextra.AbsoluteConstraints(930, 210, -1, -1));

    jLabel31.setIcon(new javax.swing.ImageIcon(getClass().getResource("/View/bg1.png"))); // NOI18N
    jPanel9.add(jLabel31, new org.netbeans.lib.awtextra.AbsoluteConstraints(540, 190, -1, -1));

    UploadPhoto.setText("Upload");
    UploadPhoto.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            UploadPhotoActionPerformed(evt);
        }
    });
    jPanel9.add(UploadPhoto, new org.netbeans.lib.awtextra.AbsoluteConstraints(350, 500, -1, -1));

    jLabel34.setText("Photo");
    jPanel9.add(jLabel34, new org.netbeans.lib.awtextra.AbsoluteConstraints(350, 470, -1, -1));

    jLabel33.setIcon(new javax.swing.ImageIcon(getClass().getResource("/View/bg1.png"))); // NOI18N
    jPanel9.add(jLabel33, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 190, -1, -1));

    jLabel54.setIcon(new javax.swing.ImageIcon(getClass().getResource("/View/DashBoardBg.jpg"))); // NOI18N
    jPanel9.add(jLabel54, new org.netbeans.lib.awtextra.AbsoluteConstraints(-10, 0, 1080, 180));

    Main3.add(jPanel9, "card2");

    PlayerManagement.add(Main3, new org.netbeans.lib.awtextra.AbsoluteConstraints(240, 0, 1070, 720));

    jPanel1.add(PlayerManagement, "card5");

    BookingManagement.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

    Nav4.setBackground(new java.awt.Color(0, 102, 102));
    Nav4.setMinimumSize(new java.awt.Dimension(225, 720));
    Nav4.setLayout(new java.awt.CardLayout());

    jPanel20.setBackground(new java.awt.Color(0, 102, 102));
    jPanel20.setMinimumSize(new java.awt.Dimension(220, 720));

    Title4.setFont(new java.awt.Font("Serif", 0, 48)); // NOI18N
    Title4.setForeground(new java.awt.Color(0, 51, 51));
    Title4.setText("volta.");

    DashBoardBtn4.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
    DashBoardBtn4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/View/UnclickedDashboard.png"))); // NOI18N

    ManageTeamBtn4.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
    ManageTeamBtn4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/View/UnclickedManageTeam.png"))); // NOI18N

    ManagePlayerBtn4.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
    ManagePlayerBtn4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/View/UnclickedManagePlayer.png"))); // NOI18N

    BookingBtn4.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
    BookingBtn4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/View/ClickedBooking.png"))); // NOI18N

    ScheduleBtn4.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
    ScheduleBtn4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/View/UnclickedSchedule.png"))); // NOI18N

    AnnouncementBtn4.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
    AnnouncementBtn4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/View/UnclickedAnnouncement.png"))); // NOI18N

    javax.swing.GroupLayout jPanel20Layout = new javax.swing.GroupLayout(jPanel20);
    jPanel20.setLayout(jPanel20Layout);
    jPanel20Layout.setHorizontalGroup(
        jPanel20Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(jPanel20Layout.createSequentialGroup()
            .addGroup(jPanel20Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel20Layout.createSequentialGroup()
                    .addGap(118, 118, 118)
                    .addComponent(Title4, javax.swing.GroupLayout.PREFERRED_SIZE, 108, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGroup(jPanel20Layout.createSequentialGroup()
                    .addGap(6, 6, 6)
                    .addComponent(DashBoardBtn4, javax.swing.GroupLayout.PREFERRED_SIZE, 220, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGroup(jPanel20Layout.createSequentialGroup()
                    .addGap(6, 6, 6)
                    .addComponent(ManageTeamBtn4, javax.swing.GroupLayout.PREFERRED_SIZE, 220, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGroup(jPanel20Layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(ManagePlayerBtn4, javax.swing.GroupLayout.PREFERRED_SIZE, 216, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGroup(jPanel20Layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(BookingBtn4, javax.swing.GroupLayout.PREFERRED_SIZE, 218, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGroup(jPanel20Layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(ScheduleBtn4, javax.swing.GroupLayout.PREFERRED_SIZE, 218, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGroup(jPanel20Layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(AnnouncementBtn4, javax.swing.GroupLayout.PREFERRED_SIZE, 218, javax.swing.GroupLayout.PREFERRED_SIZE)))
            .addContainerGap(14, Short.MAX_VALUE))
    );
    jPanel20Layout.setVerticalGroup(
        jPanel20Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(jPanel20Layout.createSequentialGroup()
            .addGap(33, 33, 33)
            .addComponent(Title4, javax.swing.GroupLayout.PREFERRED_SIZE, 58, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(89, 89, 89)
            .addComponent(DashBoardBtn4)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(ManageTeamBtn4)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
            .addComponent(ManagePlayerBtn4)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
            .addComponent(BookingBtn4)
            .addGap(12, 12, 12)
            .addComponent(ScheduleBtn4, javax.swing.GroupLayout.PREFERRED_SIZE, 66, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
            .addComponent(AnnouncementBtn4, javax.swing.GroupLayout.PREFERRED_SIZE, 51, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addContainerGap())
    );

    // Code adding the component to the parent container - not shown here

    DashBoardBtn4.addMouseListener(new java.awt.event.MouseAdapter() {
        @Override
        public void mouseClicked(java.awt.event.MouseEvent e) {
            System.out.print("Button clicked");
            // Get the CardLayout from jPanel1
            CardLayout cardLayout = (CardLayout) jPanel1.getLayout();

            // Switch to the AdminDashboard card
            cardLayout.show(jPanel1, "card3");
        }

        @Override
        public void mouseEntered(java.awt.event.MouseEvent e) {
            DashBoardBtn4.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        }
    });
    ManageTeamBtn4.addMouseListener(new java.awt.event.MouseAdapter() {
        @Override
        public void mouseClicked(java.awt.event.MouseEvent e) {
            System.out.print("Button clicked");
            // Get the CardLayout from jPanel1
            CardLayout cardLayout = (CardLayout) jPanel1.getLayout();

            // Switch to the AdminDashboard card
            cardLayout.show(jPanel1, "card4");
        }

        @Override
        public void mouseEntered(java.awt.event.MouseEvent e) {
            ManageTeamBtn4.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        }
    });
    ManagePlayerBtn4.addMouseListener(new java.awt.event.MouseAdapter() {
        @Override
        public void mouseClicked(java.awt.event.MouseEvent e) {
            System.out.print("Button clicked");
            // Get the CardLayout from jPanel1
            CardLayout cardLayout = (CardLayout) jPanel1.getLayout();

            // Switch to the AdminDashboard card
            cardLayout.show(jPanel1, "card5");
        }

        @Override
        public void mouseEntered(java.awt.event.MouseEvent e) {
            ManagePlayerBtn4.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        }
    });
    BookingBtn4.addMouseListener(new java.awt.event.MouseAdapter() {
        @Override
        public void mouseClicked(java.awt.event.MouseEvent e) {
            System.out.print("Button clicked");
            // Get the CardLayout from jPanel1
            CardLayout cardLayout = (CardLayout) jPanel1.getLayout();

            // Switch to the AdminDashboard card
            cardLayout.show(jPanel1, "card6");
        }

        @Override
        public void mouseEntered(java.awt.event.MouseEvent e) {
            BookingBtn4.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        }
    });
    ScheduleBtn4.addMouseListener(new java.awt.event.MouseAdapter() {
        @Override
        public void mouseClicked(java.awt.event.MouseEvent e) {
            System.out.print("Button clicked");
            // Get the CardLayout from jPanel1
            CardLayout cardLayout = (CardLayout) jPanel1.getLayout();

            // Switch to the AdminDashboard card
            cardLayout.show(jPanel1, "card7");
        }

        @Override
        public void mouseEntered(java.awt.event.MouseEvent e) {
            ScheduleBtn4.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        }
    });
    AnnouncementBtn4.addMouseListener(new java.awt.event.MouseAdapter() {
        @Override
        public void mouseClicked(java.awt.event.MouseEvent e) {
            System.out.print("Button clicked");
            // Get the CardLayout from jPanel1
            CardLayout cardLayout = (CardLayout) jPanel1.getLayout();

            // Switch to the AdminDashboard card
            cardLayout.show(jPanel1, "card8");
        }

        @Override
        public void mouseEntered(java.awt.event.MouseEvent e) {
            AnnouncementBtn4.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        }
    });

    Nav4.add(jPanel20, "card2");

    BookingManagement.add(Nav4, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 240, 720));

    Main4.setLayout(new java.awt.CardLayout());

    jPanel12.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

    jLabel36.setFont(new java.awt.Font("Serif", 0, 50)); // NOI18N
    jLabel36.setForeground(new java.awt.Color(0, 153, 153));
    jLabel36.setText("Booking ");
    jPanel12.add(jLabel36, new org.netbeans.lib.awtextra.AbsoluteConstraints(80, 110, -1, -1));

    BookingTable.setModel(new javax.swing.table.DefaultTableModel(
        new Object [][] {
            {null, null, null, null, null, null, null, null, null},
            {null, null, null, null, null, null, null, null, null},
            {null, null, null, null, null, null, null, null, null},
            {null, null, null, null, null, null, null, null, null}
        },
        new String [] {
            "Booking ID", "Name", "Date", "Time", "Ground no", "Team 1", "Team 2", "Payment Status", "Action"
        }
    ));
    jScrollPane4.setViewportView(BookingTable);

    jPanel12.add(jScrollPane4, new org.netbeans.lib.awtextra.AbsoluteConstraints(120, 260, 904, 380));

    jLabel55.setIcon(new javax.swing.ImageIcon(getClass().getResource("/View/DashBoardBg.jpg"))); // NOI18N
    jPanel12.add(jLabel55, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 0, 1080, 180));

    jButton1.setText("Undo");
    jPanel12.add(jButton1, new org.netbeans.lib.awtextra.AbsoluteConstraints(940, 230, -1, -1));

    Main4.add(jPanel12, "card2");

    BookingManagement.add(Main4, new org.netbeans.lib.awtextra.AbsoluteConstraints(200, 0, 1110, 720));

    jPanel1.add(BookingManagement, "card6");

    ScheduleManagement.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

    Nav5.setBackground(new java.awt.Color(0, 102, 102));
    Nav5.setMinimumSize(new java.awt.Dimension(225, 720));
    Nav5.setLayout(new java.awt.CardLayout());

    jPanel8.setBackground(new java.awt.Color(0, 102, 102));
    jPanel8.setMinimumSize(new java.awt.Dimension(220, 720));

    Title5.setFont(new java.awt.Font("Serif", 0, 48)); // NOI18N
    Title5.setForeground(new java.awt.Color(0, 51, 51));
    Title5.setText("volta.");

    DashBoardBtn5.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
    DashBoardBtn5.setIcon(new javax.swing.ImageIcon(getClass().getResource("/View/UnclickedDashboard.png"))); // NOI18N

    ManageTeamBtn5.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
    ManageTeamBtn5.setIcon(new javax.swing.ImageIcon(getClass().getResource("/View/UnclickedManageTeam.png"))); // NOI18N

    ManagePlayerBtn5.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
    ManagePlayerBtn5.setIcon(new javax.swing.ImageIcon(getClass().getResource("/View/UnclickedManagePlayer.png"))); // NOI18N

    BookingBtn5.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
    BookingBtn5.setIcon(new javax.swing.ImageIcon(getClass().getResource("/View/UnclickedBooking.png"))); // NOI18N

    ScheduleBtn5.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
    ScheduleBtn5.setIcon(new javax.swing.ImageIcon(getClass().getResource("/View/ClickedSchedule.png"))); // NOI18N

    AnnouncementBtn5.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
    AnnouncementBtn5.setIcon(new javax.swing.ImageIcon(getClass().getResource("/View/UnclickedAnnouncement.png"))); // NOI18N

    javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
    jPanel8.setLayout(jPanel8Layout);
    jPanel8Layout.setHorizontalGroup(
        jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(jPanel8Layout.createSequentialGroup()
            .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel8Layout.createSequentialGroup()
                    .addGap(118, 118, 118)
                    .addComponent(Title5, javax.swing.GroupLayout.PREFERRED_SIZE, 108, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGroup(jPanel8Layout.createSequentialGroup()
                    .addGap(6, 6, 6)
                    .addComponent(DashBoardBtn5, javax.swing.GroupLayout.PREFERRED_SIZE, 220, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGroup(jPanel8Layout.createSequentialGroup()
                    .addGap(6, 6, 6)
                    .addComponent(ManageTeamBtn5, javax.swing.GroupLayout.PREFERRED_SIZE, 220, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGroup(jPanel8Layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(ManagePlayerBtn5, javax.swing.GroupLayout.PREFERRED_SIZE, 216, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGroup(jPanel8Layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(BookingBtn5, javax.swing.GroupLayout.PREFERRED_SIZE, 218, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGroup(jPanel8Layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(ScheduleBtn5, javax.swing.GroupLayout.PREFERRED_SIZE, 218, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGroup(jPanel8Layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(AnnouncementBtn5, javax.swing.GroupLayout.PREFERRED_SIZE, 218, javax.swing.GroupLayout.PREFERRED_SIZE)))
            .addContainerGap(14, Short.MAX_VALUE))
    );
    jPanel8Layout.setVerticalGroup(
        jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(jPanel8Layout.createSequentialGroup()
            .addGap(33, 33, 33)
            .addComponent(Title5, javax.swing.GroupLayout.PREFERRED_SIZE, 58, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(89, 89, 89)
            .addComponent(DashBoardBtn5)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(ManageTeamBtn5)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
            .addComponent(ManagePlayerBtn5)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
            .addComponent(BookingBtn5)
            .addGap(12, 12, 12)
            .addComponent(ScheduleBtn5, javax.swing.GroupLayout.PREFERRED_SIZE, 66, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
            .addComponent(AnnouncementBtn5, javax.swing.GroupLayout.PREFERRED_SIZE, 51, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addContainerGap())
    );

    // Code adding the component to the parent container - not shown here

    DashBoardBtn5.addMouseListener(new java.awt.event.MouseAdapter() {
        @Override
        public void mouseClicked(java.awt.event.MouseEvent e) {
            System.out.print("Button clicked");
            // Get the CardLayout from jPanel1
            CardLayout cardLayout = (CardLayout) jPanel1.getLayout();

            // Switch to the AdminDashboard card
            cardLayout.show(jPanel1, "card3");
        }

        @Override
        public void mouseEntered(java.awt.event.MouseEvent e) {
            DashBoardBtn5.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        }
    });
    ManageTeamBtn5.addMouseListener(new java.awt.event.MouseAdapter() {
        @Override
        public void mouseClicked(java.awt.event.MouseEvent e) {
            System.out.print("Button clicked");
            // Get the CardLayout from jPanel1
            CardLayout cardLayout = (CardLayout) jPanel1.getLayout();

            // Switch to the AdminDashboard card
            cardLayout.show(jPanel1, "card4");
        }

        @Override
        public void mouseEntered(java.awt.event.MouseEvent e) {
            ManageTeamBtn5.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        }
    });
    ManagePlayerBtn5.addMouseListener(new java.awt.event.MouseAdapter() {
        @Override
        public void mouseClicked(java.awt.event.MouseEvent e) {
            System.out.print("Button clicked");
            // Get the CardLayout from jPanel1
            CardLayout cardLayout = (CardLayout) jPanel1.getLayout();

            // Switch to the AdminDashboard card
            cardLayout.show(jPanel1, "card5");
        }

        @Override
        public void mouseEntered(java.awt.event.MouseEvent e) {
            ManagePlayerBtn5.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        }
    });
    BookingBtn5.addMouseListener(new java.awt.event.MouseAdapter() {
        @Override
        public void mouseClicked(java.awt.event.MouseEvent e) {
            System.out.print("Button clicked");
            // Get the CardLayout from jPanel1
            CardLayout cardLayout = (CardLayout) jPanel1.getLayout();

            // Switch to the AdminDashboard card
            cardLayout.show(jPanel1, "card6");
        }

        @Override
        public void mouseEntered(java.awt.event.MouseEvent e) {
            BookingBtn5.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        }
    });
    ScheduleBtn5.addMouseListener(new java.awt.event.MouseAdapter() {
        @Override
        public void mouseClicked(java.awt.event.MouseEvent e) {
            System.out.print("Button clicked");
            // Get the CardLayout from jPanel1
            CardLayout cardLayout = (CardLayout) jPanel1.getLayout();

            // Switch to the AdminDashboard card
            cardLayout.show(jPanel1, "card7");
        }

        @Override
        public void mouseEntered(java.awt.event.MouseEvent e) {
            ScheduleBtn5.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        }
    });
    AnnouncementBtn5.addMouseListener(new java.awt.event.MouseAdapter() {
        @Override
        public void mouseClicked(java.awt.event.MouseEvent e) {
            System.out.print("Button clicked");
            // Get the CardLayout from jPanel1
            CardLayout cardLayout = (CardLayout) jPanel1.getLayout();

            // Switch to the AdminDashboard card
            cardLayout.show(jPanel1, "card8");
        }

        @Override
        public void mouseEntered(java.awt.event.MouseEvent e) {
            AnnouncementBtn5.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        }
    });

    Nav5.add(jPanel8, "card2");

    ScheduleManagement.add(Nav5, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 240, 720));

    Main5.setLayout(new java.awt.CardLayout());

    jPanel14.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

    dateChooserPanel1.setCurrentView(new datechooser.view.appearance.AppearancesList("Grey",
        new datechooser.view.appearance.ViewAppearance("custom",
            new datechooser.view.appearance.swing.SwingCellAppearance(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 12),
                new java.awt.Color(0, 0, 0),
                new java.awt.Color(0, 0, 255),
                false,
                true,
                new datechooser.view.appearance.swing.ButtonPainter()),
            new datechooser.view.appearance.swing.SwingCellAppearance(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 12),
                new java.awt.Color(0, 0, 0),
                new java.awt.Color(0, 0, 255),
                true,
                true,
                new datechooser.view.appearance.swing.ButtonPainter()),
            new datechooser.view.appearance.swing.SwingCellAppearance(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 12),
                new java.awt.Color(0, 0, 255),
                new java.awt.Color(0, 0, 255),
                false,
                true,
                new datechooser.view.appearance.swing.ButtonPainter()),
            new datechooser.view.appearance.swing.SwingCellAppearance(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 12),
                new java.awt.Color(128, 128, 128),
                new java.awt.Color(0, 0, 255),
                false,
                true,
                new datechooser.view.appearance.swing.LabelPainter()),
            new datechooser.view.appearance.swing.SwingCellAppearance(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 12),
                new java.awt.Color(0, 0, 0),
                new java.awt.Color(0, 0, 255),
                false,
                true,
                new datechooser.view.appearance.swing.LabelPainter()),
            new datechooser.view.appearance.swing.SwingCellAppearance(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 12),
                new java.awt.Color(0, 0, 0),
                new java.awt.Color(255, 0, 0),
                false,
                false,
                new datechooser.view.appearance.swing.ButtonPainter()),
            (datechooser.view.BackRenderer)null,
            false,
            true)));
dateChooserPanel1.setCalendarBackground(new java.awt.Color(0, 0, 0));
dateChooserPanel1.addSelectionChangedListener(new datechooser.events.SelectionChangedListener() {
    public void onSelectionChange(datechooser.events.SelectionChangedEvent evt) {
        dateChooserPanel1OnSelectionChange(evt);
    }
    });
    dateChooserPanel1.addCommitListener(new datechooser.events.CommitListener() {
        public void onCommit(datechooser.events.CommitEvent evt) {
            dateChooserPanel1OnCommit(evt);
        }
    });
    jPanel14.add(dateChooserPanel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(80, 200, 740, 450));

    jLabel52.setFont(new java.awt.Font("Serif", 0, 50)); // NOI18N
    jLabel52.setForeground(new java.awt.Color(0, 153, 153));
    jLabel52.setText("Schedule");
    jPanel14.add(jLabel52, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 110, -1, -1));

    jLabel56.setIcon(new javax.swing.ImageIcon(getClass().getResource("/View/DashBoardBg.jpg"))); // NOI18N
    jPanel14.add(jLabel56, new org.netbeans.lib.awtextra.AbsoluteConstraints(-10, 0, 1080, 180));

    Main5.add(jPanel14, "card2");

    ScheduleManagement.add(Main5, new org.netbeans.lib.awtextra.AbsoluteConstraints(240, 0, 1070, 720));

    jPanel1.add(ScheduleManagement, "card7");

    AnnouncementManagement.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

    Nav6.setBackground(new java.awt.Color(0, 102, 102));
    Nav6.setMinimumSize(new java.awt.Dimension(225, 720));
    Nav6.setLayout(new java.awt.CardLayout());

    jPanel11.setBackground(new java.awt.Color(0, 102, 102));
    jPanel11.setMinimumSize(new java.awt.Dimension(220, 720));

    Title6.setFont(new java.awt.Font("Serif", 0, 48)); // NOI18N
    Title6.setForeground(new java.awt.Color(0, 51, 51));
    Title6.setText("volta.");

    DashBoardBtn6.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
    DashBoardBtn6.setIcon(new javax.swing.ImageIcon(getClass().getResource("/View/UnclickedDashboard.png"))); // NOI18N

    ManageTeamBtn6.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
    ManageTeamBtn6.setIcon(new javax.swing.ImageIcon(getClass().getResource("/View/UnclickedManageTeam.png"))); // NOI18N

    ManagePlayerBtn6.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
    ManagePlayerBtn6.setIcon(new javax.swing.ImageIcon(getClass().getResource("/View/UnclickedManagePlayer.png"))); // NOI18N

    BookingBtn6.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
    BookingBtn6.setIcon(new javax.swing.ImageIcon(getClass().getResource("/View/UnclickedBooking.png"))); // NOI18N

    ScheduleBtn6.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
    ScheduleBtn6.setIcon(new javax.swing.ImageIcon(getClass().getResource("/View/UnclickedSchedule.png"))); // NOI18N

    AnnouncementBtn6.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
    AnnouncementBtn6.setIcon(new javax.swing.ImageIcon(getClass().getResource("/View/ClickedAnnouncement.png"))); // NOI18N

    javax.swing.GroupLayout jPanel11Layout = new javax.swing.GroupLayout(jPanel11);
    jPanel11.setLayout(jPanel11Layout);
    jPanel11Layout.setHorizontalGroup(
        jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(jPanel11Layout.createSequentialGroup()
            .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel11Layout.createSequentialGroup()
                    .addGap(118, 118, 118)
                    .addComponent(Title6, javax.swing.GroupLayout.PREFERRED_SIZE, 108, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGroup(jPanel11Layout.createSequentialGroup()
                    .addGap(6, 6, 6)
                    .addComponent(DashBoardBtn6, javax.swing.GroupLayout.PREFERRED_SIZE, 220, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGroup(jPanel11Layout.createSequentialGroup()
                    .addGap(6, 6, 6)
                    .addComponent(ManageTeamBtn6, javax.swing.GroupLayout.PREFERRED_SIZE, 220, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGroup(jPanel11Layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(ManagePlayerBtn6, javax.swing.GroupLayout.PREFERRED_SIZE, 216, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGroup(jPanel11Layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(BookingBtn6, javax.swing.GroupLayout.PREFERRED_SIZE, 218, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGroup(jPanel11Layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(ScheduleBtn6, javax.swing.GroupLayout.PREFERRED_SIZE, 218, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGroup(jPanel11Layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(AnnouncementBtn6, javax.swing.GroupLayout.PREFERRED_SIZE, 218, javax.swing.GroupLayout.PREFERRED_SIZE)))
            .addContainerGap(14, Short.MAX_VALUE))
    );
    jPanel11Layout.setVerticalGroup(
        jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(jPanel11Layout.createSequentialGroup()
            .addGap(33, 33, 33)
            .addComponent(Title6, javax.swing.GroupLayout.PREFERRED_SIZE, 58, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(89, 89, 89)
            .addComponent(DashBoardBtn6)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(ManageTeamBtn6)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
            .addComponent(ManagePlayerBtn6)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
            .addComponent(BookingBtn6)
            .addGap(12, 12, 12)
            .addComponent(ScheduleBtn6, javax.swing.GroupLayout.PREFERRED_SIZE, 66, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
            .addComponent(AnnouncementBtn6, javax.swing.GroupLayout.PREFERRED_SIZE, 51, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addContainerGap())
    );

    // Code adding the component to the parent container - not shown here

    DashBoardBtn6.addMouseListener(new java.awt.event.MouseAdapter() {
        @Override
        public void mouseClicked(java.awt.event.MouseEvent e) {
            System.out.print("Button clicked");
            // Get the CardLayout from jPanel1
            CardLayout cardLayout = (CardLayout) jPanel1.getLayout();

            // Switch to the AdminDashboard card
            cardLayout.show(jPanel1, "card3");
        }

        @Override
        public void mouseEntered(java.awt.event.MouseEvent e) {
            DashBoardBtn6.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        }
    });
    ManageTeamBtn6.addMouseListener(new java.awt.event.MouseAdapter() {
        @Override
        public void mouseClicked(java.awt.event.MouseEvent e) {
            System.out.print("Button clicked");
            // Get the CardLayout from jPanel1
            CardLayout cardLayout = (CardLayout) jPanel1.getLayout();

            // Switch to the AdminDashboard card
            cardLayout.show(jPanel1, "card4");
        }

        @Override
        public void mouseEntered(java.awt.event.MouseEvent e) {
            ManageTeamBtn6.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        }
    });
    ManagePlayerBtn6.addMouseListener(new java.awt.event.MouseAdapter() {
        @Override
        public void mouseClicked(java.awt.event.MouseEvent e) {
            System.out.print("Button clicked");
            // Get the CardLayout from jPanel1
            CardLayout cardLayout = (CardLayout) jPanel1.getLayout();

            // Switch to the AdminDashboard card
            cardLayout.show(jPanel1, "card5");
        }

        @Override
        public void mouseEntered(java.awt.event.MouseEvent e) {
            ManagePlayerBtn6.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        }
    });
    BookingBtn6.addMouseListener(new java.awt.event.MouseAdapter() {
        @Override
        public void mouseClicked(java.awt.event.MouseEvent e) {
            System.out.print("Button clicked");
            // Get the CardLayout from jPanel1
            CardLayout cardLayout = (CardLayout) jPanel1.getLayout();

            // Switch to the AdminDashboard card
            cardLayout.show(jPanel1, "card6");
        }

        @Override
        public void mouseEntered(java.awt.event.MouseEvent e) {
            BookingBtn6.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        }
    });
    ScheduleBtn6.addMouseListener(new java.awt.event.MouseAdapter() {
        @Override
        public void mouseClicked(java.awt.event.MouseEvent e) {
            System.out.print("Button clicked");
            // Get the CardLayout from jPanel1
            CardLayout cardLayout = (CardLayout) jPanel1.getLayout();

            // Switch to the AdminDashboard card
            cardLayout.show(jPanel1, "card7");
        }

        @Override
        public void mouseEntered(java.awt.event.MouseEvent e) {
            ScheduleBtn6.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        }
    });
    AnnouncementBtn6.addMouseListener(new java.awt.event.MouseAdapter() {
        @Override
        public void mouseClicked(java.awt.event.MouseEvent e) {
            System.out.print("Button clicked");
            // Get the CardLayout from jPanel1
            CardLayout cardLayout = (CardLayout) jPanel1.getLayout();

            // Switch to the AdminDashboard card
            cardLayout.show(jPanel1, "card8");
        }

        @Override
        public void mouseEntered(java.awt.event.MouseEvent e) {
            AnnouncementBtn6.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        }
    });

    Nav6.add(jPanel11, "card2");

    AnnouncementManagement.add(Nav6, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 240, 720));

    Main6.setLayout(new java.awt.CardLayout());

    jPanel16.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

    jLabel58.setFont(new java.awt.Font("Serif", 0, 50)); // NOI18N
    jLabel58.setForeground(new java.awt.Color(0, 153, 153));
    jLabel58.setText("Annoucement");
    jPanel16.add(jLabel58, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 110, -1, -1));

    jLabel57.setIcon(new javax.swing.ImageIcon(getClass().getResource("/View/DashBoardBg.jpg"))); // NOI18N
    jPanel16.add(jLabel57, new org.netbeans.lib.awtextra.AbsoluteConstraints(-10, 0, 1080, 180));

    jPanel3.setBackground(new java.awt.Color(153, 153, 153));
    jPanel3.setForeground(new java.awt.Color(153, 153, 153));
    jPanel3.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

    cross3.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
    cross3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/View/cross.png"))); // NOI18N
    jPanel3.add(cross3, new org.netbeans.lib.awtextra.AbsoluteConstraints(780, 10, 30, 30));

    jLabel59.setFont(new java.awt.Font("Serif", 0, 24)); // NOI18N
    jLabel59.setText("Safalta fully Paid for the booking for Futsal Ground G1 for Feb 12 2026");
    jPanel3.add(jLabel59, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 20, 720, 30));

    jPanel27.setBackground(new java.awt.Color(153, 153, 153));
    jPanel27.setForeground(new java.awt.Color(153, 153, 153));
    jPanel27.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

    jLabel64.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
    jLabel64.setIcon(new javax.swing.ImageIcon(getClass().getResource("/View/cross.png"))); // NOI18N
    jPanel27.add(jLabel64, new org.netbeans.lib.awtextra.AbsoluteConstraints(780, 10, 30, 30));

    jLabel65.setFont(new java.awt.Font("Serif", 0, 24)); // NOI18N
    jLabel65.setText("Dibika Cancelled Booking for Futsal Ground G2 for Feb 26 2025, 6:00 PM");
    jPanel27.add(jLabel65, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 20, 720, 30));

    jPanel3.add(jPanel27, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 380, 820, 150));

    jPanel16.add(jPanel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 410, 820, 70));

    jPanel26.setBackground(new java.awt.Color(153, 153, 153));
    jPanel26.setForeground(new java.awt.Color(153, 153, 153));
    jPanel26.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

    cross1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
    cross1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/View/cross.png"))); // NOI18N
    jPanel26.add(cross1, new org.netbeans.lib.awtextra.AbsoluteConstraints(780, 10, 30, 30));

    jLabel63.setFont(new java.awt.Font("Serif", 0, 24)); // NOI18N
    jLabel63.setText("Aarogya Just Booked Futsal Ground G6 for Jan 26 2025, 6:00 PM");
    jPanel26.add(jLabel63, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 20, 700, -1));

    jPanel16.add(jPanel26, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 210, 820, 70));

    jPanel28.setBackground(new java.awt.Color(153, 153, 153));
    jPanel28.setForeground(new java.awt.Color(153, 153, 153));
    jPanel28.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

    cross2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
    cross2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/View/cross.png"))); // NOI18N
    jPanel28.add(cross2, new org.netbeans.lib.awtextra.AbsoluteConstraints(780, 10, 30, 30));

    jLabel67.setFont(new java.awt.Font("Serif", 0, 24)); // NOI18N
    jLabel67.setText("Dibika Cancelled Booking for Futsal Ground G2 for Feb 26 2025, 6:00 PM");
    jPanel28.add(jLabel67, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 20, 720, 30));

    jPanel29.setBackground(new java.awt.Color(153, 153, 153));
    jPanel29.setForeground(new java.awt.Color(153, 153, 153));
    jPanel29.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

    jLabel68.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
    jLabel68.setIcon(new javax.swing.ImageIcon(getClass().getResource("/View/cross.png"))); // NOI18N
    jPanel29.add(jLabel68, new org.netbeans.lib.awtextra.AbsoluteConstraints(780, 10, 30, 30));

    jLabel69.setFont(new java.awt.Font("Serif", 0, 24)); // NOI18N
    jLabel69.setText("Dibika Cancelled Booking for Futsal Ground G2 for Feb 26 2025, 6:00 PM");
    jPanel29.add(jLabel69, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 20, 720, 30));

    jPanel28.add(jPanel29, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 380, 820, 150));

    jPanel16.add(jPanel28, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 310, 820, 70));

    Main6.add(jPanel16, "card2");

    AnnouncementManagement.add(Main6, new org.netbeans.lib.awtextra.AbsoluteConstraints(240, 0, 1070, 720));

    jPanel1.add(AnnouncementManagement, "card8");

    ProfileSettings.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

    Nav7.setBackground(new java.awt.Color(0, 102, 102));
    Nav7.setMinimumSize(new java.awt.Dimension(225, 720));
    Nav7.setLayout(new java.awt.CardLayout());

    jPanel21.setBackground(new java.awt.Color(0, 102, 102));
    jPanel21.setMinimumSize(new java.awt.Dimension(220, 720));

    Title7.setFont(new java.awt.Font("Serif", 0, 48)); // NOI18N
    Title7.setForeground(new java.awt.Color(0, 51, 51));
    Title7.setText("volta.");

    DashBoardBtn7.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
    DashBoardBtn7.setIcon(new javax.swing.ImageIcon(getClass().getResource("/View/UnclickedDashboard.png"))); // NOI18N

    ManageTeamBtn7.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
    ManageTeamBtn7.setIcon(new javax.swing.ImageIcon(getClass().getResource("/View/UnclickedManageTeam.png"))); // NOI18N

    ManagePlayerBtn7.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
    ManagePlayerBtn7.setIcon(new javax.swing.ImageIcon(getClass().getResource("/View/UnclickedManagePlayer.png"))); // NOI18N

    BookingBtn7.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
    BookingBtn7.setIcon(new javax.swing.ImageIcon(getClass().getResource("/View/UnclickedBooking.png"))); // NOI18N

    ScheduleBtn7.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
    ScheduleBtn7.setIcon(new javax.swing.ImageIcon(getClass().getResource("/View/UnclickedSchedule.png"))); // NOI18N

    AnnouncementBtn7.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
    AnnouncementBtn7.setIcon(new javax.swing.ImageIcon(getClass().getResource("/View/UnclickedAnnouncement.png"))); // NOI18N

    javax.swing.GroupLayout jPanel21Layout = new javax.swing.GroupLayout(jPanel21);
    jPanel21.setLayout(jPanel21Layout);
    jPanel21Layout.setHorizontalGroup(
        jPanel21Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(jPanel21Layout.createSequentialGroup()
            .addGroup(jPanel21Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel21Layout.createSequentialGroup()
                    .addGap(118, 118, 118)
                    .addComponent(Title7, javax.swing.GroupLayout.PREFERRED_SIZE, 108, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGroup(jPanel21Layout.createSequentialGroup()
                    .addGap(6, 6, 6)
                    .addComponent(DashBoardBtn7, javax.swing.GroupLayout.PREFERRED_SIZE, 220, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGroup(jPanel21Layout.createSequentialGroup()
                    .addGap(6, 6, 6)
                    .addComponent(ManageTeamBtn7, javax.swing.GroupLayout.PREFERRED_SIZE, 220, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGroup(jPanel21Layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(ManagePlayerBtn7, javax.swing.GroupLayout.PREFERRED_SIZE, 216, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGroup(jPanel21Layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(BookingBtn7, javax.swing.GroupLayout.PREFERRED_SIZE, 218, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGroup(jPanel21Layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(ScheduleBtn7, javax.swing.GroupLayout.PREFERRED_SIZE, 218, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGroup(jPanel21Layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(AnnouncementBtn7, javax.swing.GroupLayout.PREFERRED_SIZE, 218, javax.swing.GroupLayout.PREFERRED_SIZE)))
            .addContainerGap(14, Short.MAX_VALUE))
    );
    jPanel21Layout.setVerticalGroup(
        jPanel21Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(jPanel21Layout.createSequentialGroup()
            .addGap(33, 33, 33)
            .addComponent(Title7, javax.swing.GroupLayout.PREFERRED_SIZE, 58, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(89, 89, 89)
            .addComponent(DashBoardBtn7)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(ManageTeamBtn7)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
            .addComponent(ManagePlayerBtn7)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
            .addComponent(BookingBtn7)
            .addGap(12, 12, 12)
            .addComponent(ScheduleBtn7, javax.swing.GroupLayout.PREFERRED_SIZE, 66, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
            .addComponent(AnnouncementBtn7, javax.swing.GroupLayout.PREFERRED_SIZE, 51, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addContainerGap())
    );

    // Code adding the component to the parent container - not shown here

    DashBoardBtn7.addMouseListener(new java.awt.event.MouseAdapter() {
        @Override
        public void mouseClicked(java.awt.event.MouseEvent e) {
            System.out.print("Button clicked");
            // Get the CardLayout from jPanel1
            CardLayout cardLayout = (CardLayout) jPanel1.getLayout();

            // Switch to the AdminDashboard card
            cardLayout.show(jPanel1, "card3");
        }

        @Override
        public void mouseEntered(java.awt.event.MouseEvent e) {
            DashBoardBtn7.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        }
    });
    ManageTeamBtn7.addMouseListener(new java.awt.event.MouseAdapter() {
        @Override
        public void mouseClicked(java.awt.event.MouseEvent e) {
            System.out.print("Button clicked");
            // Get the CardLayout from jPanel1
            CardLayout cardLayout = (CardLayout) jPanel1.getLayout();

            // Switch to the AdminDashboard card
            cardLayout.show(jPanel1, "card4");
        }

        @Override
        public void mouseEntered(java.awt.event.MouseEvent e) {
            ManageTeamBtn7.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        }
    });
    ManagePlayerBtn7.addMouseListener(new java.awt.event.MouseAdapter() {
        @Override
        public void mouseClicked(java.awt.event.MouseEvent e) {
            System.out.print("Button clicked");
            // Get the CardLayout from jPanel1
            CardLayout cardLayout = (CardLayout) jPanel1.getLayout();

            // Switch to the AdminDashboard card
            cardLayout.show(jPanel1, "card5");
        }

        @Override
        public void mouseEntered(java.awt.event.MouseEvent e) {
            ManagePlayerBtn7.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        }
    });
    BookingBtn7.addMouseListener(new java.awt.event.MouseAdapter() {
        @Override
        public void mouseClicked(java.awt.event.MouseEvent e) {
            System.out.print("Button clicked");
            // Get the CardLayout from jPanel1
            CardLayout cardLayout = (CardLayout) jPanel1.getLayout();

            // Switch to the AdminDashboard card
            cardLayout.show(jPanel1, "card6");
        }

        @Override
        public void mouseEntered(java.awt.event.MouseEvent e) {
            BookingBtn7.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        }
    });
    ScheduleBtn7.addMouseListener(new java.awt.event.MouseAdapter() {
        @Override
        public void mouseClicked(java.awt.event.MouseEvent e) {
            System.out.print("Button clicked");
            // Get the CardLayout from jPanel1
            CardLayout cardLayout = (CardLayout) jPanel1.getLayout();

            // Switch to the AdminDashboard card
            cardLayout.show(jPanel1, "card7");
        }

        @Override
        public void mouseEntered(java.awt.event.MouseEvent e) {
            ScheduleBtn7.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        }
    });
    AnnouncementBtn7.addMouseListener(new java.awt.event.MouseAdapter() {
        @Override
        public void mouseClicked(java.awt.event.MouseEvent e) {
            System.out.print("Button clicked");
            // Get the CardLayout from jPanel1
            CardLayout cardLayout = (CardLayout) jPanel1.getLayout();

            // Switch to the AdminDashboard card
            cardLayout.show(jPanel1, "card8");
        }

        @Override
        public void mouseEntered(java.awt.event.MouseEvent e) {
            AnnouncementBtn7.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        }
    });

    Nav7.add(jPanel21, "card2");

    ProfileSettings.add(Nav7, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 240, 720));

    Main7.setLayout(new java.awt.CardLayout());

    jPanel18.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

    LogOutBtn.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
    LogOutBtn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/View/Logout.png"))); // NOI18N
    jPanel18.add(LogOutBtn, new org.netbeans.lib.awtextra.AbsoluteConstraints(379, 485, 218, 58));
    LogOutBtn.addMouseListener(new java.awt.event.MouseAdapter() {
        @Override
        public void mouseClicked(java.awt.event.MouseEvent e) {
            System.out.print("Button clicked");
            // Get the CardLayout from jPanel1
            CardLayout cardLayout = (CardLayout) jPanel1.getLayout();

            // Switch to the AdminDashboard card
            cardLayout.show(jPanel1, "card1");
        }

        @Override
        public void mouseEntered(java.awt.event.MouseEvent e) {
            LogOutBtn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        }
    });

    jLabel60.setIcon(new javax.swing.ImageIcon(getClass().getResource("/View/DashBoardBg.jpg"))); // NOI18N
    jPanel18.add(jLabel60, new org.netbeans.lib.awtextra.AbsoluteConstraints(-10, 0, 1080, 180));

    Main7.add(jPanel18, "card2");

    ProfileSettings.add(Main7, new org.netbeans.lib.awtextra.AbsoluteConstraints(240, 0, 1070, 720));

    jPanel1.add(ProfileSettings, "card9");

    getContentPane().add(jPanel1);
    jPanel1.setBounds(0, 0, 1310, 720);

    pack();
    }// </editor-fold>//GEN-END:initComponents

    private void LoginBtnActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_LoginBtnActionPerformed
        // Get user input
        String username = LoginUsername.getText().trim();
        String password = new String(LoginPassword.getPassword());

        // Basic validation
        if (username.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter username",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter password",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Call controller to login
        User user = userController.loginUser(username, password);

        if (user != null) {
            // Login successful
            JOptionPane.showMessageDialog(this, "Login successful!");

            // Clear form
            LoginUsername.setText("");
            LoginPassword.setText("");

            // Switch to Dashboard based on user role
            CardLayout cardLayout = (CardLayout) jPanel1.getLayout();

            if (username.endsWith(".admin")) {
                // Admin Dashboard
                isAdmin = true;
                cardLayout.show(jPanel1, "card3");
            } else {
                // User Dashboard
                isAdmin = false;
                cardLayout.show(jPanel1, "card11");
            }

            // Set welcome message
            setWelcomeMessage(user.getFirstName());
        } else {
            JOptionPane.showMessageDialog(this,
                    "Invalid username or password",
                    "Error", JOptionPane.ERROR_MESSAGE);
            System.out.println("Testing");
            System.out.println(username);
            System.out.println(password);
        }
    }// GEN-LAST:event_LoginBtnActionPerformed

    private void LoginPasswordActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_LoginPasswordActionPerformed
        // TODO add your handling code here:
    }// GEN-LAST:event_LoginPasswordActionPerformed

    private void LoginUsernameActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_LoginUsernameActionPerformed
        // TODO add your handling code here:
    }// GEN-LAST:event_LoginUsernameActionPerformed

    private void TMTeamNameTextFieldActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_TMTeamNameTextFieldActionPerformed
        // TODO add your handling code here:
    }// GEN-LAST:event_TMTeamNameTextFieldActionPerformed

    private void PMSortByComboBoxActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_PMSortByComboBoxActionPerformed
        // TODO add your handling code here:
    }// GEN-LAST:event_PMSortByComboBoxActionPerformed

    private void SignUpUsernameActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_SignUpUsernameActionPerformed
        // TODO add your handling code here:
    }// GEN-LAST:event_SignUpUsernameActionPerformed

    private void SignUpPasswordActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_SignUpPasswordActionPerformed
        // TODO add your handling code here:
    }// GEN-LAST:event_SignUpPasswordActionPerformed

    private void SignUpBtnActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_SignUpBtnActionPerformed
        // Get user input
        String username = SignUpUsername.getText().trim();
        String password = new String(SignUpPassword.getPassword());
        String retypePassword = new String(SignUpRetypePw.getPassword());
        String firstName = SignUpFirstName.getText().trim();
        String lastName = SignUpLastName.getText().trim();
        String dob = getDateFromChooser();

        // Validation
        if (username.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter username",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter password",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!password.equals(retypePassword)) {
            JOptionPane.showMessageDialog(this,
                    "Password and Retype Password do not match",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (firstName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter first name",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (lastName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter last name",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (dob.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select date of birth",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Call controller to register
        boolean success = userController.registerUser(username, password,
                retypePassword, firstName, lastName, dob);

        if (success) {
            // Registration successful
            JOptionPane.showMessageDialog(this,
                    "Registration successful! You can now login.");

            // Clear form
            SignUpUsername.setText("");
            SignUpPassword.setText("");
            SignUpRetypePw.setText("");
            SignUpFirstName.setText("");
            SignUpLastName.setText("");
            clearDateChooser();

            // Switch to login screen
            CardLayout cardLayout = (CardLayout) jPanel1.getLayout();
            cardLayout.show(jPanel1, "card1");
        } else {
            // Registration failed
            JOptionPane.showMessageDialog(this,
                    "Registration failed. Username might already exist.",
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }// GEN-LAST:event_SignUpBtnActionPerformed

    private void SignUpFirstNameActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_SignUpFirstNameActionPerformed
        // TODO add your handling code here:
    }// GEN-LAST:event_SignUpFirstNameActionPerformed

    private void SignUpLastNameActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_SignUpLastNameActionPerformed
        // TODO add your handling code here:
    }// GEN-LAST:event_SignUpLastNameActionPerformed

    private void SignUpRetypePwActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_SignUpRetypePwActionPerformed
        // TODO add your handling code here:
    }// GEN-LAST:event_SignUpRetypePwActionPerformed

    private void PMUpdateBtnActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_PMUpdateBtnActionPerformed
        // TODO add your handling code here:
    }// GEN-LAST:event_PMUpdateBtnActionPerformed

    private void ManagerTextFieldActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_ManagerTextFieldActionPerformed
        // TODO add your handling code here:
    }// GEN-LAST:event_ManagerTextFieldActionPerformed

    private void TMSearchTextFieldActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_TMSearchTextFieldActionPerformed
        // TODO add your handling code here:
    }// GEN-LAST:event_TMSearchTextFieldActionPerformed

    private void PMSearchComboBoxActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_PMSearchComboBoxActionPerformed
        // TODO add your handling code here:
    }// GEN-LAST:event_PMSearchComboBoxActionPerformed

    private void dateChooserPanel1OnCommit(datechooser.events.CommitEvent evt) {// GEN-FIRST:event_dateChooserPanel1OnCommit
        // TODO add your handling code here:
    }// GEN-LAST:event_dateChooserPanel1OnCommit

    private void dateChooserPanel1OnSelectionChange(datechooser.events.SelectionChangedEvent evt) {// GEN-FIRST:event_dateChooserPanel1OnSelectionChange
        try {
            java.util.Calendar selectedDate = dateChooserPanel1.getSelectedDate();
            if (selectedDate != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                String dateStr = sdf.format(selectedDate.getTime());

                LinkedList<Booking> dayBookings = bookingController.getBookingsForDate(dateStr);

                if (dayBookings.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "No accepted bookings for " + dateStr, "Schedule",
                            JOptionPane.INFORMATION_MESSAGE);
                } else {
                    StringBuilder sb = new StringBuilder("Accepted Bookings for " + dateStr + ":\n\n");
                    for (Booking b : dayBookings) {
                        sb.append("ID: ").append(b.getBookingId()).append("\n");
                        sb.append("Team: ").append(b.getTeamName()).append("\n");
                        sb.append("Status: ").append(b.getStatus()).append("\n");
                        sb.append("--------------------------\n");
                    }
                    JOptionPane.showMessageDialog(this, sb.toString(), "Schedule Details",
                            JOptionPane.INFORMATION_MESSAGE);
                }
            }
        } catch (Exception e) {
            System.err.println("Error handling date selection: " + e.getMessage());
        }
    }// GEN-LAST:event_dateChooserPanel1OnSelectionChange

    private void UploadPhotoActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_UploadPhotoActionPerformed
        // TODO add your handling code here:
    }

    public static void main(String args[]) {
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ReflectiveOperationException | javax.swing.UnsupportedLookAndFeelException ex) {
            logger.log(java.util.logging.Level.SEVERE, null, ex);
        }

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> new Admin().setVisible(true));

    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel AdminDashboard;
    private javax.swing.JLabel AnnouncementBtn1;
    private javax.swing.JLabel AnnouncementBtn2;
    private javax.swing.JLabel AnnouncementBtn3;
    private javax.swing.JLabel AnnouncementBtn4;
    private javax.swing.JLabel AnnouncementBtn5;
    private javax.swing.JLabel AnnouncementBtn6;
    private javax.swing.JLabel AnnouncementBtn7;
    private javax.swing.JPanel AnnouncementManagement;
    private javax.swing.JLabel BookingBtn1;
    private javax.swing.JLabel BookingBtn2;
    private javax.swing.JLabel BookingBtn3;
    private javax.swing.JLabel BookingBtn4;
    private javax.swing.JLabel BookingBtn5;
    private javax.swing.JLabel BookingBtn6;
    private javax.swing.JLabel BookingBtn7;
    private javax.swing.JPanel BookingManagement;
    private javax.swing.JTable BookingTable;
    private javax.swing.JLabel DashBoardBtn1;
    private javax.swing.JLabel DashBoardBtn2;
    private javax.swing.JLabel DashBoardBtn3;
    private javax.swing.JLabel DashBoardBtn4;
    private javax.swing.JLabel DashBoardBtn5;
    private javax.swing.JLabel DashBoardBtn6;
    private javax.swing.JLabel DashBoardBtn7;
    private datechooser.beans.DateChooserCombo DateOfBirth;
    private javax.swing.JLabel LogOutBtn;
    private javax.swing.JPanel Login;
    private javax.swing.JLabel LoginBg;
    private javax.swing.JLabel LoginBg1;
    private javax.swing.JToggleButton LoginBtn;
    private javax.swing.JPasswordField LoginPassword;
    private javax.swing.JLabel LoginSignUp;
    private javax.swing.JTextField LoginUsername;
    private javax.swing.JPanel Main1;
    private javax.swing.JPanel Main2;
    private javax.swing.JPanel Main3;
    private javax.swing.JPanel Main4;
    private javax.swing.JPanel Main5;
    private javax.swing.JPanel Main6;
    private javax.swing.JPanel Main7;
    private javax.swing.JLabel ManagePlayerBtn1;
    private javax.swing.JLabel ManagePlayerBtn2;
    private javax.swing.JLabel ManagePlayerBtn3;
    private javax.swing.JLabel ManagePlayerBtn4;
    private javax.swing.JLabel ManagePlayerBtn5;
    private javax.swing.JLabel ManagePlayerBtn6;
    private javax.swing.JLabel ManagePlayerBtn7;
    private javax.swing.JLabel ManageTeamBtn1;
    private javax.swing.JLabel ManageTeamBtn2;
    private javax.swing.JLabel ManageTeamBtn3;
    private javax.swing.JLabel ManageTeamBtn4;
    private javax.swing.JLabel ManageTeamBtn5;
    private javax.swing.JLabel ManageTeamBtn6;
    private javax.swing.JLabel ManageTeamBtn7;
    private javax.swing.JTextField ManagerTextField;
    private javax.swing.JPanel Nav1;
    private javax.swing.JPanel Nav2;
    private javax.swing.JPanel Nav3;
    private javax.swing.JPanel Nav4;
    private javax.swing.JPanel Nav5;
    private javax.swing.JPanel Nav6;
    private javax.swing.JPanel Nav7;
    private javax.swing.JTextField NoPlayerTextField;
    private javax.swing.JButton PMAddBtn;
    private javax.swing.JButton PMDeleteBtn;
    private javax.swing.JButton PMReadBtn;
    private javax.swing.JLabel PMSearchBtn;
    private javax.swing.JComboBox<String> PMSearchComboBox;
    private javax.swing.JTextField PMSearchTextField;
    private javax.swing.JComboBox<String> PMSortByComboBox;
    private javax.swing.JTextField PMTeamNameTextField;
    private javax.swing.JButton PMUpdateBtn;
    private javax.swing.JTextField PlayerAgeTextField;
    private javax.swing.JTextField PlayerIDTextField;
    private javax.swing.JTextField PlayerJerseyNoTextField;
    private javax.swing.JPanel PlayerManagement;
    private javax.swing.JTextField PlayerNameTextField;
    private javax.swing.JTextField PlayerPositionTextField;
    private javax.swing.JTable PlayerTable;
    private javax.swing.JLabel ProfileBtn;
    private javax.swing.JPanel ProfileSettings;
    private javax.swing.JTextArea RecentActivitesTextArea;
    private javax.swing.JLabel ScheduleBtn1;
    private javax.swing.JLabel ScheduleBtn2;
    private javax.swing.JLabel ScheduleBtn3;
    private javax.swing.JLabel ScheduleBtn4;
    private javax.swing.JLabel ScheduleBtn5;
    private javax.swing.JLabel ScheduleBtn6;
    private javax.swing.JLabel ScheduleBtn7;
    private javax.swing.JPanel ScheduleManagement;
    private javax.swing.JPanel SignUp;
    private javax.swing.JToggleButton SignUpBtn;
    private javax.swing.JTextField SignUpFirstName;
    private javax.swing.JTextField SignUpLastName;
    private javax.swing.JPasswordField SignUpPassword;
    private javax.swing.JPasswordField SignUpRetypePw;
    private javax.swing.JTextField SignUpUsername;
    private javax.swing.JLabel SignupLogin;
    private javax.swing.JButton TMAddBtn;
    private javax.swing.JButton TMDeleteBtn;
    private javax.swing.JButton TMReadBtn;
    private javax.swing.JLabel TMSearchBtn;
    private javax.swing.JComboBox<String> TMSearchComboBox;
    private javax.swing.JTextField TMSearchTextField;
    private javax.swing.JComboBox<String> TMSortByComboBox;
    private javax.swing.JTextField TMTeamNameTextField;
    private javax.swing.JButton TMUpdateBtn;
    private javax.swing.JTextField TeamIDTextField;
    private javax.swing.JPanel TeamManagement;
    private javax.swing.JTable TeamTable;
    private javax.swing.JLabel Title1;
    private javax.swing.JLabel Title2;
    private javax.swing.JLabel Title3;
    private javax.swing.JLabel Title4;
    private javax.swing.JLabel Title5;
    private javax.swing.JLabel Title6;
    private javax.swing.JLabel Title7;
    private javax.swing.JButton UploadPhoto;
    private javax.swing.JLabel cross1;
    private javax.swing.JLabel cross2;
    private javax.swing.JLabel cross3;
    private datechooser.beans.DateChooserPanel dateChooserPanel1;
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel24;
    private javax.swing.JLabel jLabel25;
    private javax.swing.JLabel jLabel26;
    private javax.swing.JLabel jLabel27;
    private javax.swing.JLabel jLabel28;
    private javax.swing.JLabel jLabel29;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel30;
    private javax.swing.JLabel jLabel31;
    private javax.swing.JLabel jLabel32;
    private javax.swing.JLabel jLabel33;
    private javax.swing.JLabel jLabel34;
    private javax.swing.JLabel jLabel35;
    private javax.swing.JLabel jLabel36;
    private javax.swing.JLabel jLabel37;
    private javax.swing.JLabel jLabel38;
    private javax.swing.JLabel jLabel39;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel40;
    private javax.swing.JLabel jLabel41;
    private javax.swing.JLabel jLabel42;
    private javax.swing.JLabel jLabel43;
    private javax.swing.JLabel jLabel44;
    private javax.swing.JLabel jLabel45;
    private javax.swing.JLabel jLabel46;
    private javax.swing.JLabel jLabel47;
    private javax.swing.JLabel jLabel48;
    private javax.swing.JLabel jLabel49;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel50;
    private javax.swing.JLabel jLabel51;
    private javax.swing.JLabel jLabel52;
    private javax.swing.JLabel jLabel53;
    private javax.swing.JLabel jLabel54;
    private javax.swing.JLabel jLabel55;
    private javax.swing.JLabel jLabel56;
    private javax.swing.JLabel jLabel57;
    private javax.swing.JLabel jLabel58;
    private javax.swing.JLabel jLabel59;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel60;
    private javax.swing.JLabel jLabel63;
    private javax.swing.JLabel jLabel64;
    private javax.swing.JLabel jLabel65;
    private javax.swing.JLabel jLabel67;
    private javax.swing.JLabel jLabel68;
    private javax.swing.JLabel jLabel69;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel10;
    private javax.swing.JPanel jPanel11;
    private javax.swing.JPanel jPanel12;
    private javax.swing.JPanel jPanel13;
    private javax.swing.JPanel jPanel14;
    private javax.swing.JPanel jPanel15;
    private javax.swing.JPanel jPanel16;
    private javax.swing.JPanel jPanel17;
    private javax.swing.JPanel jPanel18;
    private javax.swing.JPanel jPanel19;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel20;
    private javax.swing.JPanel jPanel21;
    private javax.swing.JPanel jPanel24;
    private javax.swing.JPanel jPanel26;
    private javax.swing.JPanel jPanel27;
    private javax.swing.JPanel jPanel28;
    private javax.swing.JPanel jPanel29;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JScrollBar jScrollBar1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    // End of variables declaration//GEN-END:variables
}
