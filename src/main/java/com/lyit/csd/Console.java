package com.lyit.csd;


import com.lyit.csd.UnclosableInputStreamDecorator;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Date;

import java.util.Locale;
import java.util.Scanner;

/**
 * Current class represents the console.
 */
public class Console {


  /**
   * User's unique API key.
   */
  private String key = "J0E2Ge85rgajGHOO28u0R7gcZ3T0SjC44f5RzekF";

  /**
   * User object instance with unique API key.
   */
  private User user = new User(key);

  /**
   * Representation of exit proposal.
   */
  private boolean isExit = false;

  /**
   * Determination of the first run.
   */
  private boolean isFirstRun = true;

  /**
   * ConnectionPort details for request.
   */
  private ConnectionPort cp;

  /**
   * ANSI code for changing background color to green.
   */
  public static final String ANSI_GREEN = "\u001B[32m";

  /**
   * ANSI code for changing background color to yellow.
   */
  public static final String ANSI_YELLOW = "\u001B[33m";

  /**
   * ANSI code for color reset.
   */
  public static final String ANSI_RESET = "\u001B[0m";


  /**
   * Method to run the application. It handles console display, handling user options and
   * prompts user with appropriate requests.
   *
   * @throws IOException if underlying service fails.
   * @throws InterruptedException if underlying service fails.
   * @throws ParseException if underlying service fails.
   */
  public void run() throws IOException, InterruptedException, ParseException {

    // Initial wording to be displayed.
    System.out.println("Welcome to portfolio system.");
    System.out.println("Please follow the commands on the console.");
    System.out.println();

    // Scanner for user input.
    Scanner scanner = new Scanner(new UnclosableInputStreamDecorator(System.in));

    while (!isExit) {

      // Specific message if it is the initial run of the program.
      if (isFirstRun) {
        System.out.println("\n+-------------------------------------------------+");
        System.out.println("|         Welcome to the Portfolio System         |");
        System.out.println("|                      Menu                       |");
        System.out.println("+-------------------------------------------------+\n");
        isFirstRun = false;
      }

      System.out.println();
      // print choice menu
      selector();
      System.out.print("Please enter one of the options: ");

      if (scanner.hasNextInt()) {

        // user input
        int userInput = scanner.nextInt();

        // Handling user input.
        switch (userInput) {

          // Handling purchase asset request.
          case 1:

            // Displaying relevant information and requests.
            System.out.println("Selected: PURCHASE AN ASSET.");
            System.out.println(ANSI_YELLOW + "Currently in portfolio: " + user.getShortStatus() +
                    ANSI_RESET);
            String symbol = requiredString(ANSI_GREEN + "\nPlease enter the symbol: " +
                    ANSI_RESET);
            double amount = requiredDouble(ANSI_GREEN + "Please enter the amount: " +
                    ANSI_RESET);

            // Sending and handling user request.
            cp = new ConnectionPort(
                    "v6/finance/quote?region=US&lang=en&symbols=" + symbol, key);

            if (cp.getAssetQuote().isEmpty() || cp.getAssetQuote().get(0).getLivePrice() == 0) {
              System.out.println("Please enter a valid option. Transaction declined.");
            } else {
              AssetQuote quote = cp.getAssetQuote().get(0);

              System.out.println("\nYour quote: " +
                      "\nAsset Full Name  : " + quote.getAssetFullName() +
                      "\nAsset Symbol     : " + quote.getAssetSymbol() +
                      "\nLive Price       : " + quote.getLivePrice() + " USD" +
                      "\nTransaction cost : " + quote.getLivePrice() * amount + " USD");

              // Requesting purchase confirmation.
              String confirm = requiredString(ANSI_GREEN + "\nPlease type Y to "
                      + "confirm or any other key to cancel: " + ANSI_RESET);

              if (confirm.toLowerCase(Locale.ROOT).equals("y")) {
                double funds = user.getAvailableFunds();
                user.purchaseAsset(symbol, amount);
                if (user.getAvailableFunds() == funds) {
                  System.out.println("You don't have enough funds to make "
                          + "this transaction. Transaction declined.");
                  continue;
                }
                System.out.println("\nYou've purchased: " + quote.getAssetSymbol() + " x " + amount);
                System.out.println("Funds balance after purchase: " + user.getAvailableFunds() + " USD");
              } else {
                System.out.println("Transaction canceled by the user.");
              }
            }
            System.out.println("+-------------------------------------------------+");
            break;

          // Handling sell asset request.
          case 2:

            // Displaying relevant information and requests.
            System.out.println("Selected: SELL AN ASSET.");
            System.out.println(ANSI_YELLOW + "Currently in portfolio: " + user.getShortStatus() +
                    ANSI_RESET);
            symbol = requiredString(ANSI_GREEN + "\nPlease enter the symbol: " + ANSI_RESET);
            amount = requiredDouble(ANSI_GREEN + "Please enter the amount: " + ANSI_RESET);

            // Sending and handling user request.
            cp = new ConnectionPort(
                    "v6/finance/quote?region=US&lang=en&symbols=" + symbol, key);

            if (cp.getAssetQuote().isEmpty() || cp.getAssetQuote().get(0).getLivePrice() == 0) {
              System.out.println("Transaction declined.");
              continue;
            } else {
              AssetQuote quote = cp.getAssetQuote().get(0);
              System.out.println("\nYour quote: " +
                      "\nAsset Full Name  : " + quote.getAssetFullName() +
                      "\nAsset Symbol     : " + quote.getAssetSymbol() +
                      "\nLive Price       : " + quote.getLivePrice() + " USD" +
                      "\nTransaction cost : " + quote.getLivePrice() * amount + " USD");

              // Requesting purchase confirmation.
              String confirm = requiredString(ANSI_GREEN + "\nPlease type Y to "
                      + "confirm or any other key to cancel: " + ANSI_RESET);

              if (confirm.toLowerCase(Locale.ROOT).equals("y")) {
                double fundsBefore = user.getAvailableFunds();
                user.sellAsset(symbol.toUpperCase(), amount);
                if (user.getAvailableFunds() == fundsBefore) {
                  System.out.println("Transaction declined as amount to sell is higher than in "
                          + "portfolio.");
                  continue;
                }
                System.out.println("\nYou've sold: " + quote.getAssetSymbol() + " x " + amount);
                System.out.println("Funds balance after sale: " + user.getAvailableFunds() + " USD");
              } else {
                System.out.println("Transaction canceled by the user.");
              }
            }
            System.out.println("+-------------------------------------------------+");
            break;

          // Not supported function as yet.
          case 3:

            break;

          // Not supported function as yet.
          case 4:

            break;

          // Not supported function as yet.
          case 5:

            break;

          // Handling asset quote request.
          case 6:

            // Displaying relevant information and requests.
            System.out.println("Selected: GET REALTIME QUOTE ON SPECIFIC ASSETS.\n");
            System.out.println(ANSI_YELLOW + "Please type the asset name to add to the quote list.\n"
                    + "** Enter 99 to get the quote **.\n" + ANSI_RESET);
            List<String> assetNames = requiredListTypeString(ANSI_GREEN + "Asset Name: " +
                    ANSI_RESET);

            // Data structure to hold current assets
            List<AssetQuote> quotes = user.getAssetInformation(assetNames);

            // Handling asset information.
            if (quotes.isEmpty()) {
              System.out.println("No matching results on the quotes.");
            } else {
              System.out.println(ANSI_GREEN + "\nAsset quotes :" + ANSI_RESET);
              for (AssetQuote quote : quotes) {
                System.out.println("Asset Symbol     : " + quote.getAssetSymbol());
                System.out.println("Asset Full Name  : " + quote.getAssetFullName());
                System.out.println("Asset type       : " + quote.getAssetType());
                System.out.println("Live Asset Price : " + quote.getLivePrice());
                System.out.println();
              }
            }

            System.out.println("+-------------------------------------------------+");
            break;

          // Handling portfolio value request.
          case 7:

            // Displaying relevant information and requests.
            System.out.println("Selected: GET TOTAL PORTFOLIO LIVE VALUE.");
            System.out.println(ANSI_YELLOW + "Currently in portfolio: " + user.getShortStatus() +
                    ANSI_RESET);
            System.out.println("\nTotal Portfolio Value is: " + user.getPortfolioValue() + " USD");
            System.out.println("+-------------------------------------------------+");
            break;

          // Handling list investment request.
          case 8:

            // Displaying relevant information and requests.
            System.out.println("Selected: LIST OF ALL INVESTMENT.\n");
            System.out.println(user.listAllInvestments());
            System.out.println("+-------------------------------------------------+");
            break;

          // Handling list portfolio request.
          case 9:

            // Displaying relevant information and requests.
            System.out.println("Selected: LIST SPECIFIC PORTFOLIO TYPE.\n");
            String type = requiredString(ANSI_GREEN + "Please enter S for 'STOCK' or C for 'CRYPTO'"
                    + "to list the portfolio type: " + ANSI_RESET);
            if (type.toLowerCase(Locale.ROOT).equals("s")) {
              System.out.println(user.listPortfolioAssetsByType("Stock"));
            } else if (type.toLowerCase(Locale.ROOT).equals("c")) {
              System.out.println(user.listPortfolioAssetsByType("Crypto"));
            } else {
              System.out.println("Invalid selection. Please enter 'S' or 'C'.\n");
            }
            System.out.println("+-------------------------------------------------+");
            break;

          // Handling asset information request.
          case 10:

            // Displaying relevant information and requests.
            System.out.println("Selected: LIST SPECIFIC ASSETS IN PORTFOLIO.\n");
            System.out.println(ANSI_YELLOW + "Please type the asset name to add to the quote list.\n"
                    + "** Enter 99 to get the quote **.\n" + ANSI_RESET);

            List<String> names = requiredListTypeString(ANSI_GREEN + "Please type full "
                    + "symbol or at least first three characters of the full asset name: " +
                    ANSI_RESET);

            String result = user.listPortfolioAssetsByName(names);

            if (result.isEmpty()) {
              System.out.println("No matching assets found.");
            } else {
              System.out.println(result);
            }
            System.out.println("+-------------------------------------------------+");
            break;

          // Handling list purchased asset in a range request.
          case 11:

            // Displaying relevant information and requests.
            System.out.println("Selected: LIST PURCHASED ASSETS IN SPECIFIC INTERVAL.\n");

            //Start Scanner to collect Dates
            Scanner purchaseDate1 = new Scanner(System.in);
            System.out.print("Please enter starting date (DD/MM/YYYY): ");
            String startDate1 = purchaseDate1.next();

            Scanner purchaseDate2 = new Scanner(System.in);
            System.out.print("Please enter ending date (DD/MM/YYYY): ");
            String endDate1 = purchaseDate2.next();

            //Check if Date is valid -- Regex Validation for date found at:
            // https://mkyong.com/regular-expressions/how-to-validate-date-with-regular-expression/
            if(startDate1.matches("(0?[1-9]|[12][0-9]|3[01])/(0?[1-9]|1[012])/((?:19|20)[0-9][0-9])")
              && endDate1.matches("(0?[1-9]|[12][0-9]|3[01])/(0?[1-9]|1[012])/((?:19|20)[0-9][0-9])")){

              SimpleDateFormat formData = new SimpleDateFormat("dd/MM/yyyy");

              //Convert the Date to UnixTimeStamp
              Date start = formData.parse(startDate1);
              long unixDate1 = start.getTime()/1000;

              //Convert the Date to UnixTimeStamp
              Date end = formData.parse(endDate1);
              long unixDate2 = end.getTime()/1000;

              if ( unixDate2<= unixDate1) {
                System.out.println("Enter appropriate interval. Please try again ... ");
              } else {
                System.out.println(user.listPortfolioPurchasesInRange(unixDate1, unixDate2));
              }

            } else {

              System.out.println("Please enter the correct Date format DD/MM/YYYY eg: 25/10/2021. Try again");

            }

            System.out.println("+-------------------------------------------------+");
            break;

          // Handling sold asset in a range request.
          case 12:

            // Displaying relevant information and requests.
            System.out.println("Selected: LIST SOLD ASSETS IN SPECIFIC INTERVAL.\n");
            //Start Scanner to collect Dates
            Scanner saleDate1 = new Scanner(System.in);
            System.out.print("Please enter starting date (DD/MM/YYYY): ");
            String startDate2 = saleDate1.next();

            Scanner saleDate2 = new Scanner(System.in);
            System.out.print("Please enter ending date (DD/MM/YYYY): ");
            String endDate2 = saleDate2.next();

            //Check if Date is valid -- Regex Validation for date found at:
            // https://mkyong.com/regular-expressions/how-to-validate-date-with-regular-expression/
            if(startDate2.matches("(0?[1-9]|[12][0-9]|3[01])/(0?[1-9]|1[012])/((?:19|20)[0-9][0-9])")
                    && endDate2.matches("(0?[1-9]|[12][0-9]|3[01])/(0?[1-9]|1[012])/((?:19|20)[0-9][0-9])")){

              SimpleDateFormat formData = new SimpleDateFormat("dd/MM/yyyy");

              //Convert the Date to UnixTimeStamp
              Date start = formData.parse(startDate2);
              long unixDate1 = start.getTime()/1000;

              //Convert the Date to UnixTimeStamp
              Date end = formData.parse(endDate2);
              long unixDate2 = end.getTime()/1000;

              if ( unixDate2<= unixDate1) {
                System.out.println("Enter appropriate interval. Please try again ... ");
              } else {
                System.out.println(user.listPortfolioSalesInRange(unixDate1, unixDate2));
              }

            } else {

              System.out.println("Please enter the correct Date format DD/MM/YYYY eg: 25/10/2021. Try again");

            }

            System.out.println("+-------------------------------------------------+");
            break;


          // Exit option.
          case 99:
            System.out.println("Thank you for using Portfolio System.");
            System.out.println("System exit.");

            // close scanner and exit system
            scanner.close();
            System.exit(1);

        }
      } else {
        System.out.println("Please choose option from the menu ... ");
        scanner.nextLine();
        System.out.println();
      }
    }
  }

