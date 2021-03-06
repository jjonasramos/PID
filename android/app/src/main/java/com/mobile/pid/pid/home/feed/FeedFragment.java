package com.mobile.pid.pid.home.feed;


import android.app.AlertDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.mobile.pid.pid.R;
import com.mobile.pid.pid.home.adapters.PostAdapter;
import com.mobile.pid.pid.classes_e_interfaces.Post;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */

// https://www.youtube.com/watch?v=4mwnhvRzRfw LINK PRA USAR RECYCLER/CARD/FRAGMENT
public class FeedFragment extends Fragment {

    private List<Post> posts;
    private FloatingActionButton criarPost;
    private TextView countChars;
    private EditText edit_post;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private FrameLayout conteudo;

    private String usuario;
    private LinearLayout mensagemSemPosts;

    private PostAdapter postAdapter;

    private ValueEventListener postListener;
    private Query postQuery;

    public FeedFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_feed, container, false);

        mensagemSemPosts = view.findViewById(R.id.ll_mensagem_feed_sem_posts);
        recyclerView = view.findViewById(R.id.recycler_view);
        criarPost    = view.findViewById(R.id.fab_add_post);
        progressBar  = view.findViewById(R.id.pb_feed);
        conteudo     = view.findViewById(R.id.fl_feed);

        conteudo.setVisibility(View.GONE);

        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        mLayoutManager.setReverseLayout(true);
        mLayoutManager.setStackFromEnd(true);

        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setAdapter(postAdapter);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener()
        {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy)
            {
                super.onScrolled(recyclerView, dx, dy);

                if(dy < 0 && !criarPost.isShown())
                    criarPost.show();
                else if(dy > 0 && criarPost.isShown())
                    criarPost.hide();
            }
        });

        criarPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                criarPost(view);
            }
        });

        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        posts = new ArrayList<>();
        postAdapter = new PostAdapter(getActivity(), posts);
        usuario = FirebaseAuth.getInstance().getCurrentUser().getUid();

        postQuery = FirebaseDatabase.getInstance().getReference("feed").child(usuario).orderByChild("postData");

        postListener = new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.exists())
                {
                    postAdapter.clear();

                    for(DataSnapshot data : dataSnapshot.getChildren())
                    {
                        Post p = data.getValue(Post.class);
                        p.setId(data.getKey());
                        postAdapter.add(p);
                    }

                    mensagemSemPosts.setVisibility(View.GONE);
                }
                else
                    mensagemSemPosts.setVisibility(View.VISIBLE);


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
        postQuery.addValueEventListener(postListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        postQuery.removeEventListener(postListener);
    }

    @Override
    public void onPause() {
        super.onPause();
        //postQuery.removeEventListener(postListener);
    }

    @Override
    public void onResume() {
        super.onResume();
        //postQuery.addValueEventListener(postListener);
    }

    private void criarPost(View view)
    {
        final View mView = getLayoutInflater().inflate(R.layout.dialog_criar_post, null);
        final AlertDialog dialog = new AlertDialog.Builder(getContext(), R.style.DialogTheme)
                .setView(mView)
                .setPositiveButton(R.string.postar, null)
                .setNegativeButton(R.string.cancel, null)
                .create();

        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.gray_font));
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.gray_font));

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Post post = new Post(null, usuario, edit_post.getText().toString().trim());
                FeedFunctions.criarPost(post);
                dialog.dismiss();
            }
        });

        countChars = mView.findViewById(R.id.count_chars);
        edit_post = mView.findViewById(R.id.edit_post);

        final TextWatcher textWatcher = new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence s, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence s, int i, int i1, int i2)
            {
                if(s.length() <= 200 && s.length() > 0) {
                    countChars.setTextColor(getResources().getColor(R.color.gray_font));
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.white));
                }
                else {
                    countChars.setTextColor(getResources().getColor(R.color.colorAccent));
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.gray_font));
                }

                countChars.setText(String.valueOf(s.length()) + "/" + getText(R.string.limit_chars_post));
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }

        };

        edit_post.addTextChangedListener(textWatcher);
    }
}
