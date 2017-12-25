import jm.music.data.Note;

import java.util.ArrayList;

/**
 * This class calculates error for pso algorithms using restrictions from Assigment2 and restrictons created by me
 */
public class ErrorCalculator {
    private static final int DIFFERENCE = 2;

    /**
     * Checks that this note
     * @param baseOfChord
     * @param note
     * @return
     */
    private static int inChord(Note baseOfChord, Note note) {
        int diff = Math.abs(note.getPitch() - baseOfChord.getPitch());
        if ((diff / 12 == DIFFERENCE && diff % 12 == 0) || ((diff + 4) / 12 == DIFFERENCE && (diff + 4) % 12 == 0) || ((diff + 7) / 12 == DIFFERENCE && (diff + 7) % 12 == 0)) {
            return 0;
        } else {
            if (note.getPitch() < baseOfChord.getPitch()) {
                return diff;
            } else {
                return Math.abs(note.getPitch() - (baseOfChord.getPitch() + 7));
            }
        }
    }

    /**
     * checks is it growing
     * @param note
     * @param nextNote
     */
    private static boolean growing(Note note, Note nextNote) {
        return note.getPitch() > nextNote.getPitch();
    }

    /**
     * checks is it falling
     * @param note
     * @param nextNote
     */
    private static boolean falling(Note note, Note nextNote) {
        return note.getPitch() < nextNote.getPitch();
    }

    /**
     * checks distance between neighbour notes
     * @param note
     * @param nextNote
     * @param max
     * @return if distance more than max return distance between notes
     */
    private static int dist(Note note, Note nextNote, int max) {
        if (Math.abs(note.getPitch() - nextNote.getPitch()) <= max) {
            return 0;
        }
        return Math.abs(note.getPitch() - nextNote.getPitch());
    }

    /**
     * checks is note repeats more than 3 times in row
     * @param chords
     * @param note
     * @param i
     */
    private static boolean notRepeate(ArrayList<Note> chords, Note note, int i) {
        return (i < 3 || (!(note.getPitch() == chords.get(i - 1).getPitch() && note.getPitch() == chords.get(i - 2).getPitch() && note.getPitch() == chords.get(i - 3).getPitch())));
    }

    /**
     * checks is this note in do major
     * @param note
     */
    private static boolean inDoMajor(Note note) {
        return remOnDiv12(note) == 0 || remOnDiv12(note) == 2 || remOnDiv12(note) == 4 || remOnDiv12(note) == 5 || remOnDiv12(note) == 7 || remOnDiv12(note) == 9 || remOnDiv12(note) == 11;
    }

    /**
     * calculates reminder on division on 12
     * @param note
     */
    private static int remOnDiv12(Note note) {
        return note.getPitch() % 12;
    }

    /**
     * checks is note in given range
     * @param note
     * @param lower_bound
     * @param upper_bound
     * @return
     */
    private static int InRange(Note note, int lower_bound, int upper_bound) {
        if (note.getPitch() >= upper_bound) {
            return upper_bound - note.getPitch();
        }
        if (note.getPitch() < lower_bound) {
            return lower_bound - note.getPitch();
        }
        return 0;
    }

    public static int acceptableChord(ArrayList<Note> chords, int i) {
        int error = 0;
        Note current;
        Note next;
        if (i == 0) {
            current = chords.get(i);
            next = chords.get(i + 1);
        } else {
            current = chords.get(i);
            next = chords.get(i - 1);
        }
        if (!(InRange(current, Generator.LOWER_BOUND, Generator.UPPER_BOUND) == 0)) {
            error += Math.abs(InRange(current, Generator.LOWER_BOUND, Generator.UPPER_BOUND));

        }
        if (!inDoMajor(current)) {
            error += 1;
        }
        if (!notRepeate(chords, current, i)) {
            error += 1;
        }
        if (!(dist(current, next, 12) == 0)) {
            error += Math.abs((-dist(current, next, 12)));
        }
        if (i <= 8) {
            if (!growing(current, next)) {
                error += Math.abs(((next).getPitch() - current.getPitch()));
            }
        } else {
            if (!falling(current, next)) {
                error += Math.abs((next.getPitch() - current.getPitch()));
            }
        }


        return error;
    }

    /**
     * calculates error for each note
     * @param chords
     * @param notes
     * @param i
     * @return error for single note
     */
    public static int acceptableNote(ArrayList<Note> chords, ArrayList<Note> notes, int i) {
        int error = 0;
        Note cur = notes.get(i);
        Note next;
        if (i == 0) {
            next = notes.get(i + 1);
        } else {
            next = notes.get(i - 1);
        }
        error = error + Math.abs(InRange(notes.get(i), Generator.LOWER_BOUND_NOTE, Generator.UPPER_BOUND_NOTE)) + Math.abs(dist(cur, next, 12));
        if (!notRepeate(notes, cur, i)) {
            error += 1;
        }
        if (!inDoMajor(cur)) {
            error += 1;
        }

        if (i % 2 == 0) {
            error += inChord(chords.get(i / 2), cur);
        } else {
            error += dist(cur, next, 4);
        }
        return error;
    }

    /**
     * calculates error for entire notes sequence
     * @param notes
     * @param chords
     * @return error of sequence of notes
     */
    public int errorNotes(ArrayList<Note> notes, ArrayList<Note> chords) {
        int error = 0;
        for (int i = 0; i < notes.size(); i++) {
            error += acceptableNote(chords, notes, i);
        }
        return error;
    }

    /**
     * calculates error for entire chords sequence
     * @param chords
     * @return error of sequence of chords
     */
    public  int error(ArrayList<Note> chords) {
        int error = 0;
        for (int i = 0; i < chords.size(); i++) {
            error += acceptableChord(chords, i);
        }
        return error;
    }
}
