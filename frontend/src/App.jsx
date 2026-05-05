import { useEffect, useRef, useState } from "react";
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
const emptyPriceFilter = { min: "", max: "" };
const ADMIN_USER_ID = "admin";
const PRODUCTS_PER_PAGE = 12;

function App() {
  const [activeTab, setActiveTab] = useState("products");
  const [data, setData] = useState({ products: [], categories: [], users: [], orders: [], items: [] });
  const [catalogProducts, setCatalogProducts] = useState([]);
  const [catalogPage, setCatalogPage] = useState({ totalPages: 1, totalElements: 0, number: 0 });
  const [selectedCategoryId, setSelectedCategoryId] = useState("all");
  const [query, setQuery] = useState("");
  const [isSearchFocused, setIsSearchFocused] = useState(false);
  const [productPage, setProductPage] = useState(1);
  const [cart, setCart] = useState([]);
  const [cartDrafts, setCartDrafts] = useState({});
  const [activeCartProductId, setActiveCartProductId] = useState(null);
  const [isCartOpen, setIsCartOpen] = useState(false);
  const [cartMode, setCartMode] = useState("full");
  const [priceFilter, setPriceFilter] = useState(emptyPriceFilter);
  const [selectedUserId, setSelectedUserId] = useState(ADMIN_USER_ID);
  const [isUserMenuOpen, setIsUserMenuOpen] = useState(false);
  const [quickUserForm, setQuickUserForm] = useState(emptyQuickUser);
  const [loading, setLoading] = useState(true);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState("");
  const [, setNotice] = useState("");
  const cartRef = useRef(null);
  const userMenuRef = useRef(null);

  const [productForm, setProductForm] = useState(emptyProduct);
  const [categoryForm, setCategoryForm] = useState(emptyCategory);
  const [userForm, setUserForm] = useState(emptyUser);
  const [itemForm, setItemForm] = useState(emptyItem);
  const [orderDraft, setOrderDraft] = useState(emptyOrderDraft);
  const [orderItems, setOrderItems] = useState([]);

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
  const visibleProducts = catalogProducts;
  const totalProductPages = Math.max(1, Number(catalogPage.totalPages) || 1);
  const currentProductPage = Math.min(productPage, totalProductPages);
  const productPageStart = (currentProductPage - 1) * PRODUCTS_PER_PAGE;
  const paginatedProducts = visibleProducts;
  const productPageNumbers = Array.from({ length: totalProductPages }, (_, index) => index + 1);
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
    setProductPage(1);
  }, [query, selectedCategoryId, priceFilter.min, priceFilter.max]);

  useEffect(() => {
    if (activeTab !== "products") {
      return;
    }

    if (productPage === 1) {
      loadCatalog(1);
    }
  }, [activeTab, query, selectedCategoryId, priceFilter.min, priceFilter.max]);

  useEffect(() => {
    if (activeTab === "products" && productPage !== 1) {
      loadCatalog(productPage);
    }
  }, [activeTab, productPage]);

  useEffect(() => {
    if (isAdmin && activeTab !== "products") {
      loadAdminSection(activeTab);
    }
  }, [activeTab, query, isAdmin]);

  useEffect(() => {
    setProductPage((current) => Math.min(current, totalProductPages));
  }, [totalProductPages]);

  useEffect(() => {
    setQuery("");
    setIsSearchFocused(false);
  }, [activeTab]);

  useEffect(() => {
    loadCartForUser(selectedUserId);
  }, [selectedUserId]);

  useEffect(() => {
    function handleOutsideClick(event) {
      if (isCartOpen && cartRef.current && !cartRef.current.contains(event.target)) {
        setIsCartOpen(false);
      }

      if (isUserMenuOpen && userMenuRef.current && !userMenuRef.current.contains(event.target)) {
        setIsUserMenuOpen(false);
      }
    }

    document.addEventListener("mousedown", handleOutsideClick);
    return () => document.removeEventListener("mousedown", handleOutsideClick);
  }, [isCartOpen, isUserMenuOpen]);

  async function loadCatalog(pageNumber = productPage) {
    setLoading(true);
    setError("");

    try {
      const nextCatalogPage = await api.listCatalogProducts({
        page: Math.max(0, pageNumber - 1),
        size: PRODUCTS_PER_PAGE,
        query: activeTab === "products" ? query.trim() : "",
        categoryId: selectedCategoryId === "all" ? "" : selectedCategoryId,
        minPrice: priceFilter.min,
        maxPrice: priceFilter.max
      });

      setCatalogProducts(nextCatalogPage?.content ?? []);
      setCatalogPage({
        totalPages: nextCatalogPage?.totalPages ?? 1,
        totalElements: nextCatalogPage?.totalElements ?? 0,
        number: nextCatalogPage?.number ?? Math.max(0, pageNumber - 1)
      });
    } catch (requestError) {
      setCatalogProducts([]);
      setCatalogPage({ totalPages: 1, totalElements: 0, number: 0 });
      setError(requestError.message);
    } finally {
      setLoading(false);
    }
  }

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
      const nextCatalogPage = await api.listCatalogProducts({
        page: Math.max(0, productPage - 1),
        size: PRODUCTS_PER_PAGE,
        query: activeTab === "products" ? query.trim() : "",
        categoryId: selectedCategoryId === "all" ? "" : selectedCategoryId,
        minPrice: priceFilter.min,
        maxPrice: priceFilter.max
      });

      setData({
        products: nextProducts,
        categories: nextCategories,
        users: nextUsers,
        orders: nextOrders,
        items: nextItems
      });
      setSelectedUserId((current) => current || nextUsers[0]?.id || "");
      setCatalogProducts(nextCatalogPage?.content ?? []);
      setCatalogPage({
        totalPages: nextCatalogPage?.totalPages ?? 1,
        totalElements: nextCatalogPage?.totalElements ?? 0,
        number: nextCatalogPage?.number ?? Math.max(0, productPage - 1)
      });
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

  async function loadAdminSection(tabId) {
    setError("");

    try {
      if (tabId === "categories") {
        const nextCategories = await api.listCategories(query.trim());
        setData((current) => ({ ...current, categories: nextCategories }));
      }

      if (tabId === "users") {
        const nextUsers = await api.listUsers(query.trim());
        setData((current) => ({ ...current, users: nextUsers }));
      }

      if (tabId === "orders") {
        const nextOrders = await api.listOrders(query.trim());
        setData((current) => ({ ...current, orders: nextOrders }));
      }

      if (tabId === "items") {
        const nextItems = await api.listItems(query.trim());
        setData((current) => ({ ...current, items: nextItems }));
      }
    } catch (requestError) {
      setError(requestError.message);
    }
  }

  async function openUserMenu() {
    const shouldOpen = !isUserMenuOpen;
    setIsUserMenuOpen(shouldOpen);

    if (!shouldOpen) {
      return;
    }

    try {
      const nextUsers = await api.listUsers();
      setData((current) => ({ ...current, users: nextUsers }));
    } catch (requestError) {
      setError(requestError.message);
    }
  }

  function selectCategory(categoryId) {
    setProductPage(1);
    setSelectedCategoryId(categoryId);
    setActiveTab("products");
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

  function getCartDraft(productId) {
    return cartDrafts[productId] ?? 1;
  }

  function setCartDraft(productId, value) {
    const quantity = Math.max(1, Number(value) || 1);
    setCartDrafts((current) => ({ ...current, [productId]: quantity }));
  }

  function setPriceFilterValue(field, value) {
    if (value.startsWith("-") || Number(value) < 0) {
      return;
    }

    setPriceFilter((current) => ({ ...current, [field]: value }));
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
        setActiveCartProductId(null);
        setCartMode("compact");
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

  async function openCompactCart() {
    if (cartMode === "compact" && isCartOpen) {
      setIsCartOpen(false);
      return;
    }

    setCartMode("compact");
    await loadCartForUser(selectedUserId);
    setIsCartOpen(true);
  }

  function resetProductFilters() {
    setPriceFilter(emptyPriceFilter);
    selectCategory("all");
  }

  const filteredCategories = categories;
  const filteredUsers = users;
  const filteredOrders = orders;
  const filteredItems = items;

  return (
    <div className="app-shell">
      <header className="market-header">
        <div className="market-topbar">
          <button className="brand-button" type="button" onClick={() => selectCategory("all")}>Astore</button>
          <div className="user-switcher" ref={userMenuRef}>
            <button
              className="user-icon-button"
              type="button"
              onClick={openUserMenu}
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
          {activeTab === "products" && (
            <>
              <select
                className="category-inline-select"
                value={selectedCategoryId}
                onChange={(event) => selectCategory(event.target.value)}
                aria-label="Категории"
              >
                <option value="all">Категории</option>
                {categories.map((category) => (
                  <option key={category.id} value={category.id}>{category.name}</option>
                ))}
              </select>
              <input
                className="price-inline-input"
                min="0"
                step="0.01"
                type="number"
                placeholder="Цена от"
                value={priceFilter.min}
                onChange={(event) => setPriceFilterValue("min", event.target.value)}
              />
              <input
                className="price-inline-input"
                min="0"
                step="0.01"
                type="number"
                placeholder="Цена до"
                value={priceFilter.max}
                onChange={(event) => setPriceFilterValue("max", event.target.value)}
              />
              <button className="ghost reset-inline-button" type="button" onClick={resetProductFilters}>Сбросить</button>
            </>
          )}
          {!isAdmin && (
            <button className="header-action strong" type="button" onClick={openCompactCart}>
              Корзина · {cartQuantity}
            </button>
          )}
        </div>
      </header>

      {isAdmin && (
        <nav className="tabs" aria-label="Разделы приложения">
          {visibleTabs.map((tab) => (
            <button
              key={tab.id}
              className={activeTab === tab.id ? "tab active" : "tab"}
              type="button"
              onClick={() => {
                setActiveTab(tab.id);
                loadAdminSection(tab.id);
              }}
            >
              {tab.label}
            </button>
          ))}
        </nav>
      )}

      {error && <div className="status status-error">{error}</div>}

      {isCartOpen && renderCartDrawer()}

      <section className={isAdmin ? "workspace" : "workspace buyer-workspace"}>
        {isAdmin && <aside className="panel form-panel">{renderForm()}</aside>}
        <main className="panel content-panel">
          <div className="toolbar">
            <div>
              <h2>{tabs.find((tab) => tab.id === activeTab)?.label}</h2>
              {activeTab === "products" && (
                <p className="muted">
                  {catalogPage.totalElements === 0
                    ? "Ничего не найдено"
                    : `${selectedCategory ? selectedCategory.name : "Все товары"} · ${productPageStart + 1}-${Math.min(productPageStart + PRODUCTS_PER_PAGE, catalogPage.totalElements)} из ${catalogPage.totalElements}`}
                </p>
              )}
            </div>
          </div>

          {loading ? <div className="empty-state">Загружаю данные из API...</div> : renderContent()}
        </main>
      </section>
    </div>
  );

  function renderCartDrawer() {
    return (
      <section
        className={cartMode === "compact" ? "cart-drawer cart-drawer-compact" : "cart-drawer"}
        ref={cartRef}
        aria-label="Корзина покупателя"
      >
        <div className="cart-drawer-head">
          <div>
            <h2>{selectedUser ? `${selectedUser.firstName} ${selectedUser.lastName}` : "Клиент не выбран"}</h2>
          </div>
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
    );
  }

  function renderForm() {
    if (!isAdmin) {
      return null;
    }

    if (activeTab === "products") {
      return (
        <form className="stack" onSubmit={handleProductSubmit}>
          <PanelTitle title={editingProductId ? "Редактировать товар" : "Добавить товар"} />
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
          <PanelTitle title={editingCategoryId ? "Редактировать категорию" : "Создать категорию"} />
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
          <PanelTitle title={editingUserId ? "Редактировать пользователя" : "Создать пользователя"} />
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
          <PanelTitle title="Создать заказ" />
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
          <PanelTitle title={editingItemId ? "Редактировать позицию" : "Добавить позицию"} />
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
          <div className="product-grid">
            {paginatedProducts.map((product) => (
              <article className="market-card" key={product.id}>
                <figure>
                  <img src={productImage(product)} alt={product.name} />
                </figure>
                <div className="market-card-body">
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
                      activeCartProductId === product.id ? (
                        <div className="cart-add-controls expanded">
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
                            Добавить {getCartDraft(product.id)} шт.
                          </button>
                        </div>
                      ) : (
                        <button className="add-cart-button" type="button" onClick={() => setActiveCartProductId(product.id)}>
                          В корзину
                        </button>
                      )
                    )}
                  </div>
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
          {catalogPage.totalElements === 0 ? (
            <EmptyState />
          ) : (
            <div className="pagination-bar" aria-label="Пагинация товаров">
              <button
                className="ghost"
                type="button"
                disabled={currentProductPage === 1}
                onClick={() => setProductPage((current) => Math.max(1, current - 1))}
              >
                Назад
              </button>
              <div className="page-buttons">
                {productPageNumbers.map((page) => (
                  <button
                    key={page}
                    className={page === currentProductPage ? "page-button active" : "page-button"}
                    type="button"
                    onClick={() => setProductPage(page)}
                    aria-label={`Страница ${page}`}
                    aria-current={page === currentProductPage ? "page" : undefined}
                  >
                    {page}
                  </button>
                ))}
              </div>
              <button
                className="ghost"
                type="button"
                disabled={currentProductPage === totalProductPages}
                onClick={() => setProductPage((current) => Math.min(totalProductPages, current + 1))}
              >
                Вперёд
              </button>
            </div>
          )}
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
                <div className="relation-line">Заказов: {userOrders.length}</div>
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

function PanelTitle({ title, text }) {
  return (
    <div>
      <h2>{title}</h2>
      {text && <p className="muted">{text}</p>}
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
