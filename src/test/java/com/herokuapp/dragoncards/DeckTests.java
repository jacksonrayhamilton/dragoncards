package com.herokuapp.dragoncards;

import static org.junit.Assert.assertArrayEquals;

import java.util.HashMap;
import java.util.Map.Entry;

import org.junit.Test;

import com.herokuapp.dragoncards.game.Card;
import com.herokuapp.dragoncards.game.Deck;
import com.herokuapp.dragoncards.game.Element;

public class DeckTests {

  /**
   * First is number of occurrences of an element, last 3 are occurrences of
   * each level.
   */
  private static final int[] DISTRIBUTIONS = new int[] {
      15, 3, 3, 3, 3, 3
  };

  @Test
  public void test_Constructor_Constructed_InitiallyRandomizedAndComplete() {
    Deck deck = new Deck();
    HashMap<Element, int[]> elementCounts = new HashMap<>();
    for (Element element : Element.ELEMENTS) {
      elementCounts.put(element, new int[] {
          0, 0, 0, 0, 0, 0
      });
    }

    while (!deck.isEmpty()) {
      Card card = deck.draw();
      int[] counts = elementCounts.get(card.getElement());
      counts[0] += 1;
      counts[card.getLevel()] += 1;
    }

    for (Entry<Element, int[]> entry : elementCounts.entrySet()) {
      assertArrayEquals(DISTRIBUTIONS, entry.getValue());
    }
  }
}
