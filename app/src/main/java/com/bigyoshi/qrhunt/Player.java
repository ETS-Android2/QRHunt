package com.bigyoshi.qrhunt;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Random;
import java.util.UUID;

public class Player implements Serializable {
    private static final String TAG = Player.class.getSimpleName();
    private static final String SHARED_PREFS = "sharedPrefs";
    private static final String PLAYER_ID_PREF = "playerId";
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    final CollectionReference collectionReference = db.collection("users");

//    private PlayerInfo playerInfo;
    private int totalScore;
//    private int QRTotalScanned;
    private String username;
    private Contact contact;
    private Boolean admin;
    private QRLibrary qrLibrary;
    private String playerId = null;
    private Context context;

    public Player(Context context) {
        this.context = context;
//        playerInfo = new PlayerInfo(context);
        this.totalScore = 0;
//        this.QRTotalScanned = 0;
        this.username = generateUsername(context);
        this.admin = false;
        this.contact = new Contact();
//        qrLibrary = new QRLibrary();
    }

    public Contact getContact(){
        return this.contact;
    }

//    public int getQRTotalScanned(){
//        return this.QRTotalScanned;
//    }

    public int getTotalScore(){
        return this.totalScore;
    }

    public String getUsername(){
        return this.username;
    }

    public Boolean isAdmin(){
        return admin;
    }

    public void updateContact(Contact contact){
        // Use the editTextId to identify which contact to update (with toUpdate)
        this.contact.updateSocial(contact.getSocial());
        this.contact.updateEmail(contact.getEmail());
    }

    public void updateUsername(String newName){
        this.username = newName;
    }

    public String makeAdmin(Player newAdmin, Player approvingAdmin){
        if (approvingAdmin.isAdmin()){
            newAdmin.admin = true;
            return "PlayerInfo now admin.";
        } else {
            newAdmin.admin = false;
            return "PlayerInfo did not become admin.";
        }
    }

    public String getPlayerId() {
        // fetched lazily, but only once
        if (playerId == null) {
            SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);
            playerId = sharedPreferences.getString(PLAYER_ID_PREF, "");
            // generated lazily, only once
            if (playerId.isEmpty()) {
                setPlayerId(UUID.randomUUID().toString());
            }
        }
        Log.d(TAG, String.format("retrieved uuid: %s", playerId));
        return playerId;
    }

    public void setPlayerId(String id) {
        // to think about: when the id is changed, it's essentially a new player?
        playerId = id;
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(PLAYER_ID_PREF, playerId);
        editor.apply();
        Log.d(TAG, String.format("set uuid: %s", playerId));
        savePlayer();
    }

    public void savePlayer() {
        HashMap<String, Object> playerData = new HashMap<>();
        if (playerId.length() > 0) {
            // If there’s some data in the EditText field, then we create a new key-value pair.
            playerData.put("admin", this.admin);
            playerData.put("contact", this.contact);
            playerData.put("totalScore", this.totalScore);
//            playerData.put("qrTotalScanned", this.QRTotalScanned);
            playerData.put("username", this.username);
            // The set method sets a unique id for the document
            collectionReference
                    .document(playerId)
                    .set(playerData)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            // These are a method which gets executed when the task is succeeded

                            Log.d(TAG, "Data has been added successfully!");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // These are a method which gets executed if there’s any problem
                            Log.d(TAG, "Data could not be added!" + e.toString());
                        }
                    });
        }
    }

    public void initialize() {
        collectionReference.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException error) {
                for (QueryDocumentSnapshot doc: queryDocumentSnapshots){
                    if (doc.getId().matches(playerId)) {
                        Log.d(TAG, String.valueOf(doc.getData().get("admin")));
                        Log.d(TAG, String.valueOf(doc.getData().get("contact")));
                        Log.d(TAG, String.valueOf(doc.getData().get("totalScore")));
//                        Log.d(TAG, String.valueOf(doc.getData().get("qrTotalScanned")));
                        Log.d(TAG, String.valueOf(doc.getData().get("username")));

                        admin = (Boolean) doc.getData().get("admin");
                        HashMap<String,String> contactMap = (HashMap<String,String>) doc.getData().get("contact");
                        contact.updateEmail(contactMap.get("email"));
                        contact.updateEmail(contactMap.get("social"));
                        totalScore = Math.toIntExact((long) doc.getData().get("totalScore"));
//                        QRTotalScanned = Math.toIntExact((long) doc.getData().get("qrTotalScanned"));
                        username = (String) doc.getData().get("username");
                    }
                }
            }
        });

    }

    public void updateDB() {
        collectionReference
                .document(playerId)
                .update("admin", this.admin);
        collectionReference
                .document(playerId)
                .update("contact", this.contact);
        collectionReference
                .document(playerId)
                .update("totalScore", this.totalScore);
//        collectionReference
//                .document(playerId)
//                .update("QRTotalScanned", this.QRTotalScanned);
        collectionReference
                .document(playerId)
                .update("username", this.username);
    }

    public String generateUsername(Context context){
        // Random unique username generated when account is first created
        Random rand = new Random();
        Resources res = context.getResources();
        String[] adj = res.getStringArray(R.array.adjectives);
        String[] noun = res.getStringArray(R.array.noun);
        String adjName = adj[rand.nextInt(adj.length - 1)];
        String nounName = noun[rand.nextInt(noun.length - 1)];
        int upperbound = 100;
        String numName = Integer.toString(rand.nextInt(upperbound));
        return adjName + nounName + numName;
    }
}
