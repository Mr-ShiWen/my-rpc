package com.sw.rpc.utils;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

public class AddressUtil {
    public static void main(String[] args) {
        printAllAddresses();
    }

    private static void printAllAddresses() {
        try {
            Enumeration<NetworkInterface> faces = NetworkInterface.getNetworkInterfaces();
            // 遍历网络接口
            while (faces.hasMoreElements()) {
                NetworkInterface face = faces.nextElement();
                if (face.isLoopback() || face.isVirtual() || !face.isUp()) {
                    continue;
                }
                System.out.print("网络接口名：" + face.getDisplayName() + "，地址：");
                Enumeration<InetAddress> address = face.getInetAddresses();
                // 遍历网络地址
                while (address.hasMoreElements()) {
                    InetAddress addr = address.nextElement();
                    if (!addr.isLoopbackAddress() && addr.isSiteLocalAddress() && !addr.isAnyLocalAddress()) {
                        System.out.print(addr.getHostAddress());
                        if (addr instanceof Inet4Address) {
                            System.out.print("(ipv4)");
                        }
                        System.out.print(" ");
                    }
                }
                System.out.println("");
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    public static String getLocalHost()  {
        try {
            Enumeration<NetworkInterface> faces = NetworkInterface.getNetworkInterfaces();
            // 遍历网络接口
            while (faces.hasMoreElements()) {
                NetworkInterface face = faces.nextElement();
                if (face.isLoopback() || face.isVirtual() || !face.isUp()) {
                    continue;
                }
                if ("en0".equals(face.getDisplayName())) {
                    Enumeration<InetAddress> address = face.getInetAddresses();
                    // 遍历接口的网络地址
                    while (address.hasMoreElements()) {
                        InetAddress addr = address.nextElement();
                        if (!addr.isLoopbackAddress() && addr.isSiteLocalAddress() && !addr.isAnyLocalAddress()) {
                            return addr.getHostAddress();
                        }
                    }
                }
            }
            return "";
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }
    }


}
