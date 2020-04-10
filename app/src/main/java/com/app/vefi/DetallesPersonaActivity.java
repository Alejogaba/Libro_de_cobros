package com.app.vefi;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.app.vefi.data.model.Registro;
import com.app.vefi.data.Adaptador.RegistrosAdaptador;
import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.MutableData;
import com.firebase.client.Query;
import com.firebase.client.Transaction;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseError;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import static com.app.vefi.ui.login.LoginActivity.lgnActivity;

public class DetallesPersonaActivity extends AppCompatActivity implements RegistrosAdaptador.MyHolder.OnNoteListener {
    Bundle datos;
    private FirebaseAuth mauth;
    private Firebase firebase;
    private TextView titulo;
    private TextView total;
    public static FloatingActionButton btnAdd;
    private ListView listaDatos;
    public String copyDatobtenido;
    private RecyclerView milistaDatos;
    private RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager manager;
    private static String tag = "AVISO== ";
    ArrayList<Registro> registroArrayList = new ArrayList<>();
    ArrayList<Registro> copiaregistroArrayList = new ArrayList<>();

    private String url = "https://proyecto-cobros.firebaseio.com/users/";
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalles_persona);

        mauth = FirebaseAuth.getInstance();
        lgnActivity.finish();
        Firebase.setAndroidContext(this);
        firebase =  new Firebase(url);

        datos= getIntent().getExtras();
        final String datoObtenido = datos.getString("uid");
        final String datoNombre = datos.getString("name");
        copyDatobtenido=datoObtenido;
        titulo = findViewById(R.id.textView_titulo);
        btnAdd = findViewById(R.id.buttonAdd);
        total = findViewById(R.id.textViewTotal);
        milistaDatos = findViewById(R.id.recyclerLlistaDatos);
        manager = new LinearLayoutManager(this);
        milistaDatos.setLayoutManager(manager);

        adapter = new RegistrosAdaptador(DetallesPersonaActivity.this,getApplicationContext(),registroArrayList,datoObtenido);
        milistaDatos.setAdapter(adapter);
        milistaDatos.addItemDecoration(new DividerItemDecoration(DetallesPersonaActivity.this, LinearLayout.VERTICAL));

        titulo.setText(datoNombre);

        final FirebaseUser user = mauth.getCurrentUser();
        Log.e(tag,"SE OBTIENE USUARIO");
        firebase =  new Firebase(url+user.getUid()+"/registros");
        Log.e(tag,"SE INSERTA URL");
        Log.e(tag,"SE DECLARA ARAYADAPTER");
        Log.e(tag,"SET ADAPATER");

        firebase.child(datoObtenido).orderByChild("month").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if (dataSnapshot != null && dataSnapshot.getValue() != null) {
                    try {

                            Registro event = dataSnapshot.getValue(Registro.class);
                            registroArrayList.add(event);
                            milistaDatos.scrollToPosition(registroArrayList.size() - 1);
                            adapter.notifyItemInserted(registroArrayList.size() - 1);
                            total.setText(sumar());
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
                        Registro event = dataSnapshot.getValue(Registro.class);
                        for (int i = 0; i < registroArrayList.size(); i++) {
                            if (registroArrayList.get(i).getRegistroId().equals(event.getRegistroId())) {
                                registroArrayList.remove(i);
                                adapter.notifyItemRemoved(i);
                                break;
                            }
                        }
                        total.setText(sumar());
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



        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                abrirDialog(datoObtenido);
            }
        });
    }






    public void abrirDialog(final String obtenido){
        AlertDialog.Builder builder = new AlertDialog.Builder(DetallesPersonaActivity.this);

        LayoutInflater inflater = getLayoutInflater();

        View view = inflater.inflate(R.layout.retrieve_data_dialog,null);

        builder.setView(view);

        final AlertDialog dialog = builder.create();
        dialog.show();

        final DatePicker fecha = view.findViewById(R.id.calendarFecha);
        final EditText descripcion = view.findViewById(R.id.editTextDescripcion);
        final EditText valor = view.findViewById(R.id.editTextValor);
        descripcion.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(descripcion, InputMethodManager.SHOW_IMPLICIT);
        descripcion.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES|InputType.TYPE_TEXT_FLAG_MULTI_LINE);

        long now = System.currentTimeMillis() - 1000;
        fecha.setMaxDate(now);
        final Button btnAceptar = view.findViewById(R.id.buttonAceptar);
        final Button btnCancelar = view.findViewById(R.id.buttonCancelar);

        TextView.OnEditorActionListener miteclado;
        miteclado = new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    if(descripcion.hasFocus()){
                        valor.requestFocus();
                    }else{
                        valor.clearFocus();
                        btnAceptar.callOnClick();
                    }

                }
                return false;
            }
        };

        btnAceptar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int year;
                int month;
                int day;
                int count=10;
                String des;
                float val;



                try {
                    des = descripcion.getText().toString();
                    val = Float.parseFloat(valor.getText().toString());
                    year = fecha.getYear();
                    month = fecha.getMonth();
                    day = fecha.getDayOfMonth();

                    addDato(day,month,year,des,val,obtenido);
                }catch (Exception r){
                    String m= r.getMessage();
                    Toast.makeText(getApplicationContext(),m, Toast.LENGTH_LONG).show();
                    des = "";
                    val = 0;
                    year = fecha.getYear();
                    month = fecha.getMonth();
                    day = fecha.getDayOfMonth();

                    addDato(day,month,year,des,val,obtenido);
                }

                dialog.dismiss();
            }
        });

        btnCancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }

    public int safeParseInt(String number) throws Exception {
        if(number != null) {
            return Integer.parseInt(number.trim());
        } else {
            throw new NullPointerException("Date string is invalid");
        }
    }

    public void addDato(int day,int month,int year,String descripcion,float value,String persona){
        String pushID= firebase.push().getKey();
        Registro person = new Registro(day,descripcion,month,pushID,value,year);
        FirebaseUser user = mauth.getCurrentUser();
        firebase =  new Firebase(url+user.getUid());
        firebase.child("registros").child(persona).child(pushID).setValue(person);
    }

    public String getNombre(){
        TextView nombre = findViewById(R.id.textView_titulo);
        return nombre.getText().toString();
    }

    @SuppressLint("DefaultLocale")
    public String sumar(){
        int total = 0;
        for(Registro p : registroArrayList){ // for each Player p in list
            total += p.getValor();
        }
        if(total == (int) total)
            return String.format("%d",(int)total);
        else
            return String.format("%s",total);
    }

    @Override
    public void onBackPressed() {
        if(RegistrosAdaptador.visibleDelete){
            btnAdd.setImageResource(R.drawable.plus);
            RegistrosAdaptador.visibleDelete=false;
            adapter.notifyDataSetChanged();
            btnAdd.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                        abrirDialog(copyDatobtenido);
                }
            });
        }else{
            super.onBackPressed();
        }

    }

    @Override
    public void OnNoteClick(int position) {
       Registro r = registroArrayList.get(position);
       String m = "MENSAJE: "+r.toString();
        Toast.makeText(this, m, Toast.LENGTH_SHORT).show();
    }

}
