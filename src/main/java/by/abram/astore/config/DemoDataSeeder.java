package by.abram.astore.config;

import by.abram.astore.entity.Cart;
import by.abram.astore.entity.CartItem;
import by.abram.astore.entity.Category;
import by.abram.astore.entity.Item;
import by.abram.astore.entity.Order;
import by.abram.astore.entity.Product;
import by.abram.astore.entity.Status;
import by.abram.astore.entity.User;
import by.abram.astore.repository.CategoryRepository;
import by.abram.astore.repository.CartItemRepository;
import by.abram.astore.repository.CartRepository;
import by.abram.astore.repository.ItemRepository;
import by.abram.astore.repository.OrderRepository;
import by.abram.astore.repository.ProductRepository;
import by.abram.astore.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "astore.demo.seed", havingValue = "true")
public class DemoDataSeeder implements CommandLineRunner {

    private static final String DEMO_PASSWORD = "demo-password";
    private static final String USER_MARTA = "marta";
    private static final String USER_ALEX = "alex";
    private static final String USER_SOFIA = "sofia";
    private static final String USER_IVAN = "ivan";
    private static final String USER_NIKA = "nika";
    private static final String CATEGORY_ELECTRONICS = "electronics";
    private static final String CATEGORY_BOOKS = "books";
    private static final String CATEGORY_HOME = "home";
    private static final String CATEGORY_BATH = "bath";
    private static final String CATEGORY_CLOTHES = "clothes";
    private static final String CATEGORY_SHOES = "shoes";

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final ItemRepository itemRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;

    @Override
    @Transactional
    public void run(String... args) {
        clearDatabase();

        Map<String, Category> categories = createCategories();
        Map<String, Product> products = createProducts(categories);
        Map<String, User> users = createUsers();

        createOrder(users.get(USER_MARTA), Status.DELIVERED, 18, List.of(
                orderItem(products.get("phone"), 1),
                orderItem(products.get("book"), 1),
                orderItem(products.get("faceWash"), 2)
        ));
        createOrder(users.get(USER_ALEX), Status.PROCESSING, 4, List.of(
                orderItem(products.get("lamp"), 1),
                orderItem(products.get("hoodie"), 1),
                orderItem(products.get("sneakers"), 1)
        ));
        createOrder(users.get(USER_SOFIA), Status.SHIPPED, 9, List.of(
                orderItem(products.get("headphones"), 1),
                orderItem(products.get("towels"), 1),
                orderItem(products.get("plaid"), 1)
        ));
        createOrder(users.get(USER_IVAN), Status.NEW, 1, List.of(
                orderItem(products.get("tablet"), 1),
                orderItem(products.get("storageBoxes"), 2),
                orderItem(products.get("shampoo"), 1)
        ));
        createOrder(users.get(USER_NIKA), Status.DELIVERED, 27, List.of(
                orderItem(products.get("cookbook"), 1),
                orderItem(products.get("shirt"), 1),
                orderItem(products.get("loafers"), 1)
        ));

        createCart(users.get(USER_MARTA), List.of(
                cartItem(products.get("diffuser"), 1),
                cartItem(products.get("cream"), 2)
        ));
        createCart(users.get(USER_ALEX), List.of(
                cartItem(products.get("powerbank"), 1),
                cartItem(products.get("jeans"), 1)
        ));
        createCart(users.get(USER_SOFIA), List.of(
                cartItem(products.get("sandals"), 1),
                cartItem(products.get("codeBook"), 1)
        ));

        log.info("Astore demo database was reset and seeded with {} products", products.size());
    }

    private void clearDatabase() {
        cartItemRepository.deleteAllInBatch();
        cartRepository.deleteAllInBatch();
        itemRepository.deleteAllInBatch();
        orderRepository.deleteAllInBatch();

        List<Product> products = productRepository.findAll();
        products.forEach(product -> product.getCategories().clear());
        productRepository.saveAll(products);
        productRepository.deleteAllInBatch();

        categoryRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();
    }

