import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Date;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

class Worker implements Runnable {

    int id;
    Socket s;
    String input, fileRequested;
    PrintWriter pr;
    BufferedReader in, temp;
    BufferedOutputStream bos = null;
    File indexFile = new File("index.html");
    File notFoundfile = new File("404.html");

    public Worker(Socket s, int id) {
        this.s = s;
        this.id = id;
    }

    @Override
    public void run() {
        while (true) {
            try {
                in = new BufferedReader(new InputStreamReader(s.getInputStream()));
                bos = new BufferedOutputStream(s.getOutputStream());
                pr = new PrintWriter(s.getOutputStream());
                temp = in;

                input = temp.readLine();
                StringTokenizer parse = new StringTokenizer(input);
                String method = parse.nextToken().toUpperCase();
                fileRequested = parse.nextToken().toLowerCase();
                if (method.equals("GET")) {
                    respondGET(fileRequested);
                } else if (method.equals("POST")) {
                    respondPOST(in);
                }

                /*System.out.println("Here Input : " + input);
                while (!input.isEmpty() && null != input) {
                    input = temp.readLine();
                    System.out.println(input);
                }*/
            } catch (IOException ex) {
            } finally {
                try {
                    s.close();
                } catch (IOException ex) {
                    Logger.getLogger(Worker.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    void respondGET(String fileName) throws IOException {
        if (fileName.equals("/")) {
            loadFile(makehtmlFile("", "GET"), "text/html");
        } else if (searchFile(fileName.substring(1)) == true) {
            loadFile(new File(fileName.substring(1)), getMimeType(fileName));
        } else {
            fileNotFound();
        }

    }

    String getMimeType(String fileName) {
        fileName = fileName.substring(1);
        String mimeType = "";
        String extension = "";
        StringTokenizer parse = new StringTokenizer(fileName, ".");
        while (parse.hasMoreTokens()) {
            extension = parse.nextToken();
        }
        switch (extension) {
            case "html":
                mimeType = "text/html";
                break;
            case "txt":
                mimeType = "text/plain";
                break;
            case "pdf":
                mimeType = "application/pdf";
                break;
            case "gif":
                mimeType = "image/gif";
                break;
            case "jpg":
                mimeType = "image/jpeg";
                break;
            default:
                mimeType = "text/html";
                break;
        }

        return mimeType;
    }

    void respondPOST(BufferedReader input) throws IOException {
        String line;
        int contLength = 0;
        while ((line = input.readLine()).length() != 0) {
            if (line.startsWith("Content-Length")) {
                contLength = Integer.parseInt(line.substring(16, line.length()));
            }
            if (line != null) {
                System.out.println(line);
            }
        }
        BufferedReader br = new BufferedReader(in);
        int c;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < contLength; i++) {
            c = br.read();
            sb.append((char) c);
        }
        String username = sb.substring(5);
        loadFile(makehtmlFile(username,"POST"),"text/html");

    }

    File makehtmlFile(String name,String method) throws IOException {
        File temp = new File("temp.html");
        FileWriter fileWriter = new FileWriter(temp);
        PrintWriter printWriter = new PrintWriter(fileWriter);
        printWriter.println("<html>");
        printWriter.println("<head>");
        printWriter.println("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">");
        printWriter.println("</head>");
        printWriter.println("<body>");
        printWriter.println("<h1> Welcome to CSE 322 Offline 1</h1>");
        printWriter.println("<h2> HTTP REQUEST TYPE-> "+method+"</h2>");
        printWriter.println("<h2> Post-> "+name+"</h2>");
        printWriter.println("<form name=\"input\" action=\"http://localhost:8080/form_submited\" method=\"post\">");
        printWriter.println("Your Name: <input type=\"text\" name=\"user\">");
        printWriter.println("<input type=\"submit\" value=\"Submit\">");
        printWriter.println("</form>");
        printWriter.println("</body>");
        printWriter.println("</html>");
        printWriter.close();
        return temp;
    }

    void loadFile(File file, String mimeType) throws IOException {
        int fileLength = (int) file.length();
        byte[] fileData = readFile(file, fileLength);
        pr.println("HTTP/1.1 200 OK");
        pr.println("Server: Java HTTP Server : 1.0");
        pr.println("Date: " + new Date());
        pr.println("Content-type: " + mimeType);
        pr.println("Content-length: " + fileLength);
        pr.println(); // blank line between headers and content, very important !
        pr.flush(); // flush character output stream buffer
        bos.write(fileData, 0, fileLength);
        bos.flush();
    }

    void fileNotFound() throws IOException {
        File file = notFoundfile;
        int fileLength = (int) file.length();
        byte[] fileData = readFile(file, fileLength);
        pr.println("HTTP/1.1 404 File Not Found");
        pr.println("Server: Java HTTP Server : 1.0");
        pr.println("Date: " + new Date());
        pr.println("Content-type: " + "text/html");
        pr.println("Content-length: " + fileLength);
        pr.println(); // blank line between headers and content, very important !
        pr.flush(); // flush character output stream buffer
        bos.write(fileData, 0, fileLength);
        bos.flush();
    }

    boolean searchFile(String fileName) {
        File f = new File(".");
        File[] files = f.listFiles();
        for (File file : files) {
            if (file.getName().equalsIgnoreCase(fileName)) {
                return true;
            }
        }
        return false;
    }

    private byte[] readFile(File file, int fileLength) throws IOException {
        FileInputStream fileIn = null;
        byte[] fileData = new byte[fileLength];
        try {
            fileIn = new FileInputStream(file);
            fileIn.read(fileData);
        } finally {
            if (fileIn != null) {
                fileIn.close();
            }
        }
        return fileData;
    }

}