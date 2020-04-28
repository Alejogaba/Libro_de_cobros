package com.app.vefi;

import android.app.Activity;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;

import com.app.vefi.data.Adaptador.PrincipalAdaptador;
import com.app.vefi.data.Adaptador.RegistrosAdaptador;
import com.app.vefi.data.model.Person;
import com.app.vefi.data.model.Registro;
import com.app.vefi.ui.login.LoginActivity;
import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static com.app.vefi.ui.login.LoginActivity.lgnActivity;

public class PrincipalActivity extends AppCompatActivity {
    private FirebaseAuth mauth;
    private Firebase firebase;
    private ListView lista;
    private ListView listaDatos;
    private RecyclerView milistaDatos;
    private RecyclerView.Adapter madapter;
    private EditText persona;
    private Button btnBuscar;
    private RecyclerView.LayoutManager manager;
    private int contador;
    private boolean existe;
    private String url = "https://proyecto-cobros.firebaseio.com/users/";
    ArrayList<Person> personArrayList = new ArrayList<>();
    ArrayList<String> userArrayList = new ArrayList<>();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mauth = FirebaseAuth.getInstance();
        //FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        lgnActivity.finish();
        Firebase.setAndroidContext(this);
        firebase =  new Firebase(url);

        //generateDatabase();


        setContentView(R.layout.activity_principal);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        btnBuscar = findViewById(R.id.buttonSearchPrincipal);
        milistaDatos = (RecyclerView) findViewById(R.id.lista_personas_a_cobrar);
        milistaDatos.setHasFixedSize(true);
        manager = new LinearLayoutManager(this);
        milistaDatos.setLayoutManager(manager);

        persona = findViewById(R.id.add_persona);
        contador=0;
        persona.setVisibility(View.GONE);
        madapter = new PrincipalAdaptador(PrincipalActivity.this,getApplicationContext(),personArrayList);
        milistaDatos.setAdapter(madapter);
        milistaDatos.addItemDecoration(new DividerItemDecoration(PrincipalActivity.this, LinearLayout.VERTICAL));


        FirebaseUser user = mauth.getCurrentUser();

        firebase = new Firebase(url+user.getUid()+"/personas");

