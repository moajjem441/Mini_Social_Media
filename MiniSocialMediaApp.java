package basicjava.minisocialmediaapp;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

class User implements Serializable {
    private String username;
    private String email;
    private String password;

    public User(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.password = password;
    }

    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
}

class Post implements Serializable {
    private String content;
    private User author;
    private int likes;
    private List<String> comments;

    public Post(String content, User author) {
        this.content = content;
        this.author = author;
        this.likes = 0;
        this.comments = new ArrayList<>();
    }

    public String getContent() { return content; }
    public User getAuthor() { return author; }
    public int getLikes() { return likes; }
    public void likePost() { likes++; }
    public void addComment(String comment) { comments.add(comment); }
    public List<String> getComments() { return comments; }
}

public class MiniSocialMediaApp {
    private static List<User> users = new ArrayList<>();
    private static List<Post> posts = new ArrayList<>();
    private static User currentUser = null;
    private static boolean darkMode = false;

    private static final String USERS_FILE = "users.dat";
    private static final String POSTS_FILE = "posts.dat";

    private static JLabel messageBar;

    // Main Frame components that need to be accessed globally
    private static JFrame mainFrame;
    private static JPanel mainContentPanel;
    private static JPanel sidebar;

    public static void main(String[] args) {
        loadData();
        showLoginRegisterFrame();
    }

    private static void showLoginRegisterFrame() {
        JFrame loginFrame = new JFrame("Login or Register");
        loginFrame.setSize(350, 300);
        loginFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        loginFrame.setLayout(new GridLayout(8, 1));

        JTextField userField = new JTextField();
        JTextField emailField = new JTextField();
        JPasswordField passField = new JPasswordField();

        JButton loginButton = new JButton("Login");
        JButton registerButton = new JButton("Register");

        loginFrame.add(new JLabel("Username:"));
        loginFrame.add(userField);
        loginFrame.add(new JLabel("Email:"));
        loginFrame.add(emailField);
        loginFrame.add(new JLabel("Password:"));
        loginFrame.add(passField);
        loginFrame.add(loginButton);
        loginFrame.add(registerButton);

        loginButton.addActionListener(e -> {
            String username = userField.getText();
            String password = new String(passField.getPassword());
            for (User user : users) {
                if (user.getUsername().equals(username) && user.getPassword().equals(password)) {
                    currentUser = user;
                    loginFrame.dispose();
                    showMainApp();
                    return;
                }
            }
            JOptionPane.showMessageDialog(null, "Login failed!");
        });

        registerButton.addActionListener(e -> {
            String username = userField.getText();
            String email = emailField.getText();
            String password = new String(passField.getPassword());
            if (username.trim().isEmpty() || email.trim().isEmpty() || password.trim().isEmpty()) {
                JOptionPane.showMessageDialog(null, "Please fill all fields.");
                return;
            }
            for (User user : users) {
                if (user.getUsername().equals(username)) {
                    JOptionPane.showMessageDialog(null, "Username already exists!");
                    return;
                }
            }
            User newUser = new User(username, email, password);
            users.add(newUser);
            saveData();
            JOptionPane.showMessageDialog(null, "Registered successfully! You can now login.");
        });

        loginFrame.setLocationRelativeTo(null);  // Center on screen
        loginFrame.setVisible(true);
    }

    private static void showMainApp() {
        mainFrame = new JFrame("Mini Social Media App - Logged in as " + currentUser.getUsername());
        mainFrame.setSize(800, 600);
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setLayout(new BorderLayout());

        // Message bar at the top
        messageBar = new JLabel("");
        messageBar.setHorizontalAlignment(SwingConstants.CENTER);
        messageBar.setOpaque(true);
        messageBar.setBackground(Color.YELLOW);
        messageBar.setPreferredSize(new Dimension(mainFrame.getWidth(), 30));
        mainFrame.add(messageBar, BorderLayout.NORTH);

        // Sidebar on the left
        sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setPreferredSize(new Dimension(150, mainFrame.getHeight()));
        sidebar.setBackground(Color.LIGHT_GRAY);

        String[] navItems = {"Home", "Profile", "Messages", "Settings", "Logout"};
        for (String item : navItems) {
            JButton navButton = new JButton(item);
            navButton.setAlignmentX(Component.CENTER_ALIGNMENT);
            navButton.setMaximumSize(new Dimension(140, 40));
            sidebar.add(Box.createRigidArea(new Dimension(0, 10)));
            sidebar.add(navButton);

            navButton.addActionListener(e -> {
                switch (item) {
                    case "Home":
                        showHomeView();
                        break;
                    case "Profile":
                        showProfileView();
                        break;
                    case "Messages":
                        showNotification("Messages feature coming soon!");
                        break;
                    case "Settings":
                        toggleDarkMode(mainFrame);
                        showNotification(darkMode ? "Dark Mode Enabled" : "Light Mode Enabled");
                        break;
                    case "Logout":
                        System.exit(0);
                        break;
                }
            });
        }

        mainFrame.add(sidebar, BorderLayout.WEST);

        // Main content panel on the right - dynamic content will be swapped here
        mainContentPanel = new JPanel(new BorderLayout());
        mainFrame.add(mainContentPanel, BorderLayout.CENTER);

        showHomeView();  // Show home posts by default

        mainFrame.setLocationRelativeTo(null);  // Center window
        mainFrame.setVisible(true);
    }

