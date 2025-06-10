package org.universidad.granm.metodos;

import org.xml.sax.SAXNotRecognizedException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

import static org.universidad.granm.metodos.MGranM.indexParaNuevoZ;

/**
 * Clase para resolver problemas de programación lineal utilizando el método Simplex.
 * Esta implementación está diseñada para problemas de maximización con restricciones de tipo &lt;=.
 */
public class MSimplex {
    /**
     * Representa la matriz tableau utilizada en el algoritmo Simplex.
     * Esta matriz 2D almacena los coeficientes y valores necesarios para procesar
     * problemas de programación lineal, incluyendo la función objetivo, restricciones,
     * y variables de holgura.
     */
    protected double M[][];

    /**
     * Número de filas en la matriz tableau.
     * Es igual a 1 (para la función objetivo) más el número de restricciones.
     */
    protected int nroFilas;

    /**
     * Número de columnas en la matriz tableau.
     * Es igual a 1 (columna de identidad) + variables originales + variables de holgura + término independiente.
     */
    protected int nroColumnas;

    /**
     * Número de variables de decisión en el problema original.
     */
    protected int nroVariables;

    /**
     * Número de restricciones en el problema original.
     */
    protected int nroRestricciones;

    /**
     * Mapa que almacena la solución óptima del problema.
     * Las claves son los nombres de las variables (x1, x2, ...) y "z" para el valor de la función objetivo.
     * Los valores son los valores numéricos correspondientes en la solución óptima.
     */
    public Map<String, Double> solucion;

    /**
     * Mapa que relaciona los índices de las variables con las filas donde aparecen en la base.
     * Se utiliza para reconstruir la solución óptima a partir del tableau final.
     */
    protected Map<Integer, Integer> indiceSolucion;

    /**
     * Objeto que almacena el historial de pasos realizados durante la ejecución del algoritmo Simplex.
     * Permite visualizar el proceso paso a paso y entender cómo se llegó a la solución óptima.
     */
    protected GuardarPasos historialDePasos;

    protected boolean esGranM;

    private static final int ESCALA_CALCULOS = 20;


    /**
     * Constructor para problemas de maximización con restricciones <=
     *
     * @param funcionObjetivo        Coeficientes de la función objetivo (sin incluir término independiente)
     * @param restricciones          Matriz de coeficientes de las restricciones (sin incluir términos independientes)
     * @param terminosIndependientes Términos independientes de las restricciones (lado derecho de las desigualdades)
     */
    public MSimplex(double[] funcionObjetivo, double[][] restricciones, double[] terminosIndependientes) {
        this.esGranM = false;
        this.nroRestricciones = restricciones.length;
        this.nroVariables = funcionObjetivo.length;

        // Número de filas = 1 (función objetivo) + número de restricciones
        this.nroFilas = 1 + nroRestricciones;

        // Número de columnas = 1 (columna de identidad) + variables originales + variables de holgura + término independiente
        this.nroColumnas = 1 + nroVariables + nroRestricciones + 1;

        this.M = new double[nroFilas][nroColumnas];

        // Configurar la fila de la función objetivo (fila 0)
        // La primera columna es para la identidad (siempre 1 en la fila 0)
        setValor(0, 0, 1);

        // Coeficientes de la función objetivo (con signo cambiado para maximización)
        for (int j = 0; j < nroVariables; j++)
            setValor(0, j + 1, -funcionObjetivo[j]);

        // Las variables de holgura tienen coeficiente 0 en la función objetivo
        for (int j = 0; j < nroRestricciones; j++)
            setValor(0, nroVariables + j + 1, 0);


        // El término independiente de la función objetivo es 0
        setValor(0, nroColumnas - 1, 0);

        // Configurar las filas de las restricciones
        for (int i = 0; i < nroRestricciones; i++) {
            // La primera columna es para la identidad (siempre 0 en las filas de restricciones)
            setValor(i + 1, 0, 0);

            // Coeficientes de las variables originales
            for (int j = 0; j < nroVariables; j++)
                setValor(i + 1, j + 1, restricciones[i][j]);

            // Variables de holgura (1 en la diagonal, 0 en el resto)
            for (int j = 0; j < nroRestricciones; j++)
                setValor(i + 1, nroVariables + j + 1, (i == j) ? 1 : 0);

            // Términos independientes
            setValor(i + 1, nroColumnas - 1, terminosIndependientes[i]);
        }

        //Para almacenar la solución
        solucion = new HashMap<>();
        indiceSolucion = new HashMap<>();
        for (int i = 0; i < nroVariables; i++) {
            solucion.put("x" + (i + 1), 0.0);
        }
        solucion.put("z", 0.0);

        historialDePasos = new GuardarPasos();

        guardarPaso("Tabla inicial", "", "");
    }

