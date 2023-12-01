package com.example.application.views.app;

import com.example.application.OpenAI;
import com.example.application.model.ChatHistory;
import com.example.application.model.User;
import com.example.application.service.ChatHistoryService;
import com.example.application.service.UserService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.messages.MessageInput;
import com.vaadin.flow.component.messages.MessageList;
import com.vaadin.flow.component.messages.MessageListItem;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.server.PWA;
import com.vaadin.flow.server.VaadinService;
import org.springframework.beans.factory.annotation.Autowired;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;



import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;
import java.util.ArrayList;

@Route("gtp-chat")
@PageTitle("Chat")
public class ChatView extends AppLayout {

    private final Grid<ChatHistory> chatHistoryGrid;
    private final MessageList chat;
    private final MessageInput input;

    private final UserService userService;
    private final OpenAI openAI;
    private final ChatHistoryService chatHistoryService;

    private final String USER_AVATAR = "https://api.dicebear.com/7.x/lorelei/svg?seed=Jack";
    private final String AI_AVATAR = "https://api.dicebear.com/7.x/bottts/svg?seed=Bubba&baseColor=039be5&mouth=smile01";
    private final String SYSTEM_AVATAR = "https://api.dicebear.com/6.x/bottts/svg?seed=Sheba";

    @Autowired
    public ChatView(UserService userService, OpenAI openAI, ChatHistoryService chatHistoryService) {
        this.userService = userService;
        this.openAI = openAI;
        this.chatHistoryService = chatHistoryService;

        // Setup header and navigation
        H1 title = new H1("BU GPT");
        title.getElement().getStyle().set("margin", "0");
        RouterLink historyLink = new RouterLink("Chat History", ChatView.class); // assuming this is the same view for history
        RouterLink settingsLink = new RouterLink("Settings", SettingsView.class);
        Anchor logoutLink = new Anchor("", "Log out");

        // Layout for header
        HorizontalLayout header = new HorizontalLayout(title, historyLink, settingsLink, logoutLink);
        header.setWidthFull();
        header.setJustifyContentMode(JustifyContentMode.BETWEEN);
        header.setDefaultVerticalComponentAlignment(Alignment.CENTER);

        // Chat components
        chatHistoryGrid = new Grid<>(ChatHistory.class);
        chatHistoryGrid.setHeightFull();

        chat = new MessageList();
        input = new MessageInput();
        input.setWidth("100%");
        input.addSubmitListener(this::onSubmit);

        // Layout for chat list and input
        VerticalLayout chatLayout = new VerticalLayout(chat, input);
        chatLayout.setHeightFull();
        chatLayout.setPadding(false);
        chatLayout.setSpacing(false);
        chatLayout.setAlignItems(Alignment.STRETCH);

        // Combine layouts
        HorizontalLayout content = new HorizontalLayout(chatHistoryGrid, chatLayout);
        content.setFlexGrow(1, chatLayout);
        content.setSizeFull(); // This sets the size of the content to full instead
        content.setPadding(false);
        content.setSpacing(false);
        setContent(content);

        // Apply to the main view
        addToNavbar(header);
        setContent(content);

        updateChatHistory();
    }

    private void onSubmit(MessageInput.SubmitEvent event) {
        String message = event.getValue().trim();
        User currentUser = userService.getCurrentUser();
        if (!message.isEmpty() && currentUser != null) {
            MessageListItem userMessageItem = new MessageListItem(message, Instant.now(), formatName("user"), getAvatar("user"));
            List<MessageListItem> items = new ArrayList<>(chat.getItems());
            items.add(userMessageItem);
            chat.setItems(items);

            openAI.sendAsync(message).whenComplete((messages, throwable) -> {
                if (throwable != null) {
                    throwable.printStackTrace(); // Consider better error handling
                    return;
                }
                getUI().ifPresent(ui -> ui.access(() -> {
                    updateChatWithResponse(messages);
                    // Save AI response in chat history
                    messages.stream()
                            .filter(msg -> "assistant".equals(msg.getRole()))
                            .forEach(msg -> saveChatHistory(currentUser, message, msg.getContent()));
                }));
            });

            // Save user message in chat history
            saveChatHistory(currentUser, message, null);
        }
    }


    private void saveChatHistory(User currentUser, String userMessage, String aiMessage) {
        ChatHistory userChatHistory = new ChatHistory(currentUser.getUsername(), "BU Assistant", userMessage);
        chatHistoryService.saveChatHistoryAsync(userChatHistory);
        if (aiMessage != null) {
            ChatHistory aiChatHistory = new ChatHistory("BU Assistant", currentUser.getUsername(), aiMessage);
            chatHistoryService.saveChatHistoryAsync(aiChatHistory);
        }
    }





    private void sendMessage(String messageText) {
        if (messageText != null && !messageText.trim().isEmpty()) {
            // Here you would add the logic to send the message and update the chat list
        }
    }


    private void updateChatWithResponse(List<OpenAI.Message> messages) {
        // Filter out user messages if needed or handle them accordingly
        List<MessageListItem> assistantMessages = messages.stream()
                .filter(msg -> "assistant".equals(msg.getRole())) // or use a different condition if needed
                .map(this::convertMessage)
                .collect(Collectors.toList());

        List<MessageListItem> items = new ArrayList<>(chat.getItems());
        items.addAll(assistantMessages);
        chat.setItems(items);
    }


    private void updateChatHistory() {
        User currentUser = userService.getCurrentUser();
        if (currentUser != null) {
            List<ChatHistory> history = chatHistoryService.getChatHistoryForUser(currentUser.getUsername());
            chatHistoryGrid.setItems(history);
        }
    }

    private String getCurrentUserId() {
        User currentUser = userService.getCurrentUser();
        return currentUser != null ? currentUser.getUsername() : null;
    }





    private MessageListItem convertMessage(OpenAI.Message msg) {
        String displayName;
        String avatarUrl;

        if ("user".equals(msg.getRole())) {
            // Set the display name to the current user's username
            displayName = getCurrentUserDisplayName();
            avatarUrl = USER_AVATAR;
        } else if ("assistant".equals(msg.getRole())) {
            // Set the display name to "BU Assistant"
            displayName = "BU Assistant";
            avatarUrl = AI_AVATAR;
        } else {
            displayName = "System";
            avatarUrl = SYSTEM_AVATAR;
        }

        return new MessageListItem(msg.getContent(), msg.getTime(), displayName, avatarUrl);
    }



    private String getCurrentUserDisplayName() {
        User currentUser = userService.getCurrentUser();
        return currentUser != null ? currentUser.getUsername() : "User";
    }


    private String getAvatar(String role) {
        if ("assistant".equals(role)) {
            return AI_AVATAR;
        }
        if ("user".equals(role)) {
            return USER_AVATAR;
        }
        return SYSTEM_AVATAR;
    }

    private String formatName(String role) {
        return role != null && !role.isEmpty()? role.substring(0,1).toUpperCase()+role.substring(1): role;
    }

}