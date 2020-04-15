package com.app.vefi;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.app.vefi.data.TemplatePDF;
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
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseError;

import java.sql.Date;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Currency;
import java.util.Locale;

import static com.app.vefi.ui.login.LoginActivity.lgnActivity;

public class DetallesPersonaActivity extends AppCompatActivity implements RegistrosAdaptador.MyHolder.OnNoteListener {
    Bundle datos;
    private FirebaseAuth mauth;
    private Firebase firebase;
    private TextView titulo;
    private TextView total;
    private TextView textoTotal;
    private TextView textoPazysalvo;
    private ImageView imagenPazysalvo;
    public static Button btnAbonar;
    private Button btnPdf;
    public static EditText valorAbonar;
    public static FloatingActionButton btnAdd;
    private ListView listaDatos;
    private ScrollView scrollViewRegistros;
    public String copyDatobtenido;
    private LinearLayout customSnackview;
    private RecyclerView milistaDatos;
    private RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager manager;
    private static String tag = "AVISO== ";
    private String datoOb;
    private String[] headers={"Dia","Descripcion","Valor"};
    ArrayList<Registro> registroArrayList = new ArrayList<>();
    ArrayList<Registro> listaAmostrar = new ArrayList<>();
    ArrayList<Registro> copiaregistroArrayList = new ArrayList<>();

    private String url = "https://proyecto-cobros.firebaseio.com/users/";
    @Override
    public void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
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
        textoTotal = findViewById(R.id.textoTotal);
        textoPazysalvo = findViewById(R.id.textViewPazySalvo);
        imagenPazysalvo = findViewById(R.id.imageViewPazySalvo);
        btnAdd = findViewById(R.id.buttonAdd);
        btnAbonar = findViewById(R.id.buttonAbonar);
        btnPdf = findViewById(R.id.buttonPdf);
        valorAbonar = findViewById(R.id.editTextValorAbonar);
        total = findViewById(R.id.textViewTotal);
        customSnackview = findViewById(R.id.linearLayoutCustom_Snackview);
        scrollViewRegistros = findViewById(R.id.scrollViewRegistros);
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

