package com.mobile.pid.pid.home.buscar;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.SearchView;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mobile.pid.pid.R;
import com.mobile.pid.pid.home.adapters.BuscarAdapter;
import com.mobile.pid.pid.home.adapters.SugestaoAdapter;
import com.mobile.pid.pid.home.adapters.TurmaAdapter;
import com.mobile.pid.pid.home.turmas.Turma;
import com.mobile.pid.pid.login.Usuario;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class BuscarFragment extends Fragment
{
    private static final String TAG = "BuscarFragment";

    private DatabaseReference turmasRef;
    private ChildEventListener turmasChildEventListener;

    private TurmaAdapter turmaAdapter;
    private BuscarAdapter buscarAdapter;
    private SugestaoAdapter sugestaoAdapter_usuarios;

    private RecyclerView recyclerView_usuarios;
    private RecyclerView recycle_busca;
    private Toolbar toolbar;
    private List<Turma> turmasCriadas;
    private FrameLayout sugestoes;
    private FrameLayout busca;

    // TODO: Código turmas criadas
    public BuscarFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

        buscarAdapter = new BuscarAdapter(getActivity());

        sugestaoAdapter_usuarios = new SugestaoAdapter(getActivity());

        FirebaseDatabase.getInstance().getReference("usuarios").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Usuario u = dataSnapshot.getValue(Usuario.class);
                u.setUid(dataSnapshot.getKey());
                sugestaoAdapter_usuarios.add(u);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_buscar, container, false);

        // Recycler View
        recyclerView_usuarios = v.findViewById(R.id.rv_usuarios);
        recycle_busca = v.findViewById(R.id.recycle_busca);
        //searchView = v.findViewById(R.id.search_view);

        LinearLayoutManager llm2 = new LinearLayoutManager(getActivity());
        llm2.setOrientation(LinearLayoutManager.HORIZONTAL);
        recyclerView_usuarios.setLayoutManager(llm2);
        recycle_busca.setLayoutManager(new LinearLayoutManager(getActivity()));

        toolbar = v.findViewById(R.id.toolbar_buscar);
        sugestoes = v.findViewById(R.id.sugestoes);
        busca = v.findViewById(R.id.busca);

        recyclerView_usuarios.setAdapter(sugestaoAdapter_usuarios);
        recycle_busca.setAdapter(buscarAdapter);

        if(getActivity() instanceof AppCompatActivity) {
            ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
            ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayShowHomeEnabled(false);
            ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        }


        return v;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_search, menu);

        super.onCreateOptionsMenu(menu, inflater);

        MenuItem item = menu.findItem(R.id.menuSearch);

        SearchView searchView = (SearchView) item.getActionView();

        //searchView.setMaxWidth(getActivity().getWindowManager().getDefaultDisplay().getWidth());
        
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {

                FirebaseDatabase.getInstance().getReference("usuarios").orderByChild("nome").startAt(query)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                buscarAdapter.clear();

                                if (dataSnapshot.exists()) {

                                    for (DataSnapshot user : dataSnapshot.getChildren()) {
                                        Usuario u = user.getValue(Usuario.class);
                                        u.setUid(user.getKey());
                                        buscarAdapter.add(u);
                                    }

                                    sugestoes.setVisibility(View.GONE);
                                    busca.setVisibility(View.VISIBLE);
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });

                FirebaseDatabase.getInstance().getReference("turmas").orderByChild("nome").startAt(query)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()) {

                                    for (DataSnapshot turma : dataSnapshot.getChildren()) {
                                        Turma t = turma.getValue(Turma.class);
                                        t.setId(turma.getKey());
                                        buscarAdapter.add(t);
                                    }

                                    sugestoes.setVisibility(View.GONE);
                                    busca.setVisibility(View.VISIBLE);
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });

                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                return true;
            }
        });

        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {

                buscarAdapter.clear();

                sugestoes.setVisibility(View.VISIBLE);
                busca.setVisibility(View.GONE);
                return false;
            }
        });
    }
}
