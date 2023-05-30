package ru.spbstu.planetarysystem;

import android.content.Context;
import android.view.View;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

public class KeplerRunner extends View {
    private static final String TAG = "ANIM";         // Diagnostic label

    // class constants defining state of the thread
    private static final int DONE = 0;
    private static final int RUNNING = 1;

    private static final int ORBIT_COLOR = Color.argb(255, 66, 66, 66);
    private static final int PLANET_COLOR = Color.argb(255, 117, 118, 248);
    private static final int LABEL_COLOR = Color.argb(255, 255, 255, 255);
    private static final int SUN_COLOR = Color.YELLOW;
    private static int nsteps = 600;             // number animation steps around orbit
    private static int timeScale = 1;
    private static final int numpoints = 100;           // number points used to draw orbit as line segments
    // angular increment for orbit straight-line segment
    private static final float dphi = (float) (2 * Math.PI / (float) numpoints);
    private static final double THIRD = 1.0 / 3.0;
    private static final int planetRadius = 7;         // radius of spherical planet (pixels)
    private static int sunRadius = 12;            // radius of sun (pixels)
    private static final float X0 = 0;                 // x offset from center (pixels)
    private static final float Y0 = 0;                 // y offset from center (pixels)
    private static double direction = -1;        // Orbit direction: counter-clockwise -1; clockwise +1
    private static final double fracWidth = 0.95;      // Fraction of screen width to use for display
    private static final int numObjects = 12;          // Number of bodies to include (max = 12)

      /* Data for 8 planets, dwarf planet Pluto, 2 Apollo (Earth-crossing) asteroids,  and Halley's
      Comet.The semimajor elliptical axis a is in astronomical units (AU),  eccentricity epsilon is
      dimensionless,  period is in years, and theta0 (initial angle) is in radians, with
      clockwise positive and measured from the 12-o'clock position.  orientDeg[] is the relative
      orientation of the ellipse in degrees.  The variable retroFac controls whether the motion is
      prograde (+1) or retrograde (-1). The relative orientations of the ellipses were eyeballed
      from a plot, so are only approximately correct.  Likewise, the initial orientation angles theta0
      were eyeballed from plots.
      The period and semimajor axis length are not independent, being related by Kepler's 3rd
      law T = a^{3/2} in these units.  Let's include them as separate static final arrays for
      computational efficiency. */

    private static final String[] celestialBodyName = {
            "Mercury", "Venus", "Earth", "Mars",
            "Jupiter", "Saturn", "Uranus", "Neptune",
            "Pluto", "2008 VB4", "2009 FG", "Halley"
    };
    private static final double[] epsilon = {
            0.206, 0.007, 0.017, 0.093,
            0.048, 0.056, 0.047, 0.009,
            0.248, 0.617, 0.529, 0.967
    };
    protected static final double[] a = {
            0.387, 0.723, 1.0, 1.524,
            5.203, 9.54, 19.18, 30.06,
            39.53, 2.35, 1.97, 17.83};
    private static final double[] period = { // Precalculated from Kepler's 3rd law T = a^{3/2}
            0.241, 0.615, 1.0, 1.881,
            11.86, 29.46, 84.01, 164.8,
            248.5, 3.61, 2.76, 75.32
    };
    private static final double[] theta0 = {
            5.1, 1.4, 1.2, 1.6,
            1.2, 4.4, 1.2, 2.0,
            5.6, 3.1, 3.1, 3.1
    };
    private static final float[] orientDeg = {
            0f, 0.0f, 0f, 100f,
            0f, 0f, 0f, 0f,
            200f, 70f, -45f, 115f
    };
    private static final double[] retroFac = {
            1, 1, 1, 1,
            1, 1, 1, 1,
            1, 1, 1, -1
    };  // +1 prograde; -1 retrograde

