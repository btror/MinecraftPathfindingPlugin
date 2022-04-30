package com.gubertmc.plugin.commands;

import com.gubertmc.MazeGeneratorPlugin;
import com.gubertmc.plugin.ControlPlatform;
import com.gubertmc.plugin.main.Maze;
import com.gubertmc.plugin.main.mazes.custom.*;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.Objects;

public record MazeGeneratorCommand(MazeGeneratorPlugin plugin) implements CommandExecutor, Listener {

    private static Maze maze;
    private static Location mazeLocation;
    private static ControlPlatform controlPlatform;
    private static final String[] algorithms = {"A* 2D", "A* 3D", "BFS 2D", "BFS 3D", "DFS 2D", "DFS 3D"};
    private static int index = 0;
    private static int size = 15;
    private static double blockerPercentage = 0.35;

    /**
     * Handle the command call.
     *
     * @param commandSender command sender.
     * @param command       command.
     * @param alias         alias.
     * @param args          args.
     * @return true.
     */
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String alias, String[] args) {
        Player player = (Player) commandSender;
        try {
            Location location = player.getLocation();
            location = new Location(
                    location.getWorld(),
                    location.getX() + 1,
                    location.getY(),
                    location.getZ() + 1
            );
            mazeLocation = location;
            maze = new PathfindingMaze2D(
                    plugin,
                    location.getBlock(),
                    size,
                    blockerPercentage
            );

            player.sendMessage("Spawning control platform...");

            controlPlatform = new ControlPlatform(
                    player.getLocation().getBlock(),
                    size,
                    blockerPercentage,
                    algorithms[index]
            );
            controlPlatform.spawn();

            return true;

        } catch (Exception e) {
            System.out.println(e);
            player.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "/maze");
        }

        return false;
    }

    /**
     * Clear the old maze out and create a new one.
     *
     * @param e PlayerInteractEvent
     */
    @EventHandler
    public void onButtonPressed(PlayerInteractEvent e) {
        if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (Objects.equals(e.getClickedBlock(), controlPlatform.getStartButton())) {
                boolean acceptableFrames = true;
                ItemFrame[] frames = controlPlatform.getFrames();

                for (ItemFrame item : frames) {
                    if (!item.getItem().getType().isBlock()) {
                        acceptableFrames = false;
                    }
                }
                if (acceptableFrames) {
                    controlPlatform.setFrames(frames);
                    maze.generateNewMaze(
                            controlPlatform.getCoreMaterial(),
                            controlPlatform.getBlockerMaterial(),
                            controlPlatform.getSpreadMaterial(),
                            controlPlatform.getPathMaterial(),
                            controlPlatform.getStartPointGlassMaterial(),
                            controlPlatform.getEndPointGlassMaterial()
                    );
                } else {
                    e.getPlayer().sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "Maze items must be blocks...");
                }
            } else if (Objects.equals(e.getClickedBlock(), controlPlatform.getRightButton2())) {
                Sign sign = controlPlatform.getAlgorithmSign();
                if (index == algorithms.length - 1) {
                    index = 0;
                } else {
                    index++;
                }
                sign.setLine(1, algorithms[index]);
                sign.update();
                controlPlatform.setAlgorithmSign(sign);
            } else if (Objects.equals(e.getClickedBlock(), controlPlatform.getLeftButton2())) {
                Sign sign = controlPlatform.getAlgorithmSign();
                if (index == 0) {
                    index = algorithms.length - 1;
                } else {
                    index--;
                }
                sign.setLine(1, algorithms[index]);
                sign.update();
                controlPlatform.setAlgorithmSign(sign);
            } else if (Objects.equals(e.getClickedBlock(), controlPlatform.getRightButton1())) {
                Sign sign = controlPlatform.getSizeSign();
                sign.setLine(1, "" + (size + 1) + "x" + (size + 1));
                sign.update();
                size++;
                controlPlatform.setSizeSign(sign);
            } else if (Objects.equals(e.getClickedBlock(), controlPlatform.getLeftButton1())) {
                Sign sign = controlPlatform.getSizeSign();
                if (size > 6) {
                    sign.setLine(1, "" + (size - 1) + "x" + (size - 1));
                    sign.update();
                    size--;
                    controlPlatform.setSizeSign(sign);
                }
            } else if (Objects.equals(e.getClickedBlock(), controlPlatform.getRightButton3())) {
                Sign sign = controlPlatform.getPercentageSign();
                if (blockerPercentage < .80) {
                    sign.setLine(1, "" + (int) ((blockerPercentage * 100) + 1) + "%");
                    sign.update();
                    blockerPercentage += .01;
                    controlPlatform.setPercentageSign(sign);
                }
            } else if (Objects.equals(e.getClickedBlock(), controlPlatform.getLeftButton3())) {
                Sign sign = controlPlatform.getPercentageSign();
                if (blockerPercentage > 0) {
                    sign.setLine(1, "" + (int) ((blockerPercentage * 100) - 1) + "%");
                    sign.update();
                    blockerPercentage -= .01;
                    controlPlatform.setPercentageSign(sign);
                }
            }

            switch (index) {
                case 0 -> maze = new PathfindingMaze2D(
                        plugin,
                        mazeLocation.getBlock(),
                        size,
                        blockerPercentage
                );
                case 1 -> maze = new PathfindingMaze3D(
                        plugin,
                        mazeLocation.getBlock(),
                        size,
                        blockerPercentage
                );
                case 2 -> maze = new BreadthFirstSearchMaze2D(
                        plugin,
                        mazeLocation.getBlock(),
                        size,
                        blockerPercentage
                );
                case 3 -> maze = new BreadthFirstSearchMaze3D(
                        plugin,
                        mazeLocation.getBlock(),
                        size,
                        blockerPercentage
                );
                case 4 -> maze = new DepthFirstSearchMaze2D(
                        plugin,
                        mazeLocation.getBlock(),
                        size,
                        blockerPercentage
                );
                case 5 -> maze = new DepthFirstSearchMaze3D(
                        plugin,
                        mazeLocation.getBlock(),
                        size,
                        blockerPercentage
                );
            }
        }
    }
}
