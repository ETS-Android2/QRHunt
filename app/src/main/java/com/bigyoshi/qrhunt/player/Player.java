package com.bigyoshi.qrhunt.player;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.bigyoshi.qrhunt.qr.QrLibrary;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Definition: Object representing player, keeps track of all player data and deals with db functions regarding players
 * Note: N/A
 * Issues: N/A
 */
public class Player implements Serializable {
    private static final String TAG = Player.class.getSimpleName();


    protected transient FirebaseFirestore db;
    protected transient CollectionReference collectionReference;

    protected int totalScore;
    protected int numScanned;
    protected final RankInfo rankInfo;
    protected final BestQr bestUniqueQr;
    protected final BestQr bestScoringQr;
    protected String username;
    protected Contact contact;
    protected Boolean admin;
    protected String playerId;
    // TODO: fix this. In theory, QrLibrary is _perfectly_ serializable. The android runtime disagrees
    // we're leaving it this way for now because confusingly, everything seems to work
    public transient QrLibrary qrLibrary;
    protected transient Context context;

    /**
     * Constructor method
     *
     * @param playerId  Player id
     * @param context   Context
     */
    public Player(String playerId, Context context) {
        db = FirebaseFirestore.getInstance();
        collectionReference = db.collection("users");
        this.context = context;
        this.rankInfo = new RankInfo();
        this.bestScoringQr = new BestQr();
        this.bestUniqueQr = new BestQr();
        this.totalScore = 0;
        this.numScanned = 0;
        this.username = "";
        this.playerId = playerId;
        this.admin = false;
        this.contact = new Contact();
        this.qrLibrary = new QrLibrary(db, Optional.ofNullable(playerId).orElse(getPlayerId()));
    }

    /**
     * Get player by player id
     *
     * @param playerId  Player id
     * @return player
     */
    @Deprecated
    public static Player fromPlayerId(String playerId) {
        // note: don't ever use this, left in for legacy reasons
        // what this does is serve you a race condition on a silver platter
        Player player = new Player(playerId, null);
        player.initialize();
        return player;
    }

    /**
     * Get player from the database
     *
     * @param doc   doc result from query
     * @return player
     */
    public static Player fromDoc(DocumentSnapshot doc) {
        Player player = new Player(doc.getId(), null);
        player.setPropsFromDoc(doc);
        return player;
    }

    /**
     * Getter method
     *
     * @return player Id
     */
    public String getPlayerId() {
        return playerId;
    }

