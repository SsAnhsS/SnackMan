package de.hsrm.mi.swt.snackman.messaging.MessageLoop;

import de.hsrm.mi.swt.snackman.entities.mobileObjects.ScriptGhost;
import de.hsrm.mi.swt.snackman.entities.mobileObjects.eatingMobs.Chicken.Direction;

public record ScriptGhostDTO(long id, int scriptGhostPosX, int scriptGhostPosZ, Direction lookingDirection) {

    public static ScriptGhostDTO fromScriptGhost(ScriptGhost scriptGhost) {
        return new ScriptGhostDTO(scriptGhost.getId(), scriptGhost.getGhostPosX(), scriptGhost.getGhostPosZ(), scriptGhost.getLookingDirection());
    }

}