    private Paint paint;                       // Paint object controlling format of screen draws
    private ShapeDrawable planet;              // Planet symbol
    private float X[];                         // Current X position of planet (pixels)
    private float Y[];                         // Current Y position of planet (pixels)
    private float centerX;                     // X for center of display (pixels)
    private float centerY;                     // Y for center of display (pixels)
    private float R0[];                        // Radius of planetary orbit in pixels
    private double theta[];                    // Planet angle (radians clockwise from 12 o'clock)
    private double dTheta[];                   // Angular increment each step (radians)
    private double pixelScale;                 // Scale factor: number of pixels per AU
    private double c1[];                       // The constant distance scale factor a*(1+epsilon^2)
    /**
     * Constant used to compute dTheta[i] form dt
     */
    private double c2[];                       // Constant used to computer dTheta[i] from dt
    protected double dt;                         // Animation timestep (years)
    private long delay = 20;                   // Milliseconds of delay in the update loop
    private double zoomFac = 1.0;              // Zoom factor (relative to 1) for display
    public boolean showLabels = false;         // Whether to show planet labels
    private static int mState = DONE;          // Whether thread runs
    private static boolean isAnimating = true; // Whether planet motion is updated on screen
    private boolean showOrbits = true;         // Whether to show the orbital paths as curves
    private boolean showToast1 = true;         // Whether to Toast indicating short-press action
    private boolean showToast2 = true;         // Whether to Toast indicating long-press action

    // Handler to implement updates from the background thread to views
    // on the main UI

    private Handler handler = new Handler();

