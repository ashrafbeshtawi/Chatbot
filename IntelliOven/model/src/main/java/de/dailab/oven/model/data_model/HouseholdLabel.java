package de.dailab.oven.model.data_model;

import javax.annotation.Nullable;

public enum HouseholdLabel {

	HOUSEHOLD,
	GUEST;
	
	@Nullable
	public HouseholdLabel getLabel(@Nullable String str) {

		for(HouseholdLabel label : HouseholdLabel.values()) {
			if (label.toString().equalsIgnoreCase(str)) {
				return label;
			}
		}
		
		return null;
	}
}
