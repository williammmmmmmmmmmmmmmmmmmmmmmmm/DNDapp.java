package org.example.dndapp;

import java.util.List;

/**
 * Data class to hold map information for saving and loading with Gson.
 */
public class MapData {
    private int rowCount;
    private int colCount;
    private List<String> grid;

    public MapData(int rowCount, int colCount, List<String> grid) {
        this.rowCount = rowCount;
        this.colCount = colCount;
        this.grid = grid;
    }

    public int getRowCount() {
        return rowCount;
    }

    public int getColCount() {
        return colCount;
    }

    public List<String> getGrid() {
        return grid;
    }
}
