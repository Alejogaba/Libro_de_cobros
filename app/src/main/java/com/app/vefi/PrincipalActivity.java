package com.app.vefi;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;

import com.app.vefi.controlador.logica.GestionDeudor;
import com.app.vefi.controlador.logica.GestionProducto;
import com.app.vefi.controlador.logica.VozAtexto;
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
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.speech.RecognitionService;
import android.speech.RecognizerIntent;
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
import java.time.Month;
import java.time.Year;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static com.app.vefi.ui.login.LoginActivity.lgnActivity;

public class PrincipalActivity extends AppCompatActivity {
    private FirebaseAuth mauth;
    private Firebase firebase;
    private ListView lista;
    private ListView listaDatos;
    private RecyclerView milistaDatos;
    private RecyclerView.Adapter madapter;
    private FloatingActionButton btnGrabar;
    public EditText persona;
    private Button btnBuscar;
    private RecyclerView.LayoutManager manager;
    private int contador;
    private boolean existe;
    private static final int REQ_CODE_SPEECH_INPUT=100;
    private static String url = "https://proyecto-cobros.firebaseio.com/users/";
    public ArrayList<Person> personArrayList = new ArrayList<>();
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
        btnGrabar = findViewById(R.id.fab_grabar);

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

        btnGrabar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                iniciarEntradaVoz();
            }
        });

        btnBuscar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mostrarEdiTextPersona();
            }
        });

        persona.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(count>0)
                    buscar(s.toString());
                if(personArrayList.isEmpty()&&s.toString().length()>=2){
                    buscar(s.toString().substring(0,2));
                }

                else
                    buscar("");
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    public void mostrarEdiTextPersona(){
        persona.setVisibility(View.VISIBLE);
        persona.requestFocus();
        persona.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
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
        if(persona.getVisibility()==View.VISIBLE||!persona.getText().toString().matches("")){
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

    public void analiza_texto(String text, final Context context){

        text=text.toLowerCase();

        if(text.contains("anota")&&text.contains("por valor de")){
            String[] texto_partido=null;

            if(text.contains("anota a ")){
                texto_partido = text.split("anota a");
            }else{
                texto_partido = text.split("anota");
            }



            texto_partido = texto_partido[1].split("por valor de");


            texto_partido[0] = texto_partido[0].trim();

            float value;
            try{
                value = Float.parseFloat(texto_partido[1].trim());
            }catch(Exception e){
                value = 0;
            }

            final float valor = value;

            String[] nombre_descripcion = texto_partido[0]. split(" ", 2);

            String nombre = nombre_descripcion[0];

            GestionDeudor gestionDeudor = new GestionDeudor();

            final String uid = "";

            final String descripcion = nombre_descripcion[1];

            persona.setText(nombre);

            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    guardarDato(uid,descripcion,context,valor);
                }
            }, 1000);

        }else{
            Toast.makeText(context,"Intente de nuevo",Toast.LENGTH_LONG).show();
        }

    }

    public void guardarDato(String uid,String descripcion,Context context,float valor){
        try {
            uid = personArrayList.get(0).getId();
        }catch (Exception e){
            uid = "ERROR AL BUSCAR="+e.toString();
        }

        Calendar cal = Calendar.getInstance();
        int day = cal.get(Calendar.DAY_OF_MONTH);
        int month = cal.get(Calendar.MONTH);
        int year = cal.get(Calendar.YEAR);

        VozAtexto vozAtexto = new VozAtexto();

        String des;

        //Toast.makeText(context,uid,Toast.LENGTH_LONG).show();

            try {
                des=vozAtexto.analiza_descripcion(descripcion);

                GestionProducto gestionProducto = new GestionProducto(getApplicationContext());
                gestionProducto.addDato(day, month, year, des, valor, uid);

                Toast.makeText(context,"Registrado con exito",Toast.LENGTH_LONG).show();
            }catch (Exception e){
                Toast.makeText(context,"ERROR AL GUARDAR: "+e.toString(),Toast.LENGTH_LONG).show();
            }

    }

    public static void hideKeyboard(Activity activity) {
        View view = activity.findViewById(android.R.id.content);
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public void iniciarEntradaVoz(){
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        //intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,"es-MX");
        //intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,"Di el nombre del deudor y productos a añadir");

        try {
            startActivityForResult(intent,REQ_CODE_SPEECH_INPUT);
        }catch (ActivityNotFoundException e){
            Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode){
            case REQ_CODE_SPEECH_INPUT:{
                if (resultCode==RESULT_OK && null!=data){
                    ArrayList<String> result=data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    Toast.makeText(getApplicationContext(), result.get(0), Toast.LENGTH_SHORT).show();

                    final String nombre = result.get(0);
                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            VozAtexto vozAtexto = new VozAtexto();
                            try {
                                analiza_texto(nombre,getApplicationContext());
                            }catch (Exception e){
                                Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_LONG).show();
                            }

                        }
                    }, 1500);

                }
                break;
            }

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
                        if(event.getNombre().toLowerCase().contains(palabra_clave.toLowerCase())){
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
