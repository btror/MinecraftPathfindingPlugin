package com.gubertmc.plugin;

import com.gubertmc.MazeGeneratorPlugin;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.scheduler.BukkitRunnable;

public abstract class Maze {

    public int size;
    public MazeGeneratorPlugin plugin;
    public Block block;
    public Location[][][] locations;
    public int[] startCoordinate;
    public int[] endCoordinate;
    public double wallPercentage = 0.2;


    public Maze(MazeGeneratorPlugin plugin, Block block, int size) {
        this.plugin = plugin;
        this.block = block;
        this.size = size;
        this.locations = new Location[size][size][size];
        this.startCoordinate = new int[3];
        this.endCoordinate = new int[3];
    }

    /**
     * Generate simulation maze.
     */
    public abstract int[][][] generateSimulation();

    /**
     * Create a new maze via simulation.
     */
    public abstract void generateNewMaze(Material coreMaterial, Material blockerMaterial, Material spreadMaterial, Material pathMaterial, Material startPointGlassMaterial, Material endPointGlassMaterial);

    /**
     * Create the core part of the mazes.
     * <p>
     * 2D - the floor.
     * 3D - the volume.
     */
    public abstract void generateCore(Material core);

    /**
     * Create the start and end points of the mazes.
     */
    public abstract void generateStartAndEndPoints();

    /**
     * Generate the blocked parts of the mazes.
     * <p>
     * 2D - the mazes walls to navigate.
     * 3D - the blocked areas in the mazes.
     */
    public abstract void generateBlockedAreas(int[][][] maze, Material blockerMaterial);

    /**
     * Make the animations.
     *
     * @param location spot of a block.
     * @param time     when to change the block material.
     * @param material type of block.
     * @param row      x of the block.
     * @param col      y of the block.
     * @param zNum     z of the block.
     */
    public void runnableDelayed(Location location, long time, Material material, int row, int col, int zNum) {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (row < 0 || row > size || col < 0 || col > size || zNum < 0 || zNum > size) {
                    location.getBlock().setType(material);
                    cancel();
                } else {
                    location.getBlock().setType(material);
                    locations[row][col][zNum] = location;
                    cancel();
                }
            }
        }.runTaskTimer(this.plugin, time, 20L);
    }

}
