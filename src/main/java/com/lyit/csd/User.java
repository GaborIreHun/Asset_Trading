package com.lyit.csd;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;
import java.util.Map.Entry;

/**
 * Current class represent a user that implements controls from PortfolioSystem class
 */
public class User implements PortfolioSystem {

  /**
   * The API key.
   */
  private String keyApi;

  /**
   * The available funds.
   */
  private double availableFunds;

  /**
   * The portfolio of the user.
   */
  private Portfolio userPortfolio;

  /**
   * Constructor to instantiate a User.
   *
   * @param keyApi the Api Key used to the user connect to Yahoo Finance API
   * and retrieve information.
   */
  public User(String keyApi) {
    this.keyApi = keyApi;
    availableFunds = 10_000;
    userPortfolio = new Portfolio();
  }


  /**
   * @inheritDoc
   */
  @Override
  public void addFunds(double amount) {
    if (amount > 0) {
      availableFunds += amount;
    }
  }

  /**
   * @inheritDoc
   */
  @Override
  public boolean withdrawFunds(double amount) {
    return availableFunds - amount >= 0;
  }


  /**
   * @inheritDoc
   */
  @Override
  public boolean purchaseAsset(String assetSymbol, double amount)
      throws IOException, InterruptedException {

    // if amount is negative or zero
    if (amount <= 0) {
      return false;
    }

    // make connection to get asset quote from yahoo finance api
    String requestString = "v6/finance/quote?region=US&lang=en&symbols=" + assetSymbol;
    ConnectionPort cp = new ConnectionPort(requestString, keyApi);

    // if requested symbol is not real asset symbol
    if (cp.getAssetQuote().isEmpty()) {
      return false;
    }

    // if we have enough funds to purchase asset
    double transactionCost = cp.getAssetQuote().get(0).getLivePrice() * amount;

    if (withdrawFunds(transactionCost)) {

      //create new asset instance
      Asset newAsset = new Asset(
          cp.getAssetQuote().get(0).getAssetSymbol(),
          cp.getAssetQuote().get(0).getAssetFullName(),
          cp.getAssetQuote().get(0).getAssetType(),
          cp.getAssetQuote().get(0).getTimeStamp(),
          cp.getAssetQuote().get(0).getLivePrice(),
          amount
      );

      //add it to the right portfolio type and pay for asset
      findPortfolioType(newAsset.getAssetType(), newAsset);
      availableFunds -= transactionCost;

      // update our portfolio class with needed information
      if (!userPortfolio.getAssetsInPortfolio().containsKey(assetSymbol)) {
        userPortfolio.getAssetsInPortfolio().put(newAsset.getAssetSymbol(),
            newAsset.getAssetType());
      }

      if (!userPortfolio.getAllAssetNames().contains(newAsset.getAssetFullName())) {
        userPortfolio.getAllAssetNames().add(newAsset.getAssetFullName());
      }
    }

    return true;
  }

  /**
   * @inheritDoc
   */
  private void findPortfolioType(String type, Asset asset) {

    //check asset type and add it in the right type portfolio list
    switch (type) {
      case "EQUITY" -> userPortfolio.getStock().add(asset);
      case "CRYPTOCURRENCY" -> userPortfolio.getCrypto().add(asset);
    }
  }

