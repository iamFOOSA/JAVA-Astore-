import { useEffect, useState } from "react";
import { api } from "./api";

const statuses = ["NEW", "PROCESSING", "SHIPPED", "DELIVERED", "CANCELLED"];
const tabs = [
  { id: "products", label: "Витрина" },
  { id: "categories", label: "Категории" },
  { id: "users", label: "Пользователи" },
  { id: "orders", label: "Заказы" },
  { id: "items", label: "Позиции" }
];

const imageChoices = [
  { label: "Смартфон", url: "/product-images/smartphone.svg" },
  { label: "Наушники", url: "/product-images/headphones.svg" },
  { label: "Колонка", url: "/product-images/smart-speaker.svg" },
  { label: "Планшет", url: "/product-images/tablet.svg" },
  { label: "Пауэрбанк", url: "/product-images/powerbank.svg" },
  { label: "Книга Java", url: "/product-images/book-java.svg" },
  { label: "Планер", url: "/product-images/planner-book.svg" },
  { label: "Кулинарная книга", url: "/product-images/cookbook.svg" },
  { label: "Книга по коду", url: "/product-images/code-book.svg" },
  { label: "Лампа", url: "/product-images/desk-lamp.svg" },
  { label: "Плед", url: "/product-images/plaid.svg" },
  { label: "Диффузор", url: "/product-images/diffuser.svg" },
  { label: "Контейнеры", url: "/product-images/storage-box.svg" },
  { label: "Органайзер", url: "/product-images/bath-organizer.svg" },
  { label: "Полотенца", url: "/product-images/towels.svg" },
  { label: "Умывание", url: "/product-images/face-wash.svg" },
  { label: "Шампунь", url: "/product-images/shampoo.svg" },
  { label: "Крем", url: "/product-images/cream.svg" },
  { label: "Худи", url: "/product-images/hoodie.svg" },
  { label: "Футболка", url: "/product-images/tshirt.svg" },
  { label: "Рубашка", url: "/product-images/shirt.svg" },
  { label: "Джинсы", url: "/product-images/jeans.svg" },
  { label: "Кроссовки", url: "/product-images/sneakers.svg" },
  { label: "Ботинки", url: "/product-images/boots.svg" },
  { label: "Лоферы", url: "/product-images/loafers.svg" },
  { label: "Сандалии", url: "/product-images/sandals.svg" }
];

const emptyProduct = {
  name: "",
  description: "",
  price: "",
  quantity: "",
  imageUrl: imageChoices[0].url,
  categories: []
};
const emptyCategory = { name: "", description: "" };
const emptyUser = { email: "", firstName: "", lastName: "" };
const emptyItem = { orderId: "", productId: "", quantity: 1, price: "" };
const emptyOrderDraft = { userId: "", productId: "", quantity: 1 };
const emptyQuickUser = { email: "", firstName: "", lastName: "" };
const ADMIN_USER_ID = "admin";

