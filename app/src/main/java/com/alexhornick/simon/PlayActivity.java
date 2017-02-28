package com.alexhornick.simon;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileOutputStream;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;


public class PlayActivity extends AppCompatActivity implements View.OnClickListener{

    enum VERSION{REPEAT,MULTI,SPEED}
    enum STATE{WATCHING,PLAYING,BEFORE}
    VERSION version=VERSION.REPEAT;
    STATE gameState=STATE.BEFORE;

    private boolean player=false;
    int numOn=0;
    int score=0;
    int highScore[] = {0,0,0};
    Sequencer mysequence;
    private int buttonIds[]={R.id.simon1,R.id.simon2,R.id.simon3,R.id.simon4};
    playButton Task;
    playButton buttonTask;
    private SoundPool soundPool;
    private Set<Integer> soundsLoaded;
    private int[] notes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);

        //When soundsLoaded is empty, set it equal to a new HashSet of Integers containing the sound ids
        if(soundsLoaded==null)
        soundsLoaded = new HashSet<Integer>();

        //notes contain the soundIds that can be used to play the sound
        if(notes==null)
            notes = new int[4];

        Intent intent=getIntent();
        //Get version based on spinner from activity_main
        if(intent.hasExtra("version")){
            int temp=intent.getIntExtra("version",0);
            if(temp==0){
                version=VERSION.REPEAT; //enum for Repeat Version
            }
            else if(temp==1){
                version=VERSION.MULTI; //enum for Multi Version
            }
            else{
                version=VERSION.SPEED; //enum for Speed version
            }
            Toast.makeText(this,"Version "+version.toString(),Toast.LENGTH_SHORT).show(); //display version selected to user
        }
        if(mysequence==null){
            mysequence=new Sequencer(); //sequencer controls the Simon pattern
        }

        for(int ids:buttonIds){ //Set each button to the onClickListener
            ImageView im = (ImageView) findViewById(ids);
            im.setOnClickListener(this);
        }
        Button b = (Button) findViewById(R.id.start_button);
        b.setOnClickListener(this);
        getFile(); //read file containing high scores

    }
    int time=500;

    @Override
    protected void onResume() {
        super.onResume();

        //IF API >= 21, use SoundPool Builder
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            soundPool = new SoundPool.Builder()
                    .setMaxStreams(1)
                    .build();
        }

        //ELSE IF API < 21, Use deprecated SoundPool Constructor
        else
            soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 1);

        //Load sounds
        soundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                if (status == 0) {
                    soundsLoaded.add(sampleId);
                    Log.i("SOUND", "Sound loaded " + sampleId);
                } else {
                    Log.i("SOUND", "Error cannot load sound status = " + status);
                }
            }
        });

        //Load sounds into notes array
        notes[0]= soundPool.load(this, R.raw.a4, 1);
        notes[1] = soundPool.load(this, R.raw.csharp4, 1);
        notes[2] = soundPool.load(this, R.raw.e4, 1);
        notes[3] = soundPool.load(this, R.raw.g4, 1);

    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i("onStop","------OnPause");
        if(Task!=null)
            Task.cancel(true);
        if(buttonTask!=null)
            buttonTask.cancel(true);

        if (soundPool != null)
        {
            soundPool.release(); //when paused, stop sounds
            soundPool = null;
            soundsLoaded.clear();
        }

    }

    //accepts a soundId and plays that sound
    private void playSound(int soundId) {
        //check if soundId is in the sounds loaded.
        if (soundsLoaded.contains(soundId)) {
            soundPool.play(soundId, 1.0f, 1.0f, 0, 0, 1.0f);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i("onStop","------OnStop");
      }

    @Override
    public void onClick(View v) {

        if(v.getId()==R.id.start_button && gameState==STATE.BEFORE){
            startGame(); //starts game

            }
        else if(gameState==STATE.PLAYING&&!(v.getId()==R.id.start_button)) {
            ImageView im = (ImageView) v;

            int temp=0;
            if (im.getId() == R.id.simon1)
                temp=1;
            if (im.getId() == R.id.simon2)
                temp=2;
            if (im.getId() == R.id.simon3)
                temp=3;
            if (im.getId() == R.id.simon4)
                temp=4;

            Log.i("Number","-----"+numOn+" "+mysequence.pattern.size());
            if(numOn>mysequence.pattern.size()-1) {
                gameState = STATE.BEFORE;
                score++;
                if(version==VERSION.SPEED)
                time -= 20;
                if(time <= 100)
                {
                    time = 100;
                }

                TextView tv = (TextView) findViewById(R.id.score);
                tv.setText(String.valueOf(score));

                buttonTask = new playButton(temp);
                buttonTask.execute();

                startGame();
            }
            else if (im.getId() == R.id.simon1) {
                buttonTask = new playButton(temp);
                buttonTask.execute();
            if(mysequence.pattern.get(numOn-1)==1)
                numOn++;
            else
                restart();

            } else if (im.getId() == R.id.simon2) {
                buttonTask = new playButton(temp);
                buttonTask.execute();
                if(mysequence.pattern.get(numOn-1)==2)
                    numOn++;
                else
                    restart();
            } else if (im.getId() == R.id.simon3) {
                buttonTask = new playButton(temp);
                buttonTask.execute();
                if(mysequence.pattern.get(numOn-1)==3)
                    numOn++;
                else
                    restart();
            } else if (im.getId() == R.id.simon4) {
                buttonTask = new playButton(temp);
                buttonTask.execute();
                if(mysequence.pattern.get(numOn-1)==4)
                    numOn++;
                else
                    restart();
            }

        }
    }
public void startGame(){

    soundPool.stop(notes[0]);
    soundPool.stop(notes[1]);
    soundPool.stop(notes[2]);
    soundPool.stop(notes[3]);

    Task = new playButton(0);
    Task.execute();

}

public void restart(){

    time=500;

    //create Dialog when user loses. They can click Ok to restart
    AlertDialog.Builder builder = new AlertDialog.Builder(this);

    builder.setTitle("You lose");

    builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {

        }
    });
    builder.show();

    mysequence.pattern.clear(); //clear the old pattern because the user lost.

    //Depending on version, save the correct high score, and Toast if it's a new high score.
    if(version == VERSION.REPEAT) {
        if (score > highScore[0]) {
            highScore[0]=score;
            writeToFile();
            Toast.makeText(this, "New high score!", Toast.LENGTH_LONG).show();
            TextView tv = (TextView) findViewById(R.id.high_score);
            tv.setText("High Score: " + highScore[0]);
        }
    }
    else if(version == VERSION.MULTI) {
        if (score > highScore[1]) {
            highScore[1]=score;
            writeToFile();
            Toast.makeText(this, "New high score!", Toast.LENGTH_LONG).show();
            TextView tv = (TextView) findViewById(R.id.high_score);
            tv.setText("High Score: " + highScore[1]);
        }
    }
    else if(version == VERSION.SPEED) {
        if (score > highScore[2]) {
            highScore[2]=score;
            writeToFile();
            Toast.makeText(this, "New high score!", Toast.LENGTH_LONG).show();
            TextView tv = (TextView) findViewById(R.id.high_score);
            tv.setText("High Score: " + highScore[2]);
        }
    }
    score=0; //reset score to 0
    TextView tv = (TextView) findViewById(R.id.score);
    tv.setText(String.valueOf(score));

    gameState=STATE.BEFORE; //change gameState

}
    //This function retrieves and reads the file that stores the high scores for each version
    protected void getFile(){
        try {
            FileInputStream fis = openFileInput("simon_highscore.txt");
            Scanner s = new Scanner(fis);

            highScore[0] = s.nextInt(); //Get high score for repeat
            highScore[1] = s.nextInt(); //Get high score for multi
            highScore[2] = s.nextInt(); //Get high score for speed

            s.close();
        } catch (FileNotFoundException e){
            Log.i("ReadData", "No input file found");
        }

        TextView tv = (TextView) findViewById(R.id.high_score);

        switch (version)
        {
            case REPEAT:
                tv.setText("High Score: " + highScore[0]);
                break;
            case MULTI:
                tv.setText("High Score: " + highScore[1]);
                break;
            case SPEED:
                tv.setText("High Score: " + highScore[2]);
                break;
            default:
                break;
        }
    }

    //Called when app stops. If there's a high score, it will save to a text file for the particular version the user is playing
    protected void writeToFile() {
        try {
            FileOutputStream fos = openFileOutput("simon_highscore.txt", Context.MODE_PRIVATE);
            OutputStreamWriter osw = new OutputStreamWriter(fos);
            BufferedWriter bw = new BufferedWriter(osw);
            PrintWriter pw = new PrintWriter(bw);

            pw.println(highScore[0]); //Repeat version high score
            pw.println(highScore[1]); //Multi version high score
            pw.println(highScore[2]); //Speed high score
            pw.println(score); //write high score to file.
            pw.close();
        } catch (FileNotFoundException e){
            e.printStackTrace();
            Toast.makeText(this, "Unable to save high score", Toast.LENGTH_LONG).show();
        }
    }

    //PlayButton Class uses AsyncTask to manage the Simon patterns and sounds
