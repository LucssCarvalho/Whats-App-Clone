package br.com.chatandroid.studentroom.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import br.com.chatandroid.studentroom.R;
import br.com.chatandroid.studentroom.adapter.TabAdapter;
import br.com.chatandroid.studentroom.config.ConfiguracaoFirebase;
import br.com.chatandroid.studentroom.helper.Base64Custom;
import br.com.chatandroid.studentroom.helper.Preferencias;
import br.com.chatandroid.studentroom.helper.SlidingTabLayout;
import br.com.chatandroid.studentroom.model.Contato;
import br.com.chatandroid.studentroom.model.Usuario;


public class MainActivity extends AppCompatActivity {

    private FirebaseAuth autenticacao;
    private Button botaoSair;
    private android.support.v7.widget.Toolbar toolbar;
    private FirebaseAuth usuarioAutenticacao;

    private SlidingTabLayout slidingTabLayout;
    private ViewPager viewPager;

    private String identificadorContato;
    private DatabaseReference firebase;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        usuarioAutenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();

        toolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Space Chat");
        setSupportActionBar(toolbar);


//        slidingTabLayout.setDistributeEvenly(true);
//        slidingTabLayout.setSelectedIndicatorColors(ContextCompat.getColor(this,R.color.colorAccent));


        //Configurando o adapter
        slidingTabLayout = (SlidingTabLayout) findViewById(R.id.stl_tabs);
        viewPager = (ViewPager) findViewById(R.id.vp_pagina);

        TabAdapter tabAdapter = new TabAdapter(getSupportFragmentManager());
        viewPager.setAdapter(tabAdapter);

        slidingTabLayout.setViewPager(viewPager);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.item_sair:
                deslogarUsuario();
                return true;
            case R.id.item_adicionar:
                abrirCadastroContato();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    private void abrirCadastroContato() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);

        //Caixa de diálogo
        alertDialog.setTitle("Novo Contato");
        alertDialog.setMessage("\nE-mail do usuário");
        alertDialog.setCancelable(false);

        final EditText editText = new EditText(MainActivity.this);
        alertDialog.setView(editText);

        alertDialog.setPositiveButton("Adicionar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                String emailContato = editText.getText().toString();

                if (emailContato.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Digite o e-mail", Toast.LENGTH_LONG).show();
                } else {

                    identificadorContato = Base64Custom.codificarBase64(emailContato);

                    //Encontrar o usuário no nó dos usuários
                    firebase = ConfiguracaoFirebase.getFirebase().child("usuario").child(identificadorContato);

                    firebase.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            if (dataSnapshot.getValue() != null) {

                                Usuario usuarioContato = dataSnapshot.getValue(Usuario.class);

                                Preferencias preferencias = new Preferencias(MainActivity.this);
                                String identificadorUsuarioLogado = preferencias.getIdentificador();

                                //Criando a estrutura de contatos no firebase
                                firebase = ConfiguracaoFirebase.getFirebase();
                                firebase = firebase.child("contatos")
                                        .child(identificadorUsuarioLogado)
                                        .child(identificadorContato);

                                Contato contato = new Contato();
                                contato.setIdentificadorUsuario(identificadorContato);
                                contato.setEmail(usuarioContato.getEmail());
                                contato.setNome(usuarioContato.getNome());

                                firebase.setValue(contato);

                                Toast.makeText(MainActivity.this, "Contato adicionado", Toast.LENGTH_LONG).show();

                            } else {
                                Toast.makeText(MainActivity.this, "Usuário não cadastrado", Toast.LENGTH_LONG).show();
                            }

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                }

            }
        });

        alertDialog.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        alertDialog.create();
        alertDialog.show();
    }

    public void deslogarUsuario() {
        usuarioAutenticacao.signOut();
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}
