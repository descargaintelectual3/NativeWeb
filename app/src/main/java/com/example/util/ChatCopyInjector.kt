package com.example.util

import android.webkit.WebView

/**
 * 📋 ChatCopyInjector - Inyector de Funciones Avanzadas de Chat para WebNative Pro
 * Proporciona detección inteligente de burbujas de chat (ChatGPT, Claude, Gemini, Bené Portal, DeepSeek, Bolt, etc.),
 * inyección de botones de copiado con feedback visual y desplazamiento ultra rápido.
 */
object ChatCopyInjector {

    private const val INJECT_JS = """
        (function() {
            if (window.__chatCopyInjectorInitialized) return;
            window.__chatCopyInjectorInitialized = true;

            // 1. Motor de Copiado al Portapapeles con Doble Fallback
            window.__copyToClipboardNative = function(text, btnEl) {
                if (!text) return;
                
                function showSuccess() {
                    if (!btnEl) return;
                    const origHtml = btnEl.innerHTML;
                    btnEl.innerHTML = '<span style="color:#10b981;font-weight:bold;">✓ Copiado</span>';
                    btnEl.style.borderColor = '#10b981';
                    setTimeout(() => {
                        btnEl.innerHTML = origHtml;
                        btnEl.style.borderColor = '';
                    }, 2000);
                }

                if (navigator.clipboard && navigator.clipboard.writeText) {
                    navigator.clipboard.writeText(text).then(showSuccess).catch(err => {
                        fallbackExecCopy(text, showSuccess);
                    });
                } else {
                    fallbackExecCopy(text, showSuccess);
                }
            };

            function fallbackExecCopy(text, cb) {
                const ta = document.createElement('textarea');
                ta.value = text;
                ta.style.position = 'fixed';
                ta.style.opacity = '0';
                document.body.appendChild(ta);
                ta.select();
                try {
                    document.execCommand('copy');
                    if (cb) cb();
                } catch(e) {
                    console.error('Copy fallback failed:', e);
                }
                document.body.removeChild(ta);
            }

            // 2. Desplazamiento Rápido Suave
            window.__scrollChatToTop = function() {
                window.scrollTo({ top: 0, behavior: 'smooth' });
                const mainBox = document.getElementById('messages-list') || document.querySelector('.messages-box') || document.querySelector('main');
                if (mainBox) mainBox.scrollTo({ top: 0, behavior: 'smooth' });
            };

            window.__scrollChatToBottom = function() {
                window.scrollTo({ top: document.body.scrollHeight, behavior: 'smooth' });
                const mainBox = document.getElementById('messages-list') || document.querySelector('.messages-box') || document.querySelector('main');
                if (mainBox) mainBox.scrollTo({ top: mainBox.scrollHeight, behavior: 'smooth' });
            };

            // 3. Inyección Automática de Botones de Copiado en Bloques y Mensajes
            function scanAndAttachCopyButtons() {
                const messageSelectors = [
                    '.msg:not([data-copy-injected])',
                    '[data-message-author-role]:not([data-copy-injected])',
                    '.chat-message:not([data-copy-injected])',
                    'article:not([data-copy-injected])'
                ];

                document.querySelectorAll(messageSelectors.join(',')).forEach(el => {
                    el.setAttribute('data-copy-injected', 'true');
                    
                    // Si ya tiene botón interno, omitir
                    if (el.querySelector('.btn-copy-msg') || el.querySelector('button[title*="Copiar"]')) return;

                    const header = el.querySelector('.msg-header') || el.querySelector('header') || el;
                    const copyBtn = document.createElement('button');
                    copyBtn.className = 'webnative-injected-copy-btn';
                    copyBtn.innerHTML = '📋 Copiar';
                    copyBtn.style.cssText = 'background:rgba(15,23,42,0.7); border:1px solid #334155; color:#94a3b8; border-radius:6px; padding:2px 7px; font-size:11px; cursor:pointer; margin-left:auto; z-index:10;';
                    
                    copyBtn.onclick = function(e) {
                        e.stopPropagation();
                        const contentEl = el.querySelector('.content') || el.querySelector('.message-body') || el;
                        const text = (contentEl.innerText || contentEl.textContent || '').trim();
                        window.__copyToClipboardNative(text, copyBtn);
                    };

                    if (header !== el) {
                        header.appendChild(copyBtn);
                    }
                });
            }

            // Observador de Mutaciones para Chat Reactivo
            const observer = new MutationObserver(() => {
                scanAndAttachCopyButtons();
            });
            observer.observe(document.body, { childList: true, subtree: true });
            scanAndAttachCopyButtons();
        })();
    """

    fun inject(webView: WebView?) {
        webView?.evaluateJavascript(INJECT_JS, null)
    }

    fun scrollToTop(webView: WebView?) {
        webView?.evaluateJavascript("if (window.__scrollChatToTop) { window.__scrollChatToTop(); } else { window.scrollTo({top:0, behavior:'smooth'}); }", null)
    }

    fun scrollToBottom(webView: WebView?) {
        webView?.evaluateJavascript("if (window.__scrollChatToBottom) { window.__scrollChatToBottom(); } else { window.scrollTo({top:document.body.scrollHeight, behavior:'smooth'}); }", null)
    }
}