public class playButton extends AsyncTask<Void,Integer,Void>{
    int type;

    public playButton(int type1){
        type=type1;
    }
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected Void doInBackground(Void... params) {

       if(type==0) {
           gameState = STATE.WATCHING;

           try {
               Thread.sleep(time);
           } catch (InterruptedException e) {
               e.printStackTrace();
           }

           numOn = 1;

           if(version==VERSION.REPEAT) {
               //get next pattern
               int temp1 = mysequence.nextPattern();
               while (mysequence.pattern.size() >= 1 && temp1 == mysequence.pattern.get(mysequence.pattern.size() - 1))
                   temp1 = mysequence.nextPattern();

               mysequence.pattern.add(temp1);
           }
           else if(version==VERSION.MULTI){
               int j = mysequence.nextPattern();
               int temp1 = mysequence.nextPattern();
               for(int i=0;i<j;i++) {
                   mysequence.pattern.add(temp1); //Add multiples of the pattern selection
               }
           }
           else if(version == VERSION.SPEED) {
               int temp1 = mysequence.nextPattern();
               while (mysequence.pattern.size() >= 1 && temp1 == mysequence.pattern.get(mysequence.pattern.size() - 1))
                   temp1 = mysequence.nextPattern();

               mysequence.pattern.add(temp1);
           }
           if(version==VERSION.SPEED) {
               int temp1 = mysequence.nextPattern();
               while (mysequence.pattern.size() >= 1 && temp1 == mysequence.pattern.get(mysequence.pattern.size() - 1))
                   temp1 = mysequence.nextPattern();

               mysequence.pattern.add(temp1);
           }


           for (int i = 0; i < mysequence.pattern.size(); i++) {

               final int temp = mysequence.pattern.get(i);

               //sends 0 and temp which holds the pattern values from 1 - 4
               publishProgress(0, temp);

               try {
                   Thread.sleep(time);
               } catch (InterruptedException e) {
                   e.printStackTrace();
               }

               if (isCancelled())
                   break;

               //Sends 1 and the pattern value to onProgressUpdate to unset the button color overlay
               publishProgress(1, temp);


               if(time<200){
               try {
                   Thread.sleep(300);
               } catch (InterruptedException e) {
                   e.printStackTrace();
               }}


           }
           //set game state to Playing
           gameState = STATE.PLAYING;
       }
       else{
        for(int i=0;i<1;i++) {
            int temp = type;
            publishProgress(0, temp);

            try {
                Thread.sleep(time);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            publishProgress(1, temp);

        }
       }
        return null;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {

        //Stop all 4 sounds from playing
        soundPool.stop(notes[0]);
        soundPool.stop(notes[1]);
        soundPool.stop(notes[2]);
        soundPool.stop(notes[3]);

        //If first parameter is 0, set the color overlay
        if(values[0]==0){
        ImageView im = (ImageView) findViewById(buttonIds[values[1]-1]);
            //set button color overlay when they are pressed
            switch(buttonIds[values[1]-1])
            {
                case R.id.simon1:
                    im.setColorFilter(Color.CYAN); //Cyan for blue button
                    break;
                case R.id.simon2:
                    im.setColorFilter(Color.rgb(255, 102, 102)); //red
                    break;
                case R.id.simon4:
                    im.setColorFilter(Color.rgb(255, 255, 201)); //yellow
                    break;
                case R.id.simon3:
                    im.setColorFilter(Color.rgb(123, 255, 159)); //green
                    break;
                default:
                    im.setColorFilter(0xffffffff); //default white overlay
                    break;
            }

            Log.i("Number", "-----In set color");

            //Play correct sound depending on what image was clicked
        switch (values[1]) {
            case 1:
                playSound(notes[0]);
                break;
            case 2:
                playSound(notes[1]);
                break;
            case 3:
                playSound(notes[2]);
                break;
            case 4:
                playSound(notes[3]);
                break;
        }
    }
        //when second parameter is 1, unset color overlay for button
    else if(values[0]==1){

            Log.i("Number", "-----In unset color");
        ImageView im = (ImageView) findViewById(buttonIds[values[1]-1]);
        im.setColorFilter(0x00000000); //set color overlay to nothing to show original color
    }
    }

    @Override
    protected void onCancelled() {
        for(int i=0;i<4;i++){
        ImageView im = (ImageView) findViewById(buttonIds[i]);
        im.setColorFilter(0x00000000);} //for each image button, set color overlay to nothing
        Task=null;
        super.onCancelled();
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
    }
}
}
