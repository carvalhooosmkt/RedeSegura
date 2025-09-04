package com.redesegura.accessibility;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * RedeSegura Accessibility Service - Versão 3.0
 * Serviço de acessibilidade avançado para proteção mental em redes sociais
 * 
 * Funcionalidades:
 * - Análise em tempo real de conteúdo de redes sociais
 * - Detecção de triggers psicológicos baseada em IA
 * - Aplicação de overlays protetivos instantâneos
 * - Auto-scroll inteligente para pular conteúdo tóxico
 * - Processamento local com zero coleta de dados
 */
public class RedeSeguraAccessibilityService extends AccessibilityService {
    
    private static final String TAG = "RedeSeguraAccessibility";
    private static RedeSeguraAccessibilityService instance;
    
    private ExecutorService executor = Executors.newFixedThreadPool(3);
    private Handler mainHandler = new Handler(Looper.getMainLooper());
    
    // Apps de redes sociais para monitorar
    private static final List<String> SOCIAL_MEDIA_APPS = Arrays.asList(
        "com.instagram.android",
        "com.facebook.katana", 
        "com.zhiliaoapp.musically", // TikTok
        "com.twitter.android",
        "com.linkedin.android",
        "com.snapchat.android",
        "com.facebook.orca", // Messenger
        "com.whatsapp", // WhatsApp Status
        "com.pinterest" // Pinterest
    );

    private OverlayManager overlayManager;
    private PsychAIEngine aiEngine;
    private AutoScrollController autoScrollController;
    private ContentAnalyzer contentAnalyzer;
    
    private boolean isProtectionActive = false;
    private boolean isAnalyzing = false;
    private long lastAnalysisTime = 0;
    private int analysisCount = 0;
    
    // Configurações avançadas
    private int protectionLevel = 85;
    private boolean autoScrollEnabled = true;
    private boolean realTimeAnalysis = true;
    private boolean learningMode = true;

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        instance = this;
        
        Log.d(TAG, "🚀 RedeSegura Accessibility Service v3.0 conectado");
        
        // Configurar serviço com configurações otimizadas
        AccessibilityServiceInfo serviceInfo = getServiceInfo();
        serviceInfo.eventTypes = AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED |
                                AccessibilityEvent.TYPE_VIEW_SCROLLED |
                                AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED |
                                AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED;
        serviceInfo.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;
        serviceInfo.flags = AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS |
                           AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS |
                           AccessibilityServiceInfo.FLAG_REQUEST_ENHANCED_WEB_ACCESSIBILITY;
        serviceInfo.notificationTimeout = 0; // Análise instantânea
        setServiceInfo(serviceInfo);
        
        // Inicializar componentes avançados
        overlayManager = new OverlayManager(this);
        aiEngine = new PsychAIEngine();
        autoScrollController = new AutoScrollController(this);
        contentAnalyzer = new ContentAnalyzer();
        
        Log.d(TAG, "✅ Todos os componentes RedeSegura inicializados");
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (!isProtectionActive || !realTimeAnalysis) return;
        
        String packageName = event.getPackageName() != null ? 
                            event.getPackageName().toString() : "";
                            
        // Verificar se é uma rede social monitorada
        if (!SOCIAL_MEDIA_APPS.contains(packageName)) return;
        
