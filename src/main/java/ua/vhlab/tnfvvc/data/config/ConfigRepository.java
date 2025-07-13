package ua.vhlab.tnfvvc.data.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import ua.vhlab.tnfvvc.util.AESCryptoUtil;

import java.io.File;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Repository
@Component
public class ConfigRepository {

    private final File file;
    private final PasswordEncoder passwordEncoder;
    private final AtomicLong lastModified = new AtomicLong(0);
    private final ObjectMapper mapper = new ObjectMapper();
    private Config config;

    public ConfigRepository(@Value("${sys.config.file}") String jsonFilePath, PasswordEncoder passwordEncoder) {
        this.file = new File(jsonFilePath);
        this.passwordEncoder = passwordEncoder;
        loadConfig();
    }

    public void saveConfig(Config config) {
        try {

            mapper.writerWithDefaultPrettyPrinter().writeValue(file, config);

            lastModified.set(0);  // force reload
            loadConfig();          // update in-memory map

        } catch (Exception e) {
            throw new RuntimeException("Cannot save users.json", e);
        }
    }

    private void loadConfig() {
        try {
            long modified = file.lastModified();
            if (modified > lastModified.get()) {
                config = mapper.readValue(file, new TypeReference<>() {
                });
                lastModified.set(modified);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to load config.json", e);
        }
    }

    public TrueNASConfig getTrueNASConfig() {
        loadConfig();
        return config.getTruenas();
    }

    public List<DashboardConfig> getDashboardConfig() {
        loadConfig();
        return config.getDashboard();

    }

    public Config getConfig() {
        loadConfig();
        return config;
    }
}
