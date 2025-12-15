/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Controller;

/**
 *
 * @author WELCOME
 */
import Model.Team;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import java.io.*;

public class TeamController {
    private static final String TEAM_DATA_FILE = "teams.txt";
    private List<Team> teams;
    
    public TeamController() {
        teams = new ArrayList<>();
        loadTeamsFromFile();
    }
    
    // CRUD Operations
    public boolean addTeam(String teamId, String teamName, String manager, String noOfPlayers) {
        // Validation
        if (teamId.isEmpty() || teamName.isEmpty() || manager.isEmpty() || noOfPlayers.isEmpty()) {
            JOptionPane.showMessageDialog(null, "All fields are required!", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        // Check if team ID already exists
        for (Team team : teams) {
            if (team.getTeamId().equals(teamId)) {
                JOptionPane.showMessageDialog(null, "Team ID already exists!", "Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        }
        
        try {
            // Validate numeric field
            int playerCount = Integer.parseInt(noOfPlayers);
            if (playerCount < 0) {
                JOptionPane.showMessageDialog(null, "Number of players cannot be negative!", "Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
            
            // Create new team
            Team newTeam = new Team(teamId, teamName, manager, playerCount);
            teams.add(newTeam);
            saveTeamsToFile();
            
            JOptionPane.showMessageDialog(null, "Team added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            return true;
            
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "Number of players must be a valid integer!", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }
    
    public boolean updateTeam(String teamId, String teamName, String manager, String noOfPlayers) {
        // Validation
        if (teamId.isEmpty() || teamName.isEmpty() || manager.isEmpty() || noOfPlayers.isEmpty()) {
            JOptionPane.showMessageDialog(null, "All fields are required!", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        try {
            int playerCount = Integer.parseInt(noOfPlayers);
            if (playerCount < 0) {
                JOptionPane.showMessageDialog(null, "Number of players cannot be negative!", "Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
            
            // Find and update team
            for (int i = 0; i < teams.size(); i++) {
                if (teams.get(i).getTeamId().equals(teamId)) {
                    Team updatedTeam = new Team(teamId, teamName, manager, playerCount);
                    teams.set(i, updatedTeam);
                    saveTeamsToFile();
                    
                    JOptionPane.showMessageDialog(null, "Team updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    return true;
                }
            }
            
            JOptionPane.showMessageDialog(null, "Team not found!", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
            
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "Number of players must be a valid integer!", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }
    
    public boolean deleteTeam(String teamId) {
        if (teamId.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Team ID is required!", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        // Find and remove team
        for (int i = 0; i < teams.size(); i++) {
            if (teams.get(i).getTeamId().equals(teamId)) {
                teams.remove(i);
                saveTeamsToFile();
                
                JOptionPane.showMessageDialog(null, "Team deleted successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                return true;
            }
        }
        
        JOptionPane.showMessageDialog(null, "Team not found!", "Error", JOptionPane.ERROR_MESSAGE);
        return false;
    }
    
    public void readTeams(DefaultTableModel tableModel) {
        // Clear existing rows
        tableModel.setRowCount(0);
        
        // Add all teams to table
        for (Team team : teams) {
            Object[] rowData = {
                team.getTeamId(),
                team.getTeamName(),
                team.getManager(),
                team.getNoOfPlayers()
            };
            tableModel.addRow(rowData);
        }
    }
    
    // Load team data from table selection
    public void loadTeamFromTable(String teamId, javax.swing.JTextField teamIdField, 
                                   javax.swing.JTextField teamNameField, 
                                   javax.swing.JTextField managerField, 
                                   javax.swing.JTextField noPlayersField) {
        if (teamId.isEmpty()) {
            return;
        }
        
        for (Team team : teams) {
            if (team.getTeamId().equals(teamId)) {
                teamIdField.setText(team.getTeamId());
                teamNameField.setText(team.getTeamName());
                managerField.setText(team.getManager());
                noPlayersField.setText(String.valueOf(team.getNoOfPlayers()));
                break;
            }
        }
    }
    
    // File Operations
    private void loadTeamsFromFile() {
        try {
            File file = new File(TEAM_DATA_FILE);
            if (!file.exists()) {
                file.createNewFile();
                return;
            }
            
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 4) {
                    try {
                        String teamId = parts[0];
                        String teamName = parts[1];
                        String manager = parts[2];
                        int noOfPlayers = Integer.parseInt(parts[3]);
                        
                        Team team = new Team(teamId, teamName, manager, noOfPlayers);
                        teams.add(team);
                    } catch (NumberFormatException e) {
                        System.err.println("Invalid data format in teams file: " + line);
                    }
                }
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private void saveTeamsToFile() {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(TEAM_DATA_FILE));
            for (Team team : teams) {
                String line = team.getTeamId() + "," + 
                             team.getTeamName() + "," + 
                             team.getManager() + "," + 
                             team.getNoOfPlayers();
                writer.write(line);
                writer.newLine();
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    // Getter for teams list
    public List<Team> getTeams() {
        return teams;
    }
}
