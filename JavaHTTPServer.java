import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.util.Date;
import java.util.StringTokenizer;


import java.nio.file.Paths;

public class JavaHTTPServer implements Runnable {
    static final File WEB_ROOT = new File(".");
    static final String DEFAULT_FILE = "index.html";
    static final String FILE_NOT_FOUND = "404.html";
    static final String METHOD_NOT_SUPPORTED = "not_supported.html";
    static final int PORT = 8080;

    static final boolean verbose = true;

    private Socket clientSocket;

    public JavaHTTPServer(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    public static void main(String[] args) {
        try {
            ServerSocket serverSocket = new ServerSocket(PORT);
            System.out.println("Server initiated");

            while (true) {
                System.out.println("Waiting for client connection...");
                JavaHTTPServer server = new JavaHTTPServer(serverSocket.accept());

                if (verbose) {
                    System.out.println("Connection opened. (" + new Date() + ")");
                }

                System.out.println("Attempting to begin thread");
                Thread thread = new Thread(server);
                thread.start();
            }
        } catch (IOException e) {
            System.err.println("Error with server connection: " + e.getMessage());
        }
    }

    @Override
    public void run() {
        System.out.println("Thread Begun");
        BufferedReader in = null;
        PrintWriter out = null;
        BufferedOutputStream dOut = null;
        String requestedFile = null;

        try {
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            out = new PrintWriter(clientSocket.getOutputStream());

            dOut = new BufferedOutputStream(clientSocket.getOutputStream());

            String input = in.readLine();
            System.out.println("Input: " + input);

            StringTokenizer parse = new StringTokenizer(input);
            String method = parse.nextToken().toUpperCase();
            System.out.println("method: " + method);

            requestedFile = parse.nextToken().toLowerCase();
            System.out.println("requested file: " + requestedFile);

            if (!method.equals("GET")) {
                System.out.println("501 Not Implemented : " + method + " method");

                File file = new File(WEB_ROOT, METHOD_NOT_SUPPORTED);
                int fileLength = (int) file.length();
                String contentMimeType = "text/html";

                byte[] fileData = readFileData(file, fileLength);

                out.println("HTTP/1.1 501 Not Implemented");
                out.println("Server: WebServer project, GVSU, Nathan Wichman");
                out.println("Date: " + new Date());
                out.println("Content-type: " + contentMimeType);
                out.println("Content-length: " + fileLength);
                out.println(); //Blank line between header and content
                out.flush(); //Clearing output stream buffer

                dOut.write(fileData, 0, fileLength);
                dOut.flush();
            } else {
                if (requestedFile.endsWith("/")) {
                    requestedFile += DEFAULT_FILE;
                }

                //File file = new File(WEB_ROOT, requestedFile);
                File file = new File("test.html");
                System.out.println(new String(Files.readAllBytes(Paths.get("test.html"))));
           //     int fileLength = (int) file.length();
             //   String content = getContentType(requestedFile);

              //  byte[] fileData = readFileData(file, fileLength);
                out.println("HTTP/1.1 200 OK");
                out.println("Server: WebServer project, GVSU, Nathan Wichman");
               // out.println("Content-type: " + content);
             //   out.println("Content-length: " + fileLength);
                out.println();
                out.flush();

              //  dOut.write(fileData, 0, fileLength);
              //  dOut.flush();

                if (verbose) {
                  //  System.out.println("File " + requestedFile + " of type " + content + " returned");
                }
            }
        } catch (FileNotFoundException e) {
            try {
                fileNotFound(out, dOut, requestedFile);
            } catch (IOException e2) {
                System.err.println("Error handling file not found exception: " + e.getMessage());
            }

        } catch (IOException e) {
            System.err.println("Server error : " + e);
        } finally {
            try {
                in.close();
                out.close();
                clientSocket.close();
                dOut.close();
            } catch (Exception e) {
                System.err.println("Error closing stream : " + e.getMessage());
            }
            if (verbose) {
                System.out.println("Connection closed.\n");
            }
        }
    }

    private byte[] readFileData(File file, int fileLength) throws IOException{
        FileInputStream fileIn = null;
        byte[] fileData = new byte[fileLength];

        try{
            fileIn = new FileInputStream(file);
            fileIn.read(fileData);
        }finally{
            if(fileIn != null){
                fileIn.close();
            }
        }
        return fileData;
    }

    private String getContentType(String requestedFile){
        if(requestedFile.endsWith(".htm") || requestedFile.endsWith(".html")){
            return "text/html";
        }else{
            return "text/plain";
        }
    }

    private void fileNotFound(PrintWriter out, OutputStream dOut, String requestedFile) throws IOException{
        File file = new File(WEB_ROOT, FILE_NOT_FOUND);
        int fileLength = (int) file.length();
        String content = "text/html";
        byte[] fileData = readFileData(file, fileLength);

        out.println("HTTP/1.1 0 File Not Found");
        out.println("Server: Java HTTP Server from Nate : 1.0");
        out.println("Date: " + new Date());
        out.println("Content-type: " + content);
        out.println("Content-length: " + fileLength);
        out.println();
        out.flush();

        dOut.write(fileData, 0, fileLength);
        dOut.flush();

        if(verbose){
            System.out.println("File " + requestedFile + " not found");
        }
    }
}
