package ch.zhaw.pm2.racetrack.given;

import ch.zhaw.pm2.racetrack.PositionVector;
import ch.zhaw.pm2.racetrack.SpaceType;

/**
 * Interface representing the mandatory functions of the racetrack board.<br/> IMPORTANT: This
 * interface shall not be altered!<br/> It specifies elements we use to test Racetrack for
 * grading.<br/> You may change or extend the default implementation provided in
 * {@link ch.zhaw.pm2.racetrack.Track}<br/> Full Javadoc can be found in the implementation file.
 */
public interface TrackSpecification {

    int MAX_CARS = 9;
    int MIN_CARS = 2;

    int getHeight();

    int getWidth();

    int getCarCount();

    CarSpecification getCar(int carIndex);

    SpaceType getSpaceTypeAtPosition(PositionVector position);

    char getCharRepresentationAtPosition(int row, int col);

    String toString();
}
