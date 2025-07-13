package ua.vhlab.tnfvvc.data;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import java.io.File;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

@Repository
@Component
public class UserRepository {

    private final ObjectMapper mapper = new ObjectMapper();
    private final File file;
    private final AtomicLong lastModified = new AtomicLong(0);
    private final PasswordEncoder passwordEncoder;
    private List<User> users = new ArrayList<>();

    public UserRepository(@Value("${auth.users.file}") String jsonFilePath, PasswordEncoder passwordEncoder) {
        this.file = new File(jsonFilePath);
        this.passwordEncoder = passwordEncoder;
        loadUsers();

    }

    private void loadUsers() {
        try {
            long modified = file.lastModified();
            if (modified > lastModified.get()) {
                users = mapper.readValue(file, new TypeReference<>() {
                });
                lastModified.set(modified);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to load users.json", e);
        }
    }

    public void saveUsers(List<User> userList) {
        try {
            List<User> encodedUsers = userList.stream().map(user -> {
                String pwd = user.getHashedPassword();
                if (!pwd.startsWith("$2")) { // check if not already bcrypt
                    pwd = passwordEncoder.encode(pwd);
                }
                return new User(user.getUsername(), user.getName(), pwd, user.getRoles(), user.getProfilePicture());
            }).toList();

            mapper.writerWithDefaultPrettyPrinter().writeValue(file, encodedUsers);

            lastModified.set(0);  // force reload
            loadUsers();          // update in-memory map

        } catch (Exception e) {
            throw new RuntimeException("Cannot save users.json", e);
        }
    }

    public Optional<User> findByUsername(String username) {
        loadUsers();
        return users.stream()
                .filter(u -> u.getUsername().equalsIgnoreCase(username))
                .findFirst();
    }

    public List<User> findAll() {
        loadUsers();
        return new ArrayList<>(users);
    }

}
