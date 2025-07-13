package ua.vhlab.tnfvvc.services;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class DashboardViewUpdateService {

    private final List<String> items = new ArrayList<>();

    public final List<Runnable> listeners = new CopyOnWriteArrayList<>();

    public List<String> getItems() {
        return new ArrayList<>(items);
    }

    public void addItem(String item) {
        items.add(item);
        listeners.forEach(Runnable::run);
    }

    public void registerListener(Runnable listener) {
        listeners.add(listener);
    }

    public void unregisterListener(Runnable listener) {
        listeners.remove(listener);
    }
}
