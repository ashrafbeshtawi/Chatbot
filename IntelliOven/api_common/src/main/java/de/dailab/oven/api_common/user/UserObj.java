package de.dailab.oven.api_common.user;

import de.dailab.oven.api_common.Sendable;

import java.util.List;

public class UserObj {

    private UserObj(){
        //SOnarQube
    }

    // for requesting
    public static class UserRequest {

        private String userName = "";
        private long userID = -1;
        private int rating = -69;
        private long recipeID = -69;

        public UserRequest() {

        }

        public UserRequest(final long userID) {
            this.userID = userID;
            this.userName = "";
        }

        public UserRequest(final long userID, final int rating, final long recipeID) {
            this.userID = userID;
            this.userName = "";
            this.rating = rating;
            this.recipeID = recipeID;
        }

        public UserRequest(final String userName) {
            this.userName = userName;
            this.userID = -1;
        }

        public UserRequest(final long userID, final String userName) {
            this.userID = userID;
            this.userName = userName;
        }

        public int getRating() {
            return this.rating;
        }

        public long getRecipeID() {
            return this.recipeID;
        }

        public long getUserID() {
            return this.userID;
        }

        public String getUserName() {
            return this.userName;
        }

    }


    public static class UserResponse implements Sendable {
        List<de.dailab.oven.model.data_model.User> userList;

        /**
         * @param userList for Jackson
         */
        public UserResponse(final List<de.dailab.oven.model.data_model.User> userList) {
            this.userList = userList;
        }

        /**
         * for Jackson
         *
         * @return userList
         */
        public List<de.dailab.oven.model.data_model.User> getUserList() {
            return this.userList;
        }
    }

}
