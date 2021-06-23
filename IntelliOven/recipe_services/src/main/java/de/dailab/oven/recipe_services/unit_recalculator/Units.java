package de.dailab.oven.recipe_services.unit_recalculator;

public class Units {
	
	double amount;
    String[] name;
    String category;
    
    public Units(){}
    
    public Units(final double amount, final String[] name, final String category)
	{
		this.amount = amount;
		this.name = name;
		this.category = category;
	}
    
    public String[] getName() {return this.name;}
    
    public double getAmount() {return this.amount;}
    
    public String getCategory() {return this.category;}
	
}