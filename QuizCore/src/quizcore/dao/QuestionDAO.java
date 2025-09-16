package quizcore.dao;

import quizcore.db.DBConnection;
import quizcore.model.Question;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class QuestionDAO {

    public static boolean insertQuestion(Question q) {
        String sql = "INSERT INTO questions (subject, question_text, option_a, option_b, option_c, option_d, correct_option, created_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, NOW())";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, q.getSubject());
            ps.setString(2, q.getQuestionText());
            ps.setString(3, q.getOptionA());
            ps.setString(4, q.getOptionB());
            ps.setString(5, q.getOptionC());
            ps.setString(6, q.getOptionD());
            ps.setString(7, q.getCorrectAnswer());

            int r = ps.executeUpdate();
            if (r > 0) {
                try (ResultSet gk = ps.getGeneratedKeys()) {
                    if (gk.next()) q.setId(gk.getInt(1));
                }
                return true;
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    public static boolean updateQuestion(Question q) {
        String sql = "UPDATE questions SET subject=?, question_text=?, option_a=?, option_b=?, option_c=?, option_d=?, correct_option=? " +
                     "WHERE id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, q.getSubject());
            ps.setString(2, q.getQuestionText());
            ps.setString(3, q.getOptionA());
            ps.setString(4, q.getOptionB());
            ps.setString(5, q.getOptionC());
            ps.setString(6, q.getOptionD());
            ps.setString(7, q.getCorrectAnswer());
            ps.setInt(8, q.getId());

            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    public static boolean deleteQuestion(int id) {
        String sql = "DELETE FROM questions WHERE id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    public static List<Question> loadQuestions(String subjectFilter) {
        List<Question> list = new ArrayList<>();
        boolean filter = subjectFilter != null && !subjectFilter.trim().isEmpty()
                && !subjectFilter.equalsIgnoreCase("All") && !subjectFilter.equalsIgnoreCase("All (Mix)");

        String sql = "SELECT id, subject, question_text, option_a, option_b, option_c, option_d, correct_option " +
                     "FROM questions " + (filter ? "WHERE LOWER(TRIM(subject)) = LOWER(TRIM(?)) " : "") +
                     "ORDER BY id";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            if (filter) ps.setString(1, subjectFilter.trim());

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return list;
    }
    
    public static List<Question> getRandomQuestions(String subjectFilter, int limit) {
        List<Question> list = new ArrayList<>();
        boolean filter = subjectFilter != null && !subjectFilter.trim().isEmpty()
                && !subjectFilter.equalsIgnoreCase("All") && !subjectFilter.equalsIgnoreCase("All (Mix)");

        String sql = "SELECT id, subject, question_text, option_a, option_b, option_c, option_d, correct_option " +
                     "FROM questions " + (filter ? "WHERE LOWER(TRIM(subject)) = LOWER(TRIM(?)) " : "") +
                     "ORDER BY RAND() LIMIT ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            int idx = 1;
            if (filter) {
                ps.setString(idx++, subjectFilter.trim());
            }
            ps.setInt(idx, limit);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return list;
    }

    private static Question mapRow(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String subject = rs.getString("subject");
        String qtext = rs.getString("question_text");
        String a = rs.getString("option_a");
        String b = rs.getString("option_b");
        String c = rs.getString("option_c");
        String d = rs.getString("option_d");
        String correct = rs.getString("correct_option");

        if (qtext == null) qtext = "";
        if (a == null) a = "";
        if (b == null) b = "";
        if (c == null) c = "";
        if (d == null) d = "";
        if (correct == null) correct = "";

        return new Question(id, subject, qtext, a, b, c, d, correct);
    }
}
