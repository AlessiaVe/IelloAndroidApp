package com.projectiello.teampiattaforme.iello.UI.segnalazioneActivity;

import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.projectiello.teampiattaforme.iello.dataLogic.Segnalazione;

/**
 * Created by riccardomaldini on 08/10/17.
 * Classe per la gestione della connessione con il DB remoto, gestito con tecnologia Firebase.
 */
class FirebaseHandler implements OnCompleteListener<AuthResult> {

    // riferimenti agli attributi per il collegamento al DB
    private FirebaseDatabase mFirebaseDB;
    private FirebaseAuth mFireAuth;

    // riferimento a SegnalazioneActivity
    private SegnalazioneActivity mSegnalActivity;

    // costanti utili
    private static final String TAG = "FireHandler";
    private static final String TAG_SEGNALAZIONI = "segnalazioni";



    /**
     * Costruttore con il quale vengono inizializzati i vari componenti
     */
    FirebaseHandler(SegnalazioneActivity a) {
        // inizializza il riferimento all'activity
        mSegnalActivity = a;

        // inizializza il db firebase
        mFirebaseDB = FirebaseDatabase.getInstance();
        mFireAuth = FirebaseAuth.getInstance();
        mFireAuth.signInWithEmailAndPassword("piattaforme@gmail.com", "piattaforme101")
                .addOnCompleteListener(mSegnalActivity, this);

        // avvisa l'utente del collegamento in corso
        Toast.makeText(mSegnalActivity, "Mi sto collegando al DB Iello...", Toast.LENGTH_SHORT).show();
    }


    /**
     * Al termine dell'autenticazione alla piattaforma Firebase, vengono rese disponibili all'utente
     * le funzionalità della mappa.
     */
    @Override
    public void onComplete(@NonNull Task task) {
        mSegnalActivity.hideProgressBar();

        if (task.isSuccessful()) {
            // mostra un messaggio all'utente
            Log.d(TAG, "signInWithEmail:success");
            Toast.makeText(mSegnalActivity, "Connesso al DB! L'app è pronta a inviare segnalazioni.",
                    Toast.LENGTH_LONG).show();

            // attiva le funzioni della mappa
            mSegnalActivity.getMappa().attivaFunzioniMappa();
        } else {
            // mostra un messaggio all'utente
            Log.d(TAG, "signInWithEmail:failure", task.getException());
            Toast.makeText(mSegnalActivity, "Non sei connesso al DB. Non puoi inviare segnalazioni.",
                    Toast.LENGTH_LONG).show();

            // lascia disattivate le funzioni della mappa
        }
    }


    /**
     * Metodo per ottenere l'eventuale utente autenticato tramite Firebase
     */
    private FirebaseUser getFirebaseUser() {
        return mFireAuth.getCurrentUser();
    }


    /**
     * Metodo per inviare la segnalazione al DB remoto
     */
    void sendLocationToFirebase(LatLng location)
    {
        if (location != null  && getFirebaseUser() != null) {

            /* todo ricava l'indirizzo della coordinata tramite Google Geocoding API, se disponibile
            String indirizzo;
            Geocoder geocoder = new Geocoder(mSegnalActivity, Locale.getDefault());
            try {
                List<Address> addresses = geocoder.getFromLocation(pos.latitude, pos.longitude, 1);
                indirizzo = addresses.get(0).getAddressLine(0);
            } catch (IOException e) {
                e.printStackTrace();
                indirizzo = "Indirizzo non disponibile";
            }

            // todo carica anche l'indirizzo nell'oggetto segnalazione prima dell'invio
            */

            // crea la segnalzaione...
            Segnalazione segnalazione = new Segnalazione(
                    location.latitude,
                    location.longitude,
                    getFirebaseUser().getUid()
            );

            // ... la invia al database ...
            mFirebaseDB.getReference("/" + TAG_SEGNALAZIONI)
                    .push()
                    .setValue(segnalazione);

            // ... e notifica l'utente
            Log.d(TAG, "Inviata nuova coordinata al DB, " + location.toString());
            Toast.makeText(mSegnalActivity, "Segnalazione Inviata.", Toast.LENGTH_SHORT).show();

            // l'interfaccia viene aggiornata di conseguenza
            mSegnalActivity.getMappa().resettaInterfacciaMappa();

        } else {
            Log.d(TAG, "Errore inaspettato nell'invio della posizione");
            Toast.makeText(mSegnalActivity, "Errore inaspettato nell'invio della posizione. Riprova più tardi", Toast.LENGTH_SHORT).show();

        }
    }
}