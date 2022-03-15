package com.bigyoshi.qrhunt;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;

/**
 * Definition: Library to keep track of QR codes scanned by a certain player
 * Note: NA
 * Issues: TBA
 */
public class QRLibrary {

    private HashMap<String, PlayableQRCode> qrCodes;
    private String playerId;
    private FirebaseFirestore db;
    private double lat;
    private double lon;
    private String qrHash;
    private int score;
    private PlayableQRCode qrCode;

    /**
     * Finds player in database by ID and grabs all QR codes associated w/ them
     * @param db Player's QRDatabase
     * @param playerId Current player
     */
    public QRLibrary(FirebaseFirestore db, String playerId){
        qrCodes = new HashMap<>();
        if (playerId != null) {
        this.playerId = playerId;
        } else {
            this.playerId = "2c5ed7c6-545a-4a8d-bb90-f817e004f4a8";
        }
        this.db = db;
        update();
    }

    /**
     * Updates the QRLibrary of the player (aligning to the their QR database)
     */
    public void update() {
        Query qrList =  db.collection("users").document(playerId).collection("qrCodes");
        qrList.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot doc : task.getResult()) {
                    if (doc.exists()) {
                        doc.toObject(PlayableQRCode.class);
                    }
                }
            }
        });
    }

    public HashMap<String, PlayableQRCode> getQrCodes() { return qrCodes; }

    /**
     * Sorts all QRs in library from lowest to highest scoring
     */
    public void sortLowestToHighest(){
        /* Integer in HashMap would either be the value of the QRCode
        or just some sort of order we use to rank the QRCodes (ie the values)
         */
    }

    /**
     * Sorts all QRs in Library from highest to lowest scoring
     */
    public void sortHighestToLowest(){
        /* Integer in HashMap would either be the value of the QRCode
        or just some sort of order we use to rank the QRCodes (ie the values)
         */
    }
}
