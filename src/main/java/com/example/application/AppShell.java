package com.example.application;

import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.server.PWA;
import com.vaadin.flow.theme.Theme;

@Theme(value = "gtpchatapp")
@Push
public class AppShell implements AppShellConfigurator {
    // You can add other global configurations here if needed
}