package by.abram.astore.mapper;

import by.abram.astore.dto.ItemDto;
import by.abram.astore.entity.Item;
import org.springframework.stereotype.Component;

@Component
public class ItemMapper {

    public ItemDto toDto(Item item) {
        if (item == null) {
            return null;
        }

        ItemDto dto = new ItemDto();
        dto.setId(item.getId());
        dto.setQuantity(item.getQuantity());
        dto.setPrice(item.getPrice());
        dto.setProductName(item.getProductName());

        if (item.getProduct() != null) {
            dto.setProductId(item.getProduct().getId());
        }

        return dto;
    }

    public Item toEntity(ItemDto dto) {
        if (dto == null) {
            return null;
        }

        Item item = new Item();
        item.setId(dto.getId());
        item.setQuantity(dto.getQuantity());
        item.setPrice(dto.getPrice());
        item.setProductName(dto.getProductName());

        return item;
    }
}