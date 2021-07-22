package br.com.chatandroid.studentroom.activity;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;

import br.com.chatandroid.studentroom.helper.Base64Custom;
import br.com.chatandroid.studentroom.model.Usuario;
import br.com.chatandroid.studentroom.config.ConfiguracaoFirebase;
import br.com.chatandroid.studentroom.R;
import br.com.chatandroid.studentroom.helper.Preferencias;



public class CadastroUsuarioAcitivity extends AppCompatActivity {

    private EditText nome;
    private EditText email;
    private EditText senha;
    private Button botaoCadastrar;
    private Usuario usuario;

    private FirebaseAuth autenticacao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro_usuario_acitivity);

        nome = (EditText) findViewById(R.id.edit_cadastro_nome);
        email = (EditText) findViewById(R.id.edit_cadastro_email);
        senha = (EditText) findViewById(R.id.edit_cadastro_senha);
        botaoCadastrar = (Button) findViewById(R.id.bt_cadastrar);

        botaoCadastrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View w) {

                usuario = new Usuario();
                usuario.setNome(nome.getText().toString());
                usuario.setEmail(email.getText().toString());
                usuario.setSenha(senha.getText().toString());
                cadastrarUsuario();

            }
        });
    }

    private void cadastrarUsuario() {

        autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
        autenticacao.createUserWithEmailAndPassword(
                usuario.getEmail(),
                usuario.getSenha()
        ).addOnCompleteListener(CadastroUsuarioAcitivity.this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if (task.isSuccessful()) {

                    Toast.makeText(CadastroUsuarioAcitivity.this, "Sucesso ao cadastrar usuário", Toast.LENGTH_LONG).show();

                    FirebaseUser usuarioFirebase = task.getResult().getUser();

                    //Cadastrando o usuário com o código convertido em base 64 para depois conseguir recuperar
                    String identificadorUsuario = Base64Custom.codificarBase64(usuario.getEmail());
                    usuario.setId(identificadorUsuario);
                    usuario.salvar();

                    Preferencias preferencias = new Preferencias(CadastroUsuarioAcitivity.this);
                    preferencias.salvarDados(identificadorUsuario, usuario.getNome());

                    abrirLoginUsusario();

                }else{

                    String erro = "";
                    try{
                        throw task.getException();
                    } catch (FirebaseAuthWeakPasswordException e) {
                        erro = "Escolha uma senha que contenha, letras e números.";
                    } catch (FirebaseAuthInvalidCredentialsException e) {
                        erro = "Email indicado não é válido.";
                    } catch (FirebaseAuthUserCollisionException e) {
                        erro = "Já existe uma conta com esse e-mail.";
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    Toast.makeText(CadastroUsuarioAcitivity.this, "Erro ao cadastrar usuário: " + erro, Toast.LENGTH_LONG ).show();
                }

            }
        });

    }

    private void abrirLoginUsusario() {
        Intent intent = new Intent(CadastroUsuarioAcitivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }


}