        firebase.child(datoObtenido).orderByChild("year_month_day").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if (dataSnapshot != null && dataSnapshot.getValue() != null) {
                    try {
                            Registro event = dataSnapshot.getValue(Registro.class);
                            registroArrayList.add(event);
                            milistaDatos.scrollToPosition(registroArrayList.size() - 1);
                            adapter.notifyItemInserted(registroArrayList.size() - 1);
                            total.setText(sumar());
                            listaEstavacia();
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
                        listaEstavacia();
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

        btnPdf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                generarPdf(registroArrayList);
            }
        });

        btnAbonar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                abonar(datoObtenido);
            }
        });

        milistaDatos.getRecycledViewPool().setMaxRecycledViews(0,0);

     /*  milistaDatos.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                switch(newState) {
                    case 0: // SCROLL_STATE_IDLE
                        final Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                customSnackview.setVisibility(View.VISIBLE);
                            }
                        }, 1300);
                        adapter.notifyDataSetChanged();
                        break;

                    case 1: // SCROLL_STATE_TOUCH_SCROLL
                        customSnackview.setVisibility(View.GONE);
                        break;

                    case 2: // SCROLL_STATE_FLING
                        customSnackview.setVisibility(View.GONE);
                        break;

                    default:
                        //show popup here
                        customSnackview.setVisibility(View.VISIBLE);
                        break;
                }
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
            }
        });
      */
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                listaEstavacia();
            }
        }, 1000);


    }

    public void listaEstavacia(){
        if(adapter.getItemCount()==0){
            textoPazysalvo.setText("¡Todo correcto!\nEste cliente no tiene deudas pendientes");
            imagenPazysalvo.setVisibility(View.VISIBLE);
            textoPazysalvo.setVisibility(View.VISIBLE);
        }else{
            imagenPazysalvo.setVisibility(View.GONE);
            textoPazysalvo.setVisibility(View.GONE);
        }
    }




    public void generarPdf(ArrayList<Registro> r2){
        listaAmostrar = r2;
        float total = 0;
        for(Registro p : registroArrayList){
            total += p.getValor();
        }
        Registro r = new Registro(0,"TOTAL",1,"abc",total,1,1);
        if(listaAmostrar.get(listaAmostrar.size()-1).getDay()!=0){
            listaAmostrar.add(r);
        }
        TemplatePDF templatePDF = new TemplatePDF(getApplicationContext());
        if(templatePDF.validarPermisos(this)){
            Calendar cal = Calendar.getInstance();
            int day = cal.get(Calendar.DAY_OF_MONTH);
            int month = cal.get(Calendar.MONTH);
            int year = cal.get(Calendar.YEAR);
            String fecha_actual = day+"/"+month+"/"+year;
            templatePDF.openDocument(this);
            templatePDF.addMetaData("Reporte","Ventas","alguien");
            templatePDF.addTitles( "Variedades Flor","Reporte",fecha_actual);
            templatePDF.createTable(headers,listaAmostrar,this);
            templatePDF.closeDocument();
            Toast.makeText(getApplicationContext(),"PDF Generado",Toast.LENGTH_SHORT).show();

            templatePDF.appViewPDF(this);
        }
    }

    public void abonar(final String datoObtenido){
        AlertDialog.Builder builder = new AlertDialog.Builder(DetallesPersonaActivity.this);

        LayoutInflater inflater = getLayoutInflater();

        View view = inflater.inflate(R.layout.abonar_dialog,null);

        builder.setView(view);

        final AlertDialog dialog = builder.create();
        dialog.show();

        final TextView valorDeuda = view.findViewById(R.id.textViewValorDeuda);
        final EditText valorAbono = view.findViewById(R.id.editTextValorAbonado);
        final Button btnAceptarAbono = view.findViewById(R.id.buttonAceptar_Abonar);
        final Button btnCancelarAbono = view.findViewById(R.id.buttonCancelar_Abonar);

        String msj = "Deuda total: "+sumar();
        valorDeuda.setText(msj);



        valorAbono.requestFocus();

        btnAceptarAbono.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try{
                    Calendar cal = Calendar.getInstance();
                    int day = cal.get(Calendar.DAY_OF_MONTH);
                    int month = cal.get(Calendar.MONTH);
                    int year = cal.get(Calendar.YEAR);

                    float total=0;
                    float valorAbonado = Float.parseFloat(valorAbono.getText().toString());
                    for(Registro p : registroArrayList){ // for each Player p in list
                        total += p.getValor();
                    }
                    firebase.child(datoObtenido).setValue(null);
                    if(valorAbonado<total){
                        float restante = total-valorAbonado;
                        addDato(day,month,year,"Saldo deuda anterior",restante,datoObtenido);
                    }else{
                        if(valorAbonado>total){
                            float restante = total-valorAbonado;
                            addDato(day,month,year,"Saldo a favor",restante,datoObtenido);
                        }
                    }
                    dialog.dismiss();
                }catch (Exception e){
                    Log.e("Aceptar-Abonno: ",e.toString());
                    dialog.dismiss();
                }
            }
        });

        btnCancelarAbono.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
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
        int fecha;
        if(month>=0&&month<10){
            if(day>=0&&day<10){
                fecha = Integer.parseInt( String.valueOf(year)+"0"+String.valueOf(month)+"0"+String.valueOf(day));
            }else{
                fecha = Integer.parseInt( String.valueOf(year)+"0"+String.valueOf(month)+String.valueOf(day));
            }
        }else{
            fecha = Integer.parseInt( String.valueOf(year)+String.valueOf(month)+String.valueOf(day));
        }

        Registro person = new Registro(day,descripcion,month,pushID,value,year,fecha);
        FirebaseUser user = mauth.getCurrentUser();
        firebase =  new Firebase(url+user.getUid());
        firebase.child("registros").child(persona).child(pushID).setValue(person);
        recreateActivity();
    }

    public void recreateActivity(){
        startActivity(getIntent());
        finish();
        overridePendingTransition(0,0);
    }

    public String getNombre(){
        TextView nombre = findViewById(R.id.textView_titulo);
        return nombre.getText().toString();
    }

    @SuppressLint("DefaultLocale")
    public String sumar(){
        float total = 0;
        Locale locale =getResources().getConfiguration().locale;
        Currency currency = Currency.getInstance(locale);
        String symbolEspacio = currency.getSymbol()+" ";
        NumberFormat col = NumberFormat.getCurrencyInstance(locale);
        for(Registro p : registroArrayList){ // for each Player p in list
            total += p.getValor();
        }

        if(total<0){
            textoTotal.setText("Restante:");
        }else{
            textoTotal.setText("Total:");
        }

        if(total == (int) total) {
            //return String.format("%d",(int)total);
            int valor = (int) total;
            if(total<0){
                return col.format(valor).replace(currency.getSymbol(),symbolEspacio);
            }else{
                return col.format(valor).replace(currency.getSymbol(),symbolEspacio);
            }
        }else
        if(total<0){
            return col.format(total).replace(currency.getSymbol(),symbolEspacio).replace("-","");
        }else{
            return col.format(total).replace(currency.getSymbol(),symbolEspacio);
        }
    }

    @Override
    public void onBackPressed() {
        valorAbonar.setVisibility(View.GONE);
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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode==100){
            if(grantResults.length==2&&grantResults[0]== PackageManager.PERMISSION_GRANTED&&grantResults[1]
                    ==PackageManager.PERMISSION_GRANTED){
                generarPdf(registroArrayList);
            }
        }
    }

    public void solicitarPermisosManual(){
        final CharSequence[] opciones ={"si","no"};

    }

    @Override
    public void OnNoteClick(int position) {
       Registro r = registroArrayList.get(position);
       String m = "MENSAJE: "+r.toString();
        Toast.makeText(this, m, Toast.LENGTH_SHORT).show();
    }

}
