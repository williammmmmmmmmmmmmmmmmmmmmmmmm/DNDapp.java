// DoomEngine.java
package org.example.dndapp;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Comparator;

/**
 * This class holds all the logic and rendering for a minimal DOOM-style
 * Raycasting game engine with concurrency fixes, dynamic enemy spawning, and three enemy types.
 */
public class DoomEngine {

    // --- GAME ENTITY CLASS (Internal) ---
    private static class Entity {
        double x, y;
        double dirX, dirY;
        String type; // "Bullet", "EnemyBullet", "NormalEnemy", "BigEnemy", "GunEnemy"
        double speed;
        double size;
        int health;
        double attackTimer;

        Entity(double x, double y, double dirX, double dirY, String type, double speed, double size, int health, double attackTimer) {
            this.x = x;
            this.y = y;
            this.dirX = dirX;
            this.dirY = dirY;
            this.type = type;
            this.speed = speed;
            this.size = size;
            this.health = health;
            this.attackTimer = attackTimer;
        }
    }

    private final List<Entity> entities = new ArrayList<>();
    // Entities created during the update loop are added here first to prevent ConcurrentModificationException.
    private final List<Entity> entitiesToAdd = new ArrayList<>();

    // --- GAME MAP ---
    private final int[][] map = {
            {1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
            {1, 0, 0, 0, 0, 0, 0, 0, 0, 1},
            {1, 0, 1, 1, 0, 0, 0, 1, 0, 1},
            {1, 0, 1, 0, 0, 0, 0, 0, 0, 1},
            {1, 0, 1, 0, 0, 1, 1, 1, 0, 1},
            {1, 0, 0, 0, 0, 0, 0, 0, 0, 1},
            {1, 0, 0, 0, 0, 0, 0, 0, 0, 1},
            {1, 0, 1, 0, 0, 0, 1, 0, 0, 1},
            {1, 0, 1, 0, 0, 0, 0, 0, 0, 1},
            {1, 1, 1, 1, 1, 1, 1, 1, 1, 1}
    };
    private final int mapWidth = map[0].length;
    private final int mapHeight = map.length;

    // --- PLAYER PROPERTIES ---
    private double posX = 1.5, posY = 1.5;
    private double dirX = -1.0, dirY = 0.0;
    private double planeX = 0.0, planeY = 0.66;
    private final double collisionRadius = 0.2;
    private final double WALL_HEIGHT = 1.0;
    private final int textureSize = 64;

    // --- WEAPON STATS ---
    private final int MAX_CLIP_HANDGUN = 10;
    private final int MAX_CLIP_SHOTGUN = 2;
    private final int MAX_CLIP_AR = 30;

    // --- HUD/GAME STATS ---
    private int health = 100;
    private int totalAmmo = 50;
    private int armor = 50;
    private int selectedWeapon = 1;
    private int clipAmmo = MAX_CLIP_HANDGUN;
    private boolean isFiring = false;
    private boolean isReloading = false;
    private double fireTimer = 0.0;
    private double reloadTimer = 0.0;
    private final double fireDuration = 0.2;
    private final double reloadDuration = 1.5;
    private final Font hudFont = Font.font("Consolas", FontWeight.BOLD, 24);

    // --- ENGINE CONSTANTS ---
    private final double moveSpeed = 3.0;
    private final double rotSpeed = 2.0;
    private long lastFrameTime = 0;
    private double currentFPS = 0;

    // --- INPUT FLAGS ---
    private boolean movingForward = false;
    private boolean movingBackward = false;
    private boolean turningLeft = false;
    private boolean turningRight = false;

    public DoomEngine() {
        clipAmmo = MAX_CLIP_HANDGUN;
    }

    /**
     * Handles all per-frame game logic.
     */
    public void update(long now) {
        if (lastFrameTime == 0) {
            lastFrameTime = now;
            return;
        }

        double deltaTime = (now - lastFrameTime) / 1_000_000_000.0;
        lastFrameTime = now;
        currentFPS = 1.0 / deltaTime;

        handleMovement(deltaTime);
        handleReloading(deltaTime);
        handleFiring(deltaTime);
        updateEntities(deltaTime);

        // Armor logic fix: no more cycling
        if (health < 0) health = 0;
        if (armor < 0) armor = 0;
    }

    // --- MOVEMENT AND COLLISION ---
    private void handleMovement(double delta) {
        double speed = delta * moveSpeed;
        double moveStepX = 0;
        double moveStepY = 0;

        if (movingForward) {
            moveStepX = dirX * speed;
            moveStepY = dirY * speed;
        }
        if (movingBackward) {
            moveStepX = -dirX * speed;
            moveStepY = -dirY * speed;
        }

        double newPosX = posX + moveStepX;
        double newPosY = posY + moveStepY;

        if (map[(int)posY][(int)(newPosX + Math.signum(moveStepX) * collisionRadius)] == 0 || Math.abs(moveStepX) < 1e-6) {
            posX = newPosX;
        } else {
            posX = (int)posX + (Math.signum(moveStepX) < 0 ? collisionRadius : 1.0 - collisionRadius);
        }

        if (map[(int)(newPosY + Math.signum(moveStepY) * collisionRadius)][(int)posX] == 0 || Math.abs(moveStepY) < 1e-6) {
            posY = newPosY;
        } else {
            posY = (int)posY + (Math.signum(moveStepY) < 0 ? collisionRadius : 1.0 - collisionRadius);
        }

        if (turningRight || turningLeft) {
            double rotation = (turningLeft ? -1.0 : 1.0) * rotSpeed * delta;
            double oldDirX = dirX;
            dirX = dirX * Math.cos(rotation) - dirY * Math.sin(rotation);
            dirY = oldDirX * Math.sin(rotation) + dirY * Math.cos(rotation);
            double oldPlaneX = planeX;
            planeX = planeX * Math.cos(rotation) - planeY * Math.sin(rotation);
            planeY = oldPlaneX * Math.sin(rotation) + planeY * Math.cos(rotation);
        }
    }

    // --- DAMAGE UTILITY ---
    private void applyDamage(int baseDamage) {
        // Armor absorbs 50% of the damage if armor is available
        int damageToArmor = (int) Math.min(armor, baseDamage * 0.5);

        armor -= damageToArmor;
        health -= (baseDamage - damageToArmor);

        System.out.println("Player took " + baseDamage + " damage. Armor used: " + damageToArmor + ". Health: " + health + " Armor: " + armor);
    }

    // --- FIRING, RELOADING, AND ENTITY MANAGEMENT ---

    private void handleReloading(double delta) {
        if (isReloading) {
            reloadTimer += delta;
            if (reloadTimer >= reloadDuration) {
                int required = getMaxClip(selectedWeapon) - clipAmmo;
                int transfer = Math.min(required, totalAmmo);

                clipAmmo += transfer;
                totalAmmo -= transfer;

                isReloading = false;
                reloadTimer = 0.0;
            }
        }
    }

    private void handleFiring(double delta) {
        if (isFiring) {
            fireTimer += delta;
            if (fireTimer >= fireDuration) {
                isFiring = false;
                fireTimer = 0.0;
            }
        }
    }

    private void fireWeapon() {
        if (isReloading || isFiring || clipAmmo <= 0) return;

        isFiring = true;
        fireTimer = 0.0;
        clipAmmo -= 1;

        double spawnX = posX + dirX * 0.3;
        double spawnY = posY + dirY * 0.3;

        // Add to entitiesToAdd list for safe merging later
        if (selectedWeapon == 1) {
            entitiesToAdd.add(new Entity(spawnX, spawnY, dirX, dirY, "Bullet", 8.0, 0.1, 0, 0.0));
        } else if (selectedWeapon == 2) {
            for (int i = 0; i < 5; i++) {
                double spread = (Math.random() - 0.5) * 0.2;
                double newDirX = dirX * Math.cos(spread) - dirY * Math.sin(spread);
                double newDirY = dirX * Math.sin(spread) + dirY * Math.cos(spread);
                entitiesToAdd.add(new Entity(spawnX, spawnY, newDirX, newDirY, "Bullet", 10.0, 0.05, 0, 0.0));
            }
        } else if (selectedWeapon == 3) {
            entitiesToAdd.add(new Entity(spawnX, spawnY, dirX, dirY, "Bullet", 15.0, 0.08, 0, 0.0));
        }
    }

    private void spawnEnemy(String enemyType) {
        final double spawnDistance = 5.0;

        double spawnX = posX + dirX * spawnDistance;
        double spawnY = posY + dirY * spawnDistance;

        int mapSpawnX = (int) spawnX;
        int mapSpawnY = (int) spawnY;

        double speed = 0.5;
        double size = 0.4;
        int health = 10;
        double initialAttackTimer = 0.0;

        switch (enemyType) {
            case "BigEnemy":
                health = 30;
                speed = 0.3;
                size = 0.6;
                break;
            case "GunEnemy":
                health = 5;
                speed = 1.0;
                size = 0.4;
                initialAttackTimer = 1.0; // Ready to fire immediately
                break;
            case "NormalEnemy":
            default:
                break;
        }

        if (mapSpawnX >= 0 && mapSpawnX < mapWidth &&
                mapSpawnY >= 0 && mapSpawnY < mapHeight &&
                map[mapSpawnY][mapSpawnX] == 0) {

            entitiesToAdd.add(new Entity(spawnX, spawnY, 0, 0, enemyType, speed, size, health, initialAttackTimer));
            System.out.println(enemyType + " Spawned in front of player at (" + spawnX + ", " + spawnY + ")");
        } else {
            System.out.println("Enemy failed to spawn: location is outside map or blocked by a wall.");
        }
    }

    private void spawnEnemyBullet(double x, double y, double dirX, double dirY) {
        // FIX: Spawn the bullet 0.5 units away from the enemy's center to prevent immediate collision/removal.
        final double offset = 0.5;
        double spawnX = x + dirX * offset;
        double spawnY = y + dirY * offset;

        entitiesToAdd.add(new Entity(spawnX, spawnY, dirX, dirY, "EnemyBullet", 5.0, 0.05, 0, 0.0));
    }

    /**
     * Logic for all entities, ensuring thread-safe additions and removals.
     */
    private void updateEntities(double delta) {
        Iterator<Entity> mainIter = entities.iterator();

        while (mainIter.hasNext()) {
            Entity entity = mainIter.next();
            double distToPlayer = Math.sqrt(Math.pow(posX - entity.x, 2) + Math.pow(posY - entity.y, 2));

            if (entity.type.contains("Enemy")) {
                // --- ENEMY LOGIC (Movement and State) ---

                double dx = posX - entity.x;
                double dy = posY - entity.y;
                double dist = distToPlayer;

                boolean shouldMove = true;
                boolean shouldAttack = false;

                if (entity.type.equals("GunEnemy")) {
                    final double retreatDistance = 5.0;
                    final double fireDistance = 10.0;

                    if (dist > fireDistance) {
                        entity.dirX = dx / dist;
                        entity.dirY = dy / dist;
                    } else if (dist < retreatDistance) {
                        entity.dirX = -dx / dist; // Retreat
                        entity.dirY = -dy / dist;
                    } else { // Firing range: 5.0 <= dist <= 10.0
                        shouldMove = false;
                        shouldAttack = true;
                        entity.dirX = dx / dist; // Aim
                        entity.dirY = dy / dist;
                    }
                } else { // NormalEnemy and BigEnemy (Melee)
                    if (dist > 1.0) {
                        entity.dirX = dx / dist;
                        entity.dirY = dy / dist;
                    } else {
                        shouldMove = false;
                        shouldAttack = true; // Melee attack when close
                    }
                }

                // Movement Execution
                if (shouldMove) {
                    double newX = entity.x + entity.dirX * entity.speed * delta;
                    double newY = entity.y + entity.dirY * entity.speed * delta;

                    if (map[(int)newY][(int)entity.x] == 0) entity.y = newY;
                    if (map[(int)entity.y][(int)newX] == 0) entity.x = newX;
                }

                // Attack Execution
                if (shouldAttack) {
                    entity.attackTimer += delta;
                    if (entity.type.equals("GunEnemy")) {
                        final double fireRate = 1.0;
                        if (entity.attackTimer >= fireRate) {
                            entity.attackTimer = 0.0;
                            spawnEnemyBullet(entity.x, entity.y, entity.dirX, entity.dirY);
                        }
                    } else { // NormalEnemy and BigEnemy Melee Attack
                        final double meleeRate = 0.5; // Attack twice per second
                        if (entity.attackTimer >= meleeRate) {
                            entity.attackTimer = 0.0;
                            int damage = entity.type.equals("BigEnemy") ? 10 : 5;
                            applyDamage(damage);
                        }
                    }
                }

                // Health Check
                if (entity.health <= 0) {
                    mainIter.remove();
                }

            } else if (entity.type.equals("Bullet")) {
                // --- PLAYER BULLET LOGIC ---

                // 1. Move and Wall Collision
                entity.x += entity.dirX * entity.speed * delta;
                entity.y += entity.dirY * entity.speed * delta;
                if (map[(int)entity.y][(int)entity.x] != 0) {
                    mainIter.remove();
                    continue;
                }

                // 2. Enemy Collision Check
                boolean removed = false;
                for (Entity target : entities) {
                    if (target.type.contains("Enemy")) {
                        double dx = entity.x - target.x;
                        double dy = entity.y - target.y;
                        double dist = Math.sqrt(dx * dx + dy * dy);

                        if (dist < entity.size + target.size) {
                            target.health -= 1;
                            mainIter.remove();
                            removed = true;
                            break;
                        }
                    }
                }
                if (removed) continue;

            } else if (entity.type.equals("EnemyBullet")) {
                // --- ENEMY BULLET LOGIC ---

                // 1. Move and Wall Collision
                entity.x += entity.dirX * entity.speed * delta;
                entity.y += entity.dirY * entity.speed * delta;
                if (map[(int)entity.y][(int)entity.x] != 0) {
                    mainIter.remove();
                    continue;
                }

                // 2. Player Collision Check
                double dx = entity.x - posX;
                double dy = entity.y - posY;
                double dist = Math.sqrt(dx * dx + dy * dy);

                if (dist < entity.size + collisionRadius) {
                    applyDamage(5);
                    mainIter.remove();
                }
            }
        }

        // Safely add any new entities created during this update frame
        if (!entitiesToAdd.isEmpty()) {
            entities.addAll(entitiesToAdd);
            entitiesToAdd.clear();
        }
    }

    // --- RENDERING (Unchanged) ---
    public void render(GraphicsContext gc, double width, double height) {
        // 1. Draw Floor/Ceiling
        gc.setFill(Color.web("#8b4513"));
        gc.fillRect(0, height / 2, width, height / 2);
        gc.setFill(Color.web("#00008b"));
        gc.fillRect(0, 0, width, height / 2);

        // 2. Raycasting Loop (Wall Rendering)
        for (int x = 0; x < width; x++) {
            double cameraX = 2 * x / width - 1;
            double rayDirX = dirX + planeX * cameraX;
            double rayDirY = dirY + planeY * cameraX;

            int mapX = (int)posX;
            int mapY = (int)posY;

            double sideDistX, sideDistY, perpWallDist;
            double deltaDistX = (rayDirX == 0) ? 1e30 : Math.abs(1 / rayDirX);
            double deltaDistY = (rayDirY == 0) ? 1e30 : Math.abs(1 / rayDirY);
            int stepX, stepY;
            boolean hit = false;
            int side = 0;

            if (rayDirX < 0) { stepX = -1; sideDistX = (posX - mapX) * deltaDistX; } else { stepX = 1; sideDistX = (mapX + 1.0 - posX) * deltaDistX; }
            if (rayDirY < 0) { stepY = -1; sideDistY = (posY - mapY) * deltaDistY; } else { stepY = 1; sideDistY = (mapY + 1.0 - posY) * deltaDistY; }

            while (!hit) {
                if (sideDistX < sideDistY) { sideDistX += deltaDistX; mapX += stepX; side = 0; } else { sideDistY += deltaDistY; mapY += stepY; side = 1; }
                if (map[mapY][mapX] > 0) hit = true;
            }

            if (side == 0) { perpWallDist = (mapX - posX + (1 - stepX) / 2) / rayDirX; } else { perpWallDist = (mapY - posY + (1 - stepY) / 2) / rayDirY; }

            int lineHeight = (int) (height / perpWallDist * WALL_HEIGHT);
            int drawStart = (int)(-lineHeight / 2 + height / 2);
            int drawEnd = (int)(lineHeight / 2 + height / 2);
            if (drawStart < 0) drawStart = 0;
            if (drawEnd >= height) drawEnd = (int)height - 1;

            Color wallColor;
            switch (map[mapY][mapX]) {
                case 1: wallColor = Color.web("#800000"); break;
                default: wallColor = Color.web("#4682b4"); break;
            }

            if (side == 1) { wallColor = wallColor.darker().darker(); }

            double wallX = (side == 0) ? posY + perpWallDist * rayDirY : posX + perpWallDist * rayDirX;
            wallX -= Math.floor(wallX);
            int texX = (int)(wallX * textureSize);
            if(side == 0 && rayDirX > 0) texX = textureSize - texX - 1;
            if(side == 1 && rayDirY < 0) texX = textureSize - texX - 1;

            gc.setStroke(wallColor);
            gc.setLineWidth(1);
            gc.strokeLine(x + 0.5, drawStart, x + 0.5, drawEnd);

            if (texX % 8 == 0) {
                gc.setStroke(wallColor.brighter());
                gc.strokeLine(x + 0.5, drawStart, x + 0.5, drawEnd);
            }
        }

        // 3. Render Entity Sprites
        renderEntities(gc, width, height);

        // 4. Draw Weapon Sprite (Foreground)
        drawWeapon(gc, width, height);

        // 5. Draw HUD (Overlay)
        drawHUD(gc, width, height);

        // 6. Display FPS (Debug Info)
        gc.setFill(Color.web("#ffffff"));
        gc.setFont(Font.font("Consolas", 12));
        gc.fillText(String.format("FPS: %.1f", currentFPS), 10, 20);
    }

    private void renderEntities(GraphicsContext gc, double width, double height) {
        // Sort entities by distance
        List<Entity> visibleEntities = new ArrayList<>(entities);
        visibleEntities.sort(Comparator.comparingDouble(e -> -((posX - e.x) * (posX - e.x) + (posY - e.y) * (posY - e.y))));

        for (Entity entity : visibleEntities) {
            double spriteX = entity.x - posX;
            double spriteY = entity.y - posY;

            double invDet = 1.0 / (planeX * dirY - dirX * planeY);
            double transformX = invDet * (dirY * spriteX - dirX * spriteY);
            double transformY = invDet * (-planeY * spriteX + planeX * spriteY);

            if (transformY <= 0.001) continue;

            int spriteScreenX = (int)((width / 2) * (1 + transformX / transformY));
            int spriteHeight = (int)(height / transformY * WALL_HEIGHT);
            int drawStartScreenY = (int)(-spriteHeight / 2 + height / 2);

            int spriteWidth = (int)(height / transformY * WALL_HEIGHT * entity.size);
            int drawStartScreenX = spriteScreenX - spriteWidth / 2;

            Color entityColor = null;

            // Determine Color
            if (entity.type.equals("NormalEnemy")) {
                entityColor = Color.RED;
            } else if (entity.type.equals("BigEnemy")) {
                entityColor = Color.BLUE;
            } else if (entity.type.equals("GunEnemy")) {
                entityColor = Color.GREEN;
            } else if (entity.type.equals("Bullet")) {
                entityColor = Color.YELLOW;
            } else if (entity.type.equals("EnemyBullet")) {
                entityColor = Color.ORANGE;
            }

            // Draw Entity
            if (entityColor != null && (entity.type.contains("Enemy"))) {
                gc.setFill(entityColor);
                gc.fillRect(drawStartScreenX, drawStartScreenY, spriteWidth, spriteHeight);

                // Draw Health Bar
                gc.setFill(Color.GREEN);
                double maxHealth = (entity.type.equals("BigEnemy") ? 30.0 : (entity.type.equals("GunEnemy") ? 5.0 : 10.0));
                double healthBarWidth = spriteWidth * (entity.health / maxHealth);
                if (healthBarWidth > spriteWidth) healthBarWidth = spriteWidth;

                gc.setFill(Color.web("#550000"));
                gc.fillRect(drawStartScreenX, drawStartScreenY - 10, spriteWidth, 5);

                gc.setFill(Color.GREEN);
                gc.fillRect(drawStartScreenX, drawStartScreenY - 10, healthBarWidth, 5);

            } else if (entity.type.equals("Bullet")) {
                gc.setFill(entityColor);
                gc.fillOval(spriteScreenX - 3, drawStartScreenY + spriteHeight / 2 - 3, 6, 6);
            } else if (entity.type.equals("EnemyBullet")) {
                gc.setFill(entityColor);
                gc.fillOval(spriteScreenX - 4, drawStartScreenY + spriteHeight / 2 - 4, 8, 8);
            }
        }

        // Muzzle flash after sprites but before weapon overlay
        if (isFiring && fireTimer < fireDuration / 2) {
            gc.setFill(Color.web("#ffcc00", 0.8));
            double flashSize = width * 0.05 + Math.random() * 10;
            gc.fillOval(width / 2 - flashSize / 2, height / 2 - flashSize / 2, flashSize, flashSize);
        }
    }

    private void drawWeapon(GraphicsContext gc, double width, double height) {
        double fireOffset = 0;
        if (isFiring) {
            double t = fireTimer / fireDuration;
            fireOffset = -20 * Math.sin(t * Math.PI);
        } else if (isReloading) {
            fireOffset = 50 * Math.sin(reloadTimer / reloadDuration * Math.PI);
        }

        double centerX = width / 2;
        double gunBaseY = height * 0.95 + fireOffset;
        double gunWidth = width * 0.4;
        double gunHeight = height * 0.3;

        gc.setFill(Color.web("#444444"));

        if (selectedWeapon == 1) {
            gc.fillRect(centerX - gunWidth * 0.1, gunBaseY - gunHeight * 0.5, gunWidth * 0.2, gunHeight * 0.5);
            gc.fillRect(centerX - gunWidth * 0.2, gunBaseY - gunHeight * 0.2, gunWidth * 0.4, gunHeight * 0.2);
            gc.setFill(Color.web("#a0a0a0"));
            gc.fillRect(centerX - gunWidth * 0.05, gunBaseY - gunHeight * 0.6, gunWidth * 0.1, gunHeight * 0.1);
        } else if (selectedWeapon == 2) {
            gc.setFill(Color.web("#696969"));
            gc.fillPolygon(
                    new double[]{centerX - gunWidth * 0.5, centerX + gunWidth * 0.5, centerX + gunWidth * 0.4, centerX - gunWidth * 0.4},
                    new double[]{gunBaseY, gunBaseY, gunBaseY - gunHeight * 0.3, gunBaseY - gunHeight * 0.3},
                    4
            );
            gc.setFill(Color.web("#a0a0a0"));
            gc.fillRect(centerX - gunWidth * 0.05, gunBaseY - gunHeight * 0.4, gunWidth * 0.1, gunHeight * 0.1);
        } else if (selectedWeapon == 3) {
            gc.setFill(Color.web("#333333"));
            gc.fillRect(centerX - gunWidth * 0.3, gunBaseY - gunHeight * 0.3, gunWidth * 0.6, gunHeight * 0.15);
            gc.fillRect(centerX + gunWidth * 0.2, gunBaseY - gunHeight * 0.15, gunWidth * 0.1, gunHeight * 0.1);
            gc.setFill(Color.web("#a0a0a0"));
            gc.fillRect(centerX - gunWidth * 0.05, gunBaseY - gunHeight * 0.4, gunWidth * 0.1, gunHeight * 0.1);
        }
    }

    private void drawHUD(GraphicsContext gc, double width, double height) {
        double hudHeight = height * 0.15;
        double hudY = height - hudHeight;

        gc.setFill(Color.web("#222222"));
        gc.fillRect(0, hudY, width, hudHeight);
        gc.setStroke(Color.web("#808080"));
        gc.setLineWidth(2);
        gc.strokeLine(0, hudY, width, hudY);

        gc.setFont(hudFont);

        // LEFT SIDE: ARMOR and HEALTH
        double leftX = width * 0.05;
        gc.setFill(Color.web("#c0c0c0")); gc.fillText("HEALTH", leftX, hudY + hudHeight * 0.25);
        gc.setFill(getStatColor(health)); gc.fillText(String.format("%3d", health) + "%", leftX + 10, hudY + hudHeight * 0.7);

        double armorX = width * 0.25;
        gc.setFill(Color.web("#c0c0c0")); gc.fillText("ARMOR", armorX, hudY + hudHeight * 0.25);
        gc.setFill(getStatColor(armor)); gc.fillText(String.format("%3d", armor) + "%", armorX + 10, hudY + hudHeight * 0.7);

        // CENTER: MUGSHOT / FACE
        double mugshotSize = hudHeight * 0.8;
        double mugshotX = width * 0.5 - mugshotSize / 2;
        double mugshotY = hudY + hudHeight * 0.1;

        gc.setFill(Color.web("#000000"));
        gc.fillRect(mugshotX, mugshotY, mugshotSize, mugshotSize);
        gc.setStroke(Color.web("#c0c0c0"));
        gc.setLineWidth(2);
        gc.strokeRect(mugshotX, mugshotY, mugshotSize, mugshotSize);

        Color faceColor = getStatColor(health).desaturate().brighter();
        if (isFiring) faceColor = Color.web("#ff0000");
        if (isReloading) faceColor = Color.web("#ffa500");
        if (health < 25) faceColor = Color.web("#ff0000").darker();

        gc.setFill(faceColor);
        gc.fillOval(mugshotX + mugshotSize * 0.2, mugshotY + mugshotSize * 0.2, mugshotSize * 0.6, mugshotSize * 0.6);
        gc.setFill(Color.web("red"));
        gc.fillOval(mugshotX + mugshotSize * 0.3, mugshotY + mugshotSize * 0.3, mugshotSize * 0.1, mugshotSize * 0.1);
        gc.fillOval(mugshotX + mugshotSize * 0.6, mugshotY + mugshotSize * 0.3, mugshotSize * 0.1, mugshotSize * 0.1);


        // RIGHT SIDE: AMMO and WEAPON
        double rightX = width * 0.70;

        // CLIP AMMO
        gc.setFill(Color.web("#c0c0c0"));
        String ammoLabel = isReloading ? "RELOAD" : "CLIP";
        gc.fillText(ammoLabel, rightX, hudY + hudHeight * 0.25);
        gc.setFill(isReloading ? Color.web("#ff0000") : Color.web("#ffff00"));
        gc.fillText(String.format("%3d", clipAmmo), rightX + 10, hudY + hudHeight * 0.7);

        // TOTAL AMMO
        gc.setFill(Color.web("#c0c0c0"));
        gc.fillText("/ " + totalAmmo, rightX + 60, hudY + hudHeight * 0.7);


        // WEAPON
        double weaponX = width * 0.85;
        gc.setFill(Color.web("#c0c0c0"));
        gc.fillText("WPN", weaponX, hudY + hudHeight * 0.25);

        gc.setFill(Color.web("#00ff00"));
        gc.fillText(String.valueOf(selectedWeapon), weaponX + 20, hudY + hudHeight * 0.7);
    }

    // Helper methods
    private Color getStatColor(int stat) {
        if (stat >= 75) return Color.web("#00ff00");
        if (stat >= 25) return Color.web("#ffff00");
        return Color.web("#ff0000");
    }

    private int getMaxClip(int weaponId) {
        return switch (weaponId) {
            case 1 -> MAX_CLIP_HANDGUN;
            case 2 -> MAX_CLIP_SHOTGUN;
            case 3 -> MAX_CLIP_AR;
            default -> 0;
        };
    }

    // --- INPUT HANDLING ---

    public void handleMouseClick(MouseButton button) {
        if (button == MouseButton.PRIMARY) {
            fireWeapon();
        }
    }

    public void handleKeyPress(KeyCode keyCode) {
        if (keyCode == KeyCode.W || keyCode == KeyCode.UP) {
            movingForward = true;
        } else if (keyCode == KeyCode.S || keyCode == KeyCode.DOWN) {
            movingBackward = true;
        } else if (keyCode == KeyCode.A || keyCode == KeyCode.LEFT) {
            turningLeft = true;
        } else if (keyCode == KeyCode.D || keyCode == KeyCode.RIGHT) {
            turningRight = true;
        } else if (keyCode == KeyCode.R) {
            if (clipAmmo < getMaxClip(selectedWeapon) && totalAmmo > 0 && !isReloading) {
                isReloading = true;
                reloadTimer = 0.0;
            }
        } else if (keyCode == KeyCode.DIGIT1) {
            changeWeapon(1);
        } else if (keyCode == KeyCode.DIGIT2) {
            changeWeapon(2);
        } else if (keyCode == KeyCode.DIGIT3) {
            changeWeapon(3);
        } else if (keyCode == KeyCode.DIGIT4) {
            spawnEnemy("NormalEnemy"); // Red
        } else if (keyCode == KeyCode.DIGIT5) {
            spawnEnemy("BigEnemy"); // Blue
        } else if (keyCode == KeyCode.DIGIT6) {
            spawnEnemy("GunEnemy"); // Green
        }
    }

    public void handleKeyRelease(KeyCode keyCode) {
        if (keyCode == KeyCode.W || keyCode == KeyCode.UP) {
            movingForward = false;
        } else if (keyCode == KeyCode.S || keyCode == KeyCode.DOWN) {
            movingBackward = false;
        } else if (keyCode == KeyCode.A || keyCode == KeyCode.LEFT) {
            turningLeft = false;
        } else if (keyCode == KeyCode.D || keyCode == KeyCode.RIGHT) {
            turningRight = false;
        }
    }

    private void changeWeapon(int newWeaponId) {
        if (selectedWeapon == newWeaponId || isFiring || isReloading) return;

        selectedWeapon = newWeaponId;
        clipAmmo = getMaxClip(selectedWeapon);
    }
}