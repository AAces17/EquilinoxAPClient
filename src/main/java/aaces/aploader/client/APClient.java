package aaces.aploader.client;

import aaces.aploader.APAgent;
import aaces.aploader.APBridge;
import io.github.archipelagomw.Client;
import io.github.archipelagomw.flags.ItemsHandling;
import tasks.Task;

import java.net.URISyntaxException;

public class APClient extends Client {

    private static APClient client;

    private static String lastHostAttempt = "";

    public SlotData slotData;

    public APClient(String slot, String password, ItemManager itemManager, LocationManager locationManager){
        super();

        this.setGame("Equilinox");
        this.setItemsHandlingFlags(ItemsHandling.SEND_ITEMS | ItemsHandling.SEND_OWN_ITEMS | ItemsHandling.SEND_STARTING_INVENTORY);

        this.getEventManager().registerListener(new ConnectResult(this));
        this.getEventManager().registerListener(new ReceiveItem(this, itemManager));

        this.setName(slot);
        this.setPassword(password);
    }

    /// Creates the APClient instance and tries to connect to the room.
    /// @param host the room host (eg, "archipelago.gg:42352")
    /// @param slot the slot name
    /// @param password the room password
    /// @param itemManager the ItemManager instance used by APBridge
    /// @param locationManager the LocationManager instance used by APBridge
    /// @return the APClient instance
    public static APClient tryConnect(String host, String slot, String password, ItemManager itemManager, LocationManager locationManager, boolean forceReconnect){
        if (Connected()) {
            if (slot.equals(client.getMyName()) && host.equals(lastHostAttempt) && !forceReconnect){
                return client;
            } else {
                APAgent.log("Already connected, but got a connect call with different information. Closing the existing connection.");
                tryDisconnect();
            }
        }
        APAgent.log("Trying to connect with host: "+host+", slot: "+slot+", password: "+password);
        client = new APClient(slot, password, itemManager, locationManager);
        lastHostAttempt = host;
        try {
            client.connect(host);
        } catch (URISyntaxException e) {
            APAgent.log("Couldn't connect!");
            e.printStackTrace();
        }
        return client;
    }

    @Override
    public void onError(Exception ex) {
        String error = String.format("Connection error: %s", ex.getLocalizedMessage());
        APBridge.sendNotification("Connection Error", "Archipelago Connection Error: " + ex.getLocalizedMessage());
        APAgent.log(error);
    }

    @Override
    public void onClose(String reason, int attemptingReconnect) {
        if (attemptingReconnect > 0) {
            APAgent.log(String.format("%s \n... reconnecting in %ds", reason, attemptingReconnect));
        } else {
            APAgent.log(reason);
        }
    }

    public static void tryDisconnect(){
        if(client == null) return;
        APAgent.log("Trying to disconnect");
        client.close();
        APAgent.log("Disconnected");
    }

    public static boolean Connected(){
        if(client == null) return false;
        return client.isConnected();
    }

}
