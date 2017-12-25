import jm.music.data.*;
import jm.util.Write;

import java.util.ArrayList;
import java.util.Random;

import static jm.constants.Durations.EIGHTH_NOTE;
import static jm.constants.Durations.QUARTER_NOTE;
import static jm.constants.ProgramChanges.PIANO;

/**
 * This class generates melody using two pso algorithms
 * @author Salikhov Dilshat
 */
public class Generator {
    private static final int MAX_NUMBER_OF_ITERATION = 1000;
    private static final int POPULATION_SIZE = 5000;
    private static final int POPULATION_SIZE_OF_NOTES = 15000;
    private static final int AMOUNT_OF_ACCORDS = 16;
    private static final int AMOUNT_OF_NOTES = 32;
    private static final int DIFFERENCE = 2;
    public static final int LOWER_BOUND = 48;
    public static final int UPPER_BOUND = 71;
    public static final int LOWER_BOUND_NOTE = 72;
    public static final int UPPER_BOUND_NOTE = 95;

    public static void main(String[] args) {
        Score stochScore = new Score("JMDemo - Stochastic");
        Part inst = new Part("", PIANO, 0);
        Part melody = new Part("", PIANO, 0);
        ArrayList chords = pso1();
        inst.addCPhrase(makeChords(chords));
        ArrayList mel = pso2(chords);
        melody.addPhrase(makeNotes(mel));
        stochScore.addPart(inst);
        stochScore.addPart(melody);
        stochScore.setTempo(120);
        Write.midi(stochScore, "stochy17.mid");
    }

    /**
     * make note phrase from arraylist of notes
     * @param notes
     * @return phrase of notes
     */
    static Phrase makeNotes(ArrayList<Note> notes) {
        Phrase phr = new Phrase();
        for (Note note : notes) {
            phr.addNote(note);
        }
        return phr;
    }

    /**
     *  make chord phrase from arraylist of notes
     * @param chords
     * @return chord phrase
     */
    static CPhrase makeChords(ArrayList<Note> chords) {
        CPhrase chordPart = new CPhrase();
        Note[] pitchAr = new Note[3];
        for (Note p : chords) {
            pitchAr[0] = p;
            pitchAr[1] = new Note(p.getPitch() + 4, QUARTER_NOTE);
            pitchAr[2] = new Note(p.getPitch() + 7, QUARTER_NOTE);
            chordPart.addChord(pitchAr);
        }
        return chordPart;
    }

    /**
     * creates arrayList of notes generated randomly
     * @param lowerBound
     * @param upperBound
     * @param lenght
     * @param duration
     * @return arraylist of notes in given range
     */
    static ArrayList<Note> randomNote(int lowerBound, int upperBound, int lenght, double duration) {
        ArrayList<Note> notes = new ArrayList<>();
        Random random = new Random();
        for (int i = 0; i < lenght; i++) {
            Note note = new Note(random.nextInt(upperBound - lowerBound + 1) + lowerBound, duration);
            notes.add(note);
        }

        return notes;
    }

