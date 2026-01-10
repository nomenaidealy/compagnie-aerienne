package main.project.servlets;

import main.project.beans.*;
import main.project.utils.DBConnection;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.sql.*;
import java.util.*;

@WebServlet("/bookings")
public class BookingServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String action = request.getParameter("action");
        if (action == null) action = "list";

        switch (action) {
            case "new":
                showNewBookingForm(request, response);
                break;
            case "selectFlight":
                showFlightSelection(request, response);
                break;
            case "cancel":
                cancelBooking(request, response);
                break;
            default:
                listBookings(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String action = request.getParameter("action");

        switch (action) {
            case "createPassenger":
                createPassenger(request, response);
                break;
            case "createBooking":
                createBooking(request, response);
                break;
            case "createMultipleBookings":
                createMultipleBookings(request, response);
                break;
            case "searchPassenger":
                searchPassenger(request, response);
                break;
        }
    }

    // ========================== LIST BOOKINGS ==========================
    private void listBookings(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        List<Booking> bookings = new ArrayList<>();
        String sql = "SELECT b.*, p.*, fi.*, fr.*, da.code AS dep_code, da.city AS dep_city, " +
                     "aa.code AS arr_code, aa.city AS arr_city, ac.registration " +
                     "FROM bookings b " +
                     "JOIN passengers p ON b.passenger_id = p.id " +
                     "JOIN flight_instances fi ON b.flight_instance_id = fi.id " +
                     "JOIN flight_routes fr ON fi.route_id = fr.id " +
                     "JOIN airports da ON fr.departure_airport_id = da.id " +
                     "JOIN airports aa ON fr.arrival_airport_id = aa.id " +
                     "LEFT JOIN aircrafts ac ON fi.aircraft_id = ac.id " +
                     "ORDER BY b.booking_date DESC";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Booking b = new Booking();
                b.setId(rs.getInt("id"));
                b.setBookingReference(rs.getString("booking_reference"));
                b.setSeatNumber(rs.getString("seat_number"));
                b.setBookingDate(rs.getTimestamp("booking_date"));
                b.setTotalAmount(rs.getDouble("total_amount"));
                b.setStatus(rs.getString("status"));

                Passenger p = new Passenger();
                p.setId(rs.getInt("passenger_id"));
                p.setFirstName(rs.getString("first_name"));
                p.setLastName(rs.getString("last_name"));
                p.setEmail(rs.getString("email"));
                b.setPassenger(p);

                Flight f = new Flight();
                f.setId(rs.getInt("flight_instance_id"));
                f.setFlightNumber(rs.getString("flight_number"));
                f.setDepartureTime(rs.getTimestamp("departure_time"));
                f.setArrivalTime(rs.getTimestamp("arrival_time"));
                f.setBasePrice(rs.getDouble("base_price"));

                Airport dep = new Airport();
                dep.setCode(rs.getString("dep_code"));
                dep.setCity(rs.getString("dep_city"));
                f.setDepartureAirport(dep);

                Airport arr = new Airport();
                arr.setCode(rs.getString("arr_code"));
                arr.setCity(rs.getString("arr_city"));
                f.setArrivalAirport(arr);

                Aircraft ac = new Aircraft();
                ac.setRegistration(rs.getString("registration"));
                f.setAircraft(ac);

                b.setFlight(f);
                bookings.add(b);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        request.setAttribute("bookings", bookings);
        request.getRequestDispatcher("/WEB-INF/jsp/bookings/listBookings.jsp").forward(request, response);
    }

    // ========================== NEW BOOKING FORM ==========================
    private void showNewBookingForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("/WEB-INF/jsp/bookings/newBooking.jsp").forward(request, response);
    }

    // ========================== SELECT FLIGHT ==========================
    private void showFlightSelection(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        int passengerId = Integer.parseInt(request.getParameter("passengerId"));

        try (Connection conn = DBConnection.getConnection()) {
            // Charger le passager
            String sql = "SELECT * FROM passengers WHERE id = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, passengerId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        Passenger p = new Passenger(
                                rs.getInt("id"),
                                rs.getString("first_name"),
                                rs.getString("last_name"),
                                rs.getString("email"),
                                rs.getString("phone"),
                                rs.getString("passport_number"),
                                rs.getDate("date_of_birth")
                        );
                        request.setAttribute("passenger", p);
                    }
                }
            }

            // Charger les vols
            loadAvailableFlights(request);

        } catch (SQLException e) {
            e.printStackTrace();
        }

        request.getRequestDispatcher("/WEB-INF/jsp/bookings/selectFlight.jsp").forward(request, response);
    }

    // ========================== CREATE PASSENGER ==========================
    private void createPassenger(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        try (Connection conn = DBConnection.getConnection()) {
            String sql = "INSERT INTO passengers (first_name,last_name,email,phone,passport_number,date_of_birth) VALUES (?,?,?,?,?,?)";
            try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, request.getParameter("firstName"));
                ps.setString(2, request.getParameter("lastName"));
                ps.setString(3, request.getParameter("email"));
                ps.setString(4, request.getParameter("phone"));
                ps.setString(5, request.getParameter("passportNumber"));
                ps.setDate(6, java.sql.Date.valueOf(request.getParameter("dateOfBirth")));
                ps.executeUpdate();

                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        int passengerId = rs.getInt(1);
                        response.sendRedirect("bookings?action=selectFlight&passengerId=" + passengerId);
                        return;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            request.getSession().setAttribute("errorMessage", "Erreur création passager: " + e.getMessage());
            response.sendRedirect("bookings?action=new");
        }
    }

    // ========================== SEARCH PASSENGER ==========================
    private void searchPassenger(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String passportNumber = request.getParameter("passportNumber");
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT id FROM passengers WHERE passport_number = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, passportNumber);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        response.sendRedirect("bookings?action=selectFlight&passengerId=" + rs.getInt("id"));
                        return;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        request.setAttribute("errorMessage", "Aucun passager trouvé avec ce numéro de passeport");
        request.getRequestDispatcher("/WEB-INF/jsp/bookings/newBooking.jsp").forward(request, response);
    }

    // ========================== CREATE SINGLE BOOKING ==========================
    private void createBooking(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        int passengerId = Integer.parseInt(request.getParameter("passengerId"));
        int flightInstanceId = Integer.parseInt(request.getParameter("flightInstanceId"));
        String seatNumber = request.getParameter("seatNumber");

        try (Connection conn = DBConnection.getConnection()) {
            // Si random seat
            if ("random".equalsIgnoreCase(seatNumber)) {
                seatNumber = getRandomAvailableSeat(conn, flightInstanceId);
                if (seatNumber == null) {
                    request.getSession().setAttribute("errorMessage", "Aucun siège disponible");
                    response.sendRedirect("bookings?action=selectFlight&passengerId=" + passengerId);
                    return;
                }
            }

            // Prix du vol
            double price = 0;
            String sqlPrice = "SELECT base_price FROM flight_instances WHERE id = ?";
            try (PreparedStatement ps = conn.prepareStatement(sqlPrice)) {
                ps.setInt(1, flightInstanceId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) price = rs.getDouble("base_price");
                }
            }

            // Référence réservation
            String bookingRef = generateBookingReference(conn);

            String sqlBooking = "INSERT INTO bookings (booking_reference, flight_instance_id, passenger_id, seat_number, total_amount, status) VALUES (?,?,?,?,?, 'confirmed')";
            try (PreparedStatement ps = conn.prepareStatement(sqlBooking)) {
                ps.setString(1, bookingRef);
                ps.setInt(2, flightInstanceId);
                ps.setInt(3, passengerId);
                ps.setString(4, seatNumber);
                ps.setDouble(5, price);
                ps.executeUpdate();
            }

            request.getSession().setAttribute("successMessage", "Réservation créée! Réf: " + bookingRef);
            response.sendRedirect("bookings");

        } catch (SQLException e) {
            e.printStackTrace();
            request.getSession().setAttribute("errorMessage", "Erreur création réservation: " + e.getMessage());
            response.sendRedirect("bookings");
        }
    }

    // ========================== CREATE MULTIPLE BOOKINGS ==========================
    private void createMultipleBookings(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        int passengerId = Integer.parseInt(request.getParameter("passengerId"));
        int flightInstanceId = Integer.parseInt(request.getParameter("flightInstanceId"));
        String[] seatNumbers = request.getParameterValues("seatNumbers");

        HttpSession session = request.getSession();

        if (seatNumbers == null || seatNumbers.length == 0) {
            session.setAttribute("errorMessage", "Aucun siège sélectionné");
            response.sendRedirect("bookings?action=selectFlight&passengerId=" + passengerId);
            return;
        }

        try (Connection conn = DBConnection.getConnection()) {
            // Prix du vol
            double price = 0;
            String sqlPrice = "SELECT base_price FROM flight_instances WHERE id = ?";
            try (PreparedStatement ps = conn.prepareStatement(sqlPrice)) {
                ps.setInt(1, flightInstanceId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) price = rs.getDouble("base_price");
                }
            }

            int successCount = 0;
            int failCount = 0;
            List<String> bookingRefs = new ArrayList<>();
            StringBuilder errorDetails = new StringBuilder();

            for (String seat : seatNumbers) {
                try {
                    seat = seat.trim();
                    if (seat.isEmpty() || "random".equalsIgnoreCase(seat)) {
                        seat = getRandomAvailableSeat(conn, flightInstanceId);
                        if (seat == null) { failCount++; errorDetails.append("Aucun siège disponible; "); continue; }
                    } else if (isSeatOccupied(conn, flightInstanceId, seat)) {
                        failCount++; errorDetails.append(seat).append(" déjà occupé; "); continue;
                    }

                    String ref = generateBookingReference(conn);
                    String sql = "INSERT INTO bookings (booking_reference, flight_instance_id, passenger_id, seat_number, total_amount, status) VALUES (?,?,?,?,?, 'confirmed')";
                    try (PreparedStatement ps = conn.prepareStatement(sql)) {
                        ps.setString(1, ref);
                        ps.setInt(2, flightInstanceId);
                        ps.setInt(3, passengerId);
                        ps.setString(4, seat);
                        ps.setDouble(5, price);
                        ps.executeUpdate();
                        bookingRefs.add(ref);
                        successCount++;
                    }
                } catch (SQLException e) {
                    failCount++;
                    errorDetails.append(seat).append(": ").append(e.getMessage()).append("; ");
                }
            }

            if (successCount > 0) {
                String msg = "✅ " + successCount + " réservation(s) créée(s)";
                if (!bookingRefs.isEmpty()) msg += " | Réf: " + String.join(", ", bookingRefs);
                if (failCount > 0) msg += " ⚠️ (" + failCount + " échec(s))";
                session.setAttribute("successMessage", msg);
            }
            if (failCount > 0 && successCount == 0) session.setAttribute("errorMessage", "❌ Échec: " + errorDetails.toString());

            response.sendRedirect("bookings");

        } catch (SQLException e) {
            e.printStackTrace();
            session.setAttribute("errorMessage", "Erreur BD: " + e.getMessage());
            response.sendRedirect("bookings");
        }
    }

    // ========================== CANCEL BOOKING ==========================
    private void cancelBooking(HttpServletRequest request, HttpServletResponse response) throws IOException {
        int id = Integer.parseInt(request.getParameter("id"));
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "UPDATE bookings SET status='cancelled' WHERE id=?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, id);
                ps.executeUpdate();
            }
            request.getSession().setAttribute("successMessage", "Réservation annulée avec succès");
        } catch (SQLException e) {
            e.printStackTrace();
            request.getSession().setAttribute("errorMessage", "Erreur lors de l'annulation");
        }
        response.sendRedirect("bookings");
    }

    // ========================== UTILITAIRES ==========================
    private boolean isSeatOccupied(Connection conn, int flightInstanceId, String seatNumber) throws SQLException {
        String sql = "SELECT COUNT(*) FROM bookings WHERE flight_instance_id=? AND seat_number=? AND status='confirmed'";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, flightInstanceId);
            ps.setString(2, seatNumber);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    private String getRandomAvailableSeat(Connection conn, int flightInstanceId) throws SQLException {
        String sqlSeats = "SELECT ac.total_seats FROM flight_instances fi JOIN aircrafts ac ON fi.aircraft_id=ac.id WHERE fi.id=?";
        int totalSeats = 0;
        try (PreparedStatement ps = conn.prepareStatement(sqlSeats)) {
            ps.setInt(1, flightInstanceId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) totalSeats = rs.getInt("total_seats");
            }
        }
        if (totalSeats == 0) return null;

        List<String> occupied = new ArrayList<>();
        String sqlOcc = "SELECT seat_number FROM bookings WHERE flight_instance_id=? AND status='confirmed'";
        try (PreparedStatement ps = conn.prepareStatement(sqlOcc)) {
            ps.setInt(1, flightInstanceId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) occupied.add(rs.getString("seat_number"));
            }
        }

        Random r = new Random();
        for (int i = 0; i < 100; i++) {
            int row = r.nextInt(totalSeats / 6) + 1;
            char seat = (char) ('A' + r.nextInt(6));
            String s = row + "" + seat;
            if (!occupied.contains(s)) return s;
        }
        return null;
    }

    private String generateBookingReference(Connection conn) throws SQLException {
        Random r = new Random();
        String ref;
        while (true) {
            ref = "AIR" + 2026 + String.format("%05d", r.nextInt(100000));
            String sql = "SELECT COUNT(*) FROM bookings WHERE booking_reference=?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, ref);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next() && rs.getInt(1) == 0) return ref;
                }
            }
        }
    }

    private void loadAvailableFlights(HttpServletRequest request) {
        List<FlightInstance> flights = new ArrayList<>();
        String sql = "SELECT fi.*, fr.flight_number, fr.departure_airport_id, fr.arrival_airport_id, " +
                     "da.code dep_code, da.name dep_name, da.city dep_city, " +
                     "aa.code arr_code, aa.name arr_name, aa.city arr_city, " +
                     "ac.id aircraft_id, ac.registration, ac.model, ac.total_seats " +
                     "FROM flight_instances fi " +
                     "JOIN flight_routes fr ON fi.route_id=fr.id " +
                     "JOIN airports da ON fr.departure_airport_id=da.id " +
                     "JOIN airports aa ON fr.arrival_airport_id=aa.id " +
                     "LEFT JOIN aircrafts ac ON fi.aircraft_id=ac.id " +
                     "WHERE fi.status='scheduled' AND fi.departure_time>CURRENT_TIMESTAMP " +
                     "ORDER BY fi.departure_time";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                FlightInstance fi = new FlightInstance();
                fi.setId(rs.getInt("id"));
                fi.setRouteId(rs.getInt("route_id"));
                fi.setDepartureTime(rs.getTimestamp("departure_time"));
                fi.setArrivalTime(rs.getTimestamp("arrival_time"));
                fi.setFlightDate(rs.getDate("flight_date"));
                fi.setBasePrice(rs.getDouble("base_price"));
                fi.setStatus(rs.getString("status"));

                FlightRoute fr = new FlightRoute(
                        rs.getInt("route_id"),
                        rs.getString("flight_number"),
                        rs.getInt("departure_airport_id"),
                        rs.getInt("arrival_airport_id")
                );

                fr.setDepartureAirport(new Airport(rs.getInt("departure_airport_id"), rs.getString("dep_code"), rs.getString("dep_name"), rs.getString("dep_city"), null));
                fr.setArrivalAirport(new Airport(rs.getInt("arrival_airport_id"), rs.getString("arr_code"), rs.getString("arr_name"), rs.getString("arr_city"), null));

                fi.setRoute(fr);

                if (rs.getInt("aircraft_id") != 0) {
                    fi.setAircraft(new Aircraft(
                            rs.getInt("aircraft_id"),
                            rs.getString("registration"),
                            rs.getString("model"),
                            rs.getInt("total_seats")
                    ));
                }

                flights.add(fi);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        request.setAttribute("flightInstances", flights);
    }
}
