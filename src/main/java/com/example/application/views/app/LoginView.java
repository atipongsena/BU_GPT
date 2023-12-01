package com.example.application.views.app;

import com.example.application.service.UserService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.annotation.UIScope;
import org.springframework.beans.factory.annotation.Autowired;
import com.vaadin.flow.component.textfield.PasswordField;

@Route("")
@PageTitle("Login")
@UIScope
public class LoginView extends VerticalLayout {

    private final UserService userService;

    @Autowired
    public LoginView(UserService userService) {
        this.userService = userService;
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);

        TextField loginField = new TextField("Username or Email");

        // Use PasswordField instead of TextField for password
        PasswordField passwordField = new PasswordField("Password");
        passwordField.setRevealButtonVisible(true); // This allows the password to be shown or hidden

        Button loginButton = new Button("Login");
        loginButton.addClickListener(event -> {
            boolean isAuthenticated = userService.authenticate(loginField.getValue(), passwordField.getValue());
            if (isAuthenticated) {
                UI.getCurrent().navigate("gtp-chat"); // Navigate to ChatView
            } else {
                Notification.show("Login failed, please try again.", 3000, Position.MIDDLE);
            }
        });

        Button signupButton = new Button("Sign Up", event -> UI.getCurrent().navigate("signup"));
        add(new H1("Login"), loginField, passwordField, loginButton, signupButton);
    }
}