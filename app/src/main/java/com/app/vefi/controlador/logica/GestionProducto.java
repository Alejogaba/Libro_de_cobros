package com.app.vefi.controlador.logica;

import android.content.Context;

import com.app.vefi.data.model.Registro;
import com.firebase.client.Firebase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import static com.app.vefi.ui.login.LoginActivity.lgnActivity;

public class GestionProducto {
    private FirebaseAuth mauth;
    private Firebase firebase;
    private String url = "https://proyecto-cobros.firebaseio.com/users/";

    public GestionProducto(Context context) {
        mauth = FirebaseAuth.getInstance();
        Firebase.setAndroidContext(context);
        final FirebaseUser user = mauth.getCurrentUser();
        firebase =  new Firebase(url+user.getUid()+"/registros");
    }

    public void addDato(int day,int month,int year,String descripcion,float value,String persona){
        FirebaseUser user = mauth.getCurrentUser();
        firebase =  new Firebase(url+user.getUid()+"/registros/"+persona);
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
        firebase.child(pushID).setValue(person);
    }
}
