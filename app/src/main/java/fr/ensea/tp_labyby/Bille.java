package fr.ensea.tp_labyby;

import static java.lang.Math.abs;

import android.graphics.Point;
import android.graphics.RectF;
import android.media.MediaPlayer;
import android.util.Log;
import android.view.Display;
import android.widget.ImageView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import fr.ensea.tp_labyby.R;
import fr.ensea.tp_labyby.jeuActivity;

public class Bille {

    private Display display;
    private Point sizeScreen;
    private int width;
    private int height;
    private enum minima{
        droite,
        gauche,
        haut,
        bas
    }

    private final double COEFF_FROTTEMENT = 0.8;
    private final double COEFF_REBOND = 0.6;

    private final static float VOLUME_REBOND = 0.2f;


    private final int COEFF_DIV_VITESSE = 10;
    private final static String TAG = "debugBille";
    private jeuActivity jeu;
   private int sizeX; // taille de la bille (px)
private int sizeY; // taille de la bille (px)
private float posX; // position de la bille
private float posY; // // position de la bille
private int maxX; // coordonnée max en X
private int maxY; // coordonnée max en Y
private ImageView imageView; // ImageView
    private ArrayList<RectF> rectanglesProxVide = new ArrayList();
    private boolean memFinNiveau;

    private MediaPlayer rebond;

    //Casse-Briques
    int nbMursHauteur = 12;
    int nbMursLargeur = 5;
    RectF[][] rectanglesCasseBriques;
    ImageView[][] imagesCasseBriques;


    /*  Variables pour nouvelle méthode de calcul           */
    float vitesseX;
    float vitesseY;

    long timeSinceLastEvent;
    long actualTime;
    long memTime;

    public Bille(jeuActivity jeuA, int size, Display display, ArrayList<RectF> rectanglesProxVide, RectF[][] rectanglesCasseBriques, ImageView[][] imagesCasseBriques, int nbMursHauteur, int nbMursLargeur){
        this.jeu = jeuA;
        sizeX = size;
        sizeY = size;
        posX = (jeu.getResources().getDisplayMetrics().widthPixels-sizeX)/2;
        posY = (jeu.getResources().getDisplayMetrics().heightPixels-sizeY)/2;
        vitesseX = 0;
        vitesseY = 0;
        this.rectanglesProxVide = rectanglesProxVide;

        this.display = display;

        memTime = System.currentTimeMillis();
        sizeScreen = new Point();
        this.display.getSize(sizeScreen);
        width = sizeScreen.x;
        height = sizeScreen.y;

        rebond = MediaPlayer.create(jeu, R.raw.rebond);
        rebond.setVolume(VOLUME_REBOND, VOLUME_REBOND);

        //Casse-Briques

    this.imagesCasseBriques = imagesCasseBriques;
    this.rectanglesCasseBriques = rectanglesCasseBriques;
    this.nbMursHauteur = nbMursHauteur;
    this.nbMursLargeur = nbMursLargeur;
    memFinNiveau = false;
    }

    /*  Getters         */
    public float getPosx(){
        return this.posX;
    }

    public float getPosy(){
        return this.posY;
    }