  /**
   * Method to display console options.
   */
  private void selector() {
    System.out.println("Please enter your choice number and press enter: ");
    System.out.println(" 1  - Purchase an asset");
    System.out.println(" 2  - Sell an asset");
    System.out.println(" 3  - Get trending stock on specific region");
    System.out.println(" 4  - Get historical data on specified assets");
    System.out.println(" 5  - Get exchange summary in the specified region");
    System.out.println(" 6  - Get realtime quote on specific assets");
    System.out.println(" 7  - Get total portfolio live value");
    System.out.println(" 8  - List of all investments");
    System.out.println(" 9  - List specific portfolio type");
    System.out.println(" 10 - List specific assets in portfolio");
    System.out.println(" 11 - List purchased assets in specific interval");
    System.out.println(" 12 - List sold assets in specific interval");
    System.out.println(" 99 - Exit portfolio system");
    System.out.println();
  }

  /**
   * Helper method for asset purchase request.
   *
   * @param action is the amount of units for the attempted purchase.
   * @return the user input for amount of units returned after possible error handling.
   */
  private double requiredDouble(String action) {
    Scanner scanner = new Scanner(new UnclosableInputStreamDecorator(System.in));
    double result = 0;

    // User input error handling.
    while (true) {
      System.out.print(action);
      if (scanner.hasNextDouble()) {
        double doubleInput = scanner.nextDouble();
        if (doubleInput > 0) {
          result = doubleInput;
          scanner.close();
          break;
        } else {
          System.out.println("Please enter positive amount ... ");
          scanner.nextLine();
        }
      } else {
        System.out.println("Please enter numeric value.");
        scanner.nextLine();
      }
    }

    return result;
  }

