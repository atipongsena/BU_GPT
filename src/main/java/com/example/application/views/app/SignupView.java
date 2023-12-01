package com.example.application.views.app;

import com.example.application.model.User;
import com.example.application.service.UserService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.spring.annotation.UIScope;
import org.springframework.beans.factory.annotation.Autowired;

@Route("signup")
@PageTitle("Sign Up")
@UIScope
public class SignupView extends VerticalLayout {

    private final UserService userService;

    @Autowired
    public SignupView(UserService userService) {
        this.userService = userService;
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);

        TextField usernameField = new TextField("Username");
        TextField fullNameField = new TextField("Full Name");
        TextField studentIdField = new TextField("Student ID");
        TextField emailField = new TextField("Email");
        TextField passwordField = new TextField("Password");
        passwordField.getElement().setAttribute("type", "password");

        Button signupButton = new Button("Sign Up");
        signupButton.addClickListener(event -> {
            User newUser = new User();
            newUser.setUsername(usernameField.getValue());
            newUser.setFullName(fullNameField.getValue());
            newUser.setStudentId(studentIdField.getValue());
            newUser.setEmail(emailField.getValue());
            newUser.setPassword(passwordField.getValue());

            boolean isRegistered = userService.register(newUser);
            if (isRegistered) {
                UI.getCurrent().navigate(""); // Redirect to login view
            } else {
                Notification.show("Signup failed, please try again.", 3000, Notification.Position.MIDDLE);
            }
        });

        Button returnToLoginButton = new Button("Return to Login", event -> UI.getCurrent().navigate(""));

        add(new H1("Sign Up"), usernameField, fullNameField, studentIdField, emailField, passwordField, signupButton, returnToLoginButton);
    }
}