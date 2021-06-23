package de.dailab.oven.model.data_model;

import java.util.Objects;

import javax.annotation.Nonnull;

/**
 * TODO add missing JavaDoc
 *
 * @author Hendrik Motza
 * @since 20.02
 */
public class CustomCategory implements ICategory {

    @Nonnull
    private final String id;

    public CustomCategory(@Nonnull final String categoryId) {
        this.id = Objects.requireNonNull(categoryId, "Parameter 'categoryId' must not be null");
    }

    @Nonnull
    @Override
    public String getID() {
        return this.id;
    }

    @Override
    public final boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || !o.getClass().equals(ICategory.class)) {
            return false;
        }
        final ICategory that = (ICategory) o;
        return this.id.equals(that.getID());
    }

    @Override
    public final int hashCode() {
        return this.id.hashCode();
    }
}