  /**
   * @inheritDoc
   */
  @Override
  public boolean sellAsset(String assetSymbol, double amount)
      throws IOException, InterruptedException {

    // check if asset symbol is in portfolio
    if(!userPortfolio.getAssetsInPortfolio().containsKey(assetSymbol))
      return false;

    if(amount <= 0)
      return false;

    // find amount of asset user holds in the portfolio
    double userHoldsAmount = 0;
    double toSell = amount;
    String assetFullName = "";

    //make list with all assets of this symbol
    List<Asset> requestedAssets = new ArrayList<>();
    for (Asset asset : userPortfolio.findPortfolioListType(assetSymbol)) {
      if(asset.getAssetSymbol().equals(assetSymbol)){
        userHoldsAmount += asset.getAmount();
        requestedAssets.add(asset);
      }
    }

    // if user wants to sell more than he have
    if(amount > userHoldsAmount)
      return false;

    // sort list by price (low to high)
    requestedAssets.sort(Comparator.comparing(Asset::getPriceBought));

    // as list is sorted, we can sell assets in right order
    double avgPurchasePrice = 0;
    int assetsCount = 0;
    boolean isStill = false;
    for (Asset asset : requestedAssets){

      avgPurchasePrice += asset.getPriceBought();
      assetsCount++;

      if(toSell - asset.getAmount() >= 0) {
        userPortfolio.findPortfolioListType(assetSymbol).remove(asset);
        toSell -= asset.getAmount();
      } else {
        asset.setAmount(asset.getAmount() - toSell);
        isStill = true;
      }
      if(assetFullName.isEmpty())
        assetFullName = asset.getAssetFullName();
    }

    // make connection to get live price
    String requestString = "v6/finance/quote?region=US&lang=en&symbols=" + assetSymbol;
    ConnectionPort cp = new ConnectionPort(requestString, keyApi);

    // to avoid limit exceeded if user has more than 100 requests per day
    if(cp.getAssetQuote().isEmpty())
      return false;

    // check transaction cost and add it to our funds
    double transactionCost = cp.getAssetQuote().get(0).getLivePrice() * amount;
    availableFunds += transactionCost;

    // add sold asset to the sold asset list
    userPortfolio.getSoldAssets().add(new SoldAsset(
        cp.getAssetQuote().get(0).getAssetSymbol(),
        cp.getAssetQuote().get(0).getAssetFullName(),
        cp.getAssetQuote().get(0).getAssetType(),
        cp.getAssetQuote().get(0).getTimeStamp(),
        avgPurchasePrice / assetsCount,
        cp.getAssetQuote().get(0).getLivePrice(),
        amount
    ));

    // if asset is fully sold we need to remove extra information we hold
    if(!isStill) {
      userPortfolio.getAssetsInPortfolio().remove(assetSymbol);
      userPortfolio.getSymbolFullName().remove(assetSymbol);
      userPortfolio.getAllAssetNames().remove(assetFullName);
    }

    return true;
  }

  /**
   * @inheritDoc
   */
  @Override
  public List<String> getTrendingStocks(String region) {
    return null;
  }

  /**
   * @inheritDoc
   */
  @Override
  public List<AssetQuote> getHistoricalData(List<String> assetSymbols, String interval, String range) {
    return null;
  }

  /**
   * @inheritDoc
   */
  @Override
  public String getExchangeSummary(String region, String exchange) {
    return null;
  }

  /**
   * @inheritDoc
   */
  @Override
  public List<AssetQuote> getAssetInformation(List<String> assetNames)
      throws IOException, InterruptedException {

    // build multiple symbol request string part
    StringBuilder symbolList = new StringBuilder();

    // build string with string builder
    for (String assetSymbol : assetNames) {
      symbolList.append(assetSymbol).append("%2C");
    }

    // result to return
    List<AssetQuote> assetInformation = new ArrayList<>();

    //return empty list if asset names is not in our portfolio
    if(symbolList.length() == 0)
      return assetInformation;

    // connect to api to get information
    String requestString = "v6/finance/quote?region=US&lang=en&symbols=" + symbolList.toString();
    ConnectionPort cp = new ConnectionPort(requestString, keyApi);

    // create and populate return list
    assetInformation = new ArrayList<>(cp.getAssetQuote());

    return assetInformation;

  }

  /**
   * @inheritDoc
   */
  @Override
  public double getPortfolioValue() throws IOException, InterruptedException {
    double livePrice = 0;
    double result ;

    List<String> userHoldSymbols = userPortfolio.getAllAssetNames();


    // build multiple symbol request string part
    StringBuilder symbolList = new StringBuilder();

    List<String> symbols = namesToSymbolList(userHoldSymbols);

    // build string with string builder
    for (String assetSymbol : symbols) {
      symbolList.append(assetSymbol).append("%2C");
    }

    // result to return
    List<AssetQuote> assetInformation = new ArrayList<>();


    // connect to api to get information
    String requestString = "v6/finance/quote?region=US&lang=en&symbols=" + symbolList.toString();
    ConnectionPort cp = new ConnectionPort(requestString, keyApi);

    // create and populate return list
    assetInformation = new ArrayList<>(cp.getAssetQuote());


    List<AssetQuote> quotes = assetInformation;

    for(AssetQuote quote : quotes) {
      livePrice += quote.getLivePrice();

    }
    NumberFormat formatter  = new DecimalFormat(".##");
    result = Double.parseDouble(formatter.format(livePrice));


    return result;

  }

