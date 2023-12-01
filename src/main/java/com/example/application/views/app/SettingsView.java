package com.example.application.views.app;

import com.example.application.model.User;
import com.example.application.service.UserService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;

@Route("settings")
@PageTitle("Settings")
public class SettingsView extends VerticalLayout {

    private final UserService userService;

    @Autowired
    public SettingsView(UserService userService) {
        this.userService = userService;
        User currentUser = userService.getCurrentUser();

        // Full Name Field
        TextField fullNameField = new TextField("Full Name");
        fullNameField.setValue(currentUser.getFullName());

        // Email Field
        TextField emailField = new TextField("Email");
        emailField.setValue(currentUser.getEmail());

        // Password Field
        PasswordField passwordField = new PasswordField("Password");
        passwordField.setPlaceholder("Enter new password");

        Button saveButton = new Button("Save", event -> {
            // Perform validation if necessary
            if (fullNameField.isEmpty() || emailField.isEmpty() || passwordField.isEmpty()) {
                Notification.show("Please fill in all fields.", 3000, Notification.Position.MIDDLE);
                return;
            }

            // Update the currentUser object with new values
            currentUser.setFullName(fullNameField.getValue());
            currentUser.setEmail(emailField.getValue());
            if (!passwordField.isEmpty()) {
                currentUser.setPassword(passwordField.getValue());
            }

            // Call saveSettings with the updated User object
            userService.saveSettings(currentUser);
            Notification.show("Settings saved successfully.", 3000, Notification.Position.MIDDLE);
        });

        // Create a back button
        Button backButton = new Button("Back to Chat", event ->
                UI.getCurrent().navigate("gtp-chat") // Navigate to the chat view
        );

        FormLayout formLayout = new FormLayout();
        formLayout.addFormItem(fullNameField, "Full Name");
        formLayout.addFormItem(emailField, "Email");
        formLayout.addFormItem(passwordField, "Password");

        add(new H1("User Settings"), formLayout, saveButton, backButton);
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);


    }

}
