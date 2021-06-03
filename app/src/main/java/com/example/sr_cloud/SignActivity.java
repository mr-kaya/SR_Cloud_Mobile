package com.example.sr_cloud;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SignActivity extends AppCompatActivity {
    EditText emailText, passwordText;
    TextView errorText;
    private FirebaseAuth _firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign_activity);
        _firebaseAuth = FirebaseAuth.getInstance();
        emailText = findViewById(R.id.emailText);
        passwordText = findViewById(R.id.passwordText);
        errorText = findViewById(R.id.textViewErrorText);

        //Daha önceden giriş yapılmışmı diye bakar. (Bunu kullanarak tekrar tekrar giriş yapmaya (eposta ve şifre girmeye) gerek yok.)
        FirebaseUser firebaseUser = _firebaseAuth.getCurrentUser();

        if(firebaseUser != null) {
            Intent intent = new Intent(SignActivity.this, MainPageActivity.class);
            startActivity(intent);
            //Kullanıcı giriş sayfasına tekrar geri dönmesin diye finish yazılır.
            finish();
        }

    }

    public void clickedSignIn (View view) {
        String email = emailText.getText().toString();
        String password = passwordText.getText().toString();

        if(email.matches("")) {
            errorText.setText("Lütfen E-mail giriniz.");
            errorText.setPadding(40,20, 40, 20);
            errorText.setBackgroundColor(0xFFFF0000);
        }
        else if (password.matches("")) {
            errorText.setText("Lütfen Password giriniz.");
            errorText.setPadding(40,20, 40, 20);
            errorText.setBackgroundColor(0xFFFF0000);
        }
        else {
            errorText.setText("");
            errorText.setBackgroundColor(0xFFFFFFFF);

            _firebaseAuth.signInWithEmailAndPassword(email, password).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                @Override
                public void onSuccess(AuthResult authResult) {
                    Intent intent = new Intent(SignActivity.this, MainPageActivity.class);
                    startActivity(intent);
                    finish();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(SignActivity.this, e.getLocalizedMessage().toString(), Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    /*Kayıt Olma Komutları*/
    public void clickedSignUp (View view) {
        String email = emailText.getText().toString();
        String password = passwordText.getText().toString();

        if(email.matches("")) {
            errorText.setText("Lütfen E-mail giriniz.");
            errorText.setPadding(40,20, 40, 20);
            errorText.setBackgroundColor(0xFFFF0000);
        }
        else if (password.matches("")) {
            errorText.setText("Lütfen Password giriniz.");
            errorText.setPadding(40,20, 40, 20);
            errorText.setBackgroundColor(0xFFFF0000);
        }
        else
        {
            errorText.setText("");
            errorText.setBackgroundColor(0xFFFFFFFF);
            _firebaseAuth.createUserWithEmailAndPassword(email, password).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                @Override
                public void onSuccess(AuthResult authResult) {
                    //Ekranda yazı gösterme
                    Toast.makeText(SignActivity.this, "Kayıt Başarılı", Toast.LENGTH_LONG).show();

                    //Başka bir sayfaya gönderme
                    Intent intent = new Intent(SignActivity.this, MainPageActivity.class);
                    startActivity(intent);
                    finish();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(SignActivity.this, e.getLocalizedMessage().toString(), Toast.LENGTH_LONG).show();
                }
            });
        }

    }
}