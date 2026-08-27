package com.example.util

import android.webkit.WebView

object ChatCopyInjector {

    /**
     * Injects custom CSS & JavaScript into any web chat/IDE (ChatGPT, Claude, Gemini, DeepSeek, Google AI Studio, bolt.new, v0, etc.)
     * so that every message, prompt, response bubble, and code block gets an interactive 1-click "Copiar" button.
     */
    fun injectCopyButtons(webView: WebView, onResult: (Int) -> Unit = {}) {
        val script = """
            (function() {
                let injectedCount = 0;
                
                // Helper to attach copy button
                function attachCopyBtn(element, isCode) {
                    if (element.dataset.copyAttached === 'true') return;
                    element.dataset.copyAttached = 'true';
                    
                    const btn = document.createElement('button');
                    btn.innerText = isCode ? '📋 Copiar Código' : '📋 Copiar Mensaje';
                    btn.className = 'webnative-copy-bubble-btn';
                    btn.style.cssText = 'position: relative; display: inline-flex; align-items: center; justify-content: center; margin: 6px 4px; padding: 5px 12px; background: #6366F1; color: #FFFFFF; font-size: 11px; font-weight: bold; border: 1px solid #818CF8; border-radius: 8px; cursor: pointer; z-index: 99999; box-shadow: 0 2px 8px rgba(0,0,0,0.35); transition: all 0.2s ease; font-family: system-ui, -apple-system, sans-serif;';
                    
                    btn.onclick = function(e) {
                        e.stopPropagation();
                        e.preventDefault();
                        const clone = element.cloneNode(true);
                        const btns = clone.querySelectorAll('.webnative-copy-bubble-btn');
                        btns.forEach(b => b.remove());
                        const text = (clone.innerText || clone.textContent || '').trim();
                        
                        function showSuccess() {
                            btn.innerText = '✅ ¡Copiado!';
                            btn.style.background = '#10B981';
                            btn.style.borderColor = '#34D399';
                            setTimeout(() => {
                                btn.innerText = isCode ? '📋 Copiar Código' : '📋 Copiar Mensaje';
                                btn.style.background = '#6366F1';
                                btn.style.borderColor = '#818CF8';
                            }, 2000);
                        }

                        if (navigator.clipboard && navigator.clipboard.writeText) {
                            navigator.clipboard.writeText(text).then(showSuccess).catch(() => {
                                fallbackCopy(text, showSuccess);
                            });
                        } else {
                            fallbackCopy(text, showSuccess);
                        }
                    };
                    
                    function fallbackCopy(str, cb) {
                        const textarea = document.createElement('textarea');
                        textarea.value = str;
                        textarea.style.position = 'fixed';
                        textarea.style.opacity = '0';
                        document.body.appendChild(textarea);
                        textarea.select();
                        try {
                            document.execCommand('copy');
                            cb();
                        } catch(err) {
                            console.error('Fallback copy failed', err);
                        }
                        document.body.removeChild(textarea);
                    }

                    if (element.firstChild) {
                        element.insertBefore(btn, element.firstChild);
                    } else {
                        element.appendChild(btn);
                    }
                    injectedCount++;
                }

                // Selectors covering modern AI chats, IDEs, markdown viewers, forums
                const bubbleSelectors = [
                    '[class*="message"]',
                    '[class*="bubble"]',
                    '[class*="chat-turn"]',
                    '[class*="chat-item"]',
                    '[class*="response-container"]',
                    '[class*="prompt-container"]',
                    '[data-message-author-role]',
                    '[data-testid*="conversation-turn"]',
                    '.user-message',
                    '.bot-message',
                    '.assistant-message',
                    '.model-response',
                    'blockquote'
                ];

                bubbleSelectors.forEach(sel => {
                    document.querySelectorAll(sel).forEach(el => {
                        if (el.innerText && el.innerText.trim().length > 10 && el !== document.body && el !== document.documentElement) {
                            attachCopyBtn(el, false);
                        }
                    });
                });

                // Target code blocks specifically
                document.querySelectorAll('pre, code.hljs, div.highlight').forEach(el => {
                    if (el.innerText && el.innerText.trim().length > 10) {
                        attachCopyBtn(el, true);
                    }
                });

                return injectedCount;
            })();
        """.trimIndent()

        webView.evaluateJavascript(script) { result ->
            val count = result?.replace("\"", "")?.toIntOrNull() ?: 0
            onResult(count)
        }
    }

    /**
     * Extracts full page text in clean plain format.
     */
    fun extractFullText(webView: WebView, onTextExtracted: (String) -> Unit) {
        val script = """
            (function() {
                return document.body.innerText || document.body.textContent || '';
            })();
        """.trimIndent()

        webView.evaluateJavascript(script) { result ->
            val clean = if (result != null && result.length > 2 && result.startsWith("\"") && result.endsWith("\"")) {
                result.substring(1, result.length - 1)
                    .replace("\\n", "\n")
                    .replace("\\t", "\t")
                    .replace("\\\"", "\"")
                    .replace("\\\\", "\\")
            } else {
                result ?: ""
            }
            onTextExtracted(clean)
        }
    }

    /**
     * Smoothly scrolls to top or bottom of the active webpage.
     */
    fun scrollTo(webView: WebView, toTop: Boolean) {
        val yPos = if (toTop) "0" else "document.body.scrollHeight"
        webView.evaluateJavascript("window.scrollTo({ top: $yPos, behavior: 'smooth' });", null)
    }

    /**
     * Quick in-page highlight and find.
     */
    fun findInPage(webView: WebView, query: String, onMatchesFound: (Int) -> Unit = {}) {
        if (query.isBlank()) return
        val script = """
            (function() {
                if (window.find) {
                    return window.find('$query', false, false, true, false, false, false) ? 1 : 0;
                }
                return 0;
            })();
        """.trimIndent()
        webView.evaluateJavascript(script) { res ->
            val found = res?.toIntOrNull() ?: 0
            onMatchesFound(found)
        }
    }
}
