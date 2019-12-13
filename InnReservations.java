import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

import java.util.*;
import java.time.LocalDate;

class InnReservations {
    int codenum = 0;
    public static void main(String[] args) 
    {
        try 
        {
            InnReservations ir = new InnReservations();
            ir.askUser();
        } 
        catch (SQLException e) 
        {
            System.err.println("SQLException: " + e.getMessage());
        }
    }

    private void askUser() throws SQLException 
    {
        Scanner sc = new Scanner(System.in);
        String answer = "";
        while (!answer.toLowerCase().equals("q")) 
        {
            System.out.println("\nChoose Option: \n");
            System.out.println("[G]et Rooms and Rates");
            System.out.println("[F]ind Available Rooms");
            System.out.println("[C]hange Reservation");
            System.out.println("[Ca]ncel Reservation");
            System.out.println("[S]earch Reservations");
            System.out.println("[R]evenue by Month");
            System.out.println("[Q]uit Program\n");
            answer = sc.nextLine();
            if (answer.toLowerCase().equals("g")) 
            {
                RoomsAndRates();
            } 
            else if (answer.toLowerCase().equals("f")) 
            {
                Reservations();
            } 
            else if (answer.toLowerCase().equals("c")) 
            {
                ReservationChange();
            } 
            else if (answer.toLowerCase().equals("ca")) 
            {
                ReservationCancellation();
            } 
            else if (answer.toLowerCase().equals("s")) 
            {
                SearchReservations();
            } 
            else if (answer.toLowerCase().equals("r")) 
            {
                RevenueByMonth();
            }
        }
    }

