package br.com.chatandroid.studentroom.fragment;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import br.com.chatandroid.studentroom.R;
import br.com.chatandroid.studentroom.activity.ConversaActivity;
import br.com.chatandroid.studentroom.activity.MainActivity;
import br.com.chatandroid.studentroom.adapter.ContatoAdapter;
import br.com.chatandroid.studentroom.config.ConfiguracaoFirebase;
import br.com.chatandroid.studentroom.helper.Preferencias;
import br.com.chatandroid.studentroom.model.Contato;

/**
 * A simple {@link Fragment} subclass.
 */
public class ContatosFragment extends Fragment {

    private ListView listView;
    private ArrayAdapter adapter;
    private ArrayList<Contato> contatos;
    private DatabaseReference firebase;
    private ValueEventListener valueEventListenerContatos;

    public ContatosFragment() {
        // Required empty public constructor
    }

    @Override
    public void onStart() {
        super.onStart();
        firebase.addValueEventListener(valueEventListenerContatos);
        Log.i("ValueEventListenet","onStart");
    }

    @Override
    public void onStop() {
        super.onStop();
        firebase.removeEventListener(valueEventListenerContatos);
        Log.i("ValueEventListenet","onStop");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        contatos = new ArrayList<>();

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_contatos, container, false);

        listView = (ListView) view.findViewById(R.id.lv_contatos);


        adapter = new ContatoAdapter(getActivity(), contatos);
        listView.setAdapter(adapter);


        //Pegando os contatos do firebase
        Preferencias preferencias = new Preferencias (getActivity());
        String identificadorUsuarioLogado = preferencias.getIdentificador();
        //Apenas do usuário logado
        firebase = ConfiguracaoFirebase.getFirebase()
                    .child("contatos")
                        .child(identificadorUsuarioLogado);

        //Listener que será notificado cada vez que os dados acima forem alterados
        valueEventListenerContatos = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                //Limpar Lista para não duplicar
                contatos.clear();

                //Criar lista
                for (DataSnapshot dados: dataSnapshot.getChildren()){

                    //Recuperar os valores dentro de "dados" e criar um objeto baseado no "Contato.class"
                    Contato contato = dados.getValue(Contato.class);
                    contatos.add(contato);
                }

                //Notificar o adapter das alreções realizadas
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        //Abrir o activity de conversas ao clicar no nome do contato
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Intent intent = new Intent(getActivity(), ConversaActivity.class);

                //recuperar dados antes de enviar
                Contato contato = contatos.get(position);

                //enviar dados para a conversa
                intent.putExtra("nome",contato.getNome());
                intent.putExtra("email",contato.getEmail());

                startActivity(intent);

            }
        });

        return view;
    }

}
