package org.fileprocessor;

import java.io.BufferedReader;
import java.io.IOException;
/**
 * This class is a customized version of buffer reader
 */
public class PlayerBufferReader implements Comparable<PlayerBufferReader> {

    private BufferedReader in;
    private String playerEntry;
    private int palyerId;

    PlayerBufferReader(BufferedReader in) throws IOException {
        this.in = in;
        readNext();
    }

    void readNext() throws IOException {
        if ((this.playerEntry = this.in.readLine()) == null)
            this.palyerId = Integer.MAX_VALUE;
        else
            this.palyerId = Integer.parseInt(this.playerEntry.split("\\s+")[2]);
    }

    boolean hasData() {
        return (this.playerEntry != null);
    }

    String geLine() {
        return this.playerEntry;
    }

    @Override
    public int compareTo(PlayerBufferReader that) {
        return Integer.compare(this.palyerId, that.palyerId);
    }
}
