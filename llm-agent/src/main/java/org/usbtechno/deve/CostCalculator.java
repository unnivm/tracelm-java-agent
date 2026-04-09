package org.usbtechno.deve;

public class CostCalculator {

    public static double calculate(String model, int promptTokens, int responseTokens) {

        double inputCost = 0;
        double outputCost = 0;

        switch (model) {

            case "gpt-4o-mini":
                inputCost  = 0.00015;
                outputCost = 0.0006;
                break;

            case "gpt-4o":
                inputCost = 0.005;
                outputCost = 0.015;
                break;

            default:
                inputCost = 0.001;
                outputCost = 0.002;
        }

        return (promptTokens / 1000.0) * inputCost +
                (responseTokens / 1000.0) * outputCost;
    }
}