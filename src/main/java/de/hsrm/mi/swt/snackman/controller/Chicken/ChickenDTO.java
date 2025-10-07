package de.hsrm.mi.swt.snackman.controller.Chicken;

import de.hsrm.mi.swt.snackman.entities.mobileObjects.eatingMobs.Chicken.Thickness;
import de.hsrm.mi.swt.snackman.entities.mobileObjects.eatingMobs.Chicken.Chicken;
import de.hsrm.mi.swt.snackman.entities.mobileObjects.eatingMobs.Chicken.Direction;

public record ChickenDTO(long id, int chickenPosX, int chickenPosZ, Thickness thickness, Direction lookingDirection) {
    public static ChickenDTO fromChicken(Chicken chicken) {
        return new ChickenDTO(chicken.getId(), chicken.getChickenPosX(), chicken.getChickenPosZ(), chicken.getThickness(), chicken.getLookingDirection());
    }
}
