package ua.vhlab.tnfvvc.services;

import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import ua.vhlab.tnfvvc.data.config.Config;
import ua.vhlab.tnfvvc.data.config.ConfigRepository;
import ua.vhlab.tnfvvc.data.config.DashboardConfig;
import ua.vhlab.tnfvvc.data.config.TrueNASConfig;

import java.util.List;

@Service
@Component
public class ConfigService {

    private final ConfigRepository configRepository;

    public ConfigService(ConfigRepository configRepository) {
        this.configRepository = configRepository;
    }

    public TrueNASConfig getTrueNASConfig() {
        return configRepository.getTrueNASConfig();
    }

    public List<DashboardConfig> getDashboardConfig() {
        return configRepository.getDashboardConfig();
    }

    public void saveConfig(Config config) {
        configRepository.saveConfig(config);
    }

    public Config getConfig() {
        return configRepository.getConfig();
    }
}
