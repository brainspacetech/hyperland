package com.brainspace.hyperland.utils;


import java.io.*;

import java.nio.charset.StandardCharsets;

public class PrintReceiptTemplate {
    private PrintReceiptTemplate() {
    }

    public static String getReceiptTemplate() throws IOException {
        ClassLoader classLoader = PrintReceiptTemplate.class.getClassLoader();
        InputStream inputStream = classLoader.getResourceAsStream("./payment.html");
        Reader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
        BufferedReader br = new BufferedReader(reader);
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            sb.append(line);
        }
        br.close();
        System.out.println(sb.toString());
        return sb.toString();
    }

    public static void main(String s[]) {
        try {
            getReceiptTemplate();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
