package com.sw.rpc.constant;

public class Constant {
    public static final int DEFAULT_RPC_PORT = 8080;
    public static final int DEFAULT_SERVER_NETTY_WORKER_NUM = 4;
    public static final int DEFAULT_CLIENT_NETTY_WORKER_NUM = 2;

    public static final int MAX_FRAME_LENGTH = 1024 * 1024;

    public static final int MESSAGE_MAGIC_NUM = 888;

    public static final int ZOOKEEPER_SESSION_TIME_OUT=2000;

    public static final String RPC_ROOT_PATH="/rpcServices";

    public static final String DEFAULT_SERVICE_IMPL_NAME="";

    public enum MessageVersion {
        MESSAGE_VERSION_1(1);
        private int version;

        MessageVersion(int version) {
            this.version = version;
        }

        public int getVersion() {
            return version;
        }
    }


    public enum RpcVersion {
        RPC_VERSION_1(1);

        private int version;

        RpcVersion(int version) {
            this.version = version;
        }

        public int getVersion() {
            return version;
        }
    }
}
