package Controller;

import Model.Team;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import java.util.LinkedList;

public class TeamController {
    private LinkedList<Team> teams = new LinkedList<>();
    
    public TeamController() {
        teams = new LinkedList<>();
        prepareInitialTeamData();
    }
    
    // Add initial sample data (like in MainScreen)
    private void prepareInitialTeamData() {
        teams.add(new Team("T001", "Manchester City", "Pep Guardiola", 25));
        teams.add(new Team("T002", "Real Madrid", "Carlo Ancelotti", 24));
        teams.add(new Team("T003", "FC Barcelona", "Xavi Hernandez", 23));
        teams.add(new Team("T004", "Bayern Munich", "Thomas Tuchel", 22));
    }
    
    // CRUD Operations using LinkedList (similar to MainScreen pattern)
    public boolean addTeam(String teamId, String teamName, String manager, String noOfPlayers) {
        // Validation
        if (teamId.isEmpty() || teamName.isEmpty() || manager.isEmpty() || noOfPlayers.isEmpty()) {
            JOptionPane.showMessageDialog(null, "All fields are required!", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        // Check if team ID already exists using LinkedList iteration
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
            
            // Create new team and add to LinkedList
            Team newTeam = new Team(teamId, teamName, manager, playerCount);
            teams.add(newTeam); // Adds to the end of LinkedList
            
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
            
            // Find and update team in LinkedList
            for (int i = 0; i < teams.size(); i++) {
                Team team = teams.get(i);
                if (team.getTeamId().equals(teamId)) {
                    Team updatedTeam = new Team(teamId, teamName, manager, playerCount);
                    teams.set(i, updatedTeam); // Update at specific index
                    
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
        
        // Find and remove team from LinkedList
        for (int i = 0; i < teams.size(); i++) {
            if (teams.get(i).getTeamId().equals(teamId)) {
                teams.remove(i); // Remove from specific index
                JOptionPane.showMessageDialog(null, "Team deleted successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                return true;
            }
        }
        
        JOptionPane.showMessageDialog(null, "Team not found!", "Error", JOptionPane.ERROR_MESSAGE);
        return false;
    }
    
    // Load teams to table (like loadStudentListToTable in MainScreen)
    public void loadTeamsToTable(DefaultTableModel tableModel) {
        tableModel.setRowCount(0); // Clear existing rows
        
        for (Team team : teams) {
            Object[] row = {
                team.getTeamId(),
                team.getTeamName(),
                team.getManager(),
                team.getNoOfPlayers()
            };
            tableModel.addRow(row);
        }
    }
    
    // Get team by ID using LinkedList iteration
    public Team getTeamById(String teamId) {
        for (Team team : teams) {
            if (team.getTeamId().equals(teamId)) {
                return team;
            }
        }
        return null;
    }
    
    // Load team data from table selection (like MainScreen's loadStudentFromTable)
public void loadTeamFromTable(String teamId, javax.swing.JTextField teamIdField,
                              javax.swing.JTextField teamNameField,
                              javax.swing.JTextField managerField,
                              javax.swing.JTextField noPlayerField) {
    if (teamId.isEmpty()) {
        return;
    }
    
    // Search in LinkedList
    for (Team team : teams) {
        if (team.getTeamId().equals(teamId)) {
            teamIdField.setText(team.getTeamId());
            teamNameField.setText(team.getTeamName());
            managerField.setText(team.getManager());
            noPlayerField.setText(String.valueOf(team.getNoOfPlayers()));
            break;
        }
    }
}
    
    // Search teams by name using LinkedList iteration
    public LinkedList<Team> searchTeamsByName(String name) {
        LinkedList<Team> results = new LinkedList<>();
        for (Team team : teams) {
            if (team.getTeamName().toLowerCase().contains(name.toLowerCase())) {
                results.add(team);
            }
        }
        return results;
    }
    
    // Sort teams by number of players (bubble sort implementation for LinkedList)
    public void sortTeamsByPlayerCount() {
        int n = teams.size();
        for (int i = 0; i < n - 1; i++) {
            for (int j = 0; j < n - i - 1; j++) {
                if (teams.get(j).getNoOfPlayers() > teams.get(j + 1).getNoOfPlayers()) {
                    // Swap teams
                    Team temp = teams.get(j);
                    teams.set(j, teams.get(j + 1));
                    teams.set(j + 1, temp);
                }
            }
        }
    }
    
    // Getter for teams LinkedList
    public LinkedList<Team> getTeams() {
        return teams;
    }
    
    // Get team count
    public int getTeamCount() {
        return teams.size();
    }
    
    // Check if team exists by ID
    public boolean teamExists(String teamId) {
        for (Team team : teams) {
            if (team.getTeamId().equals(teamId)) {
                return true;
            }
        }
        return false;
    }
}