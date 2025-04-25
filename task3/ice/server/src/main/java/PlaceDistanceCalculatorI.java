package main.java;

import Demo.PlaceDistanceCalculator;
import Demo.Point;
import com.zeroc.Ice.Current;

import java.util.Objects;

public class PlaceDistanceCalculatorI implements PlaceDistanceCalculator {
    private Point point = new Point(0, 0);
    private final String servantId;

    public PlaceDistanceCalculatorI(com.zeroc.Ice.Identity id) {
        this.servantId = String.format("PlacePointCalc(Place: %s)@%s",
                id.name, Integer.toHexString(hashCode()));
        System.out.printf("[%s] Servant constructor for Identity '%s/%s'%n",
                servantId, id.category, id.name);
    }

    @Override
    public void setPoint(Point p, Current current) {
        System.out.printf("[%s] Handling setPoint (%f, %f) request for Identity '%s/%s'%n",
                servantId, p.x, p.y, current.id.category, current.id.name);
        this.point = p;
    }

    @Override
    public double calculateDistance(Point p, Current current) {
        double dx = point.x - p.x;
        double dy = point.y - p.y;
        double distance = Math.sqrt(dx * dx + dy * dy);

        System.out.printf("[%s] Calculating distance between (%f, %f) and (%f, %f) = %f%n",
                servantId, point.x, point.y, p.x, p.y, distance);
        return distance;
    }

    @Override
    public Point getPoint(Current current) {
        System.out.printf("[%s] Handling getPoint (%f, %f) request for Identity '%s/%s'%n",
                servantId, point.x, point.y, current.id.category, current.id.name);
        return this.point;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PlaceDistanceCalculatorI that = (PlaceDistanceCalculatorI) o;
        return Objects.equals(point, that.point) && Objects.equals(servantId, that.servantId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(point, servantId);
    }
}