    public MSimplex() {
    }

    /**
     * Guarda un paso en el historial del proceso Simplex, registrando la descripción
     * del paso y las variables involucradas.
     *
     * @param descripcion     Descripción del paso realizado en el proceso.
     * @param variableEntrada Variable que entra a la base en este paso.
     * @param variableSalida  Variable que sale de la base en este paso.
     */
    protected void guardarPaso(String descripcion, String variableEntrada, String variableSalida) {
        historialDePasos.agregarPaso(M, variableEntrada, variableSalida, descripcion);
    }

    /**
     * Recupera el histórico de pasos dados durante el proceso Simplex.
     *
     * @return Una instancia de GuardarPasos que contiene la lista de pasos registrados.
     */
    public GuardarPasos getHistorialDePasos() {
        return historialDePasos;
    }

    /**
     * Obtiene la cantidad de pasos registrados en el historial del proceso Simplex.
     *
     * @return El número de pasos almacenados en el historial.
     */
    public int cantPasos() {
        return historialDePasos.getListaPasos().size();
    }


    /**
     * Recupera la descripción de un paso en el proceso Simplex basado en su índice.
     *
     * @param i El índice del paso cuya descripción se desea recuperar.
     * @return La descripción del paso en el índice especificado.
     */
    public String getDescripcionPaso(int i) {
        return historialDePasos.getListaPasos().get(i).getDescripcionPaso();
    }


    /**
     * Recupera la variable de entrada asociada a un paso específico del proceso Simplex.
     * en el proceso Simplex, basado en el índice proporcionado.
     *
     * @param i Índice del paso del proceso Simplex cuya variable de entrada se desea recuperar.
     * @return La variable de entrada del paso especificado.
     */
    public String getVariableEntradaPaso(int i) {
        return historialDePasos.getListaPasos().get(i).getVariableEntrada();
    }


    /**
     * Recupera la variable de salida asociada a un paso específico del proceso Simplex.
     * paso en el proceso Simplex, basado en el índice proporcionado.
     *
     * @param i Índice del paso del proceso Simplex cuya variable de salida se desea recuperar.
     * @return La variable de salida del paso especificado.
     */
    public String getVariableSalidaPaso(int i) {
        return historialDePasos.getListaPasos().get(i).getVariableSalida();
    }


    /**
     * Borra el historial de pasos registrados durante el proceso Simplex.
     * <p>
     * Este método elimina todas las entradas en el historial de pasos, reiniciando efectivamente
     * el registro de pasos realizados.
     */
    public void limpiarPasos() {
        historialDePasos.limpiarPasos();
    }


    /**
     * Recupera la matriz tableau de un paso específico en el proceso Simplex basado en su índice.
     *
     * @param indice El índice del paso en el proceso Simplex cuya matriz tableau se va a recuperar.
     * @return Una matriz 2D que representa la matriz tableau del paso especificado.
     */
    public double[][] getTablaPaso(int indice) {
        return historialDePasos.getTablaPaso(indice);
    }

    /**
     * Establece un valor en la matriz del tableau Simplex.
     *
     * @param f     Índice de la fila
     * @param c     Índice de la columna
     * @param valor Valor a establecer
     * @throws IllegalArgumentException Si los índices están fuera de rango
     */
    public void setValor(int f, int c, double valor) {
        validarRango(f, c);
        M[f][c] = valor;
    }

