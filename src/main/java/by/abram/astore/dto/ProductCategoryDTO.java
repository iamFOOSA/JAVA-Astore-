package by.abram.astore.dto;

public interface ProductCategoryDTO {
    Long getId();
    String getName();
    String getDescription();
    java.math.BigDecimal getPrice();
    Integer getQuantity();
    String getCategoryName();
}