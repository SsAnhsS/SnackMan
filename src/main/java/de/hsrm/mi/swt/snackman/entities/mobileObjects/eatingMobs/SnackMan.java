package de.hsrm.mi.swt.snackman.entities.mobileObjects.eatingMobs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.hsrm.mi.swt.snackman.configuration.GameConfig;
import de.hsrm.mi.swt.snackman.entities.map.GameMap;
import de.hsrm.mi.swt.snackman.entities.map.Square;
import de.hsrm.mi.swt.snackman.entities.map.enums.WallAlignmentStatus;
import de.hsrm.mi.swt.snackman.entities.map.enums.WallSectionStatus;
import de.hsrm.mi.swt.snackman.entities.mapObject.snack.Snack;
import de.hsrm.mi.swt.snackman.entities.mapObject.snack.SnackType;
import de.hsrm.mi.swt.snackman.entities.mechanics.SprintHandler;
import de.hsrm.mi.swt.snackman.entities.mobileObjects.Ghost;
import de.hsrm.mi.swt.snackman.entities.mobileObjects.Mob;
import de.hsrm.mi.swt.snackman.entities.mobileObjects.ScriptGhost;

import static de.hsrm.mi.swt.snackman.configuration.GameConfig.DOUBLE_JUMP_CALORIE_COSTS;
import static de.hsrm.mi.swt.snackman.configuration.GameConfig.SINGLE_JUMP_CALORIE_COSTS;

public class SnackMan extends EatingMob {
    private final Logger log = LoggerFactory.getLogger(SnackMan.class);
    private final int GAME_FINISH_BECAUSE_OF_TOO_FEW_CKAL = -1;
    private boolean isJumping = false;
    private double velocityY = 0.0;
    private boolean isSprinting = false;
    private SprintHandler sprintHandler = new SprintHandler();
    private boolean isScared = false;
    private boolean hasDoubleJumped = false;
    private long jumpStartTime = 0;
    private long elapsedTime = 0;
    private double gravity = GameConfig.GRAVITY;
    private boolean squareUnderneathIsWall = false;
    private long lastHit;

    public SnackMan(GameMap gameMap, Square currentSquare, double posX, double posY, double posZ) {
        this(gameMap, GameConfig.SNACKMAN_SPEED, GameConfig.SNACKMAN_RADIUS, posX, posY, posZ);

        currentSquare.addMob(this);
    }

    // only for tests??
    public SnackMan(GameMap gameMap, double speed, double radius) {
        super(gameMap, speed, radius);
    }

    public SnackMan(GameMap gameMap, double speed, double radius, double posX, double posY, double posZ) {
        super(gameMap, speed, radius, posX, posY, posZ);

        this.setKcal(GameConfig.SNACKMAN_START_CALORIES);
        lastHit = System.currentTimeMillis();
    }

    public void isScaredFromGhost(boolean scared) {
        if (scared) {
            if(System.currentTimeMillis() - lastHit >= GameConfig.INVINCIBILITY_TIME){
                if (super.getKcal() > GameConfig.GHOST_DAMAGE) {
                    setKcal(getKcal() - GameConfig.GHOST_DAMAGE);
                    isScared = true;
                    lastHit = System.currentTimeMillis();
                } else super.setKcal(GAME_FINISH_BECAUSE_OF_TOO_FEW_CKAL);
            }
        } else {
            isScared = false;
        }
    }

    //JUMPING
    public void jump() {
        if (!isJumping && getKcal() >= SINGLE_JUMP_CALORIE_COSTS) {
            this.velocityY = GameConfig.JUMP_STRENGTH;
            this.isJumping = true;
            this.hasDoubleJumped = false;
            subtractCaloriesSingleJump();
        }

    }

    public void doubleJump() {
        if (isJumping && getKcal() >= DOUBLE_JUMP_CALORIE_COSTS) {
            this.velocityY += GameConfig.DOUBLEJUMP_STRENGTH;
            subtractCaloriesDoubleJump();
            this.hasDoubleJumped = true;
        }
    }

