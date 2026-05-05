const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? "";

async function request(path, options = {}) {
  const method = options.method ?? "GET";
  const url = `${API_BASE_URL}${path}`;
  const headers = {
    ...(options.body ? { "Content-Type": "application/json" } : {}),
    ...options.headers
  };

  console.info(`[Astore API] ${method} ${url}`);

  const response = await fetch(url, {
    ...options,
    cache: "no-store",
    headers
  });

  console.info(`[Astore API] ${response.status} ${method} ${url}`);

  if (response.status === 204) {
    return null;
  }

  const text = await response.text();
  const data = text ? JSON.parse(text) : null;

  if (!response.ok) {
    throw new Error(data?.message || data?.error || `HTTP ${response.status}`);
  }

  return data;
}

function page(path, size = 100) {
  const separator = path.includes("?") ? "&" : "?";
  return request(`${path}${separator}page=0&size=${size}`).then((data) => data?.content ?? []);
}

function pageRequest(path, params = {}) {
  const searchParams = new URLSearchParams();

  Object.entries(params).forEach(([key, value]) => {
    if (value !== undefined && value !== null && value !== "") {
      searchParams.set(key, value);
    }
  });

  const query = searchParams.toString();
  return request(query ? `${path}?${query}` : path);
}

export const api = {
  listProducts: () => page("/api/products"),
  listCatalogProducts: (params) => pageRequest("/api/products/catalog", params),
  listProductsByCategory: (categoryId) => page(`/api/products/category/${categoryId}`),
  createProduct: (payload) => request("/api/products", { method: "POST", body: JSON.stringify(payload) }),
  updateProduct: (id, payload) => request(`/api/products/${id}`, { method: "PUT", body: JSON.stringify(payload) }),
  deleteProduct: (id) => request(`/api/products/${id}`, { method: "DELETE" }),
  searchProducts: ({ userId, categoryName }) =>
    page(`/api/products/search?userId=${encodeURIComponent(userId)}&categoryName=${encodeURIComponent(categoryName)}`),

  listCategories: (query = "") => pageRequest("/api/categories", { page: 0, size: 100, query }).then((data) => data?.content ?? []),
  createCategory: (payload) => request("/api/categories", { method: "POST", body: JSON.stringify(payload) }),
  updateCategory: (id, payload) => request(`/api/categories/${id}`, { method: "PUT", body: JSON.stringify(payload) }),
  deleteCategory: (id) => request(`/api/categories/${id}`, { method: "DELETE" }),

  listUsers: (query = "") => pageRequest("/api/users", { page: 0, size: 100, query }).then((data) => data?.content ?? []),
  createUser: (payload) => request("/api/users", { method: "POST", body: JSON.stringify(payload) }),
  updateUser: (id, payload) => request(`/api/users/${id}`, { method: "PUT", body: JSON.stringify(payload) }),
  deleteUser: (id) => request(`/api/users/${id}`, { method: "DELETE" }),

  listOrders: (query = "") => pageRequest("/api/orders", { page: 0, size: 100, query }).then((data) => data?.content ?? []),
  createOrder: (payload) => request("/api/orders", { method: "POST", body: JSON.stringify(payload) }),
  updateOrderStatus: (id, status) =>
    request(`/api/orders/${id}/status?status=${encodeURIComponent(status)}`, { method: "PATCH" }),
  deleteOrder: (id) => request(`/api/orders/${id}`, { method: "DELETE" }),

  listItems: (query = "") => pageRequest("/api/items", { page: 0, size: 100, query }).then((data) => data?.content ?? []),
  createItem: (orderId, payload) =>
    request(`/api/items/order/${orderId}`, { method: "POST", body: JSON.stringify(payload) }),
  updateItem: (id, payload) => request(`/api/items/${id}`, { method: "PUT", body: JSON.stringify(payload) }),
  deleteItem: (id) => request(`/api/items/${id}`, { method: "DELETE" }),

  getCart: (userId) => request(`/api/carts/user/${userId}`),
  addCartItem: (userId, payload) =>
    request(`/api/carts/user/${userId}/items`, { method: "POST", body: JSON.stringify(payload) }),
  updateCartItem: (userId, productId, payload) =>
    request(`/api/carts/user/${userId}/items/${productId}`, { method: "PUT", body: JSON.stringify(payload) }),
  deleteCartItem: (userId, productId) =>
    request(`/api/carts/user/${userId}/items/${productId}`, { method: "DELETE" }),
  clearCart: (userId) => request(`/api/carts/user/${userId}`, { method: "DELETE" })
};
