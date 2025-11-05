import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Date;
import java.util.Random;
import java.util.ArrayList;
import javax.swing.table.DefaultTableModel;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.io.File;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

// Main Application Entry Point
public class CtrlYouApp {
    public static void main(String[] args) {
        // Initialize database on startup
        DatabaseManager.initializeDatabase();
        
        SwingUtilities.invokeLater(() -> {
            new DashboardFrame().setVisible(true);
        });
    }
}

// Icon Manager Class - Handles all icon loading
class IconManager {
    private static final String ICON_PATH = "icons/";
    
    // Load icon from file with fallback
    public static ImageIcon loadIcon(String filename, int width, int height) {
        try {
            // Try to load from icons folder
            File iconFile = new File(ICON_PATH + filename);
            if (iconFile.exists()) {
                BufferedImage img = ImageIO.read(iconFile);
                Image scaledImg = img.getScaledInstance(width, height, Image.SCALE_SMOOTH);
                return new ImageIcon(scaledImg);
            }
        } catch (Exception e) {
            System.out.println("Could not load icon: " + filename);
        }
        
        // Fallback: Create a colored circle icon
        return createFallbackIcon(width, height, getFallbackColor(filename));
    }
    
    // Create a simple colored circle as fallback
    private static ImageIcon createFallbackIcon(int width, int height, Color color) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        g2d.setColor(color);
        g2d.fillOval(2, 2, width-4, height-4);
        
        g2d.setColor(color.darker());
        g2d.setStroke(new BasicStroke(2));
        g2d.drawOval(2, 2, width-4, height-4);
        
        g2d.dispose();
        return new ImageIcon(image);
    }
    
    // Get color based on icon name
    private static Color getFallbackColor(String filename) {
        if (filename.contains("home")) return ColorPalette.ACCENT_PINK;
        if (filename.contains("notes")) return ColorPalette.SOFT_PINK;
        if (filename.contains("productivity")) return ColorPalette.LAVENDER;
        if (filename.contains("wellness")) return ColorPalette.PALE_ROSE;
        if (filename.contains("gift")) return new Color(255, 215, 0);
        return ColorPalette.MEDIUM_PURPLE;
    }
    
    // Create emoji-style icon (works without image files!)
    public static JLabel createEmojiIcon(String emoji, int size) {
        JLabel label = new JLabel(emoji);
        label.setFont(new Font("Segoe UI Emoji", Font.PLAIN, size));
        label.setHorizontalAlignment(SwingConstants.CENTER);
        return label;
    }
}