  /**
   * Helper method for handling user request.
   *
   * @param action is the appropriate string value for the display.
   * @return the selected user request.
   */
  private List<String> requiredListTypeString(String action) {
    Scanner scanner = new Scanner(new UnclosableInputStreamDecorator(System.in));
    List<String> result = new ArrayList<>();

    // Storing user input
    while (true) {
      System.out.print(action);
      String stringInput = scanner.nextLine();
      if (stringInput.equals("99")) {
        break;
      }
      result.add(stringInput);

    }

    return result;
  }

  /**
   * Helper method for asset purchase request.
   *
   * @param action is the appropriate string value for the display.
   * @return the trimmed value of the user input.
   */
  private String requiredString(String action) {

    Scanner scanner = new Scanner(new UnclosableInputStreamDecorator(System.in));

    System.out.print(action);

    String stringInput = scanner.nextLine().trim();
    scanner.close();
    return stringInput;
  }

  /**
   * Helper method for requesting information of asset purchased in range.
   *
   * @param action is the appropriate string value for the display.
   * @return the user input for length of interval returned after possible error handling.
   */
  private long requiredLong(String action) {
    Scanner scanner = new Scanner(new UnclosableInputStreamDecorator(System.in));
    long result = 0;

    while (true) {
      System.out.print(action);
      if (scanner.hasNextLong()) {
        long longInput = scanner.nextLong();
        if (longInput > 0) {
          result = longInput;
          scanner.close();
          break;
        } else {
          System.out.println("Please enter positive amount ... ");
          scanner.nextLine();
        }
      } else {
        System.out.println("Please enter numeric value.");
        scanner.nextLine();
      }
    }

    return result;
  }
}



