package de.dailab.oven.api_common.recipe;

import de.dailab.oven.api_common.Sendable;
import de.dailab.oven.api_common.view.Viewable;
import de.dailab.oven.model.data_model.Recipe;

import java.lang.reflect.Field;
import java.util.List;

public class RecipeResponse implements Sendable, Viewable {

    // these enum is used relevant for GUI!
    private enum RecipeDetail {

        OVERVIEW(0, "name"),
        INGREDIENTS(1, "ingredients"),
        INSTRUCTIONS(2, "instructions");

        private final int index;
        private final String jsonKey;

        /**
         * @param index   index 0 - x, used to go through
         * @param jsonKey the JSON key
         */
        RecipeDetail(final int index, final String jsonKey) {
            this.index = index;
            this.jsonKey = jsonKey;
        }

        public int getIndex() {
            return this.index;
        }

        public String getJsonKey() {
            return this.jsonKey;
        }

        public static RecipeDetail getByIndex(final int index) {
            for (final RecipeDetail e : RecipeDetail.values()) {
                if (index == e.index) return e;
            }
            return null;
        }
    }

    private List<Recipe> recipeList;
    //the current mark  of Object (e.g. the highlighting of on Dish in a list)
    private Selection selection;

    public RecipeResponse(final List<Recipe> recipeList) {
        this.recipeList = recipeList;
        //on first creation, the view is in a list...
        if (!recipeList.isEmpty())
            this.selection = new Selection(recipeList.get(0).getId(), 0);
        else
            //default value for empty Traversing point
            this.selection = new Selection(-1, -1);
    }

    private RecipeResponse() {
    }

    public List<Recipe> getRecipeList() {
        return this.recipeList;
    }

    public Selection getSelection() {
        return this.selection;
    }

    /**
     * Navigae up (Language or other input method) e.g. in a list or in a Recipe
     *
     * @return true: commend was accepted and processed; false: command is not allowed/was not able to processed.
     */
    @Override
    public boolean up() {

        switch (this.selection.getViewType()) {
            case LISTVIEW:

                //less than 2 Recipes makes no sense to select
                if (this.recipeList.size() < 2) {
                    return false;
                }

                //if first element: go to bottom
                if (this.selection.getRecipeIndex() == 0) {
                    this.selection = new Selection(this.recipeList.get(this.recipeList.size() - 1).getId(), this.recipeList.size() - 1);
                    return true;
                }

                //normal element
                final int currentIndex = this.selection.getRecipeIndex();
                this.selection = new Selection(this.recipeList.get(currentIndex - 1).getId(), currentIndex - 1);
                return true;

            case DETAILVIEW:

                //if first element: go to bottom
                if (this.selection.getRecipeDetail().getIndex() == 0) {
                    //get new element
					this.selection = new Selection(this.selection, RecipeDetail.getByIndex(RecipeDetail.values().length - 1));
                    return true;
                }

                //normal element
                final int newRecipeDetailIndex = this.selection.getRecipeDetail().getIndex() - 1;
                this.selection = new Selection(this.selection, RecipeDetail.getByIndex(newRecipeDetailIndex));
                return true;

            case DETAILLISTVIEW:

                try {
                    //get the List
                    final List detailList = getDetailList(this.recipeList, this.selection);

                    //if first last: go to Top
                    if (this.selection.getRecipeDetailListIndex() == 0) {
                        this.selection = new Selection(this.selection, detailList.size() - 1);
                        return true;
                    }

                    //normal element
                    final int detailListIndex = this.selection.getRecipeDetailListIndex();
                    this.selection = new Selection(this.selection, detailListIndex - 1);
                    return true;


                } catch (final Exception e) {
                    return false;
                }

            default:
                return false;
        }

    }

