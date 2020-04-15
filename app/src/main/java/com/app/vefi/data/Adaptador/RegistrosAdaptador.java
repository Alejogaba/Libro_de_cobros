package com.app.vefi.data.Adaptador;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.app.vefi.DetallesPersonaActivity;
import com.app.vefi.R;
import com.app.vefi.data.model.Registro;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.sql.Array;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Currency;
import java.util.EventListener;
import java.util.List;
import java.util.Locale;

public class RegistrosAdaptador extends RecyclerView.Adapter {
    private Context context;
    private FirebaseAuth mauth;
    private ArrayList<Registro> registroArrayList;
    private String url = "https://proyecto-cobros.firebaseio.com/users/";
    private Firebase firebase;
    private String persona;
    private Activity mactivity;
    public static boolean visibleDelete;
    private FloatingActionButton myflaotingbtn;
    private boolean[] mes_marcado = new boolean[11];
    private String[] mes_ocupado = new String[11];
    private boolean[] mes_visible = new boolean[11];
    private float total;
    private float abonado;
    private boolean modificar;
    private boolean marzo= false;
    private ArrayList<Registro> listaSeleccionados=new ArrayList<>();
    private boolean isLongPress;
    Runnable mRunnable,timeRunnable;
    Handler mHandler=new Handler();


