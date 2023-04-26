package eu.fiit.cookingmanager.cookingmanager.repository.entity;

public class IngredientRecipe {

    private int pieces;
    private int recipeId;
    private int ingredienId;

    public int getPieces() {
        return pieces;
    }

    public void setPieces(int pieces) {
        this.pieces = pieces;
    }

    public int getRecipeId() {
        return recipeId;
    }

    public void setRecipeId(int recipeId) {
        this.recipeId = recipeId;
    }

    public int getIngredienId() {
        return ingredienId;
    }

    public void setIngredienId(int ingredienId) {
        this.ingredienId = ingredienId;
    }
}