    /**
     * Navigae down (Language or other input method) e.g. in a list
     *
     * @return true: commend was accepted and processed; false: command is not allowed/was not able to processed.
     */
    @Override
    public boolean down() {
        switch (this.selection.getViewType()) {
            case LISTVIEW:

                //less than 2 Recipes makes no sense to select
                if (this.recipeList.size() < 2) {
                    return false;
                }

                //if last element: go to Top
                if (this.selection.getRecipeIndex() == this.recipeList.size() - 1) {
                    this.selection = new Selection(this.recipeList.get(0).getId(), 0);
                    return true;
                }

                //normal element
                final int currentIndex = this.selection.getRecipeIndex();
                this.selection = new Selection(this.recipeList.get(currentIndex + 1).getId(), currentIndex + 1);
                return true;

            case DETAILVIEW:

                //if last element: go to Top
                if (this.selection.getRecipeDetail().getIndex() == RecipeDetail.values().length - 1) {
					this.selection = new Selection(this.selection, RecipeDetail.getByIndex(0));
                    return true;
                }

                //normal element
                final int newRecipeDetailIndex = this.selection.getRecipeDetail().getIndex() + 1;
                this.selection = new Selection(this.selection, RecipeDetail.getByIndex(newRecipeDetailIndex));
                return true;

            case DETAILLISTVIEW:

                try {
                    //get the List
                    final List detailList = getDetailList(this.recipeList, this.selection);

                    //if first last: go to Top
                    if (this.selection.getRecipeDetailListIndex() == detailList.size() - 1) {
                        this.selection = new Selection(this.selection, 0);
                        return true;
                    }

                    //normal element
                    final int detailListIndex = this.selection.getRecipeDetailListIndex();
                    this.selection = new Selection(this.selection, detailListIndex + 1);
                    return true;


                } catch (final Exception e) {
                    return false;
                }

            default:
                return false;
        }
    }

    /**
     * Navigae left (Language or other input method), go back from Recipe view to List view
     *
     * @return true: commend was accepted and processed; false: command is not allowed/was not able to processed.
     */
    @Override
    public boolean left() {
        switch (this.selection.getViewType()) {
            case LISTVIEW:

                //we are in a list and cant move one out.
                return false;

            case DETAILVIEW:

                //go back to ListView
                this.selection = new Selection(this.selection.getRecipeID(), this.selection.getRecipeIndex());
                return true;

            case DETAILLISTVIEW:

                //go back to DetailView
                this.selection = new Selection(this.selection, this.selection.getRecipeDetail());
                return true;

            default:
                //we can't step one step out
                return false;
        }
    }

    /**
     * Navigate right (Language or other input method). e.g. select a Dish from a List
     *
     * @return true: commend was accepted and processed; false: command is not allowed/was not able to processed.
     */
    @Override
    public boolean right() {
        switch (this.selection.getViewType()) {
            case LISTVIEW:

                //check if selection is not empty
                if (this.recipeList.isEmpty()) {
                    return false;
                }

                //the new View should be default the first one
				this.selection = new Selection(this.selection, RecipeDetail.getByIndex(0));
                return true;

            case DETAILVIEW:

                //check if the DetailField is a List
                try {
                    //try to get the List
                    final List detailList = getDetailList(this.recipeList, this.selection);

                    //check if empty
                    if (detailList.isEmpty())
                        return false;

                    //Default List element should be the first
					this.selection = new Selection(this.selection, 0);
                    return true;

                } catch (final Exception e) {
                    return false;
                }

            case DETAILLISTVIEW:

                //Because we can't step one view deeper
                return false;

            default:
                return false;
        }
    }

    @Override
    public boolean volUp() {
        return false;
    }

    @Override
    public boolean volDown() {
        return false;
    }

    @Override
    public boolean mute() {
        return false;
    }

    @Override
    public boolean action() {
        return false;
    }

    @Override
    public boolean back() {
        return false;
    }

    @Override
    public boolean forth() {
        return false;
    }

