package br.com.chatandroid.studentroom.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import br.com.chatandroid.studentroom.R;
import br.com.chatandroid.studentroom.adapter.MensagemAdapter;
import br.com.chatandroid.studentroom.config.ConfiguracaoFirebase;
import br.com.chatandroid.studentroom.helper.Base64Custom;
import br.com.chatandroid.studentroom.helper.Preferencias;
import br.com.chatandroid.studentroom.model.Conversa;
import br.com.chatandroid.studentroom.model.Mensagem;

public class ConversaActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private EditText editMensagem;
    private ImageButton btMensagem;
    private DatabaseReference firebase;
    private ListView listView;
    private ArrayList<Mensagem> mensagens;
    private ArrayAdapter<Mensagem> adapter;
    private ValueEventListener valueEventListenerMensagem;


    //dados do destinatário
    private String nomeUsuarioDestinatario;
    private String idUsuarioDestinatario;

    //dados do rementente
    private String idUsuarioRemetente;
    private String nomeUsuarioRemetente;

    //TESTE - APAGAR DEPOIS
    private String idMensagem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversa);

        toolbar = (Toolbar) findViewById(R.id.tb_conversa);
        editMensagem = (EditText) findViewById(R.id.edit_mensagem);
        btMensagem = (ImageButton) findViewById(R.id.bt_enviar);
        listView = (ListView) findViewById(R.id.lv_conversas);

        //dados do usuario logado
        Preferencias preferencias = new Preferencias(ConversaActivity.this);
        idUsuarioRemetente = preferencias.getIdentificador();
        nomeUsuarioRemetente = preferencias.getNome();

        //recuperando dados da "ContatosFragment"
        Bundle extra = getIntent().getExtras();

        if (extra != null) {
            nomeUsuarioDestinatario = extra.getString("nome");
            String emailDestinatario = extra.getString("email");
            idUsuarioDestinatario = Base64Custom.codificarBase64(emailDestinatario);

            Preferencias preferenciasMsg = new Preferencias(ConversaActivity.this);
            idMensagem = preferenciasMsg.getIdentificador();

        }

        toolbar.setTitle(nomeUsuarioDestinatario);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        setSupportActionBar(toolbar);

        //monta lv e adapter
        mensagens = new ArrayList<>();

        adapter = new MensagemAdapter(ConversaActivity.this, mensagens);

        listView.setAdapter(adapter);

        //recuperar msgs do firebase
        firebase = ConfiguracaoFirebase.getFirebase()
                .child("mensagem")
                .child(idUsuarioRemetente)
                .child(idUsuarioDestinatario);

        //Criar listener para msg
        valueEventListenerMensagem = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                mensagens.clear();

                for (DataSnapshot dados : dataSnapshot.getChildren()) {
                    Mensagem mensagem = dados.getValue(Mensagem.class);
                    mensagens.add(mensagem);
                }

                adapter.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        firebase.addValueEventListener(valueEventListenerMensagem);

        //Enviar a msg
        btMensagem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String textoMensagem = editMensagem.getText().toString();

                if (textoMensagem.isEmpty()) {
                    Toast.makeText(ConversaActivity.this, "Digite uma mensagem para enviar!", Toast.LENGTH_LONG).show();
                } else {
                    Mensagem mensagem = new Mensagem();
                    mensagem.setIdUsuario(idUsuarioRemetente);
                    mensagem.setMensagem(textoMensagem);

                    //salvar msg no remetente
                    Boolean retornoMensagemRemetente = salvarMensagem(idUsuarioRemetente, idUsuarioDestinatario, mensagem);
                    if (!retornoMensagemRemetente) {
                        Toast.makeText(ConversaActivity.this, "Problema ao salvar mensagem, tente novamente!", Toast.LENGTH_LONG).show();
                    } else {
                        //salvar msg no destinatario
                        Boolean retornoMensagemDestinatario = salvarMensagem(idUsuarioDestinatario, idUsuarioRemetente, mensagem);
                        if (!retornoMensagemRemetente) {
                            Toast.makeText(ConversaActivity.this, "O destinatario nao recebeu a mensagem, tente novamente!", Toast.LENGTH_LONG).show();
                        }
                    }

                    //Salvar a MENSAGEM para o remetente
                    Conversa conversa = new Conversa();
                    conversa.setIdUsuario(idUsuarioDestinatario);
                    conversa.setNome(nomeUsuarioDestinatario);
                    conversa.setMensagem(textoMensagem);
                    Boolean retornoConversaRemetente = salvarConversa(idUsuarioRemetente, idUsuarioDestinatario, conversa);
                    if (!retornoConversaRemetente) {
                        Toast.makeText(ConversaActivity.this, "Problema ao salvar a conversa, tente novamente!", Toast.LENGTH_LONG).show();
                    } else {
                        //Salvar a CONVERSA para o destinatario
                        conversa = new Conversa();
                        conversa.setIdUsuario(idUsuarioRemetente);
                        conversa.setNome(nomeUsuarioRemetente);
                        conversa.setMensagem(textoMensagem);
                        Boolean retornoConversaDestinatario = salvarConversa(idUsuarioDestinatario, idUsuarioRemetente, conversa);
                        if (!retornoConversaDestinatario) {
                            Toast.makeText(ConversaActivity.this, "Problema ao salvar a conversa para o destinatario, tente novamente!", Toast.LENGTH_LONG).show();

                        }
                    }


                    editMensagem.setText("");
                }
            }
        });

    }

    private boolean salvarMensagem(String idRemetente, String idDestinatario, Mensagem mensagem) {
        try {
            firebase = ConfiguracaoFirebase.getFirebase().child("mensagem");

            firebase.child(idRemetente)
                    .child(idDestinatario)
                    .push()
                    .setValue(mensagem);

            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean salvarConversa(String idRemetente, String idDestinatario, Conversa conversa) {
        try {
            firebase = ConfiguracaoFirebase.getFirebase().child("conversas");

            firebase.child(idRemetente)
                    .child(idDestinatario)
                    .setValue(conversa);

            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /*
    //Deletar msgs enviadas
    private boolean deletarMensagem(String idRemetente, String idDestinatario){
        try{

            firebase = ConfiguracaoFirebase.getFirebase();

            firebase.child("mensagem")
                    .child(idRemetente)
                    .child(idDestinatario).setValue(null);

            return true;

        }catch(Exception e){
            e.printStackTrace();
            return false;
        }
    }
    */

    @Override
    protected void onStop() {
        super.onStop();
        firebase.removeEventListener(valueEventListenerMensagem);

        //deletarMensagem(idUsuarioRemetente,idUsuarioDestinatario);

    }


}
