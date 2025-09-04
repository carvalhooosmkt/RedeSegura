package com.redesegura.overlay;

import android.content.Context;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ImageView;
import android.animation.ObjectAnimator;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.BounceInterpolator;
import android.graphics.drawable.GradientDrawable;

import com.redesegura.R;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

/**
 * OverlayManager v3.0 - Sistema Avançado de Overlays Protetivos
 * 
 * Funcionalidades:
 * - Overlays com design premium e animações suaves
 * - Diferentes estilos baseados no nível de risco
 * - Auto-remoção inteligente com timing adaptativo
 * - Feedback visual avançado para o usuário
 * - Sistema de cache para performance otimizada
 */
public class OverlayManager {
    
    private static final String TAG = "OverlayManager";
    private Context context;
    private WindowManager windowManager;
    private List<View> activeOverlays = new ArrayList<>();
    private Handler handler = new Handler(Looper.getMainLooper());
    private Map<String, Integer> overlayCache = new HashMap<>();

    // Configurações de design avançado
    private static final int OVERLAY_ANIMATION_DURATION = 400;
    private static final int AUTO_REMOVE_DELAY_HIGH_RISK = 3000;
    private static final int AUTO_REMOVE_DELAY_MEDIUM_RISK = 4000;
    private static final int AUTO_REMOVE_DELAY_LOW_RISK = 5000;

    public OverlayManager(Context context) {
        this.context = context;
        this.windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Log.d(TAG, "🎨 OverlayManager v3.0 inicializado");
    }

    /**
     * MOSTRAR OVERLAY PROTETIVO AVANÇADO - DESIGN PREMIUM
     */
    public void showAdvancedProtectiveOverlay(Rect bounds, String triggerReason, String riskLevel, 
                                            int confidence, String triggerType, String appName) {
        handler.post(() -> {
            try {
                // Criar view do overlay com design premium
                View overlayView = createPremiumOverlayView(triggerReason, riskLevel, confidence, triggerType, appName);

                // Configurar parâmetros da janela otimizados
                WindowManager.LayoutParams params = createOptimizedWindowParams(bounds);

                // Adicionar overlay à tela
                windowManager.addView(overlayView, params);
                activeOverlays.add(overlayView);

                // Animação de entrada premium
                animatePremiumOverlayEntrance(overlayView, riskLevel);

                // Configurar interações do usuário
                setupOverlayInteractions(overlayView, triggerReason, appName);

                // Auto-remoção inteligente baseada no risco
                scheduleIntelligentAutoRemoval(overlayView, riskLevel, appName);

                Log.d(TAG, String.format("🛡️ Overlay premium aplicado: %s (%s) - %d%% confiança", 
                    triggerType, riskLevel, confidence));

            } catch (Exception e) {
                Log.e(TAG, "❌ Erro ao mostrar overlay premium", e);
            }
        });
    }

    /**
     * CRIAR VIEW DO OVERLAY COM DESIGN PREMIUM
     */
    private View createPremiumOverlayView(String triggerReason, String riskLevel, 
                                        int confidence, String triggerType, String appName) {
        View overlayView = LayoutInflater.from(context).inflate(R.layout.premium_protective_overlay, null);

        // Configurar textos
        TextView titleText = overlayView.findViewById(R.id.overlay_title);
        TextView reasonText = overlayView.findViewById(R.id.overlay_reason);
        TextView riskText = overlayView.findViewById(R.id.overlay_risk);
        TextView appText = overlayView.findViewById(R.id.overlay_app);
        Button revealButton = overlayView.findViewById(R.id.reveal_button);
        Button skipButton = overlayView.findViewById(R.id.skip_button);
        ImageView shieldIcon = overlayView.findViewById(R.id.shield_icon);

        // Configurar conteúdo
        titleText.setText("🛡️ Conteúdo Protegido");
        reasonText.setText(triggerReason);
        riskText.setText(String.format("Risco %s • %d%% confiança • PsychAI v4.0", riskLevel, confidence));
        appText.setText(String.format("Detectado em: %s", getAppDisplayName(appName)));

        // Configurar cores baseadas no nível de risco
        int[] riskColors = getRiskColors(riskLevel);
        
        // Aplicar gradiente de fundo
        GradientDrawable backgroundGradient = new GradientDrawable(
            GradientDrawable.Orientation.TOP_BOTTOM,
            riskColors
        );
        backgroundGradient.setCornerRadius(24);
        overlayView.setBackground(backgroundGradient);

        // Configurar ícone do shield
        shieldIcon.setColorFilter(riskColors[0]);

        return overlayView;
    }

