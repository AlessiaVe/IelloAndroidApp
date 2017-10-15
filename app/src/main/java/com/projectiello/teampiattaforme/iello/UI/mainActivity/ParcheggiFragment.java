package com.projectiello.teampiattaforme.iello.UI.mainActivity;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.github.aakira.expandablelayout.ExpandableRelativeLayout;
import com.google.android.gms.maps.model.LatLng;
import com.projectiello.teampiattaforme.iello.R;
import com.projectiello.teampiattaforme.iello.utilities.RecyclerItemClickListener;
import com.projectiello.teampiattaforme.iello.dataLogic.ElencoParcheggi;
import com.projectiello.teampiattaforme.iello.dataLogic.Parcheggio;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by riccardomaldini on 25/09/17.
 * Fragment che mostra la lista di parcheggi cercati, una volta disponibile.
 */

public class ParcheggiFragment extends Fragment {

    // componenti del fragment
    private Button mBtnEspandi;
    private ExpandableRelativeLayout mExpLayParcheggi;
    private Parcheggio mParkSelezionato;


    /**
     * Metodo statico per instanziare più facilmente il fragment quando necessario
     */
    public static void newInstance(MainActivity activity, LatLng coordinate) {
        ParcheggiFragment newFragment = new ParcheggiFragment();
        // Supply num input as an argument.
        Bundle args = new Bundle();
        args.putDouble("lat", coordinate.latitude);
        args.putDouble("lng", coordinate.longitude);
        newFragment.setArguments(args);


        FragmentTransaction ft = activity.getFragmentManager().beginTransaction();
        ft.replace(R.id.fragment_parcheggi_container, newFragment, "indirizzoParcheggio");
        //ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        ft.commit();
        activity.setFragmentAttivo(newFragment);
    }


    /**
     * Metodo statico per eliminare il fragment, quando non sono disponibili parcheggi
     */
    public static void clearFragment(MainActivity activity) {
        if(activity.getFragmentAttivo() != null) {
            ParcheggiFragment fragAttivo = activity.getFragmentAttivo();
            FragmentTransaction ft = activity.getFragmentManager().beginTransaction();
            ft.remove(fragAttivo);
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
            ft.commit();
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        double lat = getArguments().getDouble("lat");
        double lng = getArguments().getDouble("lng");
        LatLng coordinate = new LatLng(lat, lng);

        mParkSelezionato = ElencoParcheggi .getInstance().findParcheggioByCoordinate(coordinate);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_parcheggi, container, false);

        // 1. assegna i componenti del fragment
        TextView txtIndirizzoPP = view.findViewById(R.id.txtIndirizzo);
        TextView txtDistanzaPP = view.findViewById(R.id.txtDistanza);
        RelativeLayout layoutPP = view.findViewById(R.id.item_parcheggio);
        Button btnNavigaPP = view.findViewById(R.id.btnNaviga);

        mBtnEspandi = view.findViewById(R.id.btnEspandi);
        mExpLayParcheggi = view.findViewById(R.id.expRecParcheggi);
        final RecyclerView mRecParcheggi = view.findViewById(R.id.recParcheggi);


        // 2. inizializza i componenti del fragment

        // inizializza il primo elemento
        txtIndirizzoPP.setText(mParkSelezionato.getIndirizzoUI());
        txtDistanzaPP.setText(mParkSelezionato.getDistanzaUI());
        layoutPP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // sposta la mappa sulla posizione
                ((MainActivity) getActivity()).getMappa()
                        .muoviCamera(mParkSelezionato.getCoordinate());
            }
        });
        btnNavigaPP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                double lat = mParkSelezionato.getCoordinate().latitude;
                double lng = mParkSelezionato.getCoordinate().longitude;

                String uri = "http://maps.google.com/maps?daddr="+ lat +"," + lng;
                Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(uri));
                intent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
                startActivity(intent);
            }
        });


        // inizializza il RecyclerView
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        final ParcheggiAdapter pAdapter = new ParcheggiAdapter();
        mRecParcheggi.setLayoutManager(mLayoutManager);
        mRecParcheggi.setItemAnimator(new DefaultItemAnimator());
        mRecParcheggi.setAdapter(pAdapter);
        mRecParcheggi.addOnItemTouchListener(new RecyclerItemClickListener(getActivity(),
                mRecParcheggi, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                // sposta la mappa sulla posizione
                ((MainActivity) getActivity()).getMappa()
                        .muoviCamera(pAdapter.getListParcheggi().get(position).getCoordinate());
            }

            @Override
            public void onItemLongClick(View view, int position) {
            }
        }));

        // chiude le stats estese all'avvio (a causa di un bug dell'expandable API)
        mExpLayParcheggi.collapse();

        // inizializza il button di espansione
        mBtnEspandi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mExpLayParcheggi.isExpanded()) {
                    mBtnEspandi.setText(getString(R.string.tutti_i_risultati));
                    Drawable img = VectorDrawableCompat.create(getActivity().getResources(),
                                   R.drawable.ic_keyboard_arrow_up_black_24px, null);
                    mBtnEspandi.setCompoundDrawablesWithIntrinsicBounds(null, null, img, null);
                } else {
                    mBtnEspandi.setText(R.string.nascondi_risultati);
                    Drawable img = VectorDrawableCompat.create(getActivity().getResources(),
                                   R.drawable.ic_keyboard_arrow_down_black_24px, null);
                    mBtnEspandi.setCompoundDrawablesWithIntrinsicBounds(null, null, img, null);
                }
                mExpLayParcheggi.toggle();
            }
        });

        return view;
    }



    /** inner class dentro la quale viene definito un custom adapter per la recyclerView */
    private class ParcheggiAdapter extends RecyclerView.Adapter<ParcheggiAdapter.MyViewHolder> {

        private List<Parcheggio> mParcheggiRecycler = new ArrayList<>();

        private ParcheggiAdapter() {
            mParcheggiRecycler = ElencoParcheggi.getInstance().getListParcheggi();
        }

        class MyViewHolder extends RecyclerView.ViewHolder {

            // widget dell'interfaccia di ogni elemento
            TextView txtIndirizzo, txtDistanza;
            Button btnNaviga;


            MyViewHolder(View view) {
                super(view);
                txtIndirizzo = view.findViewById(R.id.txtIndirizzo);
                txtDistanza = view.findViewById(R.id.txtDistanza);
                btnNaviga = view.findViewById(R.id.btnNaviga);
            }
        }


        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.listitem_parcheggio, parent, false);
            return new MyViewHolder(itemView);
        }


        // mette i dati all'interno dello scheletro
        @Override
        public void onBindViewHolder(MyViewHolder holder, int position) {

            // variabile di appoggio che contiene i dati del parcheggio corrente
            final Parcheggio parcheggio = mParcheggiRecycler.get(position);

            // pone i dati all'interno dei widget
            holder.txtIndirizzo.setText(parcheggio.getIndirizzoUI());
            holder.txtDistanza.setText(parcheggio.getDistanzaUI());
            holder.btnNaviga.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    double lat = parcheggio.getCoordinate().latitude;
                    double lng = parcheggio.getCoordinate().longitude;

                    String uri = "http://maps.google.com/maps?daddr="+ lat +"," + lng;
                    Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(uri));
                    intent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
                    startActivity(intent);
                }
            });
        }


        List<Parcheggio> getListParcheggi() {
            return mParcheggiRecycler;
        }

        @Override
        public int getItemCount() {
            return mParcheggiRecycler.size();
        }
    }
}