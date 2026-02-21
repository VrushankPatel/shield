import http from "k6/http";
import { check, sleep } from "k6";
import exec from "k6/execution";

const API_BASE_URL = __ENV.BASE_URL || "http://localhost:8080/api/v1";
const ROOT_BOOTSTRAP_FILE = __ENV.ROOT_BOOTSTRAP_FILE || "./root-bootstrap-credential.txt";
const ROOT_NEW_PASSWORD = __ENV.PERF_ROOT_PASSWORD || "RootPerf#2026!Secure";
const PERF_ROOT_EMAIL = __ENV.PERF_ROOT_EMAIL || "root.perf@shield.dev";
const PERF_ROOT_MOBILE = __ENV.PERF_ROOT_MOBILE || "9999999999";
const PERF_ADMIN_PASSWORD = __ENV.PERF_ADMIN_PASSWORD || "AdminPerf#2026!";
const BOOTSTRAP_CONTENT = open(ROOT_BOOTSTRAP_FILE);

export const options = {
  vus: Number(__ENV.PERF_VUS || 5),
  duration: __ENV.PERF_DURATION || "30s",
  thresholds: {
    http_req_failed: ["rate<0.01"],
    http_req_duration: ["p(95)<750"],
  },
};

function jsonHeaders(extra) {
  return {
    headers: Object.assign({ "Content-Type": "application/json" }, extra || {}),
  };
}

function bearerHeaders(token, contentType) {
  const headers = { Authorization: `Bearer ${token}` };
  if (contentType) {
    headers["Content-Type"] = contentType;
  }
  return { headers };
}

function extractBootstrapPassword(content) {
  const lines = content.split("\n");
  for (const line of lines) {
    if (line.startsWith("credential=")) {
      return line.substring("credential=".length).trim();
    }
  }
  throw new Error("Unable to find credential entry in root bootstrap credential file");
}

function rootLogin(password) {
  const response = http.post(
    `${API_BASE_URL}/platform/root/login`,
    JSON.stringify({ loginId: "root", password }),
    jsonHeaders()
  );

  check(response, {
    "root login status is 200": (r) => r.status === 200,
    "root login returns access token": (r) => !!r.json("data.accessToken"),
  });

  if (response.status !== 200) {
    throw new Error(`Root login failed with status ${response.status}`);
  }

  return {
    accessToken: response.json("data.accessToken"),
    passwordChangeRequired: response.json("data.passwordChangeRequired") === true,
  };
}

function changeRootPassword(accessToken, newPassword) {
  const response = http.post(
    `${API_BASE_URL}/platform/root/change-password`,
    JSON.stringify({
      email: PERF_ROOT_EMAIL,
      mobile: PERF_ROOT_MOBILE,
      newPassword,
      confirmNewPassword: newPassword,
    }),
    bearerHeaders(accessToken, "application/json")
  );

  check(response, {
    "root password change status is 200": (r) => r.status === 200,
  });

  if (response.status !== 200) {
    throw new Error(`Root password change failed with status ${response.status}`);
  }
}

function onboardSociety(accessToken, suffix) {
  const payload = {
    societyName: `Perf Society ${suffix}`,
    societyAddress: "Performance Test Address",
    adminName: `Perf Admin ${suffix}`,
    adminEmail: `perf.admin.${suffix}@shield.dev`,
    adminPhone: "9000000000",
    adminPassword: PERF_ADMIN_PASSWORD,
  };

  const response = http.post(
    `${API_BASE_URL}/platform/societies`,
    JSON.stringify(payload),
    bearerHeaders(accessToken, "application/json")
  );

  check(response, {
    "society onboarding status is 200": (r) => r.status === 200,
    "society onboarding returns tenant id": (r) => !!r.json("data.tenantId"),
  });

  if (response.status !== 200) {
    throw new Error(`Society onboarding failed with status ${response.status}`);
  }

  return payload;
}

function adminLogin(email, password) {
  const response = http.post(
    `${API_BASE_URL}/auth/login`,
    JSON.stringify({ email, password }),
    jsonHeaders()
  );

  check(response, {
    "admin login status is 200": (r) => r.status === 200,
    "admin login returns token": (r) => !!r.json("data.accessToken"),
  });

  if (response.status !== 200) {
    throw new Error(`Admin login failed with status ${response.status}`);
  }

  return response.json("data.accessToken");
}

export function setup() {
  const bootstrapPassword = extractBootstrapPassword(BOOTSTRAP_CONTENT);
  let root = rootLogin(bootstrapPassword);

  if (root.passwordChangeRequired) {
    changeRootPassword(root.accessToken, ROOT_NEW_PASSWORD);
    root = rootLogin(ROOT_NEW_PASSWORD);
  }

  const suffix = Date.now();
  const onboarded = onboardSociety(root.accessToken, suffix);
  const adminToken = adminLogin(onboarded.adminEmail, onboarded.adminPassword);

  return { adminToken };
}

export default function runAuthenticatedFlow(data) {
  const token = data.adminToken;
  const settingsKey = `perf.setting.${exec.vu.idInTest}`;

  const updateConfig = http.put(
    `${API_BASE_URL}/config/${settingsKey}`,
    JSON.stringify({ value: "5", category: "performance" }),
    bearerHeaders(token, "application/json")
  );
  check(updateConfig, { "update config is 200": (r) => r.status === 200 });

  const fetchConfig = http.get(
    `${API_BASE_URL}/config/${settingsKey}`,
    bearerHeaders(token)
  );
  check(fetchConfig, { "fetch config is 200": (r) => r.status === 200 });

  const listModules = http.get(
    `${API_BASE_URL}/settings/modules`,
    bearerHeaders(token)
  );
  check(listModules, { "list modules is 200": (r) => r.status === 200 });

  sleep(1);
}

export function handleSummary(data) {
  return {
    "performance-authenticated-summary.json": JSON.stringify(data, null, 2),
  };
}