    /**
     * Obtiene un valor de la matriz del tableau Simplex.
     *
     * @param f Índice de la fila
     * @param c Índice de la columna
     * @return Valor en la posición especificada
     * @throws IllegalArgumentException Si los índices están fuera de rango
     */
    public double getValor(int f, int c) {
        validarRango(f, c);
        return M[f][c];
    }

    /**
     * Valida que los índices de fila y columna estén dentro del rango válido de la matriz.
     *
     * @param f Índice de la fila a validar
     * @param c Índice de la columna a validar
     * @throws IllegalArgumentException Si los índices están fuera del rango válido
     */
    private void validarRango(int f, int c) {
        if (!(f >= 0 && f < nroFilas && c >= 0 && c < nroColumnas))
            throw new IllegalArgumentException("Fuera de rango");
    }

    /**
     * Resuelve el problema de programación lineal utilizando el método Simplex.
     * Este método implementa el algoritmo Simplex para encontrar la solución óptima.
     * Se ejecuta recursivamente hasta que no haya coeficientes negativos en la función objetivo.
     */
    public void resolverMSimplex() {
        int columnaPivote = obtenerColumnaPivote();
        int filaPivote = obtenerFilaPivote(columnaPivote);

        // Verificar si el problema es no acotado (no hay elementos positivos en la columna pivote)
        if (filaPivote == 0) {
            guardarPaso("Problema no acotado", "", "");
            return;
        }

        double valorPivote = getValor(filaPivote, columnaPivote);

        normalizarFilaPivote(filaPivote, valorPivote);
        guardarPaso("Normalizacion de la fila pivote", "Variable de entrada X" + columnaPivote, "Variable Salida X" + filaPivote);

        for (int i = 0; i < nroFilas; i++) {
            if (i == filaPivote) continue;
            double factor = -getValor(i, columnaPivote);
            for (int j = 0; j < nroColumnas; j++)
                setValor(i, j, getValor(i, j) + factor * getValor(filaPivote, j));
        }

        solucion.put("x" + columnaPivote, 0.0);
        indiceSolucion.put(columnaPivote, filaPivote);

        guardarPaso("Eliminar la columna pivote", "Variable de entrada X" + columnaPivote, "Variable Salida X" + filaPivote);

        if (existenNegativosEnlaFuncionObjetivo())
            resolverMSimplex();
        else {
            guardarPaso("Solucion optima encontrada", "", "");
            indiceSolucion.forEach((k, v) -> {
                solucion.put("x" + k, getValor(v, nroColumnas - 1));
            });
            solucion.put("z", getValor(0, nroColumnas - 1));
        }
    }

    /**
     * Verifica si existen coeficientes negativos en la función objetivo.
     * Este método es utilizado para determinar si el algoritmo Simplex debe continuar iterando.
     *
     * @return true si existen coeficientes negativos en la función objetivo, false en caso contrario
     */
    protected boolean existenNegativosEnlaFuncionObjetivo() {
        for (int j = 1; j < nroColumnas - 1; j++) {
            if (getValor(0, j) < 0)
                return true;
        }
        return false;
    }

