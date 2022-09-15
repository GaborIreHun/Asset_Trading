package com.lyit.csd;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Current class represent an Asset quote.
 */
public class AssetQuote {

   /**
   * The symbol of the asset e.g. APPL, TSLA, BARC or BTC-USD
   */
  @JsonProperty("symbol")
  private String assetSymbol;

  /**
   * The full name of the asset e.g. Apple, Tesla, Barclays PLC, Bitcoin USD
   */
  @JsonProperty("shortName")
  private String assetFullName;

  /**
   * The type of the asset. e.g. Crypto
   */
  @JsonProperty("quoteType")
  private String assetType;

  /**
   * The UNIX timestamp of the asset's quoted value. Using long instead of int to avoid the year
   * 2038 problem.
   */
  @JsonProperty("regularMarketTime")
  private long timeStamp;

  /**
   * The value in USD of the named asset at this point in time.
   */
  @JsonProperty("regularMarketPrice")
  private double livePrice;


  /**
   * Constructor to instantiate AssetQuote object.
   *
   * @param assetSymbol symbol of the asset.
   * @param assetFullName full name of the asset.
   * @param assetType type of the asset.
   * @param timeStamp UNIX timestamp
   * @param livePrice current price in USD on the market
   */
  public AssetQuote(String assetSymbol, String assetFullName, String assetType, long timeStamp,
                    double livePrice) {
    this.assetSymbol = assetSymbol;
    this.assetFullName = assetFullName;
    this.assetType = assetType;
    this.timeStamp = timeStamp;
    this.livePrice = livePrice;
  }

  /**
   * Default constructor.
   */
  public AssetQuote() {
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
  public String getAssetType() { return assetType; }


  /**
   * Returning the time stamp.
   *
   * @return the timestamp of the relevant asset's initial quoted value.
   */
  public long getTimeStamp() { return timeStamp; }


  /**
   * Returning the live price.
   *
   * @return the live/up-to-date price of the asset.
   */
  public double getLivePrice() { return livePrice; }

}
