package com.aldebaran.demo.example;

import com.aldebaran.demo.StartInterface;
import com.aldebaran.qimessaging.Application;
import com.aldebaran.qimessaging.CallError;
import com.aldebaran.qimessaging.Future;
import com.aldebaran.qimessaging.Session;
import com.aldebaran.qimessaging.helpers.al.ALMemory;
import com.aldebaran.qimessaging.helpers.al.ALMotion;
import com.aldebaran.qimessaging.helpers.al.ALSoundLocalization;

import java.util.ArrayList;

/**
 * Created by epinault on 11/05/2014.
 */
public class ExReactToSound implements StartInterface {

    private Application application;
    private boolean isMoving = false;


    private ALMemory alMemory;
    private ALSoundLocalization sound;
    private ALMotion motion;

    @Override
    public void start(String ip, String port) {
        application = new Application();
        Session session = new Session();
        Future<Void> future = null;
        try {
	        future = session.connect("tcp://"+ip+":"+port);

            synchronized (future) {
                future.wait(1000);
            }


            alMemory = new ALMemory(session);
            sound = new ALSoundLocalization(session);
            motion = new ALMotion(session);
//            new ALMotion(session).wakeUp();
            sound.subscribe("demo");
            alMemory.subscribeToEvent("ALSoundLocalization/SoundLocated", "onSoundLocated::(m)", this);
            alMemory.subscribeToEvent("MiddleTactilTouched", "onEnd::(f)", this);

            application.run();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void onEnd(Float touch) throws InterruptedException, CallError {
        if (touch == 1.0) {
            sound.unsubscribe("demo");
            motion.rest();
            application.stop();
        }
    }


    @SuppressWarnings("unchecked")
    public void onSoundLocated(Object parameters) throws InterruptedException, CallError {
        try {
            if (parameters != null && parameters instanceof ArrayList<?>) {
                ArrayList<?> soundParams = (ArrayList<?>) parameters;
                if (soundParams.size() >= 2) {

                    ArrayList<Float> soundParam = (ArrayList<Float>) soundParams.get(1);
                    ArrayList<Float> torsoCoord = (ArrayList<Float>) soundParams.get(2);
                    if (soundParam.size() >= 4) {
                        float az = soundParam.get(0) + torsoCoord.get(5);
                        float ey = soundParam.get(1) + torsoCoord.get(4);
                        System.out.println(" Level: " + soundParam.get(3)+ "isMoving "+ motion.moveIsActive());

                        if (soundParam.get(3) > 5) {
                            if( !motion.moveIsActive() ) {
                                if(az > 0)
                                    az = 0.4f;
                                else
                                    az = -0.4f;

                                if(ey > 0)
                                    ey = 0.4f;
                                else
                                    ey = -0.4f;
                                motion.moveToward(az, ey, 0f);
                            }
                        }
                    }
                }
            }
        } catch (ClassCastException e) {
            e.printStackTrace();
        }
    }
}
