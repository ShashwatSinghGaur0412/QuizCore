package quizcore.admin;

import quizcore.db.DBConnection;
import quizcore.utils.GradientPanel;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Vector;

public class AllQuestions extends JFrame {

    private final JComboBox<String> cbSubject;
    private final JTextField tfKeyword;
    private final JButton btnSearch, btnRefresh, btnExport, btnPrev, btnNext, btnClose;
    private final JLabel lblPageInfo;
    private final DefaultTableModel tableModel;
    private final JTable table;

    private int pageSize = 100;
    private int currentPage = 1;
    private int totalRows = 0;

    public AllQuestions() {
        setTitle("All Questions - QuizCore");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1280, 720);
        setLocationRelativeTo(null);
        setResizable(true);

        GradientPanel root = new GradientPanel();
        root.setLayout(new BorderLayout(10, 10));
        root.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 8));
        top.setOpaque(false);
        top.add(new JLabel("Subject:"));
        cbSubject = new JComboBox<>();
        cbSubject.addItem("All");
        top.add(cbSubject);

        top.add(new JLabel("Keyword:"));
        tfKeyword = new JTextField(30);
        top.add(tfKeyword);

        btnSearch = new JButton("Search");
        btnRefresh = new JButton("Refresh");
        btnExport = new JButton("Export CSV");

        top.add(btnSearch);
        top.add(btnRefresh);
        top.add(btnExport);

        root.add(top, BorderLayout.NORTH);

        String[] cols = {"ID", "Subject", "Question (preview)", "A", "B", "C", "D", "Answer", "Created At"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        table.getTableHeader().setReorderingAllowed(false);

        table.getColumnModel().getColumn(0).setPreferredWidth(60); 
        table.getColumnModel().getColumn(1).setPreferredWidth(120); 
        table.getColumnModel().getColumn(2).setPreferredWidth(500);
        table.getColumnModel().getColumn(3).setPreferredWidth(160); 
        table.getColumnModel().getColumn(4).setPreferredWidth(160); 
        table.getColumnModel().getColumn(5).setPreferredWidth(160); 
        table.getColumnModel().getColumn(6).setPreferredWidth(160); 
        table.getColumnModel().getColumn(7).setPreferredWidth(80); 
        table.getColumnModel().getColumn(8).setPreferredWidth(140); 

        table.setRowHeight(36);

        table.setDefaultRenderer(Object.class, new SimpleTooltipRenderer());

        JScrollPane sp = new JScrollPane(table);
        root.add(sp, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setOpaque(false);

        JPanel leftBottom = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        leftBottom.setOpaque(false);
        btnPrev = new JButton("Previous");
        btnNext = new JButton("Next");
        leftBottom.add(btnPrev);
        leftBottom.add(btnNext);

        lblPageInfo = new JLabel("Showing 0-0 of 0");
        leftBottom.add(lblPageInfo);

        bottom.add(leftBottom, BorderLayout.WEST);

        JPanel rightBottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 6));
        rightBottom.setOpaque(false);
        btnClose = new JButton("Close");
        rightBottom.add(btnClose);
        bottom.add(rightBottom, BorderLayout.EAST);

        root.add(bottom, BorderLayout.SOUTH);

        setContentPane(root);

        btnSearch.addActionListener(e -> { currentPage = 1; loadData(); });
        btnRefresh.addActionListener(e -> { tfKeyword.setText(""); cbSubject.setSelectedIndex(0); currentPage = 1; loadData(); });
        btnExport.addActionListener(e -> exportCsv());
        btnPrev.addActionListener(e -> { if (currentPage > 1) { currentPage--; loadData(); } });
        btnNext.addActionListener(e -> {
            int maxPage = (int) Math.ceil((double) totalRows / pageSize);
            if (currentPage < maxPage) { currentPage++; loadData(); }
        });
        btnClose.addActionListener(e -> dispose());

        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) showFullDetails();
            }
        });

        loadSubjects();
        loadData();
    }

    private void loadSubjects() {
        cbSubject.removeAllItems();
        cbSubject.addItem("All");
        String sql = "SELECT DISTINCT subject FROM questions WHERE subject IS NOT NULL AND subject <> '' ORDER BY subject";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                cbSubject.addItem(rs.getString(1));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to load subjects: " + ex.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadData() {
        tableModel.setRowCount(0);
        String subject = (String) cbSubject.getSelectedItem();
        if (subject == null) subject = "All";
        String keyword = tfKeyword.getText().trim();

        String countSql = "SELECT COUNT(*) FROM questions WHERE (? = 'All' OR subject = ?) AND question_text LIKE ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement psCount = conn.prepareStatement(countSql)) {
            psCount.setString(1, subject);
            psCount.setString(2, subject);
            psCount.setString(3, "%" + keyword + "%");
            try (ResultSet rs = psCount.executeQuery()) {
                if (rs.next()) totalRows = rs.getInt(1);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to count questions: " + ex.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
            totalRows = 0;
        }

        int offset = (currentPage - 1) * pageSize;

        String sql = "SELECT id, subject, question_text, option_a, option_b, option_c, option_d, correct_option, created_at " +
                "FROM questions " +
                "WHERE (? = 'All' OR subject = ?) AND question_text LIKE ? " +
                "ORDER BY created_at DESC " +
                "LIMIT ?, ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, subject);
            ps.setString(2, subject);
            ps.setString(3, "%" + keyword + "%");
            ps.setInt(4, offset);
            ps.setInt(5, pageSize);

            try (ResultSet rs = ps.executeQuery()) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                int rowStart = offset + 1;
                int count = 0;
                while (rs.next()) {
                    Vector<Object> r = new Vector<>();
                    r.add(rs.getInt("id"));
                    r.add(rs.getString("subject"));
                    String q = rs.getString("question_text");
                    r.add(truncate(q, 180));
                    r.add(rs.getString("option_a"));
                    r.add(rs.getString("option_b"));
                    r.add(rs.getString("option_c"));
                    r.add(rs.getString("option_d"));
                    r.add(rs.getString("correct_option"));
                    Timestamp ts = rs.getTimestamp("created_at");
                    r.add(ts == null ? "" : sdf.format(ts));
                    tableModel.addRow(r);
                    count++;
                }
                int rowEnd = offset + count;
                if (totalRows == 0) {
                    lblPageInfo.setText("Showing 0-0 of 0");
                } else {
                    lblPageInfo.setText("Showing " + rowStart + "-" + rowEnd + " of " + totalRows + " (Page " + currentPage + ")");
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to load questions: " + ex.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String truncate(String s, int len) {
        if (s == null) return "";
        return s.length() > len ? s.substring(0, len - 3) + "..." : s;
    }

    private void showFullDetails() {
        int sel = table.getSelectedRow();
        if (sel == -1) return;
        int id = (int) tableModel.getValueAt(sel, 0);

        String sql = "SELECT subject, question_text, option_a, option_b, option_c, option_d, correct_option, created_at FROM questions WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("ID: ").append(id).append("\n");
                    sb.append("Subject: ").append(rs.getString("subject")).append("\n\n");
                    sb.append("Question:\n").append(rs.getString("question_text")).append("\n\n");
                    sb.append("A: ").append(rs.getString("option_a")).append("\n");
                    sb.append("B: ").append(rs.getString("option_b")).append("\n");
                    sb.append("C: ").append(rs.getString("option_c")).append("\n");
                    sb.append("D: ").append(rs.getString("option_d")).append("\n\n");
                    sb.append("Correct: ").append(rs.getString("correct_option")).append("\n");
                    Timestamp ts = rs.getTimestamp("created_at");
                    if (ts != null) sb.append("\nCreated At: ").append(ts.toString());
                    JTextArea ta = new JTextArea(sb.toString());
                    ta.setEditable(false);
                    ta.setLineWrap(true);
                    ta.setWrapStyleWord(true);
                    JScrollPane sp = new JScrollPane(ta);
                    sp.setPreferredSize(new Dimension(800, 400));
                    JOptionPane.showMessageDialog(this, sp, "Question Details", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "Question not found.", "Not Found", JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "DB error: " + ex.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void exportCsv() {
        if (tableModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "No rows to export.", "Export", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Save CSV");
        fc.setSelectedFile(new File("questions_export.csv"));
        int sel = fc.showSaveDialog(this);
        if (sel != JFileChooser.APPROVE_OPTION) return;
        File f = fc.getSelectedFile();

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(f))) {
            for (int c = 0; c < tableModel.getColumnCount(); c++) {
                bw.write(escapeCsv(tableModel.getColumnName(c)));
                if (c < tableModel.getColumnCount() - 1) bw.write(",");
            }
            bw.write("\n");

            for (int r = 0; r < tableModel.getRowCount(); r++) {
                for (int c = 0; c < tableModel.getColumnCount(); c++) {
                    Object val = tableModel.getValueAt(r, c);
                    bw.write(escapeCsv(val == null ? "" : val.toString()));
                    if (c < tableModel.getColumnCount() - 1) bw.write(",");
                }
                bw.write("\n");
            }
            bw.flush();
            JOptionPane.showMessageDialog(this, "Exported to " + f.getAbsolutePath(), "Export CSV", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to export CSV: " + ex.getMessage(), "Export Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String escapeCsv(String s) {
        if (s == null) return "";
        String out = s.replace("\"", "\"\"");
        if (out.contains(",") || out.contains("\"") || out.contains("\n") || out.contains("\r")) {
            return "\"" + out + "\"";
        } else return out;
    }

    private static class SimpleTooltipRenderer extends JLabel implements TableCellRenderer {
        SimpleTooltipRenderer() {
            setOpaque(false); // background transparent
            setVerticalAlignment(SwingConstants.TOP);
            setBorder(BorderFactory.createEmptyBorder(4,4,4,4));
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            String text = value == null ? "" : value.toString();
            String preview = text.length() > 180 ? text.substring(0, 177) + "..." : text;
            setText(preview);
            setToolTipText(text.length() > 0 ? text : null);

            setForeground(Color.BLACK);

            return this;
        }
    }


    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            AllQuestions aq = new AllQuestions();
            aq.setVisible(true);
        });
    }
}
