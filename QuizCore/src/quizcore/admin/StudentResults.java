package quizcore.admin;

import quizcore.db.DBConnection;
import quizcore.utils.GradientPanel;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Vector;

public class StudentResults extends JFrame {

    private final JComboBox<String> cbSubject;
    private final JTextField tfKeyword;
    private final JSpinner spFrom, spTo;
    private final JButton btnSearch, btnRefresh, btnExport, btnPrev, btnNext, btnClose;
    private final JLabel lblPageInfo;
    private final DefaultTableModel tableModel;
    private final JTable table;

    private int pageSize = 100;
    private int currentPage = 1;
    private int totalRows = 0;
    private final SimpleDateFormat sdfDisplay = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    public StudentResults() {
        setTitle("Student Results - QuizCore");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1280, 720);
        setLocationRelativeTo(null);
        setResizable(true);

        GradientPanel root = new GradientPanel();
        root.setLayout(new BorderLayout(10, 10));
        root.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        top.setOpaque(false);
        top.add(new JLabel("Subject:"));
        cbSubject = new JComboBox<>();
        cbSubject.addItem("All");
        top.add(cbSubject);

        top.add(new JLabel("Name / Mobile:"));
        tfKeyword = new JTextField(20);
        top.add(tfKeyword);

        top.add(new JLabel("From:"));
        spFrom = new JSpinner(new SpinnerDateModel());
        spFrom.setEditor(new JSpinner.DateEditor(spFrom, "yyyy-MM-dd"));
        try { Date d = new SimpleDateFormat("yyyy-MM-dd").parse("2000-01-01"); spFrom.setValue(d); } catch (Exception ignored) {}
        top.add(spFrom);

        top.add(new JLabel("To:"));
        spTo = new JSpinner(new SpinnerDateModel());
        spTo.setEditor(new JSpinner.DateEditor(spTo, "yyyy-MM-dd"));
        spTo.setValue(new Date());
        top.add(spTo);

        btnSearch = new JButton("Search");
        btnRefresh = new JButton("Refresh");
        btnExport = new JButton("Export CSV");
        top.add(btnSearch);
        top.add(btnRefresh);
        top.add(btnExport);

        root.add(top, BorderLayout.NORTH);

