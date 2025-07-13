package ua.vhlab.tnfvvc.data.config;

import java.util.List;

public class Config {

    private TrueNASConfig truenas;
    private List<DashboardConfig> dashboard;

    // Getters and setters
    public TrueNASConfig getTruenas() {
        return truenas;
    }

    public void setTruenas(TrueNASConfig truenas) {
        this.truenas = truenas;
    }

    public List<DashboardConfig> getDashboard() {
        return dashboard;
    }

    public void setDashboard(List<DashboardConfig> dashboard) {
        this.dashboard = dashboard;
    }
}
