package com.herokuapp.dragoncards;

import java.util.Random;

/**
 * Generates random numbers as the name strongly implies.
 * 
 * @author Jackson Hamilton
 */
public class RandomNumberGenerator {
  private static Random random = new Random();

  public static int inclusiveRange(int min, int max) {
    return random.nextInt((max - min) + 1) + min;
  }
}