        String[] cols = {"ResultID", "StudentName", "Mobile", "Subject", "Score", "Total", "Percentage", "Status", "TimeTaken(s)", "AttemptedAt"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        table.getTableHeader().setReorderingAllowed(false);

        if (table.getColumnModel().getColumnCount() > 0) {
            table.getColumnModel().getColumn(0).setPreferredWidth(80);
            table.getColumnModel().getColumn(1).setPreferredWidth(220);
            table.getColumnModel().getColumn(2).setPreferredWidth(120);
            table.getColumnModel().getColumn(3).setPreferredWidth(120);
            table.getColumnModel().getColumn(4).setPreferredWidth(70);
            table.getColumnModel().getColumn(5).setPreferredWidth(70);
            table.getColumnModel().getColumn(6).setPreferredWidth(90);
            table.getColumnModel().getColumn(7).setPreferredWidth(80);
            table.getColumnModel().getColumn(8).setPreferredWidth(100);
            table.getColumnModel().getColumn(9).setPreferredWidth(180);
        }

        table.setRowHeight(36);
        table.setDefaultRenderer(Object.class, new RowColorRenderer());
        JTableHeaderStyler.style(table);

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

        loadSubjects();
        loadData();

        btnSearch.addActionListener(e -> { currentPage = 1; loadData(); });
        btnRefresh.addActionListener(e -> { tfKeyword.setText(""); cbSubject.setSelectedIndex(0); currentPage = 1; loadData(); });
        btnExport.addActionListener(e -> exportCsv());
        btnPrev.addActionListener(e -> { if (currentPage > 1) { currentPage--; loadData(); } });
        btnNext.addActionListener(e -> { int max = (int)Math.ceil((double)totalRows/pageSize); if (currentPage < max) { currentPage++; loadData(); } });
        btnClose.addActionListener(e -> dispose());

        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) { if (e.getClickCount() == 2) showDetails(); }
        });
    }

    private void loadSubjects() {
        String sql = "SELECT DISTINCT subject FROM results WHERE subject IS NOT NULL AND subject <> '' ORDER BY subject";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) cbSubject.addItem(rs.getString(1));
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Failed to load subjects: " + ex.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadData() {
        tableModel.setRowCount(0);
        String subject = (String) cbSubject.getSelectedItem();
        if (subject == null) subject = "All";
        String keyword = tfKeyword.getText().trim();

        Date fromDate = (Date) spFrom.getValue();
        Date toDate = (Date) spTo.getValue();
        Calendar cal = Calendar.getInstance();
        cal.setTime(fromDate);
        cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0); cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0);
        Timestamp tsFrom = new Timestamp(cal.getTimeInMillis());
        cal.setTime(toDate);
        cal.set(Calendar.HOUR_OF_DAY, 23); cal.set(Calendar.MINUTE, 59); cal.set(Calendar.SECOND, 59); cal.set(Calendar.MILLISECOND, 999);
        Timestamp tsTo = new Timestamp(cal.getTimeInMillis());

        String countSql = "SELECT COUNT(*) FROM results WHERE (? = 'All' OR subject = ?) AND (student_name LIKE ? OR mobile LIKE ?) AND attempted_at BETWEEN ? AND ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement psCount = conn.prepareStatement(countSql)) {
            psCount.setString(1, subject);
            psCount.setString(2, subject);
            psCount.setString(3, "%" + keyword + "%");
            psCount.setString(4, "%" + keyword + "%");
            psCount.setTimestamp(5, tsFrom);
            psCount.setTimestamp(6, tsTo);
            try (ResultSet rs = psCount.executeQuery()) { if (rs.next()) totalRows = rs.getInt(1); else totalRows = 0; }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Count failed: " + ex.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
            totalRows = 0;
        }

        int offset = (currentPage - 1) * pageSize;
        String sql = "SELECT id, student_name, mobile, subject, score, total_marks, percentage, status, time_taken_seconds, attempted_at " +
                "FROM results WHERE (? = 'All' OR subject = ?) AND (student_name LIKE ? OR mobile LIKE ?) AND attempted_at BETWEEN ? AND ? " +
                "ORDER BY attempted_at DESC LIMIT ?, ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, subject);
            ps.setString(2, subject);
            ps.setString(3, "%" + keyword + "%");
            ps.setString(4, "%" + keyword + "%");
            ps.setTimestamp(5, tsFrom);
            ps.setTimestamp(6, tsTo);
            ps.setInt(7, offset);
            ps.setInt(8, pageSize);

            try (ResultSet rs = ps.executeQuery()) {
                int count = 0;
                while (rs.next()) {
                    Vector<Object> r = new Vector<>();
                    r.add(rs.getInt("id"));
                    r.add(rs.getString("student_name"));
                    r.add(rs.getString("mobile"));
                    r.add(rs.getString("subject"));
                    r.add(rs.getInt("score"));
                    r.add(rs.getInt("total_marks"));
                    r.add(rs.getBigDecimal("percentage"));
                    r.add(rs.getString("status"));
                    r.add(rs.getInt("time_taken_seconds"));
                    Timestamp t = rs.getTimestamp("attempted_at");
                    r.add(t == null ? "" : sdfDisplay.format(t));
                    tableModel.addRow(r);
                    count++;
                }
                int rowStart = offset + 1;
                int rowEnd = offset + count;
                if (totalRows == 0) lblPageInfo.setText("Showing 0-0 of 0");
                else lblPageInfo.setText("Showing " + rowStart + "-" + rowEnd + " of " + totalRows + " (Page " + currentPage + ")");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Load failed: " + ex.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showDetails() {
        int sel = table.getSelectedRow();
        if (sel == -1) return;
        int id = (int) tableModel.getValueAt(sel, 0);
        String sql = "SELECT * FROM results WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("Result ID: ").append(id).append("\n");
                    sb.append("Student: ").append(rs.getString("student_name")).append("\n");
                    sb.append("Mobile: ").append(rs.getString("mobile")).append("\n");
                    sb.append("Subject: ").append(rs.getString("subject")).append("\n\n");
                    sb.append("Score: ").append(rs.getInt("score")).append(" / ").append(rs.getInt("total_marks")).append("\n");
                    sb.append("Percentage: ").append(rs.getBigDecimal("percentage")).append("%\n");
                    sb.append("Status: ").append(rs.getString("status")).append("\n");
                    sb.append("Time Taken (s): ").append(rs.getInt("time_taken_seconds")).append("\n");
                    Timestamp t = rs.getTimestamp("attempted_at");
                    if (t != null) sb.append("Attempted At: ").append(sdfDisplay.format(t)).append("\n");
                    JTextArea ta = new JTextArea(sb.toString());
                    ta.setEditable(false);
                    ta.setLineWrap(true);
                    ta.setWrapStyleWord(true);
                    JScrollPane sp = new JScrollPane(ta);
                    sp.setPreferredSize(new Dimension(700, 350));
                    JOptionPane.showMessageDialog(this, sp, "Result Details", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "Result not found.", "Not Found", JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Details load failed: " + ex.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void exportCsv() {
        if (tableModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "No rows to export.", "Export", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Save CSV");
        fc.setSelectedFile(new File("results_export.csv"));
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
            JOptionPane.showMessageDialog(this, "Export failed: " + ex.getMessage(), "Export Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String escapeCsv(String s) {
        if (s == null) return "";
        String out = s.replace("\"", "\"\"");
        if (out.contains(",") || out.contains("\"") || out.contains("\n") || out.contains("\r")) return "\"" + out + "\"";
        return out;
    }

    private static class RowColorRenderer extends JLabel implements TableCellRenderer {
        private static final Color PASS_BG = new Color(200, 245, 210);
        private static final Color FAIL_BG = new Color(255, 220, 220);
        private static final Color ALT_BG = new Color(250, 250, 250);
        private static final Color SELECT_BG = new Color(51, 122, 183);
        private static final Color SELECT_FG = Color.WHITE;

        RowColorRenderer() { setOpaque(true); setVerticalAlignment(SwingConstants.TOP); setBorder(BorderFactory.createEmptyBorder(4,6,4,6)); setFont(new Font("SansSerif", Font.PLAIN, 13)); }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            String text = value == null ? "" : value.toString();
            setText(text);
            setToolTipText(text.isEmpty() ? null : text);
            String status = "";
            try {
                Object o = table.getModel().getValueAt(row, 7);
                status = o == null ? "" : o.toString();
            } catch (Exception ignored) {}
            if (isSelected) {
                setBackground(SELECT_BG);
                setForeground(SELECT_FG);
            } else {
                if ("Pass".equalsIgnoreCase(status)) setBackground(PASS_BG);
                else if ("Fail".equalsIgnoreCase(status)) setBackground(FAIL_BG);
                else setBackground((row % 2 == 0) ? Color.WHITE : ALT_BG);
                setForeground(Color.BLACK);
            }
            return this;
        }
    }

    private static class JTableHeaderStyler {
        static void style(JTable table) {
            JTableHeader header = table.getTableHeader();
            header.setOpaque(true);
            header.setBackground(new Color(153, 76, 0));
            header.setForeground(Color.WHITE);
            header.setFont(header.getFont().deriveFont(Font.BOLD));
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            StudentResults sr = new StudentResults();
            sr.setVisible(true);
        });
    }
}
