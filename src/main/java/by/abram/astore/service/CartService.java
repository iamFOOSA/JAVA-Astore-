package by.abram.astore.service;

import by.abram.astore.dto.CartDto;
import by.abram.astore.dto.CartItemRequest;
import by.abram.astore.entity.Cart;
import by.abram.astore.entity.CartItem;
import by.abram.astore.entity.Product;
import by.abram.astore.entity.User;
import by.abram.astore.exception.BusinessLogicException;
import by.abram.astore.exception.ResourceNotFoundException;
import by.abram.astore.mapper.CartMapper;
import by.abram.astore.repository.CartRepository;
import by.abram.astore.repository.ProductRepository;
import by.abram.astore.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final CartMapper cartMapper;

    @Transactional
    public CartDto findByUser(Long userId) {
        return cartMapper.toDto(getOrCreateCart(userId));
    }

    @Transactional
    public CartDto addItem(Long userId, CartItemRequest request) {
        Cart cart = getOrCreateCart(userId);
        Product product = findProduct(request.getProductId());
        CartItem existingItem = findCartItem(cart, product.getId());
        int nextQuantity = request.getQuantity();

        if (existingItem != null) {
            nextQuantity += existingItem.getQuantity();
        }

        validateStock(product, nextQuantity);

        if (existingItem != null) {
            existingItem.setQuantity(nextQuantity);
        } else {
            CartItem item = new CartItem();
            item.setProduct(product);
            item.setQuantity(request.getQuantity());
            cart.addItem(item);
        }

        return cartMapper.toDto(cartRepository.save(cart));
    }

    @Transactional
    public CartDto updateItem(Long userId, Long productId, CartItemRequest request) {
        Cart cart = getOrCreateCart(userId);
        Product product = findProduct(productId);
        CartItem item = findCartItem(cart, productId);

        if (item == null) {
            throw new ResourceNotFoundException("Cart item", productId);
        }

        validateStock(product, request.getQuantity());
        item.setQuantity(request.getQuantity());

        return cartMapper.toDto(cartRepository.save(cart));
    }

    @Transactional
    public CartDto removeItem(Long userId, Long productId) {
        Cart cart = getOrCreateCart(userId);
        CartItem item = findCartItem(cart, productId);

        if (item != null) {
            cart.removeItem(item);
        }

        return cartMapper.toDto(cartRepository.save(cart));
    }

    @Transactional
    public CartDto clear(Long userId) {
        Cart cart = getOrCreateCart(userId);
        cart.getItems().clear();
        return cartMapper.toDto(cartRepository.save(cart));
    }

    private Cart getOrCreateCart(Long userId) {
        return cartRepository.findByUserId(userId)
                .orElseGet(() -> createCart(userId));
    }

    private Cart createCart(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
        Cart cart = new Cart();
        cart.setUser(user);
        return cartRepository.save(cart);
    }

    private Product findProduct(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", productId));
    }

    private CartItem findCartItem(Cart cart, Long productId) {
        return cart.getItems().stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst()
                .orElse(null);
    }

    private void validateStock(Product product, Integer requestedQuantity) {
        if (requestedQuantity == null || requestedQuantity < 1) {
            throw new BusinessLogicException("Количество должно быть больше 0");
        }

        if (product.getQuantity() != null && requestedQuantity > product.getQuantity()) {
            throw new BusinessLogicException(String.format(
                    "На складе только %d шт. товара '%s'",
                    product.getQuantity(),
                    product.getName()
            ));
        }
    }
}
