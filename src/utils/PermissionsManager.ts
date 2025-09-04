import { Platform, Alert, Linking, PermissionsAndroid } from 'react-native';
import NativeModuleService from '../services/NativeModuleService';

export class PermissionsManager {
  
  public static async requestAllPermissions(): Promise<boolean> {
    try {
      console.log('🔐 Solicitando todas as permissões necessárias para RedeSegura...');

      if (Platform.OS === 'android') {
        return await this.requestAndroidPermissions();
      } else {
        return await this.requestIOSPermissions();
      }

    } catch (error) {
      console.error('Erro ao solicitar permissões:', error);
      return false;
    }
  }

  private static async requestAndroidPermissions(): Promise<boolean> {
    try {
      // 1. Solicitar permissões básicas do Android
      const basicPermissions = await PermissionsAndroid.requestMultiple([
        PermissionsAndroid.PERMISSIONS.SYSTEM_ALERT_WINDOW,
        PermissionsAndroid.PERMISSIONS.FOREGROUND_SERVICE,
        PermissionsAndroid.PERMISSIONS.WAKE_LOCK,
        PermissionsAndroid.PERMISSIONS.RECEIVE_BOOT_COMPLETED,
      ]);

      console.log('📱 Permissões básicas Android:', basicPermissions);

      // 2. Verificar permissão de accessibility
      const hasAccessibility = await NativeModuleService.AccessibilityModule.isAccessibilityEnabled();
      
      if (!hasAccessibility) {
        const accessibilityGranted = await this.requestAccessibilityPermission();
        if (!accessibilityGranted) return false;
      }

      // 3. Verificar permissão de overlay
      const hasOverlay = await NativeModuleService.OverlayModule.canDrawOverlays();
      
      if (!hasOverlay) {
        const overlayGranted = await this.requestOverlayPermission();
        if (!overlayGranted) return false;
      }

      console.log('✅ Todas as permissões Android concedidas');
      return true;

    } catch (error) {
      console.error('Erro nas permissões Android:', error);
      return false;
    }
  }

  private static async requestIOSPermissions(): Promise<boolean> {
    // iOS tem limitações, mas podemos solicitar o que é possível
    
    return new Promise((resolve) => {
      Alert.alert(
        '🛡️ Permissões RedeSegura - iOS',
        'Para proteger sua saúde mental, o RedeSegura precisa de:\n\n' +
        '📱 Screen Recording: Para analisar conteúdo em tempo real\n' +
        '🔔 Notificações: Para avisos de proteção\n' +
        '🔄 Background App Refresh: Para proteção contínua\n' +
        '🌐 VPN: Para interceptação de rede (opcional)\n\n' +
        '⚠️ Algumas funcionalidades podem ser limitadas pelas políticas da Apple.',
        [
          { 
            text: 'Cancelar', 
            style: 'cancel',
            onPress: () => resolve(false)
          },
          {
            text: 'Ativar Proteção',
            onPress: async () => {
              try {
                // Tentar ativar funcionalidades iOS
                await NativeModuleService.AccessibilityModule.startAccessibilityService();
                resolve(true);
              } catch (error) {
                console.error('Erro ao ativar proteção iOS:', error);
                resolve(false);
              }
            }
          }
        ]
      );
    });
  }

