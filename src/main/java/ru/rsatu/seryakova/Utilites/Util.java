package ru.rsatu.seryakova.Utilites;

import javax.ws.rs.core.MultivaluedMap;
import java.io.*;

public class Util {
    public String parseFileName(MultivaluedMap<String, String> headers) {
        String[] contentDispositionHeader = headers.getFirst("Content-Disposition").split(";");
        for (String name : contentDispositionHeader) {
            if ((name.trim().startsWith("filename"))) {
                String[] tmp = name.split("=");
                return tmp[1].trim().replaceAll("\"","");
            }
        }
        return "randomName";
    }
    // save uploaded file to a defined location on the server
    public void saveFile(InputStream uploadedInputStream, String serverLocation) {
        try {
            int read = 0;
            byte[] bytes = new byte[1024];
            OutputStream outpuStream = new FileOutputStream(new File(serverLocation));
            while ((read = uploadedInputStream.read(bytes)) != -1) {
                outpuStream.write(bytes, 0, read);
            }
            outpuStream.flush();
            outpuStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
