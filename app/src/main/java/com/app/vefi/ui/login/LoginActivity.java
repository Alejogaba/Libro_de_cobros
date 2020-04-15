package com.app.vefi.ui.login;

import android.app.Activity;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.multidex.MultiDex;

import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.app.vefi.PrincipalActivity;
import com.app.vefi.R;

import com.firebase.client.Firebase;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthEmailException;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;
import com.itextpdf.text.BaseColor;

public class LoginActivity extends AppCompatActivity {

    private LoginViewModel loginViewModel;
    private FirebaseAuth mauth;
    private FirebaseAuth.AuthStateListener mauthStateListener;
    private ProgressBar progressBarLogin;
    private Button loginButton;
    private EditText usernameEditText;
    private EditText passwordEditText;
    private ConstraintLayout contenedorPrincipal;
    private static String tag = "TAG";
    private Firebase firebase;
    public static Activity lgnActivity;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        Log.v(tag,"Se inicia LoginActivty");
        mauth = FirebaseAuth.getInstance();
        lgnActivity=this;
        IsLogged();
        Log.v(tag,"Se asigna contexto a Fireebase");

        setContentView(R.layout.activity_login);
        loginViewModel = ViewModelProviders.of(this, new LoginViewModelFactory())
                .get(LoginViewModel.class);

        MultiDex.install(this);
        progressBarLogin = findViewById(R.id.progressBarLogin);
        contenedorPrincipal = findViewById(R.id.contenedorPrincipalLogin);
        usernameEditText = findViewById(R.id.username);
        passwordEditText = findViewById(R.id.password);
        loginButton = findViewById(R.id.login);
        final ProgressBar loadingProgressBar = findViewById(R.id.loading);


        mauthStateListener = new FirebaseAuth.AuthStateListener(){
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if(user!=null){
                    Log.d(tag,"Esta logeado: "+user.getUid());
                }else{
                    Log.d(tag,"No esta logeado");
                }
            }
        };

        loginViewModel.getLoginFormState().observe(this, new Observer<LoginFormState>() {
            @Override
            public void onChanged(@Nullable LoginFormState loginFormState) {
                if (loginFormState == null) {
                    return;
                }
                loginButton.setEnabled(loginFormState.isDataValid());
                if (loginFormState.getUsernameError() != null) {
                    usernameEditText.setError(getString(loginFormState.getUsernameError()));
                }
                if (loginFormState.getPasswordError() != null) {
                    passwordEditText.setError(getString(loginFormState.getPasswordError()));
                }
            }
        });

        loginViewModel.getLoginResult().observe(this, new Observer<LoginResult>() {
            @Override
            public void onChanged(@Nullable LoginResult loginResult) {
                if (loginResult == null) {
                    return;
                }
                loadingProgressBar.setVisibility(View.GONE);
                if (loginResult.getError() != null) {
                    showLoginFailed(loginResult.getError());
                }
                if (loginResult.getSuccess() != null) {
                    updateUiWithUser(loginResult.getSuccess());
                }
                setResult(Activity.RESULT_OK);

                //Complete and destroy login activity once successful

            }
        });

        TextWatcher afterTextChangedListener = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // ignore
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // ignore
            }

            @Override
            public void afterTextChanged(Editable s) {
                loginViewModel.loginDataChanged(usernameEditText.getText().toString(),
                        passwordEditText.getText().toString());
            }
        };
        usernameEditText.addTextChangedListener(afterTextChangedListener);
        passwordEditText.addTextChangedListener(afterTextChangedListener);
        passwordEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    loginViewModel.login(usernameEditText.getText().toString(),
                            passwordEditText.getText().toString());
                }
                return false;
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //loadingProgressBar.setVisibility(View.VISIBLE);
                //loginViewModel.login(usernameEditText.getText().toString(),
                  //      passwordEditText.getText().toString());
                SignIn(usernameEditText.getText().toString(),
                        passwordEditText.getText().toString());

            }
        });
    }

    private void IsLogged() {
        FirebaseUser user = mauth.getCurrentUser();
        if(user!=null){
            LaunchActivity();

        }
    }

    public void cargando(){
        progressBarLogin.setVisibility(View.VISIBLE);
        loginButton.setEnabled(false);
        loginButton.setTextColor(Color.GRAY);
        usernameEditText.setEnabled(false);
        passwordEditText.setEnabled(false);
    }

    public void detenercarga(){
        progressBarLogin.setVisibility(View.GONE);
        loginButton.setEnabled(true);
        loginButton.setTextColor(Color.BLACK);
        usernameEditText.setEnabled(true);
        passwordEditText.setEnabled(true);
    }

    private void createuser(final String username, final String password){
        final String mensaje,email,mpassword,mensaje_exito,mensaje_fallo;
        mensaje_exito="Se creo exitosamente el usuario";
        mensaje_fallo="Fallo al crear el usuario";
        mensaje="Datos incorrectos";
            email= username;
            mpassword= password;
            mauth.createUserWithEmailAndPassword(email,mpassword).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()){
                        Toast.makeText(getApplicationContext(),mensaje_exito,Toast.LENGTH_SHORT).show();
                        SignIn(username,password);
                    }else{
                        Toast.makeText(getApplicationContext(),mensaje_fallo,Toast.LENGTH_SHORT).show();
                    }
                }
            });
    }



    private void SignIn(final String username, final String password){
        cargando();
        String mensaje,email,mpassword;
        final String mensaje_exito= "Inicio de sesion correcto";
        final String mensaje_fallo= "No se pudo iniciar sesion";
        mensaje="Datos incorrectos";
            email= username;
            mpassword= password;
            mauth.signInWithEmailAndPassword(email,mpassword).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()){
                        Toast.makeText(LoginActivity.this,mensaje_exito,Toast.LENGTH_SHORT).show();
                        LaunchActivity();
                    }else{
                        detenercarga();
                        Toast.makeText(LoginActivity.this,mensaje_fallo,Toast.LENGTH_SHORT).show();
                    }

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    detenercarga();
                    if(e instanceof FirebaseAuthInvalidCredentialsException){
                        Onerror("Contraseña invalida");
                    }else{
                        if(e instanceof FirebaseAuthInvalidUserException){
                           createuser(username,password);
                        }else{
                            Onerror(e.getLocalizedMessage());
                        }

                    }

                }
            });


    }


    private void Onerror(String e){
        detenercarga();
        Toast.makeText(LoginActivity.this,"Error: "+e,Toast.LENGTH_SHORT).show();
    }

    private void LaunchActivity(){
        Intent intent = new Intent(getApplicationContext(), PrincipalActivity.class);
        startActivity(intent);

    }

    private void updateUiWithUser(LoggedInUserView model) {
        String welcome = getString(R.string.welcome) + model.getDisplayName();
        // TODO : initiate successful logged in experience
        Toast.makeText(getApplicationContext(), welcome, Toast.LENGTH_LONG).show();
    }

    private void showLoginFailed(@StringRes Integer errorString) {
        Toast.makeText(getApplicationContext(), errorString, Toast.LENGTH_SHORT).show();
    }
}
