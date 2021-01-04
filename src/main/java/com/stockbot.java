package com;

// IMPORTS
import net.jacobpeterson.alpaca.AlpacaAPI;

import java.util.ArrayList;

import static net.jacobpeterson.alpaca.enums.OrderSide.BUY;
import static net.jacobpeterson.alpaca.enums.OrderSide.SELL;
import static net.jacobpeterson.alpaca.enums.OrderTimeInForce.GTC;

// STOCK BOT CLASS
public class stockbot {

    // Creating a new STATIC instance of the alpacaAPI
   static AlpacaAPI alpacaAPI = new AlpacaAPI();

    // VARIABLES
    public static ArrayList<Stock> portfolio = new ArrayList<>(); // ArrayList to hold our portfolio of stocks
    public float currentBal = (float) 100000.00; // Current balance

    // MAIN PROGRAM
    public static void main(String [] args)
    {

        System.out.println("Main program");
        System.out.println(getCurrentPrice("WORK"));
        Stock newStock = new Stock("WORK");
        portfolio.add(newStock);
        System.out.println(findInPortfolio("WORK"));
        buyStock("WORK", 2);
        buyStock("AAPL", 2);
        buyStock("WORK", 1);
        System.out.println("porfolio size: " + portfolio.size());
        for (int i = 0; i < portfolio.size(); i++) {

            System.out.println(i + " : " + portfolio.get(i));

        }

    }

    // METHODS

    // Static method to determine if a stock is already listed in our portfolio
    // Returns the index of the stock if it is in the portfolio or -1 if it is not
    public static int findInPortfolio(String name) {

        int index = -1;

        for (int i = 0; i < portfolio.size(); i++) {

            if (name.equals(portfolio.get(i).ticker)) {

                index = i;

            }

        }

        return index;

    }


    // Static method that uses the alpacaAPI to place an order
    // Method correctly adds the stock to our portfolio
    public static void buyStock (String name, int quantity) {

        // Indicator variable to check if an exception was thrown while accessing the API
        boolean exception = false;

        try {
            // Attempts to place an order by using the alpacaAPI
            alpacaAPI.requestNewMarketOrder(name, quantity, BUY, GTC);
        } catch (Exception e) {
            // Code to run if an exception was thrown
            e.printStackTrace();
            exception = true;
        }

        // Checks if an exception was thrown
        if (!exception) {
            // Checks if the stock is already present in portfolio
            if ((findInPortfolio(name) != -1)) {

                // Loops through each value of portfolio
                for (Stock stock : portfolio) {

                    // Checks if the names are the same
                    if (name.equals(stock.ticker)) {

                        stock.stake += quantity;

                    }

                }

            }
            // If the stock is not already present, it is appended onto the end of portfolio
            else {

                // Creates a new Stock object
                Stock newStock = new Stock(name);
                newStock.stake = quantity;
                System.out.println("test: " + newStock.stake);
                portfolio.add(newStock);

            }
        }


    }

    // Static method to sell stock
    public static void sellStock(String name, int quantity) {

        // Indicator variable to check if an exception was thrown while accessing the API
        boolean exception = false;

        // Checks if the stock is in the portfolio
        if ((findInPortfolio(name)) != -1) {
            if (portfolio.get(findInPortfolio(name)).stake <= quantity) {

                try {
                    // Attempts to sell a stock using the alpacaAPI
                    alpacaAPI.requestNewMarketOrder(name, quantity, SELL, GTC);

                } catch (Exception e) {
                    // Code to run if an exception is thrown
                    e.printStackTrace();
                    exception = true;
                }

                if (!exception) {

                    // Decreases stake of the portfolio by quantity
                    portfolio.get(findInPortfolio(name)).stake = -quantity;

                }
            }
            else {

                // Print an error message
                System.out.println("Error: You do not have that quantity of stock to sell.");

            }


        }
        else {

            // Print an error message
            System.out.println("Error: " + name + " is not present in the portfolio!");

        }

    }

    // Static method to get the current price of a stock
    public static double getCurrentPrice(String name){

        // Initializing price variable that will be returned at the end
        double price = 0.0;

        // Attempt to access the alpacaAPI and get stock price
        try {

            price = alpacaAPI.getLastTrade(name).getLast().getPrice();

        }
        catch (Exception e) {
            e.printStackTrace();

        }
        return price;

    }

}
