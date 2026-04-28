package Services;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public class HerculesTcpClient implements AutoCloseable {

    public interface Listener {
        void onConnected();

        void onDisconnected();

        void onMessage(String message);

        void onError(String message, Throwable error);
    }

    private final String host;
    private final int port;
    private final Listener listener;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final ExecutorService ioExecutor = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "hercules-tcp-client");
        t.setDaemon(true);
        return t;
    });

    private Socket socket;
    private OutputStream out;

    public HerculesTcpClient(String host, int port, Listener listener) {
        this.host = Objects.requireNonNull(host, "host");
        this.port = port;
        this.listener = Objects.requireNonNull(listener, "listener");
    }

    public void connectAsync(int connectTimeoutMs) {
        if (!running.compareAndSet(false, true)) {
            return;
        }

        ioExecutor.submit(() -> {
            try {
                socket = new Socket();
                socket.connect(new InetSocketAddress(host, port), connectTimeoutMs);
                socket.setTcpNoDelay(true);
                out = socket.getOutputStream();
                listener.onConnected();

                // Hercules often sends data without a trailing newline, so don't rely on readLine().
                try (InputStream in = socket.getInputStream()) {
                    byte[] buf = new byte[4096];
                    while (running.get()) {
                        int n = in.read(buf);
                        if (n == -1) {
                            break;
                        }

                        String chunk = new String(buf, 0, n, StandardCharsets.UTF_8);
                        // If we got line terminators, split nicely; otherwise emit the chunk as-is.
                        if (chunk.indexOf('\n') >= 0 || chunk.indexOf('\r') >= 0) {
                            String normalized = chunk.replace("\r\n", "\n").replace('\r', '\n');
                            for (String part : normalized.split("\n", -1)) {
                                if (!part.isEmpty()) {
                                    listener.onMessage(part);
                                }
                            }
                        } else if (!chunk.isEmpty()) {
                            listener.onMessage(chunk);
                        }
                    }
                }
            } catch (Exception e) {
                listener.onError("Failed to connect/read from " + host + ":" + port, e);
            } finally {
                disconnectInternal();
            }
        });
    }

    public void sendLine(String message) throws IOException {
        if (!isConnected()) {
            throw new IOException("Not connected");
        }
        String payload = message + "\r\n";
        out.write(payload.getBytes(StandardCharsets.UTF_8));
        out.flush();
    }

    public boolean isConnected() {
        return socket != null && socket.isConnected() && !socket.isClosed();
    }

    public void disconnect() {
        running.set(false);
        disconnectInternal();
    }

    private void disconnectInternal() {
        try {
            if (socket != null) {
                socket.close();
            }
        } catch (IOException ignored) {
        } finally {
            socket = null;
            out = null;
            listener.onDisconnected();
        }
    }

    @Override
    public void close() {
        disconnect();
        ioExecutor.shutdownNow();
    }
}
