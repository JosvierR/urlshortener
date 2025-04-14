document.addEventListener("DOMContentLoaded", async function() {
    // 1) Verificar si el usuario está logueado
    const token = localStorage.getItem("jwtToken");
    if (!token) {
        alert("No has iniciado sesión.");
        window.location.href = "/index.html";
        return;
    }

    // 2) Verificar si es admin
    try {
        const res = await fetch("/api/me", {
            headers: { "Authorization": `Bearer ${token}` }
        });
        if (!res.ok) {
            alert("No autorizado. Debes iniciar sesión como admin.");
            window.location.href = "/index.html";
            return;
        }
        const { username, role } = await res.json();
        if (role !== "admin") {
            alert("No tienes privilegios de administrador.");
            window.location.href = "/index.html";
            return;
        }
        // Si llega aquí, es admin
    } catch (err) {
        console.error("Error al verificar usuario admin:", err);
        alert("Error de autenticación.");
        window.location.href = "/index.html";
        return;
    }

    // 3) Cargar todos los enlaces
    await loadAllLinks();
});

// Función para cargar todos los enlaces
async function loadAllLinks() {
    const token = localStorage.getItem("jwtToken");
    const adminLinksTableBody = document.querySelector("#adminLinksTable tbody");
    adminLinksTableBody.innerHTML = ""; // Limpia la tabla

    try {
        const res = await fetch("/api/urls", {
            headers: { "Authorization": `Bearer ${token}` }
        });
        if (!res.ok) {
            alert("Error al cargar todos los links: " + res.status);
            return;
        }
        const data = await res.json();
        console.log("Recibidos", data.length, "links en admin.js");

        data.forEach(link => {
            // Construimos la URL usando window.location.origin
            const shortUrl = window.location.origin + "/go/" + link.shortCode;

            const tr = document.createElement("tr");
            tr.innerHTML = `
        <td>${link.originalUrl}</td>
        <td>${link.shortCode}</td>
        <td><a href="${shortUrl}" target="_blank">${shortUrl}</a></td>
        <td>${link.createdBy || "N/A"}</td>
        <td>${link.accessCount}</td>
        <td>${new Date(link.creationDate).toLocaleString()}</td>
        <td>
          <button class="btn-secondary" onclick="deleteLink('${link.shortCode}')">Eliminar</button>
        </td>
      `;
            adminLinksTableBody.appendChild(tr);
        });
    } catch (err) {
        console.error("Error loadAllLinks:", err);
    }
}

// Función para eliminar un link
window.deleteLink = async function(shortCode) {
    const token = localStorage.getItem("jwtToken");
    if (!token) {
        alert("No token found");
        return;
    }
    if (!confirm(`¿Seguro que deseas eliminar el link ${shortCode}?`)) {
        return;
    }
    try {
        const res = await fetch(`/delete/${shortCode}`, {
            method: "DELETE",
            headers: { "Authorization": `Bearer ${token}` }
        });
        if (res.ok) {
            alert("Link eliminado exitosamente.");
            loadAllLinks(); // recargar la tabla
        } else {
            const text = await res.text();
            alert("Error al eliminar: " + text);
        }
    } catch (error) {
        console.error("Error al eliminar link:", error);
        alert("Error al eliminar link.");
    }
};
