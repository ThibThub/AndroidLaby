package fr.ensea.tp_labyby;

import android.app.Service;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import fr.ensea.tp_labyby.jeuActivity;


public class Accelero {

    private final static String TAG = "debugAccelero";
    private float X;
    private float Y;

    private jeuActivity jeu; //nécessaire pour appeler les fonctions de la classe JeuActivity appelées
    private SensorManager mManager;
    private Sensor mAccelerometre;

    public Accelero(jeuActivity jeuA){ //Constructeur récupérant la valeur instanciée de jeuActivitys
        this.jeu = jeuA;
        mManager = (SensorManager) jeu.getBaseContext().getSystemService(Service.SENSOR_SERVICE);
        mAccelerometre = mManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

    }

    SensorEventListener mSensorEventListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent pEvent) {
            X = pEvent.values[0];
            Y = pEvent.values[1];
            //Log.d(TAG, "new sensor values: X=" + X + " Y=" + Y);
        }

        @Override
        public void onAccuracyChanged(Sensor pSensor, int pAccuracy) {

        }
    };

    void startAcceleroListener(){ //A appeler dans jeuActivity (sur onResume par exemple)
        mManager.registerListener(mSensorEventListener, mAccelerometre, SensorManager.SENSOR_DELAY_GAME);
    }

    void stopAcceleroListener(){ //A appeler dans jeuActivity (sur onPause par exemple)
        mManager.unregisterListener(mSensorEventListener, mAccelerometre);
    }

    public float getAccelX(){
        return this.X;
    }

    public float getAccelY(){
        return this.Y;
    }


}
