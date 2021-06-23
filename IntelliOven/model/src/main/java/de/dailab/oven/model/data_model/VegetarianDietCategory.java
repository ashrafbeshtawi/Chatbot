package de.dailab.oven.model.data_model;

import java.util.Optional;

import javax.annotation.Nonnull;

/**
 * Declares vegetarian diet types like vegetarian and vegan as categories.
 *
 * @author Hendrik Motza
 * @since 18.02
 */
public class VegetarianDietCategory implements ICategory {

    public static final VegetarianDietCategory VEGETERIAN = new VegetarianDietCategory("vegetarian");
    public static final VegetarianDietCategory VEGAN = new VegetarianDietCategory("vegan");

    @Nonnull
    private final String id;

    private VegetarianDietCategory(@Nonnull final String identifier) {
        this.id = identifier;
    }

    @Nonnull
    @Override
    public String getID() {
        return this.id;
    }

    public boolean isCompatibleTo(@Nonnull final VegetarianDietCategory category) {
        return this != VEGETERIAN && category != VEGAN;
    }

    public static Optional<VegetarianDietCategory> valueOf(@Nonnull final String value) {
        final String lowerValue = value.toLowerCase();
        if(VEGETERIAN.id.equals(lowerValue)) {
            return Optional.of(VEGETERIAN);
        }
        if(VEGAN.id.equals(lowerValue)) {
            return Optional.of(VEGAN);
        }
        return Optional.empty();
    }

    @Override
    public final boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null) {
            return false;
        }
        if(o.getClass() == String.class) {
            final String that = (String) o;
            return this.id.equals(that);
        }
        if(o instanceof ICategory) {
            final ICategory that = (ICategory) o;
            return this.id.equals(that.getID());
        }
        return false;
    }

    @Override
    public final int hashCode() {
        return this.id.hashCode();
    }
}
