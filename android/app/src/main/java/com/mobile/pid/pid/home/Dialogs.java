package com.mobile.pid.pid.home;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mobile.pid.pid.R;
import com.mobile.pid.pid.home.perfil.UsuarioPerfilActivity;
import com.mobile.pid.pid.home.turmas.Turma;
import com.mobile.pid.pid.login.Usuario;

public class Dialogs {

    public void dialogUsuario(final Usuario u, final Context context) {
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_perfil_usuario, null);
        final String usuario = FirebaseAuth.getInstance().getCurrentUser().getUid();

        final AlertDialog.Builder mBuilderUsuario = new AlertDialog.Builder(context);
        mBuilderUsuario.setView(view);

        final Button seguir = view.findViewById(R.id.seguir);
        final TextView nome = view.findViewById(R.id.nome);
        final ImageView foto = view.findViewById(R.id.foto);
        final TextView count_seguindo = view.findViewById(R.id.seguindo);
        final TextView count_seguidores = view.findViewById(R.id.seguidores);

        if(u.getUid().equals(usuario)) {
            seguir.setVisibility(View.GONE);
        } else {
            FirebaseDatabase.getInstance().getReference("userSeguindo").child(usuario).child(u.getUid())
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if(dataSnapshot.exists()) {
                                seguir.setText(context.getText(R.string.following));
                            } else {
                                seguir.setText(context.getText(R.string.follow));
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
        }

        final AlertDialog dialogUsuario = mBuilderUsuario.create();
        dialogUsuario.show();

        nome.setText(u.getNome());
        Glide.with(context)
                .load(u.getFotoUrl())
                .into(foto);

        FirebaseDatabase.getInstance().getReference("userSeguindo").child(u.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()) {
                            count_seguindo.setText(String.valueOf(dataSnapshot.getChildrenCount()));
                        } else {
                            count_seguindo.setText("0");
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

        FirebaseDatabase.getInstance().getReference("userSeguidores").child(u.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()) {
                            count_seguidores.setText(String.valueOf(dataSnapshot.getChildrenCount()));
                        } else {
                            count_seguidores.setText("0");
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

        seguir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final DatabaseReference dbSeguidores = FirebaseDatabase.getInstance().getReference("userSeguidores");
                final DatabaseReference dbSeguindo = FirebaseDatabase.getInstance().getReference("userSeguindo");
                final String usuario = FirebaseAuth.getInstance().getCurrentUser().getUid();

                if(seguir.getText().toString().equals(context.getText(R.string.following))) {
                    dialogUsuario.dismiss();

                    AlertDialog.Builder mBuilder = new AlertDialog.Builder(context);
                    mBuilder.setTitle(R.string.confirmacao)
                            .setMessage(context.getText(R.string.unfollow_message) + " "+ u.getNome() + "?")
                            .setPositiveButton(R.string.confirmar, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogUsuario.show();
                                    seguir.setText(R.string.follow);

                                    // EXCLUI NOS "SEGUINDO" DO USUARIO LOGADO
                                    dbSeguindo.child(usuario).child(u.getUid()).removeValue();
                                    // EXCLUI NOS "SEGUIDORES" DO USUARIO
                                    dbSeguidores.child(u.getUid()).child(usuario).removeValue();

                                    seguir.setText(context.getText(R.string.follow));
                                }
                            })
                            .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogUsuario.show();
                                }
                            });

                    AlertDialog dialog = mBuilder.create();
                    dialog.show();
                    dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(context.getResources().getColor(R.color.gray_font));
                } else {
                    seguir.setText(context.getText(R.string.following));

                    // ADICIONA NOS "SEGUINDO" DO USUARIO LOGADO
                    dbSeguindo.child(usuario).child(u.getUid()).setValue(true);
                    // ADICIONA NOS "SEGUIDORES" DO USUARIO
                    dbSeguidores.child(u.getUid()).child(usuario).setValue(true);
                }
            }
        });

        foto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(context, UsuarioPerfilActivity.class);
                i.putExtra("usuario", u.getUid());
                context.startActivity(i);
            }
        });
    }

    public void dialogTurma(final Turma t, final Context context)
    {
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_detalhe_turma, null);
        final String usuario = FirebaseAuth.getInstance().getCurrentUser().getUid();

        final AlertDialog.Builder mBuilderTurma = new AlertDialog.Builder(context);
        mBuilderTurma.setView(view);

        final Button solicitar = view.findViewById(R.id.solicitar);
        final TextView nome = view.findViewById(R.id.nome);
        final ImageView capa = view.findViewById(R.id.capa);
        final ImageView foto =  view.findViewById(R.id.foto);
        final TextView count_alunos = view.findViewById(R.id.count_alunos);

        final AlertDialog dialogTurma = mBuilderTurma.create();
        dialogTurma.show();

        nome.setText(t.getNome());
        Glide.with(context)
                .load(t.getCapaUrl())
                .into(capa);

        carregarFotoProfessor(foto, t);

        count_alunos.setText(String.valueOf(t.getQtdAlunos()));

        if(t.estaNaTurma(usuario))
            solicitar.setVisibility(View.GONE);
        else if(t.enviouSolicitacao(usuario))
            desabilitarBotaoSolicitar(solicitar);
        else
            solicitar.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                     AlertDialog.Builder mBuilder = new AlertDialog.Builder(context);
                     mBuilder.setTitle(R.string.warning)
                        .setMessage(R.string.deseja_solicitacao)
                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i)
                            {
                                enviarSolicitacaoTurma(t, usuario);

                                new AlertDialog.Builder(context)
                                        .setMessage(R.string.solicitacao_sucesso)
                                        .setPositiveButton(R.string.Ok, null)
                                        .show();

                                desabilitarBotaoSolicitar(solicitar);
                            }
                        })
                        .setNegativeButton(R.string.no, null);

                     AlertDialog dialog = mBuilder.create();
                     dialog.show();
                }
            });
    }

    private void carregarFotoProfessor(final ImageView foto, Turma t) {
        FirebaseDatabase.getInstance().getReference("usuarios").child(t.getProfessorUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Usuario u = dataSnapshot.getValue(Usuario.class);
                        Glide.with(foto).load(u.getFotoUrl()).into(foto);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    private void enviarSolicitacaoTurma(Turma t, String uid)
    {
        FirebaseDatabase.getInstance().getReference()
                .child("turmas")
                .child(t.getId())
                .child("solicitacoes")
                .child(uid).setValue(true);

        t.adicionarSolicitacao(uid);
    }

    private void desabilitarBotaoSolicitar(Button solicitar)
    {
        Context context = solicitar.getContext();

        solicitar.setText(context.getText(R.string.solicitado));
        solicitar.setClickable(false);
        solicitar.setTextColor(context.getResources().getColor(R.color.gray_font));
    }
}