    /*      Fonction de mise à jour de la position de la bille      */
    public boolean updatePosBille(float AccelX, float AccelY) throws IOException {
        vitesseX += -AccelX/COEFF_DIV_VITESSE;
        vitesseY += AccelY/COEFF_DIV_VITESSE;

        if(posX < 0 || posX + sizeX > width) {
            if(posX < 0)
                posX = 0;
            else
                posX = width - sizeX;
            vitesseX = -vitesseX*(float)COEFF_REBOND;
            Log.d(TAG, "Vitesse X : " +vitesseX);
        }


        if(posY < 0 || posY + 150 > height) {
            if (posY < 0)
                posY = 0;
            else
                posY = height - 150;
            vitesseY = -vitesseY*(float)COEFF_REBOND;
            Log.d(TAG, "Vitesse Y : " +vitesseY);
        }



        /*  test des collisions         */

        int mem = 0;
        int mem2 = 0;
        minima pointEntree;

        //Version Labyrinthe
/*        for(int i = 0; i < rectanglesProxVide.size(); i++){
            if (rectanglesProxVide.get(i).intersects(posX, posY, posX + sizeX, posY + sizeY)){ // S'il y a collision avec la bille (positions passées en paramètre)
                *//*  On teste les distances entre les bords de la bille et les murs pour trouver la distance la plus courte avec le mur et trouver son point d'entrée *//*
                mem = abs((int)rectanglesProxVide.get(i).right - (int)posX);
                pointEntree = minima.droite;
                mem2 = abs((int)rectanglesProxVide.get(i).left - ((int)posX + sizeX));
                if(mem2 < mem) {
                    mem = mem2;
                    pointEntree = minima.gauche;
                }
                mem2 = abs((int)rectanglesProxVide.get(i).top - ((int)posY + sizeY));
                if(mem2 < mem) {
                    mem = mem2;
                    pointEntree = minima.haut;
                }
                mem2 = abs((int)rectanglesProxVide.get(i).bottom - (int)posY);
                if(mem2 < mem) {
                    pointEntree = minima.bas;
                }

                switch (pointEntree){
                    case bas:
                        posY = (int)rectanglesProxVide.get(i).bottom;
                        vitesseY = - vitesseY;
                        break;
                    case haut:
                        posY = (int)rectanglesProxVide.get(i).top - sizeY;
                        vitesseY = - vitesseY;
                        break;
                    case droite:
                        posX = (int)rectanglesProxVide.get(i).right;
                        vitesseX = - vitesseX;
                        break;
                    case gauche:
                        posX = (int)rectanglesProxVide.get(i).left - sizeX;
                        vitesseX = - vitesseX;
                        break;
                }
            }
        }*/


        //Version Casse-Briques
        memFinNiveau = true;
        for (int i = 0; i < nbMursHauteur; i++) {
            for (int j = 0; j < nbMursLargeur; j++) {

                Log.d(TAG, "Rectangle de collision : " +rectanglesCasseBriques[i][j].toShortString());
                Log.d(TAG, "i : " +i+ " j : " +j);
                if (rectanglesCasseBriques[i][j].intersects(posX, posY, posX + sizeX, posY + sizeY)) { // S'il y a collision avec la bille (positions passées en paramètre)
                    if(rebond.isPlaying()){
                        rebond.stop();
                        rebond.prepare();
                    }
                    rebond.start();

                    /*  On teste les distances entre les bords de la bille et les murs pour trouver la distance la plus courte avec le mur et trouver son point d'entrée */
                    mem = abs((int) rectanglesCasseBriques[i][j].right - (int) posX);
                    pointEntree = minima.droite;
                    mem2 = abs((int) rectanglesCasseBriques[i][j].left - ((int) posX + sizeX));
                    if (mem2 < mem) {
                        mem = mem2;
                        pointEntree = minima.gauche;
                    }
                    mem2 = abs((int) rectanglesCasseBriques[i][j].top - ((int) posY + sizeY));
                    if (mem2 < mem) {
                        mem = mem2;
                        pointEntree = minima.haut;
                    }
                    mem2 = abs((int) rectanglesCasseBriques[i][j].bottom - (int) posY);
                    if (mem2 < mem) {
                        pointEntree = minima.bas;
                    }

                    switch (pointEntree) {
                        case bas:
                            posY = (int) rectanglesCasseBriques[i][j].bottom;
                            vitesseY = -vitesseY*(float)COEFF_REBOND;
                            break;
                        case haut:
                            posY = (int) rectanglesCasseBriques[i][j].top - sizeY;
                            vitesseY = -vitesseY*(float)COEFF_REBOND;
                            break;
                        case droite:
                            posX = (int) rectanglesCasseBriques[i][j].right;
                            vitesseX = -vitesseX*(float)COEFF_REBOND;
                            break;
                        case gauche:
                            posX = (int) rectanglesCasseBriques[i][j].left - sizeX;
                            vitesseX = -vitesseX*(float)COEFF_REBOND;
                            break;
                    }

                    //On fait disparaître l'image et on enlève le rectangle de collision
                    imagesCasseBriques[i][j].setAlpha(0.0f);
                    rectanglesCasseBriques[i][j].setEmpty();
                }
                if(!rectanglesCasseBriques[i][j].isEmpty()){
                    memFinNiveau = false;
                }
            }
        }

        posX += vitesseX * COEFF_FROTTEMENT;
        posY += vitesseY * COEFF_FROTTEMENT;


        //Version précédente
       /* actualTime = System.currentTimeMillis();
        timeSinceLastEvent = actualTime -memTime;
        memTime = actualTime;
        // v = d/t ; a = v/t
        posX += (-AccelX * ((timeSinceLastEvent * timeSinceLastEvent))/COEFF_DIV_VITESSE);
        posY += (AccelY * ((timeSinceLastEvent * timeSinceLastEvent))/COEFF_DIV_VITESSE);
        Log.d(TAG, "\n\nTime = "+timeSinceLastEvent+", AccelX = " +AccelX+ ", AccelY = " +AccelY+ ", posX = " +posX+ ", posY = " +posY);

        if(posX < 0) {
            posX = 0;
        }

        if(posY < 0) {
            posY = 0;
        }

        if(posX + sizeX > width)
        {
            posX = width - sizeX;
        }

        if(posY + 390 > height)
        {
            posY = height - 390; //Valeur 390 en "dur" car je sais pas comment prendre en compte le bandeau du bas dans les calculs

        }
*/

    if(memFinNiveau)
        return true;
    else
        return false;
    }



}
