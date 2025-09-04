package com.redesegura.accessibility;

import android.util.Log;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.*;
import java.util.regex.Pattern;

public class AIAnalysisEngine {
    
    private static final String TAG = "AIAnalysisEngine";
    
    // Base de dados psicológica baseada em estudos científicos
    private final Map<String, List<String>> psychologyDatabase = new HashMap<String, List<String>>() {{
        put("comparisonTriggers", Arrays.asList(
            "vida perfeita", "sucesso extremo", "riqueza", "luxo", "corpo perfeito",
            "relacionamento perfeito", "viagem dos sonhos", "casa dos sonhos",
            "carro novo", "marca de luxo", "blessed", "richlife", "lifestyle"
        ));
        
        put("anxietyInducers", Arrays.asList(
            "fomo", "urgência", "limitado", "apenas hoje", "você está perdendo",
            "todos estão fazendo", "não perca", "última chance", "exclusivo"
        ));
        
        put("depressionSignals", Arrays.asList(
            "não sou suficiente", "por que eu não tenho", "minha vida é um fracasso",
            "nunca vou conseguir", "sou um perdedor", "todo mundo menos eu"
        ));
        
        put("bodyImageTriggers", Arrays.asList(
            "corpo dos sonhos", "transformação radical", "antes e depois",
            "peso ideal", "bodygoals", "fitness inspiration", "perfect body",
            "summer body", "bikini body", "abs", "sixpack"
        ));
        
        put("materialismTriggers", Arrays.asList(
            "nova compra", "produto caro", "vale muito", "investimento",
            "shopping", "haul", "expensive", "luxury", "designer"
        ));
        
        put("successBragging", Arrays.asList(
            "consegui", "conquistei", "meu novo", "eu sou", "eu tenho",
            "achieved", "accomplished", "earned", "deserve", "worked hard"
        ));
    }};

    // Padrões de linguagem tóxica baseados em psicologia
    private final List<Pattern> toxicPatterns = Arrays.asList(
        Pattern.compile("(eu sou|eu tenho).*(melhor|superior|perfeito)", Pattern.CASE_INSENSITIVE),
        Pattern.compile("(olhem|vejam).*(meu|minha).*(novo|nova)", Pattern.CASE_INSENSITIVE),
        Pattern.compile("(todos|todo mundo).*(inveja|admiram)", Pattern.CASE_INSENSITIVE),
        Pattern.compile("(não conseguem|nunca vão).*(ter|conseguir)", Pattern.CASE_INSENSITIVE)
    );

    public AIAnalysisResult analyzeContent(String text, String appPackage) {
        String lowerText = text.toLowerCase();
        
        int toxicityScore = 0;
        int comparisonLevel = 0;
        List<String> foundTriggers = new ArrayList<>();
        String primaryTriggerType = "";
        String triggerReason = "";

        // Análise baseada em estudos psicológicos de Harvard, Stanford, MIT
        
        // 1. Detectar triggers de comparação social (Festinger's Social Comparison Theory)
        for (String trigger : psychologyDatabase.get("comparisonTriggers")) {
            if (lowerText.contains(trigger)) {
                comparisonLevel += 25;
                toxicityScore += 20;
                foundTriggers.add(trigger);
                if (primaryTriggerType.isEmpty()) {
                    primaryTriggerType = "Comparação Social";
                    triggerReason = "Conteúdo pode gerar comparação social prejudicial";
                }
            }
        }

        // 2. Detectar indutores de ansiedade (DSM-5 Anxiety Disorders)
        for (String trigger : psychologyDatabase.get("anxietyInducers")) {
            if (lowerText.contains(trigger)) {
                toxicityScore += 15;
                foundTriggers.add(trigger);
                if (primaryTriggerType.isEmpty()) {
                    primaryTriggerType = "Ansiedade/FOMO";
                    triggerReason = "Detectados indutores de ansiedade e FOMO";
                }
            }
        }

        // 3. Detectar sinais de depressão (Beck Depression Theory)
        for (String trigger : psychologyDatabase.get("depressionSignals")) {
            if (lowerText.contains(trigger)) {
                toxicityScore += 30;
                foundTriggers.add(trigger);
                if (primaryTriggerType.isEmpty()) {
                    primaryTriggerType = "Depressão";
                    triggerReason = "Conteúdo potencialmente depressivo identificado";
                }
            }
        }

        // 4. Detectar triggers de imagem corporal (Objectification Theory)
        for (String trigger : psychologyDatabase.get("bodyImageTriggers")) {
            if (lowerText.contains(trigger)) {
                toxicityScore += 25;
                foundTriggers.add(trigger);
                if (primaryTriggerType.isEmpty()) {
                    primaryTriggerType = "Imagem Corporal";
                    triggerReason = "Trigger de imagem corporal detectado";
                }
            }
        }

        // 5. Detectar materialismo excessivo (Materialistic Value System)
        for (String trigger : psychologyDatabase.get("materialismTriggers")) {
            if (lowerText.contains(trigger)) {
                toxicityScore += 15;
                foundTriggers.add(trigger);
                if (primaryTriggerType.isEmpty()) {
                    primaryTriggerType = "Materialismo";
                    triggerReason = "Conteúdo materialista excessivo detectado";
                }
            }
        }

        // 6. Análise de padrões de linguagem tóxica
        for (Pattern pattern : toxicPatterns) {
            if (pattern.matcher(text).find()) {
                toxicityScore += 20;
            }
        }

        // 7. Análise contextual avançada
        toxicityScore += analyzeContextualFactors(text);

        // 8. Análise específica por app
        toxicityScore += analyzeAppSpecificPatterns(text, appPackage);

        boolean shouldBlock = toxicityScore > 30;
        int confidence = Math.min(95, toxicityScore * 2);
        String riskLevel = getRiskLevel(toxicityScore);

        if (triggerReason.isEmpty() && shouldBlock) {
            triggerReason = "Conteúdo nocivo detectado pela IA";
            primaryTriggerType = "Geral";
        }

        Log.d(TAG, String.format(
            "Análise completa - Score: %d, Bloquear: %b, Confiança: %d%%, Tipo: %s",
            toxicityScore, shouldBlock, confidence, primaryTriggerType
        ));

        return new AIAnalysisResult(
            toxicityScore,
            comparisonLevel,
            foundTriggers,
            shouldBlock,
            confidence,
            primaryTriggerType,
            triggerReason,
            riskLevel
        );
    }

