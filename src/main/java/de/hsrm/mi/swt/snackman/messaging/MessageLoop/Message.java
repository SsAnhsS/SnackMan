package de.hsrm.mi.swt.snackman.messaging.MessageLoop;

public record Message<T>(EventEnum event, T message) {

}
