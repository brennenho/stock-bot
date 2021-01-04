package com;

import net.jacobpeterson.alpaca.*;
import net.jacobpeterson.domain.alpaca.asset.Asset;
import net.jacobpeterson.polygon.*;

public class Stock {

    // Initializing the alpaca API
    AlpacaAPI alpacaAPI = new AlpacaAPI();

    // VARIABLES
    public String ticker; // Stock name
    public double price; // Current price
    public double open; // Open of the day
    public Asset stake; // Number of stocks we have currently

    // CONSTRUCTORS

    // Default Constructor
    public Stock ()
    {

        ticker = "";
        price = 0.0;
        open = 0.0;

    }

    // Normal Constructor
    public Stock (String name)
    {

        ticker = name;
        try {

            price = alpacaAPI.getLastTrade(ticker).getLast().getPrice();

        }
        catch (Exception e){

            System.out.println("An exception was thrown");

        }

        try {
            stake = alpacaAPI.getAssetBySymbol(ticker);
        }
        catch (Exception e) {

            System.out.println("An exception was thrown");

        }

    }

    // MUTATOR METHODS

    public void addStock(int quantity) {

        System.out.println("addStock method");

    }

}