// Database Manager Class (same as before, with mood task additions)
class DatabaseManager {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/ctrlyou_db";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "vaanya"; 
    
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }
    
    public static void initializeDatabase() {
        try {
            Connection conn = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/", DB_USER, DB_PASSWORD);
            Statement stmt = conn.createStatement();
            stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS ctrlyou_db");
            stmt.close();
            conn.close();
            
            conn = getConnection();
            stmt = conn.createStatement();
            
            String createNotesTable = "CREATE TABLE IF NOT EXISTS notes (" +
                "id INT AUTO_INCREMENT PRIMARY KEY," +
                "title VARCHAR(255) NOT NULL," +
                "content TEXT," +
                "created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "modified_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP" +
                ")";
            stmt.executeUpdate(createNotesTable);
            
            String createTasksTable = "CREATE TABLE IF NOT EXISTS tasks (" +
                "id INT AUTO_INCREMENT PRIMARY KEY," +
                "task_name VARCHAR(255) NOT NULL," +
                "status VARCHAR(50) DEFAULT 'Pending'," +
                "created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ")";
            stmt.executeUpdate(createTasksTable);
            
            String createMoodTable = "CREATE TABLE IF NOT EXISTS mood_entries (" +
                "id INT AUTO_INCREMENT PRIMARY KEY," +
                "mood VARCHAR(100) NOT NULL," +
                "entry_date DATE," +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ")";
            stmt.executeUpdate(createMoodTable);
            
            String createJournalTable = "CREATE TABLE IF NOT EXISTS journal_entries (" +
                "id INT AUTO_INCREMENT PRIMARY KEY," +
                "content TEXT," +
                "entry_date DATE," +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ")";
            stmt.executeUpdate(createJournalTable);
            
            String createMindMapTable = "CREATE TABLE IF NOT EXISTS mindmap_nodes (" +
                "id INT AUTO_INCREMENT PRIMARY KEY," +
                "mindmap_id INT," +
                "node_text VARCHAR(255)," +
                "position_x INT," +
                "position_y INT," +
                "created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ")";
            stmt.executeUpdate(createMindMapTable);
            
            stmt.close();
            conn.close();
            
            System.out.println("Database initialized successfully!");
            
        } catch (SQLException e) {
            System.err.println("Database initialization error: " + e.getMessage());
            JOptionPane.showMessageDialog(null, 
                "Database connection failed! Please check your MySQL settings.\nError: " + e.getMessage(),
                "Database Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    // NOTES OPERATIONS
    public static void saveNote(String title, String content) {
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(
                 "INSERT INTO notes (title, content) VALUES (?, ?)")) {
            pstmt.setString(1, title);
            pstmt.setString(2, content);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public static void updateNote(String oldTitle, String newTitle, String content) {
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(
                 "UPDATE notes SET title = ?, content = ? WHERE title = ?")) {
            pstmt.setString(1, newTitle);
            pstmt.setString(2, content);
            pstmt.setString(3, oldTitle);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public static void deleteNote(String title) {
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(
                 "DELETE FROM notes WHERE title = ?")) {
            pstmt.setString(1, title);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public static ArrayList<String> getAllNotes() {
        ArrayList<String> notes = new ArrayList<>();
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT title FROM notes ORDER BY modified_date DESC")) {
            while (rs.next()) {
                notes.add(rs.getString("title"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return notes;
    }
    
    public static String getNoteContent(String title) {
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(
                 "SELECT content FROM notes WHERE title = ?")) {
            pstmt.setString(1, title);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString("content");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "";
    }
    
    public static int getNotesCount() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as count FROM notes")) {
            if (rs.next()) {
                return rs.getInt("count");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
    
    // TASK OPERATIONS
    public static void addTask(String taskName) {
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(
                 "INSERT INTO tasks (task_name, status) VALUES (?, 'Pending')")) {
            pstmt.setString(1, taskName);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public static void updateTaskStatus(String taskName, String status) {
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(
                 "UPDATE tasks SET status = ? WHERE task_name = ?")) {
            pstmt.setString(1, status);
            pstmt.setString(2, taskName);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public static void deleteTask(String taskName) {
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(
                 "DELETE FROM tasks WHERE task_name = ?")) {
            pstmt.setString(1, taskName);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public static ArrayList<Object[]> getAllTasks() {
        ArrayList<Object[]> tasks = new ArrayList<>();
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT task_name, status FROM tasks ORDER BY created_date DESC")) {
            while (rs.next()) {
                tasks.add(new Object[]{rs.getString("task_name"), rs.getString("status")});
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return tasks;
    }
    
    // MOOD OPERATIONS
    public static void saveMoodEntry(String mood) {
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(
                 "INSERT INTO mood_entries (mood, entry_date) VALUES (?, CURDATE())")) {
            pstmt.setString(1, mood);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public static int getMoodEntriesCount() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as count FROM mood_entries")) {
            if (rs.next()) {
                return rs.getInt("count");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
    
    // JOURNAL OPERATIONS
    public static void saveJournalEntry(String content) {
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(
                 "INSERT INTO journal_entries (content, entry_date) VALUES (?, CURDATE())")) {
            pstmt.setString(1, content);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public static ArrayList<Object[]> getAllJournalEntries() {
        ArrayList<Object[]> entries = new ArrayList<>();
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT id, content, entry_date FROM journal_entries ORDER BY entry_date DESC")) {
            while (rs.next()) {
                entries.add(new Object[]{
                    rs.getInt("id"),
                    rs.getString("content"),
                    rs.getDate("entry_date")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return entries;
    }
    
    // MINDMAP OPERATIONS
    public static void saveMindMap(int mindmapId, ArrayList<MindMapNode> nodes) {
        try (Connection conn = getConnection()) {
            PreparedStatement deletePstmt = conn.prepareStatement(
                "DELETE FROM mindmap_nodes WHERE mindmap_id = ?");
            deletePstmt.setInt(1, mindmapId);
            deletePstmt.executeUpdate();
            
            PreparedStatement insertPstmt = conn.prepareStatement(
                "INSERT INTO mindmap_nodes (mindmap_id, node_text, position_x, position_y) VALUES (?, ?, ?, ?)");
            
            for (MindMapNode node : nodes) {
                insertPstmt.setInt(1, mindmapId);
                insertPstmt.setString(2, node.text);
                insertPstmt.setInt(3, node.x);
                insertPstmt.setInt(4, node.y);
                insertPstmt.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public static ArrayList<MindMapNode> loadMindMap(int mindmapId) {
        ArrayList<MindMapNode> nodes = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(
                 "SELECT node_text, position_x, position_y FROM mindmap_nodes WHERE mindmap_id = ?")) {
            pstmt.setInt(1, mindmapId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                nodes.add(new MindMapNode(
                    rs.getString("node_text"),
                    rs.getInt("position_x"),
                    rs.getInt("position_y")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return nodes;
    }
}

// Mood Gift Box - Provides tasks and playlists based on mood
class MoodGiftBox {
    
    public static void showGiftBox(Component parent, String mood) {
        // Get recommendations based on mood
        String task = getTaskForMood(mood);
        String playlist = getPlaylistForMood(mood);
        String message = getEncouragementMessage(mood);
        
        // Create gift box dialog
        JDialog giftDialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(parent), "Your Wellness Gift Box 🎁", true);
        giftDialog.setSize(550, 450);
        giftDialog.setLocationRelativeTo(parent);
        
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(new Color(255, 250, 245));
        mainPanel.setBorder(new EmptyBorder(30, 30, 30, 30));
        
        // Gift box animation label
        JLabel giftIcon = IconManager.createEmojiIcon("🎁", 60);
        giftIcon.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(giftIcon);
        
        mainPanel.add(Box.createVerticalStrut(20));
        
        // Title
        JLabel title = new JLabel("A Gift Just For You!");
        title.setFont(new Font("Georgia", Font.BOLD, 26));
        title.setForeground(ColorPalette.MEDIUM_PURPLE);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(title);
        
        mainPanel.add(Box.createVerticalStrut(20));
        
        // Encouragement message
        JTextArea messageArea = new JTextArea(message);
        messageArea.setFont(new Font("Georgia", Font.ITALIC, 14));
        messageArea.setForeground(ColorPalette.TEXT_DARK);
        messageArea.setBackground(new Color(255, 250, 245));
        messageArea.setLineWrap(true);
        messageArea.setWrapStyleWord(true);
        messageArea.setEditable(false);
        messageArea.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(messageArea);
        
        mainPanel.add(Box.createVerticalStrut(25));
        
        // Task Panel
        JPanel taskPanel = createGiftItemPanel("✨ Suggested Activity", task, ColorPalette.SOFT_PINK);
        mainPanel.add(taskPanel);
        
        mainPanel.add(Box.createVerticalStrut(15));
        
        // Playlist Panel
        JPanel playlistPanel = createGiftItemPanel("🎵 Playlist Recommendation", playlist, ColorPalette.LILAC);
        mainPanel.add(playlistPanel);
        
        mainPanel.add(Box.createVerticalStrut(25));
        
        // Close button
        JButton closeBtn = new JButton("Thank You! 💝");
        closeBtn.setBackground(ColorPalette.MEDIUM_PURPLE);
        closeBtn.setForeground(Color.WHITE);
        closeBtn.setFocusPainted(false);
        closeBtn.setBorderPainted(false);
        closeBtn.setFont(new Font("Georgia", Font.BOLD, 16));
        closeBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        closeBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        closeBtn.addActionListener(e -> giftDialog.dispose());
        mainPanel.add(closeBtn);
        
        giftDialog.add(mainPanel);
        giftDialog.setVisible(true);
    }
    
    private static JPanel createGiftItemPanel(String title, String content, Color bgColor) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(bgColor);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ColorPalette.LAVENDER, 2),
            new EmptyBorder(15, 15, 15, 15)));
        panel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Georgia", Font.BOLD, 16));
        titleLabel.setForeground(ColorPalette.TEXT_DARK);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(titleLabel);
        
        panel.add(Box.createVerticalStrut(8));
        
        JTextArea contentArea = new JTextArea(content);
        contentArea.setFont(new Font("Georgia", Font.PLAIN, 14));
        contentArea.setForeground(ColorPalette.TEXT_DARK);
        contentArea.setBackground(bgColor);
        contentArea.setLineWrap(true);
        contentArea.setWrapStyleWord(true);
        contentArea.setEditable(false);
        contentArea.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(contentArea);
        
        return panel;
    }
    
    private static String getTaskForMood(String mood) {
        switch (mood) {
            case "Happy & Energetic":
                return "Channel this amazing energy! Try: Dance to your favorite song, start that project you've been excited about, or call a friend to spread the joy!";
            case "Calm & Peaceful":
                return "Embrace this serenity! Try: Practice gratitude journaling, enjoy a mindful tea break, or take a peaceful nature walk.";
            case "Neutral":
                return "Perfect time for self-care! Try: Organize your space, try a new hobby, or simply enjoy doing nothing – you deserve rest!";
            case "Sad or Down":
                return "Be gentle with yourself. Try: Watch a comfort movie, create a cozy space with blankets and warm drinks, or reach out to someone you trust.";
            case "Anxious":
                return "Let's ease that anxiety. Try: 5-4-3-2-1 grounding technique, gentle stretching or yoga, or write down your worries to get them out of your head.";
            case "Stressed or Overwhelmed":
                return "Time to decompress. Try: Take a 10-minute break outside, do a brain dump of everything on your mind, or try box breathing (4-4-4-4).";
            default:
                return "Take a moment for yourself today!";
        }
    }
    
    private static String getPlaylistForMood(String mood) {
        switch (mood) {
            case "Happy & Energetic":
                return "🎉 'Feel Good Vibes' - Upbeat pop and dance tracks to keep your energy high! Artists: Dua Lipa, Harry Styles, Lizzo";
            case "Calm & Peaceful":
                return "🌊 'Peaceful Moments' - Soft acoustic and ambient sounds for relaxation. Artists: Norah Jones, Bon Iver, Ólafur Arnalds";
            case "Neutral":
                return "☕ 'Easy Listening' - Chill indie and lo-fi beats for any activity. Artists: Rex Orange County, Clairo, The Neighbourhood";
            case "Sad or Down":
                return "💙 'Healing Hearts' - Comforting songs that validate your feelings. Artists: Taylor Swift, Lewis Capaldi, Adele";
            case "Anxious":
                return "🧘 'Calm & Centered' - Soothing instrumental and nature sounds. Artists: Ludovico Einaudi, Enya, Piano Guys";
            case "Stressed or Overwhelmed":
                return "🌸 'Stress Relief' - Gentle melodies to help you unwind. Artists: Yiruma, Max Richter, Sleeping At Last";
            default:
                return "🎵 'Daily Favorites' - A mix for any moment!";
        }
    }
    
    private static String getEncouragementMessage(String mood) {
        switch (mood) {
            case "Happy & Energetic":
                return "Your positive energy is contagious! This is a beautiful moment – savor it and use this momentum!";
            case "Calm & Peaceful":
                return "What a lovely state of mind. Your inner peace is a gift to yourself and those around you.";
            case "Neutral":
                return "Neutral is perfectly valid! Not every day needs to be extreme. You're doing just fine.";
            case "Sad or Down":
                return "It's okay to not be okay. Your feelings are valid, and this heaviness won't last forever. Be patient with yourself.";
            case "Anxious":
                return "Your anxiety is real, but it doesn't define you. You've gotten through this before, and you will again. One breath at a time.";
            case "Stressed or Overwhelmed":
                return "You're carrying a lot right now. Remember: you don't have to do everything at once. Break it down, breathe, and be kind to yourself.";
            default:
                return "Thank you for checking in with yourself. That's already an act of self-care!";
        }
    }
}

// Color Palette
class ColorPalette {
    public static final Color SOFT_PINK = new Color(255, 228, 240);
    public static final Color LILAC = new Color(230, 220, 250);
    public static final Color LAVENDER = new Color(200, 190, 230);
    public static final Color LIGHT_PURPLE = new Color(180, 160, 220);
    public static final Color MEDIUM_PURPLE = new Color(160, 130, 200);
    public static final Color PALE_ROSE = new Color(255, 210, 230);
    public static final Color CREAM = new Color(255, 250, 245);
    public static final Color SOFT_GRAY = new Color(240, 235, 245);
    public static final Color TEXT_DARK = new Color(80, 60, 100);
    public static final Color ACCENT_PINK = new Color(255, 182, 193);
}

// Dashboard Frame
class DashboardFrame extends JFrame {
    private JPanel contentPanel;
    private CardLayout cardLayout;
    
    public DashboardFrame() {
        setTitle("Ctrl+You - Digital Planner");
        setSize(1300, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(ColorPalette.CREAM);
        
        JPanel sidebar = createSidebar();
        mainPanel.add(sidebar, BorderLayout.WEST);
        
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(ColorPalette.CREAM);
        
        contentPanel.add(new HomePanel(this), "home");
        contentPanel.add(new NotesPanel(), "notes");
        contentPanel.add(new ProductivityPanel(), "productivity");
        contentPanel.add(new WellnessPanel(), "wellness");
        
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        add(mainPanel);
    }
    
    public void showPanel(String panelName) {
        cardLayout.show(contentPanel, panelName);
    }
    
    private JPanel createSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(ColorPalette.LILAC);
        sidebar.setPreferredSize(new Dimension(280, 800));
        sidebar.setBorder(new EmptyBorder(30, 20, 30, 20));
        
        JLabel logo = new JLabel("Ctrl+You");
        logo.setFont(new Font("Georgia", Font.BOLD, 28));
        logo.setForeground(ColorPalette.TEXT_DARK);
        logo.setAlignmentX(Component.CENTER_ALIGNMENT);
        sidebar.add(logo);
        
        sidebar.add(Box.createVerticalStrut(10));
        
        JLabel subtitle = new JLabel("Digital Planner");
        subtitle.setFont(new Font("Georgia", Font.ITALIC, 14));
        subtitle.setForeground(ColorPalette.MEDIUM_PURPLE);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        sidebar.add(subtitle);
        
        sidebar.add(Box.createVerticalStrut(50));
        
        addMenuItem(sidebar, "Home", "home");
        addMenuItem(sidebar, "Notes & Mind Maps", "notes");
        addMenuItem(sidebar, "Productivity", "productivity");
        addMenuItem(sidebar, "Wellness Journal", "wellness");
        
        sidebar.add(Box.createVerticalGlue());
        
        JLabel footer = new JLabel("<html><center>Hit Ctrl,<br>Not Panic</center></html>");
        footer.setFont(new Font("Georgia", Font.BOLD, 16));
        footer.setForeground(ColorPalette.MEDIUM_PURPLE);
        footer.setAlignmentX(Component.CENTER_ALIGNMENT);
        sidebar.add(footer);
        
        return sidebar;
    }
    
    private void addMenuItem(JPanel sidebar, String text, String cardName) {
        JButton btn = new JButton(text);
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setMaximumSize(new Dimension(240, 50));
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        btn.setBackground(ColorPalette.SOFT_PINK);
        btn.setForeground(ColorPalette.TEXT_DARK);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ColorPalette.LAVENDER, 1),
            new EmptyBorder(10, 20, 10, 20)));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> {
            cardLayout.show(contentPanel, cardName);
            updateButtonStyles(sidebar, btn);
        });
        
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(ColorPalette.PALE_ROSE);
            }
            public void mouseExited(MouseEvent e) {
                if (!btn.getBackground().equals(ColorPalette.LAVENDER)) {
                    btn.setBackground(ColorPalette.SOFT_PINK);
                }
            }
        });
        
        sidebar.add(btn);
        sidebar.add(Box.createVerticalStrut(15));
    }
    
    private void updateButtonStyles(JPanel sidebar, JButton selectedBtn) {
        for (Component comp : sidebar.getComponents()) {
            if (comp instanceof JButton) {
                JButton btn = (JButton) comp;
                if (btn == selectedBtn) {
                    btn.setBackground(ColorPalette.LAVENDER);
                } else {
                    btn.setBackground(ColorPalette.SOFT_PINK);
                }
            }
        }
    }
}

// Home Panel - WITH ICONS
class HomePanel extends JPanel {
    private DashboardFrame parentFrame;
    
    public HomePanel(DashboardFrame parent) {
        this.parentFrame = parent;
        setLayout(new BorderLayout());
        setBackground(ColorPalette.CREAM);
        
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(ColorPalette.CREAM);
        contentPanel.setBorder(new EmptyBorder(40, 50, 40, 50));
        
        JLabel welcome = new JLabel("Welcome to Your Digital Planner");
        welcome.setFont(new Font("Georgia", Font.BOLD, 36));
        welcome.setForeground(ColorPalette.TEXT_DARK);
        welcome.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentPanel.add(welcome);
        
        contentPanel.add(Box.createVerticalStrut(15));
        
        JLabel subtitle = new JLabel("Organize your thoughts, boost your productivity, nurture your wellness");
        subtitle.setFont(new Font("Georgia", Font.ITALIC, 18));
        subtitle.setForeground(ColorPalette.MEDIUM_PURPLE);
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentPanel.add(subtitle);
        
        contentPanel.add(Box.createVerticalStrut(50));
        
        JPanel statsPanel = new JPanel(new GridLayout(1, 3, 30, 0));
        statsPanel.setBackground(ColorPalette.CREAM);
        statsPanel.setMaximumSize(new Dimension(1000, 140));
        statsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        statsPanel.add(createStatCard("Notes Created", String.valueOf(DatabaseManager.getNotesCount())));
        statsPanel.add(createStatCard("Active Tasks", String.valueOf(DatabaseManager.getAllTasks().size())));
        statsPanel.add(createStatCard("Mood Check-ins", String.valueOf(DatabaseManager.getMoodEntriesCount())));
        
        contentPanel.add(statsPanel);
        
        contentPanel.add(Box.createVerticalStrut(50));
        
        JLabel quickActions = new JLabel("Quick Actions");
        quickActions.setFont(new Font("Georgia", Font.BOLD, 26));
        quickActions.setForeground(ColorPalette.TEXT_DARK);
        quickActions.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentPanel.add(quickActions);
        
        contentPanel.add(Box.createVerticalStrut(25));
        
        JPanel actionsPanel = new JPanel(new GridLayout(2, 2, 25, 25));
        actionsPanel.setBackground(ColorPalette.CREAM);
        actionsPanel.setMaximumSize(new Dimension(1000, 340));
        actionsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        // Action cards with icons (using emojis as fallback)
        actionsPanel.add(createActionCardWithIcon("📝", "Create Mind Map", "Visualize your concepts", "notes"));
        actionsPanel.add(createActionCardWithIcon("⏱️", "Start Focus Timer", "Begin a productive session", "productivity"));
        actionsPanel.add(createActionCardWithIcon("✅", "View Tasks", "Check your to-do list", "productivity"));
        actionsPanel.add(createActionCardWithIcon("💝", "Mood Check-in", "How are you feeling today?", "wellness"));
        
        contentPanel.add(actionsPanel);
        
        add(new JScrollPane(contentPanel), BorderLayout.CENTER);
    }
    
    private JPanel createStatCard(String label, String value) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(ColorPalette.SOFT_PINK);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ColorPalette.LAVENDER, 2),
            new EmptyBorder(25, 25, 25, 25)));
        
        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Georgia", Font.BOLD, 42));
        valueLabel.setForeground(ColorPalette.MEDIUM_PURPLE);
        valueLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel labelLabel = new JLabel(label);
        labelLabel.setFont(new Font("Georgia", Font.PLAIN, 16));
        labelLabel.setForeground(ColorPalette.TEXT_DARK);
        labelLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        card.add(valueLabel);
        card.add(Box.createVerticalStrut(8));
        card.add(labelLabel);
        
        return card;
    }
    
    private JPanel createActionCardWithIcon(String emoji, String title, String desc, String targetPanel) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(ColorPalette.LILAC);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ColorPalette.LAVENDER, 2),
            new EmptyBorder(25, 25, 25, 25)));
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Add emoji icon at top
        JLabel iconLabel = IconManager.createEmojiIcon(emoji, 48);
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(iconLabel);
        
        card.add(Box.createVerticalStrut(15));
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Georgia", Font.BOLD, 18));
        titleLabel.setForeground(ColorPalette.TEXT_DARK);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel descLabel = new JLabel(desc);
        descLabel.setFont(new Font("Georgia", Font.ITALIC, 14));
        descLabel.setForeground(ColorPalette.MEDIUM_PURPLE);
        descLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        card.add(titleLabel);
        card.add(Box.createVerticalStrut(8));
        card.add(descLabel);
        
        card.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                parentFrame.showPanel(targetPanel);
            }
            public void mouseEntered(MouseEvent e) {
                card.setBackground(ColorPalette.PALE_ROSE);
            }
            public void mouseExited(MouseEvent e) {
                card.setBackground(ColorPalette.LILAC);
            }
        });
        
        return card;
    }
}

// Notes Panel (same as before)
class NotesPanel extends JPanel {
    private DefaultListModel<String> notesListModel;
    private JTextArea noteContentArea;
    private String currentNoteTitle = null;
    private JList<String> notesList;
    
    public NotesPanel() {
        setLayout(new BorderLayout(20, 20));
        setBackground(ColorPalette.CREAM);
        setBorder(new EmptyBorder(40, 50, 40, 50));
        
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(ColorPalette.CREAM);
        
        JLabel title = new JLabel("Notes & Mind Maps");
        title.setFont(new Font("Georgia", Font.BOLD, 32));
        title.setForeground(ColorPalette.TEXT_DARK);
        headerPanel.add(title, BorderLayout.WEST);
        
        JButton newNoteBtn = new JButton("+ New Note");
        newNoteBtn.setBackground(ColorPalette.MEDIUM_PURPLE);
        newNoteBtn.setForeground(Color.WHITE);
        newNoteBtn.setFocusPainted(false);
        newNoteBtn.setBorderPainted(false);
        newNoteBtn.setFont(new Font("Georgia", Font.BOLD, 15));
        newNoteBtn.addActionListener(e -> createNewNote());
        headerPanel.add(newNoteBtn, BorderLayout.EAST);
        
        add(headerPanel, BorderLayout.NORTH);
        
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(320);
        splitPane.setBackground(ColorPalette.CREAM);
        
        notesListModel = new DefaultListModel<>();
        loadNotesFromDatabase();
        
        notesList = new JList<>(notesListModel);
        notesList.setFont(new Font("Georgia", Font.PLAIN, 16));
        notesList.setBackground(ColorPalette.SOFT_PINK);
        notesList.setForeground(ColorPalette.TEXT_DARK);
        notesList.setSelectionBackground(ColorPalette.LAVENDER);
        notesList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        notesList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                loadNoteContent(notesList.getSelectedValue());
            }
        });
        
        JScrollPane listScroll = new JScrollPane(notesList);
        listScroll.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(ColorPalette.LAVENDER, 2),
            "My Notes",
            TitledBorder.DEFAULT_JUSTIFICATION,
            TitledBorder.DEFAULT_POSITION,
            new Font("Georgia", Font.BOLD, 14),
            ColorPalette.TEXT_DARK));
        splitPane.setLeftComponent(listScroll);
        
        JPanel editorPanel = new JPanel(new BorderLayout(10, 10));
        editorPanel.setBackground(ColorPalette.LILAC);
        editorPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        
        noteContentArea = new JTextArea();
        noteContentArea.setFont(new Font("Georgia", Font.PLAIN, 16));
        noteContentArea.setLineWrap(true);
        noteContentArea.setWrapStyleWord(true);
        noteContentArea.setBackground(Color.WHITE);
        noteContentArea.setForeground(ColorPalette.TEXT_DARK);
        noteContentArea.setText("Select a note to view or edit...");
        noteContentArea.setCaretColor(ColorPalette.MEDIUM_PURPLE);
        
        JScrollPane editorScroll = new JScrollPane(noteContentArea);
        editorScroll.setBorder(BorderFactory.createLineBorder(ColorPalette.LAVENDER, 2));
        editorPanel.add(editorScroll, BorderLayout.CENTER);
        
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        toolbar.setBackground(ColorPalette.LILAC);
        
        JButton saveBtn = createToolbarButton("Save");
        JButton deleteBtn = createToolbarButton("Delete Note");
        JButton mindMapBtn = createToolbarButton("Mind Map View");
        
        saveBtn.addActionListener(e -> saveCurrentNote());
        deleteBtn.addActionListener(e -> deleteCurrentNote());
        mindMapBtn.addActionListener(e -> openMindMapEditor());
        
        toolbar.add(saveBtn);
        toolbar.add(deleteBtn);
        toolbar.add(mindMapBtn);
        
        editorPanel.add(toolbar, BorderLayout.SOUTH);
        splitPane.setRightComponent(editorPanel);
        
        add(splitPane, BorderLayout.CENTER);
    }
    
    private void loadNotesFromDatabase() {
        notesListModel.clear();
        ArrayList<String> notes = DatabaseManager.getAllNotes();
        for (String note : notes) {
            notesListModel.addElement(note);
        }
    }
    
    private JButton createToolbarButton(String text) {
        JButton btn = new JButton(text);
        btn.setBackground(ColorPalette.SOFT_PINK);
        btn.setForeground(ColorPalette.TEXT_DARK);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ColorPalette.LAVENDER, 1),
            new EmptyBorder(8, 15, 8, 15)));
        btn.setFont(new Font("Georgia", Font.PLAIN, 13));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }
    
    private void createNewNote() {
        String noteName = JOptionPane.showInputDialog(this, "Enter note name:");
        if (noteName != null && !noteName.trim().isEmpty()) {
            DatabaseManager.saveNote(noteName, "");
            notesListModel.addElement(noteName);
            currentNoteTitle = noteName;
            noteContentArea.setText("");
        }
    }
    
    private void loadNoteContent(String noteName) {
        if (noteName != null) {
            currentNoteTitle = noteName;
            String content = DatabaseManager.getNoteContent(noteName);
            noteContentArea.setText(content);
        }
    }
    
    private void saveCurrentNote() {
        if (currentNoteTitle != null) {
            String content = noteContentArea.getText();
            DatabaseManager.updateNote(currentNoteTitle, currentNoteTitle, content);
            JOptionPane.showMessageDialog(this, "Note saved successfully!", "Saved", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "Please select or create a note first!", "Error", JOptionPane.WARNING_MESSAGE);
        }
    }
    
    private void deleteCurrentNote() {
        if (currentNoteTitle != null) {
            int confirm = JOptionPane.showConfirmDialog(this, 
                "Are you sure you want to delete the note '" + currentNoteTitle + "'?", 
                "Confirm Delete", 
                JOptionPane.YES_NO_OPTION);
            
            if (confirm == JOptionPane.YES_OPTION) {
                DatabaseManager.deleteNote(currentNoteTitle);
                notesListModel.removeElement(currentNoteTitle);
                currentNoteTitle = null;
                noteContentArea.setText("Select a note to view or edit...");
                JOptionPane.showMessageDialog(this, "Note deleted successfully!", "Deleted", JOptionPane.INFORMATION_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select a note to delete!", "Error", JOptionPane.WARNING_MESSAGE);
        }
    }
    
    private void openMindMapEditor() {
        new MindMapFrame().setVisible(true);
    }
}

// Mind Map Frame
class MindMapFrame extends JFrame {
    private MindMapCanvas canvas;
    private int currentMindMapId = 1;
    
    public MindMapFrame() {
        setTitle("Mind Map Editor");
        setSize(1000, 700);
        setLocationRelativeTo(null);
        setBackground(ColorPalette.CREAM);
        
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(ColorPalette.CREAM);
        
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        toolbar.setBackground(ColorPalette.LILAC);
        toolbar.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        JButton addNodeBtn = createToolbarButton("Add Node");
        JButton deleteBtn = createToolbarButton("Delete");
        JButton clearBtn = createToolbarButton("Clear All");
        JButton saveBtn = createToolbarButton("Save");
        
        canvas = new MindMapCanvas();
        
        addNodeBtn.addActionListener(e -> canvas.addNode());
        deleteBtn.addActionListener(e -> canvas.deleteSelectedNode());
        clearBtn.addActionListener(e -> canvas.clearAll());
        saveBtn.addActionListener(e -> {
            DatabaseManager.saveMindMap(currentMindMapId, canvas.getNodes());
            JOptionPane.showMessageDialog(this, "Mind map saved!", "Success", JOptionPane.INFORMATION_MESSAGE);
        });
        
        toolbar.add(addNodeBtn);
        toolbar.add(deleteBtn);
        toolbar.add(clearBtn);
        toolbar.add(saveBtn);
        
        mainPanel.add(toolbar, BorderLayout.NORTH);
        mainPanel.add(canvas, BorderLayout.CENTER);
        
        JLabel instructions = new JLabel("Click 'Add Node' to create nodes - Drag nodes to move - Click to select - Double-click to edit text");
        instructions.setFont(new Font("Georgia", Font.ITALIC, 12));
        instructions.setForeground(ColorPalette.MEDIUM_PURPLE);
        instructions.setBorder(new EmptyBorder(10, 10, 10, 10));
        instructions.setHorizontalAlignment(SwingConstants.CENTER);
        mainPanel.add(instructions, BorderLayout.SOUTH);
        
        add(mainPanel);
    }
    
    private JButton createToolbarButton(String text) {
        JButton btn = new JButton(text);
        btn.setBackground(ColorPalette.SOFT_PINK);
        btn.setForeground(ColorPalette.TEXT_DARK);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ColorPalette.LAVENDER, 1),
            new EmptyBorder(8, 15, 8, 15)));
        btn.setFont(new Font("Georgia", Font.PLAIN, 13));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }
}

