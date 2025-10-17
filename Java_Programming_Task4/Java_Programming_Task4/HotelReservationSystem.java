package Java_Programming_Task4;

import java.util.*;
import java.io.*;

enum RoomType {
    STANDARD(100.0), DELUXE(150.0), SUITE(250.0);

    private double price;

    RoomType(double price) {
        this.price = price;
    }

    public double getPrice() { return price; }
}

class Room {
    private int roomNumber;
    private RoomType type;
    private boolean isAvailable;

    public Room(int roomNumber, RoomType type) {
        this.roomNumber = roomNumber;
        this.type = type;
        this.isAvailable = true;
    }

    public int getRoomNumber() { return roomNumber; }
    public RoomType getType() { return type; }
    public boolean isAvailable() { return isAvailable; }
    public void setAvailable(boolean available) { isAvailable = available; }
}
class Reservation implements Serializable {
    private String guestName;
    private int roomNumber;
    private RoomType roomType;
    private Date checkInDate;
    private Date checkOutDate;
    private double totalPrice;
    private String reservationId;

    public Reservation(String guestName, int roomNumber, RoomType roomType,
                       Date checkInDate, Date checkOutDate, double totalPrice) {
        this.guestName = guestName;
        this.roomNumber = roomNumber;
        this.roomType = roomType;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
        this.totalPrice = totalPrice;
        this.reservationId = UUID.randomUUID().toString().substring(0, 8);
    }

    // Getters
    public String getReservationId() { return reservationId; }
    public String getGuestName() { return guestName; }
    public int getRoomNumber() { return roomNumber; }
    public RoomType getRoomType() { return roomType; }
    public double getTotalPrice() { return totalPrice; }
    public Date getCheckInDate() { return checkInDate; }
    public Date getCheckOutDate() { return checkOutDate; }
}

public class HotelReservationSystem {
    private List<Room> rooms;
    private List<Reservation> reservations;
    private Scanner scanner;
    private static final String DATA_FILE = "reservations.dat";

    public HotelReservationSystem() {
        rooms = new ArrayList<>();
        reservations = new ArrayList<>();
        scanner = new Scanner(System.in);
        initializeRooms();
        loadReservations();
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
            System.out.println("Error saving reservations: " + e.getMessage());
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

    public void searchAvailableRooms() {
        System.out.println("\n=== AVAILABLE ROOMS ===");
        System.out.printf("%-15s %-15s %-15s%n", "Room Number", "Room Type", "Price per Night");
        System.out.println("-------------------------------------------");

        boolean found = false;
        for (Room room : rooms) {
            if (room.isAvailable()) {
                System.out.printf("%-15d %-15s $%-14.2f%n",
                        room.getRoomNumber(),
                        room.getType(),
                        room.getType().getPrice());
                found = true;
            }
        }

        if (!found) {
            System.out.println("No available rooms found.");
        }
        System.out.println();
    }

    public void makeReservation() {
        searchAvailableRooms();

        System.out.print("Enter guest name: ");
        String guestName = scanner.nextLine();

        System.out.print("Enter room number to book: ");
        int roomNumber = scanner.nextInt();
        scanner.nextLine(); // consume newline

        Room selectedRoom = null;
        for (Room room : rooms) {
            if (room.getRoomNumber() == roomNumber && room.isAvailable()) {
                selectedRoom = room;
                break;
            }
        }

        if (selectedRoom == null) {
            System.out.println("Room not available or invalid room number!");
            return;
        }

        System.out.print("Enter number of nights: ");
        int nights = scanner.nextInt();
        scanner.nextLine(); // consume newline

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

        System.out.println("\nReservation successful!");
        System.out.println("Reservation ID: " + reservation.getReservationId());
        System.out.println("Total Price: $" + totalPrice);
        System.out.println();
    }

    public void cancelReservation() {
        System.out.print("Enter reservation ID to cancel: ");
        String resId = scanner.nextLine();

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
            System.out.println("Reservation cancelled successfully!\n");
        } else {
            System.out.println("Reservation not found!\n");
        }
    }

    public void viewReservations() {
        if (reservations.isEmpty()) {
            System.out.println("No reservations found!\n");
            return;
        }

        System.out.println("\n=== ALL RESERVATIONS ===");
        System.out.printf("%-12s %-20s %-10s %-15s %-10s%n",
                "Res ID", "Guest Name", "Room No", "Room Type", "Total Price");
        System.out.println("----------------------------------------------------------------");

        for (Reservation res : reservations) {
            System.out.printf("%-12s %-20s %-10d %-15s $%-9.2f%n",
                    res.getReservationId(),
                    res.getGuestName(),
                    res.getRoomNumber(),
                    res.getRoomType(),
                    res.getTotalPrice());
        }
        System.out.println();
    }

    public void run() {
        while (true) {
            System.out.println("=== HOTEL RESERVATION SYSTEM ===");
            System.out.println("1. Search Available Rooms");
            System.out.println("2. Make Reservation");
            System.out.println("3. Cancel Reservation");
            System.out.println("4. View All Reservations");
            System.out.println("5. Exit");
            System.out.print("Choose an option: ");

            int choice = scanner.nextInt();
            scanner.nextLine(); // consume newline

            switch (choice) {
                case 1:
                    searchAvailableRooms();
                    break;
                case 2:
                    makeReservation();
                    break;
                case 3:
                    cancelReservation();
                    break;
                case 4:
                    viewReservations();
                    break;
                case 5:
                    System.out.println("Thank you for using Hotel Reservation System!");
                    return;
                default:
                    System.out.println("Invalid option! Please try again.\n");
            }
        }
    }

    public static void main(String[] args) {
        HotelReservationSystem system = new HotelReservationSystem();
        system.run();
    }
}