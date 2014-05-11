package com.herokuapp.dragoncards;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.herokuapp.dragoncards.game.Card;
import com.herokuapp.dragoncards.game.Dragon;
import com.herokuapp.dragoncards.game.Element;

public class DragonTests {

  @Test
  public void equals_NumerousDragons_Equal() {
    assertEquals(new Dragon(Element.FIRE, 1), new Dragon(Element.FIRE, 1));
    assertEquals(new Dragon(Element.FIRE, 2), new Dragon(Element.FIRE, 2));
    assertEquals(new Dragon(Element.FIRE, 1), new Card(Element.FIRE, 1));
    assertEquals(new Card(Element.FIRE, 1), new Dragon(Element.FIRE, 1));
    assertEquals(new Card(Element.FIRE, 1), new Card(Element.FIRE, 1));
  }

}