function App() {
  const [activeTab, setActiveTab] = useState("products");
  const [data, setData] = useState({ products: [], categories: [], users: [], orders: [], items: [] });
  const [catalogProducts, setCatalogProducts] = useState([]);
  const [selectedCategoryId, setSelectedCategoryId] = useState("all");
  const [query, setQuery] = useState("");
  const [isSearchFocused, setIsSearchFocused] = useState(false);
  const [cart, setCart] = useState([]);
  const [cartDrafts, setCartDrafts] = useState({});
  const [isCartOpen, setIsCartOpen] = useState(false);
  const [favorites, setFavorites] = useState([]);
  const [selectedUserId, setSelectedUserId] = useState(ADMIN_USER_ID);
  const [isUserMenuOpen, setIsUserMenuOpen] = useState(false);
  const [quickUserForm, setQuickUserForm] = useState(emptyQuickUser);
  const [loading, setLoading] = useState(true);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState("");
  const [, setNotice] = useState("");

  const [productForm, setProductForm] = useState(emptyProduct);
  const [categoryForm, setCategoryForm] = useState(emptyCategory);
  const [userForm, setUserForm] = useState(emptyUser);
  const [itemForm, setItemForm] = useState(emptyItem);
  const [orderDraft, setOrderDraft] = useState(emptyOrderDraft);
  const [orderItems, setOrderItems] = useState([]);
  const [productSearch, setProductSearch] = useState({ userId: "", categoryName: "", results: [] });

  const [editingProductId, setEditingProductId] = useState(null);
  const [editingCategoryId, setEditingCategoryId] = useState(null);
  const [editingUserId, setEditingUserId] = useState(null);
  const [editingItemId, setEditingItemId] = useState(null);

  const { products, categories, users, orders, items } = data;
  const productById = Object.fromEntries(products.map((product) => [product.id, product]));
  const userById = Object.fromEntries(users.map((user) => [user.id, user]));
  const selectedCategory = categories.find((category) => String(category.id) === String(selectedCategoryId));
  const isAdmin = selectedUserId === ADMIN_USER_ID;
  const selectedUser = users.find((user) => String(user.id) === String(selectedUserId));
  const selectedUserLabel = isAdmin
    ? "Админ"
    : selectedUser
      ? `${selectedUser.firstName} ${selectedUser.lastName}`
      : "Покупатель";
  const visibleTabs = isAdmin ? tabs : tabs.filter((tab) => tab.id === "products");
  const visibleProducts = catalogProducts
    .filter((product) => matchesQuery(product, query))
    .sort((first, second) => Number(first.id) - Number(second.id));
  const favoriteSet = new Set(favorites);
  const cartQuantity = cart.reduce((sum, item) => sum + item.quantity, 0);
  const cartTotal = cart.reduce((sum, item) => {
    const product = productById[item.productId];
    return sum + Number(product?.price ?? item.productPrice ?? 0) * item.quantity;
  }, 0);
  const cartItems = cart
    .map((item) => ({
      ...item,
      product: productById[item.productId] ?? {
        id: item.productId,
        name: item.productName,
        price: item.productPrice,
        imageUrl: item.productImageUrl
      }
    }))
    .filter((item) => item.product);
  const heroProducts = (visibleProducts.length > 0 ? visibleProducts : products).slice(0, 4);
  const searchSuggestions = unique([
    ...products.map((product) => product.name),
    ...categories.map((category) => category.name),
    ...users.flatMap((user) => [`${user.firstName} ${user.lastName}`, user.email]),
    ...statuses,
    "кроссовки",
    "книги",
    "полотенца",
    "товары для дома",
    "уход для ванной"
  ]);
  const visibleSearchSuggestions = query.trim().length >= 2 && isSearchFocused
    ? searchSuggestions
      .filter((suggestion) => suggestion.toLowerCase().includes(query.trim().toLowerCase()))
      .slice(0, 6)
    : [];

  useEffect(() => {
    loadData();
  }, []);

  useEffect(() => {
    if (!isAdmin && activeTab !== "products") {
      setActiveTab("products");
      return;
    }

    if (!tabs.some((tab) => tab.id === activeTab)) {
      setActiveTab("products");
    }
  }, [activeTab, isAdmin]);

  useEffect(() => {
    loadCartForUser(selectedUserId);
  }, [selectedUserId]);

  async function loadData() {
    setLoading(true);
    setError("");

    try {
      const [nextProducts, nextCategories, nextUsers, nextOrders, nextItems] = await Promise.all([
        api.listProducts(),
        api.listCategories(),
        api.listUsers(),
        api.listOrders(),
        api.listItems()
      ]);
      const nextCatalog = selectedCategoryId === "all"
        ? nextProducts
        : await api.listProductsByCategory(selectedCategoryId);

      setData({
        products: nextProducts,
        categories: nextCategories,
        users: nextUsers,
        orders: nextOrders,
        items: nextItems
      });
      setSelectedUserId((current) => current || nextUsers[0]?.id || "");
      setCatalogProducts(nextCatalog);
    } catch (requestError) {
      setError(requestError.message);
    } finally {
      setLoading(false);
    }
  }

  async function loadCartForUser(userId) {
    if (!userId || userId === ADMIN_USER_ID) {
      setCart([]);
      setIsCartOpen(false);
      return;
    }

    try {
      const nextCart = await api.getCart(userId);
      setCart(normalizeCartItems(nextCart));
    } catch (requestError) {
      setCart([]);
      setError(requestError.message);
    }
  }

  async function selectCategory(categoryId) {
    setSelectedCategoryId(categoryId);
    setActiveTab("products");
    setLoading(true);
    setError("");

    try {
      const nextProducts = categoryId === "all"
        ? await api.listProducts()
        : await api.listProductsByCategory(categoryId);
      setCatalogProducts(nextProducts);
    } catch (requestError) {
      setError(requestError.message);
    } finally {
      setLoading(false);
    }
  }

  async function run(action, successMessage, { reload = true } = {}) {
    setBusy(true);
    setError("");
    setNotice("");

    try {
      const result = await action();
      if (reload) {
        await loadData();
      }
      setNotice(successMessage);
      return result;
    } catch (requestError) {
      setError(requestError.message);
      return null;
    } finally {
      setBusy(false);
    }
  }

  function handleProductSubmit(event) {
    event.preventDefault();

    if (!isAdmin) {
      setError("Создавать и изменять товары может только админ");
      return;
    }

    const payload = {
      name: productForm.name.trim(),
      description: productForm.description.trim(),
      price: Number(productForm.price),
      quantity: Number(productForm.quantity),
      imageUrl: productForm.imageUrl,
      categories: productForm.categories
    };

    run(
      () => editingProductId ? api.updateProduct(editingProductId, payload) : api.createProduct(payload),
      editingProductId ? "Товар обновлён" : "Товар создан"
    ).then((result) => {
      if (result) {
        setProductForm(emptyProduct);
        setEditingProductId(null);
      }
    });
  }

  function editProduct(product) {
    if (!isAdmin) {
      setError("Редактировать товары может только админ");
      return;
    }

    setActiveTab("products");
    setEditingProductId(product.id);
    setProductForm({
      name: product.name ?? "",
      description: product.description ?? "",
      price: product.price ?? "",
      quantity: product.quantity ?? "",
      imageUrl: product.imageUrl || imageChoices[0].url,
      categories: product.categories ?? []
    });
  }

  function toggleProductCategory(categoryName) {
    setProductForm((current) => ({
      ...current,
      categories: current.categories.includes(categoryName)
        ? current.categories.filter((name) => name !== categoryName)
        : [...current.categories, categoryName]
    }));
  }

  function handleCategorySubmit(event) {
    event.preventDefault();
    const payload = {
      name: categoryForm.name.trim(),
      description: categoryForm.description.trim()
    };

    run(
      () => editingCategoryId ? api.updateCategory(editingCategoryId, payload) : api.createCategory(payload),
      editingCategoryId ? "Категория обновлена" : "Категория создана"
    ).then((result) => {
      if (result) {
        setCategoryForm(emptyCategory);
        setEditingCategoryId(null);
      }
    });
  }

  function editCategory(category) {
    setActiveTab("categories");
    setEditingCategoryId(category.id);
    setCategoryForm({ name: category.name ?? "", description: category.description ?? "" });
  }

  function handleUserSubmit(event) {
    event.preventDefault();
    const payload = {
      email: userForm.email.trim(),
      firstName: userForm.firstName.trim(),
      lastName: userForm.lastName.trim()
    };

    run(
      () => editingUserId ? api.updateUser(editingUserId, payload) : api.createUser(payload),
      editingUserId ? "Пользователь обновлён" : "Пользователь создан"
    ).then((result) => {
      if (result) {
        setUserForm(emptyUser);
        setEditingUserId(null);
      }
    });
  }

  function handleQuickUserSubmit(event) {
    event.preventDefault();
    const payload = {
      email: quickUserForm.email.trim(),
      firstName: quickUserForm.firstName.trim(),
      lastName: quickUserForm.lastName.trim()
    };

    run(() => api.createUser(payload), "Клиент создан").then((result) => {
      if (result) {
        setSelectedUserId(result.id);
        setQuickUserForm(emptyQuickUser);
        setIsUserMenuOpen(false);
      }
    });
  }

  function editUser(user) {
    setActiveTab("users");
    setEditingUserId(user.id);
    setUserForm({
      email: user.email ?? "",
      firstName: user.firstName ?? "",
      lastName: user.lastName ?? ""
    });
  }

  function addOrderItem() {
    const product = productById[orderDraft.productId];
    const quantity = Number(orderDraft.quantity);

    if (!product) {
      setError("Выберите товар для заказа");
      return;
    }

    if (!quantity || quantity < 1) {
      setError("Количество товара должно быть больше нуля");
      return;
    }

    setOrderItems((current) => [
      ...current,
      {
        productId: product.id,
        productName: product.name,
        quantity,
        price: Number(product.price)
      }
    ]);
    setOrderDraft((current) => ({ ...current, productId: "", quantity: 1 }));
  }

  function handleOrderSubmit(event) {
    event.preventDefault();

    if (orderItems.length === 0) {
      setError("Добавьте хотя бы одну позицию в заказ");
      return;
    }

    const payload = {
      userId: Number(orderDraft.userId),
      items: orderItems.map((item) => ({
        productId: item.productId,
        quantity: item.quantity,
        price: item.price
      }))
    };

    run(() => api.createOrder(payload), "Заказ создан").then((result) => {
      if (result) {
        setOrderDraft(emptyOrderDraft);
        setOrderItems([]);
      }
    });
  }

  function handleItemSubmit(event) {
    event.preventDefault();
    const payload = {
      productId: Number(itemForm.productId),
      quantity: Number(itemForm.quantity),
      price: Number(itemForm.price)
    };

    run(
      () => editingItemId ? api.updateItem(editingItemId, payload) : api.createItem(itemForm.orderId, payload),
      editingItemId ? "Позиция обновлена" : "Позиция добавлена"
    ).then((result) => {
      if (result) {
        setItemForm(emptyItem);
        setEditingItemId(null);
      }
    });
  }

  function editItem(item) {
    setActiveTab("items");
    setEditingItemId(item.id);
    setItemForm({
      orderId: "",
      productId: item.productId ?? "",
      quantity: item.quantity ?? 1,
      price: item.price ?? ""
    });
  }

  function chooseProductForItem(productId) {
    const product = productById[productId];
    setItemForm((current) => ({ ...current, productId, price: product?.price ?? current.price }));
  }

  function deleteEntity(title, action) {
    if (!window.confirm(`Удалить "${title}"?`)) {
      return;
    }

    run(action, "Запись удалена");
  }

  function toggleFavorite(productId) {
    setFavorites((current) =>
      current.includes(productId)
        ? current.filter((id) => id !== productId)
        : [...current, productId]
    );
  }

  function getCartDraft(productId) {
    return cartDrafts[productId] ?? 1;
  }

  function setCartDraft(productId, value) {
    const quantity = Math.max(1, Number(value) || 1);
    setCartDrafts((current) => ({ ...current, [productId]: quantity }));
  }

  function addToCart(productId) {
    const product = productById[productId];
    const quantity = Number(getCartDraft(productId));
    const quantityInCart = cart.find((item) => item.productId === productId)?.quantity ?? 0;

    if (isAdmin) {
      setError("Админ управляет товарами. Для покупки выберите покупателя");
      return;
    }

    if (!selectedUserId) {
      setIsUserMenuOpen(true);
      setError("Сначала выберите покупателя, чтобы корзина сохранилась за ним");
      return;
    }

    if (!Number.isInteger(quantity) || quantity < 1) {
      setError("Количество должно быть целым числом больше нуля");
      return;
    }

    if (product?.quantity != null && quantity + quantityInCart > product.quantity) {
      setError(`На складе только ${product.quantity} шт.`);
      return;
    }

    run(
      () => api.addCartItem(selectedUserId, { productId, quantity }),
      `Добавлено в корзину: ${quantity} шт.`,
      { reload: false }
    ).then((result) => {
      if (result) {
        setCart(normalizeCartItems(result));
        setIsCartOpen(true);
      }
    });
  }

  async function updateCartQuantity(productId, value) {
    const quantity = Math.max(1, Number(value) || 1);
    const product = productById[productId];

    if (!selectedUserId || isAdmin) {
      return;
    }

    if (product?.quantity != null && quantity > product.quantity) {
      setError(`На складе только ${product.quantity} шт.`);
      return;
    }

    setCart((current) => current.map((item) => (
      item.productId === productId ? { ...item, quantity } : item
    )));

    setBusy(true);
    setError("");

    try {
      const result = await api.updateCartItem(selectedUserId, productId, { productId, quantity });
      setCart(normalizeCartItems(result));
    } catch (requestError) {
      setError(requestError.message);
      await loadCartForUser(selectedUserId);
    } finally {
      setBusy(false);
    }
  }

  function removeFromCart(productId) {
    if (!selectedUserId || isAdmin) {
      return;
    }

    run(
      () => api.deleteCartItem(selectedUserId, productId),
      "Товар удалён из корзины",
      { reload: false }
    ).then((result) => {
      if (result) {
        setCart(normalizeCartItems(result));
      }
    });
  }

  function clearCart() {
    if (!selectedUserId || isAdmin) {
      setCart([]);
      return;
    }

    run(
      () => api.clearCart(selectedUserId),
      "Корзина очищена",
      { reload: false }
    ).then((result) => {
      if (result) {
        setCart(normalizeCartItems(result));
      }
    });
  }

  function checkoutCart() {
    if (isAdmin || !selectedUserId) {
      setError("Для оплаты выберите покупателя");
      return;
    }

    if (cartItems.length === 0) {
      setError("Корзина пустая");
      return;
    }

    const payload = {
      userId: Number(selectedUserId),
      items: cartItems.map((item) => ({
        productId: item.productId,
        quantity: item.quantity,
        price: Number(item.product.price)
      }))
    };

    run(
      async () => {
        const order = await api.createOrder(payload);
        const clearedCart = await api.clearCart(selectedUserId);
        setCart(normalizeCartItems(clearedCart));
        setIsCartOpen(false);
        return order;
      },
      "Оплата прошла"
    );
  }

  function updateOrderStatus(orderId, status) {
    run(() => api.updateOrderStatus(orderId, status), "Статус заказа обновлён");
  }

  function handleProductSearch(event) {
    event.preventDefault();

    if (!productSearch.userId || !productSearch.categoryName) {
      setError("Для серверной фильтрации выберите пользователя и категорию");
      return;
    }

    run(
      async () => {
        const results = await api.searchProducts(productSearch);
        setProductSearch((current) => ({ ...current, results }));
        return results;
      },
      "Серверная фильтрация выполнена",
      { reload: false }
    );
  }

  const filteredCategories = categories.filter((category) => matchesQuery(category, query));
  const filteredUsers = users.filter((user) => matchesQuery(user, query));
  const filteredOrders = orders.filter((order) => matchesQuery(order, query));
  const filteredItems = items.filter((item) => matchesQuery(item, query));

  return (
    <div className="app-shell">
      <header className="market-header">
        <div className="market-topbar">
          <button className="brand-button" type="button" onClick={() => selectCategory("all")}>Astore</button>
          <div className="user-switcher">
            <button
              className="user-icon-button"
              type="button"
              onClick={() => setIsUserMenuOpen((current) => !current)}
              aria-label="Выбрать клиента"
            >
              <span className="user-avatar">👤</span>
              <span>{selectedUserLabel}</span>
            </button>

            {isUserMenuOpen && (
              <div className="user-menu">
                <strong>{isAdmin ? "Режим администратора" : "Покупатель"}</strong>
                <label>
                  Режим работы
                  <select
                    value={selectedUserId}
                    onChange={(event) => setSelectedUserId(event.target.value)}
                  >
                    <option value={ADMIN_USER_ID}>Админ</option>
                    {users.map((user) => (
                      <option key={user.id} value={user.id}>
                        {user.firstName} {user.lastName} · {user.email}
                      </option>
                    ))}
                  </select>
                </label>
                <form className="quick-user-form" onSubmit={handleQuickUserSubmit}>
                  <span>Создать нового</span>
                  <input
                    required
                    type="email"
                    placeholder="email@example.com"
                    value={quickUserForm.email}
                    onChange={(event) => setQuickUserForm({ ...quickUserForm, email: event.target.value })}
                  />
                  <div className="split">
                    <input
                      required
                      placeholder="Имя"
                      value={quickUserForm.firstName}
                      onChange={(event) => setQuickUserForm({ ...quickUserForm, firstName: event.target.value })}
                    />
                    <input
                      required
                      placeholder="Фамилия"
                      value={quickUserForm.lastName}
                      onChange={(event) => setQuickUserForm({ ...quickUserForm, lastName: event.target.value })}
                    />
                  </div>
                  <button type="submit" disabled={busy}>Создать и выбрать</button>
                </form>
              </div>
            )}
          </div>
          <button className="catalog-button" type="button" onClick={() => setActiveTab("products")}>Каталог</button>
          <label className="search-box">
            <span>Поиск</span>
            <input
              aria-label="Поиск по маркетплейсу"
              placeholder="Например: кроссовки, книги, полотенца..."
              value={query}
              onChange={(event) => setQuery(event.target.value)}
              onFocus={() => setIsSearchFocused(true)}
              onBlur={() => window.setTimeout(() => setIsSearchFocused(false), 120)}
            />
            {visibleSearchSuggestions.length > 0 && (
              <div className="search-suggestions">
                {visibleSearchSuggestions.map((suggestion) => (
                  <button
                    key={suggestion}
                    type="button"
                    onMouseDown={(event) => event.preventDefault()}
                    onClick={() => {
                      setQuery(suggestion);
                      setIsSearchFocused(false);
                    }}
                  >
                    {suggestion}
                  </button>
                ))}
              </div>
            )}
          </label>
          <button className="header-action" type="button">Избранное · {favorites.length}</button>
          {isAdmin ? (
            <button className="header-action strong" type="button" onClick={() => setActiveTab("products")}>
              Админ-панель
            </button>
          ) : (
            <button className="header-action strong" type="button" onClick={() => setIsCartOpen((current) => !current)}>
              Корзина · {cartQuantity}
            </button>
          )}
        </div>

        <div className="market-hero">
          <div>
            <p className="eyebrow">Astore Market</p>
            <h1>Находи своё. Покупай проще.</h1>
            <p>
              Маркетплейс для повседневных находок: техника, книги, дом, уход, одежда и обувь в одном аккуратном каталоге.
            </p>
          </div>
          <div className="cart-preview">
            <span>{isAdmin ? "Режим" : "В корзине"}</span>
            <strong>{isAdmin ? "Admin" : formatMoney(cartTotal)}</strong>
            <small>
              {isAdmin
                ? "создание, изменение и удаление товаров"
                : `${cartQuantity} шт. · клиент: ${selectedUser ? selectedUser.firstName : "не выбран"}`}
            </small>
          </div>
        </div>

        <div className="category-strip" aria-label="Фильтр по категориям">
          <button
            className={selectedCategoryId === "all" ? "category-pill active" : "category-pill"}
            type="button"
            onClick={() => selectCategory("all")}
          >
            Все товары
          </button>
          {categories.map((category) => (
            <button
              key={category.id}
              className={String(selectedCategoryId) === String(category.id) ? "category-pill active" : "category-pill"}
              type="button"
              onClick={() => selectCategory(category.id)}
            >
              {category.name}
            </button>
          ))}
        </div>

        <div className="market-showcase">
          <article className="promo-card main-promo">
            <div>
              <p className="eyebrow">{selectedCategory ? selectedCategory.name : "Все категории"}</p>
              <h2>{selectedCategory ? "Товары выбранной категории" : "Популярное сегодня"}</h2>
              <p>
                {selectedCategory
                  ? selectedCategory.description
                  : "Переключай категории без ввода названия: это отдельный режим просмотра каталога."}
              </p>
              <p className="promo-note">Скорее переходите к просмотру: ниже уже открыт каталог с товарами.</p>
            </div>
            <img src={productImage(heroProducts[0])} alt={heroProducts[0]?.name || "Товар"} />
          </article>

          {heroProducts.slice(1, 4).map((product) => (
            <article className="promo-card mini-promo" key={product.id}>
              <img src={productImage(product)} alt={product.name} />
              <h3>{product.name}</h3>
              <p>{formatMoney(product.price)}</p>
            </article>
          ))}
        </div>
      </header>

      <nav className="tabs" aria-label="Разделы приложения">
        {visibleTabs.map((tab) => (
          <button
            key={tab.id}
            className={activeTab === tab.id ? "tab active" : "tab"}
            type="button"
            onClick={() => setActiveTab(tab.id)}
          >
            {tab.label}
          </button>
        ))}
      </nav>

      {error && <div className="status status-error">{error}</div>}

      {isCartOpen && (
        <section className="cart-drawer" aria-label="Корзина покупателя">
          <div className="cart-drawer-head">
            <div>
              <p className="eyebrow">Корзина покупателя</p>
              <h2>{selectedUser ? `${selectedUser.firstName} ${selectedUser.lastName}` : "Клиент не выбран"}</h2>
              <p className="muted">Сохраняется в базе отдельно для выбранного покупателя.</p>
            </div>
            <button className="ghost" type="button" onClick={() => setIsCartOpen(false)}>Закрыть</button>
          </div>

          {cartItems.length === 0 ? (
            <div className="empty-state">Корзина этого покупателя пока пустая.</div>
          ) : (
            <>
              <div className="cart-list">
                {cartItems.map((item) => (
                  <article className="cart-line" key={item.productId}>
                    <img src={productImage(item.product)} alt={item.product.name} />
                    <div>
                      <strong>{item.product.name}</strong>
                      <small>{formatMoney(item.product.price)} за шт.</small>
                    </div>
                    <input
                      min="1"
                      max={item.product.quantity ?? undefined}
                      type="number"
                      value={item.quantity}
                      onChange={(event) => updateCartQuantity(item.productId, event.target.value)}
                      aria-label={`Количество ${item.product.name}`}
                    />
                    <strong>{formatMoney(Number(item.product.price) * item.quantity)}</strong>
                    <button className="danger link-button" type="button" onClick={() => removeFromCart(item.productId)}>
                      Удалить
                    </button>
                  </article>
                ))}
              </div>
              <div className="cart-total-row">
                <span>{cartQuantity} шт.</span>
                <strong>{formatMoney(cartTotal)}</strong>
                <button className="ghost" type="button" onClick={clearCart}>Очистить</button>
                <button className="pay-button" type="button" disabled={busy} onClick={checkoutCart}>Оплатить</button>
              </div>
            </>
          )}
        </section>
      )}

      <section className="workspace">
        <aside className="panel form-panel">{renderForm()}</aside>
        <main className="panel content-panel">
          <div className="toolbar">
            <div>
              <p className="eyebrow">Каталог API</p>
              <h2>{tabs.find((tab) => tab.id === activeTab)?.label}</h2>
              {activeTab === "products" && (
                <p className="muted">
                  {selectedCategory ? `Показана категория: ${selectedCategory.name}` : "Показаны все товары по порядку"}
                </p>
              )}
            </div>
            <div className="toolbar-actions">
              <input
                aria-label="Фильтр по данным"
                placeholder="Товар, категория, email или статус..."
                value={query}
                onChange={(event) => setQuery(event.target.value)}
              />
              <button className="ghost" type="button" disabled={loading || busy} onClick={loadData}>Обновить</button>
            </div>
          </div>

          {loading ? <div className="empty-state">Загружаю данные из API...</div> : renderContent()}
        </main>
      </section>
    </div>
  );

  function renderForm() {
    if (!isAdmin) {
      return (
        <div className="stack buyer-panel">
          <PanelTitle
            kicker="Режим покупателя"
            title={selectedUser ? selectedUserLabel : "Выберите покупателя"}
          />
          <button className="header-action strong" type="button" onClick={() => setIsCartOpen(true)}>
            Открыть корзину · {cartQuantity}
          </button>
        </div>
      );
    }

    if (activeTab === "products") {
      return (
        <form className="stack" onSubmit={handleProductSubmit}>
          <PanelTitle
            kicker="Управление товаром"
            title={editingProductId ? "Редактировать товар" : "Добавить товар"}
            text="Товар можно привязать к одной или нескольким категориям маркетплейса."
          />
          <label>Название<input required value={productForm.name} onChange={(event) => setProductForm({ ...productForm, name: event.target.value })} /></label>
          <label>Описание<textarea value={productForm.description} onChange={(event) => setProductForm({ ...productForm, description: event.target.value })} /></label>
          <div className="split">
            <label>Цена<input required min="0" step="0.01" type="number" value={productForm.price} onChange={(event) => setProductForm({ ...productForm, price: event.target.value })} /></label>
            <label>Количество<input required min="0" type="number" value={productForm.quantity} onChange={(event) => setProductForm({ ...productForm, quantity: event.target.value })} /></label>
          </div>
          <label>
            Изображение
            <select value={productForm.imageUrl} onChange={(event) => setProductForm({ ...productForm, imageUrl: event.target.value })}>
              {imageChoices.map((choice) => <option key={choice.url} value={choice.url}>{choice.label}</option>)}
            </select>
          </label>
          <fieldset>
            <legend>Категории</legend>
            <div className="check-grid">
              {categories.map((category) => (
                <label className="check" key={category.id}>
                  <input type="checkbox" checked={productForm.categories.includes(category.name)} onChange={() => toggleProductCategory(category.name)} />
                  {category.name}
                </label>
              ))}
              {categories.length === 0 && <p className="muted">Сначала создайте категорию.</p>}
            </div>
          </fieldset>
          <FormActions
            busy={busy}
            submitText={editingProductId ? "Сохранить" : "Создать"}
            onCancel={editingProductId ? () => { setProductForm(emptyProduct); setEditingProductId(null); } : null}
          />
        </form>
      );
    }

    if (activeTab === "categories") {
      return (
        <form className="stack" onSubmit={handleCategorySubmit}>
          <PanelTitle
            kicker="CRUD категорий"
            title={editingCategoryId ? "Редактировать категорию" : "Создать категорию"}
            text="Категории используются быстрыми кнопками фильтрации на витрине."
          />
          <label>Название<input required value={categoryForm.name} onChange={(event) => setCategoryForm({ ...categoryForm, name: event.target.value })} /></label>
          <label>Описание<textarea value={categoryForm.description} onChange={(event) => setCategoryForm({ ...categoryForm, description: event.target.value })} /></label>
          <FormActions
            busy={busy}
            submitText={editingCategoryId ? "Сохранить" : "Создать"}
            onCancel={editingCategoryId ? () => { setCategoryForm(emptyCategory); setEditingCategoryId(null); } : null}
          />
        </form>
      );
    }

    if (activeTab === "users") {
      return (
        <form className="stack" onSubmit={handleUserSubmit}>
          <PanelTitle kicker="CRUD пользователей" title={editingUserId ? "Редактировать пользователя" : "Создать пользователя"} text="Пользователь связан с заказами как OneToMany." />
          <label>Email<input required type="email" value={userForm.email} onChange={(event) => setUserForm({ ...userForm, email: event.target.value })} /></label>
          <label>Имя<input required value={userForm.firstName} onChange={(event) => setUserForm({ ...userForm, firstName: event.target.value })} /></label>
          <label>Фамилия<input required value={userForm.lastName} onChange={(event) => setUserForm({ ...userForm, lastName: event.target.value })} /></label>
          <FormActions
            busy={busy}
            submitText={editingUserId ? "Сохранить" : "Создать"}
            onCancel={editingUserId ? () => { setUserForm(emptyUser); setEditingUserId(null); } : null}
          />
        </form>
      );
    }

    if (activeTab === "orders") {
      return (
        <form className="stack" onSubmit={handleOrderSubmit}>
          <PanelTitle kicker="CRUD заказов" title="Создать заказ" text="Заказ создаётся для пользователя и содержит OneToMany-список позиций." />
          <label>
            Пользователь
            <select required value={orderDraft.userId} onChange={(event) => setOrderDraft({ ...orderDraft, userId: event.target.value })}>
              <option value="">Выберите пользователя</option>
              {users.map((user) => <option key={user.id} value={user.id}>{user.firstName} {user.lastName}</option>)}
            </select>
          </label>
          <div className="inline-form">
            <label>
              Товар
              <select value={orderDraft.productId} onChange={(event) => setOrderDraft({ ...orderDraft, productId: event.target.value })}>
                <option value="">Выберите товар</option>
                {products.map((product) => <option key={product.id} value={product.id}>{product.name} · {formatMoney(product.price)}</option>)}
              </select>
            </label>
            <label>Кол-во<input min="1" type="number" value={orderDraft.quantity} onChange={(event) => setOrderDraft({ ...orderDraft, quantity: event.target.value })} /></label>
            <button className="ghost add-line" type="button" onClick={addOrderItem}>Добавить</button>
          </div>
          <div className="draft-box">
            {orderItems.length === 0
              ? <p className="muted">Позиции заказа пока не добавлены.</p>
              : orderItems.map((item, index) => <span className="chip strong" key={`${item.productId}-${index}`}>{item.productName} × {item.quantity}</span>)}
          </div>
          <FormActions busy={busy} submitText="Создать заказ" />
        </form>
      );
    }

    if (activeTab === "items") {
      return (
        <form className="stack" onSubmit={handleItemSubmit}>
          <PanelTitle kicker="CRUD позиций" title={editingItemId ? "Редактировать позицию" : "Добавить позицию"} text="Позиция принадлежит заказу и ссылается на товар." />
          {!editingItemId && (
            <label>
              Заказ
              <select required value={itemForm.orderId} onChange={(event) => setItemForm({ ...itemForm, orderId: event.target.value })}>
                <option value="">Выберите заказ</option>
                {orders.map((order) => (
                  <option key={order.id} value={order.id}>
                    {orderLabel(order, userById)} · {formatMoney(order.totalAmount)}
                  </option>
                ))}
              </select>
            </label>
          )}
          <label>
            Товар
            <select required value={itemForm.productId} onChange={(event) => chooseProductForItem(event.target.value)}>
              <option value="">Выберите товар</option>
              {products.map((product) => <option key={product.id} value={product.id}>{product.name}</option>)}
            </select>
          </label>
          <div className="split">
            <label>Количество<input required min="1" type="number" value={itemForm.quantity} onChange={(event) => setItemForm({ ...itemForm, quantity: event.target.value })} /></label>
            <label>Цена<input required min="0" step="0.01" type="number" value={itemForm.price} onChange={(event) => setItemForm({ ...itemForm, price: event.target.value })} /></label>
          </div>
          <FormActions
            busy={busy}
            submitText={editingItemId ? "Сохранить" : "Добавить"}
            onCancel={editingItemId ? () => { setItemForm(emptyItem); setEditingItemId(null); } : null}
          />
        </form>
      );
    }

    return null;
  }

  function renderContent() {
    if (activeTab === "products") {
      return (
        <div className="stack">
          <div className="market-tools">
            <strong>{selectedCategory ? selectedCategory.name : "Все товары"}</strong>
            <span>{visibleProducts.length} позиций</span>
            <button className="ghost" type="button" onClick={() => selectCategory("all")}>Сбросить категорию</button>
          </div>

          <form className="filter-card" onSubmit={handleProductSearch}>
            <div>
              <strong>Серверная фильтрация из лабораторной</strong>
              <p>Поиск товаров, которые покупал пользователь в выбранной категории.</p>
            </div>
            <select value={productSearch.userId} onChange={(event) => setProductSearch({ ...productSearch, userId: event.target.value })}>
              <option value="">Пользователь</option>
              {users.map((user) => <option key={user.id} value={user.id}>{user.firstName} {user.lastName} · {user.email}</option>)}
            </select>
            <select value={productSearch.categoryName} onChange={(event) => setProductSearch({ ...productSearch, categoryName: event.target.value })}>
              <option value="">Категория</option>
              {categories.map((category) => <option key={category.id} value={category.name}>{category.name}</option>)}
            </select>
            <button type="submit" disabled={busy}>Найти</button>
          </form>

          {productSearch.results.length > 0 && (
            <div className="result-strip">
              <strong>Результат:</strong>
              {productSearch.results.map((product) => <span className="chip strong" key={product.id}>{product.name}</span>)}
            </div>
          )}

          <div className="product-grid">
            {visibleProducts.map((product) => (
              <article className="market-card" key={product.id}>
                <button
                  className={favoriteSet.has(product.id) ? "favorite active" : "favorite"}
                  type="button"
                  onClick={() => toggleFavorite(product.id)}
                  aria-label="Добавить в избранное"
                >
                  ♥
                </button>
                <figure>
                  <img src={productImage(product)} alt={product.name} />
                </figure>
                <div className="rating-row">
                  <span>★ 4.{(Number(product.id) % 8) + 1}</span>
                  <span>остаток: {product.quantity}</span>
                </div>
                <h3>{product.name}</h3>
                <p>{product.description || "Описание не указано"}</p>
                <div className="chips">
                  {(product.categories ?? []).map((category) => <span className="chip" key={category}>{category}</span>)}
                </div>
                <div className="market-card-footer">
                  <strong>{formatMoney(product.price)}</strong>
                  {!isAdmin && (
                    <div className="cart-add-controls">
                      <button type="button" onClick={() => setCartDraft(product.id, getCartDraft(product.id) - 1)}>-</button>
                      <input
                        min="1"
                        max={product.quantity ?? undefined}
                        type="number"
                        value={getCartDraft(product.id)}
                        onChange={(event) => setCartDraft(product.id, event.target.value)}
                        aria-label={`Количество ${product.name}`}
                      />
                      <button type="button" onClick={() => setCartDraft(product.id, getCartDraft(product.id) + 1)}>+</button>
                      <button className="add-cart-button" type="button" onClick={() => addToCart(product.id)}>
                        В корзину
                      </button>
                    </div>
                  )}
                </div>
                {isAdmin && (
                  <div className="admin-row">
                    <button className="link-button" type="button" onClick={() => editProduct(product)}>Изменить</button>
                    <button className="danger link-button" type="button" onClick={() => deleteEntity(product.name, () => api.deleteProduct(product.id))}>Удалить</button>
                  </div>
                )}
              </article>
            ))}
          </div>
          {visibleProducts.length === 0 && <EmptyState />}
        </div>
      );
    }

    if (activeTab === "categories") {
      return (
        <div className="card-grid">
          {filteredCategories.map((category) => {
            const linkedProducts = products.filter((product) => product.categories?.includes(category.name));
            return (
              <article className="entity-card" key={category.id}>
                <h3>{category.name}</h3>
                <p>{category.description || "Описание не указано"}</p>
                <div className="chips">
                  {linkedProducts.map((product) => <span className="chip" key={product.id}>{product.name}</span>)}
                  {linkedProducts.length === 0 && <span className="chip muted-chip">Товаров пока нет</span>}
                </div>
                <div className="card-footer">
                  <button className="ghost" type="button" onClick={() => selectCategory(category.id)}>Открыть</button>
                  <button className="link-button" type="button" onClick={() => editCategory(category)}>Изменить</button>
                  <button className="danger link-button" type="button" onClick={() => deleteEntity(category.name, () => api.deleteCategory(category.id))}>Удалить</button>
                </div>
              </article>
            );
          })}
          {filteredCategories.length === 0 && <EmptyState />}
        </div>
      );
    }

    if (activeTab === "users") {
      return (
        <div className="card-grid">
          {filteredUsers.map((user) => {
            const userOrders = orders.filter((order) => order.userId === user.id);
            return (
              <article className="entity-card" key={user.id}>
                <h3>{user.firstName} {user.lastName}</h3>
                <p>{user.email}</p>
                <div className="relation-line">User → {userOrders.length} orders</div>
                <div className="card-footer">
                  <button className="link-button" type="button" onClick={() => editUser(user)}>Изменить</button>
                  <button className="danger link-button" type="button" onClick={() => deleteEntity(user.email, () => api.deleteUser(user.id))}>Удалить</button>
                </div>
              </article>
            );
          })}
          {filteredUsers.length === 0 && <EmptyState />}
        </div>
      );
    }

    if (activeTab === "orders") {
      return (
        <div className="order-list">
          {filteredOrders.map((order) => {
            const user = userById[order.userId];
            return (
              <article className="entity-card wide-card" key={order.id}>
                <div className="order-head">
                  <div>
                    <p className="eyebrow">Заказ покупателя</p>
                    <h3>{user ? `${user.firstName} ${user.lastName}` : "Покупатель удалён"}</h3>
                    <p>{formatDate(order.orderDate)}</p>
                  </div>
                  <div className="order-actions">
                    <select value={order.status} onChange={(event) => updateOrderStatus(order.id, event.target.value)}>
                      {statuses.map((status) => <option key={status} value={status}>{status}</option>)}
                    </select>
                    <button className="danger link-button" type="button" onClick={() => deleteEntity("заказ", () => api.deleteOrder(order.id))}>Удалить</button>
                  </div>
                </div>
                <div className="item-list">
                  {(order.items ?? []).map((item) => (
                    <div className="item-row" key={item.id}>
                      <span>{item.productName || productById[item.productId]?.name || "Товар удалён"}</span>
                      <strong>{item.quantity} × {formatMoney(item.price)}</strong>
                    </div>
                  ))}
                </div>
                <div className="card-footer">
                  <span>OneToMany: order → {(order.items ?? []).length} items</span>
                  <strong>{formatMoney(order.totalAmount)}</strong>
                </div>
              </article>
            );
          })}
          {filteredOrders.length === 0 && <EmptyState />}
        </div>
      );
    }

    if (activeTab === "items") {
      return (
        <div className="card-grid">
          {filteredItems.map((item) => (
            <article className="entity-card" key={item.id}>
              <h3>{item.productName || productById[item.productId]?.name || "Товар удалён"}</h3>
              <p>{item.quantity} шт. в позиции заказа</p>
              <div className="card-footer">
                <span>{item.quantity} × {formatMoney(item.price)}</span>
                <button className="link-button" type="button" onClick={() => editItem(item)}>Изменить</button>
                <button className="danger link-button" type="button" onClick={() => deleteEntity("позицию заказа", () => api.deleteItem(item.id))}>Удалить</button>
              </div>
            </article>
          ))}
          {filteredItems.length === 0 && <EmptyState />}
        </div>
      );
    }

    return <EmptyState />;
  }
}

