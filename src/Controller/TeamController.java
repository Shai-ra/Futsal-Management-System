package Controller;

import Model.Team;
import Model.TeamManager;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import java.util.LinkedList;

public class TeamController {
    private TeamManager teamManager;

    public TeamController() {
        this.teamManager = new TeamManager();
    }

    public boolean addTeam(String teamId, String teamName, String manager, String noOfPlayers) {
        // Validation
        if (teamId.isEmpty() || teamName.isEmpty() || manager.isEmpty() || noOfPlayers.isEmpty()) {
            JOptionPane.showMessageDialog(null, "All fields are required!", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        try {
            int playerCount = Integer.parseInt(noOfPlayers);
            if (playerCount < 0) {
                JOptionPane.showMessageDialog(null, "Number of players cannot be negative!", "Error",
                        JOptionPane.ERROR_MESSAGE);
                return false;
            }

            // Add team through TeamManager
            boolean success = teamManager.addTeam(teamId, teamName, manager, playerCount);

            if (success) {
                JOptionPane.showMessageDialog(null, "Team added successfully!", "Success",
                        JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(null, "Team ID already exists!", "Error", JOptionPane.ERROR_MESSAGE);
            }
            return success;

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "Number of players must be a valid integer!", "Error",
                    JOptionPane.ERROR_MESSAGE);
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
                JOptionPane.showMessageDialog(null, "Number of players cannot be negative!", "Error",
                        JOptionPane.ERROR_MESSAGE);
                return false;
            }

            // Update team through TeamManager
            boolean success = teamManager.updateTeam(teamId, teamName, manager, playerCount);

            if (success) {
                JOptionPane.showMessageDialog(null, "Team updated successfully!", "Success",
                        JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(null, "Team not found!", "Error", JOptionPane.ERROR_MESSAGE);
            }
            return success;

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "Number of players must be a valid integer!", "Error",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    public boolean deleteTeam(String teamId) {
        if (teamId.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Team ID is required!", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        // Delete team through TeamManager
        boolean success = teamManager.deleteTeam(teamId);

        if (success) {
            JOptionPane.showMessageDialog(null, "Team deleted successfully!", "Success",
                    JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(null, "Team not found!", "Error", JOptionPane.ERROR_MESSAGE);
        }
        return success;
    }

    public void loadTeamsToTable(DefaultTableModel tableModel) {
        tableModel.setRowCount(0); // Clear existing rows

        for (Team team : teamManager.getTeams()) {
            Object[] row = {
                    team.getTeamId(),
                    team.getTeamName(),
                    team.getManager(),
                    team.getNoOfPlayers()
            };
            tableModel.addRow(row);
        }
    }

    public void loadTeamFromTable(String teamId, javax.swing.JTextField teamIdField,
            javax.swing.JTextField teamNameField,
            javax.swing.JTextField managerField,
            javax.swing.JTextField noPlayerField) {
        if (teamId.isEmpty()) {
            return;
        }

        Team team = teamManager.getTeamById(teamId);
        if (team != null) {
            teamIdField.setText(team.getTeamId());
            teamNameField.setText(team.getTeamName());
            managerField.setText(team.getManager());
            noPlayerField.setText(String.valueOf(team.getNoOfPlayers()));
        }
    }

    public Team getTeamById(String teamId) {
        return teamManager.getTeamById(teamId);
    }

    public void searchTeams(String criteria, String query, DefaultTableModel tableModel) {
        LinkedList<Team> results = teamManager.searchTeams(criteria, query);
        tableModel.setRowCount(0);
        for (Team team : results) {
            Object[] row = {
                    team.getTeamId(),
                    team.getTeamName(),
                    team.getManager(),
                    team.getNoOfPlayers()
            };
            tableModel.addRow(row);
        }
    }

    public void sortTeams(String criteria, DefaultTableModel tableModel) {
        teamManager.sortTeams(criteria);
        loadTeamsToTable(tableModel);
    }

    public LinkedList<Team> getTeams() {
        return teamManager.getTeams();
    }

    public int getTeamCount() {
        return teamManager.getTeamCount();
    }

    public boolean teamExists(String teamId) {
        return teamManager.teamExists(teamId);
    }
}