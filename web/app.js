const state = {
  resources: [],
  active: [],
  bills: []
};

const els = {
  resourceList: document.getElementById("resource-list"),
  activeList: document.getElementById("active-list"),
  billList: document.getElementById("bill-list"),
  addForm: document.getElementById("add-form"),
  toggleAdd: document.getElementById("toggle-add"),
  serviceRows: document.getElementById("service-rows"),
  addServiceRow: document.getElementById("add-service-row"),
  toast: document.getElementById("toast"),
  startDialog: document.getElementById("start-dialog"),
  startForm: document.getElementById("start-form"),
  startUser: document.getElementById("start-user"),
  startService: document.getElementById("start-service"),
  startResourceId: document.getElementById("start-resource-id"),
  startResourceLabel: document.getElementById("start-resource-label"),
  startCancel: document.getElementById("start-cancel")
};

async function api(path, options) {
  const res = await fetch(path, Object.assign({
    headers: { "Content-Type": "application/json" }
  }, options || {}));
  const text = await res.text();
  let data = null;
  try {
    data = text ? JSON.parse(text) : null;
  } catch (e) {
    data = { error: text };
  }
  if (!res.ok) {
    throw new Error((data && data.error) || "request failed");
  }
  return data;
}

function money(n) {
  return "Rs " + Number(n).toFixed(2);
}

function formatDuration(totalSeconds) {
  const s = Math.max(0, Number(totalSeconds) || 0);
  const h = Math.floor(s / 3600);
  const m = Math.floor((s % 3600) / 60);
  const sec = s % 60;
  if (h > 0) return h + "h " + m + "m " + sec + "s";
  if (m > 0) return m + "m " + sec + "s";
  return sec + "s";
}

function elapsedSeconds(isoStart) {
  const start = Date.parse(isoStart);
  if (isNaN(start)) return 0;
  return Math.max(0, Math.floor((Date.now() - start) / 1000));
}

function toast(msg) {
  els.toast.textContent = msg;
  els.toast.classList.add("show");
  setTimeout(function () {
    els.toast.classList.remove("show");
  }, 2500);
}

function escapeHtml(value) {
  return String(value == null ? "" : value)
    .replaceAll("&", "&amp;")
    .replaceAll("<", "&lt;")
    .replaceAll(">", "&gt;")
    .replaceAll('"', "&quot;");
}

function addServiceRow(name, first, extra) {
  const row = document.createElement("div");
  row.className = "service-row";
  row.innerHTML =
    '<label>Service name<input name="serviceName" required value="' + escapeHtml(name || "Hourly") + '"></label>' +
    '<label>1st hour (Rs)<input name="firstHour" type="number" min="0" value="' + (first || 30) + '" required></label>' +
    '<label>Extra hour (Rs)<input name="extraHour" type="number" min="0" value="' + (extra || 10) + '" required></label>' +
    '<button type="button" class="remove-service">remove</button>';
  els.serviceRows.appendChild(row);
}

function renderResources() {
  if (!state.resources.length) {
    els.resourceList.innerHTML = '<p class="empty">No resources yet</p>';
    return;
  }
  let html = "";
  for (let i = 0; i < state.resources.length; i++) {
    const r = state.resources[i];
    let svcs = "";
    const list = r.services || [];
    for (let j = 0; j < list.length; j++) {
      const s = list[j];
      svcs += "<li>" + escapeHtml(s.name) + " - 1st " + money(s.firstHourPriceInr) +
        ", extra " + money(s.additionalHourPriceInr) + "</li>";
    }
    html += '<div class="item' + (r.isFull ? " full" : "") + '"><div>' +
      '<p class="title">' + escapeHtml(r.name) + "</p>" +
      '<p class="meta">' + r.availableSlots + " / " + r.capacity + " free · " + escapeHtml(r.typeLabel) + "</p>" +
      '<ul class="svc">' + svcs + "</ul></div>" +
      '<button data-start="' + r.id + '"' + (r.isFull ? " disabled" : "") + ">" +
      (r.isFull ? "Full" : "Start") + "</button></div>";
  }
  els.resourceList.innerHTML = html;
}

function renderActive() {
  if (!state.active.length) {
    els.activeList.innerHTML = '<p class="empty">None running</p>';
    return;
  }
  let html = "";
  for (let i = 0; i < state.active.length; i++) {
    const s = state.active[i];
    html += '<div class="item" data-session="' + s.id + '"><div>' +
      '<p class="title">' + escapeHtml(s.userName) + " - " + escapeHtml(s.resourceName || "") + "</p>" +
      '<p class="meta">' + escapeHtml(s.serviceName || "") + ' · <span class="timer">' +
      formatDuration(elapsedSeconds(s.startTime)) + "</span></p></div>" +
      '<button class="stop-btn" data-stop="' + s.id + '">Stop</button></div>';
  }
  els.activeList.innerHTML = html;
}

