package com.alexhornick.simon;

import java.util.List;
import java.util.Random;
import java.util.Vector;

/**
 * Created by Thomas on 2/20/2017.
 */

public class Sequencer {
    Random rand;
    List<Integer> pattern;

    public Sequencer(){
        rand=new Random();
        pattern = new Vector<>();
    }
    int nextPattern(){
    return rand.nextInt(4)+1;
    }
    public int getSize(){
        return pattern.size();
    }
}
