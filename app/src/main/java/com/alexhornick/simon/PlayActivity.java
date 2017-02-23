package com.alexhornick.simon;

import android.content.DialogInterface;
import android.content.Intent;
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

import java.util.HashSet;
import java.util.Set;


public class PlayActivity extends AppCompatActivity implements View.OnClickListener{

    enum VERSION{REPEAT,MULTI,VERSUS}
    enum STATE{WATCHING,PLAYING,BEFORE}
    VERSION version=VERSION.REPEAT;
    STATE gameState=STATE.BEFORE;

    private boolean player=false;
    int numOn=0;
    int score=0;
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

        if(soundsLoaded==null)
        soundsLoaded = new HashSet<Integer>();

        if(notes==null)
            notes = new int[4];

        Intent intent=getIntent();
        if(intent.hasExtra("version")){
            int temp=intent.getIntExtra("version",0);
            if(temp==0){
                version=VERSION.REPEAT;
            }
            else if(temp==1){
                version=VERSION.MULTI;
            }
            else{
                version=VERSION.VERSUS;
            }
            Toast.makeText(this,"Version "+version.toString(),Toast.LENGTH_SHORT).show();
        }
        if(mysequence==null){
            mysequence=new Sequencer();
        }

        for(int ids:buttonIds){
            ImageView im = (ImageView) findViewById(ids);
            im.setOnClickListener(this);
        }
        Button b = (Button) findViewById(R.id.start_button);
        b.setOnClickListener(this);

    }
    int time=0;

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
            soundPool.release();
            soundPool = null;
            soundsLoaded.clear();
        }

    }

    private void playSound(int soundId) {
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
            startGame();

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
    AlertDialog.Builder builder = new AlertDialog.Builder(this);

    builder.setTitle("You lose");

    builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {

        }
    });
    builder.show();

    mysequence.pattern.clear();

    score=0;
    TextView tv = (TextView) findViewById(R.id.score);
    tv.setText(String.valueOf(score));

    gameState=STATE.BEFORE;

}
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
               Thread.sleep(1000);
           } catch (InterruptedException e) {
               e.printStackTrace();
           }

           numOn = 1;

           if(version==VERSION.REPEAT) {
               int temp1 = mysequence.nextPattern();
               while (mysequence.pattern.size() >= 1 && temp1 == mysequence.pattern.get(mysequence.pattern.size() - 1))
                   temp1 = mysequence.nextPattern();

               mysequence.pattern.add(temp1);
           }
           else if(version==VERSION.MULTI){
               int j = mysequence.nextPattern();
               int temp1 = mysequence.nextPattern();
               for(int i=0;i<j;i++) {
                   mysequence.pattern.add(temp1);
               }
           }
           time = 0;

           for (int i = 0; i < mysequence.pattern.size(); i++) {

               final int temp = mysequence.pattern.get(i);
               //Toast.makeText(getApplicationContext(),temp+" ",Toast.LENGTH_SHORT).show();

               publishProgress(0, temp);

               try {
                   Thread.sleep(500);
               } catch (InterruptedException e) {
                   e.printStackTrace();
               }

               if (isCancelled())
                   break;


               publishProgress(1, temp);


           }
           gameState = STATE.PLAYING;
       }
       else{
        for(int i=0;i<1;i++) {
            int temp = type;
            publishProgress(0, temp);

            try {
                Thread.sleep(500);
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

        soundPool.stop(notes[0]);
        soundPool.stop(notes[1]);
        soundPool.stop(notes[2]);
        soundPool.stop(notes[3]);


        if(values[0]==0){
        ImageView im = (ImageView) findViewById(buttonIds[values[1]-1]);
        im.setColorFilter(0xffffffff);

            Log.i("Number", "-----In set color");
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
    else if(values[0]==1){

            Log.i("Number", "-----In unset color");
        ImageView im = (ImageView) findViewById(buttonIds[values[1]-1]);
        im.setColorFilter(0x00000000);
    }
    }

    @Override
    protected void onCancelled() {
        for(int i=0;i<4;i++){
        ImageView im = (ImageView) findViewById(buttonIds[i]);
        im.setColorFilter(0x00000000);}
        Task=null;
        super.onCancelled();
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
    }
}
}
