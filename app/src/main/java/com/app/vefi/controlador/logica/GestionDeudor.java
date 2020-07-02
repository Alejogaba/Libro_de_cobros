package com.app.vefi.controlador.logica;

import android.util.Log;
import android.widget.Toast;

import com.app.vefi.data.model.Person;
import com.app.vefi.data.model.Registro;
import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;

public class GestionDeudor {
    private FirebaseAuth mauth;
    private Firebase firebase;
    ArrayList<Person> personArrayList = new ArrayList<>();
    private String url = "https://proyecto-cobros.firebaseio.com/users/";



    public GestionDeudor() {
    }

    public String buscar_primero(final String palabra_clave){
        int count=0;
        personArrayList.clear();
        do {
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

                }

                @Override
                public void onCancelled(FirebaseError firebaseError) {

                }
            });
            count=count+1;
        }while (personArrayList==null&&count<10);

       return personArrayList.get(0).getId();
    }


}
