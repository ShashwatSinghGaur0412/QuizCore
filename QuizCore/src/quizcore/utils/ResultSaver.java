package quizcore.utils; // बदलना अगर तुम इसे किसी और पैकेज में रखना चाहो

import quizcore.db.DBConnection;

import java.math.BigDecimal;
import java.sql.*;
import java.time.Instant;

public class ResultSaver {

    public static int saveResult(String studentName,
                                 String mobile,
                                 String subject,
                                 int score,
                                 int totalMarks,
                                 int timeTakenSeconds,
                                 String noteOverride) {

        double percentage = totalMarks == 0 ? 0.0 : (100.0 * score) / totalMarks;

        double passPercent = loadPassPercentage();
        String status = (percentage >= passPercent) ? "Pass" : "Fail";

        String note = (noteOverride != null && !noteOverride.isBlank()) ? noteOverride : computeNote(timeTakenSeconds);

        String sql = "INSERT INTO results (student_name, mobile, subject, score, total_marks, percentage, status, time_taken_seconds, attempted_at, note) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, studentName);
            ps.setString(2, mobile);
            ps.setString(3, subject);
            ps.setInt(4, score);
            ps.setInt(5, totalMarks);
            ps.setBigDecimal(6, BigDecimal.valueOf(round(percentage, 2)));
            ps.setString(7, status);
            ps.setInt(8, timeTakenSeconds);
            ps.setTimestamp(9, new Timestamp(Instant.now().toEpochMilli()));
            if (note == null) ps.setNull(10, Types.VARCHAR); else ps.setString(10, note);

            int r = ps.executeUpdate();
            if (r > 0) {
                try (ResultSet gk = ps.getGeneratedKeys()) {
                    if (gk.next()) return gk.getInt(1);
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return -1;
    }

    private static double loadPassPercentage() {
        String sql = "SELECT pass_percentage FROM settings WHERE id = 1";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                BigDecimal bd = rs.getBigDecimal("pass_percentage");
                if (bd != null) return bd.doubleValue();
            }
        } catch (SQLException ignored) {}
        return 40.0;
    }

    private static int loadTimeLimitMinutes() {
        String sql = "SELECT time_minutes FROM settings WHERE id = 1";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                int tm = rs.getInt("time_minutes");
                if (tm > 0) return tm;
            }
        } catch (SQLException ignored) {}
        return 30;
    }

    private static String computeNote(int timeTakenSeconds) {
        int allowedMin = loadTimeLimitMinutes();
        int allowedSec = allowedMin * 60;
        if (timeTakenSeconds > allowedSec) return "Completed after time (was overtime).";
        if (timeTakenSeconds < Math.max(5, allowedSec / 3)) return "Completed very quickly (maybe practice).";
        return null;
    }

    private static double round(double val, int places) {
        if (places < 0) throw new IllegalArgumentException();
        long factor = (long) Math.pow(10, places);
        long tmp = Math.round(val * factor);
        return (double) tmp / factor;
    }
}
