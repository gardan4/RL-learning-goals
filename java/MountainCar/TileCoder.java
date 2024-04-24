public class TileCoder {
    private final double min;
    private final double max;
    private final int numTiles;
    private final int numTilings;
    private final double tileWidth;

    public TileCoder(int numTiles, int numTilings, double min, double max) {
        this.min = min;
        this.max = max;
        this.numTiles = numTiles;
        this.numTilings = numTilings;
        this.tileWidth = (max - min) / (numTiles * numTilings - 1);
    }

    public int[] getTiles(double value) {
        int[] tiles = new int[numTilings];
        for (int i = 0; i < numTilings; i++) {
            double offset = i * tileWidth / numTilings;
            int tile = (int) ((value - min + offset) / tileWidth);
            tiles[i] = Math.min(numTiles - 1, Math.max(0, tile)); // Ensuring tiles are within bounds
        }
        return tiles;
    }
}
