document.addEventListener("DOMContentLoaded", function() {
    const params = new URLSearchParams(window.location.search);
    const shortCode = params.get("code");

    // Se requiere token para acceder al dashboard. Se obtiene del localStorage.
    const token = localStorage.getItem("jwtToken");
    fetch(`/api/summary/${shortCode}`, {
        headers: {
            "Authorization": token ? `Bearer ${token}` : ""
        }
    })
        .then(res => {
            if (!res.ok) throw new Error("Error al cargar los datos (status: " + res.status + ")");
            return res.json();
        })
        .then(data => {
            // Detalles
            document.getElementById("originalUrl").innerText = data.originalUrl;
            document.getElementById("shortCode").innerText = data.shortCode;
            const creationDate = new Date(data.creationDate);
            document.getElementById("creationDate").innerText = creationDate.toLocaleString();
            document.getElementById("accessCount").innerText = data.accessCount;

            // Código QR
            new QRCode(document.getElementById("qrcode"), {
                text: window.location.origin + "/go/" + data.shortCode,
                width: 128,
                height: 128,
                colorDark : "#2c3e50",
                colorLight : "#ffffff",
                correctLevel : QRCode.CorrectLevel.H
            });

            // Enlace de acceso directo
            const directLink = window.location.origin + "/go/" + data.shortCode;
            document.getElementById("directLink").href = directLink;
            document.getElementById("directLink").innerText = directLink;

            // Historial de accesos
            const accessLogs = data.accessLogs || [];
            const logsTableBody = document.querySelector("#accessLogsTable tbody");
            logsTableBody.innerHTML = "";
            accessLogs.forEach(log => {
                const tr = document.createElement("tr");
                const dateStr = new Date(log.timestamp).toLocaleString();
                const user = log.user || "invitado";
                const ip = log.ip || "desconocida";
                tr.innerHTML = `<td>${dateStr}</td><td>${user}</td><td>${ip}</td>`;
                logsTableBody.appendChild(tr);
            });

            // Gráfico de barras (cada acceso)
            createBarChart(accessLogs);

            // Gráfico de líneas (accesos por hora)
            createHourlyLineChart(accessLogs);

            // Gráfico de Línea de Tiempo (acumulado) - usando escala temporal
            createTimelineChart(accessLogs);
        })
        .catch(err => {
            console.error(err);
            document.body.innerHTML = `<p>Error al cargar el dashboard: ${err.message}</p>`;
        });

    function createBarChart(accessLogs) {
        const labels = accessLogs.map(log => new Date(log.timestamp).toLocaleString());
        const dataPoints = accessLogs.map(() => 1);

        const ctx = document.getElementById("accessChart").getContext("2d");
        new Chart(ctx, {
            type: 'bar',
            data: {
                labels: labels.length ? labels : ["Sin accesos"],
                datasets: [{
                    label: 'Accesos',
                    data: dataPoints.length ? dataPoints : [0],
                    backgroundColor: "#1abc9c"
                }]
            },
            options: {
                scales: {
                    y: {
                        beginAtZero: true,
                        ticks: { stepSize: 1 }
                    }
                }
            }
        });
    }

    function createHourlyLineChart(accessLogs) {
        // Contar accesos por hora (0 a 23)
        const hourCounts = new Array(24).fill(0);
        accessLogs.forEach(log => {
            const hour = new Date(log.timestamp).getHours();
            hourCounts[hour]++;
        });

        const labels = [];
        const dataPoints = [];
        for (let h = 0; h < 24; h++) {
            labels.push(`${h}:00`);
            dataPoints.push(hourCounts[h]);
        }

        const ctx = document.getElementById("hourlyChart").getContext("2d");
        new Chart(ctx, {
            type: 'line',
            data: {
                labels: labels,
                datasets: [{
                    label: 'Accesos por Hora',
                    data: dataPoints,
                    borderColor: "#e67e22",
                    backgroundColor: "rgba(230,126,34,0.2)",
                    fill: true,
                    tension: 0.1
                }]
            },
            options: {
                scales: {
                    y: {
                        beginAtZero: true,
                        ticks: { stepSize: 1 }
                    }
                }
            }
        });
    }

    function createTimelineChart(accessLogs) {
        // Ordenar los registros por timestamp y calcular el acumulado
        const sortedLogs = accessLogs.slice().sort((a, b) => a.timestamp - b.timestamp);
        let cumulative = 0;
        const dataPoints = sortedLogs.map(log => {
            cumulative++;
            return { x: new Date(log.timestamp), y: cumulative };
        });

        const ctx = document.getElementById("timelineChart").getContext("2d");
        new Chart(ctx, {
            type: 'line',
            data: {
                datasets: [{
                    label: 'Accesos Acumulados',
                    data: dataPoints,
                    borderColor: "#3498db",
                    backgroundColor: "rgba(52,152,219,0.2)",
                    fill: true,
                    tension: 0.3,
                    pointRadius: 4
                }]
            },
            options: {
                scales: {
                    x: {
                        type: 'time',
                        time: {
                            unit: 'hour',
                            tooltipFormat: 'MMM D, YYYY, h:mm a'
                        },
                        title: {
                            display: true,
                            text: 'Fecha y Hora'
                        }
                    },
                    y: {
                        beginAtZero: true,
                        title: {
                            display: true,
                            text: 'Accesos Acumulados'
                        }
                    }
                },
                plugins: {
                    title: {
                        display: true,
                        text: 'Línea de Tiempo de Accesos'
                    }
                }
            }
        });
    }
});
