import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.*;

public class BugTrackingSystems extends JFrame {

    private DefaultListModel<Bug> bugListModel;
    private JTextField bugTextField;

    // MySQL database configurations
    private static final String JDBC_URL = "jdbc:mysql://localhost:3306/mybug";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "root123";

    public BugTrackingSystems() {
        super("Bug Tracking System");

        bugListModel = new DefaultListModel<>();
        JList<Bug> bugList = new JList<>(bugListModel);

        bugTextField = new JTextField(20);
        JButton addButton = new JButton("Add Bug");
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        addBug();
                    }
                });
            }
        });

        JButton checkStatusButton = new JButton("Check Status");
        checkStatusButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        checkBugStatus(bugList.getSelectedValue());
                    }
                });
            }
        });

        JButton updateButton = new JButton("Update Bug");
        updateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        updateBug(bugList.getSelectedValue());
                    }
                });
            }
        });

        JButton deleteButton = new JButton("Delete Bug");
        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        deleteBug(bugList.getSelectedValue());
                    }
                });
            }
        });

        JPanel inputPanel = new JPanel();
        inputPanel.add(new JLabel("Enter Bug: "));
        inputPanel.add(bugTextField);
        inputPanel.add(addButton);
        inputPanel.add(checkStatusButton);
        inputPanel.add(updateButton);
        inputPanel.add(deleteButton);

        setLayout(new BorderLayout());
        add(new JScrollPane(bugList), BorderLayout.CENTER);
        add(inputPanel, BorderLayout.SOUTH);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 400);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void addBug() {
        String bugDescription = bugTextField.getText();
        if (!bugDescription.isEmpty()) {
            // Add the bug to the list
            Bug bug = new Bug(bugDescription);
            bugListModel.addElement(bug);

            // Save the bug to the database
            saveBugToDatabase(bug);

            bugTextField.setText("");
        } else {
            JOptionPane.showMessageDialog(this, "Please enter a bug description.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void saveBugToDatabase(Bug bug) {
        try (Connection connection = DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD)) {
            String sql = "INSERT INTO bugs (description, status) VALUES (?, ?)";
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setString(1, bug.getDescription());
                preparedStatement.setString(2, bug.getStatus().toString());
                preparedStatement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error saving bug to the database.", "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void checkBugStatus(Bug bug) {
        if (bug != null) {
            JOptionPane.showMessageDialog(this, "Bug Status: " + bug.getStatus(), "Bug Status",
                    JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "Please select a bug.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateBug(Bug bug) {
        if (bug != null) {
            String newStatus = JOptionPane.showInputDialog(this, "Enter new status for the bug:");
            if (newStatus != null) {
                bug.setStatus(Bug.Status.valueOf(newStatus.toUpperCase()));
                updateBugInDatabase(bug);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select a bug.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateBugInDatabase(Bug bug) {
        try (Connection connection = DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD)) {
            String sql = "UPDATE bugs SET status = ? WHERE description = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setString(1, bug.getStatus().toString());
                preparedStatement.setString(2, bug.getDescription());
                preparedStatement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error updating bug in the database.", "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteBug(Bug bug) {
        if (bug != null) {
            int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this bug?",
                    "Confirm Deletion", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                bugListModel.removeElement(bug);
                deleteBugFromDatabase(bug);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select a bug.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteBugFromDatabase(Bug bug) {
        try (Connection connection = DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD)) {
            String sql = "DELETE FROM bugs WHERE description = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setString(1, bug.getDescription());
                preparedStatement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error deleting bug from the database.", "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        // Load the MySQL JDBC driver
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            System.exit(1);
        }

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new BugTrackingSystems();
            }
        });
    }

    public static class Bug {
        private String description;
        private Status status;

        public Bug(String description) {
            this.description = description;
            this.status = Status.NEW;
        }

        public String getDescription() {
            return description;
        }

        public Status getStatus() {
            return status;
        }

        public void setStatus(Status status) {
            this.status = status;
        }

        @Override
        public String toString() {
            return description;
        }

        public enum Status {
            NEW,
            IN_PROGRESS,
            RESOLVED
        }
    }
}
