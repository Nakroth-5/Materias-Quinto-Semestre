package org.universidad.granm.claseabstracta;

/**
 * Implementación del método Simplex estándar para problemas con restricciones <=
 */
public class MSimplexEstandar extends SimplexBase {

    public MSimplexEstandar(double[] funcionObjetivo, double[][] restricciones,
                            double[] terminosIndependientes, String[] tipoRestriccion, boolean maximizar) {
        super();
        this.maximizar = maximizar;
        this.nroRestricciones = restricciones.length;
        this.nroVariables = funcionObjetivo.length;

        inicializarTableau(funcionObjetivo, restricciones, terminosIndependientes, tipoRestriccion);
        guardarPaso("Tabla inicial", "", "");
    }

    protected void inicializarTableau(double[] funcionObjetivo, double[][] restricciones,
                                      double[] terminosIndependientes, String[] tipoRestriccion) {
        inicializarTableauBase(funcionObjetivo, restricciones, terminosIndependientes, tipoRestriccion);

        // Fila de función objetivo
        setValor(0, 0, 1);
        for (int j = 0; j < nroVariables; j++)
            setValor(0, j + 1, -funcionObjetivo[j]);

        setValor(0, nroColumnas - 1, 0);

        int colHolgura = nroVariables + 1;

        // Filas de restricciones
        for (int i = 0; i < nroRestricciones; i++) {
            setValor(i + 1, 0, 0);
            for (int j = 0; j < nroVariables; j++)
                setValor(i + 1, j + 1, restricciones[i][j]);

            setValor(i + 1, colHolgura + i, 1);

            setValor(i + 1, nroColumnas - 1, terminosIndependientes[i]);
        }
    }

    @Override
    public void resolver() {
        if (!maximizar && !existenPositivosEnlaFuncionObjetivo()) {
            finalizarSolucion();
            return;
        }

        while (!esOptimo() && solucionEncontrada) {
            iteracionSimplex();
        }

        if (solucionEncontrada) {
            finalizarSolucion();
        }
    }

    @Override
    protected int obtenerColumnaPivote() {
        return maximizar ? obtenerColumnaPivoteMaximizar() : obtenerColumnaPivoteMinimizar();
    }

    @Override
    protected boolean esOptimo() {
        return maximizar ? !existenNegativosEnlaFuncionObjetivo() : !existenPositivosEnlaFuncionObjetivo();
    }

    @Override
    protected double calcularValorFuncionObjetivo() {
        return getValor(0, nroColumnas - 1);
    }
}