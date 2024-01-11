package org.schlunzis.kurtama.client.fx.controller;

import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.rgielen.fxweaver.core.FxmlView;
import org.schlunzis.kurtama.client.service.IChatService;
import org.springframework.stereotype.Component;

@Slf4j
@FxmlView("chat.fxml")
@Component
@RequiredArgsConstructor
public class ChatController {

    private final IChatService chatService;

    @FXML
    private ListView<String> chatListView;
    @FXML
    private TextField chatTextField;
    @FXML
    private TextField senderNameTextField;

    @FXML
    public void initialize() {
        chatListView.setItems(chatService.getChatMessages());
        senderNameTextField.setText(chatService.getCurrentUsername());
    }

    @FXML
    private void sendMessage() {
        String text = chatTextField.getText();
        if (text.isBlank())
            return;
        chatService.sendMessage(senderNameTextField.getText(), text);
        chatTextField.setText("");
    }

}
