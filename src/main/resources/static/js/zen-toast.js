/**
 * UTETra Zen Toast Notification System
 * Replaces hardcoded HTML alert blocks with a modern, non-intrusive, floating Glassmorphism toast system.
 * Works seamlessly with Spring MVC Flash Attributes and client-side interactions.
 */

(function () {
  'use strict';

  // Ensure toast container exists
  function getOrCreateContainer() {
    let container = document.getElementById('zenToastContainer');
    if (!container) {
      container = document.createElement('div');
      container.id = 'zenToastContainer';
      container.className = 'zen-toast-container';
      container.setAttribute('aria-live', 'polite');
      document.body.appendChild(container);
    }
    return container;
  }

  // Icon mapping by type
  const ICON_MAP = {
    success: '<i class="fa-solid fa-circle-check"></i>',
    error: '<i class="fa-solid fa-triangle-exclamation"></i>',
    danger: '<i class="fa-solid fa-triangle-exclamation"></i>',
    warning: '<i class="fa-solid fa-circle-exclamation"></i>',
    info: '<i class="fa-solid fa-circle-info"></i>'
  };

  // Default title mapping by type
  const TITLE_MAP = {
    success: 'Thành Công',
    error: 'Thông Báo Lỗi',
    danger: 'Thông Báo Lỗi',
    warning: 'Cảnh Báo',
    info: 'Thông Báo Hệ Thống'
  };

  /**
   * Display a floating Toast notification.
   * @param {Object} options
   * @param {string} [options.type='info'] - 'success' | 'error' | 'danger' | 'warning' | 'info'
   * @param {string} [options.title] - Custom title (optional)
   * @param {string} options.message - Notification message content
   * @param {number} [options.duration] - Auto-hide timeout in ms (default: 4500ms, error: 6500ms)
   * @returns {HTMLElement} - The created toast element
   */
  window.showToast = function (options) {
    if (!options) return null;

    if (typeof options === 'string') {
      options = { message: options, type: 'info' };
    }

    const type = (options.type || 'info').toLowerCase();
    const title = options.title || TITLE_MAP[type] || 'Thông Báo';
    const message = options.message || '';
    const duration = options.duration !== undefined ? options.duration : (type === 'error' || type === 'danger' ? 6500 : 4500);

    const container = getOrCreateContainer();

    const toast = document.createElement('div');
    toast.className = `zen-toast zen-toast-${type}`;
    toast.setAttribute('role', 'alert');

    const iconHtml = ICON_MAP[type] || ICON_MAP.info;

    toast.innerHTML = `
      <div class="zen-toast-icon-wrap">
        ${iconHtml}
      </div>
      <div class="zen-toast-body">
        <div class="zen-toast-title">${title}</div>
        <div class="zen-toast-desc">${message}</div>
      </div>
      <button type="button" class="zen-toast-close" aria-label="Đóng thông báo">
        <i class="fa-solid fa-xmark"></i>
      </button>
      <div class="zen-toast-progress-bar" style="width: 100%;"></div>
    `;

    container.appendChild(toast);

    const progressBar = toast.querySelector('.zen-toast-progress-bar');
    const closeBtn = toast.querySelector('.zen-toast-close');

    let timer = null;
    let startTime = Date.now();
    let remainingTime = duration;
    let isPaused = false;

    function hideToast() {
      if (timer) clearTimeout(timer);
      toast.classList.add('hiding');
      setTimeout(() => {
        if (toast.parentNode) {
          toast.parentNode.removeChild(toast);
        }
      }, 320);
    }

    closeBtn.addEventListener('click', function (e) {
      e.stopPropagation();
      hideToast();
    });

    if (duration > 0) {
      // Start progress countdown
      requestAnimationFrame(() => {
        if (progressBar) {
          progressBar.style.transition = `width ${duration}ms linear`;
          progressBar.style.width = '0%';
        }
      });

      function startTimer(time) {
        startTime = Date.now();
        remainingTime = time;
        timer = setTimeout(hideToast, time);
      }

      startTimer(duration);

      // Pause on hover
      toast.addEventListener('mouseenter', function () {
        if (!isPaused) {
          clearTimeout(timer);
          isPaused = true;
          const elapsed = Date.now() - startTime;
          remainingTime = Math.max(0, remainingTime - elapsed);
          if (progressBar) {
            const computedWidth = window.getComputedStyle(progressBar).width;
            progressBar.style.transition = 'none';
            progressBar.style.width = computedWidth;
          }
        }
      });

      // Resume on mouse leave
      toast.addEventListener('mouseleave', function () {
        if (isPaused) {
          isPaused = false;
          if (remainingTime > 0) {
            if (progressBar) {
              progressBar.style.transition = `width ${remainingTime}ms linear`;
              progressBar.style.width = '0%';
            }
            startTimer(remainingTime);
          } else {
            hideToast();
          }
        }
      });
    } else {
      if (progressBar) progressBar.style.display = 'none';
    }

    return toast;
  };

  // Helper convenience methods
  window.toastSuccess = (msg, title, duration) => window.showToast({ type: 'success', message: msg, title, duration });
  window.toastError = (msg, title, duration) => window.showToast({ type: 'error', message: msg, title, duration });
  window.toastWarning = (msg, title, duration) => window.showToast({ type: 'warning', message: msg, title, duration });
  window.toastInfo = (msg, title, duration) => window.showToast({ type: 'info', message: msg, title, duration });

  // Auto-scan Spring MVC Flash Attributes on page load
  document.addEventListener('DOMContentLoaded', function () {
    const flashElement = document.getElementById('zenFlashData');
    if (!flashElement) return;

    const successMsg = flashElement.getAttribute('data-success');
    const errorMsg = flashElement.getAttribute('data-error');
    const warningMsg = flashElement.getAttribute('data-warning');
    const infoMsg = flashElement.getAttribute('data-info');

    if (successMsg && successMsg.trim() !== '') {
      window.toastSuccess(successMsg);
    }
    if (errorMsg && errorMsg.trim() !== '') {
      window.toastError(errorMsg);
    }
    if (warningMsg && warningMsg.trim() !== '') {
      window.toastWarning(warningMsg);
    }
    if (infoMsg && infoMsg.trim() !== '') {
      window.toastInfo(infoMsg);
    }
  });
})();
