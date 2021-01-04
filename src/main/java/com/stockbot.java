package com;

import net.jacobpeterson.alpaca.AlpacaAPI;
import net.jacobpeterson.alpaca.rest.exception.AlpacaAPIRequestException;

import java.util.ArrayList;

public class stockbot {

    // Initializing the alpaca API
   static AlpacaAPI alpacaAPI = new AlpacaAPI();

    // VARIABLES
    public static ArrayList<Stock> portfolio = new ArrayList<>(); // ArrayList to hold our portfolio of stocks
    public float currentBal = (float) 100000.00; // Current balance

    public static void main(String [] args)
    {

        System.out.println("Main program");
        System.out.println(getCurrentPrice("APPL"));

    }


    // Check if a stock object is currently in the portfolio
    /*public static boolean findInPortfolio(String name) {


        // Integrates
        for (int i = 0; i < portfolio.size(); i++) {

            if (name.equals(portfolio.get(i))) {

                return true;

            }

        }

        return false;

    }*/


    // Buy a certain amount of stock and add it to the portfolio
    /*public static void buyStock (String name, int quantity) {

        // Create a new stock object
        Stock newStock = new Stock(name); // Create a new stock object

        if (findInPortfolio(name)) {

            for (int i = 0; i < portfolio.size(); i++) {

                if (name.equals(portfolio.get(i))) {

                    // portfolio.get(i).stake++; // Increase stock stake by one
                    System.out.println("buyStock");

                }

            }

        }

        OrderSide side = side.BUY;
        OrderTimeInForce timeInForce = GTC;


        // Use the alpaca API to send the order in
        alpacaAPI.requestNewMarketOrder(name, quantity, side, timeInForce);


    }*/


    // Get Current Price

    public static double getCurrentPrice(String name){

        double price = 0.0;
        System.out.println("1");
       try {

            price = alpacaAPI.getLastTrade(name).getLast().getPrice();
            System.out.println("2");

        }
        catch (Exception e) {
            System.out.println("3");
            System.out.println("getCurrentPrice threw an exception");

        }
        return price;

    }

    // Get Opening Price

    /*public double getOpeningPrice(String name)
    {

        //return alpacaAPI.getOpenPositionBySymbol(name).getLast().getPrice();

    }*/

    // Sell Stock

    /*public static void sellStock(String name, int quantity)
    {


    }*/

}
