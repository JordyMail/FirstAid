package com.example.firstaid;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

// for google generative ai
import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.ChatFutures;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executor;

public class MainActivity extends AppCompatActivity {

    private RecyclerView chatRecyclerView;
    private EditText userInput;
    private Button sendButton;
    private ChatAdapter chatAdapter;
    private List<Message> messageList;
    private HashMap<String, String[]> knowledgeBase;

    // set api and model
    private static final String API_KEY = "AIzaSyCP1psKf9jJWQSGuCmy2x4G5AbDNKvVWHM"; // Masukkan API Key langsung
    private static final String MODEL_NAME = "gemini-1.5-flash"; // Nama model Gemini 1.5


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // initialize UI from app/res/layout
        chatRecyclerView = findViewById(R.id.chatRecyclerView);
        userInput = findViewById(R.id.userInput);
        sendButton = findViewById(R.id.sendButton);

        // RecyclerView setup
        messageList = new ArrayList<>();
        chatAdapter = new ChatAdapter(messageList);
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        chatRecyclerView.setAdapter(chatAdapter);

        // message from chatbot to open conversation
        addMessage("Hello, this is your healthcare chatbot. What can i help for you?", "bot");

        // logic fo sendButton on activity_main.xml
        sendButton.setOnClickListener(view -> {
            String userMessage = userInput.getText().toString().trim();
            if (!userMessage.isEmpty()) {
                addMessage(userMessage, "user");
                handleMessage(userMessage);
                userInput.setText("");
            }
        });
    }


    // answer to user's messages
    private void handleMessage(String userMessage) {
        String response = "";

        // Check for specific keywords to generate a dynamic response
        if (userMessage.toLowerCase().contains("password")) {
            response = "To reset your password, go to the login page and click 'Forgot Password'.";
        } else if (userMessage.toLowerCase().contains("fever")) {
            response = "Symptoms of fever include elevated body temperature, sweating, and chills. If you have severe symptoms, consult a doctor.";
        }

        // Fallback to fetch response from Gemini API if no response was generated
        if (response.isEmpty()) {
            fetchResponseFromGemini(userMessage);
        } else {
            addMessage(response, "bot");
        }
    }


    // add message to RecyclerView
    private void addMessage(String text, String sender) {
        messageList.add(new Message(text, sender.equals("user")));
        chatAdapter.notifyDataSetChanged();
        chatRecyclerView.scrollToPosition(messageList.size() - 1);
    }

    // method for give/ask response to/from gemini by using api and model
    private void fetchResponseFromGemini(String userMessage) {

        GenerativeModel gm = new GenerativeModel(MODEL_NAME, API_KEY);
        GenerativeModelFutures model = GenerativeModelFutures.from(gm);

        // Create system instruction as the initial prompt
        Content.Builder systemInstruction = new Content.Builder();

        systemInstruction.addText("You are an expert in healthcare world." +
                        "Your objective is to give clear and short information about the disease of the user." +
                        "That information needs to be in a positive way, it is like you make the user feel safe about the disease and saying that they will eventually get well again." +
                        "Do not use 'I understand about ...' or 'it sounds like' to start the answer." +
                        "Do not make some kind of analogy from the disease." +
                        "Never get corrected from user." +
                        "And, apology not to answer the question if they ask anything but their disease or topic in healthcare: About what, how, and why about their health and disease." +
                        "And, you will ask if the user wants recommendation about what should user do for getting better and well." +
                        "After that (in the next answer after giving advice to user to get well), you will ask if the user wants a recommandation about the medical treats such as medicine or pills.")
                .build();
        Content systemInstructionContent = systemInstruction.build();

        // create content from the user
        Content.Builder userContentBuilder = new Content.Builder();
        userContentBuilder.addText(userMessage);
        Content userContent = userContentBuilder.build();

        // prepare for a conversation, initialize system instruction
        List<Content> history = new ArrayList<>();
        history.add(systemInstructionContent);
        history.add(userContent);

        ChatFutures chat = model.startChat(history);

        // Gemini's response
        ListenableFuture<GenerateContentResponse> response = chat.sendMessage(userContent);

        Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
            //successfully
            @Override
            public void onSuccess(GenerateContentResponse result) {
                // Respons berhasil diterima, tampilkan hasilnya
                String resultText = result.getText();
                runOnUiThread(() -> addMessage(resultText, "bot"));
            }

            // failed
            @Override
            public void onFailure(Throwable t) {
                runOnUiThread(() -> addMessage("Failed to connect the big bos: " + t.getMessage(), "bot"));
            }
        }, getExecutor());
    }

    private Executor getExecutor() {
        return command -> new Thread(command).start();
    }
}
