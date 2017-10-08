package com.projectiello.teampiattaforme.iello.UI.mainActivity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.FrameLayout;

import com.miguelcatalan.materialsearchview.MaterialSearchView;
import com.projectiello.teampiattaforme.iello.R;
import com.projectiello.teampiattaforme.iello.UI.segnalazioneActivity.SegnalazioneActivity;
import com.projectiello.teampiattaforme.iello.UI.mainActivity.ricercaParcheggi.AddressedResearch;
import com.projectiello.teampiattaforme.iello.UI.mainActivity.ricercaParcheggi.GeolocalizedResearch;


/**
 * Created by riccardomaldini on 25/09/17.
 * Activity principale.
 */
public class MainActivity extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback {

    // riferimento alla searchBar
    private MaterialSearchView mSearchView;

    // riferimento al navigation drawer, il menù laterale
    private DrawerLayout mDrawerLayout;

    // riferimento alla progressBar
    private FrameLayout mProgBar;

    // classe per la gestione della geolocalizzazione e della ricerca geolocalizzata
    private GeolocalizedResearch mHelpLocalization;

    // riferimento al fragment parcheggi
    private ParcheggiFragment mFragmentAttivo;

    // riferimento all'oggetto gestore della mappa
    private MappaMain mMappa;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        // inizializza i vari elementi del navigation drawer
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeAsUpIndicator(R.drawable.ic_menu_white_24px);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        NavigationView navView = (NavigationView) findViewById(R.id.nav_view);
        navView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                menuItem.setChecked(false);
                mDrawerLayout.closeDrawers();

                // setta il comportamento dell'app al click dell'utente sulle voci del menù laterale
                switch(menuItem.getItemId()) {
                    case R.id.nav_segnalazione: {
                        Intent i = new Intent(MainActivity.this, SegnalazioneActivity.class);
                        startActivity(i);
                        break;
                    }
                    case R.id.nav_raggio: {
                        DialogRaggioRicerca.newInstance(MainActivity.this);
                        break;
                    }
                    case R.id.nav_personalizza: {
                        DialogStileMappa.newInstance(MainActivity.this);
                        break;
                    }
                    case R.id.nav_project: {
                        avviaDialogProject();
                        break;
                    }
                    case R.id.nav_api: {
                        String url = "http://www.github.com";
                        Intent i = new Intent(Intent.ACTION_VIEW);
                        i.setData(Uri.parse(url));
                        startActivity(i);
                        break;
                    }
                }
                return true;
            }
        });

        // inizializzazione della mappa mostrata nell'activity
        mMappa = new MappaMain(this);

        // inizializzazione progressBar. Utilizzata come feedback all'utente durante il download
        // dei parcheggi dall'API Iello. Di default la barra viene nascosta
        mProgBar = (FrameLayout) findViewById(R.id.clippedProgressBar);
        hideProgressBar();

        // inizializzazione gestore della geolocalizzazione e fab. Un click nel fab permette di
        // determinare la posizione dell'utente (in maniera spesso approssimativa), sposta la mappa
        // su tale posizione e mostra i risultati della ricerca.
        mHelpLocalization = new GeolocalizedResearch(this);
        FloatingActionButton fabGeolocalizzazione = (FloatingActionButton) findViewById(R.id.fab);
        fabGeolocalizzazione.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mHelpLocalization.avviaRicercaPosizione();
            }
        });

        // inizializzazione searchView. Questa permette di inserire un indirizzo. Questo verrà poi
        // utilizzato per cercare parcheggi per disabili in prossimità di tale indirizzo
        mSearchView = (MaterialSearchView) findViewById(R.id.search_view);
        mSearchView.setVoiceSearch(true);
        mSearchView.setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // al click viene avviata la ricerca
                AddressedResearch adpi = new AddressedResearch(MainActivity.this, query);
                adpi.execute();

                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
    }


    /**
     * Metodo per creare il dialog che descrive project Iello all'utente.
     */
    private void avviaDialogProject() {

        AlertDialog.Builder alertProject = new AlertDialog.Builder(this);

        alertProject.setIcon(R.drawable.ic_help_outline_black_24px);
        alertProject.setTitle(R.string.project_iello);
        alertProject.setMessage(R.string.project_iello_description);
        alertProject.setPositiveButton(R.string.fantastico, null);

        AlertDialog alert = alertProject.create();
        alert.show();
    }


    /**
     * Metodo invocato al click sull'hamburger. Apre il navigation drawer.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            mDrawerLayout.openDrawer(GravityCompat.START);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    /**
     * Invocato nel momento in cui l'utente approva un permesso; nel nostro caso la geolocalizzazione.
     * Tal permesso viene richiesto per individuare i parcheggi in prossimità della posizione dell'
     * utente. Quindi quando il permesso viene approvato, viene invocata una ricerca basata
     * sulla posizione.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
            mHelpLocalization.avviaRicercaPosizione();
    }


    /**
     * Metodo invocato durante la creazione del menù. Assegna alla voce di menù (la lente)
     * l'azione di ricerca tramite l'Api di terze parti "MaterialSearchView".
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        mSearchView.setMenuItem(searchItem);

        return true;
    }


    /*
     * Getter & setter per l'attributo fragmentParcheggi, utilizzato per memorizzare il fragment
     * dove viene mostrata la lista di parcheggi nelle vicinanze.
     */

    public ParcheggiFragment getFragmentAttivo() {
        return mFragmentAttivo;
    }

    public void setFragmentAttivo(ParcheggiFragment mFragmentAttivo) {
        this.mFragmentAttivo = mFragmentAttivo;
    }


    /*
     * Metodi per accedere alla progressBar, utilizzata come schermata di caricamento durante la
     * ricerca dei parcheggi.
     */

    public void showProgressBar() {
        mProgBar.setVisibility(View.VISIBLE);
    }

    public void hideProgressBar() {
        mProgBar.setVisibility(View.GONE);
    }


    /**
     * Metodo per accedere alla mappa mostrata nell'activity.
     */
    public MappaMain getMappa() {
        return mMappa;
    }
}