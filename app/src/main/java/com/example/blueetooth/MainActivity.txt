package com.example.blueetooth;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.TextView;
import android.widget.ToggleButton;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Locale;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private static final String DEVICE_ADDRESS = "FC:A8:9A:00:14:9C"; // Replace with your HC-05 module's MAC address
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static final int REQUEST_BLUETOOTH_PERMISSION = 1;
    BluetoothSocket socket;
    private TextToSpeech textToSpeech;
    private ActivityResultLauncher<Intent> speechRecognitionLauncher;
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    String text;
    private MediaPlayer mediaPlayer;
    private boolean connected;
    public SharedPreferences sharedPreferences;
    char[] switch_state = {'0','0','0','0','0','0','0','0'};
    @RequiresApi(api = Build.VERSION_CODES.S)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Request Bluetooth permission if not granted
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, REQUEST_BLUETOOTH_PERMISSION);
            }
        }
        sharedPreferences = getSharedPreferences("MyAppPreferences", Context.MODE_PRIVATE);
        // Storing char array only if it has not been stored before
        String storedCharArrayAsString = sharedPreferences.getString("switch_state", "");
        if (storedCharArrayAsString.isEmpty()) {
            // If no value is stored, initialize with default value and store it
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("switch_state", String.valueOf(switch_state));
            editor.apply();
        } else {
            // If a value is stored, retrieve it and update switch_state array
            switch_state = storedCharArrayAsString.toCharArray();
        }
        TextView textViewResult=findViewById(R.id.textViewResult);
        textViewResult.setTextColor(getResources().getColor(R.color.myTextColor));
        // Initialize TextToSpeech
        textToSpeech = new TextToSpeech(this, status -> {
            if (status != TextToSpeech.ERROR) {
                textToSpeech.setLanguage(Locale.US);
            }
        });
        // Initialize ActivityResultLauncher for speech recognition
        speechRecognitionLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        ArrayList<String> matches = result.getData()
                                .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                        if (matches != null && !matches.isEmpty()) {
                            text = matches.get(0);
                            textViewResult.setText(text);
                            speakText(text);
                        }
                    }
                });
        mediaPlayer = MediaPlayer.create(this, R.raw.button_sound);
        ToggleButton toggleButton = findViewById(R.id.toggleButton);
        ToggleButton toggleButton2 = findViewById(R.id.toggleButton2);
        ToggleButton toggleButton3 = findViewById(R.id.toggleButton3);
        ToggleButton toggleButton4 = findViewById(R.id.toggleButton4);
        ToggleButton toggleButton5 = findViewById(R.id.toggleButton5);
        ToggleButton toggleButton6 = findViewById(R.id.toggleButton6);
        ToggleButton toggleButton7 = findViewById(R.id.toggleButton7);
        ToggleButton toggleButton8 = findViewById(R.id.toggleButton8);
        sharedPreferences = getSharedPreferences("ToggleState", Context.MODE_PRIVATE);
//        // Load the previous state of the toggle button from SharedPreferences
        boolean isChecked = sharedPreferences.getBoolean("isChecked", false);
        if(connected){
        toggleButton.setChecked(isChecked);
        toggleButton2.setChecked(isChecked);
        toggleButton3.setChecked(isChecked);
        toggleButton4.setChecked(isChecked);
        toggleButton5.setChecked(isChecked);
        toggleButton6.setChecked(isChecked);
        toggleButton7.setChecked(isChecked);
        toggleButton8.setChecked(isChecked);
        }
        toogle_state(toggleButton,0,'1','A',"");
        toogle_state(toggleButton2,1,'2','B',"");
        toogle_state(toggleButton3,2,'3','C',"");
        toogle_state(toggleButton4,3,'4','D',"");
        toogle_state(toggleButton5,4,'5','E',"night lamp  ");
        toogle_state(toggleButton6,5,'6','F',"socket  ");
        toogle_state(toggleButton7,6,'7','G',"fan  ");
        toogle_state(toggleButton8,7,'8','H',"light  ");

    }// closing on create method
    @SuppressLint("SetTextI18n")
    public void toogle_state(@NonNull ToggleButton toggleButton, int i, char on, char off, String s){
        TextView textViewResult=findViewById(R.id.textViewResult);
            toggleButton.setOnCheckedChangeListener((buttonView, isChecked1) -> {
                toggleButton.setChecked(isChecked1);
                switch_state[i] = isChecked1 ? off : on; // Update switch_state based on toggle state
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("isChecked", isChecked1);
                editor.apply();
                    if (switch_state[i] != on) {
                        if(connected){
                            speakText(s + "on");
                            textViewResult.setText(s+"on");
                        }
                            sendSwitchStates(on);
                    } else {
                        if(connected){
                            speakText(s + "off");
                            textViewResult.setText(s+"off");
                        }
                        sendSwitchStates(off);
                    }
               mediaPlayer.start();
            });
    }
    @SuppressLint("SetTextI18n")
    @RequiresApi(api = Build.VERSION_CODES.S)
    public void connect(View view) throws IOException {
        mediaPlayer.start();
        if(connected)
        {
            socket.close();
            speakText("socket close");
            connected=false;
            TextView textViewResult=findViewById(R.id.textViewResult);
            textViewResult.setText("socket close");
        }
        else {
            connectToDevice();
        }
    }
    @SuppressLint("SetTextI18n")
    public void startSpeechRecognition(View view) {
         mediaPlayer.start();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_RECORD_AUDIO_PERMISSION);
            return;
        }
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak something...");
        try {
            speechRecognitionLauncher.launch(intent); // Launch speech recognition activity
        } catch (ActivityNotFoundException e) {
            TextView textViewResult=findViewById(R.id.textViewResult);
            textViewResult.setText("Speech recognition not supported");
        }
    }
    private void speakText(String text) {
        textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
    }
    @SuppressLint("SetTextI18n")
    @RequiresApi(api = Build.VERSION_CODES.S)
    private void connectToDevice() {
        // Initialize Bluetooth adapter
        speakText("connection process started");
        TextView textViewResult=findViewById(R.id.textViewResult);
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        BluetoothDevice device = bluetoothAdapter.getRemoteDevice(DEVICE_ADDRESS);
        ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT);
        try {
            socket = device.createRfcommSocketToServiceRecord(MY_UUID);
            socket.connect();
            connected=true;
            speakText("connected successfully");
            textViewResult.setText("connected successfully");
        } catch (IOException e) {
            textViewResult.setText("error connecting");
            speakText("error connecting");
        }
    }
    public void sendSwitchStates(char c){
        if (socket != null && socket.isConnected()) {
            try {
                OutputStream outputStream = socket.getOutputStream();
                outputStream.write(c);
                outputStream.flush();
            } catch (IOException e) {
                speakText("error sending massage");
            }
        }
    }
    protected void onDestroy() {
        super.onDestroy();
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
    }
}