    /**
     * Setter method
     *
     * @param playerId player Id
     */
    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }

    /**
     * Getter method
     *
     * @return Best unique qr
     */
    public BestQr getBestUniqueQr() {
        return bestUniqueQr;
    }

    /**
     * Getter method
     *
     * @return Best scoring Qr
     */
    public BestQr getBestScoringQr() {
        return bestScoringQr;
    }

    /**
     * Getter method
     *
     * @return RankInfo
     */
    public RankInfo getRankInfo() {
        return rankInfo;
    }

    /**
     * Getter method
     *
     * @return Contact contact
     */
    public Contact getContact() {
        return this.contact;
    }

    /**
     * Getter method
     *
     * @return Integer score
     */
    public int getTotalScore() {
        return this.totalScore;
    }

    /**
     * Getter method
     *
     * @return String username
     */
    public String getUsername() {
        return this.username;
    }


    /**
     * Setter method (both email and social media contact)
     *
     * @param contact   todo tag
     */
    public void setContact(Contact contact) {
        // Use the editTextId to identify which contact to update (with toUpdate)
        this.contact.setSocial(contact.getSocial());
        this.contact.setEmail(contact.getEmail());
    }

    /**
     * Setter method
     *
     * @param newName new username to assign
     */
    public void setUsername(String newName) {
        this.username = newName;
    }

    /**
     * Assigns another player admin if this player is admin
     *
     * @param newAdmin new Admin to assign
     * @return String describing whether or not player was made admin
     */
    public String makeAdmin(Player newAdmin) {
        if (this.isAdmin()) {
            newAdmin.admin = true;
            return "PlayerInfo now admin.";
        } else {
            newAdmin.admin = false;
            return "PlayerInfo did not become admin.";
        }
    }


    /**
     * Checks whether a player is an admin
     *
     * @return Boolean representing whether a player is an admin or not
     */
    public Boolean isAdmin() {
        return admin;
    }

    /**
     * Saves PlayerData in HashMap to database associated with playerId
     */
    public void savePlayer() {
        HashMap<String, Object> playerData = new HashMap<>();
        if (playerId.length() > 0) {
            // If there’s some data in the EditText field, then we create a new key-value pair.
            playerData.put("admin", this.admin);
            playerData.put("contact", this.contact);
            playerData.put("totalScore", this.totalScore);
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

    /**
     * Gets playerData from database by matching against playerIds in database
     */
    public void initialize() {
        collectionReference.document(playerId).get().addOnCompleteListener(docTask -> {
            if (docTask.getException() == null && docTask.getResult() != null && docTask.getResult().getData() != null) {
                setPropsFromDoc(docTask.getResult());
            }
        });
    }

    /**
     * Gets the player information from the database
     *
     * @param doc   doc result
     */
    private void setPropsFromDoc(DocumentSnapshot doc) {
        Log.d(TAG, String.valueOf(doc.getData().get("admin")));
        Log.d(TAG, String.valueOf(doc.getData().get("contact")));
        Log.d(TAG, String.valueOf(doc.getData().get("totalScore")));
        Log.d(TAG, String.valueOf(doc.getData().get("username")));

        this.admin = (Boolean) doc.getData().get("admin");
        this.username = (String) doc.getData().get("username");
        admin = (Boolean) doc.getData().get("admin");
        setUsername((String) doc.getString("username"));
        setPlayerId(doc.getId());

        HashMap<String, String> contactMap = (HashMap<String, String>) doc.getData().get("contact");
        if (contactMap != null && this.contact != null) {
            this.contact.setEmail(contactMap.get("email"));
            this.contact.setSocial(contactMap.get("social"));
        }

        Map<String, Long> rankInfoMap = (HashMap<String, Long>) doc.get("rank");
        if (rankInfoMap != null) {
            this.rankInfo.setTotalScannedRank(Math.toIntExact(rankInfoMap.getOrDefault("totalScanned", Long.valueOf(1))));
            this.rankInfo.setBestUniqueQrRank(Math.toIntExact(rankInfoMap.getOrDefault("bestUniqueQr", (Long.valueOf(1)))));
            this.rankInfo.setTotalScoreRank(Math.toIntExact(rankInfoMap.getOrDefault("totalScore", (Long.valueOf(1)))));
        }
        // code duplication is cool 😎
        Map<String, Object> bestScoringQrMap = (HashMap<String, Object>) doc.get("bestScoringQr");
        if (bestScoringQrMap != null) {
            this.bestScoringQr.setQrId((String) bestScoringQrMap.getOrDefault("qrId", null));
            this.bestScoringQr.setScore(Math.toIntExact((Long) bestScoringQrMap.getOrDefault("score", 0)));
        }
        Map<String, Object> bestUniqueQrMap = (HashMap<String, Object>) doc.get("bestUniqueQr");
        if (bestUniqueQrMap != null) {
            this.bestUniqueQr.setQrId((String) bestUniqueQrMap.getOrDefault("qrId", null));
            this.bestUniqueQr.setScore(Math.toIntExact((Long) bestUniqueQrMap.getOrDefault("score", 0)));
        }
        totalScore = Math.toIntExact((long) doc.getData().get("totalScore"));
        numScanned = doc.getLong("totalScanned") != null ? Math.toIntExact(doc.getLong("totalScanned")) : 0;
    }

    /**
     * Updates db within the document associated with playerId
     */
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
        collectionReference
                .document(playerId)
                .update("username", this.username);
    }

    /**
     * Getter method
     *
     * @return Number of qr scanned
     */
    public int getNumScanned() {
        return numScanned;
    }

    /**
     * Setter method
     *
     * @param numScanned new number scanned
     */
    public void setNumScanned(int numScanned) {
        this.numScanned = numScanned;
    }
}
