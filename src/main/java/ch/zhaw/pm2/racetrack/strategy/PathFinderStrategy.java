package ch.zhaw.pm2.racetrack.strategy;

import static ch.zhaw.pm2.racetrack.Direction.getMovingDirections;
import static ch.zhaw.pm2.racetrack.SpaceType.getFinishSpaceTypes;
import static ch.zhaw.pm2.racetrack.utils.CalculationUtil.isFinishLineCrossedCorrectly;
import static java.lang.Double.MAX_VALUE;

import ch.zhaw.pm2.racetrack.Car;
import ch.zhaw.pm2.racetrack.Direction;
import ch.zhaw.pm2.racetrack.PositionVector;
import ch.zhaw.pm2.racetrack.SpaceType;
import ch.zhaw.pm2.racetrack.Track;
import ch.zhaw.pm2.racetrack.utils.CalculationUtil;
import java.util.Comparator;
import java.util.Deque;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.TreeSet;

public class PathFinderStrategy implements MoveStrategy {

    // Give impassable nodes a cost too high for anything else to match,
    // but not so high as to create any possibility of overflow.
    private static final double COST_IMPASSABLE = MAX_VALUE / 1e6;
    private static final double COST_OPEN = 1.0;
    private static final double COST_NEAR_WALL = 2.0;
    private static final double COST_DIRECTION_CONSTANT = 0.001;
    private static final double ZERO_COST = 0.0;

    private final PathFollowerMoveStrategy pathFollowerMoveStrategy;

    public PathFinderStrategy(final Track track, final Car car) {
        final PathNode pathEnd = findBestPath(track, car.getCurrentPosition());
        final Deque<PositionVector> path = new LinkedList<>();
        if (pathEnd != null) {
            int smoothIteration = 0;
            while (smoothPath(track, pathEnd)) {
                smoothIteration++;
            }
            System.out.printf("Smoothed out the path %s times.%n", smoothIteration);
            PathNode currentNode = pathEnd;
            while (currentNode != null) {
                path.addFirst(currentNode.getPosition());
                currentNode = currentNode.getPrev();
            }
        }

        this.pathFollowerMoveStrategy = new PathFollowerMoveStrategy(path.stream().toList(), car);
    }

