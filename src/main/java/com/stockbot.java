package com;

import net.jacobpeterson.alpaca.AlpacaAPI;

import java.util.ArrayList;

import static net.jacobpeterson.alpaca.enums.OrderSide.BUY;
import static net.jacobpeterson.alpaca.enums.OrderSide.SELL;
import static net.jacobpeterson.alpaca.enums.OrderTimeInForce.GTC;

public class stockbot {

    // Initializing the alpaca API
   static AlpacaAPI alpacaAPI = new AlpacaAPI();

    // VARIABLES
    public static ArrayList<Stock> portfolio = new ArrayList<>(); // ArrayList to hold our portfolio of stocks
    public float currentBal = (float) 100000.00; // Current balance

    public static void main(String [] args)
    {

        System.out.println("Main program");
        System.out.println(getCurrentPrice("WORK"));
        Stock newStock = new Stock("WORK");
        portfolio.add(newStock);
        System.out.println(findInPortfolio("WORK"));
        System.out.println(getOpeningPrice("WORK"));
        buyStock("WORK", 2);
        buyStock("AAPL", 2);
        buyStock("WORK", 1);
        System.out.println("porfolio size: " + portfolio.size());
        for (int i = 0; i < portfolio.size(); i++) {

            System.out.println(i + " : " + portfolio.get(i));

        }

    }

    // Check if a stock object is currently in the portfolio
    public static int findInPortfolio(String name) {

        int index = 0;

        for (int i = 0; i < portfolio.size(); i++) {

            if (name.equals(portfolio.get(i).ticker)) {

                index = i;

            }

        }

        return index;

    }


    // Buy a certain amount of stock and add it to the portfolio
    public static void buyStock (String name, int quantity) {

        boolean exception = false;

        // Use the alpaca API to send the order in
        try {
            alpacaAPI.requestNewMarketOrder(name, quantity, BUY, GTC);

        } catch (Exception e) {
            e.printStackTrace();
            exception = true;
        }

        if (!exception) {
            if ((findInPortfolio(name) != 0)) {

                for (int i = 0; i < portfolio.size(); i++) {

                    if (name.equals(portfolio.get(i).ticker)) {

                        portfolio.get(i).stake += quantity;

                    }

                }

            }
            else {

                Stock newStock = new Stock(name);
                newStock.stake = quantity;
                System.out.println("test: " + newStock.stake);
                portfolio.add(newStock);

            }
        }


    }

    // Sell Stock
    public static void sellStock(String name, int quantity)
    {
    if ((findInPortfolio(name)) != 0) {

        try {
            alpacaAPI.requestNewMarketOrder(name, quantity, SELL, GTC);
            portfolio.get(findInPortfolio(name)).stake =- quantity;
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    }

    // Get Current Price

    public static double getCurrentPrice(String name){

        double price = 0.0;
       try {

            price = alpacaAPI.getLastTrade(name).getLast().getPrice();

        }
        catch (Exception e) {
            e.printStackTrace();

        }
        return price;

    }

    // Get Opening Price

    public static double getOpeningPrice(String name)
    {

        double openingPrice = 0.0;
        try {
            //openingPrice = alpacaAPI.getOpenPositionBySymbol(name).getLast().getPrice();
        }
        catch (Exception e) {

            e.printStackTrace();

        }
        return openingPrice;

    }

}