function PanelTitle({ kicker, title, text }) {
  return (
    <div>
      <p className="eyebrow">{kicker}</p>
      <h2>{title}</h2>
      <p className="muted">{text}</p>
    </div>
  );
}

function FormActions({ busy, submitText, onCancel }) {
  return (
    <div className="form-actions">
      <button type="submit" disabled={busy}>{submitText}</button>
      {onCancel && <button className="ghost" type="button" onClick={onCancel}>Отменить</button>}
    </div>
  );
}

function EmptyState() {
  return <div className="empty-state">Ничего не найдено. Можно сбросить фильтр или добавить новую запись.</div>;
}

function unique(values) {
  return Array.from(new Set(values
    .map((value) => String(value ?? "").trim())
    .filter(Boolean)));
}

function orderLabel(order, userById) {
  const user = userById[order.userId];
  return user ? `Заказ: ${user.firstName} ${user.lastName}` : "Заказ покупателя";
}

function normalizeCartItems(cartDto) {
  return (cartDto?.items ?? [])
    .map((item) => ({
      ...item,
      productId: Number(item.productId),
      quantity: Number(item.quantity)
    }))
    .filter((item) => item.productId && Number.isInteger(item.quantity) && item.quantity > 0);
}

function matchesQuery(entity, query) {
  if (!query.trim()) {
    return true;
  }

  return JSON.stringify(entity).toLowerCase().includes(query.trim().toLowerCase());
}

function productImage(product) {
  return product?.imageUrl || imageChoices[0].url;
}

function formatMoney(value) {
  return new Intl.NumberFormat("ru-RU", {
    style: "currency",
    currency: "BYN",
    maximumFractionDigits: 2
  }).format(Number(value ?? 0));
}

function formatDate(value) {
  if (!value) {
    return "Дата не указана";
  }

  return new Intl.DateTimeFormat("ru-RU", {
    dateStyle: "medium",
    timeStyle: "short"
  }).format(new Date(value));
}

export default App;
