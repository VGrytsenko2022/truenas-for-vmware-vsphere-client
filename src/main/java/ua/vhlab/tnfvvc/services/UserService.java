package ua.vhlab.tnfvvc.services;

import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import ua.vhlab.tnfvvc.data.User;
import ua.vhlab.tnfvvc.data.UserRepository;

@Service
@Component
public class UserService {

    private final UserRepository repository;

    public UserService(UserRepository repository) {
        this.repository = repository;
    }

    public void saveUsers(List<User> userList) {
        repository.saveUsers(userList);
    }

    public List<User> list() {
        return repository.findAll();
    }

}
