import com.badlogic.gdx.ai.pfa.Connection;
import com.badlogic.gdx.ai.pfa.DefaultConnection;
import com.badlogic.gdx.utils.Array;

/**
 * Represents a single node in our graph.
 */
public class Location {

    final int xIndex, yIndex;
    private final Array<Connection<Location>> connectionArray;

    public Location(final int xIndex, final int yIndex) {
        this.xIndex = xIndex;
        this.yIndex = yIndex;

        connectionArray = new Array<>(false, 8);
    }

    public boolean isAdjacentTo(final Location location) {
        for (final Connection<Location> conn : this.connectionArray) {
            if (conn.getToNode().equals(location) && !conn.getToNode().equals(this)) {
                return true;
            }
        }
        return false;
    }

    public void addConnectionIfNotPresent(final Location location) {
        if (!isAdjacentTo(location)) {
            // add two directed edges to simulate one undirected edge
            this.connectionArray.add(new DefaultConnection<>(this, location));
            this.connectionArray.add(new DefaultConnection<>(location, this));
        }
    }

    public Array<Connection<Location>> getConnections() {
        return this.connectionArray;
    }

    public static String getCoordinatesKey(final int x, final int y) {
        return String.format("%s,%s", x, y);
    }
}
