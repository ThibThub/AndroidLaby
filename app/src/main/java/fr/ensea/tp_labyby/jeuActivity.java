package fr.ensea.tp_labyby;

import static android.view.View.MeasureSpec.getSize;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Intent;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import fr.ensea.tp_labyby.Bille;

public class jeuActivity extends AppCompatActivity {

    private final static int TAILLE_BILLE = 100;
    private int hauteurTexte;
    private final static String TAG = "debugPageJeu";

    private final static double COEFF_DIV_HAMMOND = 1.72;
    private final static int HAUTEUR_HAMMOND = 480;

    private final int WALL_WIDTH = 400;
    private final int WALL_HEIGTH = 265;

    private final int NB_MURS_LARGEURS = 5;
    private final int NB_MURS_HAUTEUR = 12;

    private final double RATIO_IMAGE = 1.509; //Ratio de l'image pour le "bad guy" de Jurassic Park

    private long launchTime;
    private long tempsFinNiveauUtilisateur;

    private ArrayList<RectF> rectangles; //Version labyrinthe

    private Point sizeScreen;
    private ConstraintLayout layout;

    private boolean memFin = false;

    private boolean finNiveau;

    //Version Casse-briques
    ImageView im[][] = new ImageView[NB_MURS_HAUTEUR][NB_MURS_LARGEURS];
    RectF rectangCasseBriques[][] = new RectF[NB_MURS_HAUTEUR][NB_MURS_LARGEURS];
    private Bille bille;
    ImageView imageBille;
    Accelero accelorenzo;