    /**
     * Updates the player's vertical position during a jump, applying gravity and handling wall interactions. 
     * The player is pushed based on wall alignment and section, and if the player falls below ground level, they are respawned.
     * @param deltaTime Time in seconds since the last update, used for position and velocity calculations.
     */
    public void updateJumpPosition(double deltaTime) {
        if (isJumping) {
            if (this.getPosY() < GameConfig.SNACKMAN_GROUND_LEVEL) {
                this.isJumping = false;
                this.velocityY = 0;
                this.hasDoubleJumped = false;
                jumpStartTime = 0;
                gravity = GameConfig.GRAVITY;
                respawn();
            }

            if (jumpStartTime == 0) {
                jumpStartTime = System.currentTimeMillis();
            }
            elapsedTime = System.currentTimeMillis() - jumpStartTime;
            if (elapsedTime >= 1000) {
                gravity = GameConfig.GRAVITY_AFTER_TIMEOUT;
            }

            this.velocityY += gravity * deltaTime;
            this.setPosY(this.getPosY() + this.velocityY * deltaTime);

            squareUnderneathIsWall = squareUnderneathIsWall();

            if (this.getPosY() <= GameConfig.SQUARE_HEIGHT && squareUnderneathIsWall) {
                WallAlignmentStatus wallAlignment = checkWallAlignment();
                WallSectionStatus wallSection = getWallSection();

                switch (wallAlignment) {
                    case WallAlignmentStatus.CASE0_NONE:
                        pushback();
                        break;
                    case WallAlignmentStatus.CASE1_LEFT_RIGHT:
                        if (wallSection == WallSectionStatus.CASE1_TOP_LEFT || wallSection == WallSectionStatus.CASE2_TOP_RIGHT) {
                            push_forward();
                        } else {
                            push_backward();
                        }
                        break;
                    case WallAlignmentStatus.CASE2_TOP_BOTTOM:
                        if (wallSection == WallSectionStatus.CASE1_TOP_LEFT || wallSection == WallSectionStatus.CASE3_BOTTOM_LEFT) {
                            push_left();
                        } else {
                            push_right();
                        }
                        break;
                    case WallAlignmentStatus.CASE3_BOTTOM_LEFT:
                        if (wallSection == WallSectionStatus.CASE1_TOP_LEFT || wallSection == WallSectionStatus.CASE2_TOP_RIGHT) {
                            push_forward();
                        } else if (wallSection == WallSectionStatus.CASE4_BOTTOM_RIGHT) {
                            push_right();
                        } else {
                            pushback();
                        }
                        break;
                    case WallAlignmentStatus.CASE4_TOP_LEFT:
                        if (wallSection == WallSectionStatus.CASE3_BOTTOM_LEFT || wallSection == WallSectionStatus.CASE4_BOTTOM_RIGHT) {
                            push_backward();
                        } else if (wallSection == WallSectionStatus.CASE2_TOP_RIGHT) {
                            push_right();
                        } else {
                            pushback();
                        }
                        break;
                    case WallAlignmentStatus.CASE5_TOP_RIGHT:
                        if (wallSection == WallSectionStatus.CASE1_TOP_LEFT || wallSection == WallSectionStatus.CASE3_BOTTOM_LEFT) {
                            push_left();
                        } else if (wallSection == WallSectionStatus.CASE4_BOTTOM_RIGHT) {
                            push_backward();
                        } else {
                            pushback();
                        }
                        break;
                    case WallAlignmentStatus.CASE6_BOTTOM_RIGHT:
                        if (wallSection == WallSectionStatus.CASE1_TOP_LEFT || wallSection == WallSectionStatus.CASE2_TOP_RIGHT) {
                            push_forward();
                        } else if (wallSection == WallSectionStatus.CASE3_BOTTOM_LEFT) {
                            push_left();
                        } else {
                            pushback();
                        }
                        break;
                    case WallAlignmentStatus.CASE7_BOTTOM_LEFT_RIGHT:
                        if (wallSection == WallSectionStatus.CASE1_TOP_LEFT || wallSection == WallSectionStatus.CASE2_TOP_RIGHT) {
                            push_forward();
                        } else {
                            pushback();
                        }
                        break;
                    case WallAlignmentStatus.CASE8_TOP_BOTTOM_LEFT:
                        if (wallSection == WallSectionStatus.CASE2_TOP_RIGHT || wallSection == WallSectionStatus.CASE4_BOTTOM_RIGHT) {
                            push_right();
                        } else {
                            pushback();
                        }
                        break;
                    case WallAlignmentStatus.CASE9_TOP_LEFT_RIGHT:
                        if (wallSection == WallSectionStatus.CASE3_BOTTOM_LEFT || wallSection == WallSectionStatus.CASE4_BOTTOM_RIGHT) {
                            push_backward();
                        } else {
                            pushback();
                        }
                        break;
                    case WallAlignmentStatus.CASE10_TOP_BOTTOM_RIGHT:
                        if (wallSection == WallSectionStatus.CASE1_TOP_LEFT || wallSection == WallSectionStatus.CASE3_BOTTOM_LEFT) {
                            push_left();
                        } else {
                            pushback();
                        }
                        break;
                    case WallAlignmentStatus.CASE11_BOTTOM:
                        if (wallSection == WallSectionStatus.CASE1_TOP_LEFT || wallSection == WallSectionStatus.CASE2_TOP_RIGHT) {
                            push_forward();
                        } else if (wallSection == WallSectionStatus.CASE3_BOTTOM_LEFT) {
                            push_left();
                        } else {
                            push_right();
                        }
                        break;
                    case WallAlignmentStatus.CASE12_LEFT:
                        if (wallSection == WallSectionStatus.CASE1_TOP_LEFT) {
                            push_forward();
                        } else if (wallSection == WallSectionStatus.CASE2_TOP_RIGHT || wallSection == WallSectionStatus.CASE4_BOTTOM_RIGHT) {
                            push_right();
                        } else {
                            push_backward();
                        }
                        break;
                    case WallAlignmentStatus.CASE13_TOP:
                        if (wallSection == WallSectionStatus.CASE1_TOP_LEFT) {
                            push_left();
                        } else if (wallSection == WallSectionStatus.CASE2_TOP_RIGHT) {
                            push_right();
                        } else {
                            push_backward();
                        }
                        break;
                    case WallAlignmentStatus.CASE14_RIGHT:
                        if (wallSection == WallSectionStatus.CASE1_TOP_LEFT || wallSection == WallSectionStatus.CASE3_BOTTOM_LEFT) {
                            push_left();
                        } else if (wallSection == WallSectionStatus.CASE2_TOP_RIGHT) {
                            push_forward();
                        } else {
                            push_backward();
                        }
                        break;
                }
            }

            if (this.getPosY() <= GameConfig.SNACKMAN_GROUND_LEVEL) {
                this.setPosY(GameConfig.SNACKMAN_GROUND_LEVEL);
                this.isJumping = false;
                this.velocityY = 0;
                this.hasDoubleJumped = false;
                jumpStartTime = 0;
                gravity = GameConfig.GRAVITY;
            }
        }
    }

