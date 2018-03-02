package com.mobile.pid.pid.login;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.mobile.pid.pid.R;

import java.util.Arrays;

public class RedesSociaisActivity extends AppCompatActivity
{
    private static final String TAG = "RedesSociaisActivity";

    private static final int RC_GOOGLE_SIGN_IN   = 1;

    private GoogleApiClient googleApiClient;
    private CallbackManager facebookCallbackManager;

    // Firebase
    private FirebaseAuth firebaseAuth;
    private FirebaseDatabase firebaseDatabase;

    // Listeners
    private LoginAuthStateListener  authStateListener;
    private LoginOnCompleteListener loginOnCompleteListener;

    private ImageView btn_email;
    private TextView tv_criarConta;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_redes_sociais);

        firebaseAuth           = FirebaseAuth.getInstance();
        firebaseDatabase       = FirebaseDatabase.getInstance();

        loginOnCompleteListener = new LoginOnCompleteListener(this);
        authStateListener       = new LoginAuthStateListener(this);

        // Google Sign In - https://www.youtube.com/watch?v=-ywVw2O1pP8
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        googleApiClient = new GoogleApiClient.Builder(getApplicationContext())
            .enableAutoManage(this, new GoogleApiClient.OnConnectionFailedListener()
            {
                @Override
                public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                    Toast.makeText(RedesSociaisActivity.this, "Erro ao conectar com Google API Client", Toast.LENGTH_SHORT).show();
                }
            })
            .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
            .build();

        // Facebook - Facebook Quick Start e https://www.youtube.com/watch?v=mapLbSKNc6I
        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);

        facebookCallbackManager = CallbackManager.Factory.create();
        LoginManager.getInstance().registerCallback(facebookCallbackManager,
            new FacebookCallback<LoginResult>()
            {
                @Override
                public void onSuccess(LoginResult loginResult)
                {
                    Log.d(TAG, "facebook:onSuccess:" + loginResult);
                    firebaseAuthWithFacebook(loginResult.getAccessToken());
                }

                @Override
                public void onCancel() {
                    Log.d(TAG, "facebook:onCancel");
                }

                @Override
                public void onError(FacebookException error) {
                    Log.d(TAG, "facebook:onError", error);
                }
            });

        // LOGAR POR EMAIL
        /*btn_email.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(RedesSociaisActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });

        // CRIAR UMA NOVA CONTA
        tv_criarConta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                AlertDialog.Builder mBuilder = new AlertDialog.Builder(RedesSociaisActivity.this);
                View mView = getLayoutInflater().inflate(R.layout.dialog_criar_conta, null);

                final TextInputLayout TIL_dialog_username = (TextInputLayout) mView.findViewById(R.id.TIL_dialog_username);
                final TextInputLayout TIL_dialog_email = (TextInputLayout) mView.findViewById(R.id.TIL_dialog_email);
                final TextInputLayout TIL_dialog_password = (TextInputLayout) mView.findViewById(R.id.TIL_dialog_password);
                final TextInputLayout TIL_dialog_password_confirm = (TextInputLayout) mView.findViewById(R.id.TIL_dialog_password_confirm);
                Button btn_create_account = (Button) mView.findViewById(R.id.btn_create_account);
                Button btn_cancel_account = (Button) mView.findViewById(R.id.btn_cancel_account);

                btn_create_account.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        if(!TIL_dialog_email.getEditText().getText().toString().isEmpty()) {
                            Toast.makeText(RedesSociaisActivity.this, R.string.success_create_login, Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(RedesSociaisActivity.this, R.string.error_create_login, Toast.LENGTH_SHORT).show();
                        }
                    }
                });


                mBuilder.setView(mView);
                final AlertDialog dialog = mBuilder.create();
                dialog.show();

                btn_cancel_account.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.cancel();
                    }
                });
            }
        });*/
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        firebaseAuth.addAuthStateListener(authStateListener);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_GOOGLE_SIGN_IN)
        {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);

            if (result.isSuccess())
            {
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);
            }
            else
            {
                Log.e(TAG, "GOOGLE_SIGN_IN ERROR: " + result.getStatus());
                Toast.makeText(RedesSociaisActivity.this, "Erro ao logar com sua conta do Google. Tente novamente mais tarde.", Toast.LENGTH_LONG).show();
            }
        }
        else if (requestCode == FacebookSdk.getCallbackRequestCodeOffset())
        {
            facebookCallbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }

    public void dialogCriarConta(View v)
    {
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(RedesSociaisActivity.this);
        View mView = getLayoutInflater().inflate(R.layout.dialog_criar_conta, null);

        /*final TextInputLayout TIL_dialog_username = mView.findViewById(R.id.TIL_dialog_username);
        final TextInputLayout TIL_dialog_email = mView.findViewById(R.id.TIL_dialog_email);
        final TextInputLayout TIL_dialog_password = mView.findViewById(R.id.TIL_dialog_password);
        final TextInputLayout TIL_dialog_password_confirm =  mView.findViewById(R.id.TIL_dialog_password_confirm);*/

        final EditText dialog_username         = mView.findViewById(R.id.dialog_username);
        final EditText dialog_email            = mView.findViewById(R.id.dialog_email);
        final EditText dialog_password         = mView.findViewById(R.id.dialog_password);
        final EditText dialog_password_confirm = mView.findViewById(R.id.dialog_password_confirm);

        Button btn_create_account = (Button) mView.findViewById(R.id.btn_create_account);
        Button btn_cancel_account = (Button) mView.findViewById(R.id.btn_cancel_account);

        btn_create_account.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                String username = dialog_username.getText().toString();
                String email    = dialog_email.getText().toString();
                String senha    = dialog_password.getText().toString();
                String senhaC   = dialog_password_confirm.getText().toString();

                Log.d(TAG, senha);

                if(validarCamposCriarConta(username, email, senha, senhaC))
                {
                    firebaseAuth.createUserWithEmailAndPassword(email, senha).addOnCompleteListener(new OnCompleteListener<AuthResult>()
                    {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task)
                        {
                            if (task.isSuccessful())
                            {
                                Toast.makeText(RedesSociaisActivity.this, R.string.success_create_login, Toast.LENGTH_SHORT).show();
                                FirebaseUser user = firebaseAuth.getCurrentUser();

                                // TODO: Manda pra uma activity pra avisar ele a completar o perfil
                            }
                            else
                            {
                                Log.e(TAG, task.getException().toString());
                                Toast.makeText(RedesSociaisActivity.this, "Aconteceu um erro", Toast.LENGTH_SHORT).show();
                            }

                        }
                    });

                }
                else
                    Toast.makeText(RedesSociaisActivity.this, R.string.error_create_login, Toast.LENGTH_SHORT).show();

            }
        });


        mBuilder.setView(mView);
        final AlertDialog dialog = mBuilder.create();
        dialog.show();

        btn_cancel_account.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.cancel();
            }
        });
    }

    // TODO: Validação
    private boolean validarCamposCriarConta(String username, String email, String senha, String senhaC)
    {
        /*if (username == null || email == null || senha == null || senhaC == null)
            return false;

        if (username.isEmpty() || email.isEmpty() || senha.isEmpty() || senhaC.isEmpty())
            return false;

        if (!senha.equals(senhaC))
            return false;*/

        return true;
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount account)
    {
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, loginOnCompleteListener);
    }

    private void firebaseAuthWithFacebook(AccessToken token)
    {
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, loginOnCompleteListener);
    }

    public void entrarGoogle(View v)
    {
        Intent googleSignInIntent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
        startActivityForResult(googleSignInIntent, RC_GOOGLE_SIGN_IN);
    }

    public void entrarFacebook(View v) {
        LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("email", "public_profile"));
    }

    public void entrarEmail(View v) {
        startActivity(new Intent(this, LoginActivity.class));
    }
}