package com.example.application.views.app;

import com.example.application.OpenAI;
import com.example.application.model.ChatHistory;
import com.example.application.model.User;
import com.example.application.service.ChatHistoryService;
import com.example.application.service.UserService;
import com.vaadin.flow.component.AttachEvent;
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
import java.time.ZoneId;
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

    private Instant lastMessageTimestamp = Instant.MIN;

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
        RouterLink historyLink = new RouterLink("Chat History", ChatHistoryView.class); // assuming this is the same view for history
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

        // Buttons for chat history and clear chat
        Button loadHistoryButton = new Button("Load History", event -> loadUserMessages());
        Button clearChatButton = new Button("Clear Chat", event -> clearChat());
        HorizontalLayout buttonLayout = new HorizontalLayout(loadHistoryButton, clearChatButton);

        // Layout for chat list and input
        VerticalLayout chatLayout = new VerticalLayout(chat, input, buttonLayout);
        chatLayout.setHeightFull();
        chatLayout.setPadding(false);
        chatLayout.setSpacing(false);
        chatLayout.setAlignItems(Alignment.STRETCH);

        // Combine layouts
        HorizontalLayout content = new HorizontalLayout(chatHistoryGrid, chatLayout);
        content.setFlexGrow(1, chatLayout);
        content.setSizeFull();
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
                    throwable.printStackTrace();
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


//    @Override
//    protected void onAttach(AttachEvent attachEvent) {
//        super.onAttach(attachEvent);
//        if (chat.getItems().isEmpty()) {
//            loadUserMessages();
//        }
//    }

    private void loadUserMessages() {
        User currentUser = userService.getCurrentUser();
        if (currentUser != null) {
            List<ChatHistory> messages = chatHistoryService.getChatHistoryForUser(currentUser.getUsername());
            messages.forEach(this::addMessageToList);
        }
    }



    private void addMessageToList(ChatHistory message) {
        User currentUser = userService.getCurrentUser();
        Instant messageInstant = message.getTimestamp().atZone(ZoneId.systemDefault()).toInstant();
        String displayName = message.getSenderId().equals(currentUser.getUsername()) ? currentUser.getUsername() : message.getSenderId();
        String avatarUrl = displayName.equals(currentUser.getUsername()) ? USER_AVATAR : AI_AVATAR;

        MessageListItem item = new MessageListItem(message.getMessage(), messageInstant, displayName, avatarUrl);
        List<MessageListItem> currentItems = new ArrayList<>(chat.getItems());
        currentItems.add(item);
        chat.setItems(currentItems);
    }

    private void updateChatWithResponse(List<OpenAI.Message> messages) {
        List<MessageListItem> newMessages = messages.stream()
                .filter(msg -> "assistant".equals(msg.getRole()))
                .filter(msg -> msg.getTime().isAfter(lastMessageTimestamp)) // Only add messages that are newer
                .map(this::convertMessage)
                .collect(Collectors.toList());

        if (!newMessages.isEmpty()) {
            // Update last message timestamp with the newest message from the response
            lastMessageTimestamp = newMessages.get(newMessages.size() - 1).getTime();
            List<MessageListItem> currentItems = new ArrayList<>(chat.getItems());
            currentItems.addAll(newMessages);
            chat.setItems(currentItems); // Update the chat with new messages only
        }
    }

    private void clearChat() {
        chat.setItems(new ArrayList<>());
    }

    private void updateChatHistory() {
        User currentUser = userService.getCurrentUser();
        if (currentUser != null) {
            List<ChatHistory> history = chatHistoryService.getChatHistoryForUser(currentUser.getUsername());
            chatHistoryGrid.setItems(history);
        }
    }


    private String getCurrentUserDisplayName() {
        User currentUser = userService.getCurrentUser();
        return currentUser != null ? currentUser.getUsername() : "User";
    }


    private MessageListItem convertMessage(OpenAI.Message msg) {
        String displayName;
        String avatarUrl;


        if ("user".equals(msg.getRole())) {
            displayName = getCurrentUserDisplayName(); // Use the username of the logged-in user
            avatarUrl = USER_AVATAR; // Avatar for the user
        } else if ("assistant".equals(msg.getRole())) {
            displayName = "BU Assistant"; // Name for the assistant
            avatarUrl = AI_AVATAR; // Avatar for the assistant
        } else {
            displayName = "System"; // Default name for system messages
            avatarUrl = SYSTEM_AVATAR; // Default avatar for system messages
        }

        return new MessageListItem(msg.getContent(), msg.getTime(), displayName, avatarUrl);
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