    private Map<String, Category> createCategories() {
        Category electronics = category("Электроника", "Гаджеты, звук и компактная техника для дома и работы.");
        Category books = category("Книги", "Полезные, красивые и практичные книги для учёбы и отдыха.");
        Category home = category("Товары для дома", "Предметы, которые делают комнату уютнее и функциональнее.");
        Category bath = category("Товары для ванной и умывания", "Мягкий текстиль и уход для спокойного ежедневного ритуала.");
        Category clothes = category("Одежда", "Базовые вещи на каждый день с чистым силуэтом.");
        Category shoes = category("Обувь", "Удобные пары для города, прогулок и переменчивой погоды.");

        categoryRepository.saveAll(List.of(electronics, books, home, bath, clothes, shoes));

        return Map.of(
                CATEGORY_ELECTRONICS, electronics,
                CATEGORY_BOOKS, books,
                CATEGORY_HOME, home,
                CATEGORY_BATH, bath,
                CATEGORY_CLOTHES, clothes,
                CATEGORY_SHOES, shoes
        );
    }

    private Map<String, Product> createProducts(Map<String, Category> categories) {
        Product phone = product(
                "Смартфон Aurora X",
                "Яркий экран, хорошая камера и быстрая зарядка для ежедневной связи и фото.",
                "1599.00",
                24,
                "/product-images/smartphone.svg",
                List.of(categories.get(CATEGORY_ELECTRONICS))
        );
        Product headphones = product(
                "Наушники WavePods Lite",
                "Беспроводной звук, мягкая посадка и кейс, который легко помещается в карман.",
                "249.00",
                38,
                "/product-images/headphones.svg",
                List.of(categories.get(CATEGORY_ELECTRONICS))
        );
        Product speaker = product(
                "Умная колонка Home Beat",
                "Компактная колонка для кухни, комнаты и фоновой музыки во время уборки.",
                "329.00",
                19,
                "/product-images/smart-speaker.svg",
                List.of(categories.get(CATEGORY_ELECTRONICS), categories.get(CATEGORY_HOME))
        );
        Product book = product(
                "Книга «Java без паники»",
                "Понятное введение в Java с примерами, задачами и спокойным темпом объяснения.",
                "64.90",
                42,
                "/product-images/book-java.svg",
                List.of(categories.get(CATEGORY_BOOKS))
        );
        Product planner = product(
                "Планер «Спокойная неделя»",
                "Бумажный планер с чистой сеткой, трекером привычек и местом для заметок.",
                "39.90",
                55,
                "/product-images/planner-book.svg",
                List.of(categories.get(CATEGORY_BOOKS))
        );
        Product lamp = product(
                "Настольная лампа Nordic",
                "Тёплый свет, матовый корпус и устойчивое основание для рабочего стола.",
                "129.00",
                21,
                "/product-images/desk-lamp.svg",
                List.of(categories.get(CATEGORY_HOME))
        );
        Product plaid = product(
                "Плед Cloud Soft",
                "Мягкий плед для дивана, чтения и тех вечеров, когда хочется тишины.",
                "89.00",
                30,
                "/product-images/plaid.svg",
                List.of(categories.get(CATEGORY_HOME), categories.get(CATEGORY_CLOTHES))
        );
        Product organizer = product(
                "Органайзер для ванной Clear Box",
                "Прозрачный органайзер для кремов, щёток и мелочей у раковины.",
                "34.50",
                64,
                "/product-images/bath-organizer.svg",
                List.of(categories.get(CATEGORY_BATH), categories.get(CATEGORY_HOME))
        );
        Product towels = product(
                "Набор полотенец Spa Cotton",
                "Комплект из трёх мягких полотенец спокойных оттенков для ванной.",
                "74.00",
                36,
                "/product-images/towels.svg",
                List.of(categories.get(CATEGORY_BATH))
        );
        Product faceWash = product(
                "Гель для умывания Fresh Foam",
                "Нежная пенка для ежедневного очищения без ощущения сухости.",
                "27.90",
                80,
                "/product-images/face-wash.svg",
                List.of(categories.get(CATEGORY_BATH))
        );
        Product hoodie = product(
                "Худи Daily Oversize",
                "Плотный хлопок, свободная посадка и карман-кенгуру для прохладных дней.",
                "119.00",
                26,
                "/product-images/hoodie.svg",
                List.of(categories.get(CATEGORY_CLOTHES))
        );
        Product tshirt = product(
                "Футболка Base Cotton",
                "Базовая футболка из мягкого хлопка без лишних принтов.",
                "49.00",
                74,
                "/product-images/tshirt.svg",
                List.of(categories.get(CATEGORY_CLOTHES))
        );
        Product sneakers = product(
                "Кроссовки Urban Run",
                "Лёгкая городская пара с мягкой подошвой и аккуратным силуэтом.",
                "179.00",
                31,
                "/product-images/sneakers.svg",
                List.of(categories.get(CATEGORY_SHOES))
        );
        Product boots = product(
                "Ботинки Trail Soft",
                "Устойчивые ботинки для прогулок, дождливых дней и плотного графика.",
                "229.00",
                18,
                "/product-images/boots.svg",
                List.of(categories.get(CATEGORY_SHOES))
        );
        Product tablet = product(
                "Планшет Canvas Tab",
                "Лёгкий планшет для учёбы, видео, заметок и быстрых задач в дороге.",
                "899.00",
                17,
                "/product-images/tablet.svg",
                List.of(categories.get(CATEGORY_ELECTRONICS))
        );
        Product powerbank = product(
                "Пауэрбанк Volt Mini",
                "Компактный аккумулятор с быстрой зарядкой для телефона и наушников.",
                "79.00",
                46,
                "/product-images/powerbank.svg",
                List.of(categories.get(CATEGORY_ELECTRONICS))
        );
        Product cookbook = product(
                "Книга «Ужин без суеты»",
                "Красивые рецепты на каждый день: простые продукты, понятные шаги и уютные фото.",
                "58.00",
                34,
                "/product-images/cookbook.svg",
                List.of(categories.get(CATEGORY_BOOKS), categories.get(CATEGORY_HOME))
        );
        Product codeBook = product(
                "Книга «Чистый код рядом»",
                "Практичная книга о стиле, читаемости и маленьких привычках хорошего разработчика.",
                "72.00",
                28,
                "/product-images/code-book.svg",
                List.of(categories.get(CATEGORY_BOOKS))
        );
        Product diffuser = product(
                "Аромадиффузор Calm Reed",
                "Стеклянный диффузор с мягким ароматом хлопка для спальни и ванной.",
                "52.00",
                39,
                "/product-images/diffuser.svg",
                List.of(categories.get(CATEGORY_HOME), categories.get(CATEGORY_BATH))
        );
        Product storageBoxes = product(
                "Контейнеры для хранения SetBox",
                "Набор прозрачных контейнеров для кухни, шкафа и аккуратных полок.",
                "69.00",
                44,
                "/product-images/storage-box.svg",
                List.of(categories.get(CATEGORY_HOME))
        );
        Product shampoo = product(
                "Шампунь Soft Balance",
                "Мягкий шампунь для ежедневного ухода с лёгким свежим ароматом.",
                "31.50",
                72,
                "/product-images/shampoo.svg",
                List.of(categories.get(CATEGORY_BATH))
        );
        Product cream = product(
                "Крем для рук Velvet Care",
                "Плотный крем, который быстро впитывается и спасает кожу после частого мытья.",
                "24.90",
                88,
                "/product-images/cream.svg",
                List.of(categories.get(CATEGORY_BATH))
        );
        Product shirt = product(
                "Рубашка Linen Breeze",
                "Свободная рубашка из лёгкой ткани для офиса, прогулок и спокойных выходных.",
                "98.00",
                29,
                "/product-images/shirt.svg",
                List.of(categories.get(CATEGORY_CLOTHES))
        );
        Product jeans = product(
                "Джинсы Straight Denim",
                "Прямой крой, плотный деним и универсальный цвет для базового гардероба.",
                "139.00",
                23,
                "/product-images/jeans.svg",
                List.of(categories.get(CATEGORY_CLOTHES))
        );
        Product loafers = product(
                "Лоферы City Walk",
                "Мягкая городская пара для работы и прогулок без лишней строгости.",
                "189.00",
                20,
                "/product-images/loafers.svg",
                List.of(categories.get(CATEGORY_SHOES))
        );
        Product sandals = product(
                "Сандалии Coast Day",
                "Лёгкие сандалии с мягкими ремешками для тёплой погоды и поездок.",
                "115.00",
                33,
                "/product-images/sandals.svg",
                List.of(categories.get(CATEGORY_SHOES))
        );

        productRepository.saveAll(List.of(
                phone,
                headphones,
                speaker,
                tablet,
                powerbank,
                book,
                planner,
                cookbook,
                codeBook,
                lamp,
                plaid,
                diffuser,
                storageBoxes,
                organizer,
                towels,
                faceWash,
                shampoo,
                cream,
                hoodie,
                tshirt,
                shirt,
                jeans,
                sneakers,
                boots,
                loafers,
                sandals
        ));

        return Map.ofEntries(
                Map.entry("phone", phone),
                Map.entry("headphones", headphones),
                Map.entry("speaker", speaker),
                Map.entry("book", book),
                Map.entry("planner", planner),
                Map.entry("lamp", lamp),
                Map.entry("plaid", plaid),
                Map.entry("organizer", organizer),
                Map.entry("towels", towels),
                Map.entry("faceWash", faceWash),
                Map.entry("hoodie", hoodie),
                Map.entry("tshirt", tshirt),
                Map.entry("sneakers", sneakers),
                Map.entry("boots", boots),
                Map.entry("tablet", tablet),
                Map.entry("powerbank", powerbank),
                Map.entry("cookbook", cookbook),
                Map.entry("codeBook", codeBook),
                Map.entry("diffuser", diffuser),
                Map.entry("storageBoxes", storageBoxes),
                Map.entry("shampoo", shampoo),
                Map.entry("cream", cream),
                Map.entry("shirt", shirt),
                Map.entry("jeans", jeans),
                Map.entry("loafers", loafers),
                Map.entry("sandals", sandals)
        );
    }

