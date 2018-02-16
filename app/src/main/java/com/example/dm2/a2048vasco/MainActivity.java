package com.example.dm2.a2048vasco;

import android.app.Activity;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends Activity {

    private int score = 0;
    private TextView tvScore;
    private TextView tvBestScore;
    private LinearLayout root = null;
    private Button btnNewGame;
    private GameView gameView;
    private AnimLayer animLayer = null;
    private static MainActivity mainActivity = null;
    public static String SP_KEY_BEST_SCORE = "bestScore";

    //Getters
    public static MainActivity getMainActivity() {
        return mainActivity;
    }

    public AnimLayer getAnimLayer() {
        return animLayer;
    }


    public MainActivity() {
        mainActivity = this;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        root = (LinearLayout) findViewById(R.id.container);
        root.setBackgroundColor(0xfffaf8ef);

        tvScore = (TextView) findViewById(R.id.tvScore);
        tvBestScore = (TextView) findViewById(R.id.tvBestScore);

        gameView = (GameView) findViewById(R.id.gameView);

        btnNewGame = (Button) findViewById(R.id.btnNewGame);
        btnNewGame.setOnClickListener(new View.OnClickListener() {@Override public void onClick(View v) {
            gameView.startGame();
        }});

        animLayer = (AnimLayer) findViewById(R.id.animLayer);
    }

    //Reinicia la puntuación
    public void clearScore(){
        score = 0;
        showScore();
    }

    //Actualiza la puntuación
    public void showScore(){
        tvScore.setText(score+"");
    }

    //Añade los puntos conseguidos y comprueba si se ha superado la puntuación máxima
    public void addScore(int s){
        score+=s;
        showScore();

        int maxScore = Math.max(score, getBestScore());
        saveBestScore(maxScore);
        showBestScore(maxScore);
    }

    //Guarda la puntuación máxima
    public void saveBestScore(int s){
        Editor e = getPreferences(MODE_PRIVATE).edit();
        e.putInt(SP_KEY_BEST_SCORE, s);
        e.commit();
    }

    //Recupera la puntuación máxima
    public int getBestScore(){
        return getPreferences(MODE_PRIVATE).getInt(SP_KEY_BEST_SCORE, 0);
    }

    //Muestra la puntuación máxima
    public void showBestScore(int s){
        tvBestScore.setText(s+"");
    }

}
