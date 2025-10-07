package de.hsrm.mi.swt.snackman.messaging.MessageLoop;

import de.hsrm.mi.swt.snackman.entities.mobileObjects.eatingMobs.Chicken.Chicken;
import de.hsrm.mi.swt.snackman.entities.mobileObjects.eatingMobs.Chicken.Direction;
import de.hsrm.mi.swt.snackman.entities.mobileObjects.eatingMobs.Chicken.Thickness;

public record ChickenUpdateMessage(long id, int chickenPosX, int chickenPosZ, Thickness thickness,
                                   Direction lookingDirection, boolean isScared) {
    public static ChickenUpdateMessage fromChicken(Chicken chicken) {
        return new ChickenUpdateMessage(chicken.getId(), chicken.getChickenPosX(), chicken.getChickenPosZ(),
                chicken.getThickness(), chicken.getLookingDirection(), chicken.isScared());
    }
}
