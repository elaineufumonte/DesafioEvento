package com.example.desafioevento.view;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Application;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.desafioevento.Adapter.EventosAdapter;
import com.example.desafioevento.R;
import com.example.desafioevento.model.Evento;
import com.example.desafioevento.model.People;
import com.google.gson.Gson;

import java.io.IOException;
import java.io.Serializable;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class EventosActivity extends AppCompatActivity implements EventosAdapter.OnClickEventoListener{

    private ArrayList<Evento> lista_eventos= new ArrayList<>();
    private RecyclerView rcView_eventos;
    EventosAdapter eventosAdapter;
    public static final String ARQUIVO_PREFERENCIA = "ArquivoPreferencia";
    private ProgressDialog pDialog;
    private Request request;
    final Locale locale = Locale.getDefault();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_eventos);

        rcView_eventos = findViewById(R.id.rv_eventos);
        lista_eventos.clear();

        onStart();
        Bundle dados = getIntent().getExtras();
        lista_eventos = (ArrayList<Evento>)  dados.getSerializable("listaEventos");//

        rcView_eventos.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        rcView_eventos.setLayoutManager(layoutManager);

        eventosAdapter = new EventosAdapter(EventosActivity.this, lista_eventos);
        rcView_eventos.setAdapter(eventosAdapter);




    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.navigation,menu);
        return true;

    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        int id = item.getItemId();
        if(id==R.id.action_voltar){
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed(){
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
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
    @Override
    public void salvarEventoSharedPreferences(int posicao){
        int pos = posicao;

        //https://5f5a8f24d44d640016169133.mockapi.io/api/events/1
        if (isConectado(getApplication())) {

            //infoCargasMontadas.clear();
            OkHttpClient httpClient = new OkHttpClient.Builder().connectTimeout(60, TimeUnit.SECONDS).build();

            String url = "https://5f5a8f24d44d640016169133.mockapi.io/api/events/" +lista_eventos.get(pos).getId();
            System.out.println(url);
            try {
                request = new Request.Builder().url(url).build();

            } catch (Exception e) {
                /*if (pDialog.isShowing())
                    pDialog.dismiss();*/
                e.printStackTrace();
                EventosActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(EventosActivity.this,"Erro de conexão! Tente novamente.",Toast.LENGTH_LONG).show();

                    }
                });

            }

            httpClient.newCall(request).enqueue(new Callback() {

                @Override
                public void onFailure(Call call, IOException e) {
                    /*
                    if (pDialog.isShowing())
                        pDialog.dismiss();*/
                    //mensagem.setText("Erro de conexão com servidor. Verifique sua conexão com a Internet"+e.getMessage());
                    //Toast.makeText(MainActivity.this,"Erro de conexão com servidor. Verifique sua conexão com a Internet",Toast.LENGTH_SHORT).show();

                    EventosActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(EventosActivity.this,"Falha na requisição! ",Toast.LENGTH_LONG).show();

                        }
                    });
                    //Log.e(TAG,"Erro ao finalizar o chackList: "+e.getMessage());
                    e.printStackTrace();
                }

                @Override
                public void onResponse(Call call, final Response response) throws IOException {
                    if (response.isSuccessful()) {
                        final String myResponse = response.body().string();

                        EventosActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                Gson gson = new Gson();

                                try{
                                    Evento evento = gson.fromJson(myResponse, Evento.class);

                                    Intent intent = new Intent(getApplicationContext(), CheckinActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    intent.putExtra("evento", (Serializable) evento);
                                    startActivity(intent);

                                }catch (Exception e){

                                    Toast.makeText(EventosActivity.this,"Evento indisponível... Tente mais tarde.",Toast.LENGTH_LONG).show();
                                    /*if (pDialog.isShowing())
                                        pDialog.dismiss();*/

                                }

                            }
                        });

                    } else {
                       /*
                        if (pDialog.isShowing())
                            pDialog.dismiss();*/
                        //if(!response.isSuccessful()) { //throw new IOException("Unexpected code " + response);
                        if (response.code() != 200) {
                            //tv_relatorio.setText("Erro! Número de matrícula inválida.");
                            EventosActivity.this.runOnUiThread(new Runnable() {

                                @Override
                                public void run() {
                                    System.out.println("response.code() != 200 -> " +response.code());
                                    Toast.makeText(EventosActivity.this,"Falha ao realizar uma requisição no servidor...",Toast.LENGTH_LONG).show();

                                }
                            });

                        }
                    }
                }
            });


        }else{
            /*
            if (pDialog.isShowing())
                pDialog.dismiss();*/

            Toast.makeText(EventosActivity.this,"FALHA! SEM CONEXÃO COM A INTERNET...",Toast.LENGTH_LONG).show();

        }

    }
    /*
    public void checkin( ){

        Intent intent = new Intent(getApplicationContext(), CheckinActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("evento", (Serializable) lista_eventos.get(pos));
        startActivity(intent);

    }*/
}