import com.sun.org.apache.xerces.internal.impl.dv.util.HexBin;

import javax.xml.bind.annotation.adapters.HexBinaryAdapter;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public class ProtocolHeaderAnalysis {
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Invalid arguments provided. \n Need at least full file name");
            return;
        }

        String packetString = readFile("packets/" + args[0], StandardCharsets.UTF_8);
        String[] packetBytes = parseBytes(packetString);

        printStringArray(packetBytes, 0, packetBytes.length);

        ethernetHeader(packetBytes);
        IPHeader(packetBytes);
        TCPHeader(packetBytes);

        data(packetBytes);

        System.out.println("Please note that this frame parser only works with TCP for layer 4!!!");
        System.out.println("More layers to be added later, when exams are done!!!");

    }

    private static void data(String[] bytes) {
        System.out.println("Data:");
        printStringArray(bytes, 54, bytes.length);

        System.out.println("\n");
    }

    private static void IPHeader(String[] bytes) {
        int version = Integer.decode("0x" + bytes[14].substring(0,1));
        int IHL = Integer.decode("0x" + bytes[14].substring(1,2)) * 4;

        System.out.println("IP version:");
        System.out.println(version);

        System.out.println("IP header length:");
        System.out.println(IHL);

        String TOS = convertByteToBinaryString(bytes[15]);

        System.out.println("Precedence:");
        precedence(TOS);

        TOS(TOS);

        System.out.println("Length of IP datagram:");
        datagramLength(bytes);

        System.out.println("Identification:");
        identification(bytes);

        System.out.println("Flags");
        flagsAndFrag(bytes);

        TTL(bytes[22]);

        System.out.println("Layer 4 protocol:");
        protocol(bytes[23]);

        System.out.println("Checksum:");
        printStringArray(bytes, 24, 26);

        System.out.println("Source IP address:");
        printStringArray(bytes, 26, 30);

        System.out.println("Destination IP address:");
        printStringArray(bytes, 30, 34);

        System.out.print("\n");
    }

    private static void flagsAndFrag(String[] bytes) {
        String binaryString = Integer.toBinaryString(Integer.decode("0x" + bytes[20] + bytes[21]));

        while (binaryString.length() < 16) {
            binaryString = "0" + binaryString;
        }

        System.out.println("Don't Fragment: \n" + (binaryString.charAt(1) == '1' ? "True" : "False"));
        System.out.println("More Fragment: \n" + (binaryString.charAt(2) == '1' ? "True" : "False"));

        System.out.println("Fragment Offset: \n" + Integer.parseInt(binaryString.substring(3,16), 2));

        System.out.println("Time to live:");
    }

    private static void TCPHeader(String[] bytes) {
        portAddresses(bytes);
        seqAndAck(bytes);
        dataOffset(bytes[46]);
        controlFlags(bytes[47]);
        receiverWindowSize(bytes);

        System.out.println("TCP checksum:");
        printStringArray(bytes, 50, 52);

        System.out.println("Urgent pointer:");
        printStringArray(bytes, 52, 54);

        System.out.println("\n");
    }

    private static void receiverWindowSize(String[] bytes) {
        String hex = "0x" + bytes[48] + bytes[49];

        System.out.println("Receiver window size:");
        System.out.println(Integer.decode(hex) + " (" + hex + ")");
    }

    private static void controlFlags(String byteString) {
        String binaryString = convertByteToBinaryString(byteString);

        System.out.println("Flags set: ");

        for (int i=2; i<binaryString.length(); i++) {
            switch (i) {
                case 2:
                    System.out.print("URG – " + binaryString.charAt(i) + ", ");
                    break;
                case 3:
                    System.out.print("ACK – " + binaryString.charAt(i) + ", ");
                    break;
                case 4:
                    System.out.print("PSH – " + binaryString.charAt(i) + ", ");
                    break;
                case 5:
                    System.out.print("RST – " + binaryString.charAt(i) + ", ");
                    break;
                case 6:
                    System.out.print("SYN – " + binaryString.charAt(i) + ", ");
                    break;
                case 7:
                    System.out.print("FIN – " + binaryString.charAt(i) + "\n");
                    break;
            }
        }
    }

    private static void dataOffset(String byteString) {
        System.out.println("Data offset:");
        System.out.println(Integer.decode("0x" + byteString.substring(0,1)) * 4);
    }

    private static void seqAndAck(String[] bytes) {
        String seqHex = bytes[38] + bytes[39] + bytes[40] + bytes[41];
        String ackHex = bytes[42] + bytes[43] + bytes[44] + bytes[45];

        System.out.println("Sequence number:");
        System.out.println(new BigInteger(seqHex, 16) + " (0x" + seqHex + ")");

        System.out.println("Acknowledgement number:");
        System.out.println(new BigInteger(ackHex, 16) + " (0x" + ackHex + ")");
    }

    private static void portAddresses(String[] bytes) {
        String sourceHex = "0x" + bytes[34] + bytes[35];
        String destHex = "0x" + bytes[36] + bytes[37];

        System.out.println("Source port:");
        System.out.println(Integer.decode(sourceHex) + " (" + sourceHex + ")");

        System.out.println("Destination port:");
        System.out.println(Integer.decode(destHex) + " (" + destHex + ")");
    }

    // TODO: investigate IPv4 value of 0
    private static void protocol(String byteString) {
        int val = Integer.decode("0x" + byteString);

        switch (val) {
            case 0:
                System.out.println("0 – HOPOPT");
                break;
            case 17:
                System.out.println("17 – UDP");
                break;
            case 6:
                System.out.println("6 – TCP");
                break;
            case 1:
                System.out.println("1 – TCMP");
                break;
            case 88:
                System.out.println("88 – IGRP");
                break;
            case 89:
                System.out.println("89 – OSPF");
                break;
            default:
                System.out.println("Unidentified protocol");
                break;
        }
    }

    private static void TTL(String byteString) {
        String hex = "0x" + byteString;
        System.out.println(Integer.decode(hex) + " (" + hex + ")");
    }

    private static void identification(String[] bytes) {
        String hex = "0x" + bytes[18] + bytes[19];
        System.out.println(hex);
    }

    private static void datagramLength(String[] bytes) {
        String hex = "0x" + bytes[16] + bytes[17];
        System.out.println(Integer.decode(hex) + " (" + hex + ")");
    }

    // DTR stands for delay, throughput, reliability
    private static void TOS(String byteString) {
        char[] dtr = byteString.substring(3,6).toCharArray();

        System.out.println("Delay: \n" + (dtr[0] == '0' ? "Normal" : "High"));
        System.out.println("Throughput: \n" + (dtr[1] == '0' ? "Normal" : "High"));
        System.out.println("Reliability: \n" + (dtr[2] == '0' ? "Normal" : "High"));

        System.out.println("Last two bits: \n" + byteString.substring(6,8));
    }

    private static void precedence(String byteString) {
        int val = Integer.decode(byteString.substring(0,3));

        switch (val) {
            case 0:
                System.out.println("000 – Routine");
                break;
            case 1:
                System.out.println("001 – Priority");
                break;
            case 2:
                System.out.println("010 – Immediate");
                break;
            case 3:
                System.out.println("011 – Flash");
                break;
            case 4:
                System.out.println("100 – Flash Override");
                break;
            case 5:
                System.out.println("101 – Critical");
                break;
            case 6:
                System.out.println("110 – Internetwork Protocol");
                break;
            case 7:
                System.out.println("111 – Network Control");
                break;
            default:
                System.out.println("Unidentified precedence");
        }
    }

    private static String convertByteToBinaryString(String byteString) {
        String binaryString = Integer.toBinaryString(Integer.decode("0x"+byteString));

        while (binaryString.length() < 8) {
            binaryString = "0" + binaryString;
        }

        return binaryString;
    }

    private static void ethernetHeader(String[] bytes) {
        System.out.println("Ethernet destination address:");
        printStringArray(bytes, 0, 6);

        System.out.println("Ethernet source address:");
        printStringArray(bytes, 6, 12);

        System.out.println("Payload type: ");
        System.out.println("0x" + bytes[12] + bytes[13]);

        System.out.print("\n");
    }

    private static String readFile(String path, Charset encoding){
        try {
            byte[] encoded = Files.readAllBytes(Paths.get(path));
            return new String(encoded, encoding);
        } catch (IOException e) {
            System.out.println("Failed to read from file: " + e.getMessage());
            return null;
        }
    }

    private static String[] parseBytes(String packet) {
        packet = packet.replace(" ", "").replace("\n", "");
        return packet.split("(?<=\\G.{2})");
    }

    private static void printStringArray(String[] strArray, int start, int end) {
        for (int i=start; i < strArray.length && i < end; i++) {
            System.out.print(strArray[i] + " ");
        }

        System.out.print("\n");
    }
}