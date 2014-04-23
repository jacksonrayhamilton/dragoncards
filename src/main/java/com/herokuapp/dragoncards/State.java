package com.herokuapp.dragoncards;

/**
 * Represents the state of a player at any given time.
 * 
 * @author Jackson Hamilton
 */
public enum State {
  NAMING, IN_LIMBO, IN_LOBBY, DUELING, WAITING_FOR_OPPONENT,
  CHOOSING_PRELIMINARY_ACTION, CHOOSING_ATTACK
}