    /**
     * Find aesthetically pleasing sequence of chord bases (details in doc)
     * @return chords
     */
    static ArrayList pso1() {
        ArrayList<Note>[] chordsPopulation = new ArrayList[POPULATION_SIZE];
        ArrayList<Note>[] localBestCh = new ArrayList[POPULATION_SIZE];
        ErrorCalculator errorCalculator = new ErrorCalculator();
        for (int c = 0; c < chordsPopulation.length; c++) {
            chordsPopulation[c] = randomNote(LOWER_BOUND, UPPER_BOUND, 16, QUARTER_NOTE);
        }
        int[][] velocities = new int[POPULATION_SIZE][AMOUNT_OF_ACCORDS];
        ArrayList<Note> bestChords = chordsPopulation[0];
        for (int o = 0; o < POPULATION_SIZE; o++) {
            if (errorCalculator.error(chordsPopulation[o]) < errorCalculator.error(bestChords)) {
                bestChords = chordsPopulation[o];
            }
            localBestCh[o] = chordsPopulation[o];

        }
        int n = 0;
        while (errorCalculator.error(bestChords) != 0 && n < MAX_NUMBER_OF_ITERATION) {
            for (int o = 0; o < POPULATION_SIZE; o++) {
                if (errorCalculator.error(chordsPopulation[o]) < errorCalculator.error(bestChords)) {
                    bestChords = chordsPopulation[o];
                }
                if (errorCalculator.error(localBestCh[o]) > errorCalculator.error(chordsPopulation[o])) {
                    localBestCh[o] = chordsPopulation[o];
                }
            }
            for (int i = 0; i < chordsPopulation.length; i++) {
                for (int t = 0; t < AMOUNT_OF_ACCORDS; t++) {
                    int curBest = localBestCh[i].get(t).getPitch();
                    int cur = chordsPopulation[i].get(t).getPitch();
                    int globalBest = bestChords.get(t).getPitch();
                    if (errorCalculator.acceptableChord(chordsPopulation[i], t) == 0) {
                        velocities[i][t] = 0;
                    } else {
                        velocities[i][t] = (int) Math.ceil(0.9 * velocities[i][t] + Math.random() * (curBest - cur) + Math.random() * (globalBest - cur));
                    }
                    chordsPopulation[i].get(t).setPitch(cur + velocities[i][t]);
                }

            }
            n++;
        }
        for (int o = 0; o < AMOUNT_OF_ACCORDS; o++) {
            System.out.println(bestChords.get(o).getPitch());
        }
        System.out.println(n);
        return bestChords;
    }

    /**
     * Finds aesthetically pleasing melody (details in doc)
     * @param chords
     * @return melody
     */
    static ArrayList<Note> pso2(ArrayList<Note> chords) {
        System.out.println("Notes");
        ErrorCalculator errorCalculator = new ErrorCalculator();
        ArrayList<Note>[] notesPopulation = new ArrayList[POPULATION_SIZE_OF_NOTES];
        ArrayList<Note>[] localBestN = new ArrayList[POPULATION_SIZE_OF_NOTES];
        for (int c = 0; c < notesPopulation.length; c++) {
            notesPopulation[c] = randomNote(LOWER_BOUND_NOTE, UPPER_BOUND_NOTE, 32, EIGHTH_NOTE);
        }
        int[][] velocities = new int[POPULATION_SIZE_OF_NOTES][AMOUNT_OF_NOTES];
        ArrayList<Note> bestNotes = notesPopulation[0];
        for (int o = 0; o < POPULATION_SIZE_OF_NOTES; o++) {
            if (errorCalculator.errorNotes(notesPopulation[o], chords) < errorCalculator.errorNotes(bestNotes, chords)) {
                bestNotes = notesPopulation[o];
            }
            localBestN[o] = notesPopulation[o];
        }
        int n = 0;
        while (errorCalculator.errorNotes(bestNotes, chords) != 0 && n < MAX_NUMBER_OF_ITERATION) {
            for (int o = 0; o < POPULATION_SIZE_OF_NOTES; o++) {
                if (errorCalculator.errorNotes(notesPopulation[o], chords) < errorCalculator.errorNotes(bestNotes, chords)) {
                    bestNotes = notesPopulation[o];
                }
                if (errorCalculator.errorNotes(localBestN[o], chords) > errorCalculator.errorNotes(notesPopulation[o], chords)) {
                    localBestN[o] = notesPopulation[o];
                }
            }
            for (int i = 0; i < notesPopulation.length; i++) {
                for (int t = 0; t < AMOUNT_OF_NOTES; t++) {
                    int curBest = localBestN[i].get(t).getPitch();
                    int cur = notesPopulation[i].get(t).getPitch();
                    int globalBest = bestNotes.get(t).getPitch();
                    if (errorCalculator.acceptableNote(chords, notesPopulation[i], t) == 0) {
                        velocities[i][t] = 0;
                    } else {
                        velocities[i][t] = (int) Math.floor(0.9 * velocities[i][t] + Math.random() * (curBest - cur) + Math.random() * (globalBest - cur));
                    }
                    notesPopulation[i].get(t).setPitch(cur + velocities[i][t]);
                }

            }
            n++;
        }
        for (int o = 0; o < AMOUNT_OF_NOTES; o++) {
            System.out.println(bestNotes.get(o).getPitch());
        }
        System.out.println(n);
        return bestNotes;
    }
}
