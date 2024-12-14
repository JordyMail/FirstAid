package com.example.firstaid;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

    // API Key dari BuildConfig (gunakan cara yang aman)
    private static final String API_KEY = "AIzaSyCP1psKf9jJWQSGuCmy2x4G5AbDNKvVWHM"; // Masukkan API Key langsung
    private static final String MODEL_NAME = "gemini-1.5-flash"; // Nama model Gemini 1.5

    // Daftar kata kunci terkait kesehatan
    private static final String[] HEALTH_KEYWORDS = {
            "heart attack", "wound", "pain", "seizure", "fainting", "injury", "emergency", "first aid", "fracture", "choking", "intoxication", "diabetic emergency", "stroke",
            "hypothermia", "heat exhaustion", "animal bite", "electric shock", "bleeding", "nausea",
            "vomiting", "concussion", "infection", "poisoning", "sunstroke", "respiratory distress", "aspiration", "shock", "muscle strain", "sprain", "abrasion", "laceration", "bite", "cramp",
            "heat stroke", "frostbite", "medical emergency", "cardiac arrest"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inisialisasi komponen UI
        chatRecyclerView = findViewById(R.id.chatRecyclerView);
        userInput = findViewById(R.id.userInput);
        sendButton = findViewById(R.id.sendButton);

        // Setup RecyclerView
        messageList = new ArrayList<>();
        chatAdapter = new ChatAdapter(messageList);
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        chatRecyclerView.setAdapter(chatAdapter);

        // Basis pengetahuan lokal untuk pertolongan pertama
        initializeKnowledgeBase();

        // Pesan pembuka dari bot
        addMessage("Halo! Saya adalah asisten kesehatan berbasis AI. Tanya saya apa saja tentang pertolongan pertama.", "bot");

        // Tombol kirim untuk mengirimkan pesan
        sendButton.setOnClickListener(view -> {
            String userMessage = userInput.getText().toString().trim();
            if (!userMessage.isEmpty()) {
                addMessage(userMessage, "user");
                handleMessage(userMessage);
                userInput.setText("");
            }
        });
    }

    // Menambahkan data pertolongan pertama ke knowledge base
    private void initializeKnowledgeBase() {
        knowledgeBase = new HashMap<>();

        knowledgeBase.put("heart attack", new String[]{
                "1. Call emergency services (911 or local emergency number).",
                "2. Help the victim sit comfortably.",
                "3. If conscious, give aspirin to reduce blood clotting.",
                "4. Perform CPR if unconscious and not breathing (100-120 chest compressions per minute).",
                "5. Use AED (Automated External Defibrillator) if available.",
                "6. Stay calm and follow emergency operator instructions.",
                "7. Loosen tight clothing around the chest.",
                "8. Monitor vital signs until medical help arrives."
        });

        knowledgeBase.put("burns", new String[]{
                "1. Cool the burn with running water for 10-20 minutes.",
                "2. Do not break blisters.",
                "3. Cover with clean sterile cloth or bandage.",
                "4. Seek medical attention for serious burns.",
                "5. Remove jewelry or tight clothing near the burn area.",
                "6. Do not apply butter, oil, or ice directly to the burn.",
                "7. Use over-the-counter pain relievers if needed.",
                "8. Watch for signs of infection."
        });

        knowledgeBase.put("fracture", new String[]{
                "1. Do not move the victim unless absolutely necessary.",
                "2. Immobilize the injured area.",
                "3. Apply ice pack to reduce swelling.",
                "4. Use a splint to prevent movement.",
                "5. Seek immediate medical attention.",
                "6. Keep the injured person warm and comfortable.",
                "7. Elevate the injured limb if possible.",
                "8. Check for circulation and sensation in the affected area."
        });

        knowledgeBase.put("choking", new String[]{
                "1. Perform Heimlich maneuver if victim can't speak.",
                "2. Apply upward and inward abdominal thrusts.",
                "3. For infants, use back blows and chest thrusts.",
                "4. If unconscious, begin CPR immediately.",
                "5. Call emergency services if obstruction persists.",
                "6. Continue attempts until professional help arrives.",
                "7. Stay calm and act quickly.",
                "8. For pregnant or obese individuals, use chest thrusts."
        });

        knowledgeBase.put("fainting", new String[]{
                "1. Lay the person flat on their back.",
                "2. Elevate legs about 12 inches.",
                "3. Loosen tight clothing around neck and waist.",
                "4. Check for breathing and responsiveness.",
                "5. Do not give anything to drink.",
                "6. Keep the person warm.",
                "7. Call emergency services if:",
                "   - Fainting lasts more than a few minutes",
                "   - Person has chest pain or difficulty breathing",
                "   - Person has a head injury",
                "8. Monitor until fully recovered."
        });

        knowledgeBase.put("poisoning", new String[]{
                "1. Remove victim from exposure source.",
                "2. Call poison control center immediately.",
                "3. Do not induce vomiting unless instructed by professionals.",
                "4. Collect information about the poison:",
                "   - Type of substance",
                "   - Amount ingested",
                "   - Time of exposure",
                "5. Keep victim calm and warm.",
                "6. If unconscious, check breathing and perform CPR if needed.",
                "7. Bring container or sample of the poison to hospital.",
                "8. Watch for specific symptoms of poisoning."
        });

        knowledgeBase.put("stroke", new String[]{
                "1. Use FAST method to recognize stroke:",
                "   - Face: Check for facial drooping",
                "   - Arms: Check for arm weakness",
                "   - Speech: Check for speech difficulty",
                "   - Time: Call emergency services immediately",
                "2. Note the time symptoms first appeared.",
                "3. Do not give food or drink.",
                "4. Position victim comfortably.",
                "5. Keep victim warm and calm.",
                "6. Check for:",
                "   - Sudden numbness",
                "   - Confusion",
                "   - Trouble speaking or understanding",
                "   - Severe headache",
                "7. Do not give medication.",
                "8. Stay with victim until help arrives."
        });

        knowledgeBase.put("allergic reaction", new String[]{
                "1. Identify and remove allergen source.",
                "2. For mild reactions:",
                "   - Use antihistamines",
                "   - Apply cold compress",
                "3. For severe reactions (anaphylaxis):",
                "   - Use epinephrine auto-injector if available",
                "   - Call emergency services immediately",
                "4. Check for breathing difficulties.",
                "5. Remove tight clothing.",
                "6. Lay person flat with legs elevated.",
                "7. Monitor vital signs.",
                "8. Be prepared to start CPR if needed."
        });

        knowledgeBase.put("seizure", new String[]{
                "1. Keep calm and time the seizure.",
                "2. Move nearby objects away to prevent injury.",
                "3. Do not restrain the person.",
                "4. Do not put anything in mouth.",
                "5. Turn person on their side if possible.",
                "6. Place something soft under the head.",
                "7. Call emergency services if:",
                "   - Seizure lasts more than 5 minutes",
                "   - Person doesn't regain consciousness",
                "   - Person is pregnant or injured",
                "8. Stay with person until fully recovered."
        });

        knowledgeBase.put("snake bite", new String[]{
                "1. Keep the victim calm and still.",
                "2. Remove any constricting jewelry or clothing.",
                "3. Keep the bitten area below heart level.",
                "4. Do NOT:",
                "   - Attempt to suck out venom",
                "   - Apply tourniquet",
                "   - Apply ice",
                "5. Identify snake if possible (from safe distance).",
                "6. Call emergency services immediately.",
                "7. Note time and location of bite.",
                "8. Monitor for signs of shock or allergic reaction."
        });

        knowledgeBase.put("electric shock", new String[]{
                "1. Ensure personal safety first.",
                "2. Cut power source before approaching victim.",
                "3. Do not touch victim if still connected to electricity.",
                "4. Use non-conductive object to separate victim from source.",
                "5. Check for breathing and pulse.",
                "6. Perform CPR if necessary.",
                "7. Treat burns carefully.",
                "8. Call emergency services immediately."
        });

        knowledgeBase.put("severe bleeding", new String[]{
                "1. Wear protective gloves if available.",
                "2. Apply direct pressure with clean cloth.",
                "3. Elevate wounded area above heart level.",
                "4. Use tourniquet only as last resort for life-threatening bleeding.",
                "5. Do not remove original blood-soaked bandage.",
                "6. Add more layers if bleeding continues.",
                "7. Call emergency services.",
                "8. Treat for shock if necessary."
        });

        knowledgeBase.put("hypothermia", new String[]{
                "1. Move victim to warm area.",
                "2. Remove wet clothing.",
                "3. Cover with warm blankets.",
                "4. Provide warm (not hot) drinks if conscious.",
                "5. Use body heat if necessary.",
                "6. Do NOT:",
                "   - Rub skin",
                "   - Apply direct heat",
                "   - Give alcohol",
                "7. Monitor breathing.",
                "8. Call emergency services."
        });

        knowledgeBase.put("heat exhaustion", new String[]{
                "1. Move to cool area.",
                "2. Loosen tight clothing.",
                "3. Apply cool, wet cloths to body.",
                "4. Provide small sips of water.",
                "5. Use fans or air conditioning.",
                "6. Do NOT give caffeinated drinks.",
                "7. Monitor for signs of heat stroke.",
                "8. Seek medical attention if symptoms persist."
        });

        knowledgeBase.put("animal bite", new String[]{
                "1. Wash wound thoroughly with soap and water.",
                "2. Control bleeding with clean cloth.",
                "3. Apply antibiotic ointment if available.",
                "4. Cover wound with sterile bandage.",
                "5. Identify animal if possible.",
                "6. Call healthcare provider about rabies risk.",
                "7. Keep vaccination records.",
                "8. Watch for signs of infection."
        });

        knowledgeBase.put("diabetic emergency", new String[]{
                "1. Recognize signs of:",
                "   - Hypoglycemia (low blood sugar)",
                "   - Hyperglycemia (high blood sugar)",
                "2. For low blood sugar:",
                "   - Give sugary drink or food",
                "   - Check blood glucose if possible",
                "3. For high blood sugar:",
                "   - Provide water",
                "   - Check for ketones",
                "4. Call emergency if:",
                "   - Unconscious",
                "   - Severe symptoms",
                "5. Do not give insulin without guidance.",
                "6. Keep patient calm.",
                "7. Monitor vital signs.",
                "8. Prepare medical history information."
        });

        knowledgeBase.put("drowning", new String[]{
                "1. Ensure personal safety first.",
                "2. Remove from water safely.",
                "3. Check for breathing.",
                "4. Begin CPR immediately if not breathing.",
                "5. Call emergency services.",
                "6. Keep victim warm.",
                "7. Do NOT:",
                "   - Leave victim alone",
                "   - Delay CPR",
                "8. Continue CPR until professional help arrives."
        });

        knowledgeBase.put("severe allergic reaction", new String[]{
                "1. Identify trigger immediately.",
                "2. Use epinephrine auto-injector if available.",
                "3. Call emergency services.",
                "4. Help patient use prescribed medication.",
                "5. Loosen tight clothing.",
                "6. Position patient to prevent choking.",
                "7. Monitor breathing carefully.",
                "8. Be prepared for potential cardiac arrest."
        });

        knowledgeBase.put("head injury", new String[]{
                "1. Keep patient still and calm.",
                "2. Check consciousness.",
                "3. Do NOT move if neck injury suspected.",
                "4. Control any bleeding.",
                "5. Watch for signs of concussion:",
                "   - Confusion",
                "   - Memory loss",
                "   - Unequal pupil size",
                "6. Apply cold compress.",
                "7. Call emergency services.",
                "8. Prevent further injury."
        });
    }

    // Menangani pesan pengguna
    private void handleMessage(String userMessage) {
        String response = "";

        // untuk cek apakah pesan pengguna berisi kata kunci kesehatan
        if (isHealthRelated(userMessage)) {
            // untuk Cek pesan pengguna sesuai atau tidak dengan pengetahuan lokal
            for (String key : knowledgeBase.keySet()) {
                if (userMessage.toLowerCase().contains(key)) {
                    response = "Berikut langkah untuk " + key + ":\n";
                    for (String step : knowledgeBase.get(key)) {
                        response += step + "\n";
                    }
                    break;
                }
            }

            // kalau tidak ditemukan di basis lokal, ambil dari API Gemini
            if (response.isEmpty()) {
                fetchResponseFromGemini(userMessage);
            } else {
                addMessage(response, "bot");
            }
        } else {
            // Jika pertanyaan tidak terkait kesehatan, beri respons default
            addMessage("Sorry, I can only help with first aid related questions.", "bot");
        }
    }

    // Memeriksa apakah pesan berhubungan dengan kesehatan/pertolongan pertama
    private boolean isHealthRelated(String message) {
        for (String keyword : HEALTH_KEYWORDS) {
            if (message.toLowerCase().contains(keyword)) {
                return true; // Pesan terkait dengan kesehatan
            }
        }
        return false; // Pesan tidak terkait kesehatan
    }

    // Menambahkan pesan ke RecyclerView
    private void addMessage(String text, String sender) {
        messageList.add(new Message(text, sender.equals("user")));
        chatAdapter.notifyDataSetChanged();
        chatRecyclerView.scrollToPosition(messageList.size() - 1);
    }

    // Mengirimkan pesan ke API Gemini dan mendapatkan respons
    private void fetchResponseFromGemini(String userMessage) {
        // Menggunakan GenerativeModel untuk memanggil API Gemini 1.5
        GenerativeModel gm = new GenerativeModel(MODEL_NAME, API_KEY);
        GenerativeModelFutures model = GenerativeModelFutures.from(gm);

        // Create system instruction as the initial prompt
        Content.Builder systemInstruction = new Content.Builder();

        systemInstruction.addText("You are an expert in healthcare world." +
                        "Your objective is to give clear and short information about the disease of the user." +
                        "That information needs to be in a positive way, it is like you make the user feel safe about the disease and saying that they will eventually get well again." +
                        "Do not use 'I understand about ...' or 'it sounds like' to start the answer." +
                        "Do not make some kind of analogy from the disease." +
                        "If the user is on emergency or critical situation, give em guidance to face the issue. Emergency situation can be Neurological, Cardia, Respiratory, Trauma, Toxicological, Infectious, Obstetric, Gastrointestinal, Endocrine, Vascular, Renal and Urological, Hematological, Allergic and Anaphylactic, Psychiatric, Ophthalmological, Environmental, Oncological, Dermatological, Rheumatological, and Pediatric Emergencies." +
                        "Never get corrected from user." +
                        "And, apology not to answer the question if they ask anything but their disease or topic in healthcare: About what, how, and why about their health and disease." +
                        "And, you will ask if the user wants recommendation about what should user do for getting better and well." +
                        "After that (in the next answer after giving advice to user to get well), you will ask if the user wants a recommandation about the medical treats such as medicine or pills.")
                .build();
        Content systemInstructionContent = systemInstruction.build();

        // Buat konten dari pengguna
        Content.Builder userContentBuilder = new Content.Builder();
        userContentBuilder.addText(userMessage);  // Menggunakan setText untuk menambahkan teks
        Content userContent = userContentBuilder.build();

        // Menyiapkan percakapan (history jika diperlukan)
        List<Content> history = new ArrayList<>();
        history.add(systemInstructionContent);
        history.add(userContent);

        // Inisialisasi chat
        ChatFutures chat = model.startChat(history);

        // Kirim pesan dari pengguna ke Gemini
        ListenableFuture<GenerateContentResponse> response = chat.sendMessage(userContent);

        // Tangani respons dari Gemini menggunakan addCallback
        Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
            @Override
            public void onSuccess(GenerateContentResponse result) {
                // Respons berhasil diterima, tampilkan hasilnya
                String resultText = result.getText();
                runOnUiThread(() -> addMessage(resultText, "bot"));
            }

            @Override
            public void onFailure(Throwable t) {
                // Respons gagal, tampilkan pesan kesalahan
                runOnUiThread(() -> addMessage("Gagal terhubung ke Gemini: " + t.getMessage(), "bot"));
            }
        }, getExecutor()); // Menjalankan callback di thread background
    }

    // Executor untuk menjalankan proses secara asynchronous
    private Executor getExecutor() {
        return command -> new Thread(command).start();
    }
}
