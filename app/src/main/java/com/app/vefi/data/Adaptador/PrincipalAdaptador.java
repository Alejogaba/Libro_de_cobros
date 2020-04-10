package com.app.vefi.data.Adaptador;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.LauncherActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.app.vefi.DetallesPersonaActivity;
import com.app.vefi.R;
import com.app.vefi.data.model.Person;
import com.app.vefi.data.model.Registro;
import com.firebase.client.Firebase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

public class PrincipalAdaptador extends RecyclerView.Adapter {
    private Context context;
    private FirebaseAuth mauth;
    private ArrayList<Person> personArrayList;
    private String url = "https://proyecto-cobros.firebaseio.com/users/";
    private Firebase firebase;
    private String persona;
    private Activity mactivity;
    private boolean isLongPress;
    Runnable mRunnable,timeRunnable;
    Handler mHandler=new Handler();


    public PrincipalAdaptador(Activity mactivity,Context context, ArrayList<Person> personArrayList) {
        this.context = context;
        this.mactivity = mactivity;
        this.personArrayList = personArrayList;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View conttentView = LayoutInflater.from(context).inflate(R.layout.item_list_main,null);

        return new PrincipalAdaptador.MyHolder(conttentView);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
        final Person person = personArrayList.get(position);
        final PrincipalAdaptador.MyHolder holderl = (PrincipalAdaptador.MyHolder) holder;
        mauth = FirebaseAuth.getInstance();
        holderl.nombre.setText(person.getNombre());

        final LinearLayout mlayout = holderl.mylayout;
        mlayout.setClickable(true);

        mlayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchDetallesActivity(person.getId(),person.getNombre());
            }
        });

        mlayout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                abrirDialog(person);
                return true;
            }
        });




    }


    public void abrirDialog(final Person registro){
        AlertDialog.Builder builder = new AlertDialog.Builder(mactivity);
        builder.setTitle("Eliminar");
        builder.setMessage("¿Esta seguro que desea eliminar la persona seleccionada junto con todos sus registros?")
                .setPositiveButton("Si", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        final FirebaseUser user = mauth.getCurrentUser();
                        firebase =  new Firebase(url+user.getUid());
                        firebase.child("personas").child(registro.getId()).setValue(null);
                        firebase =  new Firebase(url+user.getUid());
                        firebase.child("registros").child(registro.getNombre()).setValue(null);
                        dialog.dismiss();
                        Toast.makeText(context,"Eliminado correctamente",Toast.LENGTH_LONG).show();
                    }
                }).setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).setCancelable(false).show();

    }

    @Override
    public int getItemCount() {
        return personArrayList.size();
    }

    public void launchDetallesActivity(String seleccionado,String nombre){
        Intent intent = new Intent(context, DetallesPersonaActivity.class);
        intent.putExtra("uid",seleccionado);
        intent.putExtra("name",nombre);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        context.startActivity(intent);
    }


    public static class MyHolder extends RecyclerView.ViewHolder{

        TextView nombre;
        LinearLayout mylayout;
        public MyHolder(@NonNull View itemView) {
            super(itemView);
            nombre = itemView.findViewById(R.id.textViewNpersona);
            mylayout = itemView.findViewById(R.id.linearlayoutPrincipal);
        }

    }
}