        // Evitar análise excessiva (throttling inteligente)
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastAnalysisTime < 500) return; // Máximo 2 análises por segundo
        
        lastAnalysisTime = currentTime;
        analysisCount++;
        
        // Processar em thread separada para não bloquear UI
        executor.execute(() -> {
            try {
                analyzeContentInRealTime(event, packageName);
            } catch (Exception e) {
                Log.e(TAG, "❌ Erro na análise de conteúdo", e);
            }
        });
    }

    /**
     * ANÁLISE DE CONTEÚDO EM TEMPO REAL - CORE FUNCTION
     */
    private void analyzeContentInRealTime(AccessibilityEvent event, String packageName) {
        if (isAnalyzing) return; // Evitar análises simultâneas
        
        isAnalyzing = true;
        long startTime = System.currentTimeMillis();
        
        AccessibilityNodeInfo rootNode = event.getSource();
        if (rootNode == null) {
            isAnalyzing = false;
            return;
        }

        try {
            // Extrair todo o conteúdo textual da tela
            StringBuilder allText = new StringBuilder();
            List<AccessibilityNodeInfo> postElements = contentAnalyzer.extractAllContent(rootNode, allText);
            
            String contentText = allText.toString().trim();
            if (contentText.isEmpty() || contentText.length() < 10) {
                isAnalyzing = false;
                return;
            }

            Log.d(TAG, String.format("🔍 Analisando %s: %s", 
                packageName, contentText.substring(0, Math.min(100, contentText.length()))));

            // Análise de IA psicológica avançada
            AIAnalysisResult analysis = aiEngine.analyzeContentAdvanced(contentText, packageName);
            
            if (analysis.shouldBlock) {
                Log.d(TAG, String.format("🚫 Conteúdo tóxico detectado! Score: %d, Tipo: %s, Confiança: %d%%", 
                    analysis.toxicityScore, analysis.triggerType, analysis.confidence));
                
                // Aplicar proteção em múltiplas camadas
                applyAdvancedProtection(postElements, analysis, packageName);
                
                // Notificar React Native
                notifyContentBlocked(analysis, packageName);
                
                // Auto-scroll inteligente se habilitado
                if (autoScrollEnabled && analysis.toxicityScore > 50) {
                    scheduleAutoScroll(packageName, analysis.riskLevel);
                }
            } else {
                Log.d(TAG, String.format("✅ Conteúdo seguro - Score: %d", analysis.toxicityScore));
            }
            
            long processingTime = System.currentTimeMillis() - startTime;
            Log.d(TAG, String.format("⚡ Análise completa em %dms", processingTime));
            
        } catch (Exception e) {
            Log.e(TAG, "❌ Erro crítico na análise", e);
        } finally {
            if (rootNode != null) {
                rootNode.recycle();
            }
            isAnalyzing = false;
        }
    }

    /**
     * APLICAR PROTEÇÃO AVANÇADA EM MÚLTIPLAS CAMADAS
     */
    private void applyAdvancedProtection(List<AccessibilityNodeInfo> postElements, 
                                       AIAnalysisResult analysis, String packageName) {
        mainHandler.post(() -> {
            try {
                for (AccessibilityNodeInfo postElement : postElements) {
                    Rect bounds = new Rect();
                    postElement.getBoundsInScreen(bounds);
                    
                    // Aplicar overlay protetivo com design avançado
                    overlayManager.showAdvancedProtectiveOverlay(
                        bounds, 
                        analysis.triggerReason, 
                        analysis.riskLevel,
                        analysis.confidence,
                        analysis.triggerType,
                        packageName
                    );
                    
                    Log.d(TAG, String.format("🛡️ Overlay aplicado: %s (%d%% confiança)", 
                        analysis.triggerType, analysis.confidence));
                }
                
            } catch (Exception e) {
                Log.e(TAG, "❌ Erro ao aplicar proteção", e);
            }
        });
    }

    /**
     * AUTO-SCROLL INTELIGENTE BASEADO NO APP E RISCO
     */
    private void scheduleAutoScroll(String packageName, String riskLevel) {
        // Delay baseado no nível de risco
        int delay = "Alto".equals(riskLevel) ? 2000 : 
                   "Médio".equals(riskLevel) ? 3000 : 4000;
        
        mainHandler.postDelayed(() -> {
            try {
                boolean scrolled = autoScrollController.performIntelligentScroll(packageName);
                if (scrolled) {
                    Log.d(TAG, String.format("📜 Auto-scroll executado para %s", packageName));
                }
            } catch (Exception e) {
                Log.e(TAG, "❌ Erro no auto-scroll", e);
            }
        }, delay);
    }

    private void notifyContentBlocked(AIAnalysisResult analysis, String appName) {
        try {
            WritableMap params = Arguments.createMap();
            params.putString("appName", appName);
            params.putString("triggerReason", analysis.triggerReason);
            params.putString("triggerType", analysis.triggerType);
            params.putString("riskLevel", analysis.riskLevel);
            params.putDouble("confidence", analysis.confidence);
            params.putDouble("toxicityScore", analysis.toxicityScore);
            params.putString("timestamp", String.valueOf(System.currentTimeMillis()));
            params.putDouble("processingTime", analysis.processingTime);
            params.putString("aiVersion", "PsychAI v4.0");
            
            // Enviar para React Native
            sendEventToReactNative("onContentBlocked", params);
            
        } catch (Exception e) {
            Log.e(TAG, "❌ Erro ao notificar React Native", e);
        }
    }

    private void sendEventToReactNative(String eventName, WritableMap params) {
        try {
            ReactApplicationContext reactContext = getReactApplicationContext();
            if (reactContext != null) {
                reactContext
                    .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                    .emit(eventName, params);
            }
        } catch (Exception e) {
            Log.e(TAG, "❌ Erro ao enviar evento para React Native", e);
        }
    }

    private ReactApplicationContext getReactApplicationContext() {
        // Implementar obtenção do contexto React Native
        return ReactNativeContextHolder.getReactApplicationContext();
    }

    // MÉTODOS PARA CONTROLE EXTERNO
    
    public static RedeSeguraAccessibilityService getInstance() {
        return instance;
    }

    public void setProtectionActive(boolean active) {
        this.isProtectionActive = active;
        Log.d(TAG, String.format("🛡️ Proteção %s", active ? "ATIVADA" : "DESATIVADA"));
        
        if (active) {
            // Iniciar análise em tempo real
            startRealTimeAnalysis();
        } else {
            // Parar análise e limpar overlays
            stopRealTimeAnalysis();
            overlayManager.hideAllOverlays();
        }
    }

    public void updateAISettings(String settingsJson) {
        try {
            if (aiEngine != null) {
                aiEngine.updateConfigurationFromJson(settingsJson);
                Log.d(TAG, "🧠 Configurações da IA atualizadas");
            }
        } catch (Exception e) {
            Log.e(TAG, "❌ Erro ao atualizar configurações da IA", e);
        }
    }

    public void setProtectionLevel(int level) {
        this.protectionLevel = Math.max(25, Math.min(100, level));
        if (aiEngine != null) {
            aiEngine.setSensitivity(this.protectionLevel);
        }
        Log.d(TAG, String.format("🎯 Nível de proteção definido: %d%%", this.protectionLevel));
    }

    public void setAutoScrollEnabled(boolean enabled) {
        this.autoScrollEnabled = enabled;
        Log.d(TAG, String.format("📜 Auto-scroll %s", enabled ? "ATIVADO" : "DESATIVADO"));
    }

    public boolean isServiceRunning() {
        return isProtectionActive && instance != null;
    }

    public int getAnalysisCount() {
        return analysisCount;
    }

    private void startRealTimeAnalysis() {
        realTimeAnalysis = true;
        Log.d(TAG, "🚀 Análise em tempo real iniciada");
    }

    private void stopRealTimeAnalysis() {
        realTimeAnalysis = false;
        Log.d(TAG, "⏹️ Análise em tempo real parada");
    }

    @Override
    public void onInterrupt() {
        Log.d(TAG, "⚠️ Serviço de acessibilidade interrompido");
        isProtectionActive = false;
        realTimeAnalysis = false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        
        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
        }
        
        if (overlayManager != null) {
            overlayManager.cleanup();
        }
        
        if (autoScrollController != null) {
            autoScrollController.cleanup();
        }
        
        instance = null;
        
        Log.d(TAG, "🗑️ Serviço de acessibilidade destruído");
    }
}

