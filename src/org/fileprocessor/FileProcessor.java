package org.fileprocessor;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
/**
 * This class is a customized version of buffer reader
 */
public class FileProcessor {
    private String inputFilePath = "/Users/srinivasbanoth/myprojects/file-processor/players.txt";//update with actual path
    private String tempDirecotry = "/Users/srinivasbanoth/myprojects/file-processor/output/";//update with actual path
    private String outputFilePath = "/Users/srinivasbanoth/myprojects/file-processor/playerssorted.txt";

    /**
     * Clears the temp directory.
     */
    public void clearDirectory() {
        File directory = new File(tempDirecotry);
        File[] files = directory.listFiles();
        for (File file : files) {
            if (file.exists()) file.delete();
        }
    }

    /**
     * Spits data from input file and saves to temp files and sorts and save to target/output file
     */
    public void sortFileData() {
        clearDirectory();//clears temp directory. directory path can be dynamic in real world scenario.
        int noOfLines = 100;//for testing purpose using 100 only. In real test scenario,
        // this can be as large as possible(depends on memory)
        //this can be divided based on memory also

        int index = 0; //for temp file naming


        try (BufferedReader br = new BufferedReader(new
                FileReader(inputFilePath))) {
            Set<Player> players = new TreeSet<>(Comparator.comparing(Player::getId));//using id as primary and unique. using it for sorting
            String line;
            while ((line = br.readLine()) != null && noOfLines > 0) {
                String[] splitStr = line.trim().split("\\s+");
                handle(splitStr, players);
                noOfLines--;
                if (noOfLines == 0) {//reset to initial value and write data to file
                    noOfLines = 100;
                    createTempFile(new StringBuilder(tempDirecotry).append(index).append(".txt").toString(), players);
                    players.clear();
                    index++;
                }
            }
            index++;
            createTempFile(new StringBuilder(tempDirecotry).append(index).append(".txt").toString(), players);
            players.clear();
            mergeData();//merge temp files data
            clearDirectory();

        } catch (Exception exception) {
            ///logging
            exception.printStackTrace();
            System.out.print("Exception occurred!!");//using sys out instead of logging as this is a standalone app

        }
    }

    private void handle(String[] attributes, Set<Player> players) {
        try {
            players.add(new Player(attributes[0], Integer.valueOf(attributes[1]), Integer.valueOf(attributes[2])));
        } catch (Exception exception) {
            exception.printStackTrace();
            ///logging
            System.out.print("Exception occurred!!");//using sys out instead of logging as this is a standalone app
        }

    }

    private void createTempFile(String fileName, Set<Player> players) throws Exception {
        System.out.println("Creating temp file to save data.");
        Files.write(Paths.get(fileName),
                players.parallelStream()
                        .map(player -> String.join(" ", player.getName(), Integer.toString(player.getAge()), Integer.toString(player.getId())))
                        .collect(Collectors.toList()), Charset.defaultCharset());

        System.out.println("Created temp file" + fileName);
    }

    public void mergeData() {
        File directory = new File(tempDirecotry);
        File[] files = directory.listFiles();
        PlayerBufferReader[] readers = new PlayerBufferReader[files.length];
        try {
            for (int i = 0; i < files.length; i++) {
                readers[i] = new PlayerBufferReader(new BufferedReader(new FileReader(files[i])));
            }
            File newFile = new File(outputFilePath);
            BufferedWriter writer = new BufferedWriter(new FileWriter(newFile));
            merge(writer, readers);
            writer.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void merge(BufferedWriter writer, PlayerBufferReader... in) throws IOException {
        List<PlayerBufferReader> list = new ArrayList<>(in.length);
        for (; ; ) {
            list.clear();
            for (PlayerBufferReader reader : in)
                if (reader.hasData()) {
                    int result = (list.isEmpty() ? 0 : reader.compareTo(list.get(0)));
                    if (result < 0)
                        list.clear();
                    if (result <= 0)
                        list.add(reader);
                }
            if (list.isEmpty())
                break; // processing completed.

            writer.write(list.get(0).geLine());
            writer.write(System.lineSeparator());
            for (PlayerBufferReader reader : list)
                reader.readNext();
        }
    }
}
