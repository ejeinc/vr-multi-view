package com.eje_c.vrmultiview.common;

public class ControlMessage {
    public static final int STATE_STOP = 0;
    public static final int STATE_PLAY = 1;

    public String path = "Oculus/360Videos/video.mp4";
    public int seek = 0;
    public int state = STATE_STOP;

    @Override
    public String toString() {
        return JSON.stringify(this);
    }
}
