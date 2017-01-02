import processing.core.PApplet;
import processing.serial.Serial;

import java.awt.*;
import java.util.ArrayList;

public class Sketch extends PApplet {
    Serial serial;
    int serialPort = 0;

    static final int SENSORS = 3;
    int div = 3;

    Normalize n[] = new Normalize[SENSORS];
    MomentumAverage cama[] = new MomentumAverage[SENSORS];
    MomentumAverage axyz[] = new MomentumAverage[SENSORS];
    int[][][] lineXYZ = new int[256][256][256];
    ArrayList<float[]> lineFloats = new ArrayList<>();
    float w = 256;
    boolean[] flip = {false, true, false};
    Robot robot;

    public void settings() {
        size(800, 600, OPENGL);
    }

    public void setup() {
        frameRate(25);

        textMode(SHAPE);

        String[] serialList = Serial.list();
        if (serialList.length > 0) {
            println(serialList);
            serial = new Serial(this, serialList[serialPort], 115200);
        }
        for (int i = 0; i < SENSORS; i++) {
            n[i] = new Normalize();
            cama[i] = new MomentumAverage(.01f);
            axyz[i] = new MomentumAverage(.15f);
        }
        try {
            robot = new Robot();
        } catch (AWTException e) {
            e.printStackTrace();
        }
        reset();

    }

    public void draw() {
        updateSerial();
        drawBoard();
    }

    void updateSerial() {
        String cur = serial.readStringUntil('\n');
        if (cur != null) {
            String[] parts = split(cur, " ");
            if (parts.length == SENSORS) {
                float[] xyz = new float[SENSORS];
                for (int i = 0; i < SENSORS; i++)
                    xyz[i] = Float.parseFloat(parts[i]);

                if (keyPressed && (key == 'c' || key == 'C'))
                    for (int i = 0; i < SENSORS; i++)
                        n[i].note(xyz[i]);

                for (int i = 0; i < SENSORS; i++) {
                    float raw = n[i].choose(xyz[i]);
                    float v = flip[i] ? 1 - raw : raw;
                    cama[i].note(v);
                    axyz[i].note(v);
                }
            }
        }
    }

    void drawBoard() {
        background(255);

        float h = w / 2;
        camera(
                h + (cama[0].avg - cama[2].avg) * h,
                h + (cama[1].avg - 1) * height / 2,
                w * 2,
                h, h, h,
                0, 1, 0);

        pushMatrix();

        noFill();
        stroke(0, 40);
        translate(w / 2, w / 2, w / 2);
        rotateY(-HALF_PI / 2);
        box(w);
        popMatrix();
        float sw = w / div;
        translate(h, sw / 2, 0);
        rotateY(-HALF_PI / 2);

        for (int i = 0; i < lineFloats.size(); i++) {
            pushMatrix();
            translate(
                    lineFloats.get(i)[0],
                    lineFloats.get(i)[1],
                    lineFloats.get(i)[2]);
            fill(lineFloats.get(i)[3], lineFloats.get(i)[4], lineFloats.get(i)[5], 200);
            noStroke();
            sphere(10);
            popMatrix();
        }

        pushMatrix();
        float sd = sw * (div - 1);
        float x = axyz[0].avg * sd;
        float y = axyz[1].avg * sd;
        float z = axyz[2].avg * sd;
        translate(
                x,
                y,
                z);
        fill(255, 160, 0, 200);
        noStroke();
        sphere(18);
        popMatrix();

        if (keyPressed) {
            if (key == 'n' || key == 'N') {
                reset();
            } else if (key == 'm' || key == 'M') {
                robot.mouseMove((int) x * 5, (int) y * 5);
            } else if (key == 'c' || key == 'C') {
                msg("Calibrate!");
            } else {
                int index = -1;
                for (int i = 0; i < lineFloats.size(); i++) {
                    if (lineFloats.get(i)[0] == x && lineFloats.get(i)[1] == y && lineFloats.get(i)[2] == z) {
                        index = i;
                        break;
                    }
                }
                if (index == -1) {
                    int color[];
                    switch (key) {
                        case 'r':
                        case 'R':
                            color = new int[]{255, 0, 0};
                            break;
                        case 'b':
                        case 'B':
                            color = new int[]{0, 0, 255};
                            break;
                        case 'g':
                        case 'G':
                            color = new int[]{0, 255, 0};
                            break;
                        case 'k':
                        case 'K':
                            color = new int[]{0, 0, 0};
                            break;
                        case 'w':
                        case 'W':
                            color = new int[]{255, 255, 255};
                            break;
                        default:
                            color = null;
                    }
                    if (color != null)
                        lineFloats.add(new float[]{
                                x,
                                y,
                                z,
                                color[0],
                                color[1],
                                color[2]});
                } else {
                    switch (key) {
                        case 'd':
                        case 'D':
                            lineFloats.remove(index);
                            break;
                    }
                }
            }
        }
    }


    void reset() {
        for (int i = 0; i < SENSORS; i++) {
            n[i].reset();
            cama[i].reset();
            axyz[i].reset();
        }
    }

    void msg(String msg) {
        println(msg);
    }
}
