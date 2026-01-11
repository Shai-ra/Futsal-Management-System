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

    // Generic search for teams
    public LinkedList<Team> searchTeams(String criteria, String query) {
        LinkedList<Team> results = new LinkedList<>();
        String q = query.toLowerCase();
        for (Team team : teams) {
            boolean matches = false;
            switch (criteria) {
                case "Team ID":
                    matches = team.getTeamId().toLowerCase().contains(q);
                    break;
                case "Team Name":
                    matches = team.getTeamName().toLowerCase().contains(q);
                    break;
                case "Manager":
                    matches = team.getManager().toLowerCase().contains(q);
                    break;
                case "No. Player":
                    matches = String.valueOf(team.getNoOfPlayers()).contains(q);
                    break;
                default:
                    matches = team.getTeamName().toLowerCase().contains(q);
                    break;
            }
            if (matches) {
                results.add(team);
            }
        }
        return results;
    }

    // Generic sort for teams
    public void sortTeams(String criteria) {
        int n = teams.size();
        for (int i = 0; i < n - 1; i++) {
            for (int j = 0; j < n - i - 1; j++) {
                boolean swap = false;
                Team t1 = teams.get(j);
                Team t2 = teams.get(j + 1);
                switch (criteria) {
                    case "Team ID":
                        swap = t1.getTeamId().compareTo(t2.getTeamId()) > 0;
                        break;
                    case "No. Player":
                        swap = t1.getNoOfPlayers() > t2.getNoOfPlayers();
                        break;
                }
                if (swap) {
                    teams.set(j, t2);
                    teams.set(j + 1, t1);
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