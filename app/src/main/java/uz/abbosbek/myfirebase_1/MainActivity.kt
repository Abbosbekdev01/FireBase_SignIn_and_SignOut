package uz.abbosbek.myfirebase_1

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import uz.abbosbek.myfirebase_1.databinding.ActivityMainBinding

private const val TAG = "MainActivity"

class MainActivity : AppCompatActivity() {
    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    private lateinit var googleSingInClient: GoogleSignInClient
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()


        googleSingInClient = GoogleSignIn.getClient(this, gso)

        auth = FirebaseAuth.getInstance()

        if (auth.currentUser != null){
            finish()
            startActivity(Intent(this, MainActivity2::class.java))
        }

        binding.btnSingIn.setOnClickListener {
            binding.progressSignIn.visibility = View.VISIBLE
            singIn()
        }
    }


    private fun singIn() {
        val singInIntent = googleSingInClient.signInIntent
        activityResultLauncher.launch(singInIntent)
    }

    private val activityResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {

            val task = GoogleSignIn.getSignedInAccountFromIntent(it.data)
            try {
                val account = task.getResult(ApiException::class.java)
                Log.d(TAG, "onActivityResult: ${account.id}")
                firebaseAuthWithGoogle(account.idToken)
            } catch (e: ApiException) {
                Log.d(TAG, "onActivityResult: $e")
            }
        }

    private fun firebaseAuthWithGoogle(idToken: String?) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "firebaseAuthWithGoogle: success")
                    val user = auth.currentUser
                    Toast.makeText(this, "${user?.email}", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, MainActivity2::class.java))
                    binding.progressSignIn.visibility = View.INVISIBLE
                } else {
                    Log.d(TAG, "firebaseAuthWithGoogle: failure", task.exception)
                    Toast.makeText(this, "${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }
}