const API_BASE_URL = "http://localhost:8080/api/v1"; // my local springboot api

const TOKEN_KEY = "tm_token";
const USER_KEY = "tm_user"; // username and role can come here

function saveSession(authResponse) {
  localStorage.setItem(TOKEN_KEY, authResponse.token);
  localStorage.setItem(USER_KEY, JSON.stringify({
    username: authResponse.username,
    role: authResponse.role,
  }));
}

function getToken() {
  return localStorage.getItem(TOKEN_KEY);
}

function getCurrentUser() {
  const raw = localStorage.getItem(USER_KEY);
  return raw ? JSON.parse(raw) : null;
}

function clearSession() {
  localStorage.removeItem(TOKEN_KEY);
  localStorage.removeItem(USER_KEY);
}

function requireAuth() {
  if (!getToken()) {
    window.location.href = "login.html";
  }
}

async function apiFetch(path, options = {}) {
  const headers = Object.assign(
    { "Content-Type": "application/json" },
    options.headers || {}
  );

  const token = getToken();
  if (token) {
    headers["Authorization"] = `Bearer ${token}`;
  }

  const response = await fetch(`${API_BASE_URL}${path}`, {
    ...options,
    headers,
  });

  if (response.status === 401) {
    clearSession();
    window.location.href = "login.html";
    throw new Error("Session expired. Please log in again.");
  }

  let body = null;
  const text = await response.text();
  if (text) {
    try { body = JSON.parse(text); } catch (e) { body = text; }
  }

  if (!response.ok) {
    const message = (body && (body.message || (body.details && body.details.join(", ")))) || "Request failed";
    throw new Error(message);
  }

  return body;
}