    /**
     * Retrieves all 8 neighbors of a node. Does not check the neighboring nodes for validity in any
     * way.
     *
     * @param centerNode The central node for which to get neighbors
     * @return A List containing 8 PathNodes, each representing a neighbor (horizontal, vertical, or
     * diagonal) of the original node. The GridPoints may represent locations that are outside the
     * track or otherwise invalid.
     */
    private static PathNode[] getNeighbors(final PathNode centerNode) {
        return getMovingDirections().stream()
            .map(movingDirection -> new PathNode(
                centerNode.getPosition().add(movingDirection.getVector()), centerNode, MAX_VALUE))
            .toArray(PathNode[]::new);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Direction nextMove() {
        return pathFollowerMoveStrategy.nextMove();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTurnMessage(Car car) {
        return pathFollowerMoveStrategy.getTurnMessage(car);
    }

    /**
     * @inheritDoc}
     */
    @Override
    public String getStatistics() {
        return pathFollowerMoveStrategy.getStatistics();
    }

    /**
     * Calculates the best path from the start {@link PositionVector}.
     *
     * @param track instance of the current {@link Track}
     * @param start starting {@link PositionVector} from which the path is calculated.
     * @return a {@link PathNode} containing the next best path position.
     */
    private PathNode findBestPath(final Track track, final PositionVector start) {
        final PriorityQueue<PathNode> frontier = new PriorityQueue<>(PathNode.costComparator);
        frontier.add(new PathNode(start, null, ZERO_COST));
        final Set<PathNode> visited = new TreeSet<>(PathNode.gridPointComparator);

        PathNode endNode = null;

        while (!frontier.isEmpty()) {
            final PathNode currentNode = frontier.remove();

            // Fail when we run out of passable locations, or succeed when we reach a finish line.
            if (currentNode.getTotalCost() >= COST_IMPASSABLE) {
                break;
            }
            if (getFinishSpaceTypes().contains(
                track.getSpaceTypeAtPosition(currentNode.getPosition()))) {
                endNode = currentNode;
                break;
            }
            processNeighbors(track, frontier, visited, currentNode);

            visited.add(currentNode);
        }

        return endNode;
    }

    private void processNeighbors(final Track track, final PriorityQueue<PathNode> frontier,
        final Set<PathNode> visited, final PathNode currentNode) {
        for (PathNode neighbor : getNeighbors(currentNode)) {

            if (visited.contains(neighbor) || neighbor == null) {
                continue;
            }

            PathNode realNeighborNode = getPathNodeFromFrontier(frontier, neighbor);
            boolean needsRemovalFromFrontier = true;
            if (realNeighborNode == null) {
                realNeighborNode = neighbor;
                needsRemovalFromFrontier = false;
            }
            double totalCostToNeighbor =
                getMoveCost(track, currentNode, realNeighborNode) + currentNode.getTotalCost();
            if (totalCostToNeighbor < realNeighborNode.getTotalCost()) {
                if (needsRemovalFromFrontier) {
                    frontier.remove(realNeighborNode);
                }

                realNeighborNode.setPrev(currentNode);
                realNeighborNode.setTotalCost(totalCostToNeighbor);
                frontier.add(realNeighborNode);
            }
        }
    }

    private double getMoveCost(final Track track, final PathNode fromNode, final PathNode toNode) {
        final PositionVector difference = toNode.getPosition().subtract(fromNode.getPosition());
        final SpaceType spaceTypeAtPosition = track.getSpaceTypeAtPosition(toNode.getPosition());
        return switch (spaceTypeAtPosition) {
            case WALL -> COST_IMPASSABLE;
            case TRACK -> getOpenSpaceMoveCost(track, fromNode, toNode);
            case FINISH_UP, FINISH_DOWN, FINISH_LEFT, FINISH_RIGHT ->
                isFinishLineCrossedCorrectly(spaceTypeAtPosition, difference)
                    ? getOpenSpaceMoveCost(track, fromNode, toNode)
                    : COST_IMPASSABLE;
        };
    }

    private double getOpenSpaceMoveCost(final Track track, final PathNode fromNode,
        final PathNode toNode) {
        // Prefer straight paths that don't hug the walls
        double baseCost = isNearWall(track, toNode) ? COST_NEAR_WALL : COST_OPEN;
        double directionPenalty = ZERO_COST;
        if (fromNode.getPrev() != null) {
            PositionVector oldDirection = fromNode.getPosition()
                .subtract(fromNode.getPrev().getPosition());
            PositionVector newDirection = toNode.getPosition().subtract(fromNode.getPosition());
            directionPenalty =
                COST_DIRECTION_CONSTANT * (1 - oldDirection.dotProduct(newDirection));
        }
        return baseCost + directionPenalty;
    }

    private boolean isNearWall(final Track track, final PathNode node) {
        final PathNode[] neighbors = getNeighbors(node);
        for (PathNode neighbor : neighbors) {
            if (track.getSpaceTypeAtPosition(neighbor.getPosition()) == SpaceType.WALL) {
                return true;
            }
        }
        return false;
    }

    private boolean smoothPath(final Track track, final PathNode pathEnd) {
        boolean madeChanges = false;
        PathNode middleNode = pathEnd.getPrev();
        if (middleNode == null) {
            // pathEnd is also the path start, can't change anything
            return false;
        }
        PathNode anchorNode = middleNode.getPrev();
        if (anchorNode == null) {
            // middleNode is the path start, can't remove it
            return false;
        }

        boolean hasLineOfSight = true;
        for (PositionVector point : CalculationUtil.getPassedPositions(pathEnd.getPosition(),
            anchorNode.getPosition())) {
            if (track.getSpaceTypeAtPosition(point) == SpaceType.WALL || isNearWall(track,
                new PathNode(point, anchorNode, 0))) {
                hasLineOfSight = false;
                break;
            }
        }

        if (hasLineOfSight) {
            pathEnd.setPrev(anchorNode);
            madeChanges = true;
        }

        return madeChanges || smoothPath(track, pathEnd.getPrev());
    }

    /**
     * Returns a reference to a specified element within  an Iterable container, if such element
     * exists. Does not remove the element from its container.
     *
     * @param iterable The collection or other Iterable that (potentially) contains the specified
     *                 element
     * @param obj      An object used to select the desired element. Each element e will be tested
     *                 using e.equals(obj).
     * @param <T>      The type of element held by iterable
     * @return If one or more element e exist such that e.equals(obj), returns the first such
     * element. Otherwise, returns null.
     */
    private <T> T getPathNodeFromFrontier(final Iterable<T> iterable, final Object obj) {
        if (obj != null) {
            for (T currentElement : iterable) {
                if (currentElement.equals(obj)) {
                    return currentElement;
                }
            }
        }
        return null;
    }

    private static class PathNode {

        /**
         * Compares nodes based on the total cost from the starting point to each node. Note that
         * this Comparator is NOT consistent with PathNode#equals.
         */
        public static final Comparator<PathNode> costComparator = Comparator.comparing(
            PathNode::getTotalCost);
        // The order does not matter, as long as it is well-defined.
        // Directly access the mPosition members to avoid creating new copies.
        /**
         * Compares nodes based on their location on the grid.
         */
        public static final Comparator<PathNode> gridPointComparator = Comparator.comparingInt(
            (PathNode o) -> o.position.getX()).thenComparingInt(o -> o.position.getY());

        private final PositionVector position;
        private PathNode prev;
        private Double totalCost;

        public PathNode(final PositionVector pos, final PathNode prev, final double totalCost) {
            position = new PositionVector(pos);
            this.prev = prev;
            this.totalCost = totalCost;
        }

        @Override
        public boolean equals(final Object obj) {
            if (obj instanceof PathNode secondNode) {
                return gridPointComparator.compare(this, secondNode) == 0;
            } else if (obj instanceof PositionVector point) {
                return this.position.equals(point);
            } else {
                throw new ClassCastException();
            }
        }

        @Override
        public String toString() {
            return "Location: " + position + "; Cost: " + totalCost.toString();
        }

        public Double getTotalCost() {
            return totalCost;
        }

        public void setTotalCost(final double totalCost) {
            this.totalCost = totalCost;
        }

        public PositionVector getPosition() {
            return position;
        }

        public PathNode getPrev() {
            return prev;
        }

        public void setPrev(final PathNode prev) {
            this.prev = prev;
        }
    }

}
