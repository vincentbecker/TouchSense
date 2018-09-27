package ch.ethz.inf.vs.fingerforce.lifx;

import android.os.AsyncTask;

import java.net.DatagramPacket;
import java.net.DatagramSocket;

// Thread to send message
class MessageSendTask extends AsyncTask<Message, Integer, Boolean> {

    @Override
    protected Boolean doInBackground(Message... messages) {
		DatagramSocket dataGramSocket = null;
        try {
            //Log.d("MESSAGESENDTASK", "datagram socket created");
            dataGramSocket = new DatagramSocket();
            dataGramSocket.setBroadcast(true);
            dataGramSocket.setReuseAddress(true);
            // Send all messages
            for (Message message : messages) {
                DatagramPacket udpPacket = new DatagramPacket(message.getMessageData(),
                        message.getMessageData().length, message.getIpAddress(), message.getPort());
                if (!dataGramSocket.isClosed()) {
                    dataGramSocket.send(udpPacket);
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            dataGramSocket.close();
        }
        return null;
	}		
}