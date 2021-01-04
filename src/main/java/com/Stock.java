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
    public int stake; // Number of stocks we have currently

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
            System.out.println("Constructor:" + price);

        }
        catch (Exception e) {

            e.printStackTrace();

        }
    }

    public String toString () {

        return "Stock: " + ticker + " Stake: " + stake;

    }
    // MUTATOR METHODS

    public void addStock(int quantity) {

        stake = stake + quantity;

    }

}
