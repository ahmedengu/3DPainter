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
    ArrayList<float[]> lineFloats = new ArrayList<>();
    int drawShape = 0;
    int drawSize = 18;
    float w = 256;
    boolean[] flip = {false, true, false};
    Robot robot;
    boolean drawPainter = true;
    boolean mousePlay = true;

    public void settings() {
        size(1366, 768, OPENGL);
    }

    public void setup() {
        frameRate(25);
        Game.x = (int) random(width);
        Game.y = height - Game.base;
        Game.sketch = this;
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
        if (drawPainter)
            drawBoard();
        else {
            camera();

            if (mousePlay) {
                float sw = w / div;
                float sd = sw * (div - 1);
                float x = axyz[0].avg * sd;
                float y = axyz[1].avg * sd;

                robot.mouseMove((int) x * 9, 300);
            }
            if (keyPressed && key == '-') {
                mousePlay = false;
            } else if (keyPressed && key == '+') {
                mousePlay = true;
            }
            Game.gameDraw();
        }
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
            if (lineFloats.get(i)[6] == 0)
                sphere(lineFloats.get(i)[7]);
            else if (lineFloats.get(i)[6] == 1)
                box(lineFloats.get(i)[7]);
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

        stroke(0, 100);
        if (drawShape == 0)
            sphere(drawSize);
        else if (drawShape == 1)
            box(drawSize);
        popMatrix();

        if (keyPressed) {
            if (key == 'n' || key == 'N') {
                reset();
            } else if (key == 'm' || key == 'M') {
                robot.mouseMove((int) x * 5, (int) y * 5);
            } else if (key == 'c' || key == 'C') {
                msg("Calibrate!");
            } else if (key == 's' || key == 'S') {
                drawShape = (drawShape == 0) ? 1 : 0;
            } else if (key == 'p' || key == 'P') {
                save("pic-" + System.currentTimeMillis() + ".jpeg");
            } else if (key == '-') {
                if (drawSize > 1)
                    drawSize--;
            } else if (key == '+') {
                if (drawSize < 60)
                    drawSize++;
            } else if (key == 'f' || key == 'F') {
                drawPainter = false;
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
                                color[2],
                                drawShape,
                                drawSize});
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
        lineFloats.clear();
    }

    void msg(String msg) {
        println(msg);
    }
}
