// Pong — Programmazione Orientata agli Oggetti e Laboratori, Udine, a.a. 2026-27
package pong;

import giocatori.GiocatoreAutomatico;
import java.util.Random;
import ostacoli.Livello;
import ostacoli.MotoreDeiLivelli;
import ostacoli.Ostacoli;
import ostacoli.Ostacolo;

/**
 * Una partita a un livello: voi a sinistra, il giocatore automatico a destra, due racchette,
 * una palla, gli ostacoli del livello e i conti di chi vince. Il livello si sceglie prima di cominciare.
 */
class Partita {
    /** Si gioca a 5, con almeno due punti di scarto (5 a 4 non basta, 6 a 4 sì). */
    static final int PUNTI_PER_VINCERE = 5;
    static final int SCARTO_MINIMO = 2;

    private final Campo campo;
    private final Giocatore giocatoreUmano;
    private Giocatore giocatoreAutomatico;
    private Racchetta racchettaUmana ;
    private Racchetta racchettaAutomatica;
    private final Palla palla;
    private final Punteggio punteggio = new Punteggio(PUNTI_PER_VINCERE, SCARTO_MINIMO);
    private Livello livello;
    private Ostacoli ostacoli;
    private boolean iniziata;

    /** Una partita fra umano, a sinistra, e il giocatore automatico, a destra; si comincia dal livello 1. */
    Partita(Campo campo, Giocatore umano) {
        this.campo = campo;
        this.giocatoreUmano = umano;
        this.palla = new Palla(campo.getLarghezza() / 2.0, campo.getAltezza() / 2.0);
        preparaLivello(1);
    }

    /**
     * Prepara il livello numero: chiede al motore dei livelli ostacoli nuovi, velocità della palla ed
     * errore del giocatore automatico; rimette i punti a zero e la palla ferma al centro.
     * Pretende: la partita non è in corso; numero è fra 1 e MotoreDeiLivelli.NUMERO_LIVELLI.
     */
    void preparaLivello(int numero) {
        livello = MotoreDeiLivelli.livello(numero, campo.getLarghezza(), campo.getAltezza());
        ostacoli = livello.ostacoli();
        racchettaUmana = new Racchetta(livello.bordoUmano(), campo);
        racchettaAutomatica = new Racchetta(livello.bordoAutomatico(), campo);
        giocatoreAutomatico = new GiocatoreAutomatico(livello.erroreMassimo());
        punteggio.azzera();
        iniziata = false;
        palla.fermaIn(campo.getLarghezza() / 2.0, campo.getAltezza() / 2.0);
    }

    // Quello che lo Schermo può chiedere: solo leggere. Il Punteggio resta qui dentro.

    /** I punti del lato chi. */
    int getPunti(Lato chi) { return punteggio.getPunti(chi); }

    /** Chi ha vinto; null se la partita non è finita. */
    Lato getVincitore() { return punteggio.getVincitore(); }

    /** Vero se si sta giocando: la partita è cominciata e non è finita. */
    boolean isInCorso() { return iniziata && !punteggio.isFinita(); }

    /** Vero se la partita è cominciata (anche se poi è finita). */
    boolean isIniziata() { return iniziata; }

    /** Vero se su quel bordo non gioca nessuna racchetta, quindi la palla ci rimbalza. */
    boolean isMuro(Bordo bordo) {
        return bordo != livello.bordoUmano() && bordo != livello.bordoAutomatico();
    }

    Livello getLivello() { return livello; }
    Ostacoli getOstacoli() { return ostacoli; }

    /**
     * Fa cominciare la partita e lancia la palla. Se una partita era già stata giocata, prima
     * prepara di nuovo lo stesso livello (ostacoli nuovi, punti a zero).
     * Pretende: la partita non è in corso.
     */
    void avvia() {
        if (iniziata) preparaLivello(livello.numero());
        iniziata = true;
        Bordo bersaglio = new Random().nextBoolean() ? livello.bordoUmano() : livello.bordoAutomatico();
        lanciaPalla(bersaglio);
    }

    /** Lancia la palla dal centro, alla velocità del livello. */
    private void lanciaPalla(Bordo verso) {
        palla.lancia(campo.getLarghezza() / 2.0, campo.getAltezza() / 2.0, verso, livello.velocitaPalla());
    }

