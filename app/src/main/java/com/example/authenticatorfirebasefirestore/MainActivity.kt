package com.example.authenticatorfirebasefirestore

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Surface
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import com.example.authenticatorfirebasefirestore.ui.theme.AuthenticatorFirebaseFirestoreTheme
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.ktx.Firebase
import com.google.firebase.auth.ktx.auth
import java.lang.reflect.Modifier

private const val TAG = "MainActivity"
private const val RC_SIGN_IN = 9001

class MainActivity : ComponentActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
            auth = Firebase.auth

        if (auth.currentUser != null) {
            startActivity(Intent(this, MainActivity2::class.java))
            finish()
            return
        }

        setContentView(R.layout.activity_main)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        val emailEditText: EditText = findViewById(R.id.emailEditText)
        val passwordEditText: EditText = findViewById(R.id.passwordEditText)
        val loginButton: Button = findViewById(R.id.loginButton)
        val googleSignInButton: Button = findViewById(R.id.googleSignInButton)

        loginButton.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                Log.i(TAG, "Botão de login com email e senha pressionado")
                signInWithEmail(email, password)
            } else {
                Toast.makeText(this, "Por favor, insira email e senha.", Toast.LENGTH_SHORT).show()
            }
        }

        googleSignInButton.setOnClickListener {
            Log.i(TAG, "Botão de login com Google pressionado")
            signInWithGoogle()
        }
    }

    private fun signInWithEmail(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.i(TAG, "signInWithEmail: Sucesso")
                    Toast.makeText(this, "Login bem-sucedido!", Toast.LENGTH_SHORT).show()

                    startActivity(Intent(this, MainActivity2::class.java))
                    finish()
                } else {
                    Log.i(TAG, "signInWithEmail: Falha -> ${task.exception}")
                    Toast.makeText(this, "Falha no login: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun signInWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                Log.i(TAG, "onActivityResult: Google Sign-In bem-sucedido")
                firebaseAuthWithGoogle(account)
            } catch (e: ApiException) {
                Log.w(TAG, "onActivityResult: Falha no Google Sign-In", e)
            }
        }
    }

    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.i(TAG, "firebaseAuthWithGoogle: Sucesso - Usuário autenticado")
                    Toast.makeText(this, "Login com Google bem-sucedido!", Toast.LENGTH_SHORT).show()

                    startActivity(Intent(this, MainActivity2::class.java))
                    finish()
                } else {
                    Log.i(TAG, "firebaseAuthWithGoogle: Falha na autenticação -> ${task.exception}")
                    Toast.makeText(this, "Falha no login com Google: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }
}
