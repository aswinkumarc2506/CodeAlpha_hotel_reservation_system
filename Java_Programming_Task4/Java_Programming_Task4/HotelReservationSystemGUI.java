package Java_Programming_Task4;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.List;


public class HotelReservationSystemGUI {
    private List<Room> rooms;
    private List<Reservation> reservations;
    private static final String DATA_FILE = "reservations.dat";

    private JFrame frame;
    private JTable roomTable;
    private JTable reservationTable;
    private DefaultTableModel roomTableModel;
    private DefaultTableModel reservationTableModel;

    public HotelReservationSystemGUI() {
        rooms = new ArrayList<>();
        reservations = new ArrayList<>();
        initializeRooms();
        loadReservations();
        createAndShowGUI();
    }

    private void initializeRooms() {
        // Create some sample rooms
        for (int i = 1; i <= 10; i++) {
            RoomType type;
            if (i <= 4) type = RoomType.STANDARD;
            else if (i <= 8) type = RoomType.DELUXE;
            else type = RoomType.SUITE;

            rooms.add(new Room(i, type));
        }
    }

    private void loadReservations() {
        File file = new File(DATA_FILE);
        if (!file.exists()) {
            System.out.println("No previous reservations found.");
            return;
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(DATA_FILE))) {
            reservations = (List<Reservation>) ois.readObject();
            updateRoomAvailability();
        } catch (FileNotFoundException e) {
            System.out.println("No previous reservations found.");
        } catch (Exception e) {
            System.out.println("Error loading reservations: " + e.getMessage());
        }
    }

    private void saveReservations() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(DATA_FILE))) {
            oos.writeObject(reservations);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(frame, "Error saving reservations: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateRoomAvailability() {
        for (Room room : rooms) {
            room.setAvailable(true);
        }

        for (Reservation res : reservations) {
            for (Room room : rooms) {
                if (room.getRoomNumber() == res.getRoomNumber()) {
                    room.setAvailable(false);
                    break;
                }
            }
        }
    }

    private void createAndShowGUI() {
        frame = new JFrame("Hotel Reservation System");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(900, 600);
        frame.setLayout(new BorderLayout());

        // Create tabbed pane
        JTabbedPane tabbedPane = new JTabbedPane();

        // Available Rooms tab
        JPanel availableRoomsPanel = createAvailableRoomsPanel();
        tabbedPane.addTab("Available Rooms", availableRoomsPanel);

        // Make Reservation tab
        JPanel makeReservationPanel = createMakeReservationPanel();
        tabbedPane.addTab("Make Reservation", makeReservationPanel);

        // View Reservations tab
        JPanel viewReservationsPanel = createViewReservationsPanel();
        tabbedPane.addTab("View Reservations", viewReservationsPanel);

        // Cancel Reservation tab
        JPanel cancelReservationPanel = createCancelReservationPanel();
        tabbedPane.addTab("Cancel Reservation", cancelReservationPanel);

        frame.add(tabbedPane, BorderLayout.CENTER);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private JPanel createAvailableRoomsPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // Table column names
        String[] columnNames = {"Room Number", "Room Type", "Price per Night", "Availability"};
        roomTableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table non-editable
            }
        };
        roomTable = new JTable(roomTableModel);

        JScrollPane scrollPane = new JScrollPane(roomTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> refreshRoomTable());
        panel.add(refreshButton, BorderLayout.SOUTH);

        refreshRoomTable();
        return panel;
    }

    private void refreshRoomTable() {
        roomTableModel.setRowCount(0);
        for (Room room : rooms) {
            String availability = room.isAvailable() ? "Available" : "Occupied";
            roomTableModel.addRow(new Object[]{
                    room.getRoomNumber(),
                    room.getType(),
                    "$" + room.getType().getPrice(),
                    availability
            });
        }
    }

    private JPanel createMakeReservationPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Guest name field
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Guest Name:"), gbc);
        gbc.gridx = 1;
        JTextField guestNameField = new JTextField(20);
        panel.add(guestNameField, gbc);

        // Room number field
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Room Number:"), gbc);
        gbc.gridx = 1;
        JTextField roomNumberField = new JTextField(5);
        panel.add(roomNumberField, gbc);

        // Nights field
        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("Number of Nights:"), gbc);
        gbc.gridx = 1;
        JTextField nightsField = new JTextField(5);
        panel.add(nightsField, gbc);

        // Submit button
        gbc.gridx = 0; gbc.gridy = 3;
        gbc.gridwidth = 2;
        JButton submitButton = new JButton("Make Reservation");
        panel.add(submitButton, gbc);

        // Action listener for the submit button
        submitButton.addActionListener(e -> {
            try {
                String guestName = guestNameField.getText().trim();
                int roomNumber = Integer.parseInt(roomNumberField.getText().trim());
                int nights = Integer.parseInt(nightsField.getText().trim());

                if (guestName.isEmpty()) {
                    JOptionPane.showMessageDialog(frame, "Please enter a guest name",
                            "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                if (nights <= 0) {
                    JOptionPane.showMessageDialog(frame, "Number of nights must be positive",
                            "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                Room selectedRoom = null;
                for (Room room : rooms) {
                    if (room.getRoomNumber() == roomNumber && room.isAvailable()) {
                        selectedRoom = room;
                        break;
                    }
                }

                if (selectedRoom == null) {
                    JOptionPane.showMessageDialog(frame, "Room not available or invalid room number!",
                            "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                double totalPrice = selectedRoom.getType().getPrice() * nights;

                Reservation reservation = new Reservation(
                        guestName,
                        roomNumber,
                        selectedRoom.getType(),
                        new Date(), // current date as check-in
                        new Date(System.currentTimeMillis() + (long) nights * 24 * 60 * 60 * 1000), // check-out
                        totalPrice
                );

                reservations.add(reservation);
                selectedRoom.setAvailable(false);
                saveReservations();

                JOptionPane.showMessageDialog(frame,
                        "Reservation successful!\nReservation ID: " + reservation.getReservationId() +
                                "\nTotal Price: $" + totalPrice,
                        "Success", JOptionPane.INFORMATION_MESSAGE);

                // Clear fields
                guestNameField.setText("");
                roomNumberField.setText("");
                nightsField.setText("");

                // Refresh tables
                refreshRoomTable();
                refreshReservationTable();

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(frame, "Please enter valid numbers for room number and nights",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        return panel;
    }

    private JPanel createViewReservationsPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // Table column names
        String[] columnNames = {"Reservation ID", "Guest Name", "Room No", "Room Type", "Total Price"};
        reservationTableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table non-editable
            }
        };
        reservationTable = new JTable(reservationTableModel);

        JScrollPane scrollPane = new JScrollPane(reservationTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> refreshReservationTable());
        panel.add(refreshButton, BorderLayout.SOUTH);

        refreshReservationTable();
        return panel;
    }

    private void refreshReservationTable() {
        reservationTableModel.setRowCount(0);
        for (Reservation res : reservations) {
            reservationTableModel.addRow(new Object[]{
                    res.getReservationId(),
                    res.getGuestName(),
                    res.getRoomNumber(),
                    res.getRoomType(),
                    "$" + res.getTotalPrice()
            });
        }
    }

    private JPanel createCancelReservationPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Reservation ID field
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Reservation ID:"), gbc);
        gbc.gridx = 1;
        JTextField reservationIdField = new JTextField(10);
        panel.add(reservationIdField, gbc);

        // Cancel button
        gbc.gridx = 0; gbc.gridy = 1;
        gbc.gridwidth = 2;
        JButton cancelButton = new JButton("Cancel Reservation");
        panel.add(cancelButton, gbc);

        // Action listener for the cancel button
        cancelButton.addActionListener(e -> {
            String resId = reservationIdField.getText().trim();

            if (resId.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Please enter a reservation ID",
                        "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            Reservation toRemove = null;
            for (Reservation res : reservations) {
                if (res.getReservationId().equals(resId)) {
                    toRemove = res;
                    break;
                }
            }

            if (toRemove != null) {
                reservations.remove(toRemove);

                // Mark room as available again
                for (Room room : rooms) {
                    if (room.getRoomNumber() == toRemove.getRoomNumber()) {
                        room.setAvailable(true);
                        break;
                    }
                }

                saveReservations();
                JOptionPane.showMessageDialog(frame, "Reservation cancelled successfully!",
                        "Success", JOptionPane.INFORMATION_MESSAGE);
                reservationIdField.setText("");

                // Refresh tables
                refreshRoomTable();
                refreshReservationTable();
            } else {
                JOptionPane.showMessageDialog(frame, "Reservation not found!",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        return panel;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new HotelReservationSystemGUI());
    }
}