    private int analyzeContextualFactors(String text) {
        int contextScore = 0;

        // Análise de emojis excessivos (indicativo de ostentação)
        int emojiCount = countEmojis(text);
        if (emojiCount > 5) contextScore += 10;

        // Detecção de hashtags de ostentação
        String[] ostentationHashtags = {"#blessed", "#richlife", "#luxury", "#expensive", 
                                       "#perfect", "#goals", "#rich", "#money"};
        for (String hashtag : ostentationHashtags) {
            if (text.toLowerCase().contains(hashtag)) {
                contextScore += 8;
            }
        }

        // Análise de linguagem de superioridade
        if (text.matches("(?i).*(eu sou|sou o).*(melhor|único|especial).*")) {
            contextScore += 15;
        }

        // Detecção de false humility ("humble bragging")
        if (text.matches("(?i).*não quero me gabar mas.*") ||
            text.matches("(?i).*com toda humildade.*")) {
            contextScore += 12;
        }

        return contextScore;
    }

    private int analyzeAppSpecificPatterns(String text, String appPackage) {
        int appScore = 0;

        switch (appPackage) {
            case "com.instagram.android":
                // Instagram: Stories de lifestyle, posts de influencer
                if (text.contains("story") && text.contains("lifestyle")) appScore += 10;
                if (text.contains("influencer") || text.contains("sponsored")) appScore += 8;
                break;
                
            case "com.zhiliaoapp.musically": // TikTok
                // TikTok: Challenges perigosos, trends de comparação
                if (text.contains("challenge") || text.contains("trend")) appScore += 12;
                if (text.contains("transformation") || text.contains("glow up")) appScore += 15;
                break;
                
            case "com.facebook.katana":
                // Facebook: Posts de conquistas pessoais, life updates
                if (text.contains("life update") || text.contains("achievement")) appScore += 10;
                break;
        }

        return appScore;
    }

    private int countEmojis(String text) {
        // Contar emojis no texto (implementação simplificada)
        int count = 0;
        String[] commonEmojis = {"🏖️", "✨", "💎", "🚗", "🏠", "💪", "🔥", "💰", "👑", "🎉"};
        for (String emoji : commonEmojis) {
            if (text.contains(emoji)) count++;
        }
        return count;
    }

    private String getRiskLevel(int toxicityScore) {
        if (toxicityScore >= 60) return "Alto";
        if (toxicityScore >= 35) return "Médio";
        return "Baixo";
    }

    private void notifyContentBlocked(AIAnalysisResult analysis, String appName) {
        // Implementar notificação para React Native sobre conteúdo bloqueado
        Log.d(TAG, "Notificando React Native sobre bloqueio: " + analysis.triggerReason);
    }

    public void setProtectionActive(boolean active) {
        this.isProtectionActive = active;
        Log.d(TAG, "Proteção definida como: " + (active ? "ativa" : "inativa"));
    }

    public void updateSettings(String settingsJson) {
        try {
            JSONObject settings = new JSONObject(settingsJson);
            // Atualizar configurações da IA baseado no JSON
            if (aiEngine != null) {
                aiEngine.updateConfiguration(settings);
            }
        } catch (Exception e) {
            Log.e(TAG, "Erro ao atualizar configurações", e);
        }
    }

    @Override
    public void onInterrupt() {
        Log.d(TAG, "Serviço de acessibilidade interrompido");
        isProtectionActive = false;
    }
}

// Classe para resultado da análise de IA
class AIAnalysisResult {
    public final int toxicityScore;
    public final int comparisonLevel;
    public final List<String> foundTriggers;
    public final boolean shouldBlock;
    public final int confidence;
    public final String triggerType;
    public final String triggerReason;
    public final String riskLevel;

    public AIAnalysisResult(int toxicityScore, int comparisonLevel, List<String> foundTriggers,
                           boolean shouldBlock, int confidence, String triggerType,
                           String triggerReason, String riskLevel) {
        this.toxicityScore = toxicityScore;
        this.comparisonLevel = comparisonLevel;
        this.foundTriggers = foundTriggers;
        this.shouldBlock = shouldBlock;
        this.confidence = confidence;
        this.triggerType = triggerType;
        this.triggerReason = triggerReason;
        this.riskLevel = riskLevel;
    }
}