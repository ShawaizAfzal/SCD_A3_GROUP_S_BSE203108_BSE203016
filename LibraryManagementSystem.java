import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

// Data Access Object (DAO) interfaces
interface BookDAO {
    List<Book> getAllBooks();
    void addBook(Book book);
    void issueBook(Book book, User user);
    void returnBook(Book book);
}

interface UserDAO {
    List<User> getAllUsers();
}

// Data Transfer Objects (DTO)
class Book {
    private int id;
    private String title;
    private String author;
    private boolean issued;

    public Book(int id, String title, String author) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.issued = false;
    }

    // Getters and setters
    // ...

    public boolean isIssued() {
        return issued;
    }

    public void setIssued(boolean issued) {
        this.issued = issued;
    }
}

class User {
    private int id;
    private String name;
    private String username;

    public User(int id, String name, String username) {
        this.id = id;
        this.name = name;
        this.username = username;
    }

    // Getters and setters
    // ...
}

// Concrete implementations of DAO interfaces using JDBC
class BookDAOImpl implements BookDAO {
    private Connection connection;

    public BookDAOImpl(Connection connection) {
        this.connection = connection;
    }

    @Override
    public List<Book> getAllBooks() {
        List<Book> books = new ArrayList<>();
        try {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM books");
            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String title = resultSet.getString("title");
                String author = resultSet.getString("author");
                boolean issued = resultSet.getBoolean("issued");
                Book book = new Book(id, title, author);
                book.setIssued(issued);
                books.add(book);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return books;
    }

    @Override
    public void addBook(Book book) {
        try {
            PreparedStatement statement = connection.prepareStatement("INSERT INTO books (title, author) VALUES (?, ?)");
            statement.setString(1, book.getTitle());
            statement.setString(2, book.getAuthor());
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void issueBook(Book book, User user) {
        try {
            PreparedStatement statement = connection.prepareStatement("UPDATE books SET issued = TRUE WHERE id = ?");
            statement.setInt(1, book.getId());
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void returnBook(Book book) {
        try {
            PreparedStatement statement = connection.prepareStatement("UPDATE books SET issued = FALSE WHERE id = ?");
            statement.setInt(1, book.getId());
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

class UserDAOImpl implements UserDAO {
    private Connection connection;

    public UserDAOImpl(Connection connection) {
        this.connection = connection;
    }

    @Override
    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        try {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM users");
            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String name = resultSet.getString("name");
                String username = resultSet.getString("username");
                User user = new User(id, name, username);
                users.add(user);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }
}

public class LibraryManagementSystem extends JFrame {
    private JButton btnViewBooks;
    private JButton btnViewUsers;
    private JButton btnAddBook;
    private JButton btnIssueBook;
    private JButton btnReturnBook;

    private BookDAO bookDAO;
    private UserDAO userDAO;

    public LibraryManagementSystem() {
        initializeComponents();
        setLayout(new FlowLayout());
        setTitle("Library Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(300, 200);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void initializeComponents() {
        // Login dialog
        String username = JOptionPane.showInputDialog("Enter username:");
        String password = JOptionPane.showInputDialog("Enter password:");

        if (!"admin".equals(username) || !"admin".equals(password)) {
            JOptionPane.showMessageDialog(null, "Invalid credentials. Exiting...");
            System.exit(0);
        }

        // Create database connection
        Connection connection = null;
        try {
            String url = "jdbc:mysql://localhost/";
            String dbName = "library";
            String dbUsername = "root";
            String dbPassword = "password";
            connection = DriverManager.getConnection(url, dbUsername, dbPassword);

            // Create the database if it does not exist
            Statement statement = connection.createStatement();
            statement.executeUpdate("CREATE DATABASE IF NOT EXISTS " + dbName);

            // Use the library database
            statement.executeUpdate("USE " + dbName);

            // Create the books table if it does not exist
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS books (id INT PRIMARY KEY AUTO_INCREMENT, title VARCHAR(100), author VARCHAR(100), issued BOOLEAN)");

            // Create the users table if it does not exist
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS users (id INT PRIMARY KEY AUTO_INCREMENT, name VARCHAR(100), username VARCHAR(100))");
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Failed to connect to the database. Exiting...");
            System.exit(0);
        }

        // Create DAO instances
        bookDAO = new BookDAOImpl(connection);
        userDAO = new UserDAOImpl(connection);

        // Admin functionality buttons
        btnViewBooks = new JButton("View Available Books");
        btnViewUsers = new JButton("View Users");
        btnAddBook = new JButton("Add New Book");
        btnIssueBook = new JButton("Issue Book");
        btnReturnBook = new JButton("Return Book");

        btnViewBooks.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                viewAvailableBooks();
            }
        });

        btnViewUsers.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                viewUsers();
            }
        });

        btnAddBook.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                addBook();
            }
        });

        btnIssueBook.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                issueBook();
            }
        });

        btnReturnBook.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                returnBook();
            }
        });

        add(btnViewBooks);
        add(btnViewUsers);
        add(btnAddBook);
        add(btnIssueBook);
        add(btnReturnBook);
    }

    private void viewAvailableBooks() {
        List<Book> books = bookDAO.getAllBooks();
        if (books != null) {
            StringBuilder sb = new StringBuilder();
            for (Book book : books) {
                if (!book.isIssued()) {
                    sb.append("ID: ").append(book.getId())
                            .append(", Title: ").append(book.getTitle())
                            .append(", Author: ").append(book.getAuthor())
                            .append("\n");
                }
            }
            JOptionPane.showMessageDialog(null, sb.toString(), "Available Books", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(null, "Failed to retrieve books.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void viewUsers() {
        List<User> users = userDAO.getAllUsers();
        if (users != null) {
            StringBuilder sb = new StringBuilder();
            for (User user : users) {
                sb.append("ID: ").append(user.getId())
                        .append(", Name: ").append(user.getName())
                        .append(", Username: ").append(user.getUsername())
                        .append("\n");
            }
            JOptionPane.showMessageDialog(null, sb.toString(), "Users", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(null, "Failed to retrieve users.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addBook() {
        String title = JOptionPane.showInputDialog("Enter the book title:");
        String author = JOptionPane.showInputDialog("Enter the book author");

        Book book = new Book(0, title, author);
        bookDAO.addBook(book);
        JOptionPane.showMessageDialog(null, "Book added successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    private void issueBook() {
        List<Book> books = bookDAO.getAllBooks();
        if (books != null) {
            String bookId = JOptionPane.showInputDialog("Enter the book ID:");
            Book selectedBook = null;
            for (Book book : books) {
                if (String.valueOf(book.getId()).equals(bookId)) {
                    selectedBook = book;
                    break;
                }
            }
            if (selectedBook != null) {
                String userId = JOptionPane.showInputDialog("Enter the user ID:");
                List<User> users = userDAO.getAllUsers();
                User selectedUser = null;
                for (User user : users) {
                    if (String.valueOf(user.getId()).equals(userId)) {
                        selectedUser = user;
                        break;
                    }
                }
                if (selectedUser != null) {
                    bookDAO.issueBook(selectedBook, selectedUser);
                    JOptionPane.showMessageDialog(null, "Book issued successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                    return;
                }
            }
        }
        JOptionPane.showMessageDialog(null, "Failed to issue book.", "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void returnBook() {
        List<Book> books = bookDAO.getAllBooks();
        if (books != null) {
            String bookId = JOptionPane.showInputDialog("Enter the book ID:");
            Book selectedBook = null;
            for (Book book : books) {
                if (String.valueOf(book.getId()).equals(bookId)) {
                    selectedBook = book;
                    break;
                }
            }
            if (selectedBook != null && selectedBook.isIssued()) {
                bookDAO.returnBook(selectedBook);
                JOptionPane.showMessageDialog(null, "Book returned successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
        }
        JOptionPane.showMessageDialog(null, "Failed to return book.", "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new LibraryManagementSystem();
            }
        });
    }
}