    public KeplerRunner(Context context) {
        super(context);

        X = new float[numObjects];
        Y = new float[numObjects];
        theta = new double[numObjects];
        dTheta = new double[numObjects];
        R0 = new float[numObjects];
        c1 = new double[numObjects];
        c2 = new double[numObjects];
        dt = 1 / (double) nsteps;
        // Add click and long click listeners
        View view = getRootView();
        view.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                String ts = "Short-press toggles orbit visibility";
                showOrbits = !showOrbits;
                if (showToast1) Toast.makeText(getContext(), ts, Toast.LENGTH_LONG).show();
                showToast1 = false;   // Show only the first time
            }
        });
        view.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                String ts = "Long-press toggles planet motion on/off";
                isAnimating = !isAnimating;
                if (isAnimating) {
                    mState = RUNNING;
                    startAnimation();
                } else {
                    mState = DONE;
                }
                if (showToast2) Toast.makeText(getContext(), ts, Toast.LENGTH_LONG).show();
                showToast2 = false;   // Show only the first time
                return true;          // Consume event so long-press doesn't trigger onClick also
            }
        });

        for (int i = 0; i < numObjects; i++) {
            theta[i] = -direction * theta0[i];
        }

        // Define the planet as circular shape
        planet = new ShapeDrawable(new OvalShape());
        planet.getPaint().setColor(PLANET_COLOR);
        planet.setBounds(0, 0, 2 * planetRadius, 2 * planetRadius);

        // Set up the Paint object that will control format of screen draws
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setTextSize(12);
        paint.setStrokeWidth(0);

    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {

        // Coordinates for center of screen (X0 and Y0 permit arbitrary offset of center)
        centerX = w / 2 + X0;
        centerY = h / 2 + Y0;

        // Make orbital radius a fraction of minimum of width and height of display and scale
        // by zoomFac.
        pixelScale = zoomFac * fracWidth * Math.min(centerX, centerY) / a[4]; // a[4]?

        // Set the initial position of the planet (translate by planetRadius so center of planet
        // is at this position)
        for (int i = 0; i < numObjects; i++) {
            // Compute scales c1[] and c2[] carrying distance units in pixels
            c1[i] = pixelScale * a[i] * (1 - epsilon[i] * epsilon[i]);
            c2[i] = 2 * Math.PI * Math.sqrt(1 - epsilon[i] * epsilon[i])
                    * dt * (pixelScale * a[i]) * (pixelScale * a[i]) / period[i];
            R0[i] = (float) distanceFromFocus(c1[i], epsilon[i], theta[i]);
            // The change in theta consistent with Kepler's 2nd law (equal areas in equal time)
            dTheta[i] = direction * c2[i] / R0[i] / R0[i];
            // New values of X and Y for planet
            X[i] = centerX - R0[i] * (float) Math.sin(theta[i]) - (int) (planetRadius * zoomFac);
            Y[i] = centerY - R0[i] * (float) Math.cos(theta[i]) - (int) (planetRadius * zoomFac);
        }

        // Start the animation thread now that we have the screen geometry

        startAnimation();
    }
    // Method to start the animation thread

    public void startAnimation() {

        // Operation on background thread that updates the main thread through handler.

        Log.i(TAG, "startAnimation()");

        KeplerRunner.mState = RUNNING;
        KeplerRunner.isAnimating = true;

        new Thread(new Runnable() {
            public void run() {
                while (KeplerRunner.mState == RUNNING && KeplerRunner.isAnimating) {

                    // Update the X and Y coordinates for all planets
                    newXY();

                    // The method Thread.sleep throws an InterruptedException if Thread.interrupt()
                    // were to be issued while thread is sleeping; the exception must be caught.
                    try {
                        // Control speed of update (but precision of delay not guaranteed)
                        Thread.sleep(delay);
                    } catch (InterruptedException e) {
                        Log.e("ERROR", "Thread Interruption");
                    }

                    // Update the animation by invalidating the view to force a redraw.
                    // We cannot update views on the main UI directly from this thread, so we use
                    // handler to do it.

                    handler.post(new Runnable() {
                        public void run() {
                            // Each time through the animation loop,  invalidate the main UI
                            // view to force a redraw.
                            invalidate();
                        }
                    });
                }
            }
        }).start();
    }

    /* Method to increment angle theta and compute the new X and Y .  The orbits of the
      planets are ellipses with the Sun at one focus.   */

    private void newXY() {
        for (int i = 0; i < numObjects; i++) {
            c1[i] = pixelScale * a[i] * (1 - epsilon[i] * epsilon[i]);
            c2[i] = 2 * Math.PI * Math.sqrt(1 - epsilon[i] * epsilon[i])
                    * dt * (pixelScale * a[i]) * (pixelScale * a[i]) / period[i];
            dTheta[i] = retroFac[i] * direction * c2[i] / R0[i] / R0[i];
            theta[i] += dTheta[i];
            R0[i] = (float) distanceFromFocus(c1[i], epsilon[i], theta[i]);
            X[i] = (float) (R0[i] * Math.sin(theta[i])) + centerX - (int) (planetRadius * zoomFac);
            Y[i] = centerY - (float) (R0[i] * Math.cos(theta[i])) - (int) (planetRadius * zoomFac);
        }
    }

    /* This method will be called each time the screen is redrawn. The draw is
      on the Canvas object, with formatting controlled by the Paint object.
      When to redraw is under Android control, but we can request a redraw
      using the method invalidate() inherited from the View superclass.  In this
      case the method handler() calls invalidate() when it receives a message from
      the animation thread that it has completed one pass through the animation loop. */

    @Override
    public void onDraw(Canvas canvas) {

        super.onDraw(canvas);

        // First draw the background (Sun and orbital paths)
        drawBackground(paint, canvas);
        paint.setColor(LABEL_COLOR);  // Label font color
        paint.setTextSize(30);        // Label font size
        paint.setAntiAlias(true);

        // Now loop over the planets, asteroids, dwarf planets, and comets, placing the
        // corresponding symbol at the appropriate position.

        for (int i = 0; i < numObjects; i++) {

            // The nested sets of save() .. restore() below keep the matrix transformations
            // (translations and rotations in this case) from affecting the drawing on the canvas
            // outside of the save() .. restore() blocks.  Note: for each save() there is
            // a matching restore().

            canvas.save();
            canvas.translate(centerX, centerY);
            canvas.rotate(orientDeg[i]);
            canvas.translate(X[i] - centerX, Y[i] - centerY);
            planet.setBounds(0, 0,
                    (int) (2 * planetRadius * zoomFac),
                    (int) (2 * planetRadius * zoomFac));
            planet.draw(canvas);

            // Rotate the canvas back before drawing label so it will be horizontal instead of
            // having the orientation of the ellipse. This save() .. restore() block is nested inside
            // the outer one, so this inverse rotation affects only the orientation of the label.

            canvas.save();
            canvas.rotate(-orientDeg[i]);
            if (showLabels) canvas.drawText(celestialBodyName[i], 10, 0, paint);
            canvas.restore();
            canvas.restore();
        }
    }

    // Called by onDraw to draw the background
    private void drawBackground(Paint paint, Canvas canvas) {

        // Draw the Sun
        paint.setColor(SUN_COLOR);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(centerX, centerY, (float) (sunRadius * zoomFac), paint);

        // Orbits drawn with line segments if showOrbits is true
        if (showOrbits) {
            paint.setStyle(Paint.Style.STROKE);
            paint.setColor(ORBIT_COLOR);
            double phi = 0;

            // Loop over each object, drawing its orbit as a sequence of numpoints line segments
            for (int i = 0; i < numObjects; i++) {

                // Starting points to draw orbit.  Note that the sign of the y coordinate is flipped
                float lastxx = 0;
                float lastyy = -(float) (distanceFromFocus(c1[i], epsilon[i], phi) * Math.cos(phi));

                canvas.save();
                canvas.translate(centerX, centerY);
                canvas.rotate(orientDeg[i]);
                phi = 0;

                // Increase density of plot points for very elliptical orbits to resolve their shapes
                int plotpoints = numpoints;
                double delphi = dphi;
                if (epsilon[i] > 0.7) { // This can be done by another calculations
                    plotpoints *= 3;
                    delphi *= THIRD;
                }
                // Draw the orbit for object i
                for (int j = 0; j < plotpoints; j++) {
                    phi += delphi;
                    float rr = (float) distanceFromFocus(c1[i], epsilon[i], phi);
                    float xx = (float) (rr * Math.sin(phi));
                    float yy = -(float) (rr * Math.cos(phi));  // Sign flipped
                    canvas.drawLine(lastxx, lastyy, xx, yy, paint);
                    lastxx = xx;
                    lastyy = yy;
                }
                canvas.restore();
            }
        }
    }

    // Return distance from focus for elliptical orbit (in units of c1)
    private double distanceFromFocus(double c1, double epsilon, double theta) {
        return (c1 / (1 + epsilon * Math.cos(theta)));
    }

    // Stop the thread loop
    public void stopLooper() {
        mState = DONE;
        isAnimating = false;
    }

    // Start the thread loop
    public void startLooper() {
        if (!isAnimating) {
            String ts = "Long-press to toggle motion on/off";
            Toast.makeText(this.getContext(), ts, Toast.LENGTH_LONG).show();
        }
    }

    // Method to change the zoom factor
    void setZoom(double scale) {
        Log.i("SET ZOOM", " zoomFac = " + zoomFac * scale +
                "  Scale = " + scale +
                "  Pixel Scale = " + zoomFac * fracWidth * Math.min(centerX, centerY) / a[4]);
        if (!isAnimating) return;
        zoomFac *= scale;
        pixelScale = zoomFac * fracWidth * Math.min(centerX, centerY) / a[4];
        for (int i = 0; i < numObjects; i++) {
            c1[i] = pixelScale * a[i] * (1 - epsilon[i] * epsilon[i]);
            c2[i] = 2 * Math.PI * Math.sqrt(1 - epsilon[i] * epsilon[i])// direction *
                    * dt * (pixelScale * a[i]) * (pixelScale * a[i]) / period[i];
        }
    }

    // Method to change the speed of the animation.  Returns long int equal to the new
    // delay, or -1 if no delay change because the animation is not active, or -2 if
    // the requested new delay would be less than 1.

    long setDelay(double factor) {
        if (!isAnimating) return -1;
        if (delay == 1 && factor < 1) return -2;
        // Logic below because delay is long int, so keep it from getting less than 1 and also
        // allow it to increase from small values.
        delay = Math.max((long) (delay * factor), 1);
        if (delay < 10 && factor > 1) delay += 2;
        return delay;
    }

    private double changeNsteps(int steps) {
        if (!isAnimating) return -1d;
        nsteps = steps;
        if (steps < 2) nsteps = 2;
        dt = 1 / (double) nsteps;
        return nsteps;
    }

    public void addNsteps(int steps) {
        changeNsteps(nsteps + steps);
    }

    public void subNsteps(int steps) {
        changeNsteps(nsteps - steps);
    }
    public int getNsteps(){
        return nsteps;
    }

    public void changeDirection() {
        if (!isAnimating) return;
        Log.w("REVERSE", "REVERSE BUTTON TRIGGERED");
        direction *= -1;
    }

    public void timeJump(int timeInEarthDays) {
        if (!isAnimating) {
            Toast.makeText(getContext(), "Stop Animation", Toast.LENGTH_SHORT).show();
            return;
        }
        for (int i = 0; i < numObjects; i++) {
            dTheta[i] = timeScale * retroFac[i] * direction * c2[i] / R0[i] / R0[i];
            theta[i] += dTheta[i]*timeInEarthDays;
            R0[i] = (float) distanceFromFocus(c1[i], epsilon[i], theta[i]);
            X[i] = (float) (R0[i] * Math.sin(theta[i])) + centerX - (int) (planetRadius * zoomFac);
            Y[i] = centerY - (float) (R0[i] * Math.cos(theta[i])) - (int) (planetRadius * zoomFac);
        }
    }
}
