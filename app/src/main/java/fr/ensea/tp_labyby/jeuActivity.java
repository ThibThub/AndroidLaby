package fr.ensea.tp_labyby;

import static android.view.View.MeasureSpec.getSize;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Intent;
import android.graphics.Point;
import android.graphics.RectF;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import fr.ensea.tp_labyby.Bille;

public class jeuActivity extends AppCompatActivity {

    private final static int TAILLE_BILLE = 100;
    private final static String TAG = "debugPageJeu";

    private final int WALL_WIDTH = 400;
    private final int WALL_HEIGTH = 265;

    private final int NB_MURS_LARGEURS = 5;
    private final int NB_MURS_HAUTEUR = 12;

    private final double RATIO_IMAGE = 1.509; //Ratio de l'image pour le "bad guy" de Jurassic Park

    private ArrayList<RectF> rectangles; //Version labyrinthe

    //Version Casse-briques
    ImageView im[][] = new ImageView[NB_MURS_HAUTEUR][NB_MURS_LARGEURS];
    RectF rectangCasseBriques[][] = new RectF[NB_MURS_HAUTEUR][NB_MURS_LARGEURS];
    private Bille bille;
    ImageView imageBille;
    Accelero accelorenzo;

    private String nomJoueur;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jeu);
        imageBille = (ImageView)findViewById(R.id.imageView);
        imageBille.getLayoutParams().width = TAILLE_BILLE;
        imageBille.getLayoutParams().height = TAILLE_BILLE;
        constructionNiveau();
    }


    @Override
    protected void onStart() {
        super.onStart();


        bille = new Bille(this, TAILLE_BILLE, getWindowManager().getDefaultDisplay(), rectangles, rectangCasseBriques, im, NB_MURS_HAUTEUR, NB_MURS_LARGEURS);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Intent intent = getIntent();
        nomJoueur = intent.getStringExtra(MainActivity.NOM);
        Log.d(TAG, "Nom du joueur " +nomJoueur);
        Toast.makeText(this, "Nom du joueur " +nomJoueur, Toast.LENGTH_LONG).show();
        accelorenzo = new Accelero(jeuActivity.this);
        accelorenzo.startAcceleroListener();


        runThread();

    }


    @Override
    protected void onPause() {
        super.onPause();
        accelorenzo.stopAcceleroListener();
    }
    private void runThread() {


        /* Je pense que le runOnUiThread bloque l'exécution de tout le reste à cause de la boucle infinie           */
        new Thread() {
            public void run() {
                while(true) {

                    try {
                        runOnUiThread(new Runnable() {
                            /* On run un thread pouvant mettre à jour l'UI pour la position de la bille         */

                            @Override
                            public void run() {

                                //bille.updatePosBille(accelorenzo.getAccelX(), accelorenzo.getAccelY()); // à mettre dans un thread hors UI
                                imageBille.setX(bille.getPosx());
                                imageBille.setY(bille.getPosy());
                                //Log.d(TAG, "Boucle thread !");

                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    try {
                        Thread.sleep(15);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }

        }.start();



        new Thread() {
            public void run() {
                try {
                    while (true) {
                        bille.updatePosBille(accelorenzo.getAccelX(), accelorenzo.getAccelY());
                        Thread.sleep(15);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }.start();


    }


    void constructionNiveau(){
        Point sizeScreen = new Point();
        getWindowManager().getDefaultDisplay().getSize(sizeScreen);

        double largeurMurs = sizeScreen.x/NB_MURS_LARGEURS;
        double hauteurMurs = largeurMurs/RATIO_IMAGE;
        double resteLargeur = sizeScreen.x - largeurMurs*NB_MURS_LARGEURS;
        double resteHauteur = sizeScreen.y - hauteurMurs*NB_MURS_HAUTEUR; //En pixels

        Log.d(TAG, "Reste en hauteur : " +resteHauteur);

        Log.d(TAG, "Reste en largeur : " +resteLargeur);


        /*  Cartographie du niveau / grille    */
        //Hauteur : 13 ; Largeur : 5
        int [][] mapMurs = {
                {1, 1, 1, 1, 1},
                {1, 1, 1, 1, 1},
                {1, 0, 0, 1, 1},
                {1, 1, 0, 0, 1},
                {1, 1, 1, 0, 0},
                {1, 1, 0, 0, 1},
                {1, 1, 0, 0, 1},
                {1, 0, 0, 1, 1},
                {1, 0, 0, 1, 1},
                {0, 0, 1, 1, 1},
                {1, 1, 1, 1, 1},
                {1, 1, 1, 1, 1},
        };



        ConstraintLayout layout = (ConstraintLayout) findViewById(R.id.bille);
        //On créé une liste pour les blocs dont on veut tester l'intersection avec la bille
        rectangles = new ArrayList();
        boolean videAProximite;

        for (int i = 0; i < NB_MURS_HAUTEUR; i++){
            for(int j = 0; j < NB_MURS_LARGEURS; j++){
                videAProximite = false;
                if(mapMurs[i][j] == 1) {
                    ImageView imageView = new ImageView(this);
                    imageView.setImageResource(R.drawable.badguy);
                    imageView.setMaxWidth((int) largeurMurs);
                    imageView.setMaxHeight((int) hauteurMurs);
                    imageView.setAdjustViewBounds(true);
                    //imageView2.setScaleType(ImageView.ScaleType.FIT_XY);
/*                    imageView.setX((j * (int)largeurMurs) + (int)resteHauteur / 2);
                    imageView.setY((i * (int)hauteurMurs) + (int)resteLargeur / 2);*/
                    imageView.setX((j * (int) largeurMurs));
                    imageView.setY((i * (int) hauteurMurs));
                    imageView.setAlpha(1.0f);
                    layout.addView(imageView);

                    /*  Tentative de casse briques  */
                    rectangCasseBriques[i][j] = new RectF(j * (int) largeurMurs, i * (int) hauteurMurs, j * (int) largeurMurs + (int) largeurMurs, i * (int) hauteurMurs + (int) hauteurMurs);
                    im[i][j] = imageView;

                    //On créé un rectF à partir du rectangle image si le mur dispose d'un espace vite à proximité

                    if(mapMurs[i][j] != 0){
                        if (i != 0) {
                            if (mapMurs[i - 1][j] == 0) {
                                videAProximite = true;
                            }
                        }
                        if (i != NB_MURS_HAUTEUR - 1) {
                            if (mapMurs[i + 1][j] == 0) {
                                videAProximite = true;
                            }
                        }
                        if (j != 0) {
                            if (mapMurs[i][j - 1] == 0) {
                                videAProximite = true;
                            }
                        }
                        if (j != NB_MURS_LARGEURS - 1) {
                            if (mapMurs[i][j + 1] == 0) {
                                videAProximite = true;
                            }
                        }

                        if (videAProximite) {
                            rectangles.add(new RectF(j * (int) largeurMurs, i * (int) hauteurMurs, j * (int) largeurMurs + (int) largeurMurs, i * (int) hauteurMurs + (int) hauteurMurs));
                            Log.d(TAG, "Rectangle créé à i : " + i + " et j : " + j);
                        }

                        /*  Tentative de casse briques  */
                        rectangCasseBriques[i][j] = new RectF(j * (int) largeurMurs, i * (int) hauteurMurs, j * (int) largeurMurs + (int) largeurMurs, i * (int) hauteurMurs + (int) hauteurMurs);
                    }
                }
                else { //On créé un RectF empty pour pouvoir l'utiliser dans les collisions
                    rectangCasseBriques[i][j] = new RectF();
                    rectangCasseBriques[i][j].setEmpty();
                }


            }
        }







    }



}