// Mind Map Canvas
class MindMapCanvas extends JPanel {
    private ArrayList<MindMapNode> nodes;
    private MindMapNode selectedNode;
    private MindMapNode draggingNode;
    private Point dragOffset;
    
    public MindMapCanvas() {
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createLineBorder(ColorPalette.LAVENDER, 2));
        nodes = new ArrayList<>();
        
        nodes.add(new MindMapNode("Main Idea", 450, 300));
        
        addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                handleMousePress(e);
            }
            
            public void mouseReleased(MouseEvent e) {
                draggingNode = null;
            }
            
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && selectedNode != null) {
                    editNode();
                }
            }
        });
        
        addMouseMotionListener(new MouseAdapter() {
            public void mouseDragged(MouseEvent e) {
                if (draggingNode != null) {
                    draggingNode.x = e.getX() - dragOffset.x;
                    draggingNode.y = e.getY() - dragOffset.y;
                    repaint();
                }
            }
        });
    }
    
    public ArrayList<MindMapNode> getNodes() {
        return nodes;
    }
    
    public void setNodes(ArrayList<MindMapNode> newNodes) {
        this.nodes = newNodes;
        repaint();
    }
    
    private void handleMousePress(MouseEvent e) {
        selectedNode = null;
        for (MindMapNode node : nodes) {
            if (node.contains(e.getPoint())) {
                selectedNode = node;
                draggingNode = node;
                dragOffset = new Point(e.getX() - node.x, e.getY() - node.y);
                break;
            }
        }
        repaint();
    }
    
    public void addNode() {
        String text = JOptionPane.showInputDialog(this, "Enter node text:");
        if (text != null && !text.trim().isEmpty()) {
            Random rand = new Random();
            int x = 200 + rand.nextInt(600);
            int y = 100 + rand.nextInt(400);
            nodes.add(new MindMapNode(text, x, y));
            repaint();
        }
    }
    
    public void deleteSelectedNode() {
        if (selectedNode != null && nodes.size() > 1) {
            nodes.remove(selectedNode);
            selectedNode = null;
            repaint();
        }
    }
    
    public void clearAll() {
        int confirm = JOptionPane.showConfirmDialog(this, 
            "Clear all nodes?", 
            "Confirm", 
            JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            nodes.clear();
            nodes.add(new MindMapNode("Main Idea", 450, 300));
            selectedNode = null;
            repaint();
        }
    }
    
    private void editNode() {
        String newText = JOptionPane.showInputDialog(this, "Edit node text:", selectedNode.text);
        if (newText != null && !newText.trim().isEmpty()) {
            selectedNode.text = newText;
            repaint();
        }
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        g2d.setColor(ColorPalette.LAVENDER);
        g2d.setStroke(new BasicStroke(2));
        if (nodes.size() > 1) {
            MindMapNode center = nodes.get(0);
            for (int i = 1; i < nodes.size(); i++) {
                MindMapNode node = nodes.get(i);
                g2d.drawLine(center.x + 60, center.y + 30, node.x + 60, node.y + 30);
            }
        }
        
        for (MindMapNode node : nodes) {
            node.draw(g2d, node == selectedNode);
        }
    }
}