function renderBills() {
  if (!state.bills.length) {
    els.billList.innerHTML = '<p class="empty">No bills yet</p>';
    return;
  }
  let html = "";
  for (let i = 0; i < state.bills.length; i++) {
    const b = state.bills[i];
    html += '<div class="item"><div>' +
      '<p class="title">' + escapeHtml(b.userName) + " - " + escapeHtml(b.resourceName) + "</p>" +
      '<p class="meta">' + escapeHtml(b.serviceName) + " · " + escapeHtml(b.durationLabel) +
      " (" + b.billableHours + " hr)</p></div>" +
      '<div class="amt">' + money(b.amountInr) + "</div></div>";
  }
  els.billList.innerHTML = html;
}

function tickTimers() {
  const rows = document.querySelectorAll("#active-list [data-session]");
  for (let i = 0; i < rows.length; i++) {
    const id = rows[i].getAttribute("data-session");
    const session = state.active.find(function (s) { return s.id === id; });
    if (!session) continue;
    const timer = rows[i].querySelector(".timer");
    if (timer) timer.textContent = formatDuration(elapsedSeconds(session.startTime));
  }
}

async function refresh() {
  const resources = await api("/api/resources");
  const active = await api("/api/usage/active");
  const bills = await api("/api/bills");
  state.resources = resources;
  state.active = active;
  state.bills = bills;
  renderResources();
  renderActive();
  renderBills();
}

els.toggleAdd.addEventListener("click", function () {
  els.addForm.classList.toggle("hidden");
});

els.addServiceRow.addEventListener("click", function () {
  addServiceRow("Hourly", 30, 10);
});

els.serviceRows.addEventListener("click", function (e) {
  const btn = e.target.closest(".remove-service");
  if (!btn) return;
  if (els.serviceRows.children.length <= 1) return;
  btn.parentNode.remove();
});

els.addForm.addEventListener("submit", async function (e) {
  e.preventDefault();
  const form = new FormData(els.addForm);
  const rows = els.serviceRows.querySelectorAll(".service-row");
  const services = [];
  for (let i = 0; i < rows.length; i++) {
    services.push({
      name: rows[i].querySelector('[name="serviceName"]').value.trim(),
      firstHourPriceInr: Number(rows[i].querySelector('[name="firstHour"]').value),
      additionalHourPriceInr: Number(rows[i].querySelector('[name="extraHour"]').value)
    });
  }
  try {
    await api("/api/resources", {
      method: "POST",
      body: JSON.stringify({
        name: form.get("name"),
        type: form.get("type"),
        capacity: Number(form.get("capacity")),
        services: services
      })
    });
    els.addForm.reset();
    els.serviceRows.innerHTML = "";
    addServiceRow("Hourly", 30, 10);
    els.addForm.classList.add("hidden");
    toast("added");
    await refresh();
  } catch (err) {
    toast(err.message);
  }
});

els.resourceList.addEventListener("click", function (e) {
  const button = e.target.closest("[data-start]");
  if (!button) return;
  const id = button.getAttribute("data-start");
  const resource = state.resources.find(function (r) { return r.id === id; });
  if (!resource) return;
  els.startResourceId.value = id;
  els.startResourceLabel.textContent = resource.name;
  let opts = "";
  const svcs = resource.services || [];
  for (let i = 0; i < svcs.length; i++) {
    const s = svcs[i];
    opts += '<option value="' + s.id + '">' + escapeHtml(s.name) +
      " (1st " + money(s.firstHourPriceInr) + ", extra " + money(s.additionalHourPriceInr) + ")</option>";
  }
  els.startService.innerHTML = opts;
  els.startUser.value = "";
  els.startDialog.showModal();
});

els.startCancel.addEventListener("click", function () {
  els.startDialog.close();
});

els.startForm.addEventListener("submit", async function (e) {
  e.preventDefault();
  try {
    await api("/api/usage/start", {
      method: "POST",
      body: JSON.stringify({
        resourceId: els.startResourceId.value,
        serviceId: els.startService.value,
        userName: els.startUser.value.trim()
      })
    });
    els.startDialog.close();
    toast("started");
    await refresh();
  } catch (err) {
    toast(err.message);
  }
});

els.activeList.addEventListener("click", async function (e) {
  const button = e.target.closest("[data-stop]");
  if (!button) return;
  button.disabled = true;
  try {
    const bill = await api("/api/usage/stop", {
      method: "POST",
      body: JSON.stringify({ sessionId: button.getAttribute("data-stop") })
    });
    toast("bill: " + money(bill.amountInr));
    await refresh();
  } catch (err) {
    toast(err.message);
    button.disabled = false;
  }
});

addServiceRow("Hourly", 30, 10);
refresh().catch(function (err) { toast(err.message); });
setInterval(tickTimers, 1000);
setInterval(function () { refresh().catch(function () {}); }, 8000);