    // Show Home view: posts list + create post
    private static void showHomeView() {
        mainContentPanel.removeAll();

        // Posts panel
        JPanel postsPanel = new JPanel();
        postsPanel.setLayout(new BoxLayout(postsPanel, BoxLayout.Y_AXIS));

        for (Post post : posts) {
            JPanel postPanel = new JPanel();
            postPanel.setLayout(new BoxLayout(postPanel, BoxLayout.Y_AXIS));
            postPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY));
            postPanel.setBackground(darkMode ? Color.DARK_GRAY : Color.WHITE);

            JLabel postLabel = new JLabel("<html><b>" + post.getAuthor().getUsername() + ":</b> " + post.getContent() + "</html>");
            JLabel likesLabel = new JLabel("Likes: " + post.getLikes());
            JButton likeButton = new JButton("Like");
            JButton commentButton = new JButton("Comment");
            JTextArea commentsArea = new JTextArea("Comments:\n", 3, 30);
            commentsArea.setEditable(false);

            for (String comment : post.getComments()) {
                commentsArea.append("- " + comment + "\n");
            }

            likeButton.addActionListener(e -> {
                post.likePost();
                likesLabel.setText("Likes: " + post.getLikes());
                saveData();
            });

            commentButton.addActionListener(e -> {
                String comment = JOptionPane.showInputDialog(mainFrame, "Enter comment:");
                if (comment != null && !comment.trim().isEmpty()) {
                    post.addComment(comment);
                    saveData();
                    showHomeView();  // Refresh home view to update comments
                }
            });

            postPanel.add(postLabel);
            postPanel.add(likesLabel);

            JPanel buttonsPanel = new JPanel();
            buttonsPanel.add(likeButton);
            buttonsPanel.add(commentButton);
            postPanel.add(buttonsPanel);

            postPanel.add(new JScrollPane(commentsArea));
            postsPanel.add(postPanel);
            postsPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        }

        JScrollPane scrollPane = new JScrollPane(postsPanel);

        // Create post panel
        JPanel createPostPanel = new JPanel(new FlowLayout());
        JTextField postField = new JTextField(30);
        JButton postButton = new JButton("Post");

        createPostPanel.add(new JLabel("Create a post:"));
        createPostPanel.add(postField);
        createPostPanel.add(postButton);

        postButton.addActionListener(e -> {
            String content = postField.getText();
            if (!content.trim().isEmpty()) {
                posts.add(new Post(content, currentUser));
                postField.setText("");
                saveData();
                showNotification("Post created!");
                showHomeView(); // Refresh to show new post
            }
        });

        mainContentPanel.add(scrollPane, BorderLayout.CENTER);
        mainContentPanel.add(createPostPanel, BorderLayout.SOUTH);

        mainContentPanel.revalidate();
        mainContentPanel.repaint();
    }

    // Show Profile view: user info + stats
    private static void showProfileView() {
        mainContentPanel.removeAll();

        JPanel profilePanel = new JPanel();
        profilePanel.setLayout(new BoxLayout(profilePanel, BoxLayout.Y_AXIS));
        profilePanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        int totalLikes = 0;
        int totalPosts = 0;
        for (Post post : posts) {
            if (post.getAuthor().equals(currentUser)) {
                totalPosts++;
                totalLikes += post.getLikes();
            }
        }

        profilePanel.add(new JLabel("Username: " + currentUser.getUsername()));
        profilePanel.add(Box.createRigidArea(new Dimension(0, 10)));
        profilePanel.add(new JLabel("Email: " + currentUser.getEmail()));
        profilePanel.add(Box.createRigidArea(new Dimension(0, 10)));
        profilePanel.add(new JLabel("Posts: " + totalPosts));
        profilePanel.add(Box.createRigidArea(new Dimension(0, 10)));
        profilePanel.add(new JLabel("Total Likes: " + totalLikes));

        mainContentPanel.add(profilePanel, BorderLayout.CENTER);

        mainContentPanel.revalidate();
        mainContentPanel.repaint();
    }

    private static void saveData() {
        try (ObjectOutputStream outU = new ObjectOutputStream(new FileOutputStream(USERS_FILE));
             ObjectOutputStream outP = new ObjectOutputStream(new FileOutputStream(POSTS_FILE))) {
            outU.writeObject(users);
            outP.writeObject(posts);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void loadData() {
        try (ObjectInputStream inU = new ObjectInputStream(new FileInputStream(USERS_FILE));
             ObjectInputStream inP = new ObjectInputStream(new FileInputStream(POSTS_FILE))) {
            users = (List<User>) inU.readObject();
            posts = (List<Post>) inP.readObject();
        } catch (Exception e) {
            users = new ArrayList<>();
            posts = new ArrayList<>();
        }
    }

    private static void toggleDarkMode(JFrame frame) {
        darkMode = !darkMode;
        applyTheme(frame);
    }

    private static void applyTheme(JFrame frame) {
        SwingUtilities.invokeLater(() -> {
            for (Window window : Window.getWindows()) {
                updateComponentUI(window);
            }
        });
    }

    private static void updateComponentUI(Component comp) {
        if (darkMode) {
            comp.setBackground(Color.DARK_GRAY);
            comp.setForeground(Color.WHITE);
        } else {
            comp.setBackground(Color.WHITE);
            comp.setForeground(Color.BLACK);
        }
        if (comp instanceof Container) {
            for (Component child : ((Container) comp).getComponents()) {
                updateComponentUI(child);
            }
        }
    }

    private static void showNotification(String message) {
        messageBar.setText(message);
        new Timer(3000, e -> messageBar.setText("")).start();
    }
}
