package com.herokuapp.dragoncards;

import java.util.Random;

public class RandomNumberGenerator {
  private static Random random = new Random();

  public static int inclusiveRange(int min, int max) {
    return random.nextInt((max - min) + 1) + min;
  }
}
