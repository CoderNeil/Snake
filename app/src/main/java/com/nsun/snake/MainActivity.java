package com.nsun.snake;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;


public class MainActivity extends Activity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        View startButton = findViewById(R.id.start_button);
        startButton.setOnClickListener(this);
        View exitButton = findViewById(R.id.exit_button);
        exitButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.start_button){
            startGame();
        }
        else if(v.getId() == R.id.exit_button){
            this.finish();
        }
    }
    private void startGame(){
        Intent start = new Intent(this, Snake.class);
        startActivity(start);
    }
}
