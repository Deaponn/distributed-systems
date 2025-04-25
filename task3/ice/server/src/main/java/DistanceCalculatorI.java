package main.java;

import Demo.DistanceCalculator;
import Demo.Point;
import com.zeroc.Ice.Current;

public class DistanceCalculatorI implements DistanceCalculator {
    private final String servantId = "SharedDistanceCalculator@" + Integer.toHexString(hashCode());

    @Override
    public double calculateDistance(Point p1, Point p2, Current current) {
        double dx = p1.x - p2.x;
        double dy = p1.y - p2.y;
        double distance = Math.sqrt(dx * dx + dy * dy);

        System.out.printf("[%s] Calculated distance between (%f, %f) and (%f, %f) = %f'%n",
                servantId, p1.x, p1.y, p2.x, p2.y, distance);
        return distance;
    }
}