/**
 * Classe para análise avançada de conteúdo
 */
class ContentAnalyzer {
    
    public List<AccessibilityNodeInfo> extractAllContent(AccessibilityNodeInfo rootNode, StringBuilder textBuilder) {
        List<AccessibilityNodeInfo> postElements = new ArrayList<>();
        
        if (rootNode == null) return postElements;

        try {
            // Extrair texto de forma recursiva e inteligente
            extractTextRecursively(rootNode, textBuilder, postElements);
            
        } catch (Exception e) {
            Log.e("ContentAnalyzer", "Erro na extração de conteúdo", e);
        }
        
        return postElements;
    }

    private void extractTextRecursively(AccessibilityNodeInfo node, StringBuilder textBuilder, 
                                      List<AccessibilityNodeInfo> postElements) {
        if (node == null) return;

        // Extrair texto do nó atual
        CharSequence text = node.getText();
        if (text != null && text.length() > 0) {
            textBuilder.append(text).append(" ");
        }
        
        CharSequence contentDescription = node.getContentDescription();
        if (contentDescription != null && contentDescription.length() > 0) {
            textBuilder.append(contentDescription).append(" ");
        }

        // Identificar elementos de post baseado na estrutura do app
        if (isPostElement(node)) {
            postElements.add(node);
        }

        // Recursivamente extrair texto dos filhos
        int childCount = node.getChildCount();
        for (int i = 0; i < childCount; i++) {
            AccessibilityNodeInfo child = node.getChild(i);
            if (child != null) {
                extractTextRecursively(child, textBuilder, postElements);
                // Não reciclar aqui - será feito no final
            }
        }
    }

