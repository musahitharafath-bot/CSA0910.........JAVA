import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/** Q2: the playlist operations on a small real playlist (songs read from input/songs.txt). */
public class PlaylistDemo {
    public static void main(String[] args) throws Exception {
        List<String> songs = Files.readAllLines(Path.of("input/songs.txt"));
        List<String> playlist = new ArrayList<>();
        for (String s : songs.subList(0, 5)) playlist.add(s);          // add at the end
        playlist.add(2, "Believer");                                   // insert at a chosen position
        playlist.remove("Jai Ho");                                     // remove a song
        System.out.println("Playlist     : " + playlist);
        System.out.println("3rd song     : " + playlist.get(2));       // jump to the k-th song
        int total = 0;
        for (String s : playlist) total += s.length();
        System.out.println("Characters   : " + total);

        Deque<String> upNext = new ArrayDeque<>();                     // the 'Up Next' queue
        upNext.addLast("Perfect");
        upNext.addLast("Yellow");
        upNext.addFirst("Fix You");                                    // 'play next'
        System.out.println("Up Next      : " + upNext);
        System.out.println("Now playing  : " + upNext.pollFirst());
        System.out.println("Up Next now  : " + upNext);
    }
}
