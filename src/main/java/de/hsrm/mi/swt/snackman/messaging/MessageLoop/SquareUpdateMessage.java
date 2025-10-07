package de.hsrm.mi.swt.snackman.messaging.MessageLoop;

import de.hsrm.mi.swt.snackman.controller.Square.SquareDTO;

public record SquareUpdateMessage(SquareDTO square) {
}