  private static requestAccessibilityPermission(): Promise<boolean> {
    return new Promise((resolve) => {
      Alert.alert(
        '🔓 Permissão de Acessibilidade Obrigatória',
        'O RedeSegura precisa da permissão de Acessibilidade para:\n\n' +
        '👁️ Ler conteúdo de posts nas redes sociais\n' +
        '🧠 Detectar triggers psicológicos em tempo real\n' +
        '🛡️ Aplicar proteção automática instantânea\n' +
        '📜 Executar auto-scroll para pular conteúdo tóxico\n\n' +
        '🔒 GARANTIA: Seus dados nunca saem do dispositivo!\n' +
        '🧬 Toda análise é feita localmente com IA avançada.',
        [
          {
            text: 'Cancelar',
            style: 'cancel',
            onPress: () => resolve(false)
          },
          {
            text: 'Abrir Configurações',
            onPress: async () => {
              try {
                await NativeModuleService.AccessibilityModule.openAccessibilitySettings();
                
                // Aguardar usuário configurar e verificar novamente
                setTimeout(async () => {
                  const isEnabled = await NativeModuleService.AccessibilityModule.isAccessibilityEnabled();
                  if (isEnabled) {
                    Alert.alert(
                      '✅ Acessibilidade Ativada',
                      'Perfeito! O RedeSegura agora pode analisar conteúdo das redes sociais e proteger sua saúde mental.'
                    );
                  }
                  resolve(isEnabled);
                }, 5000);
                
              } catch (error) {
                console.error('Erro ao abrir configurações de acessibilidade:', error);
                resolve(false);
              }
            }
          }
        ]
      );
    });
  }

  private static requestOverlayPermission(): Promise<boolean> {
    return new Promise((resolve) => {
      Alert.alert(
        '🖼️ Permissão de Overlay Essencial',
        'O RedeSegura precisa da permissão de Overlay para:\n\n' +
        '🛡️ Mostrar avisos protetivos sobre posts tóxicos\n' +
        '🚫 Bloquear visualmente conteúdo nocivo\n' +
        '⚡ Fornecer proteção em tempo real\n' +
        '🎯 Aplicar overlays personalizados\n\n' +
        'Esta permissão é ESSENCIAL para o funcionamento do app.',
        [
          {
            text: 'Cancelar',
            style: 'cancel',
            onPress: () => resolve(false)
          },
          {
            text: 'Conceder Permissão',
            onPress: async () => {
              try {
                await NativeModuleService.OverlayModule.requestOverlayPermission();
                
                // Aguardar e verificar novamente
                setTimeout(async () => {
                  const canDraw = await NativeModuleService.OverlayModule.canDrawOverlays();
                  if (canDraw) {
                    Alert.alert(
                      '✅ Overlay Ativado',
                      'Excelente! O RedeSegura agora pode mostrar avisos protetivos sobre conteúdo tóxico.'
                    );
                  }
                  resolve(canDraw);
                }, 3000);
                
              } catch (error) {
                console.error('Erro ao solicitar permissão de overlay:', error);
                resolve(false);
              }
            }
          }
        ]
      );
    });
  }

  public static async checkPermissionStatus(): Promise<{
    accessibility: boolean;
    overlay: boolean;
    vpn: boolean;
    allGranted: boolean;
  }> {
    try {
      const permissions = await NativeModuleService.checkAllPermissions();
      
      return {
        ...permissions,
        allGranted: Platform.OS === 'ios' ? 
          permissions.accessibility :
          permissions.accessibility && permissions.overlay
      };
      
    } catch (error) {
      console.error('Erro ao verificar status das permissões:', error);
      return {
        accessibility: false,
        overlay: false,
        vpn: false,
        allGranted: false
      };
    }
  }

  public static async requestSpecificPermission(permission: 'accessibility' | 'overlay' | 'vpn'): Promise<boolean> {
    try {
      switch (permission) {
        case 'accessibility':
          return await this.requestAccessibilityPermission();
        case 'overlay':
          return await this.requestOverlayPermission();
        case 'vpn':
          // VPN é opcional, sempre retorna true
          return true;
        default:
          return false;
      }
    } catch (error) {
      console.error(`Erro ao solicitar permissão ${permission}:`, error);
      return false;
    }
  }

  public static openAppSettings() {
    try {
      if (Platform.OS === 'ios') {
        Linking.openURL('app-settings:');
      } else {
        Linking.openURL('package:com.redesegura');
      }
    } catch (error) {
      console.error('Erro ao abrir configurações do app:', error);
    }
  }

  public static async checkAndRequestPermissions(): Promise<boolean> {
    const status = await this.checkPermissionStatus();
    
    if (status.allGranted) {
      return true;
    }

    return await this.requestAllPermissions();
  }
}