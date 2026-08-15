package com.pvz.models.entities.plants.config.explosive;

import com.pvz.models.entities.plants.config.PlantActionConfig;

public class SquashConfig extends PlantActionConfig {
    public String turnClip="turn";
    public String jumpUpRightClip="jump_up_right";
    public String jumpUpLeftClip="jump_up_left";
    public String jumpDownRightClip="jump_down_right";
    public String jumpDownLeftClip="jump_down_left";
    public float turnDuration=1.19f;
    public float jumpUpDuration=0.8f;
    public float jumpDownDuration=0.8f;
    public float radarRangeCoefficient=1.2f;
    public float jumpHeightCoefficient=0.7f;
    public float baseDamage = 1800f;

}
