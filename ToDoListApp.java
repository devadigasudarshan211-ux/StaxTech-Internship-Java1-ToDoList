import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.ArrayList;

public class ToDoListApp extends JFrame {
    private DefaultListModel<TaskItem> taskListModel;
    private JList<TaskItem> taskList;
    private JTextField taskInput;
    private JButton addButton, deleteButton, updateButton, saveButton, clearButton;
    private static final String FILE_NAME = "tasks.txt";

    public ToDoListApp() {
        setTitle("📝 To-Do List");
        setSize(520, 560);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(15, 15));
        getContentPane().setBackground(new Color(245, 247, 250));

        // Title
        JLabel title = new JLabel("My To-Do List", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(new Color(41, 128, 185));
        title.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        add(title, BorderLayout.NORTH);

        // Task list
        taskListModel = new DefaultListModel<>();
        taskList = new JList<>(taskListModel);
        taskList.setCellRenderer(new CheckboxListRenderer());
        taskList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        taskList.setBackground(Color.WHITE);
        taskList.setFont(new Font("Segoe UI", Font.PLAIN, 16));

        // Toggle checkbox on click
        taskList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int index = taskList.locationToIndex(e.getPoint());
                if (index >= 0) {
                    TaskItem task = taskListModel.get(index);
                    task.setCompleted(!task.isCompleted());
                    taskList.repaint();
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(taskList);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(189, 195, 199), 1, true));
        add(scrollPane, BorderLayout.CENTER);

        // Buttons panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonPanel.setBackground(new Color(245, 247, 250));

        deleteButton = new JButton("Delete Task");
        updateButton = new JButton("Update Task");
        saveButton = new JButton("Save Tasks");
        clearButton = new JButton("Clear All");

        styleButton(deleteButton, new Color(231, 76, 60));
        styleButton(updateButton, new Color(52, 152, 219));
        styleButton(saveButton, new Color(241, 196, 15));
        styleButton(clearButton, new Color(155, 89, 182));

        buttonPanel.add(deleteButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(saveButton);
        buttonPanel.add(clearButton);
        add(buttonPanel, BorderLayout.NORTH);

        // Input section
        JPanel inputPanel = new JPanel(new BorderLayout(10, 10));
        inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        inputPanel.setBackground(new Color(245, 247, 250));

        taskInput = new JTextField();
        taskInput.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        inputPanel.add(taskInput, BorderLayout.CENTER);

        addButton = new JButton("Add Task");
        styleButton(addButton, new Color(46, 204, 113));
        inputPanel.add(addButton, BorderLayout.EAST);

        add(inputPanel, BorderLayout.SOUTH);

        // Action listeners
        addButton.addActionListener(e -> addTask());
        deleteButton.addActionListener(e -> deleteTask());
        updateButton.addActionListener(e -> updateTask());
        saveButton.addActionListener(e -> saveTasks());
        clearButton.addActionListener(e -> clearAllTasks());

        // Auto load tasks
        loadTasks();

        // Auto save on close
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                saveTasks();
                dispose();
                System.exit(0);
            }
        });

        setVisible(true);
    }

    private void styleButton(JButton button, Color color) {
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(color.darker(), 1),
                BorderFactory.createEmptyBorder(8, 16, 8, 16)
        ));
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(color.darker());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(color);
            }
        });
    }

    private void addTask() {
        String text = taskInput.getText().trim();
        if (!text.isEmpty()) {
            taskListModel.addElement(new TaskItem(text, false));
            taskInput.setText("");
        } else {
            JOptionPane.showMessageDialog(this, "Please enter a task!");
        }
    }

    private void deleteTask() {
        int index = taskList.getSelectedIndex();
        if (index >= 0) {
            taskListModel.remove(index);
        } else {
            JOptionPane.showMessageDialog(this, "Select a task to delete!");
        }
    }

    private void updateTask() {
        int index = taskList.getSelectedIndex();
        if (index >= 0) {
            TaskItem task = taskListModel.get(index);
            String newText = JOptionPane.showInputDialog(this, "Edit Task:", task.getText());
            if (newText != null && !newText.trim().isEmpty()) {
                task.setText(newText.trim());
                taskList.repaint();
            }
        } else {
            JOptionPane.showMessageDialog(this, "Select a task to update!");
        }
    }

    private void clearAllTasks() {
        if (taskListModel.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No tasks to clear!");
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to clear all tasks?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            taskListModel.clear();
        }
    }

    private void saveTasks() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_NAME))) {
            for (int i = 0; i < taskListModel.size(); i++) {
                TaskItem task = taskListModel.get(i);
                writer.write((task.isCompleted() ? "[x]" : "[ ]") + " " + task.getText());
                writer.newLine();
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error saving tasks: " + e.getMessage());
        }
    }

    private void loadTasks() {
        File file = new File(FILE_NAME);
        if (!file.exists()) return;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            taskListModel.clear();
            String line;
            while ((line = reader.readLine()) != null) {
                boolean completed = line.startsWith("[x]");
                String text = line.replaceFirst("\\[.\\] ", "");
                taskListModel.addElement(new TaskItem(text, completed));
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error loading tasks: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ToDoListApp::new);
    }
}

// ✅ Custom task object with checkbox state
class TaskItem {
    private String text;
    private boolean completed;

    public TaskItem(String text, boolean completed) {
        this.text = text;
        this.completed = completed;
    }

    public String getText() { return text; }
    public boolean isCompleted() { return completed; }

    public void setText(String text) { this.text = text; }
    public void setCompleted(boolean completed) { this.completed = completed; }

    @Override
    public String toString() { return text; }
}

// ✅ Custom cell renderer for checkbox-style tasks
class CheckboxListRenderer extends JCheckBox implements ListCellRenderer<TaskItem> {
    public Component getListCellRendererComponent(JList<? extends TaskItem> list, TaskItem value, int index,
                                                  boolean isSelected, boolean cellHasFocus) {
        setText(value.getText());
        setSelected(value.isCompleted());
        setFont(new Font("Segoe UI", Font.PLAIN, 16));
        setBackground(isSelected ? new Color(220, 230, 241) : Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        return this;
    }
}
