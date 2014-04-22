package com.herokuapp.dragoncards.game;

public enum Element {

  WOOD, FIRE, EARTH, METAL, WATER;

  public static Element getDominated(Element element) {
    switch (element) {
      case WOOD:
        return EARTH;
      case FIRE:
        return METAL;
      case EARTH:
        return WATER;
      case METAL:
        return WOOD;
      case WATER:
        return FIRE;
      default:
        return null;
    }
  }

  public static Element getWeakness(Element element) {
    switch (element) {
      case WOOD:
        return METAL;
      case FIRE:
        return WATER;
      case EARTH:
        return WOOD;
      case METAL:
        return FIRE;
      case WATER:
        return EARTH;
      default:
        return null;
    }
  }

  public static final Element[] ELEMENTS = new Element[] {
      WOOD, FIRE, EARTH, METAL, WATER
  };
}
