package com.app.vefi.data.Adaptador;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
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

import java.util.ArrayList;
import java.util.EventListener;
import java.util.List;

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
        holderl.valor.setText(fmt(registro.getValor()));
        if(visibleDelete) {
            holderl.seleccionado.setVisibility(View.VISIBLE);
        }else{
            holderl.seleccionado.setVisibility(View.GONE);
            holderl.seleccionado.setChecked(false);
        }

        final ConstraintLayout mlayout = holderl.mylayout;
        mlayout.setClickable(true);


            mlayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(visibleDelete){
                      if(holderl.seleccionado.isChecked()){
                          holderl.seleccionado.setChecked(false);
                          listaSeleccionados.remove(registro);
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
                //abrirDialogEliminar(registro);
                visibleDelete=true;
                myflaotingbtn.setImageResource(R.drawable.delete);
                holderl.seleccionado.setChecked(true);
                listaSeleccionados.add(registro);
                notifyDataSetChanged();
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



    public void updateList(List<Registro> newlist) {
        registroArrayList.clear();
        registroArrayList.addAll(newlist);
        this.notifyDataSetChanged();
    }

     public static class MyHolder extends RecyclerView.ViewHolder{

        TextView dia;
        TextView descripcion;
        TextView valor;
        ConstraintLayout mylayout;
        CheckBox seleccionado;

        public MyHolder(@NonNull View itemView) {

            super(itemView);
            seleccionado = itemView.findViewById(R.id.checkBoxSeleccion);
            dia = itemView.findViewById(R.id.textViewDia);
            descripcion = itemView.findViewById(R.id.textViewDescripcion);
            valor = itemView.findViewById(R.id.textViewValor);
             mylayout = itemView.findViewById(R.id.linearLayout);
        }

        public interface OnNoteListener{
            void OnNoteClick(int position);
        }
    }
}