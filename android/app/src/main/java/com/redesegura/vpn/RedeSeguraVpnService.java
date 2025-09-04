package com.redesegura.vpn;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.net.VpnService;
import android.os.Build;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RedeSeguraVpnService extends VpnService {
    
    private static final String TAG = "RedeSeguraVPN";
    private static final String CHANNEL_ID = "RedeSeguraVPN";
    
    private ParcelFileDescriptor vpnInterface;
    private ExecutorService executor = Executors.newCachedThreadPool();
    private boolean isRunning = false;
    private NetworkAnalyzer networkAnalyzer;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Iniciando RedeSegura VPN Service");
        
        createNotificationChannel();
        startForeground(1, createNotification());
        
        if (!isRunning) {
            startVpn();
        }
        
        return START_STICKY;
    }

    private void startVpn() {
        try {
            // Configurar VPN interface
            Builder builder = new Builder()
                    .setSession("RedeSegura VPN")
                    .addAddress("10.0.0.2", 32)
                    .addRoute("0.0.0.0", 0)
                    .addDnsServer("8.8.8.8")
                    .addDnsServer("8.8.4.4")
                    .setMtu(1500);

            vpnInterface = builder.establish();
            
            if (vpnInterface != null) {
                isRunning = true;
                networkAnalyzer = new NetworkAnalyzer();
                
                // Iniciar interceptação de tráfego
                executor.submit(this::interceptNetworkTraffic);
                
                Log.d(TAG, "VPN iniciada com sucesso");
            } else {
                Log.e(TAG, "Falha ao estabelecer interface VPN");
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Erro ao iniciar VPN", e);
        }
    }

    private void interceptNetworkTraffic() {
        Log.d(TAG, "Iniciando interceptação de tráfego de rede");
        
        try {
            FileInputStream in = new FileInputStream(vpnInterface.getFileDescriptor());
            FileOutputStream out = new FileOutputStream(vpnInterface.getFileDescriptor());
            
            ByteBuffer packet = ByteBuffer.allocate(32767);
            
            while (isRunning && !Thread.currentThread().isInterrupted()) {
                // Ler pacote
                int length = in.read(packet.array());
                if (length > 0) {
                    packet.limit(length);
                    
                    // Analisar pacote
                    PacketInfo packetInfo = PacketAnalyzer.analyzePacket(packet);
                    
                    if (packetInfo.isSocialMediaAPI()) {
                        // Verificar se é requisição de feed
                        if (packetInfo.isFeedRequest()) {
                            Log.d(TAG, "Interceptando requisição de feed: " + packetInfo.getUrl());
                            
                            // Analisar conteúdo da requisição/resposta
                            boolean shouldBlock = networkAnalyzer.analyzeNetworkContent(packetInfo);
                            
                            if (shouldBlock) {
                                Log.d(TAG, "Bloqueando requisição tóxica");
                                // Bloquear ou modificar resposta
                                continue;
                            }
                        }
                    }
                    
                    // Encaminhar pacote se não foi bloqueado
                    out.write(packet.array(), 0, length);
                }
                
                packet.clear();
            }
            
        } catch (IOException e) {
            Log.e(TAG, "Erro na interceptação de rede", e);
        }
    }

    private Notification createNotification() {
        return new Notification.Builder(this, CHANNEL_ID)
                .setContentTitle("RedeSegura Ativo")
                .setContentText("Protegendo sua saúde mental nas redes sociais")
                .setSmallIcon(android.R.drawable.ic_menu_shield)
                .setPriority(Notification.PRIORITY_LOW)
                .build();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "RedeSegura VPN",
                NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("Proteção de saúde mental ativa");
            
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isRunning = false;
        
        if (vpnInterface != null) {
            try {
                vpnInterface.close();
            } catch (IOException e) {
                Log.e(TAG, "Erro ao fechar interface VPN", e);
            }
        }
        
        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
        }
        
        Log.d(TAG, "RedeSegura VPN Service destruído");
    }
}