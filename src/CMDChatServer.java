import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class CMDChatServer {
    private static final int START_PORT = 5556;
    private static final int MAX_PORT_TRIES = 10;

    public static void main(String[] args) {
        int port = START_PORT;
        ServerSocket serverSocket = null;
        boolean serverStarted = false;

        for (int i = 0; i < MAX_PORT_TRIES; i++) {
            try {
                serverSocket = new ServerSocket(port);
                serverStarted = true;
                break;
            } catch (IOException e) {
                port++;
            }
        }

        if (!serverStarted) {
            System.err.println("Não foi possivel iniciar o servidor todas as portas de " + START_PORT + " a "
                    + (START_PORT + MAX_PORT_TRIES) + " estao em uso");
            return;
        }

        List<PrintWriter> clients = new ArrayList<>();

        while (true) {
            try {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Conectado: " + clientSocket);

                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                clients.add(out);

                Thread clientThread = new Thread(new ClientHandler(clientSocket, out, clients));
                clientThread.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static class ClientHandler implements Runnable {
        private final Socket clientSocket;
        private final BufferedReader in;
        private final PrintWriter out;
        private final List<PrintWriter> clients;
        private String userName;

        public ClientHandler(Socket socket, PrintWriter out, List<PrintWriter> clients) throws IOException {
            this.clientSocket = socket;
            this.out = out;
            this.clients = clients;
            this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out.println("Por favor insira seu nome:");
            this.userName = in.readLine();
            out.println("Bem vimndo " + userName);
        }

        @Override
        public void run() {
            try {
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    System.out.println("Mensagem recebida de " + userName + ": " + inputLine);
                    broadcast(userName + ": " + inputLine);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    out.close();
                    in.close();
                    clientSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        private void broadcast(String message) {
            for (PrintWriter client : clients) {
                client.println(message);
            }
        }
    }
}
