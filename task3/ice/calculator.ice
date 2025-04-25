module Demo {

    struct Point {
        double x;
        double y;
    };

    interface DistanceCalculator {
        idempotent double calculateDistance(Point p1, Point p2);
    };

    interface PlaceDistanceCalculator {
        void setPoint(Point p);
        idempotent double calculateDistance(Point p);
        idempotent Point getPoint();
    };

};