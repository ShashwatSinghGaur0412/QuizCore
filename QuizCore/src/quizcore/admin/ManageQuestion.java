package quizcore.admin;

import quizcore.db.DBConnection;
import quizcore.utils.GradientPanel;

import javax.swing.*;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.Vector;

public class ManageQuestion extends JFrame {

    private final JComboBox<String> cbSubjectFilter;
    private final JTextField tfKeyword;
    private final JButton btnSearch;
    private final JTable table;
    private final DefaultTableModel tableModel;
    private final JButton btnEdit;
    private final JButton btnDelete;
    private final JButton btnRefresh;

    public ManageQuestion() {
        setTitle("Manage Questions - QuizCore");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1280, 720);
        setLocationRelativeTo(null);
        setResizable(false);

        GradientPanel bg = new GradientPanel();
        bg.setLayout(new BorderLayout(12, 12));
        bg.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        top.setOpaque(false);
        top.add(new JLabel("Subject:"));

        cbSubjectFilter = new JComboBox<>();
        cbSubjectFilter.addItem("All"); // default
        top.add(cbSubjectFilter);

        top.add(new JLabel("Keyword:"));
        tfKeyword = new JTextField(30);
        top.add(tfKeyword);

        btnSearch = new JButton("Search");
        top.add(btnSearch);

        btnRefresh = new JButton("Refresh");
        top.add(btnRefresh);

        bg.add(top, BorderLayout.NORTH);

