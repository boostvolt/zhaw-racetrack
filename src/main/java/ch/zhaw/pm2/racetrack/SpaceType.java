package ch.zhaw.pm2.racetrack;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;

/**
 * Represents possible space types of the racetrack grid. <b></b>(This shall not be altered!)</b>
 * <p>The property {@link SpaceType#spaceChar} is used to parse from the track file and represents
 * the {@link SpaceType} in the text representation of the {@link ch.zhaw.pm2.racetrack.Track}
 * created by {@link ch.zhaw.pm2.racetrack.Track#toString()}.</p>
 * <p>The mapping of the Characters is as follows:
 *  <ul>
 *    <li>WALL = '#' : road boundary or off track space</li>
 *    <li>TRACK = ' ' : road or open track space</li>
 *    <li>FINISH_LEFT  = '&lt;' : finish line spaces which have to be crossed leftwards</li>
 *    <li>FINISH_RIGHT = '&gt;' : finish line spaces which have to be crossed rightwards</li>
 *    <li>FINISH_UP    = '^' : finish line spaces which have to be crossed upwards</li>
 *    <li>FINISH_DOWN  = 'v' : finish line spaces which have to be crossed downwards</li>
 * </ul></p>
 */
public enum SpaceType {
    WALL('#'),
    TRACK(' '),
    FINISH_UP('^'),
    FINISH_DOWN('v'),
    FINISH_LEFT('<'),
    FINISH_RIGHT('>');

    /**
     * Character representation of the {@link SpaceType} in the track file and printout.
     */
    private final char spaceChar;

    SpaceType(final char spaceChar) {
        this.spaceChar = spaceChar;
    }

    /**
     * Detects the matching {@link SpaceType} for the provided character.
     *
     * @param spaceChar char value to return the matching {@link SpaceType} for
     * @return {@link Optional < SpaceType >} matching the spaceChar,
     * {@link Optional<SpaceType>#empty()} otherwise
     */
    public static Optional<SpaceType> spaceTypeForChar(char spaceChar) {
        return Arrays.stream(SpaceType.values())
            .filter(type -> type.spaceChar == spaceChar)
            .findFirst();
    }

    /**
     * Returns a set of all finish-type spaces.
     *
     * @return a set containing all finish-type spaces.
     */
    public static Set<SpaceType> getFinishSpaceTypes() {
        return EnumSet.of(FINISH_UP, FINISH_DOWN, FINISH_LEFT, FINISH_RIGHT);
    }

    /**
     * @return spaceChar representing this {@link SpaceType}
     */
    public char getSpaceChar() {
        return spaceChar;
    }

}
