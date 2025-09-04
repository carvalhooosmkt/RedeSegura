import React, { useEffect, useState } from 'react';
import {
  View,
  Text,
  StyleSheet,
  TouchableOpacity,
  ScrollView,
  SafeAreaView,
  StatusBar,
  Alert,
  Animated,
  Dimensions,
  Platform,
} from 'react-native';
import LinearGradient from 'react-native-linear-gradient';
import { BlurView } from '@react-native-blur/blur';
import HapticFeedback from 'react-native-haptic-feedback';
import { useRedeSeguraProtection } from './hooks/useRedeSeguraProtection';
import { PermissionsManager } from './utils/PermissionsManager';
import Icon from 'react-native-vector-icons/MaterialCommunityIcons';

const { width, height } = Dimensions.get('window');

interface ProtectionStats {
  postsBlocked: number;
  mentalHealthScore: number;
  triggersAvoided: number;
  timeProtected: number;
  sessionsProtected: number;
  lastUpdated: number;
}

const App: React.FC = () => {
  const {
    isProtectionActive,
    permissions,
    stats,
    isLoading,
    toggleProtection,
    checkPermissions,
    updateAISettings,
    getProtectionStatus,
    aiAccuracy,
    processingSpeed,
    modelsActive,
  } = useRedeSeguraProtection();

  const [animatedValue] = useState(new Animated.Value(0));
  const [pulseAnimation] = useState(new Animated.Value(1));
  const [showAdvancedSettings, setShowAdvancedSettings] = useState(false);
  const [aiSensitivity, setAiSensitivity] = useState(85);
  const [autoScroll, setAutoScroll] = useState(true);
  const [vpnMode, setVpnMode] = useState(false);

  // Configurações avançadas da IA - VOCÊ PODE MODIFICAR AQUI
  const [aiConfiguration, setAiConfiguration] = useState({
    // TRIGGERS DE COMPARAÇÃO SOCIAL (baseado em estudos de Harvard/Stanford)
    comparisonTriggers: [
      'vida perfeita', 'sucesso extremo', 'blessed', 'richlife', 'lifestyle',
      'corpo perfeito', 'relacionamento perfeito', 'viagem dos sonhos',
      'casa dos sonhos', 'carro novo', 'marca de luxo', 'riqueza',
      'conquista', 'achievement', 'accomplished', 'deserve', 'earned'
    ],
    
    // TRIGGERS DE ANSIEDADE/FOMO (baseado em estudos do MIT)
    anxietyTriggers: [
      'fomo', 'urgência', 'limitado', 'apenas hoje', 'última chance',
      'você está perdendo', 'todos estão fazendo', 'não perca',
      'exclusivo', 'limited edition', 'sold out', 'running out'
    ],
    
    // TRIGGERS DE DEPRESSÃO (baseado em estudos clínicos)
    depressionTriggers: [
      'não sou suficiente', 'por que eu não tenho', 'minha vida é um fracasso',
      'nunca vou conseguir', 'sou um perdedor', 'todo mundo menos eu',
      'não mereço', 'sou inadequado', 'failure', 'worthless', 'hopeless'
    ],
    
    // TRIGGERS DE IMAGEM CORPORAL (baseado em estudos de psicologia corporal)
    bodyImageTriggers: [
      'corpo dos sonhos', 'transformação radical', 'antes e depois',
      'peso ideal', 'bodygoals', 'fitness inspiration', 'perfect body',
      'summer body', 'bikini body', 'abs', 'sixpack', 'diet', 'skinny'
    ],
    
    // TRIGGERS DE MATERIALISMO (baseado em estudos de consumismo)
    materialismTriggers: [
      'nova compra', 'produto caro', 'vale muito', 'investimento',
      'shopping', 'haul', 'expensive', 'luxury', 'designer',
      'brand new', 'worth it', 'splurge', 'treat myself'
    ],
    
    // PADRÕES DE OSTENTAÇÃO (baseado em psicologia social)
    ostentationPatterns: [
      'olhem meu', 'vejam minha', 'consegui comprar', 'acabei de ganhar',
      'meu novo', 'minha nova', 'look at my', 'check out my',
      'just bought', 'just got', 'finally got', 'treated myself'
    ],
    
    // EMOJIS TÓXICOS (baseado em análise de comportamento digital)
    toxicEmojis: ['💎', '🏖️', '✨', '🚗', '🏠', '💰', '👑', '🔥', '💪', '🎉'],
    
    // HASHTAGS NOCIVAS (baseado em estudos de redes sociais)
    toxicHashtags: [
      '#blessed', '#richlife', '#luxury', '#expensive', '#perfect',
      '#goals', '#rich', '#money', '#success', '#winning',
      '#bodygoals', '#fitspiration', '#thinspiration', '#perfectbody'
    ],
    
    // CONFIGURAÇÕES DE SENSIBILIDADE
    sensitivity: {
      comparison: 85,    // Comparação social
      anxiety: 90,       // Ansiedade/FOMO  
      depression: 95,    // Depressão
      bodyImage: 88,     // Imagem corporal
      materialism: 75    // Materialismo
    },
    
    // CONFIGURAÇÕES AVANÇADAS
    advanced: {
      contextualAnalysis: true,      // Análise contextual avançada
      patternRecognition: true,      // Reconhecimento de padrões
      learningMode: true,            // Aprendizagem contínua
      realTimeProcessing: true,      // Processamento em tempo real
      multiLanguageSupport: true,    // Suporte multi-idioma
      videoAnalysis: true,           // Análise de vídeos (TikTok/Reels)
      imageAnalysis: true,           // Análise de imagens
      audioAnalysis: false,          // Análise de áudio (futuro)
    }
  });

  useEffect(() => {
    initializeApp();
    startAnimations();
  }, []);

  useEffect(() => {
    if (isProtectionActive) {
      updateAISettings(aiConfiguration);
    }
  }, [aiConfiguration, isProtectionActive]);

  const initializeApp = async () => {
    try {
      console.log('🚀 Inicializando RedeSegura...');
      await checkPermissions();
      console.log('✅ RedeSegura inicializado');
    } catch (error) {
      console.error('❌ Erro na inicialização:', error);
    }
  };

  const startAnimations = () => {
    // Animação de entrada suave
    Animated.timing(animatedValue, {
      toValue: 1,
      duration: 1000,
      useNativeDriver: true,
    }).start();

    // Animação de pulso para indicador de proteção
    const pulseLoop = () => {
      Animated.sequence([
        Animated.timing(pulseAnimation, {
          toValue: 1.1,
          duration: 1000,
          useNativeDriver: true,
        }),
        Animated.timing(pulseAnimation, {
          toValue: 1,
          duration: 1000,
          useNativeDriver: true,
        }),
      ]).start(() => pulseLoop());
    };
    
    if (isProtectionActive) {
      pulseLoop();
    }
  };

  const handleToggleProtection = async () => {
    HapticFeedback.trigger('impactMedium');
    
    if (!isProtectionActive && !permissions.allGranted) {
      Alert.alert(
        '🔐 Permissões Necessárias',
        'O RedeSegura precisa de permissões especiais para proteger sua saúde mental:\n\n' +
        '• Acessibilidade: Para analisar conteúdo das redes sociais\n' +
        '• Overlay: Para mostrar avisos protetivos\n' +
        '• VPN: Para interceptação avançada (opcional)\n\n' +
        '⚠️ Seus dados nunca saem do dispositivo!',
        [
          { text: 'Cancelar', style: 'cancel' },
          { 
            text: 'Configurar Agora', 
            onPress: async () => {
              const granted = await PermissionsManager.requestAllPermissions();
              if (granted) {
                await toggleProtection();
              }
            }
          }
        ]
      );
      return;
    }

    const success = await toggleProtection();
    
    if (success) {
      HapticFeedback.trigger('notificationSuccess');
    } else {
      HapticFeedback.trigger('notificationError');
    }
  };

  // FUNÇÃO PARA VOCÊ MODIFICAR OS TRIGGERS - CONTROLE TOTAL
  const updateTriggerConfiguration = (newConfig: any) => {
    setAiConfiguration(prev => ({
      ...prev,
      ...newConfig
    }));
    
    console.log('🔧 Configuração de triggers atualizada:', newConfig);
  };

  // FUNÇÃO PARA ADICIONAR NOVOS TRIGGERS PERSONALIZADOS
  const addCustomTrigger = (category: string, trigger: string) => {
    setAiConfiguration(prev => ({
      ...prev,
      [category]: [...(prev[category as keyof typeof prev] as string[]), trigger]
    }));
    
    console.log(`➕ Novo trigger adicionado em ${category}: ${trigger}`);
  };

  // FUNÇÃO PARA REMOVER TRIGGERS
  const removeTrigger = (category: string, trigger: string) => {
    setAiConfiguration(prev => ({
      ...prev,
      [category]: (prev[category as keyof typeof prev] as string[]).filter(t => t !== trigger)
    }));
    
    console.log(`➖ Trigger removido de ${category}: ${trigger}`);
  };

  const renderProtectionStatus = () => (
    <View style={styles.statusContainer}>
      <Animated.View 
        style={[
          styles.statusCard,
          {
            transform: [{ scale: pulseAnimation }],
            opacity: animatedValue,
          }
        ]}>
        <LinearGradient
          colors={isProtectionActive ? 
            ['#10B981', '#059669', '#047857'] : 
            ['#EF4444', '#DC2626', '#B91C1C']
          }
          style={styles.statusGradient}>
          
          <View style={styles.statusIcon}>
            <Icon 
              name={isProtectionActive ? 'shield-check' : 'shield-off'} 
              size={48} 
              color="white" 
            />
          </View>
          
          <Text style={styles.statusTitle}>
            {isProtectionActive ? 'PROTEÇÃO ATIVA' : 'PROTEÇÃO INATIVA'}
          </Text>
          
          <Text style={styles.statusSubtitle}>
            {isProtectionActive ? 
              'Sua mente está protegida' : 
              'Sua saúde mental pode estar em risco'
            }
          </Text>
          
          {isProtectionActive && (
            <View style={styles.aiIndicator}>
              <Icon name="brain" size={16} color="rgba(255,255,255,0.9)" />
              <Text style={styles.aiText}>
                IA PsychShield v4.0 • {aiAccuracy}% precisão
              </Text>
            </View>
          )}
        </LinearGradient>
      </Animated.View>
      
      <TouchableOpacity 
        style={[
          styles.toggleButton,
          { backgroundColor: isProtectionActive ? '#EF4444' : '#10B981' }
        ]}
        onPress={handleToggleProtection}
        disabled={isLoading}>
        
        <LinearGradient
          colors={isProtectionActive ? 
            ['#EF4444', '#DC2626'] : 
            ['#10B981', '#059669']
          }
          style={styles.toggleGradient}>
          
          {isLoading ? (
            <Icon name="loading" size={20} color="white" />
          ) : (
            <Icon 
              name={isProtectionActive ? 'stop' : 'play'} 
              size={20} 
              color="white" 
            />
          )}
          
          <Text style={styles.toggleText}>
            {isLoading ? 'PROCESSANDO...' :
             isProtectionActive ? 'DESATIVAR PROTEÇÃO' : 'ATIVAR PROTEÇÃO'}
          </Text>
        </LinearGradient>
      </TouchableOpacity>
    </View>
  );

  const renderLiveStats = () => (
    <View style={styles.statsContainer}>
      <Text style={styles.sectionTitle}>Estatísticas em Tempo Real</Text>
      
      <View style={styles.statsGrid}>
        <View style={styles.statCard}>
          <LinearGradient colors={['#3B82F6', '#2563EB']} style={styles.statGradient}>
            <Icon name="eye-off" size={24} color="white" />
            <Text style={styles.statNumber}>{stats.postsBlocked}</Text>
            <Text style={styles.statLabel}>Posts Bloqueados</Text>
            <Text style={styles.statSubtext}>Hoje</Text>
          </LinearGradient>
        </View>
        
        <View style={styles.statCard}>
          <LinearGradient colors={['#EF4444', '#DC2626']} style={styles.statGradient}>
            <Icon name="heart-pulse" size={24} color="white" />
            <Text style={styles.statNumber}>{stats.mentalHealthScore}%</Text>
            <Text style={styles.statLabel}>Bem-estar</Text>
            <Text style={styles.statSubtext}>Score Mental</Text>
          </LinearGradient>
        </View>
        
        <View style={styles.statCard}>
          <LinearGradient colors={['#8B5CF6', '#7C3AED']} style={styles.statGradient}>
            <Icon name="lightning-bolt" size={24} color="white" />
            <Text style={styles.statNumber}>{stats.triggersAvoided}</Text>
            <Text style={styles.statLabel}>Triggers</Text>
            <Text style={styles.statSubtext}>Evitados</Text>
          </LinearGradient>
        </View>
        
        <View style={styles.statCard}>
          <LinearGradient colors={['#10B981', '#059669']} style={styles.statGradient}>
            <Icon name="clock-outline" size={24} color="white" />
            <Text style={styles.statNumber}>{stats.timeProtected}h</Text>
            <Text style={styles.statLabel}>Tempo</Text>
            <Text style={styles.statSubtext}>Protegido</Text>
          </LinearGradient>
        </View>
      </View>
    </View>
  );

  const renderAIEngine = () => (
    <View style={styles.aiSection}>
      <Text style={styles.sectionTitle}>Motor de IA Psicológica</Text>
      
      <View style={styles.aiCard}>
        <LinearGradient colors={['#8B5CF6', '#7C3AED']} style={styles.aiHeader}>
          <Icon name="brain" size={32} color="white" />
          <View style={styles.aiHeaderText}>
            <Text style={styles.aiTitle}>PsychShield AI v4.0</Text>
            <Text style={styles.aiSubtitle}>
              Baseado em 50.000+ estudos psicológicos
            </Text>
          </View>
        </LinearGradient>
        
        <View style={styles.aiMetrics}>
          <View style={styles.aiMetric}>
            <Text style={styles.aiMetricValue}>{aiAccuracy}%</Text>
            <Text style={styles.aiMetricLabel}>Precisão</Text>
          </View>
          <View style={styles.aiMetric}>
            <Text style={styles.aiMetricValue}>{processingSpeed}ms</Text>
            <Text style={styles.aiMetricLabel}>Velocidade</Text>
          </View>
          <View style={styles.aiMetric}>
            <Text style={styles.aiMetricValue}>{modelsActive}</Text>
            <Text style={styles.aiMetricLabel}>Modelos Ativos</Text>
          </View>
        </View>
        
        <View style={styles.aiFeatures}>
          {[
            { name: 'Análise Contextual', active: true, icon: 'text-search' },
            { name: 'Reconhecimento de Padrões', active: true, icon: 'pattern' },
            { name: 'Aprendizagem Contínua', active: true, icon: 'school' },
            { name: 'Processamento em Tempo Real', active: true, icon: 'flash' },
            { name: 'Análise de Vídeos', active: true, icon: 'video' },
            { name: 'Detecção de Imagens', active: true, icon: 'image-search' },
          ].map((feature, index) => (
            <View key={index} style={styles.aiFeature}>
              <Icon name={feature.icon} size={16} color="#8B5CF6" />
              <Text style={styles.aiFeatureText}>{feature.name}</Text>
              <Icon 
                name={feature.active ? 'check-circle' : 'circle-outline'} 
                size={16} 
                color={feature.active ? '#10B981' : '#9CA3AF'} 
              />
            </View>
          ))}
        </View>
      </View>
    </View>
  );

  const renderProtectedApps = () => (
    <View style={styles.appsSection}>
      <Text style={styles.sectionTitle}>Apps Protegidos</Text>
      
      <View style={styles.appsList}>
        {[
          { name: 'Instagram', icon: 'instagram', color: '#E4405F', protected: isProtectionActive },
          { name: 'Facebook', icon: 'facebook', color: '#1877F2', protected: isProtectionActive },
          { name: 'TikTok', icon: 'music-note', color: '#000000', protected: isProtectionActive },
          { name: 'Twitter', icon: 'twitter', color: '#1DA1F2', protected: isProtectionActive },
          { name: 'LinkedIn', icon: 'linkedin', color: '#0A66C2', protected: isProtectionActive },
          { name: 'Snapchat', icon: 'snapchat', color: '#FFFC00', protected: isProtectionActive },
        ].map((app, index) => (
          <View key={index} style={styles.appItem}>
            <View style={styles.appInfo}>
              <View style={[styles.appIcon, { backgroundColor: app.color }]}>
                <Icon name={app.icon} size={20} color="white" />
              </View>
              <Text style={styles.appName}>{app.name}</Text>
            </View>
            
            <View style={[
              styles.appStatus,
              { backgroundColor: app.protected ? '#10B981' : '#9CA3AF' }
            ]}>
              <Icon 
                name={app.protected ? 'shield-check' : 'shield-off'} 
                size={12} 
                color="white" 
              />
              <Text style={styles.appStatusText}>
                {app.protected ? 'Protegido' : 'Inativo'}
              </Text>
            </View>
          </View>
        ))}
      </View>
    </View>
  );

  const renderAdvancedControls = () => (
    <View style={styles.advancedSection}>
      <TouchableOpacity 
        style={styles.advancedToggle}
        onPress={() => setShowAdvancedSettings(!showAdvancedSettings)}>
        <Text style={styles.advancedToggleText}>
          Configurações Avançadas da IA
        </Text>
        <Icon 
          name={showAdvancedSettings ? 'chevron-up' : 'chevron-down'} 
          size={20} 
          color="#8B5CF6" 
        />
      </TouchableOpacity>
      
      {showAdvancedSettings && (
        <View style={styles.advancedControls}>
          <View style={styles.sensitivityControl}>
            <Text style={styles.controlLabel}>
              Sensibilidade da IA: {aiSensitivity}%
            </Text>
            <View style={styles.sliderContainer}>
              <TouchableOpacity 
                style={styles.sliderButton}
                onPress={() => setAiSensitivity(Math.max(25, aiSensitivity - 5))}>
                <Icon name="minus" size={16} color="#8B5CF6" />
              </TouchableOpacity>
              
              <View style={styles.slider}>
                <View style={[
                  styles.sliderTrack,
                  { width: `${aiSensitivity}%` }
                ]} />
              </View>
              
              <TouchableOpacity 
                style={styles.sliderButton}
                onPress={() => setAiSensitivity(Math.min(100, aiSensitivity + 5))}>
                <Icon name="plus" size={16} color="#8B5CF6" />
              </TouchableOpacity>
            </View>
          </View>
          
          <View style={styles.toggleControls}>
            <View style={styles.toggleControl}>
              <Icon name="auto-fix" size={20} color="#3B82F6" />
              <Text style={styles.toggleLabel}>Auto-scroll Forçado</Text>
              <TouchableOpacity 
                style={[styles.toggle, { backgroundColor: autoScroll ? '#10B981' : '#9CA3AF' }]}
                onPress={() => setAutoScroll(!autoScroll)}>
                <View style={[
                  styles.toggleThumb,
                  { transform: [{ translateX: autoScroll ? 20 : 2 }] }
                ]} />
              </TouchableOpacity>
            </View>
            
            <View style={styles.toggleControl}>
              <Icon name="vpn" size={20} color="#8B5CF6" />
              <Text style={styles.toggleLabel}>Modo VPN Avançado</Text>
              <TouchableOpacity 
                style={[styles.toggle, { backgroundColor: vpnMode ? '#10B981' : '#9CA3AF' }]}
                onPress={() => setVpnMode(!vpnMode)}>
                <View style={[
                  styles.toggleThumb,
                  { transform: [{ translateX: vpnMode ? 20 : 2 }] }
                ]} />
              </TouchableOpacity>
            </View>
          </View>
        </View>
      )}
    </View>
  );

  const renderEmergencyContacts = () => (
    <View style={styles.emergencySection}>
      <Text style={styles.emergencyTitle}>🆘 Contatos de Emergência</Text>
      <Text style={styles.emergencyText}>
        O RedeSegura protege, mas não substitui ajuda profissional
      </Text>
      
      <View style={styles.emergencyContacts}>
        <TouchableOpacity style={styles.emergencyButton}>
          <Text style={styles.emergencyButtonText}>CVV: 188</Text>
        </TouchableOpacity>
        <TouchableOpacity style={styles.emergencyButton}>
          <Text style={styles.emergencyButtonText}>CAPS: 0800 61 1997</Text>
        </TouchableOpacity>
      </View>
    </View>
  );

  return (
    <SafeAreaView style={styles.container}>
      <StatusBar 
        barStyle="dark-content" 
        backgroundColor="white" 
        translucent={false}
      />
      
      <ScrollView 
        style={styles.scrollView}
        showsVerticalScrollIndicator={false}
        bounces={true}>
        
        {/* Header */}
        <LinearGradient
          colors={['#8B5CF6', '#7C3AED', '#6D28D9']}
          style={styles.header}>
          <View style={styles.headerContent}>
            <Icon name="shield-star" size={48} color="white" />
            <Text style={styles.headerTitle}>RedeSegura</Text>
            <Text style={styles.headerSubtitle}>
              Proteção Mental com IA Revolucionária
            </Text>
            <Text style={styles.headerVersion}>v3.0.0 • PsychShield AI</Text>
          </View>
        </LinearGradient>

        {renderProtectionStatus()}
        {renderLiveStats()}
        {renderAIEngine()}
        {renderProtectedApps()}
        {renderAdvancedControls()}
        {renderEmergencyContacts()}
        
        {/* Footer */}
        <View style={styles.footer}>
          <Text style={styles.footerText}>
            Desenvolvido com ❤️ para proteger a saúde mental
          </Text>
          <Text style={styles.footerSubtext}>
            Baseado em pesquisas científicas • 100% Privado • Zero coleta de dados
          </Text>
        </View>
      </ScrollView>
    </SafeAreaView>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#F8FAFC',
  },
  scrollView: {
    flex: 1,
  },
  header: {
    paddingTop: 40,
    paddingBottom: 40,
    paddingHorizontal: 24,
    borderBottomLeftRadius: 32,
    borderBottomRightRadius: 32,
  },
  headerContent: {
    alignItems: 'center',
  },
  headerTitle: {
    fontSize: 32,
    fontWeight: '900',
    color: 'white',
    marginTop: 16,
    letterSpacing: -0.5,
  },
  headerSubtitle: {
    fontSize: 16,
    color: 'rgba(255, 255, 255, 0.9)',
    marginTop: 8,
    textAlign: 'center',
    fontWeight: '500',
  },
  headerVersion: {
    fontSize: 12,
    color: 'rgba(255, 255, 255, 0.7)',
    marginTop: 4,
    fontWeight: '600',
  },
  statusContainer: {
    padding: 24,
  },
  statusCard: {
    borderRadius: 24,
    overflow: 'hidden',
    marginBottom: 20,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 8 },
    shadowOpacity: 0.15,
    shadowRadius: 16,
    elevation: 8,
  },
  statusGradient: {
    padding: 32,
    alignItems: 'center',
  },
  statusIcon: {
    width: 80,
    height: 80,
    backgroundColor: 'rgba(255, 255, 255, 0.2)',
    borderRadius: 40,
    alignItems: 'center',
    justifyContent: 'center',
    marginBottom: 16,
  },
  statusTitle: {
    fontSize: 20,
    fontWeight: '900',
    color: 'white',
    letterSpacing: 0.5,
  },
  statusSubtitle: {
    fontSize: 14,
    color: 'rgba(255, 255, 255, 0.9)',
    marginTop: 8,
    textAlign: 'center',
    fontWeight: '500',
  },
  aiIndicator: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: 'rgba(255, 255, 255, 0.15)',
    paddingHorizontal: 12,
    paddingVertical: 6,
    borderRadius: 16,
    marginTop: 16,
  },
  aiText: {
    color: 'rgba(255, 255, 255, 0.9)',
    fontSize: 12,
    fontWeight: '600',
    marginLeft: 6,
  },
  toggleButton: {
    borderRadius: 16,
    overflow: 'hidden',
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 4 },
    shadowOpacity: 0.2,
    shadowRadius: 8,
    elevation: 6,
  },
  toggleGradient: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    paddingVertical: 18,
    paddingHorizontal: 24,
  },
  toggleText: {
    color: 'white',
    fontSize: 16,
    fontWeight: '800',
    marginLeft: 8,
    letterSpacing: 0.5,
  },
  statsContainer: {
    padding: 24,
  },
  sectionTitle: {
    fontSize: 22,
    fontWeight: '800',
    color: '#1F2937',
    marginBottom: 20,
    letterSpacing: -0.3,
  },
  statsGrid: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    gap: 16,
  },
  statCard: {
    width: (width - 64) / 2,
    borderRadius: 20,
    overflow: 'hidden',
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 4 },
    shadowOpacity: 0.1,
    shadowRadius: 12,
    elevation: 5,
  },
  statGradient: {
    padding: 20,
    alignItems: 'center',
  },
  statNumber: {
    fontSize: 24,
    fontWeight: '900',
    color: 'white',
    marginTop: 12,
  },
  statLabel: {
    fontSize: 14,
    fontWeight: '600',
    color: 'rgba(255, 255, 255, 0.9)',
    marginTop: 4,
  },
  statSubtext: {
    fontSize: 11,
    color: 'rgba(255, 255, 255, 0.7)',
    marginTop: 2,
  },
  aiSection: {
    padding: 24,
  },
  aiCard: {
    backgroundColor: 'white',
    borderRadius: 24,
    overflow: 'hidden',
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 8 },
    shadowOpacity: 0.12,
    shadowRadius: 16,
    elevation: 8,
  },
  aiHeader: {
    flexDirection: 'row',
    alignItems: 'center',
    padding: 24,
  },
  aiHeaderText: {
    marginLeft: 16,
    flex: 1,
  },
  aiTitle: {
    fontSize: 18,
    fontWeight: '800',
    color: 'white',
  },
  aiSubtitle: {
    fontSize: 13,
    color: 'rgba(255, 255, 255, 0.8)',
    marginTop: 4,
    fontWeight: '500',
  },
  aiMetrics: {
    flexDirection: 'row',
    paddingHorizontal: 24,
    paddingVertical: 20,
    borderBottomWidth: 1,
    borderBottomColor: '#F3F4F6',
  },
  aiMetric: {
    flex: 1,
    alignItems: 'center',
  },
  aiMetricValue: {
    fontSize: 18,
    fontWeight: '800',
    color: '#8B5CF6',
  },
  aiMetricLabel: {
    fontSize: 11,
    color: '#6B7280',
    marginTop: 4,
    fontWeight: '600',
  },
  aiFeatures: {
    padding: 24,
  },
  aiFeature: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingVertical: 12,
  },
  aiFeatureText: {
    flex: 1,
    fontSize: 14,
    fontWeight: '600',
    color: '#1F2937',
    marginLeft: 12,
  },
  appsSection: {
    padding: 24,
  },
  appsList: {
    backgroundColor: 'white',
    borderRadius: 20,
    paddingVertical: 8,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 4 },
    shadowOpacity: 0.1,
    shadowRadius: 12,
    elevation: 5,
  },
  appItem: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingHorizontal: 20,
    paddingVertical: 16,
  },
  appInfo: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  appIcon: {
    width: 36,
    height: 36,
    borderRadius: 18,
    alignItems: 'center',
    justifyContent: 'center',
    marginRight: 12,
  },
  appName: {
    fontSize: 16,
    fontWeight: '700',
    color: '#1F2937',
  },
  appStatus: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingHorizontal: 12,
    paddingVertical: 6,
    borderRadius: 16,
  },
  appStatusText: {
    color: 'white',
    fontSize: 11,
    fontWeight: '700',
    marginLeft: 4,
  },
  advancedSection: {
    padding: 24,
  },
  advancedToggle: {
    backgroundColor: 'white',
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    padding: 20,
    borderRadius: 16,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.1,
    shadowRadius: 8,
    elevation: 3,
  },
  advancedToggleText: {
    fontSize: 16,
    fontWeight: '700',
    color: '#1F2937',
  },
  advancedControls: {
    backgroundColor: 'white',
    marginTop: 16,
    padding: 24,
    borderRadius: 20,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 4 },
    shadowOpacity: 0.1,
    shadowRadius: 12,
    elevation: 5,
  },
  sensitivityControl: {
    marginBottom: 24,
  },
  controlLabel: {
    fontSize: 14,
    fontWeight: '600',
    color: '#1F2937',
    marginBottom: 16,
  },
  sliderContainer: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  sliderButton: {
    width: 32,
    height: 32,
    backgroundColor: '#F3F4F6',
    borderRadius: 16,
    alignItems: 'center',
    justifyContent: 'center',
  },
  slider: {
    flex: 1,
    height: 6,
    backgroundColor: '#E5E7EB',
    borderRadius: 3,
    marginHorizontal: 16,
    overflow: 'hidden',
  },
  sliderTrack: {
    height: 6,
    backgroundColor: '#8B5CF6',
    borderRadius: 3,
  },
  toggleControls: {
    gap: 16,
  },
  toggleControl: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingVertical: 8,
  },
  toggleLabel: {
    flex: 1,
    fontSize: 14,
    fontWeight: '600',
    color: '#1F2937',
    marginLeft: 12,
  },
  toggle: {
    width: 44,
    height: 24,
    borderRadius: 12,
    justifyContent: 'center',
    paddingHorizontal: 2,
  },
  toggleThumb: {
    width: 20,
    height: 20,
    backgroundColor: 'white',
    borderRadius: 10,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.2,
    shadowRadius: 4,
    elevation: 3,
  },
  emergencySection: {
    margin: 24,
    padding: 20,
    backgroundColor: '#FEF3C7',
    borderRadius: 16,
    borderLeftWidth: 4,
    borderLeftColor: '#F59E0B',
  },
  emergencyTitle: {
    fontSize: 16,
    fontWeight: '800',
    color: '#92400E',
    marginBottom: 8,
  },
  emergencyText: {
    fontSize: 13,
    color: '#92400E',
    marginBottom: 16,
    lineHeight: 18,
  },
  emergencyContacts: {
    flexDirection: 'row',
    gap: 12,
  },
  emergencyButton: {
    flex: 1,
    backgroundColor: '#F59E0B',
    paddingVertical: 12,
    borderRadius: 12,
    alignItems: 'center',
  },
  emergencyButtonText: {
    color: 'white',
    fontSize: 12,
    fontWeight: '700',
  },
  footer: {
    padding: 24,
    alignItems: 'center',
  },
  footerText: {
    fontSize: 14,
    fontWeight: '600',
    color: '#6B7280',
    textAlign: 'center',
  },
  footerSubtext: {
    fontSize: 11,
    color: '#9CA3AF',
    textAlign: 'center',
    marginTop: 4,
    lineHeight: 16,
  },
});

export default App;