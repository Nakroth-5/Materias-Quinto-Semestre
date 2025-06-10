package org.universidad.granm.metodos;

import java.util.HashMap;
import java.util.Map;

/**
 * Clase que implementa el método de la Gran M (Big M) para resolver problemas de programación lineal
 * con restricciones de tipo <=, >= y =.
 * Este método permite manejar tanto problemas de maximización como de minimización.
 */
public class MGranM extends MSimplex {

    /**
     * Valor muy grande utilizado como penalización para las variables artificiales.
     * Es lo suficientemente grande para garantizar que las variables artificiales
     * no formen parte de la solución óptima, pero no tan grande como para causar
     * problemas de precisión numérica.
     */
    public static double M_valor = 1e6; // Valor muy grande pero que evita overflow

    /**
     * Mapa que relaciona las filas de restricciones con las columnas de variables artificiales.
     * Se utiliza para actualizar la función objetivo con los términos de penalización.
     */
    public static Map<Integer, Integer> indexParaNuevoZ;

    /**
     * Indica si el problema es de maximización (true) o minimización (false).
     */
    private boolean maximizar;

    /**
     * Constructor para problemas de programación lineal utilizando el método de la Gran M.
     *
     * @param funcionObjetivo Coeficientes de la función objetivo (sin incluir término independiente)
     * @param restricciones Matriz de coeficientes de las restricciones (sin incluir términos independientes)
     * @param terminosIndependientes Términos independientes de las restricciones (lado derecho de las ecuaciones/desigualdades)
     * @param tipoRestricciones Tipos de restricciones ("<=", ">=", o "=")
     * @param maximizar true si es un problema de maximización, false si es de minimización
     */
    public MGranM(double[] funcionObjetivo, double[][] restricciones, double[] terminosIndependientes, String[] tipoRestricciones, boolean maximizar) {
        super();
        super.esGranM = true;
        this.nroRestricciones = restricciones.length;
        this.nroVariables = funcionObjetivo.length;
        this.maximizar = maximizar;
        int holguras = 0, excesos = 0, artificiales = 0;

        for (String tipo : tipoRestricciones) {
            if (tipo.equals("\u2264")) holguras++;
            else if (tipo.equals("\u2265")) {
                excesos++;
                artificiales++;
            } else if (tipo.equals("=")) artificiales++;
        }

        this.nroFilas = 1 + nroRestricciones;
        this.nroColumnas = 1 + nroVariables + holguras + excesos + artificiales + 1;
        this.M = new double[nroFilas][nroColumnas];

        this.solucion = new HashMap<>();
        this.indiceSolucion = new HashMap<>();
        this.historialDePasos = new GuardarPasos();
        this.indexParaNuevoZ = new HashMap<>();

        for (int i = 0; i < nroVariables; i++)
            solucion.put("x" + (i + 1), 0.0);
        solucion.put("z", 0.0);

        // Construcción del tableau
        int colHolgura = nroVariables + 1;
        int colExceso = colHolgura + holguras;
        int colArtificial = colExceso + excesos;

        int idxHolgura = 0, idxExceso = 0, idxArtificial = 0;

        // Fila 0: función objetivo
        setValor(0, 0, -1);

        // Para problemas de minimización, convertimos a maximización negando los coeficientes
        for (int j = 0; j < nroVariables; j++) {
            double coeficiente = maximizar ? -funcionObjetivo[j] : funcionObjetivo[j];
            setValor(0, j + 1, coeficiente);
        }

        setValor(0, nroColumnas - 1, 0); // RHS = 0

        for (int i = 0; i < nroRestricciones; i++) {
            setValor(i + 1, 0, 0); // columna identidad

            for (int j = 0; j < nroVariables; j++)
                setValor(i + 1, j + 1, restricciones[i][j]);

            switch (tipoRestricciones[i]) {
                case "\u2264":
                    setValor(i + 1, colHolgura + idxHolgura, 1);
                    idxHolgura++;
                    break;
                case "\u2265":
                    setValor(i + 1, colExceso + idxExceso, -1);
                    setValor(i + 1, colArtificial + idxArtificial, 1);
                    // Para minimización convertida a maximización, usamos +M
                    // Para maximización original, también usamos +M
                    setValor(0, colArtificial + idxArtificial, M_valor);
                    indexParaNuevoZ.put(i + 1, colArtificial + idxArtificial);
                    idxExceso++;
                    idxArtificial++;
                    break;
                case "=":
                    setValor(i + 1, colArtificial + idxArtificial, 1);
                    // Para minimización convertida a maximización, usamos +M
                    // Para maximización original, también usamos +M
                    setValor(0, colArtificial + idxArtificial, M_valor);
                    indexParaNuevoZ.put(i + 1, colArtificial + idxArtificial);
                    idxArtificial++;
                    break;
            }
            setValor(i + 1, nroColumnas - 1, terminosIndependientes[i]);
        }

        // Actualizar Z con los términos de las variables artificiales
        actualizarZ();

        guardarPaso("Tabla inicial con Gran M", "", "");
    }

    /**
     * Actualiza la fila de la función objetivo (Z) con los términos de penalización de las variables artificiales.
     * Este método suma a cada elemento de la fila Z el producto del factor -M
     * por el valor en las filas de restricciones que contienen variables artificiales.
     */
    private void actualizarZ() {
        indexParaNuevoZ.forEach((fila, columnaArtificial) -> {
            // Siempre usamos -M_valor porque queremos eliminar las variables artificiales
            for (int i = 0; i < nroColumnas; i++) {
                setValor(0, i, getValor(0, i) + getValor(fila, i) * (-M_valor));
            }
        });
    }

    /**
     * Resuelve el problema de programación lineal utilizando el método de la Gran M.
     * Este método extiende el algoritmo Simplex para manejar restricciones de tipo >=, = y problemas de minimización.
     * Al finalizar, verifica si hay variables artificiales en la base, lo que indicaría infactibilidad.
     */
    @Override
    public void resolverMSimplex() {
        super.resolverMSimplex();

        // Verificar si hay variables artificiales en la base
        if (hayVariablesArtificialesEnBase()) {
            guardarPaso("Solución no factible: variables artificiales en la base", "", "");
            System.out.println("Solución no factible: variables artificiales en la base");
            return;
        }

        // Para minimización, convertimos el valor de Z de vuelta al problema original
        if (!maximizar) {
            solucion.put("z", getValor(0, nroColumnas - 1) * - 1);
        }
    }

    /**
     * Sobrescribe el método para manejar correctamente el criterio de optimalidad
     * en problemas de minimización convertidos a maximización.
     */
    @Override
    protected boolean existenNegativosEnlaFuncionObjetivo() {
        // Para ambos casos (maximización y minimización convertida),
        // buscamos coeficientes negativos para continuar
        for (int j = 1; j < nroColumnas - 1; j++) {
            if (getValor(0, j) < -1e-10) { // Usamos tolerancia para evitar problemas numéricos
                return true;
            }
        }
        return false;
    }


}