    /** Un passo del gioco: viene chiamato 60 volte al secondo. */
    void aggiorna() {
        if (!isInCorso()) return;
        ostacoli.avanzaTutti();
        muoviRacchette();
        palla.muovi();
        controllaRimbalzi();
        controllaOstacoli();
        controllaPunto();
    }

    /** Chiede a ciascun giocatore cosa vuole fare, e lo fa. Non sa chi sono i giocatori. */
    private void muoviRacchette() {
        Mossa mossaUmana = giocatoreUmano.decidiMossa(palla, racchettaUmana);
        Mossa mossaAutomatica = giocatoreAutomatico.decidiMossa(palla, racchettaAutomatica);
        racchettaUmana.muovi(mossaUmana, campo);
        racchettaAutomatica.muovi(mossaAutomatica, campo);
    }

    private void controllaRimbalzi() {
        double x = palla.getPosizione().x();
        double y = palla.getPosizione().y();
        int r = palla.getRaggio();

        if (isMuro(Bordo.ALTO) && y - r <= 0 && palla.getVelocitaY() < 0) palla.rimbalzaVerticale();
        if (isMuro(Bordo.BASSO) && y + r >= campo.getAltezza() && palla.getVelocitaY() > 0) palla.rimbalzaVerticale();
        if (isMuro(Bordo.SINISTRA) && x - r <= 0 && palla.getVelocitaX() < 0) palla.rimbalzoOrizzontale();
        if (isMuro(Bordo.DESTRA) && x + r >= campo.getLarghezza() && palla.getVelocitaX() > 0) palla.rimbalzoOrizzontale();

        controllaRimbalzoSu(racchettaUmana);
        controllaRimbalzoSu(racchettaAutomatica);
    }

    private void controllaRimbalzoSu(Racchetta racchetta) {
        if (!palla.tocca(racchetta)) return;
        Bordo b = racchetta.getBordo();
        boolean vaVersoLaRacchetta = (b == Bordo.SINISTRA && palla.getVelocitaX() < 0)
                || (b == Bordo.DESTRA && palla.getVelocitaX() > 0)
                || (b == Bordo.ALTO && palla.getVelocitaY() < 0)
                || (b == Bordo.BASSO && palla.getVelocitaY() > 0);
        if (vaVersoLaRacchetta) {
            palla.rimbalzaSu(racchetta);
        }
    }

    /**
     * Se la palla tocca un ostacolo non rotto, rimbalza e l'ostacolo viene colpito (un ostacolo per passo).
     * Qui non si sa di che tipo è l'ostacolo: che cosa fa il colpo lo decide l'ostacolo.
     */
    private void controllaOstacoli() {
        Ostacolo colpito = ostacoli.ostacoloCheTocca(palla.getArea());
        if (colpito != null) {
            palla.rimbalzaSu(colpito.getArea());
            colpito.colpisci(ostacoli);
        }
    }

    /** Se la palla è uscita da un lato, il punto va all'altro lato. */
    private void controllaPunto() {
        double x = palla.getPosizione().x();
        double y = palla.getPosizione().y();
        Bordo uscita = null;
        if (x < 0) uscita = Bordo.SINISTRA;
        else if (x > campo.getLarghezza()) uscita = Bordo.DESTRA;
        else if (y < 0) uscita = Bordo.ALTO;
        else if (y > campo.getAltezza()) uscita = Bordo.BASSO;

        if (uscita == null) return;

        if (uscita == livello.bordoUmano()) {
            punteggio.segnaPunto(Lato.DESTRA);
            dopoIlPunto(livello.bordoUmano());
        } else if (uscita == livello.bordoAutomatico()) {
            punteggio.segnaPunto(Lato.SINISTRA);
            dopoIlPunto(livello.bordoAutomatico());
        }
    }

    /** Dopo un punto: se la partita non è finita si serve verso chi ha subito il punto, altrimenti la palla si ferma. */
    private void dopoIlPunto(Bordo versoChiHaSubito) {
        if (punteggio.isFinita()) {
            palla.fermaIn(campo.getLarghezza() / 2.0, campo.getAltezza() / 2.0);
        } else {
            lanciaPalla(versoChiHaSubito);
        }
    }

    Campo getCampo() { return campo; }
    Palla getPalla() { return palla; }
    Racchetta getRacchettaUmana() { return racchettaUmana; }
    Racchetta getRacchettaAutomatica() { return racchettaAutomatica; }
}