// Mind Map Node
class MindMapNode {
    String text;
    int x, y;
    int width = 120;
    int height = 60;
    
    public MindMapNode(String text, int x, int y) {
        this.text = text;
        this.x = x;
        this.y = y;
    }
    
    public boolean contains(Point p) {
        return p.x >= x && p.x <= x + width && p.y >= y && p.y <= y + height;
    }
    
    public void draw(Graphics2D g, boolean selected) {
        g.setColor(new Color(0, 0, 0, 30));
        g.fillRoundRect(x + 3, y + 3, width, height, 15, 15);
        
        g.setColor(selected ? ColorPalette.PALE_ROSE : ColorPalette.SOFT_PINK);
        g.fillRoundRect(x, y, width, height, 15, 15);
        
        g.setColor(selected ? ColorPalette.MEDIUM_PURPLE : ColorPalette.LAVENDER);
        g.setStroke(new BasicStroke(selected ? 3 : 2));
        g.drawRoundRect(x, y, width, height, 15, 15);
        
        g.setColor(ColorPalette.TEXT_DARK);
        g.setFont(new Font("Georgia", Font.BOLD, 12));
        FontMetrics fm = g.getFontMetrics();
        
        String[] words = text.split(" ");
        ArrayList<String> lines = new ArrayList<>();
        String currentLine = "";
        
        for (String word : words) {
            String testLine = currentLine.isEmpty() ? word : currentLine + " " + word;
            if (fm.stringWidth(testLine) > width - 20) {
                if (!currentLine.isEmpty()) {
                    lines.add(currentLine);
                }
                currentLine = word;
            } else {
                currentLine = testLine;
            }
        }
        if (!currentLine.isEmpty()) {
            lines.add(currentLine);
        }
        
        int lineHeight = fm.getHeight();
        int totalHeight = lines.size() * lineHeight;
        int startY = y + (height - totalHeight) / 2 + fm.getAscent();
        
        for (String line : lines) {
            int lineWidth = fm.stringWidth(line);
            int lineX = x + (width - lineWidth) / 2;
            g.drawString(line, lineX, startY);
            startY += lineHeight;
        }
    }
}

