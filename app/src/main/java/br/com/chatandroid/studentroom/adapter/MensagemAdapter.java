package br.com.chatandroid.studentroom.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import br.com.chatandroid.studentroom.R;
import br.com.chatandroid.studentroom.helper.Preferencias;
import br.com.chatandroid.studentroom.model.Mensagem;

public class MensagemAdapter extends ArrayAdapter<Mensagem> {

    private Context context;
    private ArrayList<Mensagem> mensagens;


    public MensagemAdapter(@NonNull Context c, @NonNull ArrayList<Mensagem> objects) {
        super(c, 0, objects);
        this.context = c;
        this.mensagens = objects;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        View view = null;

        if(mensagens != null){

            //Recupera os dados do remetente
            Preferencias preferencias = new Preferencias(context);
            String idUsuarioRemetente = preferencias.getIdentificador();

            LayoutInflater inflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);

            //Recupera mensagem
            Mensagem mensagem = mensagens.get(position);

            //Monta view a partir do xml
            if(idUsuarioRemetente.equals(mensagem.getIdUsuario())){
                view = inflater.inflate(R.layout.mensagem_enviada, parent, false);
            }else{
                view = inflater.inflate(R.layout.mensagem_recebida, parent, false);
            }


            //Recupera elemento para exibir
            TextView textoMensagem = (TextView) view.findViewById(R.id.tv_mensagem);
            textoMensagem.setText(mensagem.getMensagem());
        }

        return view;
    }
}
