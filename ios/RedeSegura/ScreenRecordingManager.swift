import Foundation
import ReplayKit
import UIKit
import CoreML

@objc(ScreenRecordingManager)
class ScreenRecordingManager: NSObject {
    
    private var isRecording = false
    private var aiAnalysisEngine: MentalHealthAI
    private var overlayManager: IOSOverlayManager
    
    override init() {
        self.aiAnalysisEngine = MentalHealthAI()
        self.overlayManager = IOSOverlayManager()
        super.init()
    }
    
    @objc func startScreenAnalysis(_ resolve: @escaping RCTPromiseResolveBlock, 
                                  rejecter reject: @escaping RCTPromiseRejectBlock) {
        
        guard !isRecording else {
            resolve(true)
            return
        }
        
        let recorder = RPScreenRecorder.shared()
        
        // Verificar se screen recording está disponível
        guard recorder.isAvailable else {
            reject("SCREEN_RECORDING_UNAVAILABLE", "Screen recording não disponível", nil)
            return
        }
        
        // Solicitar permissão se necessário
        if !recorder.isRecording {
            recorder.startCapture(handler: { (sampleBuffer, bufferType, error) in
                if let error = error {
                    print("Erro no screen recording: \(error.localizedDescription)")
                    return
                }
                
                // Analisar frame de vídeo
                if bufferType == .video {
                    self.analyzeVideoFrame(sampleBuffer)
                }
                
            }) { (error) in
                if let error = error {
                    reject("SCREEN_RECORDING_ERROR", error.localizedDescription, error)
                } else {
                    self.isRecording = true
                    print("Screen recording iniciado com sucesso")
                    resolve(true)
                }
            }
        } else {
            resolve(true)
        }
    }
    
    @objc func stopScreenAnalysis(_ resolve: @escaping RCTPromiseResolveBlock,
                                 rejecter reject: @escaping RCTPromiseRejectBlock) {
        
        let recorder = RPScreenRecorder.shared()
        
        if recorder.isRecording {
            recorder.stopCapture { (error) in
                if let error = error {
                    reject("STOP_RECORDING_ERROR", error.localizedDescription, error)
                } else {
                    self.isRecording = false
                    print("Screen recording parado")
                    resolve(true)
                }
            }
        } else {
            resolve(true)
        }
    }
    
    private func analyzeVideoFrame(_ sampleBuffer: CMSampleBuffer) {
        // Converter frame de vídeo para análise de texto
        guard let pixelBuffer = CMSampleBufferGetImageBuffer(sampleBuffer) else { return }
        
        DispatchQueue.global(qos: .background).async {
            // Usar OCR para extrair texto da tela
            let extractedText = self.extractTextFromFrame(pixelBuffer)
            
            if !extractedText.isEmpty {
                // Detectar qual app está ativo
                let currentApp = self.getCurrentApp()
                
                if self.isSocialMediaApp(currentApp) {
                    // Analisar conteúdo com IA
                    let analysis = self.aiAnalysisEngine.analyzeContent(extractedText, appContext: currentApp)
                    
                    if analysis.shouldBlock {
                        DispatchQueue.main.async {
                            self.handleToxicContentDetected(analysis, app: currentApp)
                        }
                    }
                }
            }
        }
    }
    
    private func extractTextFromFrame(_ pixelBuffer: CVPixelBuffer) -> String {
        // Implementar OCR usando Vision framework
        // Extrair texto visível na tela
        return TextExtractor.extractText(from: pixelBuffer)
    }
    
    private func getCurrentApp() -> String {
        // Implementar detecção do app ativo
        return AppDetector.getCurrentActiveApp()
    }
    
    private func isSocialMediaApp(_ appIdentifier: String) -> Bool {
        let socialMediaApps = [
            "com.burbn.instagram",
            "com.facebook.Facebook", 
            "com.zhiliaoapp.TikTok",
            "com.twitter.twitter",
            "com.linkedin.LinkedIn"
        ]
        
        return socialMediaApps.contains(appIdentifier)
    }
    
    private func handleToxicContentDetected(_ analysis: AIAnalysisResult, app: String) {
        print("🛡️ Conteúdo tóxico detectado no \(app): \(analysis.triggerReason)")
        
        // Aplicar overlay protetivo (limitado no iOS)
        overlayManager.showProtectiveNotification(
            reason: analysis.triggerReason,
            riskLevel: analysis.riskLevel,
            confidence: analysis.confidence
        )
        
        // Notificar React Native
        notifyReactNativeContentBlocked(analysis, app: app)
        
        // Tentar force scroll (limitado no iOS)
        performAutoScrollIfPossible()
    }
    
    private func notifyReactNativeContentBlocked(_ analysis: AIAnalysisResult, app: String) {
        // Enviar evento para React Native
        let eventData: [String: Any] = [
            "appName": app,
            "triggerReason": analysis.triggerReason,
            "riskLevel": analysis.riskLevel,
            "confidence": analysis.confidence,
            "timestamp": Int(Date().timeIntervalSince1970 * 1000)
        ]
        
        ReactNativeEventEmitter.shared.sendEvent("onContentBlocked", data: eventData)
    }
    