// Productivity Panel (same as before)
class ProductivityPanel extends JPanel {
    private JLabel timerLabel;
    private javax.swing.Timer timer;
    private int secondsLeft = 1500;
    private boolean isRunning = false;
    private DefaultTableModel taskTableModel;
    
    public ProductivityPanel() {
        setLayout(new BorderLayout(20, 20));
        setBackground(ColorPalette.CREAM);
        setBorder(new EmptyBorder(40, 50, 40, 50));
        
        JLabel title = new JLabel("Productivity Tools");
        title.setFont(new Font("Georgia", Font.BOLD, 32));
        title.setForeground(ColorPalette.TEXT_DARK);
        add(title, BorderLayout.NORTH);
        
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(500);
        splitPane.setBackground(ColorPalette.CREAM);
        
        JPanel timerPanel = createTimerPanel();
        splitPane.setLeftComponent(timerPanel);
        
        JPanel taskPanel = createTaskPanel();
        splitPane.setRightComponent(taskPanel);
        
        add(splitPane, BorderLayout.CENTER);
    }
    
    private JPanel createTimerPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(ColorPalette.SOFT_PINK);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ColorPalette.LAVENDER, 2),
            new EmptyBorder(30, 30, 30, 30)));
        
        JLabel heading = new JLabel("Pomodoro Focus Timer");
        heading.setFont(new Font("Georgia", Font.BOLD, 24));
        heading.setForeground(ColorPalette.TEXT_DARK);
        heading.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(heading);
        
        panel.add(Box.createVerticalStrut(40));
        
        timerLabel = new JLabel("25:00");
        timerLabel.setFont(new Font("Georgia", Font.BOLD, 72));
        timerLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        timerLabel.setForeground(ColorPalette.MEDIUM_PURPLE);
        panel.add(timerLabel);
        
        panel.add(Box.createVerticalStrut(40));
        
        JPanel btnPanel = new JPanel(new FlowLayout());
        btnPanel.setBackground(ColorPalette.SOFT_PINK);
        
        JButton startBtn = createTimerButton("Start", ColorPalette.MEDIUM_PURPLE);
        JButton pauseBtn = createTimerButton("Pause", ColorPalette.LAVENDER);
        JButton resetBtn = createTimerButton("Reset", ColorPalette.ACCENT_PINK);
        
        timer = new javax.swing.Timer(1000, e -> {
            if (secondsLeft > 0) {
                secondsLeft--;
                updateTimerDisplay();
            } else {
                timer.stop();
                isRunning = false;
                JOptionPane.showMessageDialog(this, "Time's up! Take a well-deserved break.", "Pomodoro Complete", JOptionPane.INFORMATION_MESSAGE);
            }
        });
        
        startBtn.addActionListener(e -> {
            if (!isRunning) {
                timer.start();
                isRunning = true;
            }
        });
        
        pauseBtn.addActionListener(e -> {
            if (isRunning) {
                timer.stop();
                isRunning = false;
            }
        });
        
        resetBtn.addActionListener(e -> {
            timer.stop();
            isRunning = false;
            secondsLeft = 1500;
            updateTimerDisplay();
        });
        
        btnPanel.add(startBtn);
        btnPanel.add(pauseBtn);
        btnPanel.add(resetBtn);
        
        panel.add(btnPanel);
        
        panel.add(Box.createVerticalStrut(30));
        
        JLabel tip = new JLabel("<html><center>Focus for 25 minutes,<br>then take a 5-minute break</center></html>");
        tip.setFont(new Font("Georgia", Font.ITALIC, 14));
        tip.setForeground(ColorPalette.MEDIUM_PURPLE);
        tip.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(tip);
        
        return panel;
    }
    
    private JButton createTimerButton(String text, Color bgColor) {
        JButton btn = new JButton(text);
        btn.setBackground(bgColor);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setFont(new Font("Georgia", Font.BOLD, 16));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(10, 20, 10, 20));
        return btn;
    }
    
    private void updateTimerDisplay() {
        int minutes = secondsLeft / 60;
        int seconds = secondsLeft % 60;
        timerLabel.setText(String.format("%02d:%02d", minutes, seconds));
    }
    
    private JPanel createTaskPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(ColorPalette.LILAC);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ColorPalette.LAVENDER, 2),
            new EmptyBorder(30, 30, 30, 30)));
        
        JLabel heading = new JLabel("Task Checklist");
        heading.setFont(new Font("Georgia", Font.BOLD, 24));
        heading.setForeground(ColorPalette.TEXT_DARK);
        panel.add(heading, BorderLayout.NORTH);
        
        String[] columns = {"Task", "Status"};
        taskTableModel = new DefaultTableModel(columns, 0);
        
        loadTasksFromDatabase();
        
        JTable taskTable = new JTable(taskTableModel);
        taskTable.setFont(new Font("Georgia", Font.PLAIN, 15));
        taskTable.setRowHeight(35);
        taskTable.setBackground(Color.WHITE);
        taskTable.setForeground(ColorPalette.TEXT_DARK);
        taskTable.setSelectionBackground(ColorPalette.PALE_ROSE);
        taskTable.getTableHeader().setBackground(ColorPalette.SOFT_PINK);
        taskTable.getTableHeader().setForeground(ColorPalette.TEXT_DARK);
        taskTable.getTableHeader().setFont(new Font("Georgia", Font.BOLD, 14));
        
        JScrollPane scrollPane = new JScrollPane(taskTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(ColorPalette.LAVENDER, 1));
        panel.add(scrollPane, BorderLayout.CENTER);
        
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btnPanel.setBackground(ColorPalette.LILAC);
        
        JButton addBtn = createTaskButton("Add Task");
        JButton completeBtn = createTaskButton("Complete");
        JButton deleteBtn = createTaskButton("Delete");
        
        addBtn.addActionListener(e -> {
            String task = JOptionPane.showInputDialog(this, "Enter new task:");
            if (task != null && !task.trim().isEmpty()) {
                DatabaseManager.addTask(task);
                taskTableModel.addRow(new Object[]{task, "Pending"});
            }
        });
        
        completeBtn.addActionListener(e -> {
            int row = taskTable.getSelectedRow();
            if (row >= 0) {
                String taskName = (String) taskTableModel.getValueAt(row, 0);
                DatabaseManager.updateTaskStatus(taskName, "Completed");
                taskTableModel.setValueAt("Completed", row, 1);
            }
        });
        
        deleteBtn.addActionListener(e -> {
            int row = taskTable.getSelectedRow();
            if (row >= 0) {
                String taskName = (String) taskTableModel.getValueAt(row, 0);
                DatabaseManager.deleteTask(taskName);
                taskTableModel.removeRow(row);
            }
        });
        
        btnPanel.add(addBtn);
        btnPanel.add(completeBtn);
        btnPanel.add(deleteBtn);
        
        panel.add(btnPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private void loadTasksFromDatabase() {
        ArrayList<Object[]> tasks = DatabaseManager.getAllTasks();
        for (Object[] task : tasks) {
            taskTableModel.addRow(task);
        }
    }
    
    private JButton createTaskButton(String text) {
        JButton btn = new JButton(text);
        btn.setBackground(ColorPalette.SOFT_PINK);
        btn.setForeground(ColorPalette.TEXT_DARK);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ColorPalette.LAVENDER, 1),
            new EmptyBorder(8, 15, 8, 15)));
        btn.setFont(new Font("Georgia", Font.PLAIN, 13));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }
}