        firebase.orderByChild("nombre").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if (dataSnapshot != null && dataSnapshot.getValue() != null) {
                    try {

                        Person event = dataSnapshot.getValue(Person.class);
                        personArrayList.add(event);
                        milistaDatos.smoothScrollToPosition(personArrayList.size() - 1);
                        madapter.notifyItemInserted(personArrayList.size() - 1);

                    }
                    catch (Exception ex) {
                        Log.e("ERROR", ex.getMessage());
                    }
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                try {
                    Person event = dataSnapshot.getValue(Person.class);
                    for (int i = 0; i < personArrayList.size(); i++) {
                        if (personArrayList.get(i).getId().equals(event.getId())) {
                            personArrayList.remove(i);
                            madapter.notifyItemRemoved(i);
                            break;
                        }
                    }
                }
                catch (Exception ex) {
                    Log.e("ERROR", ex.getMessage());
                }

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                Toast.makeText(getApplicationContext(), "OnclickMoved", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                Toast.makeText(getApplicationContext(), "OnCancelled", Toast.LENGTH_SHORT).show();
            }
        });

        setSupportActionBar(toolbar);

        TextView.OnEditorActionListener miteclado;
        miteclado = new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                   addDato();
                }
                return false;
            }
        };
        persona.setOnEditorActionListener(miteclado);

        final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                contador++;
               if(contador==1){
                   fab.setImageResource(R.drawable.check);
                   mostrarEdiTextPersona();
                }else{
                   if(contador>1){
                       addDato();
                   }
                }


            }
        });

        btnBuscar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mostrarEdiTextPersona();

                persona.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        if(count!=0) {
                            buscar(s.toString());
                        }
                        else
                            buscar("");
                    }

                    @Override
                    public void afterTextChanged(Editable s) {

                    }
                });
            }
        });
    }

    public void mostrarEdiTextPersona(){
        persona.setVisibility(View.VISIBLE);
        persona.requestFocus();
        persona.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(persona, InputMethodManager.SHOW_IMPLICIT);
    }

    public void addDato(){
        contador=0;
        existe=false;
        String pushID= firebase.push().getKey();
        String nombre= persona.getText().toString()+"                                                             ";
        Person person = new Person(pushID,nombre);
        FirebaseUser user = mauth.getCurrentUser();
        firebase =  new Firebase(url+user.getUid());
        //comprobar(person.getNombre());
        if((userArrayList.contains(person.getNombre()))){
            existe=true;
        }
        if(existe){
            Toast.makeText(this, "Ya existe alguien con ese nombre", Toast.LENGTH_SHORT).show();
        }else{
            if(!persona.getText().toString().isEmpty()){
                firebase.child("personas").child(pushID).setValue(person);
            }
        }

        finishEdit();
    }

    @Override
    public void onBackPressed() {
        if(persona.getVisibility()==View.VISIBLE){
            finishEdit();
        }else{
            finish();
        }

    }


    public void LaunchLoginActivity(){
        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
        startActivity(intent);
    }


    public void finishEdit(){
            contador=0;
            FloatingActionButton fab = findViewById(R.id.fab);
            fab.setImageResource(R.drawable.plus);
            persona.getText().clear();
            persona.clearFocus();
            persona.setVisibility(View.GONE);
            hideKeyboard(PrincipalActivity.this);
    }

    public static void hideKeyboard(Activity activity) {
        View view = activity.findViewById(android.R.id.content);
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }



    public void buscar(final String palabra_clave){
        personArrayList.clear();
        madapter.notifyDataSetChanged();
        FirebaseUser user = mauth.getCurrentUser();

        firebase = new Firebase(url+user.getUid()+"/personas");

        firebase.orderByChild("nombre").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if (dataSnapshot != null && dataSnapshot.getValue() != null) {
                    try {

                        Person event = dataSnapshot.getValue(Person.class);
                        if(event.getNombre().contains(palabra_clave)){
                            personArrayList.add(event);
                            milistaDatos.scrollToPosition(personArrayList.size() - 1);
                            madapter.notifyItemInserted(personArrayList.size() - 1);
                        }


                    }
                    catch (Exception ex) {
                        Log.e("ERROR", ex.getMessage());
                    }
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                try {
                    Person event = dataSnapshot.getValue(Person.class);
                    for (int i = 0; i < personArrayList.size(); i++) {
                        if (personArrayList.get(i).getId().equals(event.getId())) {
                            personArrayList.remove(i);
                            madapter.notifyItemRemoved(i);
                            break;
                        }
                    }
                }
                catch (Exception ex) {
                    Log.e("ERROR", ex.getMessage());
                }

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                Toast.makeText(getApplicationContext(), "OnclickMoved", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                Toast.makeText(getApplicationContext(), "OnCancelled", Toast.LENGTH_SHORT).show();
            }
        });
    }



  /*  public void comprobar(String p){
        final String c = p;
        final FirebaseUser user = mauth.getCurrentUser();
        firebase =  new Firebase(url+user.getUid()+"/personas");
        final ArrayAdapter<String> arrayAdapter= new ArrayAdapter<String>(this,android.R.layout.simple_list_item_activated_1, userArrayList);
        lista.setAdapter(arrayAdapter);
        firebase.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                String data = dataSnapshot.getKey();
                //Person person = new Person(data);
                firebase =  new Firebase(url+user.getUid()+"/personas/"+data);
                firebase.addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        String dato = dataSnapshot.getValue().toString();
                        if(dato==c){
                            existe=true;
                        }
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
                    public void onCancelled(FirebaseError firebaseError) {

                    }
                });

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
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }



*/

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_principal,menu);
        return true;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Checks whether a hardware keyboard is available
        if (newConfig.keyboardHidden == Configuration.KEYBOARDHIDDEN_NO) {
            Toast.makeText(this, "keyboard visible", Toast.LENGTH_SHORT).show();
        } else if (newConfig.hardKeyboardHidden == Configuration.KEYBOARDHIDDEN_YES) {
            Toast.makeText(this, "keyboard NO visible", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        switch (id){
            case R.id.logout:
                mauth.signOut();
                LaunchLoginActivity();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void generateDatabase(){
        final FirebaseUser user = mauth.getCurrentUser();
        firebase.child(user.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue()==null) {
                    Firebase refFirebase = firebase.child(user.getUid());
                    refFirebase.setValue(user.getEmail());
                }

            }
            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }
}
