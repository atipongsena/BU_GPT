package com.example.application.service;

import com.example.application.model.User;
import com.example.application.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Optional;
import com.vaadin.flow.server.VaadinService;

@Service
public class UserService {

    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public boolean authenticate(String login, String password) {
        Optional<User> user = userRepository.findByUsernameOrEmail(login, login);
        if (user.isPresent() && user.get().getPassword().equals(password)) {
            setCurrentUser(user.get());
            return true;
        }
        return false;
    }

    public boolean register(User newUser) {
        if (userRepository.existsByUsername(newUser.getUsername())) {
            return false;
        }
        // Add additional validations as necessary
        userRepository.save(newUser);
        return true;
    }

    public User getCurrentUser() {
        User currentUser = (User) VaadinService.getCurrentRequest().getWrappedSession().getAttribute("user");
        if (currentUser != null) {
            System.out.println("Current user username: " + currentUser.getUsername()); // Log สำหรับตรวจสอบ
        } else {
            System.out.println("No user is currently logged in."); // Log สำหรับตรวจสอบ
        }
        return currentUser;
    }


    public void setCurrentUser(User user) {
        VaadinService.getCurrentRequest().getWrappedSession().setAttribute("user", user);
    }

    public void saveSettings(User updatedUser) {
        if (updatedUser != null) {
            User currentUser = getCurrentUser();
            // Replace getUserId with getUsername
            if (currentUser != null && currentUser.getUsername().equals(updatedUser.getUsername())) {
                // Update specific fields like full name, email, and password
                currentUser.setFullName(updatedUser.getFullName());
                currentUser.setEmail(updatedUser.getEmail());
                if (updatedUser.getPassword() != null && !updatedUser.getPassword().trim().isEmpty()) {
                    currentUser.setPassword(updatedUser.getPassword());
                }
                userRepository.save(currentUser);
            } else {
                // Handle cases where currentUser is null or usernames don't match
                // This could be logged or an exception could be thrown depending on your error handling strategy
            }
        }
    }
}
