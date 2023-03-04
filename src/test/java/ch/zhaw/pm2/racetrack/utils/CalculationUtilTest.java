package ch.zhaw.pm2.racetrack.utils;

import static ch.zhaw.pm2.racetrack.utils.CalculationUtil.getPassedPositions;
import static org.junit.jupiter.api.Assertions.assertEquals;

import ch.zhaw.pm2.racetrack.PositionVector;
import java.util.ArrayList;
import org.junit.jupiter.api.Test;

class CalculationUtilTest {

    @Test
    void testVerticalGetPassedPosition() {
        final ArrayList<PositionVector> pointList = new ArrayList<>();
        pointList.add(new PositionVector(0, 0));
        pointList.add(new PositionVector(1, 0));
        pointList.add(new PositionVector(2, 0));
        pointList.add(new PositionVector(3, 0));
        pointList.add(new PositionVector(4, 0));
        assertEquals(pointList, getPassedPositions(new PositionVector(0, 0),
            new PositionVector(4, 0)));
    }

    @Test
    void testHorizontalGetPassedPosition() {
        final ArrayList<PositionVector> pointList = new ArrayList<>();
        pointList.add(new PositionVector(0, 1));
        pointList.add(new PositionVector(0, 2));
        pointList.add(new PositionVector(0, 3));
        pointList.add(new PositionVector(0, 4));
        pointList.add(new PositionVector(0, 5));
        assertEquals(pointList, getPassedPositions(new PositionVector(0, 1),
            new PositionVector(0, 5)));
    }

    @Test
    void testDiagonalGetPassedPosition() {
        final ArrayList<PositionVector> pointList = new ArrayList<>();
        pointList.add(new PositionVector(1, 1));
        pointList.add(new PositionVector(2, 2));
        pointList.add(new PositionVector(3, 3));
        pointList.add(new PositionVector(4, 4));
        pointList.add(new PositionVector(5, 5));
        assertEquals(pointList, getPassedPositions(new PositionVector(1, 1),
            new PositionVector(5, 5)));
    }

    @Test
    void testInverseDiagonalGetPassedPosition() {
        final ArrayList<PositionVector> pointList = new ArrayList<>();
        pointList.add(new PositionVector(5, 5));
        pointList.add(new PositionVector(4, 4));
        pointList.add(new PositionVector(3, 3));
        pointList.add(new PositionVector(2, 2));
        pointList.add(new PositionVector(1, 1));
        assertEquals(pointList, getPassedPositions(new PositionVector(5, 5),
            new PositionVector(1, 1)));
    }

}
