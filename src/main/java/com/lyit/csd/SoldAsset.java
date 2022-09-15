package com.lyit.csd;

/**
 * SoldAsset class represent transaction with necessary information for the user when he
 * sells and asset.
 */
public class SoldAsset {

  /**
   * The symbol of the asset (stock symbol or cryptocurrency).
   */
  private String assetSymbol;

  /**
   * The full name (long name) of the asset.
   */
  private String assetFullName;

  /**
   * The type of the asset (equity or cryptocurrency).
   */
  private String assetType;

  /**
   * UNIX timestamp representing a date.
   */
  private long timeStamp;

  /**
   * The average purchase price of the asset.
   */
  private double avgPurchasePrice;

  /**
   * The value the asset is sold for.
   */
  private double priceSold;

  /**
   * The number/unit of the asset (held or bought).
   */
  private double amount;


  /**
   * SoldAsset constructor
   *
   * @param assetSymbol symbol of asset sold
   * @param assetFullName full name of asset sold
   * @param assetType type of the asset sold
   * @param timeStamp timestamp for the transaction
   * @param avgPurchasePrice average purchase price for assets
   * @param priceSold price for transaction
   * @param amount amount of assets being sold
   */
  public SoldAsset(String assetSymbol, String assetFullName, String assetType, long timeStamp,
      double avgPurchasePrice, double priceSold, double amount) {
    this.assetSymbol = assetSymbol;
    this.assetFullName = assetFullName;
    this.assetType = assetType;
    this.timeStamp = timeStamp;
    this.avgPurchasePrice = avgPurchasePrice;
    this.priceSold = priceSold;
    this.amount = amount;
  }


  /**
   * Returning asset symbol.
   *
   * @return the relevant asset's symbol.
   */
  public String getAssetSymbol() {
    return assetSymbol;
  }


  /**
   * Returning asset full name.
   *
   * @return the full name of the relevant asset.
   */
  public String getAssetFullName() {
    return assetFullName;
  }


  /**
   * Returning asset type.
   *
   * @return the type of the relevant asset.
   */
  public String getAssetType() {
    return assetType;
  }


  /**
   * Returning the time stamp.
   *
   * @return the timestamp of the relevant asset's initial quoted value.
   */
  public long getTimeStamp() {
    return timeStamp;
  }


  /**
   * Returning the average price.
   *
   * @return the average asking price of the asset.
   */
  public double getAvgPurchasePrice() {
    return avgPurchasePrice;
  }


  /**
   * Returning the sold price.
   *
   * @return the amount the asset was sold for.
   */
  public double getPriceSold() {
    return priceSold;
  }


  /**
   * Returning the amount.
   *
   * @return the amount of the relevant asset.
   */
  public double getAmount() {
    return amount;
  }
}