  /**
   * @inheritDoc
   */
  @Override
  public String listAllInvestments() throws IOException, InterruptedException {


    // Creating String variables to hold the required information
    String stock = listPortfolioAssetsByType("stock");
    String crypto = listPortfolioAssetsByType("crypto");

    return stock + crypto;
  }


  /**
   * @inheritDoc
   */
  @Override
  public String listPortfolioAssetsByType(String assetType)
      throws IOException, InterruptedException {

    StringBuilder result = new StringBuilder();
    HashMap<String, List<Asset>> typeSortedPortfolio = new HashMap<>();

    if (assetType.toLowerCase().equals("stock")) {
      typeSortedPortfolio = getSortedStockPortfolio();
    }

    if (assetType.toLowerCase().equals("crypto")) {
      typeSortedPortfolio = getSortedCryptoPortfolio();
    }
    //System.out.println(typeSortedPortfolio);

    // get quote for each asset symbol related to asset type
    List<String> userHoldsSymbols = new ArrayList<>();
    for (Entry<String, List<Asset>> entry : typeSortedPortfolio.entrySet()) {
      String symbol = entry.getKey();
      if(!userHoldsSymbols.contains(symbol)) {
        userHoldsSymbols.add(symbol);
      }
    }
    List<AssetQuote> quotes = getAssetInformation(userHoldsSymbols);

    // loop through the quotes to get detailed information for each investment
    for (AssetQuote quote : quotes) {
      List<Asset> assetList = typeSortedPortfolio.get(quote.getAssetSymbol());
      result.append(assetDetailedInfo(assetList, quote.getLivePrice()));
    }

    return result.toString();
  }


  /**
   * @inheritDoc
   */
  @Override
  public String listPortfolioAssetsByName(List<String> assetNames)
          throws IOException, InterruptedException {

    StringBuilder result = new StringBuilder();
    HashMap<String, List<Asset>> chosenNamePortfolio = new HashMap<>();

    // list of all asset symbols to make request from.
    // we will use helper method to sort asset names and create sorted portfolio based on names.
    List<String> symbols = namesToSymbolList(assetNames);
    chosenNamePortfolio = getAllSortedPortfolio(symbols);

    // get quote on user held symbols
    List<AssetQuote> quotes = getAssetInformation(symbols);

    // loop through quotes and print detailed information
    for (AssetQuote quote : quotes) {
      List<Asset> assetList = chosenNamePortfolio.get(quote.getAssetSymbol());
      result.append(assetDetailedInfo(assetList, quote.getLivePrice()));
    }

    return result.toString();
  }

  /**
   * @inheritDoc
   */
  @Override
  public String listPortfolioPurchasesInRange(long startTimeStamp, long endTimeStamp)
      throws IOException, InterruptedException {

    StringBuilder result = new StringBuilder();

    // if start date is in front of the end date
    if(startTimeStamp > endTimeStamp)
      return result.toString();

    HashMap<String, List<Asset>> sortedAllPortfolio = getAllSortedPortfolio();

    // sort map sortedAllPortfolio by purchase timestamp
    for (String symbol : sortedAllPortfolio.keySet()) {
      List<Asset> toSort = sortedAllPortfolio.get(symbol);

      // remove assets not in range
      toSort.removeIf(
          asset -> asset.getTimeStamp() < startTimeStamp || asset.getTimeStamp() > endTimeStamp);

      // sort based on timestamp (low to high)
      toSort.sort(Comparator.comparing(Asset::getTimeStamp));
    }

    // get live information for each asset symbol
    List<AssetQuote> quotes = getAssetInformationAlternate(userPortfolio.getAllAssetNames());

    // loop through quotes and print detailed information
    for (AssetQuote quote : quotes) {
      List<Asset> assetList = sortedAllPortfolio.get(quote.getAssetSymbol());
      result.append(assetPurchaseRangeInfo(assetList, quote.getLivePrice()));
    }

    return result.toString();
  }

