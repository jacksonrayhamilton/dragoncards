package com.herokuapp.dragoncards.game;

/**
 * An action that is related to the action of "collecting" cards, be it from the
 * deck ("drawing") or from a discard pile ("pilfering").
 * 
 * @author Jackson Hamilton
 */
public enum CollectAction implements PreliminaryAction {
  DRAW, PILFER
}
