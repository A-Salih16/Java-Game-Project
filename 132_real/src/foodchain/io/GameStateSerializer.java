package foodchain.io;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.*;

import foodchain.board.Board;
import foodchain.core.GameEngine;
import foodchain.core.GameState;
import foodchain.core.TurnManager;
import foodchain.entities.Animal;
import foodchain.entities.Food;
import foodchain.model.Era;
import foodchain.model.GridSize;
import foodchain.model.Pos;
import foodchain.model.Role;

/**
 * Manages saving and loading the game state to/from a text file.
 * Serializes game objects into a custom key-value format.
 */
public class GameStateSerializer {

    /**
     * Saves the current state of the game engine to a file.
     * Writes header info (Era, Turn) followed by entity data.
     * @param file The destination file path.
     * @param engine The game engine instance to save.
     * @throws IOException If writing fails.
     */
    public static void save(Path file, GameEngine engine) throws IOException {
        Path parent=file.getParent();
        if(parent!=null) Files.createDirectories(parent);

        try(BufferedWriter w=Files.newBufferedWriter(file)) {
            GameState st=engine.getState();
            TurnManager tm=engine.getTurnManager();

            w.write("ERA="+st.getEra()); w.newLine();
            w.write("GRIDSIZE="+GridSize.fromSize(st.getBoard().getSize())); w.newLine();
            w.write("TOTALROUNDS="+tm.getTotalRounds()); w.newLine();
            w.write("TURN="+tm.getCurrentTurn()); w.newLine();
            w.write("ROUND="+tm.getRound()); w.newLine();

            writeAnimal(w, st.getApex());
            writeAnimal(w, st.getPredator());
            writeAnimal(w, st.getPrey());
            writeFood(w, st.getFood());
        }
    }
    
    /**
     * Loads a game state from a file and restores it into the engine.
     * Parses the custom text format to reconstruct objects.
     * @param file The save file path.
     * @param engine The engine to populate with loaded data.
     * @throws IOException If the file cannot be read.
     * @throws InvalidSaveFormatException If the file content is corrupted.
     */
    public static void load(Path file, GameEngine engine) throws IOException {
        List<String> lines;
        try {
            lines=Files.readAllLines(file);
        } catch(NoSuchFileException e) {
            throw new InvalidSaveFormatException("Save file not found: "+file.toAbsolutePath(),e);
        }
        
        Map<String,String> header=new HashMap<>();
        List<String> entityLines=new ArrayList<>();
        for(String raw:lines) {
            String line=raw.trim();
            if(line.isEmpty()) continue;
            if(line.contains("=") && !line.contains(",")) {
                String[] kv=line.split("=", 2);
                header.put(kv[0].trim(), kv[1].trim());
            } else {
                entityLines.add(line);
            }
        }
        
        Era era=parseEnum(Era.class, require(header,"ERA"),"ERA");
        GridSize gs=parseEnum(GridSize.class, require(header,"GRIDSIZE"),"GRIDSIZE");
        int totalRounds=parseInt(require(header,"TOTALROUNDS"),"TOTALROUNDS");
        Role turn=parseEnum(Role.class, require(header,"TURN"),"TURN");
        int round=parseInt(require(header,"ROUND"),"ROUND");

        Board board=new Board(gs.getSize());
        GameState st=new GameState(era, board, totalRounds);

        Map<String,String> apexM=null,predM=null,preyM=null,foodM=null;
        for(String line:entityLines) {
            Map<String,String> m=parseKV(line);
            String type=m.get("TYPE");
            if("APEX".equals(type)) apexM=m;
            else if("PREDATOR".equals(type)) predM=m;
            else if("PREY".equals(type)) preyM=m;
            else if("FOOD".equals(type)) foodM=m;
        }

        if(apexM==null || predM==null || preyM==null || foodM==null) {
            throw new InvalidSaveFormatException("Missing entity line(s) in save file");
        }

        Animal apex=buildAnimal(apexM, Role.APEX);
        Animal predator=buildAnimal(predM, Role.PREDATOR);
        Animal prey=buildAnimal(preyM, Role.PREY);

        String foodName=require(foodM,"name");
        int foodRow=parseInt(require(foodM,"row"),"food.row");
        int foodCol=parseInt(require(foodM,"col"),"food.col");
        Food food=new Food(foodName, new Pos(foodRow, foodCol));
        
        st.initEntities(prey, predator, apex, food);
        TurnManager tm=new TurnManager(totalRounds, turn, round);
        engine.loadFrom(st, tm);
    }
    
    private static String require(Map<String,String> m, String key) throws InvalidSaveFormatException {
        String v=m.get(key);
        if(v==null || v.isBlank()) throw new InvalidSaveFormatException("Missing header key: "+key);
        return v.trim();
    }

    private static int parseInt(String s, String where) throws InvalidSaveFormatException {
        try { return Integer.parseInt(s.trim()); }
        catch(Exception e) { throw new InvalidSaveFormatException("Invalid integer for "+where+": "+s, e); }
    }

    private static <E extends Enum<E>> E parseEnum(Class<E> cls, String s, String where) throws InvalidSaveFormatException {
        try { return Enum.valueOf(cls, s.trim()); }
        catch(Exception e) { throw new InvalidSaveFormatException("Invalid enum for "+where+": "+s, e); }
    }

    private static void writeAnimal(BufferedWriter w, Animal a) throws IOException {
        Pos p=a.getPos();
        w.write(a.getRole()+",name="+a.getName()
                +",score="+a.getScore()
                +",cooldown="+a.getAbilityCooldown()
                +",row="+p.getRow()
                +",col="+p.getCol());
        w.newLine();
    }

    private static void writeFood(BufferedWriter w, Food f) throws IOException {
        Pos p=f.getPos();
        w.write("FOOD,name="+f.getName()
                +",row="+p.getRow()
                +",col="+p.getCol());
        w.newLine();
    }

    private static Map<String,String> parseKV(String line) throws InvalidSaveFormatException {
        String[] parts=line.split(",");
        if(parts.length<1) throw new InvalidSaveFormatException("Malformed entity line: "+line);

        Map<String,String> m=new HashMap<>();
        m.put("TYPE",parts[0].trim());

        for(int i=1;i<parts.length;i++) {
            String[] kv=parts[i].split("=", 2);
            if(kv.length!=2) throw new InvalidSaveFormatException("Malformed kv: "+parts[i]+" in line: "+line);
            m.put(kv[0].trim(),kv[1].trim());
        }
        return m;
    }

    private static Animal buildAnimal(Map<String,String> m, Role role) throws InvalidSaveFormatException {
        String name=require(m,"name");
        int score=parseInt(require(m,"score"), role+".score");
        int cooldown=parseInt(require(m,"cooldown"), role+".cooldown");
        int row=parseInt(require(m,"row"), role+".row");
        int col=parseInt(require(m,"col"), role+".col");
        Animal a=new Animal(name, role, new Pos(row, col));
        a.addScore(score);
        a.setAbilityCooldown(cooldown);
        return a;
    }
}