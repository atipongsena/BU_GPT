package com.example.application.views.app;

import com.example.application.model.ChatHistory;
import com.example.application.service.ChatHistoryService;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import org.springframework.beans.factory.annotation.Autowired;
import com.example.application.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.component.UI;
import com.example.application.service.UserService;

import java.util.List;

@Route(value = "chat-history")
@PageTitle("Chat History")
@AnonymousAllowed
public class ChatHistoryView extends VerticalLayout {

    private final ChatHistoryService chatHistoryService;
    private final UserService userService; // The userService should be final if it is going to be set only once in the constructor
    private final Grid<ChatHistory> chatHistoryGrid;


    @Autowired
    public ChatHistoryView(ChatHistoryService chatHistoryService,UserService userService) {
        this.chatHistoryService = chatHistoryService;
        this.userService = userService; // Set the userService using constructor injection
        this.chatHistoryGrid = new Grid<>(ChatHistory.class, false);

        // Add columns to the grid
        chatHistoryGrid.addColumn(ChatHistory::getId).setHeader("ID").setSortable(true);
        chatHistoryGrid.addColumn(ChatHistory::getSenderId).setHeader("Sender").setSortable(true);
        chatHistoryGrid.addColumn(ChatHistory::getReceiverId).setHeader("Receiver").setSortable(true);
        chatHistoryGrid.addColumn(ChatHistory::getMessage).setHeader("Message");
        chatHistoryGrid.addColumn(ChatHistory::getTimestamp).setHeader("Timestamp").setSortable(true);

        // Create a back button to navigate to ChatView
        Button backButton = new Button("Back to Chat", VaadinIcon.ARROW_LEFT.create());
        backButton.addClickListener(event -> {
            // Use UI's navigate method to navigate to ChatView
            UI.getCurrent().navigate(ChatView.class);
        });
        backButton.getElement().setAttribute("theme", "tertiary");

        // Add the back button at the top of the view
        add(backButton, chatHistoryGrid);
        setSizeFull();
        updateChatHistory();
    }

    private void updateChatHistory() {
        User currentUser = userService.getCurrentUser(); // Now userService should be resolved
        if (currentUser != null) {
            List<ChatHistory> history = chatHistoryService.getChatHistoryForUser(currentUser.getUsername());
            chatHistoryGrid.setItems(history);
        } else {
            // Handle the case where there is no user logged in
            // Consider showing a message or redirecting to a login page
        }
    }


}
