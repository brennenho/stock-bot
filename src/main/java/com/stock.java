package com;

import net.jacobpeterson.alpaca.*;

public class stock {

    // Initializing the alpaca API
    AlpacaAPI alpacaAPI = new AlpacaAPI();

    // VARIABLES
    public String ticker; // Stock name
    public float price; // Current price
    public float close; // Previous day's close
    public float open; // Open of the day
    public int stake; // Number of stocks we have currently

    // CONSTRUCTORS

    // Default Constructor
    public Stock ()
    {

        ticker = "";
        price = 0.0;
        close = 0.0;
        open = 0.0;
        stake = 0;

    }

    // Normal Constructor
    public Stock (String name)
    {

        ticker = name;
        price = polygonAPI.getLastTrade(ticker);
        close = polygonAPI.getPreviousClose(ticker,false); // Adjusting for splits
        open = alpacaAPI.getOpenPositionBySymbol(ticker);
        stake = alpacaAPI.getAssetBySymbol(ticker);

    }

    // MUTATOR METHODS

    public void addStock(int quantity) {

        System.out.println("addStock method");
        stake = stake + quantity;

    }

}