    private Map<String, User> createUsers() {
        User marta = user("marta.sokolova@astore.local", "Марта", "Соколова");
        User alex = user("alex.volkov@astore.local", "Алексей", "Волков");
        User sofia = user("sofia.romanova@astore.local", "София", "Романова");
        User ivan = user("ivan.melnik@astore.local", "Иван", "Мельник");
        User nika = user("nika.orlova@astore.local", "Ника", "Орлова");

        userRepository.saveAll(List.of(marta, alex, sofia, ivan, nika));

        return Map.of(
                USER_MARTA, marta,
                USER_ALEX, alex,
                USER_SOFIA, sofia,
                USER_IVAN, ivan,
                USER_NIKA, nika
        );
    }

    private Category category(String name, String description) {
        Category category = new Category();
        category.setName(name);
        category.setDescription(description);
        return category;
    }

    private Product product(String name,
                            String description,
                            String price,
                            Integer quantity,
                            String imageUrl,
                            List<Category> categories) {
        Product product = new Product();
        product.setName(name);
        product.setDescription(description);
        product.setPrice(new BigDecimal(price));
        product.setQuantity(quantity);
        product.setImageUrl(imageUrl);
        product.setCategories(categories);
        return product;
    }

    private User user(String email, String firstName, String lastName) {
        User user = new User();
        user.setEmail(email);
        user.setPassword(DEMO_PASSWORD);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        return user;
    }

    private Item orderItem(Product product, int quantity) {
        Item item = new Item();
        item.setProduct(product);
        item.setProductName(product.getName());
        item.setPrice(product.getPrice());
        item.setQuantity(quantity);
        return item;
    }

    private CartItem cartItem(Product product, int quantity) {
        CartItem item = new CartItem();
        item.setProduct(product);
        item.setQuantity(quantity);
        return item;
    }

    private void createOrder(User user, Status status, int daysAgo, List<Item> items) {
        Order order = new Order();
        order.setUser(user);
        order.setStatus(status);
        order.setOrderDate(LocalDateTime.now().minusDays(daysAgo));

        items.forEach(order::addItem);
        order.setTotalAmount(order.calculateTotal());

        orderRepository.save(order);
    }

    private void createCart(User user, List<CartItem> items) {
        Cart cart = new Cart();
        cart.setUser(user);
        items.forEach(cart::addItem);
        cartRepository.save(cart);
    }
}
