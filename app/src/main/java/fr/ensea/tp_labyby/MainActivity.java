package fr.ensea.tp_labyby;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity { //C'est notre menu

    public final static String NOM = "fr.ensea.Thibaut.application1.intent.NOM";
    private final static String TAG = "debugPageMain";

    Button bJouer;
    EditText inputNomDuJoueur;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bJouer = (Button)findViewById(R.id.button);
        bJouer.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                Intent intent = new Intent(MainActivity.this, jeuActivity.class);
                inputNomDuJoueur = (EditText)findViewById(R.id.editTextTextPersonName);

                Log.d(TAG, "Player name = " +inputNomDuJoueur.getText());
                intent.putExtra(NOM, inputNomDuJoueur.getText().toString());

                startActivity(intent);
            }
        });


    }
}