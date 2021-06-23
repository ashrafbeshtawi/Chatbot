package de.dailab.oven.model.data_model;

import javax.annotation.Nullable;

import de.dailab.oven.model.database.ADatabaseNode;
import de.dailab.oven.model.database.NodeLabel;

public class Category extends ADatabaseNode{

	private static final NodeLabel CATEGORY_LABEL = NodeLabel.CATEGORY;
	
	public Category() {this(null);}
	
	public Category(@Nullable String name) {this(name, null);}
	
	public Category(@Nullable String name, @Nullable Long id) {
		super(name, id, CATEGORY_LABEL);
	}
}
