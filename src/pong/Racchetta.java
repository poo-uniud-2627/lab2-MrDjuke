// Pong — Programmazione Orientata agli Oggetti e Laboratori, Udine, a.a. 2026-27
package pong;

/** Una racchetta: sta a un'altezza fissa in orizzontale e si muove solo in verticale. */
public class Racchetta {
    static final int LARGHEZZA = 12;
    static final int ALTEZZA = 80;
    static final int DISTANZA_DAL_BORDO = 30;
    /** Quanti pixel si sposta la racchetta a ogni fotogramma, quando si muove. */
    static final int VELOCITA = 6;

    private final Bordo bordo;
    private double x;
    private double y;
    private final int larghezza;
    private final int altezza;

    Racchetta(Bordo bordo, Campo campo) {
        if (bordo == null) throw new IllegalArgumentException("bordo non puo essere null");
        if (campo == null) throw new IllegalArgumentException("campo non puo essere null");
        this.bordo = bordo;
        if(bordo.orizzontale()){
            this.larghezza = ALTEZZA;
            this.altezza = LARGHEZZA;
            this.x = campo.getLarghezza() / 2.0 - this.larghezza / 2.0;
            if(bordo == Bordo.ALTO){
                this.y = DISTANZA_DAL_BORDO;
            } else {
                this.y = campo.getAltezza() - DISTANZA_DAL_BORDO - this.altezza;
            }
        } else {
            this.larghezza = LARGHEZZA;
            this.altezza = ALTEZZA;
            this.y = campo.getAltezza() / 2.0 - this.altezza / 2.0;
            if (bordo == Bordo.SINISTRA){
                this.x = DISTANZA_DAL_BORDO;
            } else {
                this.x = campo.getLarghezza() - DISTANZA_DAL_BORDO - this.larghezza;
            }
        }        
    }

    // ---- quello che chiunque può chiedere alla racchetta
    
    public Bordo getBordo() {return bordo; }
    public double getX() { return x; }
    /** Il bordo alto della racchetta (le y crescono verso il basso). */
    public double getY() { return y; }
    public double getCentroX() { return x + larghezza / 2.0; }
    public double getCentroY() { return y + altezza / 2.0; }
    public int getLarghezza() { return larghezza; }
    public int getAltezza() { return altezza; }

    // ---- quello che può chiedere solo il codice del pacchetto pong (la Partita)

    /**
     * Esegue una mossa.
     * Pretende: campo non è null ed è alto almeno quanto la racchetta; la racchetta è dentro il campo.
     * Promette: con SU sale di VELOCITA pixel, con GIU scende, con FERMO (o null) resta dov'è;
     *           alla fine sta tutta dentro il campo.
     */
    void muovi(Mossa mossa, Campo campo) {
        if (bordo.orizzontale()) {
            if (mossa == Mossa.SU) x -= VELOCITA;
            if (mossa == Mossa.GIU) x += VELOCITA;
            if (x < 0) x = 0;
            if (x > campo.getLarghezza() - larghezza) x = campo.getLarghezza() - larghezza;
        } else {
            if (mossa == Mossa.SU) y -= VELOCITA;
            if (mossa == Mossa.GIU) y += VELOCITA;
            if (y < 0) y = 0;
            if (y > campo.getAltezza() - altezza) y = campo.getAltezza() - altezza;
        }
    }
}