    public RegistrosAdaptador(Activity mactivity,Context context, ArrayList<Registro> registroArrayList,String persona) {
        this.context = context;
        this.mactivity = mactivity;
        this.registroArrayList = registroArrayList;
        this.persona = persona;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View conttentView = LayoutInflater.from(context).inflate(R.layout.item_list,null);
        visibleDelete=false;

        Arrays.fill(mes_visible,true);

        return new MyHolder(conttentView);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
        final Registro registro = registroArrayList.get(position);
        final MyHolder holderl = (MyHolder) holder;
        mauth = FirebaseAuth.getInstance();
        myflaotingbtn = mactivity.findViewById(R.id.buttonAdd);
        holderl.dia.setText(String.valueOf(registro.getDay()));
        holderl.descripcion.setText(registro.getDescripcion());
        holderl.valor.setText(convertirMoneda(registro.getValor()));

        if(registro.getValor()<0){
            holderl.valor.setTextColor(mactivity.getResources().getColor(R.color.colorAccent));
            holderl.descripcion.setTextColor(mactivity.getResources().getColor(R.color.colorAccent));
            holderl.dia.setTextColor(mactivity.getResources().getColor(R.color.colorAccent));
        }

        if(registro.getDay()==nMenor(registro.getMonth())){
            if(position>0){
                final Registro registro2 = registroArrayList.get(position-1);
                if(registro2.getDay()!=registro.getDay()){
                    holderl.mes.setVisibility(View.VISIBLE);
                    switch (registro.getMonth()){
                        case 0:
                            holderl.mes.setText(R.string.month_1);
                            break;
                        case 1:
                            holderl.mes.setText(R.string.month_2);
                            break;
                        case 2:
                            holderl.mes.setText(R.string.month_3);
                            break;
                        case 3:
                            holderl.mes.setText(R.string.month_4);
                            break;
                        case 4:
                            holderl.mes.setText(R.string.month_5);
                            break;
                        case 5:
                            holderl.mes.setText(R.string.month_6);
                            break;
                        case 6:
                            holderl.mes.setText(R.string.month_7);
                            break;
                        case 7:
                            holderl.mes.setText(R.string.month_8);
                            break;
                        case 8:
                            holderl.mes.setText(R.string.month_9);
                            break;
                        case 9:
                            holderl.mes.setText(R.string.month_10);
                            break;
                        case 10:
                            holderl.mes.setText(R.string.month_11);
                            break;
                        case 11:
                            holderl.mes.setText(R.string.month_12);
                            break;

                    }
                    mes_ocupado[registro.getMonth()]=registro.getRegistroId();
                }else{
                    holderl.mes.setVisibility(View.GONE);
                }
            }else{
                holderl.mes.setVisibility(View.VISIBLE);
                switch (registro.getMonth()){
                    case 0:
                        holderl.mes.setText(R.string.month_1);
                        break;
                    case 1:
                        holderl.mes.setText(R.string.month_2);
                        break;
                    case 2:
                        holderl.mes.setText(R.string.month_3);
                        break;
                    case 3:
                        holderl.mes.setText(R.string.month_4);
                        break;
                    case 4:
                        holderl.mes.setText(R.string.month_5);
                        break;
                    case 5:
                        holderl.mes.setText(R.string.month_6);
                        break;
                    case 6:
                        holderl.mes.setText(R.string.month_7);
                        break;
                    case 7:
                        holderl.mes.setText(R.string.month_8);
                        break;
                    case 8:
                        holderl.mes.setText(R.string.month_9);
                        break;
                    case 9:
                        holderl.mes.setText(R.string.month_10);
                        break;
                    case 10:
                        holderl.mes.setText(R.string.month_11);
                        break;
                    case 11:
                        holderl.mes.setText(R.string.month_12);
                        break;

                }
            }

        }else{
            holderl.mes.setVisibility(View.GONE);
        }
        if(visibleDelete) {
            holderl.seleccionado.setVisibility(View.VISIBLE);
        }else{
            holderl.seleccionado.setVisibility(View.GONE);
            holderl.seleccionado.setChecked(false);
        }

        if(registro.getDay()<10){
            String dia = "0"+registro.getDay();
            holderl.dia.setText(dia);
        }


        final ConstraintLayout mlayout = holderl.mylayout;
        final ConstraintLayout containerRegistro = holderl.containerRegistro;
        if(mes_visible[registro.getMonth()]) {
            containerRegistro.setVisibility(View.VISIBLE);
        }else{
            containerRegistro.setVisibility(View.GONE);
        }



        holderl.mes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mes_visible[registro.getMonth()]){
                    mes_visible[registro.getMonth()]=false;
                    notifyDataSetChanged();
                }else{
                    mes_visible[registro.getMonth()]=true;
                    notifyDataSetChanged();
                }
            }
        });

        mlayout.setClickable(true);


            mlayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(visibleDelete){
                      if(holderl.seleccionado.isChecked()){
                          holderl.seleccionado.setChecked(false);
                          if(listaSeleccionados.contains(registro)){
                              listaSeleccionados.remove(registro);
                          }
                      }else{
                          holderl.seleccionado.setChecked(true);
                          listaSeleccionados.add(registro);
                      }
                    }

                }
            });


        mlayout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                abrirDialogEliminarIndividual(registro);
                /*visibleDelete=true;
                myflaotingbtn.setImageResource(R.drawable.delete);
                holderl.seleccionado.setChecked(true);
                listaSeleccionados.add(registro);
                notifyDataSetChanged();
                 */
                return true;
            }
        });


        if(visibleDelete){
            DetallesPersonaActivity.btnAdd.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    abrirDialogEliminar(listaSeleccionados);
                }
            });
        }


    }

    public int nMenor(int mes){
        int menor=32;
            for(Registro p : registroArrayList){
                if(p.getMonth()==mes){
                    if(p.getDay()<menor){
                        menor=p.getDay();
                    }
                }
            }
         return menor;
    }



    public void abrirDialogEliminar(final ArrayList<Registro> seleccionados){
        AlertDialog.Builder builder = new AlertDialog.Builder(mactivity);
        builder.setTitle("Eliminar");
        builder.setMessage("¿Esta seguro que desea eliminar los registros seleccionados?")
                .setPositiveButton("Si", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        final FirebaseUser user = mauth.getCurrentUser();
                        firebase =  new Firebase(url+user.getUid());
                       for (int i=0; i<seleccionados.size();i++){
                           Registro arg = seleccionados.get(i);
                           firebase.child("registros").child(persona).child(arg.getRegistroId()).setValue(null);
                       }
                        dialog.dismiss();
                       DetallesPersonaActivity.btnAdd.setImageResource(R.drawable.plus);
                       visibleDelete=false;
                       notifyDataSetChanged();
                       mactivity.recreate();
                        Toast.makeText(context,"Eliminado correctamente",Toast.LENGTH_LONG).show();
                    }
                }).setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).setCancelable(false).show();

    }

    public void abrirDialogEliminarIndividual(final Registro seleccionados){
        AlertDialog.Builder builder = new AlertDialog.Builder(mactivity);
        builder.setTitle("Eliminar");
        builder.setMessage("¿Esta seguro que desea eliminar los registros seleccionados?")
                .setPositiveButton("Si", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        final FirebaseUser user = mauth.getCurrentUser();
                        firebase =  new Firebase(url+user.getUid());
                        firebase.child("registros").child(persona).child(seleccionados.getRegistroId()).setValue(null);
                        dialog.dismiss();
                        DetallesPersonaActivity.btnAdd.setImageResource(R.drawable.plus);
                        Toast.makeText(context,"Eliminado correctamente",Toast.LENGTH_LONG).show();
                    }
                }).setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).setCancelable(false).show();

    }

    public static String fmt(double d)
    {
        if(d == (int) d)
            return String.format("%d",(int)d);
        else
            return String.format("%s",d);
    }

    @Override
    public int getItemCount() {
        return registroArrayList.size();
    }

    public String convertirMoneda(float valor){
        Locale locale =mactivity.getResources().getConfiguration().locale;
        Currency currency = Currency.getInstance(locale);
        NumberFormat col = NumberFormat.getCurrencyInstance(locale);
        if(valor == (int) valor)
            if(valor<0){
                return col.format((int)valor).replace(currency.getSymbol(),"").replace("-","+").replace(".00","");
            }else{
                return col.format((int)valor).replace(currency.getSymbol(),"").replace(".00","");
            }
        else
        if(valor<0){
            return col.format(valor).replace(currency.getSymbol(),"").replace("-","+").replace(".00","");
        }else{
            return col.format(valor).replace(currency.getSymbol(),"").replace(".00","");
        }
    }



    public void updateList(List<Registro> newlist) {
        registroArrayList.clear();
        registroArrayList.addAll(newlist);
        this.notifyDataSetChanged();
    }

     public static class MyHolder extends RecyclerView.ViewHolder{

        TextView mes;
        TextView dia;
        TextView descripcion;
        TextView valor;
        ConstraintLayout mylayout;
        CheckBox seleccionado;
        ConstraintLayout containerRegistro;

        public MyHolder(@NonNull View itemView) {

            super(itemView);
            mes = itemView.findViewById(R.id.textViewMes);
            seleccionado = itemView.findViewById(R.id.checkBoxSeleccion);
            dia = itemView.findViewById(R.id.textViewDia);
            descripcion = itemView.findViewById(R.id.textViewDescripcion);
            valor = itemView.findViewById(R.id.textViewValor);
            containerRegistro = itemView.findViewById(R.id.constraintLayoutRegistro);
             mylayout = itemView.findViewById(R.id.linearLayout);
        }

        public interface OnNoteListener{
            void OnNoteClick(int position);
        }
    }
}
