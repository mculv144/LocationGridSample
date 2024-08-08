import com.badlogic.gdx.ai.pfa.Connection;
import com.badlogic.gdx.ai.pfa.indexed.IndexedGraph;
import com.badlogic.gdx.utils.Array;
import javafx.util.Pair;

import java.util.*;

/**
 * This is a connected graph of Location objects, whose configuration relative to each other has been randomly generated.
 * Each location is represented by integer x,y-coordinates, and root is always at 0,0.
 * Locations are considered adjacent to any other locations that are one spot away up, down, left, or right, assuming
 * they exist in the graph.
 */
public class LocationGrid implements IndexedGraph<Location> {

    private final ArrayList<Location> locationList;
    private final Map<String, Location> coordinatesToLocationMap;
    private final Map<Location, Integer> locationToIndexMap;
    private static final Set<Pair<Integer, Integer>> DIRECTIONS_SET = setUpDirectionsSet();
    private final Random rand;

    public LocationGrid(final int numLocationsInGrid) {
        this.locationList = new ArrayList<>();
        this.coordinatesToLocationMap = new HashMap<>();
        this.locationToIndexMap = new HashMap<>();
        this.rand = new Random();

        generateForSize(numLocationsInGrid);
        updateConnectionsAmongNodes();
    }

    private static Set<Pair<Integer, Integer>> setUpDirectionsSet() {
        final Set<Pair<Integer, Integer>> set = new HashSet<>();
        set.add(new Pair<>(1, 0));
        set.add(new Pair<>(-1, 0));
        set.add(new Pair<>(0, 1));
        set.add(new Pair<>(0, -1));

        return set;
    }

    private void generateForSize(final int size) throws IllegalArgumentException {
        if (size < 1) {
            throw new IllegalArgumentException("Location grid must have at least one location.");
        }

        final Location origin = new Location(0, 0);
        this.locationList.add(origin);
        this.locationToIndexMap.put(origin, 0);
        this.coordinatesToLocationMap.put(Location.getCoordinatesKey(0, 0), origin);
        final Set<String> takenCoordinates = new HashSet<>();
        takenCoordinates.add(Location.getCoordinatesKey(0, 0));
        final Set<Pair<Integer, Integer>> validNextNodes = new HashSet<>();

        Location lastLocation = origin;
        while (this.locationList.size() < size) {
            updateValidNextNodes(validNextNodes, takenCoordinates, lastLocation);

            if (validNextNodes.isEmpty()) {
                // won't get here
                break;
            }
            final Pair<Integer, Integer> nextLocationPair = pickAndRemoveValidNextNode(validNextNodes);
            final Location nextLocation = new Location(nextLocationPair.getKey(), nextLocationPair.getValue());
            this.locationList.add(nextLocation);
            this.locationToIndexMap.put(nextLocation, this.locationList.size());
            takenCoordinates.add(Location.getCoordinatesKey(nextLocation.xIndex, nextLocation.yIndex));
            this.coordinatesToLocationMap.put(Location.getCoordinatesKey(nextLocation.xIndex, nextLocation.yIndex), nextLocation);
            lastLocation = nextLocation;
        }
    }

    private void updateConnectionsAmongNodes() {
        for (final Location location : this.locationList) {
            for (Pair<Integer, Integer> dirPair : DIRECTIONS_SET) {
                final Pair<Integer, Integer> possibleSpot = new Pair<>(
                        dirPair.getKey() + location.xIndex,
                        dirPair.getValue() + location.yIndex);

                final String coordinatesKey = Location.getCoordinatesKey(possibleSpot.getKey(), possibleSpot.getValue());
                if (!coordinatesToLocationMap.containsKey(coordinatesKey)) {
                    continue;
                }

                final Location foundLocation = this.coordinatesToLocationMap.get(coordinatesKey);
                location.addConnectionIfNotPresent(foundLocation);
                foundLocation.addConnectionIfNotPresent(location);
            }
        }
    }

    private void updateValidNextNodes(final Set<Pair<Integer, Integer>> validNextNodes,
                                      final Set<String> takenNodes,
                                      final Location lastLocation) {
        for (Pair<Integer, Integer> dirPair : DIRECTIONS_SET) {
            final Pair<Integer, Integer> candidate = new Pair<>(
                    dirPair.getKey() + lastLocation.xIndex,
                    dirPair.getValue() + lastLocation.yIndex);

            if (validNextNodes.contains(candidate)) {
                continue;
            }

            if (takenNodes.contains(Location.getCoordinatesKey(candidate.getKey(), candidate.getValue()))) {
                continue;
            }

            // if we get here, the candidate node is considered a valid next node choice
            validNextNodes.add(candidate);
        }
    }

    private Pair<Integer, Integer> pickAndRemoveValidNextNode(final Set<Pair<Integer, Integer>> validNextNodes) {
        int pick = this.rand.nextInt(validNextNodes.size());
        int i = 0;
        for (final Pair<Integer, Integer> val : validNextNodes) {
            if (i == pick) {
                validNextNodes.remove(val);
                return val;
            }

            i++;
        }

        return null; // won't get here
    }

    @Override
    public int getIndex(Location location) {
        return this.locationToIndexMap.get(location);
    }

    @Override
    public int getNodeCount() {
        return this.locationList.size();
    }

    @Override
    public Array<Connection<Location>> getConnections(Location location) {
        return location.getConnections();
    }
}