    private boolean isPostElement(AccessibilityNodeInfo node) {
        // Lógica para identificar elementos de post baseado em:
        // - IDs de recursos
        // - Classes de view
        // - Estrutura hierárquica
        // - Dimensões do elemento
        
        String resourceId = node.getViewIdResourceName();
        String className = node.getClassName() != null ? node.getClassName().toString() : "";
        
        // Padrões comuns de posts em redes sociais
        return (resourceId != null && (
            resourceId.contains("post") ||
            resourceId.contains("feed") ||
            resourceId.contains("item") ||
            resourceId.contains("card")
        )) || (
            className.contains("RecyclerView") ||
            className.contains("ListView") ||
            className.contains("ScrollView")
        );
    }
}

/**
 * Controlador de auto-scroll inteligente
 */
class AutoScrollController {
    
    private AccessibilityService service;
    private Handler handler = new Handler(Looper.getMainLooper());
    
    public AutoScrollController(AccessibilityService service) {
        this.service = service;
    }

    public boolean performIntelligentScroll(String packageName) {
        try {
            AccessibilityNodeInfo rootNode = service.getRootInActiveWindow();
            if (rootNode == null) return false;

            // Scroll específico por app
            switch (packageName) {
                case "com.instagram.android":
                    return performInstagramScroll(rootNode);
                case "com.facebook.katana":
                    return performFacebookScroll(rootNode);
                case "com.zhiliaoapp.musically": // TikTok
                    return performTikTokScroll(rootNode);
                case "com.twitter.android":
                    return performTwitterScroll(rootNode);
                default:
                    return performGenericScroll(rootNode);
            }
            
        } catch (Exception e) {
            Log.e("AutoScrollController", "Erro no auto-scroll", e);
            return false;
        }
    }

    private boolean performInstagramScroll(AccessibilityNodeInfo rootNode) {
        // Scroll específico para Instagram
        return rootNode.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD);
    }

    private boolean performFacebookScroll(AccessibilityNodeInfo rootNode) {
        // Scroll específico para Facebook
        return rootNode.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD);
    }

    private boolean performTikTokScroll(AccessibilityNodeInfo rootNode) {
        // Scroll específico para TikTok (swipe up)
        return rootNode.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD);
    }

    private boolean performTwitterScroll(AccessibilityNodeInfo rootNode) {
        // Scroll específico para Twitter
        return rootNode.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD);
    }

    private boolean performGenericScroll(AccessibilityNodeInfo rootNode) {
        // Scroll genérico para outros apps
        return rootNode.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD);
    }

    public void cleanup() {
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
    }
}

/**
 * Resultado da análise de IA
 */
class AIAnalysisResult {
    public final int toxicityScore;
    public final int comparisonLevel;
    public final int anxietyLevel;
    public final int depressionRisk;
    public final int bodyImageRisk;
    public final List<String> foundTriggers;
    public final boolean shouldBlock;
    public final int confidence;
    public final String triggerType;
    public final String triggerReason;
    public final String riskLevel;
    public final long processingTime;
    public final List<String> contextualFactors;

    public AIAnalysisResult(int toxicityScore, int comparisonLevel, int anxietyLevel,
                           int depressionRisk, int bodyImageRisk, List<String> foundTriggers,
                           boolean shouldBlock, int confidence, String triggerType,
                           String triggerReason, String riskLevel, long processingTime,
                           List<String> contextualFactors) {
        this.toxicityScore = toxicityScore;
        this.comparisonLevel = comparisonLevel;
        this.anxietyLevel = anxietyLevel;
        this.depressionRisk = depressionRisk;
        this.bodyImageRisk = bodyImageRisk;
        this.foundTriggers = foundTriggers;
        this.shouldBlock = shouldBlock;
        this.confidence = confidence;
        this.triggerType = triggerType;
        this.triggerReason = triggerReason;
        this.riskLevel = riskLevel;
        this.processingTime = processingTime;
        this.contextualFactors = contextualFactors;
    }
}

/**
 * Registry para manter referência do serviço
 */
class ReactNativeContextHolder {
    private static ReactApplicationContext reactContext;
    
    public static void setReactApplicationContext(ReactApplicationContext context) {
        reactContext = context;
    }
    
    public static ReactApplicationContext getReactApplicationContext() {
        return reactContext;
    }
}

/**
 * Registry para o serviço de acessibilidade
 */
class RedeSeguraAccessibilityServiceRegistry {
    
    public static RedeSeguraAccessibilityService getInstance() {
        return RedeSeguraAccessibilityService.getInstance();
    }
}