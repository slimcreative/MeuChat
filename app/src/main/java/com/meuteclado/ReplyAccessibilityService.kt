package com.meuteclado

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.view.accessibility.AccessibilityWindowInfo

/**
 * Coloca o texto no campo de mensagem do app aberto e, se pedido, toca no botão de enviar.
 * Se não estiver ativo, o app copia a resposta para a área de transferência.
 */
class ReplyAccessibilityService : AccessibilityService() {

    companion object {
        @Volatile
        var instance: ReplyAccessibilityService? = null
            private set

        /** Só coloca o texto no campo. */
        fun insert(text: String): Boolean = instance?.setText(text) ?: false

        /**
         * Coloca o texto e toca em enviar. Devolve false se nem conseguiu colocar o texto.
         * onDone(true) = enviou; onDone(false) = ficou no campo, falta tocar em enviar.
         */
        fun send(text: String, onDone: (Boolean) -> Unit): Boolean {
            val svc = instance ?: return false
            if (!svc.setText(text)) return false
            svc.main.postDelayed({ onDone(svc.clickSend()) }, 350)
            return true
        }
    }

    private val main = Handler(Looper.getMainLooper())

    override fun onServiceConnected() {
        instance = this
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}

    override fun onInterrupt() {}

    override fun onUnbind(intent: Intent?): Boolean {
        instance = null
        return super.onUnbind(intent)
    }

    /** Janela do app de conversa: nunca a nossa própria telinha. */
    private fun appRoot(): AccessibilityNodeInfo? {
        val active = rootInActiveWindow
        if (active != null && active.packageName?.toString() != packageName) return active
        val list = try { windows } catch (e: Exception) { emptyList() }
        for (w in list) {
            if (w.type != AccessibilityWindowInfo.TYPE_APPLICATION) continue
            val r = w.root ?: continue
            if (r.packageName?.toString() != packageName) return r
        }
        return null
    }

    private fun setText(text: String): Boolean {
        val root = appRoot() ?: return false
        val focused = root.findFocus(AccessibilityNodeInfo.FOCUS_INPUT)
        val node = (if (focused != null && focused.isEditable) focused else null)
            ?: find(root) { it.isEditable && it.isVisibleToUser }
            ?: return false
        val args = Bundle().apply {
            putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text)
        }
        return node.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, args)
    }

    private fun clickSend(): Boolean {
        val root = appRoot() ?: return false
        val usable = { n: AccessibilityNodeInfo ->
            n.isVisibleToUser && n.isEnabled && !n.isEditable
        }
        val label = { n: AccessibilityNodeInfo ->
            (n.contentDescription ?: n.text ?: "").toString().trim().lowercase()
        }
        val id = { n: AccessibilityNodeInfo ->
            (n.viewIdResourceName ?: "").substringAfterLast('/').lowercase()
        }
        val node =
            find(root) { usable(it) && (label(it) == "enviar" || label(it) == "send") }
                ?: find(root) { usable(it) && (id(it) == "send" || id(it).endsWith("send") || id(it).contains("send_b") || id(it).contains("btn_send")) }
                ?: find(root) { usable(it) && (label(it).startsWith("enviar") || label(it).startsWith("send")) }
                ?: return false
        return clickUp(node)
    }

    /** Clica no nó ou no primeiro ancestral clicável. */
    private fun clickUp(start: AccessibilityNodeInfo): Boolean {
        var n: AccessibilityNodeInfo? = start
        var hops = 0
        while (n != null && hops < 4) {
            if (n.isClickable) return n.performAction(AccessibilityNodeInfo.ACTION_CLICK)
            n = n.parent
            hops++
        }
        return false
    }

    private fun find(
        root: AccessibilityNodeInfo,
        match: (AccessibilityNodeInfo) -> Boolean
    ): AccessibilityNodeInfo? {
        val queue = ArrayDeque<AccessibilityNodeInfo>()
        queue.add(root)
        var seen = 0
        while (queue.isNotEmpty() && seen < 3000) {
            val n = queue.removeFirst()
            seen++
            if (match(n)) return n
            for (i in 0 until n.childCount) n.getChild(i)?.let { queue.add(it) }
        }
        return null
    }
}
