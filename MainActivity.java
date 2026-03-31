package com.ghost.v4;

import android.Manifest;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.connection.*;
import java.nio.charset.StandardCharsets;

public class MainActivity extends AppCompatActivity {

    private final String SERVICE_ID = "ghost.v4.p2p";
    private ConnectionsClient client;
    private String remoteId;
    private TextView chat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setBackgroundColor(0xFF000000);

        chat = new TextView(this);
        chat.setTextColor(0xFF00FF00);
        chat.setText("--- GHOST v4 READY ---\n");

        layout.addView(chat);
        setContentView(layout);

        client = Nearby.getConnectionsClient(this);

        requestPermissions(new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH_SCAN
        }, 1);

        startNearby();
    }

    private void startNearby() {
        client.startAdvertising(
                "User",
                SERVICE_ID,
                connectionCallback,
                new AdvertisingOptions.Builder().setStrategy(Strategy.P2P_CLUSTER).build()
        );

        client.startDiscovery(
                SERVICE_ID,
                discoveryCallback,
                new DiscoveryOptions.Builder().setStrategy(Strategy.P2P_CLUSTER).build()
        );
    }

    private final ConnectionLifecycleCallback connectionCallback =
            new ConnectionLifecycleCallback() {
                @Override
                public void onConnectionInitiated(String id, ConnectionInfo info) {
                    client.acceptConnection(id, payloadCallback);
                }

                @Override
                public void onConnectionResult(String id, ConnectionResolution res) {
                    if (res.getStatus().isSuccess()) {
                        remoteId = id;
                        chat.append("\n[CONNECTED]");
                    }
                }

                @Override
                public void onDisconnected(String id) {
                    chat.append("\n[DISCONNECTED]");
                }
            };

    private final EndpointDiscoveryCallback discoveryCallback =
            new EndpointDiscoveryCallback() {
                @Override
                public void onEndpointFound(String id, DiscoveredEndpointInfo info) {
                    client.requestConnection("User", id, connectionCallback);
                }

                @Override
                public void onEndpointLost(String id) {}
            };

    private final PayloadCallback payloadCallback =
            new PayloadCallback() {
                @Override
                public void onPayloadReceived(String id, Payload p) {
                    String data = new String(p.asBytes(), StandardCharsets.UTF_8);
                    runOnUiThread(() -> chat.append("\n[PEER]: " + data));
                }

                @Override
                public void onPayloadTransferUpdate(String id, PayloadTransferUpdate u) {}
            };
}
