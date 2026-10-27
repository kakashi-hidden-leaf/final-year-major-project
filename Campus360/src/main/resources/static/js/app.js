// ==============================================================
// CAMPUS 360 - JAVASCRIPT LOGIC & NOTIFICATION CENTER
// ==============================================================

document.addEventListener('DOMContentLoaded', function () {
    initNotificationCenter();
});

/**
 * Initializes the top-navbar notification center polling and dropdown events.
 */
function initNotificationCenter() {
    const badge = document.getElementById('navbarNotificationBadge');
    const bellBtn = document.getElementById('navbarNotificationBtn');
    const container = document.getElementById('notificationListContainer');

    if (!bellBtn) return;

    // Fetch unread count immediately
    updateUnreadCount();

    // Poll every 30 seconds for new alerts (lecture reminders, announcements)
    setInterval(updateUnreadCount, 30000);

    // Fetch recent notifications when dropdown opens
    bellBtn.addEventListener('show.bs.dropdown', function () {
        loadRecentNotifications();
    });
}

function updateUnreadCount() {
    fetch('/api/notifications/unread-count')
        .then(res => res.json())
        .then(data => {
            const badge = document.getElementById('navbarNotificationBadge');
            if (badge) {
                if (data.count > 0) {
                    badge.textContent = data.count > 99 ? '99+' : data.count;
                    badge.classList.remove('d-none');
                } else {
                    badge.classList.add('d-none');
                }
            }
        })
        .catch(err => console.error('Failed to update notification count:', err));
}

function loadRecentNotifications() {
    const container = document.getElementById('notificationListContainer');
    if (!container) return;

    container.innerHTML = '<div class="p-3 text-center text-muted"><div class="spinner-border spinner-border-sm me-2"></div>Loading notifications...</div>';

    fetch('/api/notifications/recent')
        .then(res => res.json())
        .then(notifications => {
            if (!notifications || notifications.length === 0) {
                container.innerHTML = '<div class="p-4 text-center text-muted"><i class="bi bi-bell-slash fs-3 d-block mb-2"></i>No notifications yet</div>';
                return;
            }

            let html = '';
            notifications.forEach(n => {
                const unreadClass = n.read ? '' : 'unread';
                const markBtn = n.read ? '' : `<button class="btn btn-sm btn-link p-0 text-primary text-decoration-none" onclick="markNotificationRead(${n.id}, event)">Mark read</button>`;

                let iconClass = 'bi-info-circle text-primary';
                if (n.type === 'LECTURE_REMINDER') iconClass = 'bi-alarm text-danger';
                else if (n.type === 'ANNOUNCEMENT') iconClass = 'bi-megaphone text-warning';
                else if (n.type === 'ASSIGNMENT') iconClass = 'bi-file-earmark-check text-success';
                else if (n.type === 'ISSUE_UPDATE') iconClass = 'bi-patch-question text-info';

                html += `
                    <div class="notification-item ${unreadClass}" id="notif-item-${n.id}">
                        <div class="d-flex align-items-start gap-2">
                            <i class="bi ${iconClass} fs-5 mt-1"></i>
                            <div class="flex-grow-1">
                                <div class="d-flex justify-content-between align-items-center mb-1">
                                    <strong class="small text-dark">${escapeHtml(n.title)}</strong>
                                    ${markBtn}
                                </div>
                                <p class="mb-1 small text-secondary">${escapeHtml(n.message)}</p>
                                <span class="text-muted" style="font-size: 0.72rem;">${n.createdAt}</span>
                            </div>
                        </div>
                    </div>
                `;
            });

            container.innerHTML = html;
        })
        .catch(err => {
            container.innerHTML = '<div class="p-3 text-center text-danger small">Failed to load notifications</div>';
            console.error(err);
        });
}

function markNotificationRead(id, event) {
    if (event) event.stopPropagation();

    fetch(`/api/notifications/${id}/read`, { method: 'POST' })
        .then(res => res.json())
        .then(data => {
            const el = document.getElementById(`notif-item-${id}`);
            if (el) {
                el.classList.remove('unread');
                const btn = el.querySelector('button');
                if (btn) btn.remove();
            }
            updateUnreadCount();
        })
        .catch(err => console.error('Error marking as read:', err));
}

function markAllNotificationsRead(event) {
    if (event) event.stopPropagation();

    fetch('/api/notifications/mark-all-read', { method: 'POST' })
        .then(res => res.json())
        .then(data => {
            loadRecentNotifications();
            updateUnreadCount();
        })
        .catch(err => console.error('Error marking all as read:', err));
}

function fillDemoLogin(username, password) {
    const userField = document.getElementById('username');
    const passField = document.getElementById('password');
    if (userField && passField) {
        userField.value = username;
        passField.value = password;
        // visual pulse effect
        userField.classList.add('is-valid');
        passField.classList.add('is-valid');
        setTimeout(() => {
            userField.classList.remove('is-valid');
            passField.classList.remove('is-valid');
        }, 1200);
    }
}

function escapeHtml(string) {
    const pre = document.createElement('pre');
    pre.textContent = string;
    return pre.innerHTML;
}