    private String nomJoueur;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "Début de la partie !");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jeu);
        imageBille = (ImageView)findViewById(R.id.imageView);
        imageBille.getLayoutParams().width = TAILLE_BILLE;
        imageBille.getLayoutParams().height = TAILLE_BILLE;
        sizeScreen = new Point();
        getWindowManager().getDefaultDisplay().getSize(sizeScreen);
        layout = (ConstraintLayout) findViewById(R.id.bille);
        finNiveau = false;
        memFin = false;
        hauteurTexte = 50;
        constructionNiveau();
    }


    @Override
    protected void onStart() {
        super.onStart();

        launchTime = System.currentTimeMillis();
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
                                if(finNiveau) {
                                    if(!memFin) {
                                        finNiveau();
                                        finNiveau = false;
                                    }
                                    }


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
                        if(bille.updatePosBille(accelorenzo.getAccelX(), accelorenzo.getAccelY())) {
                            Log.d(TAG, "Boucle 2");
                            finNiveau = true;

                        }
                        Thread.sleep(15);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }.start();


    }

    private void finNiveau(){
        memFin = true;
        Log.d(TAG, "Fin de Niveau bon sang !!!!");
        tempsFinNiveauUtilisateur = (System.currentTimeMillis() - launchTime)/1000;


        TextView NouveauTexte = new TextView(this);
        NouveauTexte.setText("Bravo, tu as fini en " +tempsFinNiveauUtilisateur+ "s ! \nClique sur John pour relancer !");
        NouveauTexte.setMaxWidth(this.getResources().getDisplayMetrics().widthPixels - (int)(HAUTEUR_HAMMOND/COEFF_DIV_HAMMOND));
        NouveauTexte.setMaxHeight(hauteurTexte);
        NouveauTexte.setTextSize((float)0.01*this.getResources().getDisplayMetrics().heightPixels);
        //NouveauTexte.setBackground(this.getDrawable(R.drawable.woood));
        NouveauTexte.setBackgroundResource(R.drawable.woood);
        //NouveauTexte.setX(this.getResources().getDisplayMetrics().heightPixels - HAUTEUR_HAMMOND + NouveauTexte.getMaxHeight());
        NouveauTexte.setY(this.getResources().getDisplayMetrics().heightPixels - (HAUTEUR_HAMMOND+220) + NouveauTexte.getMaxHeight());
        NouveauTexte.setTextColor(getResources().getColor(android.R.color.background_light));
        NouveauTexte.setTypeface(null, Typeface.BOLD);
        NouveauTexte.setWidth(this.getResources().getDisplayMetrics().widthPixels - (int)(HAUTEUR_HAMMOND/COEFF_DIV_HAMMOND));
        layout.addView(NouveauTexte);



        /*  On affiche un John HAMMOND de la gentillesse            */
        ImageView john = new ImageView(this);
        john.setImageResource(R.drawable.hammond);
        john.setMaxHeight(HAUTEUR_HAMMOND);
        john.setMaxWidth((int) (HAUTEUR_HAMMOND/COEFF_DIV_HAMMOND));
        john.setAdjustViewBounds(true);
        john.setX(this.getResources().getDisplayMetrics().widthPixels - (int)(HAUTEUR_HAMMOND/COEFF_DIV_HAMMOND));
        john.setY(this.getResources().getDisplayMetrics().heightPixels - (HAUTEUR_HAMMOND+220));
        john.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                finish();
                startActivity(getIntent());
            }
        });
        layout.addView(john);
        /*  On récupère le temps de fin
        * On affiche un message de joie et de bonheur
        * On affiche le temps final
        * On affiche le tableau des scores
        */
    }


    void constructionNiveau(){

        double largeurMurs = sizeScreen.x/NB_MURS_LARGEURS;
        double hauteurMurs = largeurMurs/RATIO_IMAGE;
        double resteLargeur = sizeScreen.x - largeurMurs*NB_MURS_LARGEURS;
        double resteHauteur = sizeScreen.y - hauteurMurs*NB_MURS_HAUTEUR; //En pixels

        Log.d(TAG, "Reste en hauteur : " +resteHauteur);

        Log.d(TAG, "Reste en largeur : " +resteLargeur);

        /* Génération automatique map       */
        Random inte = new Random();
        int posIniBilleX = (this.getResources().getDisplayMetrics().widthPixels-TAILLE_BILLE)/2;
        int posIniBilleY = (this.getResources().getDisplayMetrics().heightPixels-TAILLE_BILLE)/2;
        int [][] mapMurs= new int[NB_MURS_HAUTEUR][NB_MURS_HAUTEUR];
        for (int i = 0; i < NB_MURS_HAUTEUR; i++) {
            for (int j = 0; j < NB_MURS_LARGEURS; j++) {
                mapMurs[i][j] = inte.nextInt(2);
//                if(((j * (int) largeurMurs < posIniBilleX) && ((j+1) * (int) largeurMurs > posIniBilleX)) || (i * (int) hauteurMurs < posIniBilleY) && ((i+1) * (int) hauteurMurs > posIniBilleY))
//                    mapMurs[i][j] = 0;
//            }
                if(((j * (int) largeurMurs < posIniBilleX) && ((j+1) * (int) largeurMurs > posIniBilleX)) && (i * (int) hauteurMurs < posIniBilleY) && ((i+1) * (int) hauteurMurs > posIniBilleY))
                    mapMurs[i][j] = 0;
            }
        }


        /*  Cartographie du niveau / grille   "en dur" */
        //Hauteur : 13 ; Largeur : 5
        /*int [][] mapMurs = {
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
                {0, 0, 0, 0, 0},
        };*/

        /*int [][] mapMurs = {
                {0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0},
                {0, 1, 0, 0, 0},
                {0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0},
        };*/



        //ConstraintLayout layout = (ConstraintLayout) findViewById(R.id.bille);
        //On créé une liste pour les blocs dont on veut tester l'intersection avec la bille
        rectangles = new ArrayList();
        boolean videAProximite;

        /* Géneration niveau "en dur"       */
        for (int i = 0; i < NB_MURS_HAUTEUR; i++){
            for(int j = 0; j < NB_MURS_LARGEURS; j++){
                videAProximite = false;
                if(mapMurs[i][j] == 1) {
                    ImageView imageView = new ImageView(this);
                    //imageView.setImageResource(R.drawable.badguy);
                    imageView.setImageResource(R.drawable.caisse);
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