        String[] cols = {"ID", "Subject", "Question (preview)", "A", "B", "C", "D", "Answer"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // read-only table
            }
        };
        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        table.getTableHeader().setReorderingAllowed(false);

        if (table.getColumnModel().getColumnCount() > 2) {
            table.getColumnModel().getColumn(0).setPreferredWidth(60); 
            table.getColumnModel().getColumn(1).setPreferredWidth(120); 
            table.getColumnModel().getColumn(2).setPreferredWidth(620); 
            table.getColumnModel().getColumn(3).setPreferredWidth(140);
            table.getColumnModel().getColumn(4).setPreferredWidth(140); 
            table.getColumnModel().getColumn(5).setPreferredWidth(140); 
            table.getColumnModel().getColumn(6).setPreferredWidth(140); 
            table.getColumnModel().getColumn(7).setPreferredWidth(80);
        }

        table.setRowHeight(36);

        table.setDefaultRenderer(Object.class, new SelectionHighlightRenderer());

        JScrollPane sp = new JScrollPane(table);
        bg.add(sp, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 8));
        bottom.setOpaque(false);
        btnEdit = new JButton("Edit");
        btnDelete = new JButton("Delete");
        JButton btnClose = new JButton("Close");
        btnRefresh.setToolTipText("Clear filters and reload.");
        bottom.add(btnEdit);
        bottom.add(btnDelete);
        bottom.add(btnClose);
        bg.add(bottom, BorderLayout.SOUTH);

        setContentPane(bg);

        loadSubjects();

        loadTable("All", "");

        // Button actions
        btnSearch.addActionListener(e -> loadTable((String) cbSubjectFilter.getSelectedItem(), tfKeyword.getText().trim()));
        btnRefresh.addActionListener(e -> {
            tfKeyword.setText("");
            cbSubjectFilter.setSelectedIndex(0);
            loadTable("All", "");
        });

        btnEdit.addActionListener(e -> onEdit());
        btnDelete.addActionListener(e -> onDelete());
        btnClose.addActionListener(e -> dispose());

        ListSelectionListener selectionListener = e -> {
            boolean sel = table.getSelectedRow() != -1;
            btnEdit.setEnabled(sel);
            btnDelete.setEnabled(sel);
        };
        table.getSelectionModel().addListSelectionListener(selectionListener);
        btnEdit.setEnabled(false);
        btnDelete.setEnabled(false);

        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    onEdit();
                }
            }
        });
    }

    private void loadSubjects() {
        String sql = "SELECT DISTINCT subject FROM questions ORDER BY subject";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                String s = rs.getString("subject");
                if (s != null && !s.trim().isEmpty()) {
                    cbSubjectFilter.addItem(s);
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to load subjects: " + ex.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadTable(String subject, String keyword) {
        tableModel.setRowCount(0);
        String sql;
        boolean filterAll = subject == null || subject.equalsIgnoreCase("All");
        if (filterAll) {
            sql = "SELECT id, subject, question_text, option_a, option_b, option_c, option_d, correct_option FROM questions WHERE question_text LIKE ? ORDER BY created_at DESC";
        } else {
            sql = "SELECT id, subject, question_text, option_a, option_b, option_c, option_d, correct_option FROM questions WHERE subject = ? AND question_text LIKE ? ORDER BY created_at DESC";
        }

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            if (filterAll) {
                ps.setString(1, "%" + keyword + "%");
            } else {
                ps.setString(1, subject);
                ps.setString(2, "%" + keyword + "%");
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Vector<Object> row = new Vector<>();
                    row.add(rs.getInt("id"));
                    row.add(rs.getString("subject"));
                    String q = rs.getString("question_text");
                    row.add(truncate(q, 140)); // shorter preview
                    row.add(rs.getString("option_a"));
                    row.add(rs.getString("option_b"));
                    row.add(rs.getString("option_c"));
                    row.add(rs.getString("option_d"));
                    row.add(rs.getString("correct_option"));
                    tableModel.addRow(row);
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

    private void onEdit() {
        int sel = table.getSelectedRow();
        if (sel == -1) {
            JOptionPane.showMessageDialog(this, "Please select a question to edit.", "No selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int id = (int) tableModel.getValueAt(sel, 0);
        EditQuestionDialog dlg = new EditQuestionDialog(this, id);
        dlg.setVisible(true);
        if (dlg.isUpdated()) {
            // refresh table keeping same filter
            loadTable((String) cbSubjectFilter.getSelectedItem(), tfKeyword.getText().trim());
        }
    }

    private void onDelete() {
        int sel = table.getSelectedRow();
        if (sel == -1) {
            JOptionPane.showMessageDialog(this, "Please select a question to delete.", "No selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int id = (int) tableModel.getValueAt(sel, 0);
        int opt = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete selected question (ID: " + id + ")?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (opt != JOptionPane.YES_OPTION) return;

        String sql = "DELETE FROM questions WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            int r = ps.executeUpdate();
            if (r > 0) {
                JOptionPane.showMessageDialog(this, "Question deleted.", "Deleted", JOptionPane.INFORMATION_MESSAGE);
                loadTable((String) cbSubjectFilter.getSelectedItem(), tfKeyword.getText().trim());
            } else {
                JOptionPane.showMessageDialog(this, "Delete failed. Question may not exist.", "Delete Failed", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "DB error: " + ex.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static class EditQuestionDialog extends JDialog {
        private final int questionId;
        private boolean updated = false;

        private final JComboBox<String> cbSubject;
        private final JTextArea taQuestion;
        private final JTextField tfA, tfB, tfC, tfD;
        private final JComboBox<String> cbCorrect;

        public EditQuestionDialog(Frame owner, int questionId) {
            super(owner, "Edit Question - ID: " + questionId, true);
            this.questionId = questionId;
            setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            setSize(900, 560);
            setLocationRelativeTo(owner);
            setResizable(false);

            GradientPanel bg = new GradientPanel();
            bg.setLayout(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(8, 12, 8, 12);
            gbc.fill = GridBagConstraints.HORIZONTAL;

            JLabel title = new JLabel("Edit Question (ID: " + questionId + ")", SwingConstants.CENTER);
            title.setFont(new Font("SansSerif", Font.BOLD, 18));
            gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
            bg.add(title, gbc);
            gbc.gridwidth = 1;
            int row = 1;

            cbSubject = new JComboBox<>(new String[]{"Java","C++","Python","JavaScript","Other"});
            cbSubject.setEditable(true);
            addLabel(bg, "Subject:", cbSubject, gbc, row++);

            taQuestion = new JTextArea(6, 40);
            taQuestion.setLineWrap(true);
            taQuestion.setWrapStyleWord(true);
            JScrollPane spQ = new JScrollPane(taQuestion, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            addLabel(bg, "Question Text:", spQ, gbc, row++);

            tfA = new JTextField(); addLabel(bg, "Option A:", tfA, gbc, row++);
            tfB = new JTextField(); addLabel(bg, "Option B:", tfB, gbc, row++);
            tfC = new JTextField(); addLabel(bg, "Option C:", tfC, gbc, row++);
            tfD = new JTextField(); addLabel(bg, "Option D:", tfD, gbc, row++);

            cbCorrect = new JComboBox<>(new String[]{"A","B","C","D"});
            addLabel(bg, "Correct Option:", cbCorrect, gbc, row++);

            JPanel btns = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 6));
            btns.setOpaque(false);
            JButton bSave = new JButton("Save");
            JButton bCancel = new JButton("Cancel");
            btns.add(bSave); btns.add(bCancel);
            gbc.gridx = 0; gbc.gridy = row++; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.NONE;
            bg.add(btns, gbc);

            bCancel.addActionListener(e -> dispose());
            bSave.addActionListener(e -> onSave());

            setContentPane(bg);

            loadData();
        }

        private void addLabel(JPanel p, String labelText, Component comp, GridBagConstraints gbc, int row) {
            gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
            JLabel lbl = new JLabel(labelText);
            lbl.setFont(new Font("SansSerif", Font.PLAIN, 13));
            p.add(lbl, gbc);
            gbc.gridx = 1; gbc.gridy = row; gbc.weightx = 1.0;
            p.add(comp, gbc);
            gbc.weightx = 0;
        }

        private void loadData() {
            String sql = "SELECT subject, question_text, option_a, option_b, option_c, option_d, correct_option FROM questions WHERE id = ?";
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, questionId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        cbSubject.setSelectedItem(rs.getString("subject"));
                        taQuestion.setText(rs.getString("question_text"));
                        tfA.setText(rs.getString("option_a"));
                        tfB.setText(rs.getString("option_b"));
                        tfC.setText(rs.getString("option_c"));
                        tfD.setText(rs.getString("option_d"));
                        cbCorrect.setSelectedItem(rs.getString("correct_option"));
                    } else {
                        JOptionPane.showMessageDialog(this, "Question not found (may be deleted).", "Not Found", JOptionPane.ERROR_MESSAGE);
                        dispose();
                    }
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "DB error: " + ex.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
                dispose();
            }
        }

        private void onSave() {
            String subject = cbSubject.getEditor().getItem().toString().trim();
            String question = taQuestion.getText().trim();
            String a = tfA.getText().trim();
            String b = tfB.getText().trim();
            String c = tfC.getText().trim();
            String d = tfD.getText().trim();
            String correct = cbCorrect.getSelectedItem().toString();

            if (subject.isEmpty() || question.isEmpty() || a.isEmpty() || b.isEmpty() || c.isEmpty() || d.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill all required fields.", "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String sql = "UPDATE questions SET subject=?, question_text=?, option_a=?, option_b=?, option_c=?, option_d=?, correct_option=? WHERE id=?";
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, subject);
                ps.setString(2, question);
                ps.setString(3, a);
                ps.setString(4, b);
                ps.setString(5, c);
                ps.setString(6, d);
                ps.setString(7, correct);
                ps.setInt(8, questionId);

                int r = ps.executeUpdate();
                if (r > 0) {
                    JOptionPane.showMessageDialog(this, "Question updated successfully.", "Updated", JOptionPane.INFORMATION_MESSAGE);
                    updated = true;
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(this, "Update failed. No rows modified.", "Update Failed", JOptionPane.ERROR_MESSAGE);
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "DB error: " + ex.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
            }
        }

        public boolean isUpdated() {
            return updated;
        }
    }

    private static class SelectionHighlightRenderer extends JLabel implements TableCellRenderer {
        private static final Color SELECT_BG = new Color(51, 122, 183); 
        private static final Color SELECT_FG = Color.WHITE;
        private static final Color NORMAL_FG = Color.BLACK;

        SelectionHighlightRenderer() {
            setOpaque(false); 
            setVerticalAlignment(SwingConstants.TOP);
            setBorder(BorderFactory.createEmptyBorder(4, 6, 4, 6));
            setFont(new Font("SansSerif", Font.PLAIN, 13));
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            String text = value == null ? "" : value.toString();
            String preview = text.length() > 140 ? text.substring(0, 137) + "..." : text;
            setText(preview);
            setToolTipText(text.isEmpty() ? null : text);

            if (table.getSelectionModel().isSelectedIndex(row)) {
                setOpaque(true);
                setBackground(SELECT_BG);
                setForeground(SELECT_FG);
            } else {
                setOpaque(false);
                setBackground(null);
                setForeground(NORMAL_FG);
            }
            return this;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ManageQuestion mq = new ManageQuestion();
            mq.setVisible(true);
        });
    }
}