    /**
     * v04
     *
     * @param recipeIndex index of Recipe in Array
     * @return true if successful
     */
    @Override
    public boolean set(final int recipeIndex) {

        //check if Recipe is available
        if (this.recipeList.size() <= recipeIndex || recipeIndex < 0) {
            return false;
        }

        //get the RecipeID
        final long recipeID = this.recipeList.get(recipeIndex).getId();

        final Selection listView = new Selection(recipeID, recipeIndex);

        switch (this.selection.getViewType()) {
            case LISTVIEW:

				this.selection = listView;
                return true;

            case DETAILVIEW:

                //Set the detailView
				this.selection = new Selection(
                        listView,
						this.selection.getRecipeDetail()
                );
                return true;

            case DETAILLISTVIEW:


				this.selection = new Selection(
                        //Set the detailView
                        new Selection(
                                listView,
								this.selection.getRecipeDetail()
                        ),
                        //default go to first list element
                        0);
                return true;

            default:
                return false;
        }
    }


    private static List getDetailList(final List<Recipe> recipeList, final Selection selection) throws NoSuchFieldException, IllegalAccessException {
        final Field field = recipeList.get(selection.getRecipeIndex()).getClass().getDeclaredField(selection.getRecipeDetailJSONKey());
        field.setAccessible(true);
        return (List) field.get(recipeList.get(selection.getRecipeIndex()));
    }


    private static final class Selection {

        //id of dish in list
        private final long recipeID;
        //index of dish in list
        private final int recipeIndex;

        // nulll if not set | name of current Field object of Recipe (e.g. Name, Instructions, Categories).
        private final RecipeDetail recipeDetail;
        // -1 if not set | index of current Field of Recipe (e.g. 1).
        private final int recipeDetailListIndex;

        private enum ViewType {
            LISTVIEW, DETAILVIEW, DETAILLISTVIEW
        }

        private final ViewType viewType;


        /**
         * [ListView] A Point to tell what is selected and what we should see right now e.g. Recipe 0
         *
         * @param recipeID    the ID of the selected Recipe
         * @param recipeIndex the index of the recipe in the current recipeList
         */
        public Selection(final long recipeID, final int recipeIndex) {
            this.viewType = ViewType.LISTVIEW;
            this.recipeID = recipeID;
            this.recipeIndex = recipeIndex;
            this.recipeDetail = null;
            this.recipeDetailListIndex = -1;
        }

        /**
         * [DetailView] A Point to tell what is selected and what we should see right now e.g. Recipes Overview
         *
         * @param traversingPoint the current traversing point, it will used for the current recipe.
         * @param recipeDetail    the Detailfield of the current selection
         */
        public Selection(final Selection traversingPoint, final RecipeDetail recipeDetail) {
            this.viewType = ViewType.DETAILVIEW;
            this.recipeID = traversingPoint.getRecipeID();
            this.recipeIndex = traversingPoint.getRecipeIndex();
            this.recipeDetail = recipeDetail;
            this.recipeDetailListIndex = -1;
        }

        /**
         * [DetailListView] A Point to tell what is selected and what we should see right now, e.g. Ingredients, step 0
         *
         * @param traversingPoint       the current traversing point, it will used for the current recipe.
         * @param recipeDetailListIndex index of the list of the current DetailObject (step 0)
         */
        public Selection(final Selection traversingPoint, final int recipeDetailListIndex) {
            this.viewType = ViewType.DETAILLISTVIEW;
            this.recipeID = traversingPoint.getRecipeID();
            this.recipeIndex = traversingPoint.getRecipeIndex();
            this.recipeDetail = traversingPoint.getRecipeDetail();
            this.recipeDetailListIndex = recipeDetailListIndex;
        }


        public long getRecipeID() {
            return this.recipeID;
        }

        public int getRecipeIndex() {
            return this.recipeIndex;
        }

        // we want to return the html-id
        public String getRecipeDetailJSONKey() {
            if (this.recipeDetail != null)
                return this.recipeDetail.getJsonKey();
            return "";
        }

        // we want to return the html-id
        public int getRecipeDetailIndex() {
            if (this.recipeDetail != null)
                return this.recipeDetail.getIndex();
            return -1;
        }

        // don't send the Enum
        private RecipeDetail getRecipeDetail() {
            return this.recipeDetail;
        }

        public int getRecipeDetailListIndex() {
            return this.recipeDetailListIndex;
        }

        // don't send the viewtype
        private ViewType getViewType() {
            return this.viewType;
        }
    }
}
