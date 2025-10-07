package de.hsrm.mi.swt.snackman.entities.leaderboard;

import de.hsrm.mi.swt.snackman.services.LeaderboardService;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Represents an entry in the leaderboard, containing details about the player's name,
 * the duration of the gameplay, and the date the entry was recorded.
 */
public class LeaderboardEntry implements Comparable<LeaderboardEntry> {
    private String name;
    private LocalTime duration;
    private LocalDate releaseDate;

    public LeaderboardEntry(String name, String duration, String releaseDate) {
        this.name = name;
        this.duration = LocalTime.parse(duration);
        this.releaseDate = LocalDate.parse(releaseDate);
    }

    /**
     * Compares this leaderboard entry to another for sorting.
     * The comparison prioritizes duration, followed by release date, and then name.
     *
     * @param o the other {@link LeaderboardEntry} to compare against
     * @return a negative integer, zero, or a positive integer if this entry is less than,
     * equal to, or greater than the specified entry
     */
    @Override
    public int compareTo(@NotNull LeaderboardEntry o) {
        int durationComparison = this.duration.compareTo(o.duration);
        if (durationComparison != 0) {
            return durationComparison;
        }
        int releaseComparison = this.releaseDate.compareTo(o.releaseDate);
        if (releaseComparison != 0) {
            return releaseComparison;
        }
        return this.name.compareTo(o.name);
    }

    /**
     * Converts this leaderboard entry to a CSV-compatible string.
     *
     * @return a string formatted for CSV storage
     */
    public String getEntryAsFileLine() {
        return this.name + LeaderboardService.CSV_LINE_SPLITTER + this.duration.toString() + LeaderboardService.CSV_LINE_SPLITTER + this.releaseDate.toString() + "\n";
    }

    public String getName() {
        return name;
    }

    public LocalTime getDuration() {
        return duration;
    }

    public LocalDate getReleaseDate() {
        return releaseDate;
    }

    @Override
    public String toString() {
        return "LeaderboardEntry{" +
                "name='" + name + '\'' +
                ", duration=" + duration +
                ", releaseDate=" + releaseDate +
                '}';
    }
}