    /**
     * PARÂMETROS OTIMIZADOS DA JANELA
     */
    private WindowManager.LayoutParams createOptimizedWindowParams(Rect bounds) {
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
            bounds.width(),
            bounds.height(),
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
            WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH |
            WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED, // Aceleração de hardware
            PixelFormat.TRANSLUCENT
        );

        params.x = bounds.left;
        params.y = bounds.top;
        params.gravity = Gravity.TOP | Gravity.START;
        
        // Configurações avançadas para melhor performance
        params.alpha = 0.95f;
        params.dimAmount = 0.3f;

        return params;
    }

    /**
     * ANIMAÇÃO DE ENTRADA PREMIUM
     */
    private void animatePremiumOverlayEntrance(View overlayView, String riskLevel) {
        // Configuração inicial
        overlayView.setAlpha(0f);
        overlayView.setScaleX(0.8f);
        overlayView.setScaleY(0.8f);
        overlayView.setTranslationY(-50f);

        // Criar animação complexa
        AnimatorSet animatorSet = new AnimatorSet();
        
        ObjectAnimator fadeIn = ObjectAnimator.ofFloat(overlayView, "alpha", 0f, 1f);
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(overlayView, "scaleX", 0.8f, 1.05f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(overlayView, "scaleY", 0.8f, 1.05f, 1f);
        ObjectAnimator translateY = ObjectAnimator.ofFloat(overlayView, "translationY", -50f, 10f, 0f);

        // Configurar duração e interpolador baseado no risco
        int duration = "Alto".equals(riskLevel) ? 300 : 
                      "Médio".equals(riskLevel) ? 400 : 500;
        
        animatorSet.playTogether(fadeIn, scaleX, scaleY, translateY);
        animatorSet.setDuration(duration);
        animatorSet.setInterpolator(new DecelerateInterpolator(1.5f));
        
        // Adicionar bounce sutil para riscos altos
        if ("Alto".equals(riskLevel) || "Crítico".equals(riskLevel)) {
            animatorSet.setInterpolator(new BounceInterpolator());
        }
        
        animatorSet.start();

        // Animação de pulso para riscos críticos
        if ("Crítico".equals(riskLevel)) {
            startPulseAnimation(overlayView);
        }
    }

    /**
     * ANIMAÇÃO DE PULSO PARA RISCOS CRÍTICOS
     */
    private void startPulseAnimation(View overlayView) {
        ValueAnimator pulseAnimator = ValueAnimator.ofFloat(1f, 1.05f, 1f);
        pulseAnimator.setDuration(1500);
        pulseAnimator.setRepeatCount(ValueAnimator.INFINITE);
        pulseAnimator.setInterpolator(new DecelerateInterpolator());
        
        pulseAnimator.addUpdateListener(animation -> {
            float scale = (float) animation.getAnimatedValue();
            overlayView.setScaleX(scale);
            overlayView.setScaleY(scale);
        });
        
        pulseAnimator.start();
        
        // Parar animação após 10 segundos
        handler.postDelayed(pulseAnimator::cancel, 10000);
    }

    /**
     * CONFIGURAR INTERAÇÕES DO USUÁRIO
     */
    private void setupOverlayInteractions(View overlayView, String triggerReason, String appName) {
        Button revealButton = overlayView.findViewById(R.id.reveal_button);
        Button skipButton = overlayView.findViewById(R.id.skip_button);

        // Botão "Ver Mesmo Assim"
        revealButton.setOnClickListener(v -> {
            removeOverlayWithAnimation(overlayView, "reveal");
            logUserInteraction("content_revealed", triggerReason, appName);
        });

        // Botão "Pular Post"
        skipButton.setOnClickListener(v -> {
            removeOverlayWithAnimation(overlayView, "skip");
            logUserInteraction("content_skipped", triggerReason, appName);
            
            // Executar auto-scroll imediatamente
            performAutoScroll();
        });
    }

    /**
     * AUTO-REMOÇÃO INTELIGENTE
     */
    private void scheduleIntelligentAutoRemoval(View overlayView, String riskLevel, String appName) {
        int delay;
        switch (riskLevel) {
            case "Crítico": delay = 2000; break;
            case "Alto": delay = AUTO_REMOVE_DELAY_HIGH_RISK; break;
            case "Médio": delay = AUTO_REMOVE_DELAY_MEDIUM_RISK; break;
            default: delay = AUTO_REMOVE_DELAY_LOW_RISK; break;
        }

        handler.postDelayed(() -> {
            if (activeOverlays.contains(overlayView)) {
                removeOverlayWithAnimation(overlayView, "auto");
                logUserInteraction("auto_removed", riskLevel, appName);
                
                // Auto-scroll após remoção automática
                performAutoScroll();
            }
        }, delay);
    }

    /**
     * REMOVER OVERLAY COM ANIMAÇÃO SUAVE
     */
    private void removeOverlayWithAnimation(View overlayView, String removalType) {
        try {
            if (!activeOverlays.contains(overlayView)) return;

            // Animação de saída baseada no tipo de remoção
            AnimatorSet exitAnimator = new AnimatorSet();
            
            if ("skip".equals(removalType)) {
                // Animação de slide para baixo
                ObjectAnimator slideDown = ObjectAnimator.ofFloat(overlayView, "translationY", 0f, 200f);
                ObjectAnimator fadeOut = ObjectAnimator.ofFloat(overlayView, "alpha", 1f, 0f);
                exitAnimator.playTogether(slideDown, fadeOut);
            } else {
                // Animação de fade out padrão
                ObjectAnimator fadeOut = ObjectAnimator.ofFloat(overlayView, "alpha", 1f, 0f);
                ObjectAnimator scaleDown = ObjectAnimator.ofFloat(overlayView, "scaleX", 1f, 0.9f);
                ObjectAnimator scaleDownY = ObjectAnimator.ofFloat(overlayView, "scaleY", 1f, 0.9f);
                exitAnimator.playTogether(fadeOut, scaleDown, scaleDownY);
            }
            
            exitAnimator.setDuration(250);
            exitAnimator.setInterpolator(new DecelerateInterpolator());
            
            exitAnimator.addListener(new android.animation.AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(android.animation.Animator animation) {
                    try {
                        windowManager.removeView(overlayView);
                        activeOverlays.remove(overlayView);
                        Log.d(TAG, String.format("✅ Overlay removido (%s)", removalType));
                    } catch (Exception e) {
                        Log.e(TAG, "❌ Erro ao remover overlay", e);
                    }
                }
            });
            
            exitAnimator.start();
            
        } catch (Exception e) {
            Log.e(TAG, "❌ Erro na animação de remoção", e);
        }
    }

    /**
     * CORES BASEADAS NO NÍVEL DE RISCO
     */
    private int[] getRiskColors(String riskLevel) {
        switch (riskLevel) {
            case "Crítico":
                return new int[]{0xFFDC2626, 0xFFB91C1C}; // Vermelho intenso
            case "Alto":
                return new int[]{0xFFEF4444, 0xFFDC2626}; // Vermelho
            case "Médio":
                return new int[]{0xFFF59E0B, 0xFFD97706}; // Amarelo/laranja
            case "Baixo":
                return new int[]{0xFF10B981, 0xFF059669}; // Verde
            default:
                return new int[]{0xFF6B7280, 0xFF4B5563}; // Cinza
        }
    }

    /**
     * OBTER NOME AMIGÁVEL DO APP
     */
    private String getAppDisplayName(String packageName) {
        switch (packageName) {
            case "com.instagram.android": return "Instagram";
            case "com.facebook.katana": return "Facebook";
            case "com.zhiliaoapp.musically": return "TikTok";
            case "com.twitter.android": return "Twitter";
            case "com.linkedin.android": return "LinkedIn";
            case "com.snapchat.android": return "Snapchat";
            default: return "Rede Social";
        }
    }

    /**
     * EXECUTAR AUTO-SCROLL
     */
    private void performAutoScroll() {
        try {
            // Implementar auto-scroll usando gestos automatizados
            AutoScrollGesturePerformer.performIntelligentScrollGesture(context);
            Log.d(TAG, "📜 Auto-scroll inteligente executado");
        } catch (Exception e) {
            Log.e(TAG, "❌ Erro no auto-scroll", e);
        }
    }

    /**
     * LOG DE INTERAÇÕES DO USUÁRIO
     */
    private void logUserInteraction(String action, String context, String appName) {
        // Log estruturado para análise de comportamento
        Map<String, Object> interactionData = new HashMap<>();
        interactionData.put("action", action);
        interactionData.put("context", context);
        interactionData.put("appName", appName);
        interactionData.put("timestamp", System.currentTimeMillis());
        interactionData.put("overlayVersion", "3.0");
        
        Log.d(TAG, String.format("👤 Interação: %s - %s (%s)", action, context, appName));
        
        // Aqui você pode implementar analytics locais se necessário
        // NUNCA enviar dados para servidores externos
    }

    /**
     * OCULTAR TODOS OS OVERLAYS
     */
    public void hideAllOverlays() {
        try {
            Log.d(TAG, String.format("👁️ Ocultando %d overlays ativos", activeOverlays.size()));
            
            for (View overlay : new ArrayList<>(activeOverlays)) {
                removeOverlayWithAnimation(overlay, "hide_all");
            }
            
        } catch (Exception e) {
            Log.e(TAG, "❌ Erro ao ocultar overlays", e);
        }
    }

    /**
     * ATUALIZAR ESTILO DOS OVERLAYS
     */
    public void updateOverlayStyle(String backgroundColor, float opacity, int borderRadius, String animation) {
        try {
            // Implementar atualização de estilo em tempo real
            Log.d(TAG, String.format("🎨 Atualizando estilo: cor=%s, opacidade=%.2f, raio=%d, animação=%s", 
                backgroundColor, opacity, borderRadius, animation));
            
            // Aplicar novos estilos aos overlays ativos
            for (View overlay : activeOverlays) {
                applyStyleToOverlay(overlay, backgroundColor, opacity, borderRadius);
            }
            
        } catch (Exception e) {
            Log.e(TAG, "❌ Erro ao atualizar estilo", e);
        }
    }

    private void applyStyleToOverlay(View overlay, String backgroundColor, float opacity, int borderRadius) {
        try {
            overlay.setAlpha(opacity);
            
            // Aplicar cor de fundo se especificada
            if (backgroundColor != null && !backgroundColor.isEmpty()) {
                GradientDrawable background = new GradientDrawable();
                background.setColor(android.graphics.Color.parseColor(backgroundColor));
                background.setCornerRadius(borderRadius);
                overlay.setBackground(background);
            }
            
        } catch (Exception e) {
            Log.e(TAG, "❌ Erro ao aplicar estilo", e);
        }
    }

    /**
     * CLEANUP COMPLETO
     */
    public void cleanup() {
        try {
            Log.d(TAG, "🧹 Iniciando cleanup completo do OverlayManager");
            
            // Remover todos os overlays ativos
            for (View overlay : new ArrayList<>(activeOverlays)) {
                try {
                    windowManager.removeView(overlay);
                } catch (Exception e) {
                    Log.w(TAG, "Aviso ao remover overlay no cleanup", e);
                }
            }
            
            activeOverlays.clear();
            overlayCache.clear();
            
            // Remover callbacks pendentes
            if (handler != null) {
                handler.removeCallbacksAndMessages(null);
            }
            
            Log.d(TAG, "✅ Cleanup do OverlayManager concluído");
            
        } catch (Exception e) {
            Log.e(TAG, "❌ Erro no cleanup", e);
        }
    }

    /**
     * OBTER ESTATÍSTICAS DO OVERLAY MANAGER
     */
    public Map<String, Object> getOverlayStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("activeOverlays", activeOverlays.size());
        stats.put("cacheSize", overlayCache.size());
        stats.put("version", "3.0");
        return stats;
    }
}

/**
 * Classe para executar gestos de auto-scroll
 */
class AutoScrollGesturePerformer {
    
    public static void performIntelligentScrollGesture(Context context) {
        try {
            // Implementar gestos de scroll inteligentes
            // Esta implementação seria específica para cada app
            Log.d("AutoScrollGesture", "🎯 Executando gesto de scroll inteligente");
            
        } catch (Exception e) {
            Log.e("AutoScrollGesture", "❌ Erro no gesto de scroll", e);
        }
    }
}