    private void RoomsAndRates() throws SQLException 
    {
        String sql = "SELECT rp.Room, Popularity, NextAvailable, LengthStay, mp.CheckOut  FROM\n" +
                "(SELECT r.Room, MAX(r.CheckOut),\n" +
                "ROUND(SUM(DATEDIFF(LEAST(r.CheckOut, CurDate()),\n" +
                "    GREATEST(r.CheckIn, CurDate() - INTERVAL 180 DAY)))/180, 2) AS Popularity\n" +
                "FROM lab7_reservations r\n" +
                "JOIN lab7_rooms rm ON r.Room = rm.RoomCode\n" +
                "WHERE r.CheckOut >= (CurDate() - INTERVAL 180 DAY)\n" +
                "AND r.CheckIn <= CurDate()\n" +
                "GROUP BY r.Room) rp\n" +
                " \n" +
                "JOIN\n" +
                " \n" +
                "(SELECT r.Room, MAX(r.CheckOut) + INTERVAL 1 DAY AS NextAvailable\n" +
                "FROM lab7_reservations r\n" +
                "JOIN lab7_rooms rm ON r.Room = rm.RoomCode\n" +
                "GROUP BY r.Room) av\n" +
                " \n" +
                "ON rp.Room = av.Room\n" +
                " \n" +
                "JOIN\n" +
                " \n" +
                "(SELECT t1.Room, DATEDIFF(t1.CheckOut, t1.CheckIn) AS LengthStay, t1.Checkout FROM\n" +
                "(SELECT *\n" +
                "FROM lab7_reservations r\n" +
                "JOIN lab7_rooms rm ON r.Room = rm.RoomCode) t1\n" +
                " \n" +
                "JOIN\n" +
                " \n" +
                "(SELECT r.Room, MAX(r.CheckOut) AS MostRecent\n" +
                "FROM lab7_reservations r\n" +
                "JOIN lab7_rooms rm ON r.Room = rm.RoomCode\n" +
                "WHERE r.CheckOut >= (CurDate() - INTERVAL 180 DAY)\n" +
                "AND r.CheckIn <= CurDate()\n" +
                "GROUP BY r.Room) t2\n" +
                "ON t1.Room = t2.Room AND t1.CheckOut = t2.MostRecent) mp\n" +
                " \n" +
                "ON rp.Room = mp.Room\n" +
                " \n" +
                "ORDER BY Popularity DESC;";
        String url = System.getenv("HP_JDBC_URL");
        String user = System.getenv("HP_JDBC_USER");
        String pass = System.getenv("HP_JDBC_PW");
        try (Connection conn = DriverManager.getConnection(url, user, pass)) 
        {
            System.out.println("Database connection acquired - processing query");
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) 
            {
                System.out.println("RoomCode Popularity NextAvailable LengthStay Checkout");
                // Step 5: Receive results
                while (rs.next()) 
                {
                    String room = rs.getString("Room");
                    float popularity = rs.getFloat("Popularity");
                    Date nextAvailable = rs.getDate("NextAvailable");
                    int lengthStay = rs.getInt("LengthStay");
                    Date checkout = rs.getDate("Checkout");
                    System.out.format("%s      %.2f       %s    %d          %s%n", room, popularity,
                            nextAvailable, lengthStay, checkout);
                }
            } 
            catch (SQLException e) 
            {
                System.err.println("SQLException: " + e.getMessage());
            }
        } 
        catch (SQLException e) 
        {
            System.err.println("SQLException: " + e.getMessage());
        }
    }

    private void Reservations() throws SQLException 
    {
        String url = System.getenv("HP_JDBC_URL");
        String user = System.getenv("HP_JDBC_USER");
        String pass = System.getenv("HP_JDBC_PW");
        Scanner sc = new Scanner(System.in);
        System.out.println("Please Enter First Name: ");
        String firstName = sc.nextLine();
        System.out.println("Last Name: ");
        String lastName = sc.nextLine();
        System.out.println("Desired Room Code (or 'Any'): ");
        String roomCode = sc.nextLine();
        System.out.println("Desired Bed Type (or 'Any'): ");
        String bedType = sc.nextLine();
        System.out.println("Beginning Date of Stay: ");
        String beginDate = sc.nextLine();
        System.out.println("End Date of Stay: ");
        String endDate = sc.nextLine();
        System.out.println("Number of Children: ");
        String numChildren = sc.nextLine();
        System.out.println("Number of Adults: ");
        String numAdults = sc.nextLine();
        List<Object> params = new ArrayList<Object>();
        int total_staying = Integer.parseInt(numChildren) + Integer.parseInt(numAdults);
        params.add(total_staying);
        params.add(total_staying);
        params.add(beginDate);
        params.add(endDate);
        StringBuilder sb = new StringBuilder("select rm.roomcode AS RoomCode\n" +
                "from lab7_rooms as rm\n" +
                "where rm.maxocc >= ?\n" +
                "and rm.roomcode not in (\n" +
                "select rv.room\n" +
                "from lab7_rooms as rm\n" +
                "join lab7_reservations as rv\n" +
                "on rm.roomcode = rv.room\n" +
                "where rm.maxOcc >= ?\n" +
                "and rv.checkout > ?\n" +
                "and rv.checkin < ?\n");

        if (!"any".equalsIgnoreCase(roomCode)) 
        {
            sb.append(" AND rm.roomcode = ?");
            params.add(roomCode);
        }
        if (!"any".equalsIgnoreCase(bedType)) 
        {
            sb.append(" AND rm.bedtype = ?");
            params.add(bedType);
        }

        sb.append("group by rv.room);\n");

        if (total_staying > 4) 
        {
            System.out.println("There are no rooms for this many people, please try separate reservations");
            return;
        }

        try (Connection conn = DriverManager.getConnection(url, user, pass)) 
        {
            System.out.println("Database connection acquired - processing query");
            try (PreparedStatement pstmt = conn.prepareStatement(sb.toString())) 
            {
                int i = 1;
                for (Object p : params) 
                {
                    pstmt.setObject(i++, p);
                }
                try (ResultSet rs = pstmt.executeQuery()) 
                {
                    System.out.println("Available Rooms:\n");
                    int ind = 0;
                    List<String> rooms = new ArrayList<String>();
                    while (rs.next()) 
                    {
                        ind++;
                        String availableRoom = rs.getString("RoomCode");
                        rooms.add(availableRoom);
                        System.out.format("%d. %s      %n", ind, availableRoom);
                    }

                    if (ind > 0) 
                    {
                        System.out.println("Please Select An Above Room to Book by Number (or 0 to cancel): ");
                        int selection = Integer.parseInt(sc.nextLine());
                        if ((selection > 0) & (selection <= ind)) 
                        {
                            System.out.println(rooms.get(selection - 1));
                        }
                    } 
                    else 
                    {
                        List<Object> params2 = new ArrayList<Object>();

                        System.out.println("No options available, here are some other options: ");
                        StringBuilder sb2 = new StringBuilder("select x.room as RoomToStayIn, x.checkout as DayYouCanCheckin,\n" +
                                "y.checkin as DayYouCanCheckout\n" +
                                "from\n" +
                                "(select rm.maxocc, rv.room, rv.checkin, rv.checkout,\n" +
                                "rank() over (partition by rv.room order by rv.checkin) as DayRank\n" +
                                "from lab7_rooms as rm\n" +
                                "join lab7_reservations as rv\n" +
                                "on rm.roomcode = rv.room\n");

                        if (!"any".equalsIgnoreCase(roomCode)) 
                        {
                            sb.append("where rv.room = ?");
                            params2.add(roomCode);
                        }
                        sb.append(
                                ") as x\n" +
                                        "join\n" +
                                        "(select rm.maxocc, rv.room, rv.checkin, rv.checkout,\n" +
                                        "rank() over (partition by rv.room order by rv.checkin) as DayRank\n" +
                                        "from lab7_rooms as rm\n" +
                                        "join lab7_reservations as rv\n" +
                                        "on rm.roomcode = rv.room\n");

                        if (!"any".equalsIgnoreCase(roomCode)) 
                        {
                            sb.append("where rv.room = ?");
                            params2.add(roomCode);
                        }
                        sb.append(
                                ") as y\n" +
                                        "on x.room = y.room\n" +
                                        "and x.dayrank + 1= y.dayrank\n" +
                                        "and x.maxocc = y.maxocc\n" +
                                        "where x.maxocc >= ?\n" +
                                        "and x.checkout > ?\n" +
                                        "limit 5\n" +
                                        ";");
                        try (PreparedStatement pstmt2 = conn.prepareStatement(sb2.toString())) 
                        {
                            params2.add(total_staying);
                            params2.add(endDate);
                            int j = 1;
                            for (Object p : params2) 
                            {
                                pstmt2.setObject(j++, p);
                            }
                        }
                        try (ResultSet rs2 = pstmt.executeQuery()) 
                        {
                            int ii = 0;
                            while (rs.next()) 
                            {
                                ii++;
                                System.out.println(ii);
                                System.out.print(rs2);
                            }
                        }
                        //get user input of number

                        //confirm it
                        boolean confirm = true;
                        if (confirm == true) {
                            StringBuilder sb3 = new StringBuilder("INSERT INTO lab7_reservations * VALUES (?,?,?,?,?,?,?,?,?");
                            try (PreparedStatement pstmt3 = conn.prepareStatement(sb3.toString())) 
                            {
                                pstmt3.setObject(1, codenum);
                                codenum++;
                                pstmt3.setObject(2, roomCode);
                            }
                        }
                    }
                    }
                }
            }
          catch (SQLException e) {
            System.err.println("SQLException: " + e.getMessage());
        }
    }


    private void ReservationChange() throws SQLException {
        String url = System.getenv("HP_JDBC_URL");
        String user = System.getenv("HP_JDBC_USER");
        String pass = System.getenv("HP_JDBC_PW");
        Scanner sc = new Scanner(System.in);
        List<String> variables = new ArrayList<String>();
        List<String> updates = new ArrayList<String>();
        variables.add("FirstName");
        variables.add("LastName");
        variables.add("Checkin");
        variables.add("Checkout");
        variables.add("Adults");
        variables.add("Children");
        System.out.println("Please Enter Your Reservation Number: ");
        int resCode = Integer.parseInt(sc.nextLine());
        System.out.println("What would you like to change First Name to? (Leave Blank if no change)");
        updates.add(sc.nextLine());
        System.out.println("What would you like to change Last Name to? (Leave Blank if no change)");
        updates.add(sc.nextLine());
        System.out.println("What would you like to change Begin Date to? (Leave Blank if no change)");
        updates.add(sc.nextLine());
        System.out.println("What would you like to change End Date to? (Leave Blank if no change)");
        updates.add(sc.nextLine());
        System.out.println("What would you like to change Number of Children to? (Leave Blank if no change)");
        updates.add(sc.nextLine());
        System.out.println("What would you like to change Number of Adults to? (Leave Blank if no change)");
        updates.add(sc.nextLine());
        if (Integer.parseInt(updates.get(4)) + Integer.parseInt(updates.get(5)) > 4) {
            System.out.println("Too Many People In Room, Please Try Again");
            return;
        }
        try{
            Class.forName("com.mysql.jdbc.Driver");
            Connection conn = DriverManager.getConnection(url, user, pass);
            System.out.println("Database connection acquired - processing query");
                StringBuilder sb = new StringBuilder("UPDATE lab7_reservations SET FirstName = ? WHERE Code = ?");
                try (PreparedStatement pstmt = conn.prepareStatement(sb.toString())) {
                    if (!updates.get(0).equals("")) {
                        pstmt.setObject(1, updates.get(0));
                        pstmt.setObject(2, resCode);
                        pstmt.executeUpdate();
                    }
                }
                sb = new StringBuilder("UPDATE lab7_reservations SET LastName = ? WHERE Code = ?");
                try (PreparedStatement pstmt = conn.prepareStatement(sb.toString())) {
                    if (!updates.get(1).equals("")) {
                        pstmt.setObject(1, updates.get(1));
                        pstmt.setObject(2, resCode);
                        pstmt.executeUpdate();
                    }
                }
                sb = new StringBuilder("UPDATE lab7_reservations SET Checkin = ? WHERE Code = ?");
                try (PreparedStatement pstmt = conn.prepareStatement(sb.toString())) {
                    if (!updates.get(2).equals("")) {
                        pstmt.setObject(1, updates.get(2));
                        pstmt.setObject(2, resCode);
                        pstmt.executeUpdate();
                    }
                }
                sb = new StringBuilder("UPDATE lab7_reservations SET Checkout = ? WHERE Code = ?");
                try (PreparedStatement pstmt = conn.prepareStatement(sb.toString())) {
                    if (!updates.get(3).equals("")) {
                        pstmt.setObject(1, updates.get(3));
                        pstmt.setObject(2, resCode);
                        pstmt.executeUpdate();
                    }
                }
                sb = new StringBuilder("UPDATE lab7_reservations SET Adults = ? WHERE Code = ?");
                try (PreparedStatement pstmt = conn.prepareStatement(sb.toString())) {
                    if (!updates.get(4).equals("")) {
                        pstmt.setObject(1, updates.get(4));
                        pstmt.setObject(2, resCode);
                        pstmt.executeUpdate();
                    }
                }
                sb = new StringBuilder("UPDATE lab7_reservations SET Kids = ? WHERE Code = ?");
                try (PreparedStatement pstmt = conn.prepareStatement(sb.toString())) {
                    if (!updates.get(5).equals("")) {
                        pstmt.setObject(1, updates.get(5));
                        pstmt.setObject(2, resCode);
                        pstmt.executeUpdate();
                    }
                }
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    private void ReservationCancellation() throws SQLException {
        String url = System.getenv("HP_JDBC_URL");
        String user = System.getenv("HP_JDBC_USER");
        String pass = System.getenv("HP_JDBC_PW");
        Scanner sc = new Scanner(System.in);
        System.out.println("Enter reservation code you wish to cancel: ");
        int resCode = Integer.parseInt(sc.nextLine());
        StringBuilder sb = new StringBuilder("SELECT * FROM lab7_reservations\n" +
                "where code = ?;");
        try (Connection conn = DriverManager.getConnection(url, user, pass)) {
            System.out.println("Database connection acquired - processing query");
            try (PreparedStatement pstmt = conn.prepareStatement(sb.toString())) {
                pstmt.setObject(1, resCode);
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        String code = rs.getString("Code");
                        String room = rs.getString("Room");
                        String checkin = rs.getString("Checkin");
                        String checkout = rs.getString("CheckOut");
                        String last = rs.getString("LastName");
                        String first = rs.getString("FirstName");
                        System.out.println("Is this the reservation you would like to cancel? (Y / N)");
                        System.out.format("%s %s %s %s %s %s %n\n", code, room, checkin, checkout, first, last);
                        String resp = sc.nextLine();
                        if (resp.toLowerCase().equals("y")) {
                            String deleteSql = "DELETE from lab7_reservations where code = ?";
                            try (PreparedStatement delstmt = conn.prepareStatement(deleteSql)) {
                                delstmt.setObject(1, resCode);
                                int rowCount = delstmt.executeUpdate();
                                if (rowCount == 1) {
                                    System.out.println("Reservation successfully deleted");
                                }
                            }
                        }
                    }

                }
            }
        } catch (SQLException e) {
            System.err.println("SQLException: " + e.getMessage());
        }
    }

    private void SearchReservations() {
        String url = System.getenv("HP_JDBC_URL");
        String user = System.getenv("HP_JDBC_USER");
        String pass = System.getenv("HP_JDBC_PW");
        Scanner sc = new Scanner(System.in);
        System.out.println("Search for Reservations: ");
        System.out.println("First Name (Leave blank if any): ");
        String first = sc.nextLine();
        System.out.println("Last Name (Leave blank if any): ");
        String last = sc.nextLine();
        System.out.println("Enter Date YYYY-MM-DD (Leave blank if any): ");
        String date = sc.nextLine();
        System.out.println("Room Code (Leave blank if any): ");
        String roomCode = sc.nextLine();
        System.out.println("Reservation Code (Leave blank if any): ");
        String resCode = sc.nextLine();
        StringBuilder sb = new StringBuilder("SELECT * FROM lab7_reservations" +
                " JOIN lab7_rooms ON lab7_reservations.Room = lab7_rooms.RoomCode" +
                " WHERE Code IS NOT NULL");
        List<Object> params = new ArrayList<Object>();
        if (!"".equalsIgnoreCase(first)) {
            if (first.contains("%")){
                sb.append(" And FirstName LIKE ?");
                params.add(first);
            }
            else {
                sb.append(" AND FirstName = ?");
                params.add(first);
            }
        }
        if (!"".equalsIgnoreCase(roomCode)) {
            if (first.contains("%")){
                sb.append(" And LastName LIKE ?");
                params.add(first);
            }
            else {
                sb.append(" AND LastName = ?");
                params.add(first);
            }
        }
        if (!"".equalsIgnoreCase(date)) {
            sb.append(" AND Checkin <= ?");
            sb.append(" AND Checkout >= ?");
            params.add(date);
            params.add(date);
        }
        if (!"".equalsIgnoreCase(roomCode)) {
            if (first.contains("%")){
                sb.append(" And Room LIKE ?");
                params.add(first);
            }
            else {
                sb.append(" AND Room = ?");
                params.add(first);
            }
        }
        if (!"".equalsIgnoreCase(resCode)) {
            sb.append(" AND code = ?");
            params.add(resCode);
        }
        try (Connection conn = DriverManager.getConnection(url, user, pass)) {
            System.out.println("Database connection acquired - processing query");
            try (PreparedStatement pstmt = conn.prepareStatement(sb.toString())) {
                int i = 1;
                for (Object p : params) {
                    pstmt.setObject(i++, p);
                }
                System.out.println("ReservationCode, Room, CheckIn, Checkout, Rate, LastName, FirstName, Adults," +
                        " Kids, Roomname, Beds, BedType, MaxOcc, BasePrice, Decor");
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        String reserCode = rs.getString("Code");
                        String room = rs.getString("Room");
                        String checkin = rs.getString("CheckIn");
                        String checkout = rs.getString("Checkout");
                        String rate = rs.getString("Rate");
                        String lastname = rs.getString("LastName");
                        String FirstName = rs.getString("FirstName");
                        String adults = rs.getString("adults");
                        String kids = rs.getString("kids");
                        String roomName = rs.getString("roomname");
                        String beds = rs.getString("beds");
                        String bedtype = rs.getString("bedtype");
                        String maxOcc = rs.getString("maxocc");
                        String basePrice = rs.getString("baseprice");
                        String decor = rs.getString("decor");
                        System.out.format("%s %s %s %s %s %s %s %s %s %s %s %s %s %s %s %n", reserCode, room, checkin,
                                checkout, rate, lastname, FirstName, adults, kids, roomName, beds, bedtype, maxOcc,
                                basePrice, decor);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("SQLException: " + e.getMessage());
        }

    }

    private void RevenueByMonth() {
        String sql = "select xx.room, January, February, March, April, May, June, July, August, September, October, November, December,\n" +
                "TotalRev\n" +
                "from\n" +
                "(\n" +
                "select x.room, sum(rev) as TotalRev\n" +
                "from\n" +
                "(\n" +
                "    select rv.room as room, monthname(date) as month, sum(rate) as rev\n" +
                "    from lab7_reservations as rv\n" +
                "    join (\n" +
                "        SELECT ('2019-12-31' - INTERVAL c.number DAY) AS date\n" +
                "        FROM (SELECT singles + tens + hundreds number FROM\n" +
                "        ( SELECT 0 singles\n" +
                "        UNION ALL SELECT   1 UNION ALL SELECT   2 UNION ALL SELECT   3\n" +
                "        UNION ALL SELECT   4 UNION ALL SELECT   5 UNION ALL SELECT   6\n" +
                "        UNION ALL SELECT   7 UNION ALL SELECT   8 UNION ALL SELECT   9\n" +
                "        ) singles JOIN\n" +
                "        (SELECT 0 tens\n" +
                "        UNION ALL SELECT  10 UNION ALL SELECT  20 UNION ALL SELECT  30\n" +
                "        UNION ALL SELECT  40 UNION ALL SELECT  50 UNION ALL SELECT  60\n" +
                "       UNION ALL SELECT  70 UNION ALL SELECT  80 UNION ALL SELECT  90\n" +
                "        ) tens  JOIN\n" +
                "        (SELECT 0 hundreds\n" +
                "        UNION ALL SELECT  100 UNION ALL SELECT  200 UNION ALL SELECT  300\n" +
                "        UNION ALL SELECT  400 UNION ALL SELECT  500 UNION ALL SELECT  600\n" +
                "        UNION ALL SELECT  700 UNION ALL SELECT  800 UNION ALL SELECT  900\n" +
                "        ) hundreds\n" +
                "        ORDER BY number DESC) c \n" +
                "        WHERE c.number BETWEEN 0 and 364\n" +
                "    ) as datesofyear\n" +
                "on datesofyear.date <= rv.checkout and datesofyear.date >= rv.checkin\n" +
                "group by monthname(date), rv.room\n" +
                ") as x\n" +
                "group by x.room\n" +
                ") as xx\n" +
                "join\n" +
                "(\n" +
                "select x.room,\n" +
                "SUM(\n" +
                "        CASE\n" +
                "            WHEN x.month='January' THEN x.rev\n" +
                "            ELSE 0\n" +
                "        END\n" +
                "    ) AS 'January',\n" +
                "SUM(\n" +
                "        CASE\n" +
                "            WHEN x.month='February' THEN x.rev\n" +
                "            ELSE 0\n" +
                "        END\n" +
                "    ) AS 'February',\n" +
                "SUM(\n" +
                "        CASE\n" +
                "            WHEN x.month='March' THEN x.rev\n" +
                "            ELSE 0\n" +
                "        END\n" +
                "    ) AS 'March',\n" +
                "SUM(\n" +
                "        CASE\n" +
                "            WHEN x.month='April' THEN x.rev\n" +
                "            ELSE 0\n" +
                "        END\n" +
                "    ) AS 'April',\n" +
                "SUM(\n" +
                "        CASE\n" +
                "            WHEN x.month='May' THEN x.rev\n" +
                "            ELSE 0\n" +
                "        END\n" +
                "    ) AS 'May',\n" +
                "SUM(\n" +
                "        CASE\n" +
                "            WHEN x.month='June' THEN x.rev\n" +
                "            ELSE 0\n" +
                "        END\n" +
                "    ) AS 'June',\n" +
                "SUM(\n" +
                "        CASE\n" +
                "            WHEN x.month='July' THEN x.rev\n" +
                "            ELSE 0\n" +
                "        END\n" +
                "    ) AS 'July',\n" +
                "SUM(\n" +
                "        CASE\n" +
                "            WHEN x.month='August' THEN x.rev\n" +
                "            ELSE 0\n" +
                "        END\n" +
                "    ) AS 'August',\n" +
                "SUM(\n" +
                "        CASE\n" +
                "            WHEN x.month='September' THEN x.rev\n" +
                "            ELSE 0\n" +
                "        END\n" +
                "    ) AS 'September',\n" +
                "SUM(\n" +
                "        CASE\n" +
                "            WHEN x.month='October' THEN x.rev\n" +
                "            ELSE 0\n" +
                "        END\n" +
                "    ) AS 'October',\n" +
                "SUM(\n" +
                "        CASE\n" +
                "            WHEN x.month='November' THEN x.rev\n" +
                "            ELSE 0\n" +
                "        END\n" +
                "    ) AS 'November',\n" +
                "SUM(\n" +
                "        CASE\n" +
                "            WHEN x.month='December' THEN x.rev\n" +
                "            ELSE 0\n" +
                "        END\n" +
                "    ) AS 'December'\n" +
                "from\n" +
                "(\n" +
                "    select rv.room as room, monthname(date) as month, sum(rate) as rev\n" +
                "    from lab7_reservations as rv\n" +
                "    join (\n" +
                "        SELECT ('2019-12-31' - INTERVAL c.number DAY) AS date\n" +
                "        FROM (SELECT singles + tens + hundreds number FROM\n" +
                "        ( SELECT 0 singles\n" +
                "        UNION ALL SELECT   1 UNION ALL SELECT   2 UNION ALL SELECT   3\n" +
                "        UNION ALL SELECT   4 UNION ALL SELECT   5 UNION ALL SELECT   6\n" +
                "        UNION ALL SELECT   7 UNION ALL SELECT   8 UNION ALL SELECT   9\n" +
                "        ) singles JOIN\n" +
                "        (SELECT 0 tens\n" +
                "        UNION ALL SELECT  10 UNION ALL SELECT  20 UNION ALL SELECT  30\n" +
                "        UNION ALL SELECT  40 UNION ALL SELECT  50 UNION ALL SELECT  60\n" +
                "        UNION ALL SELECT  70 UNION ALL SELECT  80 UNION ALL SELECT  90\n" +
                "        ) tens  JOIN\n" +
                "        (SELECT 0 hundreds\n" +
                "        UNION ALL SELECT  100 UNION ALL SELECT  200 UNION ALL SELECT  300\n" +
                "        UNION ALL SELECT  400 UNION ALL SELECT  500 UNION ALL SELECT  600\n" +
                "        UNION ALL SELECT  700 UNION ALL SELECT  800 UNION ALL SELECT  900\n" +
                "        ) hundreds\n" +
                "        ORDER BY number DESC) c \n" +
                "        WHERE c.number BETWEEN 0 and 364\n" +
                "    ) as datesofyear\n" +
                "on datesofyear.date <= rv.checkout and datesofyear.date >= rv.checkin\n" +
                "group by monthname(date), rv.room\n" +
                ") as x\n" +
                "group by x.room\n" +
                ") as yy\n" +
                "on xx.room = yy.room";
        String url = System.getenv("HP_JDBC_URL");
        String user = System.getenv("HP_JDBC_USER");
        String pass = System.getenv("HP_JDBC_PW");
        try (Connection conn = DriverManager.getConnection(url, user, pass)) {
            System.out.println("Database connection acquired - processing query");
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                System.out.println("Room Jan    Feb     Mar     April   May     June    Jul     Aug     " +
                        "Sep     Oct     Nov     Dec     Total");
                while (rs.next()) {
                    String room = rs.getString("Room");
                    String jan = rs.getString("January");
                    String feb = rs.getString("february");
                    String mar = rs.getString("march");
                    String apr = rs.getString("april");
                    String may = rs.getString("may");
                    String jun = rs.getString("june");
                    String jul = rs.getString("july");
                    String aug = rs.getString("august");
                    String sep = rs.getString("september");
                    String oct = rs.getString("october");
                    String nov = rs.getString("november");
                    String dec = rs.getString("december");
                    String tot = rs.getString("totalrev");

                    System.out.format("%s %s %s %s %s %s %s %s %s %s %s %s %s %s %n", room, jan, feb, mar, apr, may, jun,
                            jul, aug, sep, oct, nov, dec, tot);
                }
            }
        } catch (SQLException e) {
            System.err.println("SQLException: " + e.getMessage());
        }

    }

}


//
//export CLASSPATH=$CLASSPATH:mysql-connector-java-8.0.16.jar:.
  //      export HP_JDBC_URL=jdbc:mysql://db.labthreesixfive.com/echris06?autoReconnect=true\&useSSL=false
    //    export HP_JDBC_USER=
      //  export HP_JDBC_PW=CSC365-F2019_01424998
