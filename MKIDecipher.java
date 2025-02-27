import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Decipher Myfaba sound tracks<br>
 * Grabs each byte and it transforms it following the custom mapping defined.
 */
public class MKIDecipher {

    private static final List<List<Integer>> byteHighNibble = Arrays.asList(
            Arrays.asList(0x30, 0x30, 0x20, 0x20, 0x10, 0x10, 0x00, 0x00, 0x70, 0x70, 0x60, 0x60, 0x50, 0x50, 0x40, 0x40, 0xB0, 0xB0, 0xA0, 0xA0, 0x90, 0x90, 0x80, 0x80, 0xF0, 0xF0, 0xE0, 0xE0, 0xD0, 0xD0, 0xC0, 0xC0),
            Arrays.asList(0x00, 0x00, 0x10, 0x10, 0x20, 0x20, 0x30, 0x30, 0x40, 0x40, 0x50, 0x50, 0x60, 0x60, 0x70, 0x70, 0x80, 0x80, 0x90, 0x90, 0xA0, 0xA0, 0xB0, 0xB0, 0xC0, 0xC0, 0xD0, 0xD0, 0xE0, 0xE0, 0xF0, 0xF0),
            Arrays.asList(0x10, 0x10, 0x00, 0x00, 0x30, 0x30, 0x20, 0x20, 0x50, 0x50, 0x40, 0x40, 0x70, 0x70, 0x60, 0x60, 0x90, 0x90, 0x80, 0x80, 0xB0, 0xB0, 0xA0, 0xA0, 0xD0, 0xD0, 0xC0, 0xC0, 0xF0, 0xF0, 0xE0, 0xE0),
            Arrays.asList(0x20, 0x20, 0x30, 0x30, 0x00, 0x00, 0x10, 0x10, 0x60, 0x60, 0x70, 0x70, 0x40, 0x40, 0x50, 0x50, 0xA0, 0xA0, 0xB0, 0xB0, 0x80, 0x80, 0x90, 0x90, 0xE0, 0xE0, 0xF0, 0xF0, 0xC0, 0xC0, 0xD0, 0xD0)
    );

    private static final List<List<Integer>> byteLowNibbleOdd = Arrays.asList(
            Arrays.asList(0x8, 0x9, 0xA, 0xB, 0xC, 0xD, 0xE, 0xF),
            Arrays.asList(0xD, 0xC, 0xF, 0xE, 0x9, 0x8, 0xB, 0xA),
            Arrays.asList(0x1, 0x0, 0x3, 0x2, 0x5, 0x4, 0x7, 0x6),
            Arrays.asList(0x8, 0x9, 0xA, 0xB, 0xC, 0xD, 0xE, 0xF)
    );

    private static final List<List<Integer>> byteLowNibbleEven = Arrays.asList(
            Arrays.asList(0x0, 0x1, 0x2, 0x3, 0x4, 0x5, 0x6, 0x7),
            Arrays.asList(0x5, 0x4, 0x7, 0x6, 0x1, 0x0, 0x3, 0x2),
            Arrays.asList(0x9, 0x8, 0xb, 0xa, 0xd, 0xc, 0xf, 0xe),
            Arrays.asList(0x0, 0x1, 0x2, 0x3, 0x4, 0x5, 0x6, 0x7)
    );

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: java MKIDecipher <input-file>");
            return;
        }

        String inputFileName = args[0];
        File inputFile = new File(inputFileName);
        String outputFileName = inputFileName + ".mp3";
        File outputFile = new File(outputFileName);

        try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(inputFile));
             BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(outputFile))) {

            int byteRead;
            int pos = 0;
            while ((byteRead = bis.read()) != -1) {
                int modifiedByte = findDecipheredData(pos, byteRead);

                // Write the modified byte to the output file.
                // System.out.println("pos: "+pos+"\tcipher: "+Integer.toHexString(byteRead)+"\torig: "+Integer.toHexString(modifiedByte));
                bos.write(modifiedByte);
                pos++;
            }

            bos.flush();
            System.out.println("File processed successfully. Output file: " + outputFileName);

        } catch (IOException e) {
            System.out.println("An error occurred: " + e.getMessage());
        }
    }

    /**
     * Based on the byte position and the value, it obtains the real byte
     * @param pos position in the file
     * @param value hex value
     * @return
     */
    private static int findDecipheredData(int pos, int value) {
        Integer highByte = (value & 0xF0);
        Integer lowByte = (value & 0x0F);
        int indexHigh = -1;
        int indexLow = -1;
        var posByte = pos % 4;
        indexHigh = byteHighNibble.get(posByte).indexOf(highByte);
        indexLow = byteLowNibbleEven.get(posByte).indexOf(lowByte);
        if (indexLow < 0) {
            indexLow = byteLowNibbleOdd.get(posByte).indexOf(lowByte);
            indexHigh++;
        }

        return indexLow * 32 + indexHigh;
    }
}
