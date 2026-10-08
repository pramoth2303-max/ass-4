import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableModel;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

public class Main {
    private static SessionFactory sessionFactory;
    private final JFrame frame = new JFrame("Student Management System");
    private final JTextField idField = new JTextField();
    private final JTextField nameField = new JTextField();
    private final JTextField emailField = new JTextField();
    private final JTextField courseField = new JTextField();

    private final DefaultTableModel model = new DefaultTableModel(
            new Object[]{"ID", "Name", "Email", "Course"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) { return false; }
    };
    private final JTable table = new JTable(model);

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                sessionFactory = buildSessionFactory();
                new Main().show();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null,
                        "Unable to start application:\n" + e.getMessage(),
                        "Startup Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    private static SessionFactory buildSessionFactory() {
        Configuration con = new Configuration();
        con.setProperty("hibernate.connection.driver_class", "com.mysql.cj.jdbc.Driver");
        con.setProperty("hibernate.connection.url",
                "jdbc:mysql://db01.dbhost.dev:5051/db_455jr37ba?useSSL=false&serverTimezone=UTC");
        con.setProperty("hibernate.connection.username", "user_455jr37ba");
        con.setProperty("hibernate.connection.password", "p455jr37ba");
        con.setProperty("hibernate.hbm2ddl.auto", "update");
        con.setProperty("hibernate.show_sql", "false");
        con.setProperty("hibernate.format_sql", "true");
        con.addAnnotatedClass(Student.class);
        return con.buildSessionFactory();
    }

    private void show() {
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setMinimumSize(new Dimension(900, 600));
        frame.setLayout(new BorderLayout(16, 16));
        frame.getContentPane().setBackground(new Color(245, 247, 250));

        frame.add(createHeader(), BorderLayout.NORTH);
        frame.add(createForm(), BorderLayout.WEST);
        frame.add(createTablePanel(), BorderLayout.CENTER);

        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getSelectionModel().addListSelectionListener(e -> fillSelectedRow());
        loadStudents();

        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBorder(BorderFactory.createEmptyBorder(20, 24, 12, 24));
        header.setBackground(new Color(30, 64, 175));

        JLabel title = new JLabel("Student Management System");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("SansSerif", Font.BOLD, 26));

        JLabel subtitle = new JLabel("Hibernate + MySQL Desktop Application");
        subtitle.setForeground(new Color(219, 234, 254));
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 13));

        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new javax.swing.BoxLayout(text, javax.swing.BoxLayout.Y_AXIS));
        text.add(title);
        text.add(subtitle);
        header.add(text, BorderLayout.WEST);
        return header;
    }

    private JPanel createForm() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(16, 16, 16, 8),
                BorderFactory.createTitledBorder("Student Details")));
        panel.setPreferredSize(new Dimension(285, 0));

        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(7, 6, 7, 6);
        g.fill = GridBagConstraints.HORIZONTAL;
        g.weightx = 1;

        addField(panel, g, 0, "Student ID", idField);
        addField(panel, g, 1, "Name", nameField);
        addField(panel, g, 2, "Email", emailField);
        addField(panel, g, 3, "Course", courseField);

        JButton add = button("Add Student");
        JButton update = button("Update");
        JButton delete = button("Delete");
        JButton clear = button("Clear");
        JButton refresh = button("Refresh");

        g.gridx = 0; g.gridy = 4; g.gridwidth = 2;
        panel.add(add, g);
        g.gridy++;
        panel.add(update, g);
        g.gridy++;
        panel.add(delete, g);
        g.gridy++;
        panel.add(clear, g);
        g.gridy++;
        panel.add(refresh, g);

        add.addActionListener(e -> addStudent());
        update.addActionListener(e -> updateStudent());
        delete.addActionListener(e -> deleteStudent());
        clear.addActionListener(e -> clearForm());
        refresh.addActionListener(e -> loadStudents());

        return panel;
    }

    private void addField(JPanel p, GridBagConstraints g, int row, String label, JTextField field) {
        g.gridx = 0; g.gridy = row; g.gridwidth = 2;
        p.add(new JLabel(label), g);
        g.gridy++;
        field.setPreferredSize(new Dimension(230, 32));
        p.add(field, g);
    }

    private JButton button(String text) {
        JButton b = new JButton(text);
        b.setPreferredSize(new Dimension(230, 34));
        return b;
    }

    private JScrollPane createTablePanel() {
        table.setRowHeight(32);
        table.setFont(new Font("SansSerif", Font.PLAIN, 13));
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 13));
        table.setFillsViewportHeight(true);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder(16, 8, 16, 16));
        return scroll;
    }

    private Student readForm() {
        try {
            int id = Integer.parseInt(idField.getText().trim());
            String name = nameField.getText().trim();
            String email = emailField.getText().trim();
            String course = courseField.getText().trim();

            if (name.isEmpty() || email.isEmpty() || course.isEmpty()) {
                throw new IllegalArgumentException("Please fill in all fields.");
            }
            return new Student(id, name, email, course);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Student ID must be a number.");
        }
    }

    private void addStudent() {
        try {
            Student s = readForm();
            try (Session session = sessionFactory.openSession()) {
                session.beginTransaction();
                session.persist(s);
                session.getTransaction().commit();
            }
            loadStudents();
            clearForm();
            message("Student added successfully.");
        } catch (Exception ex) {
            showError(ex);
        }
    }

    private void updateStudent() {
        try {
            Student s = readForm();
            try (Session session = sessionFactory.openSession()) {
                session.beginTransaction();
                Student existing = session.get(Student.class, s.getId());
                if (existing == null) {
                    throw new IllegalArgumentException("Student ID not found.");
                }
                existing.setName(s.getName());
                existing.setEmail(s.getEmail());
                existing.setCourse(s.getCourse());
                session.getTransaction().commit();
            }
            loadStudents();
            message("Student updated successfully.");
        } catch (Exception ex) {
            showError(ex);
        }
    }

    private void deleteStudent() {
        try {
            int id = Integer.parseInt(idField.getText().trim());
            int choice = JOptionPane.showConfirmDialog(frame,
                    "Delete student ID " + id + "?", "Confirm Delete",
                    JOptionPane.YES_NO_OPTION);
            if (choice != JOptionPane.YES_OPTION) return;

            try (Session session = sessionFactory.openSession()) {
                session.beginTransaction();
                Student existing = session.get(Student.class, id);
                if (existing == null) throw new IllegalArgumentException("Student ID not found.");
                session.remove(existing);
                session.getTransaction().commit();
            }
            loadStudents();
            clearForm();
            message("Student deleted successfully.");
        } catch (Exception ex) {
            showError(ex);
        }
    }

    private void loadStudents() {
        try (Session session = sessionFactory.openSession()) {
            List<Student> students = session.createQuery("from Student order by id", Student.class).list();
            model.setRowCount(0);
            for (Student s : students) {
                model.addRow(new Object[]{s.getId(), s.getName(), s.getEmail(), s.getCourse()});
            }
        } catch (Exception ex) {
            showError(ex);
        }
    }

    private void fillSelectedRow() {
        int row = table.getSelectedRow();
        if (row < 0) return;
        idField.setText(String.valueOf(model.getValueAt(row, 0)));
        nameField.setText(String.valueOf(model.getValueAt(row, 1)));
        emailField.setText(String.valueOf(model.getValueAt(row, 2)));
        courseField.setText(String.valueOf(model.getValueAt(row, 3)));
    }

    private void clearForm() {
        idField.setText("");
        nameField.setText("");
        emailField.setText("");
        courseField.setText("");
        table.clearSelection();
        idField.requestFocus();
    }

    private void message(String text) {
        JOptionPane.showMessageDialog(frame, text, "Student Management", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showError(Exception ex) {
        String msg = ex.getMessage();
        if (ex.getCause() != null && ex.getCause().getMessage() != null) msg = ex.getCause().getMessage();
        JOptionPane.showMessageDialog(frame, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }
}
