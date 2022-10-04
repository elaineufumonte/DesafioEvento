package com.example.desafioevento.view;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Application;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.desafioevento.R;
import com.example.desafioevento.model.Evento;
import com.example.desafioevento.model.People;
import com.example.desafioevento.model.PeopleEventoDTO;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class CheckinActivity extends AppCompatActivity {

    private TextView title, tv_end, tv_est, tv_pais;
    private Evento evento;
    private TextView descricao;
    private TextView tv_qtd, tv_valor, mensagem;
    private ImageView imageView;
    private ProgressDialog pDialog;
    private Request request;
    private ArrayList<Evento> lista_eventos2= new ArrayList<>();
    private TextInputEditText editText_nome;
    private TextInputEditText editText_email;
    private Button btn_conclui;
    private Button btn_cad;
    private LinearLayout llayout_cad;
    private String edtUrl, id, price;
    private int qt_people;
    private long date;
    private Location location; //coordenadas do gps
    private LocationManager locationManager;//busca qual o provedor de servico de GPS
    private Address endereco;// vai trazer o endereço das coordenadas
    //private String description, image, longitude, latitude, price, title_, id;
    int imagCarregada=0;
    public static final String ARQUIVO_PREFERENCIA = "ArquivoPreferencia";
    final Locale locale = Locale.getDefault();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkin);

        //intent.putExtra("evento", (Serializable) evento);
        Bundle dados = getIntent().getExtras();
        evento = (Evento)  dados.getSerializable("evento");

        editText_nome = findViewById(R.id.editText_nome);
        editText_email = findViewById(R.id.editText_email);
        btn_conclui = findViewById(R.id.btn_conclui);
        btn_cad = findViewById(R.id.btn_conf_evento);
        llayout_cad = findViewById(R.id.llayout_cad);
        title = findViewById(R.id.tv_title_);
        tv_end = findViewById(R.id.tv_end);
        tv_est = findViewById(R.id.tv_est);
        tv_pais = findViewById(R.id.tv_pais);
        //Bundle dados = getIntent().getExtras();
        //evento = (Evento)  dados.getSerializable("evento");

        tv_qtd = findViewById(R.id.tv_qtd_part);
        tv_valor = findViewById(R.id.tv_valor);
        descricao = findViewById(R.id.tv_mult_lin);
        imageView = (ImageView)findViewById(R.id.imageView);
        mensagem = findViewById(R.id.tv_mensg);

        tv_qtd.setText("Inscritos: "+evento.getPeople().size());
        title.setText(evento.getTitle());
        descricao.setText(evento.getDescription());
        date = evento.getDate();
        Double latitude = evento.getLatitude();
        Double longitude = evento.getLongitude();
       // price = evento.getPrice().toString();
        String formatted_valor = NumberFormat.getCurrencyInstance(locale).format(evento.getPrice());
        tv_valor.setText("Valor: " +formatted_valor);

        id = evento.getId();
        //imageView = findViewById(R.id.imageView);
        edtUrl = evento.getImage();

        if(imagCarregada ==0){
        new CheckinActivity.DownloadImagemAsyncTask().execute(
                edtUrl);
        imagCarregada = 1;
        }
        try{
           endereco = carregarEndereco(latitude, longitude);
           String end = endereco.getLocality();
           String estado = endereco.getAdminArea();
           String pais = endereco.getCountryName();
           if(end != null){
               tv_end.setText("Cidade: " + end);
           }
           if(estado != null){
               tv_est.setText("Estado: " + estado);
           }
           if(pais != null){
               tv_pais.setText("País: " +pais);
           }

        }catch (IOException e){
            Log.i("GPS", e.getMessage());
        }
        //title.setText(evento.getTitle());

        btn_cad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                llayout_cad.setVisibility(View.VISIBLE);
                btn_cad.setEnabled(false);
            }
        });

        btn_conclui.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                desabilita_btns();

                pDialog = new ProgressDialog(CheckinActivity.this);
                pDialog.setMessage("Buscando.. Aguarde...");
                pDialog.setIndeterminate(false);
                pDialog.setCancelable(false);
                pDialog.show();
                confereEntradas();
            }
        });

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
            //finish();
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed(){
        pDialog = new ProgressDialog(CheckinActivity.this);
        pDialog.setMessage("Buscando.. Aguarde...");
        pDialog.setIndeterminate(false);
        pDialog.setCancelable(false);
        pDialog.show();

        listarEventos();
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

    //insert { "eventId": "1", "name": "Otávio", "email": "otavio_souza@..." }
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
                Toast.makeText(CheckinActivity.this,"Erro de conexão! Tente novamente.", Toast.LENGTH_LONG).show();

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

                        CheckinActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                Gson gson = new Gson();
                                try {
                                    JSONArray listaEventoJson = new JSONArray(myResponse);
                                    JSONObject evento;
                                    //ArrayList<Placa> lista_placas= new ArrayList<>();
                                    lista_eventos2.clear();
                                    if(listaEventoJson.length() == 0){
                                        Toast.makeText(CheckinActivity.this,"Não possui nenhum evento no momento...", Toast.LENGTH_LONG).show();

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

                                            lista_eventos2.add(objetoEvento);

                                        }
                                        if (pDialog.isShowing())
                                            pDialog.dismiss();

                                        Intent intent = new Intent(getApplicationContext(), EventosActivity.class);
                                        intent.putExtra("listaEventos", (Serializable) lista_eventos2);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                        startActivity(intent);
                                    }

                                    //System.out.println("Tamanho da lista de eventos: "+lista_eventos2.size());
                                } catch (JSONException e) {
                                    if (pDialog.isShowing())
                                        pDialog.dismiss();
                                    Log.e("Erro", "Erro no parsing do JSON", e);
                                    //System.out.println("Erro no parsing do Json" + e);
                                    Toast.makeText(CheckinActivity.this,"Erro na requisição.", Toast.LENGTH_LONG).show();


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
                                    Toast.makeText(CheckinActivity.this,"Eventos não encontrados.", Toast.LENGTH_LONG).show();

                                }
                            });

                        }
                    }
                }
            });


        }else{
            if (pDialog.isShowing())
                pDialog.dismiss();
            Toast.makeText(CheckinActivity.this,"FALHA! SEM CONEXÃO COM A INTERNET", Toast.LENGTH_LONG).show();

        }
    }
    public void habilita_btns(){
        btn_cad.setEnabled(true);
        btn_cad.setClickable(true);
        btn_conclui.setEnabled(true);
        btn_conclui.setClickable(true);
    }
    public void desabilita_btns(){
        btn_cad.setEnabled(false);
        btn_cad.setClickable(false);
        btn_conclui.setEnabled(false);
        btn_conclui.setClickable(false);

    }
    public void confereEntradas(){

        boolean res = false;
        if(res = isCampoVazio(editText_nome.getText().toString())){
            if (pDialog.isShowing())
                pDialog.dismiss();
            editText_nome.requestFocus();
            Toast.makeText(CheckinActivity.this,"Atenção! O campo nome deve ser preenchido...",Toast.LENGTH_LONG).show();
            habilita_btns();
        }else if(res = isCampoVazio(editText_email.getText().toString())){
            if (pDialog.isShowing())
                pDialog.dismiss();
            editText_email.requestFocus();
            Toast.makeText(CheckinActivity.this,"Atenção! O campo e-mail deve ser preenchido...",Toast.LENGTH_LONG).show();
            habilita_btns();
        }

        if(!res){

            if (isConectado(getApplication())) {
                insertCad();
            } else {
                if (pDialog.isShowing())
                    pDialog.dismiss();
                mensagem.setText("FALHA! SEM CONEXÃO COM A INTERNET");
                habilita_btns();
            }

        }

    }

    private boolean isCampoVazio(String dado){
        boolean resultado = (TextUtils.isEmpty(dado) || dado.trim().isEmpty());
        return resultado;
    }
    public void insertCad(){
        //PeopleEventoDTO(String nome, String email, String eventId)
        PeopleEventoDTO obj = new PeopleEventoDTO(editText_nome.getText().toString(),editText_email.getText().toString(), id);

        Gson gson = new Gson();
        String objJson = gson.toJson(obj);

        OkHttpClient httpClient = new OkHttpClient.Builder().connectTimeout(60, TimeUnit.SECONDS).build();
        String url = "https://5f5a8f24d44d640016169133.mockapi.io/api/checkin";
        try{
            MediaType JSON = MediaType.parse("application/json; charset=utf-8");
            RequestBody body = RequestBody.create(JSON, objJson);
            request = new Request.Builder()
                    .post(body)//create(JS, objJson))
                    .url(url)
                    .build();
        }catch(Exception e){
            if (pDialog.isShowing())
                pDialog.dismiss();
            e.printStackTrace();
            mensagem.setText("Erro de conexão! Tente novamente.");
        }
        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                CheckinActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (pDialog.isShowing())
                            pDialog.dismiss();
                        Toast.makeText(CheckinActivity.this,"Falha ao realizar a inscrição... Favor entrar em contato via e-mail." +e,Toast.LENGTH_SHORT).show();

                    }
                });


            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                if(response.isSuccessful()){
                    //final String myResponse = response.body().string();
                    CheckinActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (pDialog.isShowing())
                                pDialog.dismiss();
                            Toast.makeText(CheckinActivity.this,"Inscrição realizada com sucessso...",Toast.LENGTH_SHORT).show();
                            //dialogSuc(TelaOpcoesActivity.this);
                            listarEventos();
                        }
                    });
                }else{
                    try {

                        mensagem.setText("ERRO no lançamento! " + response.code());
                        if (pDialog.isShowing())
                            pDialog.dismiss();

                    }catch (Exception e){
                        if (pDialog.isShowing())
                            pDialog.dismiss();
                    }

                }

            }
        });

    }
    /*
    public void baixarImagemClick(View v){
        new CheckinActivity.DownloadImagemAsyncTask().execute(
                evento.getImage());
    }*/



    public Address carregarEndereco(Double latitude, Double longitude) throws IOException{
        Geocoder geocoder;
        Address address = null;
        List<Address> addresses;
        geocoder = new Geocoder(getApplicationContext());

        addresses = geocoder.getFromLocation(latitude, longitude, 1);
        if(addresses.size() >0){
           address = addresses.get(0);
        }
        return address;
    }

    class DownloadImagemAsyncTask extends
            AsyncTask<String, Void, Bitmap>{

        ProgressDialog dialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = ProgressDialog.show(
                    CheckinActivity.this,
                    "Aguarde", "Carregando a  imagem...");
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            String urlString = params[0];

            try {
                URL url = new URL(urlString);
                HttpURLConnection conexao = (HttpURLConnection)
                        url.openConnection();
                conexao.setRequestMethod("GET");
                conexao.setDoInput(true);
                conexao.connect();

                InputStream is = conexao.getInputStream();
                Bitmap imagem = BitmapFactory.decodeStream(is);
                return imagem;

            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            super.onPostExecute(result);
            dialog.dismiss();
            if (result != null){
                //ImageView img = (ImageView)findViewById(R.id.imageView1);
                imageView.setImageBitmap(result);
            } else {/*
                AlertDialog.Builder builder =
                        new AlertDialog.Builder(CheckinActivity.this).
                                setTitle("Erro").
                                setMessage("Não foi possivel carregar imagem, tente novamente mais tarde!").
                setPositiveButton("OK", null);
                builder.create().show();*/
                try {
                    imageView.setImageResource(R.drawable.share);

                } catch (Exception e) {
                    mensagem.setText("Erro ao carregar um imagem...");
                }

            }
        }
    }



}