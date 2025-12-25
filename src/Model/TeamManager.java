package Model;

import javax.swing.JOptionPane;
import java.util.LinkedList;

public class TeamManager {
    private LinkedList<Team> teams;
    
    public TeamManager() {
        teams = new LinkedList<>();
        prepareInitialTeamData();
    }
    
    private void prepareInitialTeamData() {
        teams.add(new Team("T001", "Manchester City", "Pep Guardiola", 25));
        teams.add(new Team("T002", "Real Madrid", "Carlo Ancelotti", 24));
        teams.add(new Team("T003", "FC Barcelona", "Xavi Hernandez", 23));
        teams.add(new Team("T004", "Bayern Munich", "Thomas Tuchel", 22));
    }
    
    // CRUD Operations using LinkedList
    public boolean addTeam(String teamId, String teamName, String manager, int noOfPlayers) {
        // Check if team ID already exists
        for (Team team : teams) {
            if (team.getTeamId().equals(teamId)) {
                return false;
            }
        }
        
        // Create new team and add to LinkedList
        Team newTeam = new Team(teamId, teamName, manager, noOfPlayers);
        teams.add(newTeam);
        return true;
    }
    
    public boolean updateTeam(String teamId, String teamName, String manager, int noOfPlayers) {
        // Find and update team in LinkedList
        for (int i = 0; i < teams.size(); i++) {
            Team team = teams.get(i);
            if (team.getTeamId().equals(teamId)) {
                Team updatedTeam = new Team(teamId, teamName, manager, noOfPlayers);
                teams.set(i, updatedTeam);
                return true;
            }
        }
        return false;
    }
    
    public boolean deleteTeam(String teamId) {
        // Find and remove team from LinkedList
        for (int i = 0; i < teams.size(); i++) {
            if (teams.get(i).getTeamId().equals(teamId)) {
                teams.remove(i);
                return true;
            }
        }
        return false;
    }
    
    // Get team by ID
    public Team getTeamById(String teamId) {
        for (Team team : teams) {
            if (team.getTeamId().equals(teamId)) {
                return team;
            }
        }
        return null;
    }
    
    // Search teams by name
    public LinkedList<Team> searchTeamsByName(String name) {
        LinkedList<Team> results = new LinkedList<>();
        for (Team team : teams) {
            if (team.getTeamName().toLowerCase().contains(name.toLowerCase())) {
                results.add(team);
            }
        }
        return results;
    }
    
    // Sort teams by number of players (bubble sort)
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
    
    // Check if team exists
    public boolean teamExists(String teamId) {
        for (Team team : teams) {
            if (team.getTeamId().equals(teamId)) {
                return true;
            }
        }
        return false;
    }
}