  /**
   * @inheritDoc
   */
  @Override
  public String listPortfolioSalesInRange(long startTimeStamp, long endTimeStamp) {

    StringBuilder result = new StringBuilder();

    // if start date is in front of the end date
    if(startTimeStamp > endTimeStamp)
      return result.toString();

    HashMap<String, List<SoldAsset>> sortedSales = new HashMap<>();

    // populate sortedSales with related sales for each asset symbol
    for (SoldAsset soldAsset : userPortfolio.getSoldAssets()) {
      String symbol = soldAsset.getAssetSymbol();
      if(!sortedSales.containsKey(symbol)) {
        List<SoldAsset> list = new ArrayList<>();
        list.add(soldAsset);
        sortedSales.put(symbol, list);
      } else {
        sortedSales.get(symbol).add(soldAsset);
      }
    }

    // loop through map and build result string
    for (String symbol : sortedSales.keySet()) {

      // sort based on timestamp (low to high)
      List<SoldAsset> list = sortedSales.get(symbol);
      list.sort(Comparator.comparing(SoldAsset::getTimeStamp));

      // loop through sorted list and add to result string in range sold assets
      for (SoldAsset soldAsset : list) {

        double avgPurchasePrice = soldAsset.getAvgPurchasePrice();
        double soldPrice = soldAsset.getPriceSold();
        double differenceUSD = soldPrice - avgPurchasePrice;
        differenceUSD = Double.parseDouble(
            new DecimalFormat(".##").format(differenceUSD));
        int differencePercentage = (int) (100 * (soldPrice - avgPurchasePrice) / avgPurchasePrice);

        if(soldAsset.getTimeStamp() >= startTimeStamp && soldAsset.getTimeStamp() <= endTimeStamp) {
          result.append("\nAsset Name         : ").append(soldAsset.getAssetFullName());
          result.append("\nAvg Purchase Price : ").append(soldAsset.getAvgPurchasePrice());
          result.append("\nSale price         : ").append(soldAsset.getPriceSold());
          result.append("\nDifference USD     : ").append(differenceUSD);
          result.append("\nDifference %       : ").append(differencePercentage);
          result.append("\n");
        }
      }
    }

    return result.toString();
  }

  /**
   * Returning the available funds.
   *
   * @return the available funds of the user.
   */
  public double getAvailableFunds() {
    return availableFunds;
  }


  /**
   * Returning the user's portfolio.
   *
   * @return the portfolio of the user.
   */
  public Portfolio getUserPortfolio() {
    return userPortfolio;
  }


  // helper private methods


  /**
   * Helper method to check if user portfolio contains symbol or partial asset name (example: Appl).
   * Will check first 3 letters of name to avoid similar asset names.
   *
   * @param names list of asset partial names or full symbols
   * @return list with symbols which user has from names list.
   */
  private List<String> namesToSymbolList(List<String> names) {

    List<String> result = new ArrayList<>();
    // lower cased names list
    List<String> lowerCasedNames = new ArrayList<>();

    // change to lower case to be sure it matches user input
    for (String name : names) {
      lowerCasedNames.add(name.toLowerCase());
    }

    // check if names contains symbol
    for (Entry<String, String> entry : userPortfolio.getAssetsInPortfolio().entrySet()) {
      String symbol = entry.getKey();
      String toCompare = symbol.toLowerCase();
      if(lowerCasedNames.contains(toCompare)){
        result.add(symbol);
        lowerCasedNames.remove(toCompare);
      }
    }

    // if it is name, we need to cut to 3 characters
    for (int i = 0; i < lowerCasedNames.size(); i++) {
      String tempName = lowerCasedNames.get(i).substring(0, 3);
      lowerCasedNames.remove(i);
      lowerCasedNames.add(i, tempName);
    }

    // check if name contains partial name
    for (Entry<String, String> entry : userPortfolio.getSymbolFullName().entrySet()) {
      String key = entry.getKey();
      String compareTo = key.substring(0, 3).toLowerCase();
      String value = entry.getValue();
      if(lowerCasedNames.contains(compareTo)){
        result.add(value);
      }
    }

    return result;
  }

