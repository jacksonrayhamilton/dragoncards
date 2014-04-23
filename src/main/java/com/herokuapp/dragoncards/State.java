package com.herokuapp.dragoncards;

/**
 * Represents the state of a player at any given time.
 * 
 * @author Jackson Hamilton
 */
public enum State {
  IN_LIMBO, IN_LOBBY, INITIATING_DUEL, DUELING, WAITING_FOR_OPPONENT,
  COLLECTING, CHOOSING_ATTACK
}