    private func performAutoScrollIfPossible() {
        // No iOS é mais limitado, mas pode usar notificações ou sugestões
        // Implementar alternativas criativas dentro das limitações da Apple
        IOSScrollHelper.suggestScrollAction()
    }
}

// Classe para análise de IA no iOS
class MentalHealthAI {
    
    private let psychologyDatabase: [String: [String]] = [
        "comparisonTriggers": [
            "vida perfeita", "sucesso extremo", "riqueza", "luxo",
            "corpo perfeito", "relacionamento perfeito", "blessed"
        ],
        "anxietyInducers": [
            "fomo", "urgência", "limitado", "você está perdendo",
            "todos estão fazendo", "não perca"
        ],
        "bodyImageTriggers": [
            "corpo dos sonhos", "transformação", "bodygoals",
            "perfect body", "summer body", "fitness inspiration"
        ]
    ]
    
    func analyzeContent(_ text: String, appContext: String) -> AIAnalysisResult {
        let lowerText = text.lowercased()
        var toxicityScore = 0
        var comparisonLevel = 0
        var foundTriggers: [String] = []
        var primaryTriggerType = ""
        var triggerReason = ""
        
        // Análise baseada em estudos psicológicos
        for (category, triggers) in psychologyDatabase {
            for trigger in triggers {
                if lowerText.contains(trigger) {
                    foundTriggers.append(trigger)
                    
                    switch category {
                    case "comparisonTriggers":
                        comparisonLevel += 25
                        toxicityScore += 20
                        if primaryTriggerType.isEmpty {
                            primaryTriggerType = "Comparação Social"
                            triggerReason = "Conteúdo pode gerar comparação social prejudicial"
                        }
                    case "anxietyInducers":
                        toxicityScore += 15
                        if primaryTriggerType.isEmpty {
                            primaryTriggerType = "Ansiedade/FOMO"
                            triggerReason = "Detectados indutores de ansiedade"
                        }
                    case "bodyImageTriggers":
                        toxicityScore += 25
                        if primaryTriggerType.isEmpty {
                            primaryTriggerType = "Imagem Corporal"
                            triggerReason = "Trigger de imagem corporal detectado"
                        }
                    default:
                        break
                    }
                }
            }
        }
        
        // Análise contextual avançada
        toxicityScore += analyzeContextualFactors(text)
        
        let shouldBlock = toxicityScore > 30
        let confidence = min(95, toxicityScore * 2)
        let riskLevel = getRiskLevel(toxicityScore)
        
        if triggerReason.isEmpty && shouldBlock {
            triggerReason = "Conteúdo nocivo detectado pela IA"
            primaryTriggerType = "Geral"
        }
        
        print("🧠 Análise IA - Score: \(toxicityScore), Bloquear: \(shouldBlock), Tipo: \(primaryTriggerType)")
        
        return AIAnalysisResult(
            toxicityScore: toxicityScore,
            comparisonLevel: comparisonLevel,
            foundTriggers: foundTriggers,
            shouldBlock: shouldBlock,
            confidence: confidence,
            triggerType: primaryTriggerType,
            triggerReason: triggerReason,
            riskLevel: riskLevel
        )
    }
    
    private func analyzeContextualFactors(_ text: String) -> Int {
        var contextScore = 0
        
        // Contar emojis de ostentação
        let ostentationEmojis = ["💎", "🏖️", "✨", "🚗", "🏠", "💰", "👑"]
        for emoji in ostentationEmojis {
            if text.contains(emoji) {
                contextScore += 5
            }
        }
        
        // Detectar padrões de linguagem superiority
        if text.range(of: "eu sou.*(melhor|especial|único)", options: [.regularExpression, .caseInsensitive]) != nil {
            contextScore += 15
        }
        
        return contextScore
    }
    
    private func getRiskLevel(_ toxicityScore: Int) -> String {
        if toxicityScore >= 60 { return "Alto" }
        if toxicityScore >= 35 { return "Médio" }
        return "Baixo"
    }
    
    private func createNotification() -> Notification {
        if #available(iOS 10.0, *) {
            let content = UNMutableNotificationContent()
            content.title = "🛡️ RedeSegura Ativo"
            content.body = "Protegendo sua saúde mental nas redes sociais"
            content.sound = nil
            
            return UNNotificationRequest(identifier: "redesegura_active", content: content, trigger: nil)
        }
        return Notification()
    }
    
    private func createNotificationChannel() {
        // Implementar canal de notificação iOS
    }
    
    override func stopService() {
        isRunning = false
        
        if let interface = vpnInterface {
            try? interface.close()
        }
        
        executor.shutdown()
        print("RedeSegura VPN Service parado")
    }
}

// Estrutura para resultado da análise
struct AIAnalysisResult {
    let toxicityScore: Int
    let comparisonLevel: Int
    let foundTriggers: [String]
    let shouldBlock: Bool
    let confidence: Int
    let triggerType: String
    let triggerReason: String
    let riskLevel: String
}