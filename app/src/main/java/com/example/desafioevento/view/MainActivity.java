package com.example.desafioevento.view;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Application;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import com.example.desafioevento.R;
import com.example.desafioevento.model.Evento;
import com.example.desafioevento.model.People;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private Button btn_listarEventos;
    private ProgressDialog pDialog;
    private Request request;
    private ArrayList<Evento> lista_eventos= new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btn_listarEventos = findViewById(R.id.btn_listarEventos);

        btn_listarEventos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pDialog = new ProgressDialog(MainActivity.this);
                pDialog.setMessage("Buscando.. Aguarde...");
                pDialog.setIndeterminate(false);
                pDialog.setCancelable(false);
                pDialog.show();
                listarEventos();
            }
        });

    }

    public void listarEventos(){

        //http://177.128.80.187:9091/usuarios/buscarTodosUsuarios
        if (isConectado(getApplication())) {



            OkHttpClient httpClient = new OkHttpClient.Builder().connectTimeout(30, TimeUnit.SECONDS).build();

            //177.128.80.187:9091/placa/buscarTodas-placas
            String url = "https://5f5a8f24d44d640016169133.mockapi.io/api/events";
            System.out.println(url);

            try {
                request = new Request.Builder().url(url).build();
            } catch (Exception e) {
                if (pDialog.isShowing())
                    pDialog.dismiss();
                e.printStackTrace();
                Toast.makeText(MainActivity.this,"Erro de conexão! Tente novamente.", Toast.LENGTH_LONG).show();

            }

            httpClient.newCall(request).enqueue(new Callback() {

                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    if (pDialog.isShowing())
                        pDialog.dismiss();

                    e.printStackTrace();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        final String myResponse = response.body().string();

                        if (pDialog.isShowing())
                            pDialog.dismiss();

                        MainActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                Gson gson = new Gson();
                                try {
                                    JSONArray listaEventoJson = new JSONArray(myResponse);
                                    JSONObject evento;
                                    //ArrayList<Placa> lista_placas= new ArrayList<>();
                                    lista_eventos.clear();
                                    if(listaEventoJson.length() == 0){
                                        Toast.makeText(MainActivity.this,"Não possui nenhum evento no momento...", Toast.LENGTH_LONG).show();

                                    }else{
                                        System.out.println("A lista de eventos possui itens");
                                        for (int i = 0; i < listaEventoJson.length(); i++) {
                                            ArrayList<People> listaPeople = new ArrayList<>();
                                            evento = new JSONObject(listaEventoJson.getString(i));

                                            Evento objetoEvento = new Evento();


                                            objetoEvento.setDate(evento.getLong("date"));
                                            objetoEvento.setDescription(evento.getString("description"));
                                            objetoEvento.setImage(evento.getString("image"));
                                            objetoEvento.setLongitude(evento.getDouble("longitude"));
                                            objetoEvento.setLatitude(evento.getDouble("latitude"));
                                            objetoEvento.setPrice(evento.getDouble("price"));
                                            objetoEvento.setTitle(evento.getString("title"));
                                            objetoEvento.setId(evento.getString("id"));
                                            JSONArray listaPeople_Json = new JSONArray(evento.get("people").toString());
                                            //JSONArray lista_i = listaCarreg_i_Json.getJSONArray("listaDestinos");
                                            ArrayList<People> lista_people_preecher = new ArrayList<>();

                                            if(listaPeople_Json.length() !=0){
                                                for (int ii = 0; ii < listaPeople_Json.length(); ii++) {
                                                    JSONObject objPeople = new JSONObject(listaPeople_Json.getString(ii));
                                                    People obj_people = new People();
                                                    obj_people.setEventId(objPeople.getString("eventId"));
                                                    obj_people.setName(objPeople.getString("name"));
                                                    obj_people.setEmail(objPeople.getString("email"));

                                                    lista_people_preecher.add(obj_people);
                                                }
                                            }
                                            objetoEvento.setPeople(lista_people_preecher);

                                            lista_eventos.add(objetoEvento);

                                        }
                                        System.out.println("Tamanho da lista de eventos: "+lista_eventos.size());

                                        Intent intent = new Intent(getApplicationContext(), EventosActivity.class);
                                        intent.putExtra("listaEventos", (Serializable) lista_eventos);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                        startActivity(intent);
                                    }

                                    System.out.println("Tamanho da lista de eventos: "+lista_eventos.size());
                                } catch (JSONException e) {
                                    Log.e("Erro", "Erro no parsing do JSON", e);
                                    //System.out.println("Erro no parsing do Json" + e);
                                    Toast.makeText(MainActivity.this,"Erro na requisição.", Toast.LENGTH_LONG).show();


                                }


                            }
                        });

                    } else {
                        if (pDialog.isShowing())
                            pDialog.dismiss();
                        //if(!response.isSuccessful()) { //throw new IOException("Unexpected code " + response);
                        if (response.code() != 200) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(MainActivity.this,"Eventos não encontrados.", Toast.LENGTH_LONG).show();

                                }
                            });

                        }
                    }
                }
            });


        }else{
            if (pDialog.isShowing())
                pDialog.dismiss();
            Toast.makeText(MainActivity.this,"FALHA! SEM CONEXÃO COM A INTERNET", Toast.LENGTH_LONG).show();

        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu,menu);
        return true;

    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        int id = item.getItemId();
        if(id==R.id.action_sair){
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
    private Boolean isConectado(Application application) {
        ConnectivityManager connectivityManager = (ConnectivityManager) application.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Network nw = connectivityManager.getActiveNetwork();
            if (nw == null) return false;
            NetworkCapabilities actNw = connectivityManager.getNetworkCapabilities(nw);
            return actNw != null && (actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) || actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) || actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) || actNw.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH));
        } else {
            NetworkInfo nwInfo = connectivityManager.getActiveNetworkInfo();
            return nwInfo != null && nwInfo.isConnected();
        }
    }
    /*
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.navigation,menu);
        return true;

    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        int id = item.getItemId();
        if(id==R.id.action_voltar){
            //finish();
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed(){
        Intent intent = new Intent(getApplicationContext(), TelaOpcoesActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }*/
}