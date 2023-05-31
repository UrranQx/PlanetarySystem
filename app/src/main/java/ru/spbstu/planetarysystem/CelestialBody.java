package ru.spbstu.planetarysystem;

public class CelestialBody {
    private String celestialBodyName;
    private double eccentricity;
    private double semimajorAxis;
    private double period;
    private float orbitRotation;
    private double initialAngeInRad;
    private double direction;

    /**
     * CelestialBody("Mercury", 0.387, 0.206, 0.241, 5.1, 0f, 1.0),
     * @param celestialBodyName
     * @param e
     * @param a
     * @param p
     * @param theta_0
     * @param orbitRotation
     * @param direction
     */
    public CelestialBody(
            String celestialBodyName, double e, double a,
            double p, double theta_0, float orbitRotation, double direction
    ) {
        this.celestialBodyName = celestialBodyName;
        this.eccentricity = e;
        this.semimajorAxis = a;
        this.period = p;
        this.orbitRotation = orbitRotation;
        this.initialAngeInRad = theta_0;
        this.direction = direction;
    }

    public String getCelestialBodyName() {
        return celestialBodyName;
    }

    public double getEccentricity() {
        return eccentricity;
    }

    public double getSemimajorAxis() {
        return semimajorAxis;
    }

    public float getOrbitRotation() {
        return orbitRotation;
    }

    public double getInitialAngeInRad() {
        return initialAngeInRad;
    }

    public double getDirection() {
        return direction;
    }

    public void setCelestialBodyName(String celestialBodyName) {
        this.celestialBodyName = celestialBodyName;
    }

    public void setEccentricity(double eccentricity) {
        this.eccentricity = eccentricity;
    }

    public void setSemimajorAxis(double semimajorAxis) {
        this.semimajorAxis = semimajorAxis;
    }

    public void setOrbitRotation(float orbitRotation) {
        this.orbitRotation = orbitRotation;
    }

    public void setInitialAngeInRad(double initialAngeInRad) {
        this.initialAngeInRad = initialAngeInRad;
    }

    public void setDirection(double direction) {
        this.direction = direction;
    }

    public double getPeriod() {
        return period;
    }

    public void setPeriod(double period) {
        this.period = period;
    }
}