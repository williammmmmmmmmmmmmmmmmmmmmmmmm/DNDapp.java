package org.example.dndapp;

/**
 * Data structure for a shared enemy token on the map.
 * This class must be public for GSON serialization/deserialization on the server.
 */
public class EnemyToken {
    // Unique ID for the token (used for tracking movement)
    private String id;
    // Name displayed above the token
    private String name;
    // Axial coordinates (q, r)
    private int q;
    private int r;

    // Default constructor for GSON
    public EnemyToken() {
    }

    public EnemyToken(String id, String name, int q, int r) {
        this.id = id;
        this.name = name;
        this.q = q;
        this.r = r;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getQ() {
        return q;
    }

    public void setQ(int q) {
        this.q = q;
    }

    public int getR() {
        return r;
    }

    public void setR(int r) {
        this.r = r;
    }
}