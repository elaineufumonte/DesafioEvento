package com.example.desafioevento.Adapter;

import android.app.Activity;
import android.app.Application;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.desafioevento.R;
import com.example.desafioevento.model.Evento;
import com.example.desafioevento.view.CheckinActivity;
import com.example.desafioevento.view.EventosActivity;
import com.google.gson.Gson;

import java.io.IOException;
import java.io.Serializable;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class EventosAdapter extends RecyclerView.Adapter<EventosAdapter.MyViewHolder> {

    private OnClickEventoListener onClickEventoListener;
    private Activity acontext;
    public static final String ARQUIVO_PREFERENCIA = "ArquivoPreferencia";
    private ProgressDialog pDialog;
    private Request request;


    private ArrayList<Evento> alistaEvento = new ArrayList<>();
    Evento evento_selec = new Evento();
    int position;

    public interface OnClickEventoListener{
        void salvarEventoSharedPreferences(int position);

    }

    public EventosAdapter(Activity acontext, ArrayList<Evento> alistaEvento){
        this.acontext = acontext;
        this.alistaEvento = alistaEvento;
        try{
            this.onClickEventoListener = (OnClickEventoListener) acontext;
        }catch(ClassCastException ex){
            //.. should log the error or throw and exception
            Log.e("MyAdapter","Must implement the CallbackInterface in the Activity", ex);
        }
    }

    @NonNull
    @Override
    public EventosAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View linhaView = inflater.inflate(R.layout.item_evento, parent, false);

        MyViewHolder myViewHolder = new MyViewHolder(linhaView, context);

        return myViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull EventosAdapter.MyViewHolder holder, final int position) {
        final Evento evento = alistaEvento.get(position);

        holder.title.setText(""+evento.getTitle());

    }

    @Override
    public int getItemCount() {
        return alistaEvento.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{
        public TextView title;
        public ImageView btnDirecionar;

        public MyViewHolder(@NonNull View itemView, final Context acontext) {
            super(itemView);
            title = itemView.findViewById(R.id.tv_title);
            btnDirecionar = itemView.findViewById(R.id.img_ic_direcionar);

            btnDirecionar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    /*
                    pDialog = new ProgressDialog(acontext);
                    pDialog.setMessage("Verificando... Aguarde...");
                    pDialog.setIndeterminate(false);
                    pDialog.setCancelable(false);
                    pDialog.show();*/
                    Evento item_selecionado = alistaEvento.get(getLayoutPosition());
                    position = getLayoutPosition();
                    buscarEvento(position);
                   // onClickEventoListener.salvarEventoSharedPreferences(position);

                }
            });
        }
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
    public void buscarEvento(int posicao){
        int pos = posicao;

        //https://5f5a8f24d44d640016169133.mockapi.io/api/events/1
        if (isConectado(acontext.getApplication())) {

            //infoCargasMontadas.clear();
            OkHttpClient httpClient = new OkHttpClient.Builder().connectTimeout(60, TimeUnit.SECONDS).build();

            String url = "https://5f5a8f24d44d640016169133.mockapi.io/api/events/" +alistaEvento.get(pos).getId();
            System.out.println(url);
            try {
                request = new Request.Builder().url(url).build();

            } catch (Exception e) {
                /*if (pDialog.isShowing())
                    pDialog.dismiss();*/
                e.printStackTrace();
                acontext.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(acontext,"Erro de conexão! Tente novamente.",Toast.LENGTH_LONG).show();

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

                    acontext.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(acontext,"Falha na requisição! ",Toast.LENGTH_LONG).show();

                        }
                    });
                    //Log.e(TAG,"Erro ao finalizar o chackList: "+e.getMessage());
                    e.printStackTrace();
                }

                @Override
                public void onResponse(Call call, final Response response) throws IOException {
                    if (response.isSuccessful()) {
                        final String myResponse = response.body().string();

                        acontext.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                Gson gson = new Gson();

                                try{
                                    Evento evento = gson.fromJson(myResponse, Evento.class);

                                    Intent intent = new Intent(acontext.getApplicationContext(), CheckinActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    intent.putExtra("evento", (Serializable) evento);
                                    ((AppCompatActivity) acontext).startActivity(intent);

                                }catch (Exception e){

                                    Toast.makeText(acontext,"Evento indisponível... Tente mais tarde.",Toast.LENGTH_LONG).show();
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
                            acontext.runOnUiThread(new Runnable() {

                                @Override
                                public void run() {
                                    System.out.println("response.code() != 200 -> " +response.code());
                                    Toast.makeText(acontext,"Falha ao realizar uma requisição no servidor...",Toast.LENGTH_LONG).show();

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

            Toast.makeText(acontext,"FALHA! SEM CONEXÃO COM A INTERNET...",Toast.LENGTH_LONG).show();

        }

    }

}
