package infinite.cmmonserve;

import android.content.Intent;
import android.net.VpnService;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.widget.Toast;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.InetSocketAddress;
import java.nio.channels.DatagramChannel;

public class Vpn extends VpnService {

    private Thread mThread;
    private ParcelFileDescriptor mInterface;
    //a. Configure a builder for the interface.
    Builder builder = new Builder();

    // Services interface
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Start a new session by creating a new thread.


        mThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    //a. Configure the TUN and get the interface.
                    mInterface = builder.setSession("VPNService")
                            .addAddress("192.168.0.1", 24).establish();  //random
                    //   .addRoute("0.0.0.0", 0).establish();
                    // .addDnsServer("8.8.8.8")
                    // .addRoute("0.0.0.0", 0).establish();
                    Log.d("Builder session set***",mInterface.toString());
                    //b. Packets to be sent are queued in this input stream.
                    FileInputStream in = new FileInputStream(
                            mInterface.getFileDescriptor());
                    //b. Packets received need to be written to this output stream.
                    FileOutputStream out = new FileOutputStream(
                            mInterface.getFileDescriptor());
                    //c. The UDP channel can be used to pass/get ip package to/from server
                    DatagramChannel tunnel = DatagramChannel.open();

                    // Connect to the server, localhost is used for demonstration only.
                    tunnel.connect(new InetSocketAddress("127.0.0.1", 8080));

                    //d. Protect this socket, so package send by it will not be feedback to the vpn service.
                    protect(tunnel.socket());
                    if(tunnel.isConnected())
                    {
                        Toast.makeText(getApplicationContext(), "Connected to server locally hosted.", Toast.LENGTH_SHORT);
                    }

                        Log.d("Channel protected***","");

                    //e. Use a loop to pass packets.
                    while (true) {
                        //get packet with in
                        //put packet to tunnel
                        //get packet form tunnel
                        //return packet with out
                        //sleep is a must
                        Thread.sleep(100);

                    }

                } catch (Exception e) {
                    // Catch any exception
                    e.printStackTrace();
                } finally {
                    try {
                        if (mInterface != null) {
                            mInterface.close();
                            mInterface = null;
                        }
                    } catch (Exception e) {

                    }
                }
            }

        }, "MyVpnRunnable");

        //start the service
        mThread.start();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        if (mThread != null) {
            mThread.interrupt();
        }
        super.onDestroy();
    }

}