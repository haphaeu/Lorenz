/*
 * Lorenz Oscillator
 *
 *
 *
 */
package lorenz;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import static java.lang.Math.abs;
import static java.lang.Math.pow;
import static java.lang.Math.sqrt;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 *
 * @author raf
 */
public class Lorenz implements KeyListener,
                                MouseListener,
                                MouseMotionListener,
                                MouseWheelListener {

    MyDrawPanel panel;

    boolean debug = false;

    boolean paused = false;
    boolean showTimers = true;
    int timer = 5; // ms
    long updateOrbitTime, repaintTime; //ns
    int proc_time;  // ms, time required to process and repaint
    int frame_time;  // ms, time to process, draw a frame

    // Indices to plot a 3D vector in a 2D screen.
    // 0: x, 1: y, 3: z
    // So default plots x-y plane. keyevents will change this.
    int idxViewX = 0;
    int idxViewY = 1;


    double rho = 28.0;
    double sigma = 10.0;
    double beta = 8.0 / 3.0;
    double dt = 0.01;

    boolean showOrbits = true;
    int size = 5000;
    double[][] orbit;
    int orbitPoints;

    double scale;
    int shiftX, shiftY;


    public static void main(String[] args) {
        System.out.println("main()");
        if (args.length > 1) {
            System.out.println("  args:");
            for(String s: args)
                System.out.println("  " + s);
        }
        Lorenz game = new Lorenz();

        game.setup();
    }

    public void setup() {
        System.out.println("setup()");
        JFrame frame = new JFrame("Lorenz Oscillator");
        panel = new MyDrawPanel();
        frame.getContentPane().add(panel);
        frame.addKeyListener(this);
        frame.addMouseListener(this);
        frame.addMouseMotionListener(this);
        frame.addMouseWheelListener(this);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setResizable(true);
        frame.setVisible(true);
        rescale();
        paused = true;
        start();
    }

    private void rescale() {
        int w = panel.getWidth();
        int h = panel.getHeight();
        scale = 0.2;
        shiftX = w/2;
        shiftY = h/2;
    }

    private void start() {
        System.out.println("start()");
        System.out.println(panel.getWidth() + " " + panel.getHeight());
        long t0, t1, t2;
        int i;

        double x, y, z;
        x = 1.0;
        y = 1.0;
        z = 1.0;

        orbit = new double[size][3];
        orbitPoints = 0;
        orbit[orbitPoints][0] = x;
        orbit[orbitPoints][1] = y;
        orbit[orbitPoints][2] = z;
        orbitPoints++;

        while (true) {
            t0 = t1 = t2 = System.nanoTime();
            if (!paused) {

                t1 = System.nanoTime();

                // update positions
                x += dt * sigma * (y - x);
                y += dt * (x * (rho - z) - y);
                z += dt * (x * y - beta * z);

                checkOrbitSize();
                orbit[orbitPoints][0] = x;
                orbit[orbitPoints][1] = y;
                orbit[orbitPoints][2] = z;
                orbitPoints++;

                t2 = System.nanoTime();
            }
            updateOrbitTime = t2 - t1;

            panel.repaint();

            proc_time = (int)((System.nanoTime() - t0) / 1e6);

            try {
                Thread.sleep(timer - Math.min(timer, proc_time));
            } catch (InterruptedException ex) { }
        }
    }

    void checkOrbitSize() {
        if (orbitPoints == orbit.length - 1) {
            System.out.println("Increasing orbit array size...");
            double[][] tmp = new double[orbit.length + size][3];
            System.arraycopy(orbit, 0, tmp, 0, orbit.length);
            orbit = tmp;
        }
    }

    // KeyPressed interface
    int newPlanetX, newPlanetY;
    @Override
    public synchronized void keyPressed(KeyEvent ev) {
        System.out.print("Key pressed: ");

        switch (ev.getKeyCode()) {
            case KeyEvent.VK_O:
                System.out.println("O");
                showOrbits = !showOrbits;
                System.out.println("   showOrbit " + showOrbits);
                panel.repaint();
                break;
            case KeyEvent.VK_E:
                System.out.println("E");
                System.out.println("   erase orbits ");
                orbitPoints = 0;
                panel.repaint();
                break;
            case KeyEvent.VK_P:
                System.out.println("P");
                paused = !paused;
                System.out.println("   paused " + paused);
                break;
            case KeyEvent.VK_T:
                System.out.println("T");
                showTimers = !showTimers;
                System.out.println("   showTimers " + showTimers);
                break;
            case KeyEvent.VK_X:
                System.out.println("X");
                System.out.println("   view y-z plane ");
                idxViewX = 1;
                idxViewY = 2;
                break;
            case KeyEvent.VK_Y:
                System.out.println("Y");
                System.out.println("   view x-z plane ");
                idxViewX = 0;
                idxViewY = 2;
                break;
            case KeyEvent.VK_Z:
                System.out.println("Z");
                System.out.println("   view x-y plane ");
                idxViewX = 0;
                idxViewY = 1;
                break;
            case KeyEvent.VK_UP:
                System.out.println("up");
                System.out.println("   rho up ");
                rho += 0.1;
                break;
            case KeyEvent.VK_DOWN:
                System.out.println("down");
                System.out.println("   rho  down ");
                rho -= 0.1;
                break;
            case KeyEvent.VK_LEFT:
                System.out.println("left");
                System.out.println("   sigma down ");
                sigma -= 0.1;
                break;
            case KeyEvent.VK_RIGHT:
                System.out.println("right");
                System.out.println("   sigma up ");
                sigma += 0.1;
                break;
            default:
                System.out.println("not implemented");
                break;
        }
    }
    @Override public void keyReleased(KeyEvent ev) {}
    @Override public void keyTyped(KeyEvent ev) {}

    // MouseMotionListener interface
    int mouseX, mouseY;
    int newPlanetSpeedX, newPlanetSpeedY;
    double velX, velY;
    @Override
    public void mouseMoved(MouseEvent e){
        mouseX = e.getPoint().x - 1;
        mouseY = e.getPoint().y - 24;
    }

    // MouseWheelInterface
    @Override public void mouseWheelMoved(MouseWheelEvent e) {
        int cX = (int)((mouseX-shiftX)*scale);
        int cY = (int)((mouseY-shiftY)*scale);
        int notches = e.getWheelRotation();
        if (notches > 0)
            scale *= 1.1;
        else if (notches < 0)
            scale /= 1.1;
        shiftX = mouseX - (int)(cX/scale);
        shiftY = mouseY - (int)(cY/scale);
        System.out.println("Rescale " + scale);
        System.out.println(" Shift " + shiftX + " " + shiftY);
    }

    // MouseListener

    @Override public void mouseClicked(MouseEvent me) {}

    int panX, panY;
    boolean panMode = false;
    @Override public void mousePressed(MouseEvent me) {
        panX = me.getX();
        panY = me.getY();
    }
    @Override public void mouseDragged(MouseEvent e) {
            shiftX += e.getX() - panX;
            shiftY += e.getY() - panY;
            panX = e.getX();
            panY = e.getY();
    }
    @Override public void mouseReleased(MouseEvent me) {}

    @Override public void mouseEntered(MouseEvent me) {}

    @Override public void mouseExited(MouseEvent me) {}

    // drawings
    class MyDrawPanel extends JPanel {
        @Override
        public void paintComponent(Graphics gfx) {
            long t0 = System.nanoTime();
            int w = this.getWidth();
            int h = this.getHeight();
            int radius = 1;

            gfx.fillRect(0, 0, w, h);
            gfx.setColor(Color.blue);
            int r = (int)(radius / scale);
            gfx.fillOval((int)((orbit[orbitPoints-1][idxViewX] - radius) / scale + shiftX),
                         (int)((orbit[orbitPoints-1][idxViewY] - radius) / scale + shiftY),
                         2*r, 2*r);

            if (showOrbits) {
                gfx.setColor(Color.gray);
                for (int i=0; i < orbitPoints-1; i++) {
                    gfx.drawLine((int)(orbit[i  ][idxViewX]/scale + shiftX),
                                 (int)(orbit[i  ][idxViewY]/scale + shiftY),
                                 (int)(orbit[i+1][idxViewX]/scale + shiftX),
                                 (int)(orbit[i+1][idxViewY]/scale + shiftY));
                }
            }

            gfx.setColor(Color.white);
            gfx.drawString(String.format("Scale %.1f", scale), 10, 10);
            if (showTimers) {
                gfx.drawString("Mouse " + mouseX + " " + mouseY, 10, h-10);
                gfx.drawString(String.format("fps %.1f", 1000.0/Math.max(proc_time,timer)), 10, h - 25);
                gfx.drawString(String.format("update orbits %5.1fms", updateOrbitTime/1e6), 10, h - 40);
                gfx.drawString(String.format("repaint %5.1fms", repaintTime/1e6), 10, h - 55);

            }

            gfx.drawString("Oscillator parameters", w-130, h-55);
            gfx.drawString(String.format("rho %.2f", rho), w-130, h-40);
            gfx.drawString(String.format("sigma %.2f", sigma), w-130, h-25);
            gfx.drawString(String.format("beta %.2f", beta), w-130, h-10);

            repaintTime = System.nanoTime() - t0;
        }
    }
}
