package com.lyit.csd;

import java.io.IOException;
import java.text.ParseException;

/**
 * This class is the main class containing the main method of the application
 */
public class Main {

  /**
   * The main method is a so-called entry point where the program's execution begins.
   *
   * @param args required parameter of the main method.
   * @throws IOException thrown if wrong data is entered.
   * @throws InterruptedException thrown when a thread is interrupted while it's waiting
   * sleeping, * or otherwise occupied.
   * @throws ParseException if underlying service fails.
   */
  public static void main(String[] args) throws IOException, InterruptedException, ParseException {

    // Creating and instance of the console.
    Console console = new Console();
    // Running the console.
    console.run();

  }
}