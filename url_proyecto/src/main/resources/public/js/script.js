<!-- script.js -->

    document.addEventListener("DOMContentLoaded", function() {
    let isAdmin = false;  // Para determinar si el usuario es admin.

    // --- Referencias a elementos ---
    const loginModal = document.getElementById("loginModal");
    const registerModal = document.getElementById("registerModal");
    const openLoginBtn = document.getElementById("openLoginBtn");
    const openRegisterBtn = document.getElementById("openRegisterBtn");
    const closeLoginBtn = document.getElementById("closeLoginBtn");
    const closeRegisterBtn = document.getElementById("closeRegisterBtn");
    const logoutBtn = document.getElementById("logoutBtn");
    const loggedInUserSpan = document.getElementById("loggedInUser");
    const currentUserNameSpan = document.getElementById("currentUserName");
    const adminMenu = document.getElementById("adminMenu");
    const goToAdminBtn = document.getElementById("goToAdminBtn");

    const loginForm = document.getElementById("loginForm");
    const loginResult = document.getElementById("loginResult");
    const registerForm = document.getElementById("registerForm");
    const registerResult = document.getElementById("registerResult");

    const shortenForm = document.getElementById("shortenForm");
    const shortenResult = document.getElementById("shortenedUrl");
    const urlInput = document.getElementById("originalUrl");
    const spinnerContainer = document.getElementById("shortenAnimation");

    // --- Manejadores de ventanas modales ---
    openLoginBtn.addEventListener("click", () => loginModal.style.display = "block");
    openRegisterBtn.addEventListener("click", () => registerModal.style.display = "block");
    closeLoginBtn.addEventListener("click", () => loginModal.style.display = "none");
    closeRegisterBtn.addEventListener("click", () => registerModal.style.display = "none");

    window.addEventListener("click", (e) => {
    if (e.target === loginModal) loginModal.style.display = "none";
    if (e.target === registerModal) registerModal.style.display = "none";
});

    logoutBtn.addEventListener("click", () => {
    localStorage.removeItem("jwtToken");
    location.reload();
});

    goToAdminBtn.addEventListener("click", () => {
    window.location.href = "/admin.html";
});

    // --- Login ---
    loginForm.addEventListener("submit", async (e) => {
    e.preventDefault();
    const username = document.getElementById("loginUsername").value;
    const password = document.getElementById("loginPassword").value;
    try {
    const res = await fetch("/login", {
    method: "POST",
    headers: { "Content-Type": "application/x-www-form-urlencoded" },
    body: `username=${encodeURIComponent(username)}&password=${encodeURIComponent(password)}`
});
    if (res.ok) {
    const data = await res.json();
    localStorage.setItem("jwtToken", data.token);
    loginResult.innerText = "¡Ingreso exitoso!";
    loginModal.style.display = "none";
    location.reload();  // Recarga la página
} else {
    loginResult.innerText = "Credenciales inválidas";
}
} catch (err) {
    console.error(err);
    loginResult.innerText = "Error en el login.";
}
});

    // --- Register ---
    registerForm.addEventListener("submit", async (e) => {
    e.preventDefault();
    const username = document.getElementById("registerUsername").value;
    const password = document.getElementById("registerPassword").value;
    try {
    const res = await fetch("/register", {
    method: "POST",
    headers: { "Content-Type": "application/x-www-form-urlencoded" },
    body: `username=${encodeURIComponent(username)}&password=${encodeURIComponent(password)}`
});
    if (res.ok) {
    registerResult.innerText = "¡Usuario registrado exitosamente!";
    registerModal.style.display = "none";
} else {
    registerResult.innerText = "El usuario ya existe.";
}
} catch (err) {
    console.error(err);
    registerResult.innerText = "Error en el registro.";
}
});

    // --- Información del usuario ---
    async function fetchUserInfo() {
    const token = localStorage.getItem("jwtToken");
    if (!token) {
    // Usuario no logueado
    openLoginBtn.style.display = "inline-block";
    openRegisterBtn.style.display = "inline-block";
    logoutBtn.style.display = "none";
    loggedInUserSpan.style.display = "none";
    adminMenu.style.display = "none";
    return;
}
    try {
    const res = await fetch("/api/me", {
    headers: { "Authorization": `Bearer ${token}` }
});
    if (res.ok) {
    const { username, role } = await res.json();
    openLoginBtn.style.display = "none";
    openRegisterBtn.style.display = "none";
    logoutBtn.style.display = "inline-block";
    loggedInUserSpan.style.display = "inline-block";
    currentUserNameSpan.innerText = username;
    // Si es admin, mostramos botón Admin
    if (role === "admin") {
    adminMenu.style.display = "inline-block";
    isAdmin = true;
} else {
    adminMenu.style.display = "none";
    isAdmin = false;
}
} else {
    openLoginBtn.style.display = "inline-block";
    openRegisterBtn.style.display = "inline-block";
    logoutBtn.style.display = "none";
    loggedInUserSpan.style.display = "none";
    adminMenu.style.display = "none";
}
} catch (error) {
    console.error("Error fetchUserInfo:", error);
}
}

    // --- Refrescar la tabla de URLs sin recargar la página ---
    function refreshLinksTableAjax() {
    fetch('/api/urls', {
    headers: {
    "Authorization": localStorage.getItem("jwtToken") ? `Bearer ${localStorage.getItem("jwtToken")}` : ""
},
    cache: "no-store"
})
    .then(response => {
    if (!response.ok) throw new Error("Error al obtener las URLs");
    return response.json();
})
    .then(data => {
    // Guardamos en localStorage para reordenar efímeramente si lo deseamos
    localStorage.setItem("userUrls", JSON.stringify(data));
    populateUrlsTable(data);
})
    .catch(err => console.error("Error al refrescar la tabla:", err));
}

    // Se refresca la tabla cada 5 segundos
    setInterval(refreshLinksTableAjax, 5000);

    // --- Pintar la tabla de URLs ---
    function populateUrlsTable(data) {
    const tableBody = document.querySelector("#linksTable tbody");
    if (!tableBody) return;

    // En este ejemplo NO ordenamos por creationDate para permitir
    // que el link clicado quede arriba (si así lo deseas).
    // data.sort((a, b) => b.creationDate - a.creationDate); // <- Comentado

    tableBody.innerHTML = ""; // Limpia la tabla
    data.forEach(doc => {
    const shortUrl = window.location.origin + "/go/" + doc.shortCode;
    addLinkToTable(doc.originalUrl, shortUrl, doc.shortCode, doc.creationDate);
});
}

    // --- Agregar una fila con datos actualizados ---
    function addLinkToTable(originalUrl, shortUrl, shortCode, creationDate) {
    const tableBody = document.querySelector("#linksTable tbody");
    if (!tableBody) return;

    // Reemplazamos <a href="shortUrl"> por un onclick que llame a visitLink
    // De esta forma podremos "forzar" recarga o reordenar localmente.
    const row = document.createElement("tr");
    row.innerHTML = `
            <td>${originalUrl}</td>
            <td>${shortCode}</td>
            <td><a href="#" onclick="visitLink('${shortCode}', '${shortUrl}')">${shortUrl}</a></td>
            <td>0</td>
            <td>${new Date(Number(creationDate)).toLocaleString()}</td>
            <td>
                ${
    // Botón "Ver" SOLO si es admin
    isAdmin ? `<button onclick="viewLink('${shortCode}')" class="btn-secondary">Ver</button>` : ''
}
            </td>
        `;

    // Inserta la nueva fila al principio de la tabla (arriba)
    if (tableBody.firstChild) {
    tableBody.insertBefore(row, tableBody.firstChild);
} else {
    tableBody.appendChild(row);
}

    // Hacer un fetch al summary para ver el accessCount real
    fetch(`/api/summary/${shortCode}`, {
    headers: {
    "Authorization": localStorage.getItem("jwtToken") ? `Bearer ${localStorage.getItem("jwtToken")}` : ""
},
    cache: "no-store"
})
    .then(response => {
    if (!response.ok) throw new Error("Error al obtener resumen");
    return response.json();
})
    .then(details => {
    // Actualizamos la celda de accesos (4ta columna)
    row.cells[3].innerText = details.accessCount;
})
    .catch(err => console.error("Error obteniendo summary:", err));
}

    // --- Shorten URL (form) ---
    shortenForm.addEventListener("submit", async (e) => {
    e.preventDefault();
    const url = urlInput.value.trim();
    if (!url) return;

    if (spinnerContainer) spinnerContainer.style.display = "flex";
    shortenResult.innerHTML = "";

    try {
    const res = await fetch("/shorten", {
    method: "POST",
    headers: { "Content-Type": "application/x-www-form-urlencoded" },
    body: `url=${encodeURIComponent(url)}`
});
    if (res.ok) {
    const data = await res.json();
    const shortCode = data.shortUrl.split("/go/")[1];
    const fullShortUrl = window.location.origin + "/go/" + shortCode;

    if (spinnerContainer) spinnerContainer.style.display = "none";
    shortenResult.innerHTML = `
                    Link Cortado: <a href="${fullShortUrl}" target="_blank">${fullShortUrl}</a><br>
                    ${
    isAdmin
    ? `<button onclick="viewLink('${shortCode}')" class="btn-primary">Ver Dashboard</button>`
    : ""
}
                `;
    // Añadimos la nueva fila "arriba"
    addLinkToTable(url, fullShortUrl, shortCode, new Date().getTime());

    // Guardamos en localStorage (por si queremos reordenar efímeramente)
    let userUrls = [];
    const cachedData = localStorage.getItem("userUrls");
    if (cachedData) {
    userUrls = JSON.parse(cachedData);
}
    userUrls.unshift({
    originalUrl: url,
    shortCode,
    creationDate: new Date().getTime(),
    accessCount: 0
});
    localStorage.setItem("userUrls", JSON.stringify(userUrls));
} else {
    if (spinnerContainer) spinnerContainer.style.display = "none";
    shortenResult.innerText = "Error al acortar la URL.";
}
} catch (err) {
    console.error(err);
    if (spinnerContainer) spinnerContainer.style.display = "none";
    shortenResult.innerText = "Error al acortar la URL.";
}
});

    // --- Vista previa con Microlink ---
    let debounceTimer;
    urlInput.addEventListener("input", () => {
    clearTimeout(debounceTimer);
    debounceTimer = setTimeout(async () => {
    const val = urlInput.value.trim();
    const previewDiv = document.getElementById("preview");
    if (!val || !previewDiv) return;

    previewDiv.innerHTML = `<p>Cargando vista previa...</p>`;

    try {
    const res = await fetch(`https://api.microlink.io/?url=${encodeURIComponent(val)}`);
    const json = await res.json();
    if (!json || json.status !== "success") {
    previewDiv.innerHTML = `<p>No se pudo obtener la vista previa.</p>`;
    return;
}
    const { title, description, image, url } = json.data;
    previewDiv.innerHTML = `
                    <div style="padding: 0.5rem;">
                        <strong>${title || "Sin título"}</strong>
                        <p>${description || "Sin descripción."}</p>
                        ${
    image?.url
    ? `<img src="${image.url}" alt="preview" style="max-width:100%; border-radius:8px;">`
    : ""
}
                        <p><a href="${url}" target="_blank" style="color: #22d3ee;">${url}</a></p>
                    </div>
                `;
} catch (err) {
    console.error("Error en preview:", err);
    previewDiv.innerHTML = `<p>Error al cargar la vista previa.</p>`;
}
}, 600);
});

    // --- Función para redirigir al Dashboard de un shortCode (si eres admin) ---
    window.viewLink = function(shortCode) {
    window.location.href = `${window.location.origin}/summary/${shortCode}`;
};

    // --- Función para *interceptar* el clic y abrir la URL + reordenar la tabla ---
    window.visitLink = function(shortCode, shortUrl) {
    // Abrimos el link en otra pestaña:
    window.open(shortUrl, '_blank');

    // (Opcional) Si quieres forzar un reload "de verdad":
    // location.reload();
    // return;

    // Caso "efímero": reordenamos la data en localStorage de modo que
    // el link clicado pase a ser el primero.
    let data = localStorage.getItem("userUrls");
    if (data) {
    let arr = JSON.parse(data);
    let index = arr.findIndex(item => item.shortCode === shortCode);
    if (index !== -1) {
    // Sacamos el link
    let clickedLink = arr.splice(index, 1)[0];
    // Lo ponemos al inicio
    arr.unshift(clickedLink);
    localStorage.setItem("userUrls", JSON.stringify(arr));
    // Repintamos la tabla
    populateUrlsTable(arr);
}
}
};

    // --- Cargar URLs del usuario al iniciar la página ---
    function loadUserUrls() {
    const token = localStorage.getItem("jwtToken");
    if (!token) return;
    fetch("/api/urls", {
    headers: { "Authorization": `Bearer ${token}` }
})
    .then(res => {
    if (!res.ok) throw new Error("Error al obtener /api/urls");
    return res.json();
})
    .then(data => {
    localStorage.setItem("userUrls", JSON.stringify(data));
    populateUrlsTable(data);
})
    .catch(err => {
    console.error("Error loadUserUrls:", err);
    // Si no hay red o falla, mostramos la data local (si existe)
    const cachedData = localStorage.getItem("userUrls");
    if (cachedData) {
    populateUrlsTable(JSON.parse(cachedData));
}
});
}

    // --- Inicialización al cargar la página ---
    if (localStorage.getItem("jwtToken")) {
    fetchUserInfo();
    loadUserUrls();
} else {
    openLoginBtn.style.display = "inline-block";
    openRegisterBtn.style.display = "inline-block";
    logoutBtn.style.display = "none";
    loggedInUserSpan.style.display = "none";
    adminMenu.style.display = "none";
}
});