  /**
   * Helper method to get sorted stock assets in our portfolio by asset symbol
   *
   * @return sorted portfolio hash map
   */
  private HashMap<String, List<Asset>> getSortedStockPortfolio() {

    HashMap<String, List<Asset>> result = new HashMap<>();

    // populate result hash map with stock assets
    for(Asset asset : userPortfolio.getStock()) {
      if(!result.containsKey(asset.getAssetSymbol())) {
        List<Asset> newList = new ArrayList<>();
        newList.add(asset);
        result.put(asset.getAssetSymbol(), newList);
      } else {
        result.get(asset.getAssetSymbol()).add(asset);
      }
    }

    return result;
  }

  /**
   * Helper method to get sorted crypto assets in our portfolio by asset symbol
   *
   * @return sorted portfolio hash map
   */
  private HashMap<String, List<Asset>> getSortedCryptoPortfolio() {

    HashMap<String, List<Asset>> result = new HashMap<>();

    // populate result hash map with crypto assets
    for(Asset asset : userPortfolio.getCrypto()) {
      if(!result.containsKey(asset.getAssetSymbol())) {
        List<Asset> newList = new ArrayList<>();
        newList.add(asset);
        result.put(asset.getAssetSymbol(), newList);
      } else {
        result.get(asset.getAssetSymbol()).add(asset);
      }
    }

    return result;
  }

  /**
   * Helper method to get all sorted crypto and stock portfolio by asset symbol
   *
   * @return sorted portfolio hash map
   */
  private HashMap<String, List<Asset>> getAllSortedPortfolio() {

    HashMap<String, List<Asset>> result = new HashMap<>();

    // merge two hash (stock and crypto) together
    result = getSortedStockPortfolio();
    result.putAll(getSortedCryptoPortfolio());

    return result;
  }

  /**
   * Helper method to get sorted portfolio based on asset names list.
   *
   * @return sorted portfolio hash map
   */
  private HashMap<String, List<Asset>> getAllSortedPortfolio(List<String> names){


  HashMap<String, List<Asset>> result = new HashMap<>();
  // populate result hash map with selected stock assets
  for(Asset asset : userPortfolio.getStock()) {
    if (!result.containsKey(asset.getAssetSymbol()) && names.contains(asset.getAssetSymbol())) {
      List<Asset> newList = new ArrayList<>();
      newList.add(asset);
      result.put(asset.getAssetSymbol(), newList);
      continue;
    }
    if (result.containsKey(asset.getAssetSymbol())) {
      result.get(asset.getAssetSymbol()).add(asset);
    }
  }

    // populate result hash map with selected crypto assets
    for(Asset asset : userPortfolio.getCrypto()) {
      if (!result.containsKey(asset.getAssetSymbol()) && names.contains(asset.getAssetSymbol())) {
        List<Asset> newList = new ArrayList<>();
        newList.add(asset);
        result.put(asset.getAssetSymbol(), newList);
        continue;
      }

      if (result.containsKey(asset.getAssetSymbol())) {
        result.get(asset.getAssetSymbol()).add(asset);
      }
    }

    return result;
  }


