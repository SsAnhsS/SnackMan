package de.hsrm.mi.swt.snackman.controller.Square;

import de.hsrm.mi.swt.snackman.controller.Snack.SnackDTO;
import de.hsrm.mi.swt.snackman.entities.map.Square;
import de.hsrm.mi.swt.snackman.entities.mapObject.MapObjectType;

public record SquareDTO(long id, int indexX, int indexZ, MapObjectType type, SnackDTO snack) {

    public static SquareDTO fromSquare(Square s) {
        return new SquareDTO(s.getId(), s.getIndexX(), s.getIndexZ(), s.getType(), SnackDTO.fromSnack(s.getSnack()));
    }
}