    public void subtractCaloriesSingleJump() {
        setKcal(getKcal() - SINGLE_JUMP_CALORIE_COSTS);
    }

    public void subtractCaloriesDoubleJump() {
        if (!hasDoubleJumped) {
            setKcal(getKcal() - DOUBLE_JUMP_CALORIE_COSTS);
        }
    }

    public int getCurrentCalories() {
        return super.getKcal();
    }

    @Override
    public void move(boolean forward, boolean backward, boolean left, boolean right, double delta, GameMap gameMap) {
        if (isSprinting) {
            if (sprintHandler.canSprint()) {
                setSpeed(GameConfig.SNACKMAN_SPEED * GameConfig.SNACKMAN_SPRINT_MULTIPLIER);
            } else {
                sprintHandler.stopSprint();
                setSpeed(GameConfig.SNACKMAN_SPEED);
            }
        } else {
            sprintHandler.stopSprint();
            setSpeed(GameConfig.SNACKMAN_SPEED);
        }

        Square oldSquare = gameMap.getSquareAtIndexXZ(calcMapIndexOfCoordinate(super.getPosX()), calcMapIndexOfCoordinate(super.getPosZ()));
        super.move(forward, backward, left, right, delta, gameMap);
        Square newSquare = gameMap.getSquareAtIndexXZ(calcMapIndexOfCoordinate(super.getPosX()), calcMapIndexOfCoordinate(super.getPosZ()));

        if (!oldSquare.equals(newSquare)) {
            oldSquare.removeMob(this);
            newSquare.addMob(this);
        }

        // when snackman runs into a ghost
        for (Mob mob : newSquare.getMobs()) {
            if (mob instanceof Ghost || mob instanceof ScriptGhost) {
                this.isScaredFromGhost(true);
            } else {
                this.isScaredFromGhost(false);
            }
        }
    }

    public int getSprintTimeLeft() {
        return sprintHandler.getSprintTimeLeft();
    }

    public boolean isInCooldown() {
        return sprintHandler.isInCooldown();
    }

    public boolean isSprinting() {
        return isSprinting;
    }

    public void setSprinting(boolean sprinting) {
        this.isSprinting = sprinting;

        if (sprinting) {
            if (sprintHandler.canSprint()) {
                sprintHandler.startSprint();
            } else {
                this.isSprinting = false;
            }
        } else {
            sprintHandler.stopSprint();
        }
    }

    /**
     * Collects the snack on the square if there is one.
     * If there is one that remove it from the square.
     *
     * @param square to eat the snack from
     */
    @Override
    public void consumeSnackOnSquare(Square square) {
        Snack snackOnSquare = square.getSnack();

        if (snackOnSquare.getSnackType() != SnackType.EMPTY) {
            try {
                super.gainKcal(snackOnSquare.getCalories());
            } catch (Exception e) {
                log.error(e.getMessage());
            }

            //set snack to null after consuming it
            square.setSnack(new Snack(SnackType.EMPTY));
        }
    }

    public boolean isJumping() {
        return isJumping;
    }

    public void setIsJumping(boolean isJumping) {
        this.isJumping = isJumping;
    }

    public double getVelocityY() {
        return velocityY;
    }

    public void setSprintHandler(SprintHandler sprintHandler) {
        this.sprintHandler = sprintHandler;
    }

    public boolean hasDoubleJumped() {
        return hasDoubleJumped;
    }

    public void setHasDoubleJumped(boolean valueHasDoubleJumped) {
        hasDoubleJumped = valueHasDoubleJumped;
    }

    public boolean isScared() {
        return isScared;
    }
}
