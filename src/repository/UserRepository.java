package repository;

import model.*;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class UserRepository implements Repository<User> {
    private static final String FILE_PATH = "data/users.txt";
    private List<User> users;

    public UserRepository() {
        this.users = new ArrayList<>();
        createDataDirectory();
    }

    private void createDataDirectory() {
        File dir = new File("data");
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    @Override
    public void save(User user) throws IOException {
        // Check if user already exists
        boolean exists = false;
        for (User u : users) {
            if (u.getId() == user.getId()) {
                exists = true;
                break;
            }
        }
        
        if (!exists) {
            users.add(user);
        }
        saveAll(users);
    }

    @Override
    public void saveAll(List<User> entities) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH))) {
            for (User user : entities) {
                // Format: id,name,password,role
                writer.write(user.getId() + "," +
                           user.getName() + "," +
                           user.getPassword() + "," +
                           user.getRole());
                writer.newLine();
            }
        }
    }

    @Override
    public List<User> loadAll() throws IOException, ClassNotFoundException {
        File file = new File(FILE_PATH);
        if (!file.exists()) {
            return new ArrayList<>();
        }

        users = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_PATH))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                
                String[] parts = line.split(",");
                if (parts.length == 4) {
                    int id = Integer.parseInt(parts[0]);
                    String name = parts[1];
                    String password = parts[2];
                    String role = parts[3];
                    
                    User user;
                    if (role.equals(Role.ADMIN)) {
                        user = new Admin(id, name, password);
                    } else if (role.equals(Role.ENTRY_OPERATOR)) {
                        user = new EntryOperator(id, name, password);
                    } else if (role.equals(Role.EXIT_OPERATOR)) {
                        user = new ExitOperator(id, name, password);
                    } else {
                        continue;
                    }
                    users.add(user);
                }
            }
        }
        return users;
    }

    @Override
    public void delete(User user) throws IOException {
        users.remove(user);
        saveAll(users);
    }

    @Override
    public void clear() throws IOException {
        users.clear();
        saveAll(users);
    }

    public User findByName(String name) {
        for (User user : users) {
            if (user.getName().equals(name)) {
                return user;
            }
        }
        return null;
    }

    public User authenticate(String name, String password) {
        for (User user : users) {
            if (user.getName().equals(name) && user.getPassword().equals(password)) {
                return user;
            }
        }
        return null;
    }
}
