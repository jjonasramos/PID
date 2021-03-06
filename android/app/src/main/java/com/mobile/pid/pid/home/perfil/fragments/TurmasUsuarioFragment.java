package com.mobile.pid.pid.home.perfil.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mobile.pid.pid.R;
import com.mobile.pid.pid.home.adapters.TurmaAdapter;
import com.mobile.pid.pid.classes_e_interfaces.Turma;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class TurmasUsuarioFragment extends Fragment
{
    private ProgressBar progressBar;
    private FrameLayout conteudo;
    private LinearLayout mensagemSemTurma;

    private DatabaseReference turmasUsuarioRef;

    private TurmaAdapter turmaAdapter;
    private RecyclerView recyclerView;
    private List<Turma> turmasUsuario;

    private ValueEventListener turmasListener;

    public TurmasUsuarioFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        turmasUsuario = new ArrayList<>();
        turmaAdapter = new TurmaAdapter(getActivity(), turmasUsuario);

        final String uid = getArguments().getString("usuario"); //FirebaseAuth.getInstance().getCurrentUser().getUid();

        turmasUsuarioRef = FirebaseDatabase.getInstance().getReference().child("userTurmasCriadas").child(uid);

        turmasListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists())
                {
                    recyclerView.getRecycledViewPool().clear();
                    turmaAdapter.clear();

                    for(DataSnapshot dataTurma : dataSnapshot.getChildren())
                    {
                        String tuid = dataTurma.getKey();
                        FirebaseDatabase.getInstance().getReference()
                                .child("turmas")
                                .child(tuid)
                                .addListenerForSingleValueEvent(new ValueEventListener()
                                {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshotTurma)
                                    {
                                        Turma t = dataSnapshotTurma.getValue(Turma.class);
                                        t.setId(dataSnapshotTurma.getKey());

                                        turmaAdapter.add(t);
                                        turmaAdapter.notifyDataSetChanged();
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {}
                                });
                    }

                    mensagemSemTurma.setVisibility(View.GONE);
                }
                else
                    mensagemSemTurma.setVisibility(View.VISIBLE);

                progressBar.setVisibility(View.GONE);
                conteudo.setVisibility(View.VISIBLE);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
    }

    @Override
    public void onStart() {
        super.onStart();
        turmasUsuarioRef.addValueEventListener(turmasListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        turmasUsuarioRef.removeEventListener(turmasListener);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View v = inflater.inflate(R.layout.fragment_turmas_usuario, container, false);

        mensagemSemTurma = v.findViewById(R.id.ll_mensagem_sem_turmas_usuario);
        recyclerView     = v.findViewById(R.id.rv_turmas_usuario);
        progressBar      = v.findViewById(R.id.pb_turmas_usuario);
        conteudo         = v.findViewById(R.id.fl_turmas_usuario);
        conteudo.setVisibility(View.GONE);

        // Recycler View
        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(llm);

        recyclerView.setAdapter(turmaAdapter);

        return v;
    }
}