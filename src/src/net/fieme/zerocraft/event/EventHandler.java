package net.fieme.zerocraft.event;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Meant to be used in EventListener<br>
 * An annotation that marks this method as an event handler
 */
@Retention(RUNTIME)
@Target(METHOD)
public @interface EventHandler {
}