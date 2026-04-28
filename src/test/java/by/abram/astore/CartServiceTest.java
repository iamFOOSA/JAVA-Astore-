package by.abram.astore;

import by.abram.astore.dto.CartDto;
import by.abram.astore.dto.CartItemRequest;
import by.abram.astore.entity.Cart;
import by.abram.astore.entity.Product;
import by.abram.astore.entity.User;
import by.abram.astore.exception.BusinessLogicException;
import by.abram.astore.exception.ResourceNotFoundException;
import by.abram.astore.mapper.CartMapper;
import by.abram.astore.repository.CartRepository;
import by.abram.astore.repository.ProductRepository;
import by.abram.astore.repository.UserRepository;
import by.abram.astore.service.CartService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock private CartRepository cartRepository;
    @Mock private UserRepository userRepository;
    @Mock private ProductRepository productRepository;
    @Mock private CartMapper cartMapper;

    @InjectMocks private CartService cartService;

    @Test
    void addItem_ShouldCreateCartAndItem() {
        User user = new User();
        user.setId(1L);

        Product product = new Product();
        product.setId(2L);
        product.setName("Тестовый товар");
        product.setPrice(BigDecimal.TEN);
        product.setQuantity(5);

        CartItemRequest request = new CartItemRequest();
        request.setProductId(2L);
        request.setQuantity(2);

        when(cartRepository.findByUserId(1L)).thenReturn(Optional.empty());
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(productRepository.findById(2L)).thenReturn(Optional.of(product));
        when(cartMapper.toDto(any(Cart.class))).thenReturn(new CartDto());

        CartDto result = cartService.addItem(1L, request);

        assertNotNull(result);
        verify(cartRepository, times(2)).save(any(Cart.class));
    }

    @Test
    void addItem_ShouldThrow_WhenProductStockIsNotEnough() {
        Cart cart = new Cart();
        User user = new User();
        user.setId(1L);
        cart.setUser(user);

        Product product = new Product();
        product.setId(2L);
        product.setName("Тестовый товар");
        product.setQuantity(1);

        CartItemRequest request = new CartItemRequest();
        request.setProductId(2L);
        request.setQuantity(3);

        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
        when(productRepository.findById(2L)).thenReturn(Optional.of(product));

        assertThrows(BusinessLogicException.class, () -> cartService.addItem(1L, request));
    }

    @Test
    void findByUser_ShouldThrow_WhenUserDoesNotExist() {
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.empty());
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> cartService.findByUser(1L));
    }
}
