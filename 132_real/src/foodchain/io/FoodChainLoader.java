package foodchain.io;

import foodchain.model.Era;
import foodchain.model.FoodChain;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Utility class to load food chain definitions from text files.
 * Reads configuration files based on the selected Era.
 */
public class FoodChainLoader {

    /**
     * Parses the era-specific text file to create a list of valid food chains.
     * Expects files named like "past.txt", "present.txt".
     * @param era The game era to load data for.
     * @return A list of FoodChain objects containing animal names.
     * @throws IOException If the file is missing or cannot be read.
     */
    public static List<FoodChain> load(Era era) throws IOException {
        List<FoodChain> list=new ArrayList<>();
        
        String filename=era.name().toLowerCase(Locale.ROOT)+".txt";
        Path file=Path.of("data", filename);

        if(!Files.exists(file)) {
            throw new IOException("File not found for era: "+filename);
        }

        for(String line : Files.readAllLines(file)) {
            line=line.trim();
            if(line.isEmpty()) continue;
            
            if(line.toLowerCase(Locale.ROOT).startsWith("food chain")) {
                String content=line.substring(line.indexOf(':')+1).trim();
                String[] parts=content.split(",");

                if(parts.length!=4) {
                    throw new IllegalArgumentException("Invalid format in "+filename+": "+line);
                }
                
                FoodChain fc=new FoodChain(
                    parts[0].trim(), 
                    parts[1].trim(), 
                    parts[2].trim(),
                    parts[3].trim()  
                );
                list.add(fc);
            }
        }
        return list;
    }
}