  /**
   * Helper method to get detailed information String on all assets of one symbol.
   *
   * @param assets grouped list of same symbol assets
   * @param livePrice live price for this asset symbol
   * @return detailed information about asset.
   */
  private String assetDetailedInfo(List<Asset> assets, double livePrice) {

    String name = "";
    String symbol = "";
    double avgPurchasePrice = 0;
    double amount = 0;
    double differenceUSD = 0;
    int differencePercentage = 0;

    for (Asset asset : assets) {

      if (name.isEmpty()) {
        name = asset.getAssetFullName();
      }
      if (symbol.isEmpty()) {
        symbol = asset.getAssetSymbol();
      }

      avgPurchasePrice += asset.getPriceBought();
      amount += asset.getAmount();
    }

    avgPurchasePrice /= assets.size();
    differenceUSD = livePrice - avgPurchasePrice;
    livePrice = Double.parseDouble(
        new DecimalFormat(".##").format(livePrice));
    differenceUSD = Double.parseDouble(
        new DecimalFormat(".##").format(differenceUSD));
    differencePercentage = (int) (100 * (livePrice - avgPurchasePrice) / avgPurchasePrice);


    return "\nAsset Name     : " + name +
        "\nAsset Symbol   : " + symbol +
        "\nAsset Amount   : " + amount +
        "\nAverage Price  : " + avgPurchasePrice + " USD" +
        "\nLive Price     : " + livePrice + " USD" +
        "\nDifference USD : " + differenceUSD + " USD" +
        "\nDifference %   : " + differencePercentage + "%\n";

  }

  /**
   * Helper method to get detailed information on the assets' profitability.
   *
   * @param assets grouped list of assets in portfolio.
   * @param livePrice the actual price of the relevant asset.
   * @return the detailed information of profitability.
   */
  private String assetPurchaseRangeInfo(List<Asset> assets, double livePrice) {

    StringBuilder result = new StringBuilder();

    for (Asset asset : assets) {

      double purchasePrice = asset.getPriceBought();
      double differenceUSD = livePrice - purchasePrice;
      livePrice = Double.parseDouble(
              new DecimalFormat(".##").format(livePrice));
      differenceUSD = Double.parseDouble(
              new DecimalFormat(".##").format(differenceUSD));
      int differencePercentage = (int) (100 * (livePrice - purchasePrice) / purchasePrice);

      result.append("\nAsset Name     : ").append(asset.getAssetFullName());
      result.append("\nPrice Bought   : ").append(asset.getPriceBought());
      result.append("\nLive Price     : ").append(livePrice);
      result.append("\nDifference USD : ").append(differenceUSD);
      result.append("\nDifference %   : ").append(differencePercentage);
      result.append("\n");
    }

    return result.toString();
  }

  /**
   * Helper method to get information regarding the number/unit of each asset the user has.
   *
   * @return the asset name and its units the user has.
   */
  public String getShortStatus() {

    StringBuilder result = new StringBuilder();
    HashMap<String, List<Asset>> allAssets = getAllSortedPortfolio();
    for(Entry<String, List<Asset>> entry : allAssets.entrySet()) {
      String symbol = entry.getKey();
      List<Asset> list = entry.getValue();
      double amount = 0;
      for (Asset asset : list) {
        amount += asset.getAmount();
      }

      result.append("{").append(symbol.toUpperCase()).append(" x ").append(amount).append("} ");
    }
    return result.toString();
  }

  /**
   * Retrieve realtime quote data for the assets within the list assetNames from the online
   * exchange, but only assets that are on the Portfolio.
   *
   * @throws IOException if underlying service fails.
   * @throws InterruptedException if underlying service fails.
   * @param assetNames a list of asset symbols for example, "Bitcoin-USD", "Appl", "TSLA"
   * @return A list of AssetQuote objects. Return an empty list if we have no assets in our
   * portfolio.
   */
  public List<AssetQuote> getAssetInformationAlternate(List<String> assetNames)
          throws IOException, InterruptedException {

    // build multiple symbol request string part
    StringBuilder symbolList = new StringBuilder();

    // list of all asset symbols to make request from.
    // we will use helper method to sort asset names
    List<String> symbols = namesToSymbolList(assetNames);

    // build string with string builder
    for (String assetSymbol : symbols) {
      symbolList.append(assetSymbol).append("%2C");
    }

    // result to return
    List<AssetQuote> assetInformation = new ArrayList<>();

    //return empty list if asset names is not in our portfolio
    if(symbolList.length() == 0)
      return assetInformation;

    // connect to api to get information
    String requestString = "v6/finance/quote?region=US&lang=en&symbols=" + symbolList.toString();
    ConnectionPort cp = new ConnectionPort(requestString, keyApi);

    // create and populate return list
    assetInformation = new ArrayList<>(cp.getAssetQuote());

    return assetInformation;

  }
}
