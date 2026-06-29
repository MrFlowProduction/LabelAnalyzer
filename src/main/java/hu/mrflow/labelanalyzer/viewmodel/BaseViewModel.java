package hu.mrflow.labelanalyzer.viewmodel;

/**
 * Közös ős minden ViewModel-hez.
 * dispose() felszabadítja a háttérszál-kötéseket ha szükséges.
 */
public abstract class BaseViewModel {

    /**
     * Hívd meg amikor a ViewModel-t el kell dobni (ablak bezárása, projekt törlése stb.)
     * Felülírandó ha a leszármazott saját erőforrásokat tart.
     */
    public void dispose() {
        // alapértelmezetten üres
    }
}
