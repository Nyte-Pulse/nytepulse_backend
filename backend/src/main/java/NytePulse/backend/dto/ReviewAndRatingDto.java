package NytePulse.backend.dto;

public class ReviewAndRatingDto {

    private String userId;
    private String review;
    private int rating;
    private String nameOfReviewer;

    private String idOfReviewer;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }


    public String getReview() {
        return review;
    }

    public void setReview(String review) {
        this.review = review;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public String getNameOfReviewer() {
        return nameOfReviewer;
    }

    public void setNameOfReviewer(String nameOfReviewer) {
        this.nameOfReviewer = nameOfReviewer;
    }

    public String getIdOfReviewer() {
        return idOfReviewer;
    }
    public void setIdOfReviewer(String idOfReviewer) {
        this.idOfReviewer = idOfReviewer;
    }
}