// Wellness Panel - WITH GIFT BOX FEATURE
class WellnessPanel extends JPanel {
    private JComboBox<String> moodCombo;
    private JTextArea journalArea;
    
    public WellnessPanel() {
        setLayout(new BorderLayout(20, 20));
        setBackground(ColorPalette.CREAM);
        setBorder(new EmptyBorder(40, 50, 40, 50));
        
        JLabel title = new JLabel("Wellness Journal");
        title.setFont(new Font("Georgia", Font.BOLD, 32));
        title.setForeground(ColorPalette.TEXT_DARK);
        add(title, BorderLayout.NORTH);
        
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(ColorPalette.CREAM);
        
        JPanel moodPanel = createMoodPanel();
        moodPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentPanel.add(moodPanel);
        
        contentPanel.add(Box.createVerticalStrut(25));
        
        JPanel affirmationPanel = createAffirmationPanel();
        affirmationPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentPanel.add(affirmationPanel);
        
        contentPanel.add(Box.createVerticalStrut(25));
        
        JPanel journalPanel = createJournalPanel();
        journalPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentPanel.add(journalPanel);
        
        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);
    }
    
    private JPanel createMoodPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(ColorPalette.SOFT_PINK);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ColorPalette.LAVENDER, 2),
            new EmptyBorder(30, 30, 30, 30)));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 230));
        
        JLabel heading = new JLabel("Daily Mood Check-in");
        heading.setFont(new Font("Georgia", Font.BOLD, 24));
        heading.setForeground(ColorPalette.TEXT_DARK);
        heading.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(heading);
        
        panel.add(Box.createVerticalStrut(20));
        
        JLabel question = new JLabel("How are you feeling today?");
        question.setFont(new Font("Georgia", Font.ITALIC, 16));
        question.setForeground(ColorPalette.MEDIUM_PURPLE);
        question.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(question);
        
        panel.add(Box.createVerticalStrut(15));
        
        String[] moods = {"Happy & Energetic", "Calm & Peaceful", "Neutral", "Sad or Down", "Anxious", "Stressed or Overwhelmed"};
        moodCombo = new JComboBox<>(moods);
        moodCombo.setFont(new Font("Georgia", Font.PLAIN, 15));
        moodCombo.setMaximumSize(new Dimension(350, 45));
        moodCombo.setBackground(Color.WHITE);
        moodCombo.setForeground(ColorPalette.TEXT_DARK);
        moodCombo.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(moodCombo);
        
        panel.add(Box.createVerticalStrut(20));
        
        JButton submitBtn = new JButton("Submit Check-in & Get Gift 🎁");
        submitBtn.setBackground(ColorPalette.MEDIUM_PURPLE);
        submitBtn.setForeground(Color.WHITE);
        submitBtn.setFocusPainted(false);
        submitBtn.setBorderPainted(false);
        submitBtn.setFont(new Font("Georgia", Font.BOLD, 15));
        submitBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        submitBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        submitBtn.addActionListener(e -> {
            String mood = (String) moodCombo.getSelectedItem();
            DatabaseManager.saveMoodEntry(mood);
            
            // Show the gift box with tasks and playlists!
            MoodGiftBox.showGiftBox(this, mood);
        });
        panel.add(submitBtn);
        
        return panel;
    }
    
    private JPanel createAffirmationPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(ColorPalette.PALE_ROSE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ColorPalette.LAVENDER, 2),
            new EmptyBorder(30, 30, 30, 30)));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 180));
        
        JLabel heading = new JLabel("Daily Affirmation");
        heading.setFont(new Font("Georgia", Font.BOLD, 22));
        heading.setForeground(ColorPalette.TEXT_DARK);
        heading.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(heading);
        
        panel.add(Box.createVerticalStrut(20));
        
        String[] affirmations = {
            "You are capable of amazing things",
            "Every small step counts toward your goals",
            "You are doing better than you think",
            "It's okay to take breaks and rest",
            "You have overcome challenges before, and you will again",
            "Your effort and dedication are seen and valued",
            "Today is a new opportunity to grow"
        };
        
        Random rand = new Random();
        String affirmation = affirmations[rand.nextInt(affirmations.length)];
        
        JLabel affirmationLabel = new JLabel("<html><center>\"" + affirmation + "\"</center></html>");
        affirmationLabel.setFont(new Font("Georgia", Font.ITALIC, 18));
        affirmationLabel.setForeground(ColorPalette.MEDIUM_PURPLE);
        affirmationLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(affirmationLabel);
        
        return panel;
    }
    
    private JPanel createJournalPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(ColorPalette.LILAC);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ColorPalette.LAVENDER, 2),
            new EmptyBorder(30, 30, 30, 30)));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 400));
        
        JLabel heading = new JLabel("Reflection Journal");
        heading.setFont(new Font("Georgia", Font.BOLD, 24));
        heading.setForeground(ColorPalette.TEXT_DARK);
        panel.add(heading, BorderLayout.NORTH);
        
        journalArea = new JTextArea(10, 50);
        journalArea.setFont(new Font("Georgia", Font.PLAIN, 15));
        journalArea.setLineWrap(true);
        journalArea.setWrapStyleWord(true);
        journalArea.setBackground(Color.WHITE);
        journalArea.setForeground(ColorPalette.TEXT_DARK);
        journalArea.setCaretColor(ColorPalette.MEDIUM_PURPLE);
        journalArea.setText("Dear diary,\n\nToday I feel...");
        
        JScrollPane scrollPane = new JScrollPane(journalArea);
        scrollPane.setBorder(BorderFactory.createLineBorder(ColorPalette.LAVENDER, 1));
        panel.add(scrollPane, BorderLayout.CENTER);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.setBackground(ColorPalette.LILAC);
        
        JButton saveBtn = new JButton("Save Entry");
        saveBtn.setBackground(ColorPalette.MEDIUM_PURPLE);
        saveBtn.setForeground(Color.WHITE);
        saveBtn.setFocusPainted(false);
        saveBtn.setBorderPainted(false);
        saveBtn.setFont(new Font("Georgia", Font.BOLD, 14));
        saveBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        saveBtn.addActionListener(e -> {
            String content = journalArea.getText();
            DatabaseManager.saveJournalEntry(content);
            JOptionPane.showMessageDialog(this, "Journal entry saved!", "Saved", JOptionPane.INFORMATION_MESSAGE);
        });
        
        JButton clearBtn = new JButton("New Entry");
        clearBtn.setBackground(ColorPalette.SOFT_PINK);
        clearBtn.setForeground(ColorPalette.TEXT_DARK);
        clearBtn.setFocusPainted(false);
        clearBtn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ColorPalette.LAVENDER, 1),
            new EmptyBorder(8, 15, 8, 15)));
        clearBtn.setFont(new Font("Georgia", Font.PLAIN, 14));
        clearBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        clearBtn.addActionListener(e -> journalArea.setText(""));
        
        JButton viewBtn = new JButton("View Previous Entries");
        viewBtn.setBackground(ColorPalette.SOFT_PINK);
        viewBtn.setForeground(ColorPalette.TEXT_DARK);
        viewBtn.setFocusPainted(false);
        viewBtn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ColorPalette.LAVENDER, 1),
            new EmptyBorder(8, 15, 8, 15)));
        viewBtn.setFont(new Font("Georgia", Font.PLAIN, 14));
        viewBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        viewBtn.addActionListener(e -> viewPreviousEntries());
        
        buttonPanel.add(saveBtn);
        buttonPanel.add(clearBtn);
        buttonPanel.add(viewBtn);
        
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private void viewPreviousEntries() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Previous Journal Entries", true);
        dialog.setSize(700, 500);
        dialog.setLocationRelativeTo(this);
        
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBackground(ColorPalette.CREAM);
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        JLabel titleLabel = new JLabel("Your Journal History");
        titleLabel.setFont(new Font("Georgia", Font.BOLD, 24));
        titleLabel.setForeground(ColorPalette.TEXT_DARK);
        mainPanel.add(titleLabel, BorderLayout.NORTH);
        
        DefaultListModel<String> listModel = new DefaultListModel<>();
        ArrayList<Object[]> entries = DatabaseManager.getAllJournalEntries();
        
        if (entries.isEmpty()) {
            listModel.addElement("No journal entries yet. Start writing!");
        } else {
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy");
            for (Object[] entry : entries) {
                int id = (int) entry[0];
                String content = (String) entry[1];
                Date date = (Date) entry[2];
                String preview = content.length() > 60 ? content.substring(0, 60) + "..." : content;
                listModel.addElement(sdf.format(date) + " - " + preview);
            }
        }
        
        JList<String> entryList = new JList<>(listModel);
        entryList.setFont(new Font("Georgia", Font.PLAIN, 14));
        entryList.setBackground(Color.WHITE);
        entryList.setForeground(ColorPalette.TEXT_DARK);
        entryList.setSelectionBackground(ColorPalette.PALE_ROSE);
        
        entryList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedIndex = entryList.getSelectedIndex();
                if (selectedIndex >= 0 && selectedIndex < entries.size()) {
                    String fullContent = (String) entries.get(selectedIndex)[1];
                    Date date = (Date) entries.get(selectedIndex)[2];
                    SimpleDateFormat sdf = new SimpleDateFormat("MMMM dd, yyyy");
                    
                    JTextArea displayArea = new JTextArea(fullContent);
                    displayArea.setFont(new Font("Georgia", Font.PLAIN, 14));
                    displayArea.setLineWrap(true);
                    displayArea.setWrapStyleWord(true);
                    displayArea.setEditable(false);
                    displayArea.setBackground(ColorPalette.CREAM);
                    displayArea.setForeground(ColorPalette.TEXT_DARK);
                    
                    JScrollPane scrollPane = new JScrollPane(displayArea);
                    scrollPane.setPreferredSize(new Dimension(500, 300));
                    
                    JOptionPane.showMessageDialog(dialog, scrollPane, "Entry from " + sdf.format(date), JOptionPane.PLAIN_MESSAGE);
                }
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(entryList);
        scrollPane.setBorder(BorderFactory.createLineBorder(ColorPalette.LAVENDER, 2));
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        
        JButton closeBtn = new JButton("Close");
        closeBtn.setBackground(ColorPalette.MEDIUM_PURPLE);
        closeBtn.setForeground(Color.WHITE);
        closeBtn.setFocusPainted(false);
        closeBtn.setBorderPainted(false);
        closeBtn.setFont(new Font("Georgia", Font.BOLD, 14));
        closeBtn.addActionListener(e -> dialog.dispose());
        
        JPanel btnPanel = new JPanel();
        btnPanel.setBackground(ColorPalette.CREAM);
        btnPanel.add(closeBtn);
        mainPanel.add(btnPanel, BorderLayout.SOUTH);
        
        dialog.add(mainPanel);
        dialog.setVisible(true);
    }
}