    /**
     * Verifica si hay variables artificiales en la base de la solución óptima.
     * Si hay variables artificiales con valores positivos en la base, el problema es infactible.
     * NOTA: Este método solo debe usarse en MGranM después de completar el algoritmo Simplex.
     *
     * @return true si hay variables artificiales en la base, false en caso contrario
     */
    protected boolean hayVariablesArtificialesEnBase() {
        if (!esGranM || indexParaNuevoZ == null) {
            return false;
        }

        for (Map.Entry<Integer, Integer> entry : indiceSolucion.entrySet()) {
            int columna = entry.getKey();
            // Verificar si la columna corresponde a una variable artificial
            if (indexParaNuevoZ.containsValue(columna)) {
                double valor = getValor(entry.getValue(), nroColumnas - 1);
                if (Math.abs(valor) > 1e-10) {  // Tolerancia para errores de punto flotante
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Convierte el elemento pivote a 1 dividiendo toda la fila pivote por el valor del pivote.
     * Este es un paso fundamental en el algoritmo Simplex para normalizar la fila pivote.
     *
     * @param filaPivote  Índice de la fila que contiene el elemento pivote
     * @param valorPivote Valor del elemento pivote
     */
    protected void normalizarFilaPivote(int filaPivote, double valorPivote) {
        double inversoMultiplicativo = 1 / valorPivote;
        for (int j = 0; j < nroColumnas; j++)
            setValor(filaPivote, j, getValor(filaPivote, j) * inversoMultiplicativo);
    }

    /**
     * Determina la columna pivote para la siguiente iteración del algoritmo Simplex.
     * La columna pivote es aquella con el coeficiente más negativo en la función objetivo.
     *
     * @return Índice de la columna pivote
     */
    protected int obtenerColumnaPivote() {
        double maxNegativo = getValor(0, 1);
        int columnaPivote = 1;

        for (int i = 2; i < nroColumnas - 1; i++)
            if (getValor(0, i) < maxNegativo) {
                maxNegativo = getValor(0, i);
                columnaPivote = i;
            }

        return columnaPivote;
    }

    /**
     * Determina la fila pivote para la siguiente iteración del algoritmo Simplex.
     * La fila pivote se selecciona utilizando la regla del cociente mínimo (o regla de la razón mínima).
     *
     * @param columnaPivote Índice de la columna pivote previamente determinada
     * @return Índice de la fila pivote, o 0 si el problema es no acotado
     */
    protected int obtenerFilaPivote(int columnaPivote) {
        double minimoActual = Double.POSITIVE_INFINITY;
        int filaPivote = 0;

        for (int i = 1; i < nroFilas; i++) {
            if (getValor(i, columnaPivote) <= 0) continue;

            double razon = getValor(i, nroColumnas - 1) / getValor(i, columnaPivote);
            if (razon < minimoActual) {
                minimoActual = razon;
                filaPivote = i;
            }
        }
        return filaPivote;
    }

    /**
     * Obtiene la matriz completa del tableau Simplex.
     *
     * @return Matriz del tableau Simplex
     */
    public double[][] getM() {
        return M;
    }

    /**
     * Establece la matriz completa del tableau Simplex.
     *
     * @param m Nueva matriz para el tableau Simplex
     */
    public void setM(double[][] m) {
        M = m;
    }

    /**
     * Obtiene el número de filas de la matriz del tabla Simplex.
     *
     * @return Número de filas
     */
    public int getNroFilas() {
        return nroFilas;
    }

    /**
     * Establece el número de filas de la matriz del tabla Simplex.
     *
     * @param nroFilas Nuevo número de filas
     */
    public void setNroFilas(int nroFilas) {
        this.nroFilas = nroFilas;
    }

    /**
     * Obtiene el número de columnas de la matriz del tableau Simplex.
     *
     * @return Número de columnas
     */
    public int getNroColumnas() {
        return nroColumnas;
    }

    /**
     * Establece el número de columnas de la matriz del tableau Simplex.
     *
     * @param nroColumnas Nuevo número de columnas
     */
    public void setNroColumnas(int nroColumnas) {
        this.nroColumnas = nroColumnas;
    }

    /**
     * Obtiene el número de variables de decisión en el problema.
     *
     * @return Número de variables de decisión
     */
    public int getNroVariables() {
        return nroVariables;
    }

    /**
     * Establece el número de variables de decisión en el problema.
     *
     * @param nroVariables Nuevo número de variables de decisión
     */
    public void setNroVariables(int nroVariables) {
        this.nroVariables = nroVariables;
    }

    /**
     * Obtiene el valor de la función objetivo en la solución óptima
     *
     * @return Valor de la función objetivo
     */
    public double obtenerValorFuncionObjetivo() {
        return getValor(0, nroColumnas - 1);
    }

    /**
     * Obtiene el número de restricciones
     *
     * @return Número de restricciones
     */
    public int getNroRestricciones() {
        return nroRestricciones;
    }

    /**
     * Establece el número de restricciones
     *
     * @param nroRestricciones Número de restricciones
     */
    public void setNroRestricciones(int nroRestricciones) {
        this.nroRestricciones = nroRestricciones;
    }
}