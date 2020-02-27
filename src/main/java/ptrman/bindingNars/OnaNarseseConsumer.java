package ptrman.bindingNars;

import java.io.IOException;
import java.net.*;

public class OnaNarseseConsumer implements FormatedNarseseConsumer {
    public int quantization = 15;

    public String onaDestIp = "127.0.0.1"; // null if no data sent to ONA

    @Override
    public void emitLineSegment(String name, int posAX, int posAY, int posBX, int posBY, double conf) {
        String ser = "(" + (posAX/quantization)+"_"+(posAY/quantization)+" * " +(posBX/quantization)+"_" +(posBY/quantization) + ")";
        ser = ser.replace("-", "N"); // because ONA seems to have problem with minus

        String n = "<("+name+" * "+ser+") --> line >. :|:\0";
        System.out.println(n);

        byte[] buf = n.getBytes();
        DatagramSocket socket = null;
        try {
            socket = new DatagramSocket();
            DatagramPacket packet = new DatagramPacket(buf, buf.length, InetAddress.getByName(onaDestIp), 50000);
            socket.send(packet);
        }
        catch (SocketException e) {}
        catch (UnknownHostException e) {}
        catch (IOException e) {}
    }

    @Override
    public void emitLineIntersection(String nameA, String nameB) {
        String n = "<("+nameA+" * "+nameB+") --> lineIntrsctn>. :|:\0";

        byte[] buf = n.getBytes();
        DatagramSocket socket = null;
        try {
            socket = new DatagramSocket();
            DatagramPacket packet = new DatagramPacket(buf, buf.length, InetAddress.getByName(onaDestIp), 50000);
            socket.send(packet);
        }
        catch (SocketException e) {}
        catch (UnknownHostException e) {}
        catch (IOException e) {}
    }

    @Override
    public void flush() {
    }
}
