package org.universidad.granm.claseabstracta;

import org.universidad.granm.metodos.GuardarPasos;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

/**
 * Clase abstracta base para implementaciones del método Simplex.
 * Proporciona la funcionalidad común y define los métodos que deben
 * ser implementados por las clases derivadas.
 */
public abstract class SimplexBase {

    // Atributos protegidos compartidos
    protected double M[][];
    protected int nroFilas;
    protected int nroColumnas;
    protected int nroVariables;
    protected int nroRestricciones;
    protected boolean maximizar;
    protected boolean solucionEncontrada;
    protected int holguras;
    protected int excesos;
    protected int artificiales;

    public Map<String, Double> solucion;
    protected Map<Integer, Integer> indiceSolucion;
    protected GuardarPasos historialDePasos;

    public static double epsilon = 1e-10;
    public static int decimales = 6;

    /**
     * Constructor base
     */
    protected SimplexBase() {
        this.solucionEncontrada = true;
        this.solucion = new HashMap<>();
        this.indiceSolucion = new HashMap<>();
        this.historialDePasos = new GuardarPasos();
    }

    protected void inicializarTableauBase(double[] funcionObjetivo, double[][] restricciones,
                                      double[] terminosIndependientes, String[] tipoRestricciones) {
        holguras = 0;
        excesos = 0;
        artificiales = 0;

        // Contar tipos de variables necesarias
        for (String tipo : tipoRestricciones) {
            switch (tipo) {
                case "≤":
                    holguras++;
                    break;
                case "≥":
                    excesos++;
                    artificiales++;
                    break;
                case "=":
                    artificiales++;
                    break;
            }
        }

        // Dimensiones del tableau
        this.nroFilas = 1 + nroRestricciones;
        this.nroColumnas = 1 + nroVariables + holguras + excesos + artificiales + 1;
        this.M = new double[nroFilas][nroColumnas];
    }

    // Métodos comunes de gestión de matriz
    public void setValor(int f, int c, double valor) {
        validarRango(f, c);
        M[f][c] = valor;
    }

    public double getValor(int f, int c) {
        validarRango(f, c);
        return M[f][c];
    }

    private void validarRango(int f, int c) {
        if (!(f >= 0 && f < nroFilas && c >= 0 && c < nroColumnas))
            throw new IllegalArgumentException("Fuera de rango");
    }

    // Métodos comunes de gestión de pasos
    protected void guardarPaso(String descripcion, String variableEntrada, String variableSalida) {
        historialDePasos.agregarPaso(M, variableEntrada, variableSalida, descripcion);
    }

    public GuardarPasos getHistorialDePasos() {
        return historialDePasos;
    }

    public int cantPasos() {
        return historialDePasos.getListaPasos().size();
    }

    public String getDescripcionPaso(int i) {
        return historialDePasos.getListaPasos().get(i).getDescripcionPaso();
    }

    public String getVariableEntradaPaso(int i) {
        return historialDePasos.getListaPasos().get(i).getVariableEntrada();
    }

    public String getVariableSalidaPaso(int i) {
        return historialDePasos.getListaPasos().get(i).getVariableSalida();
    }

    public void limpiarPasos() {
        historialDePasos.limpiarPasos();
    }

    public double[][] getTablaPaso(int indice) {
        return historialDePasos.getTablaPaso(indice);
    }

    // Métodos comunes del algoritmo Simplex
    protected boolean existenNegativosEnlaFuncionObjetivo() {
        for (int j = 1; j < nroColumnas - 1; j++) {
            if (getValor(0, j) < -epsilon)
                return true;
        }
        return false;
    }

    protected boolean existenPositivosEnlaFuncionObjetivo() {
        for (int j = 1; j < nroColumnas - 1; j++) {
            if (getValor(0, j) > epsilon)
                return true;
        }
        return false;
    }

    protected void normalizarFilaPivote(int filaPivote, double valorPivote) {
        double inversoMultiplicativo = 1 / valorPivote;
        for (int j = 0; j < nroColumnas; j++)
            setValor(filaPivote, j, getValor(filaPivote, j) * inversoMultiplicativo);
    }

    protected int obtenerColumnaPivoteMaximizar() {
        double maxNegativo = 0;
        int columnaPivote = 0;

        for (int i = 1; i < nroColumnas - 1; i++)
            if (getValor(0, i) < maxNegativo) {
                maxNegativo = getValor(0, i);
                columnaPivote = i;
            }

        return columnaPivote;
    }

    protected int obtenerColumnaPivoteMinimizar() {
        double maxPositivo = 0;
        int columnaPivote = 0;

        for (int i = 1; i < nroColumnas - 1; i++)
            if (getValor(0, i) > maxPositivo) {
                maxPositivo = getValor(0, i);
                columnaPivote = i;
            }

        return columnaPivote;
    }

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
     * Realiza una iteración completa del algoritmo Simplex.
     * Método template que define el flujo común.
     */
    protected void iteracionSimplex() {
        int columnaPivote = obtenerColumnaPivote();

        if (columnaPivote == 0) {
            manejarProblemaNoAcotado("columna");
            return;
        }

        int filaPivote = obtenerFilaPivote(columnaPivote);

        if (filaPivote == 0) {
            manejarProblemaNoAcotado("fila");
            return;
        }

        realizarPivoteo(filaPivote, columnaPivote);
        actualizarSolucion(columnaPivote, filaPivote);
    }

    /**
     * Realiza el pivoteo en el tableau
     */
    protected void realizarPivoteo(int filaPivote, int columnaPivote) {
        double valorPivote = getValor(filaPivote, columnaPivote);

        if (Math.abs(valorPivote - 1) > epsilon) {
            normalizarFilaPivote(filaPivote, valorPivote);
            guardarPaso("Normalización de la fila pivote",
                    "Variable de entrada X" + columnaPivote,
                    "Variable de salida X" + filaPivote);
        }

        // Eliminación gaussiana
        for (int i = 0; i < nroFilas; i++) {
            if (i == filaPivote) continue;
            double factor = -getValor(i, columnaPivote);
            for (int j = 0; j < nroColumnas; j++)
                setValor(i, j, getValor(i, j) + factor * getValor(filaPivote, j));
        }
    }

    /**
     * Actualiza la solución después del pivoteo
     */
    protected void actualizarSolucion(int columnaPivote, int filaPivote) {
        solucion.put("x" + columnaPivote, 0.0);
        indiceSolucion.put(columnaPivote, filaPivote);

        guardarPaso("Eliminación de la columna pivote",
                "Variable de entrada X" + columnaPivote,
                "Variable de salida X" + filaPivote);
    }

    /**
     * Maneja el caso de problema no acotado
     */
    protected void manejarProblemaNoAcotado(String tipo) {
        guardarPaso("Problema no acotado " + tipo, "", "");
        solucionEncontrada = false;
    }

    /**
     * Finaliza el algoritmo y construye la solución final
     */
    protected void finalizarSolucion() {
        guardarPaso("Solución óptima encontrada", "", "");
        indiceSolucion.forEach((k, v) -> {
            solucion.put("x" + k, getValor(v, nroColumnas - 1));
        });

        double valorZ = calcularValorFuncionObjetivo();
        solucion.put("z", valorZ);
    }

    // Métodos abstractos que deben implementar las clases derivadas

    /**
     * Método principal de resolución que debe implementar cada clase derivada
     */
    public abstract void resolver();

    /**
     * Determina la columna pivote según el tipo específico de problema
     */
    protected abstract int obtenerColumnaPivote();

    /**
     * Verifica el criterio de optimalidad específico para cada implementación
     */
    protected abstract boolean esOptimo();


    /**
     * Calcula el valor final de la función objetivo
     */
    protected abstract double calcularValorFuncionObjetivo();

    // Métodos utilitarios estáticos
    public static double redondear(double valor, int decimales, double epsilon) {
        if (Math.abs(valor) < epsilon) {
            return 0.0;
        }
        BigDecimal bd = BigDecimal.valueOf(valor);
        bd = bd.setScale(decimales, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    // Getters y Setters
    public double[][] getM() { return M; }
    public void setM(double[][] m) { M = m; }
    public int getNroFilas() { return nroFilas; }
    public void setNroFilas(int nroFilas) { this.nroFilas = nroFilas; }
    public int getNroColumnas() { return nroColumnas; }
    public void setNroColumnas(int nroColumnas) { this.nroColumnas = nroColumnas; }
    public int getNroVariables() { return nroVariables; }
    public void setNroVariables(int nroVariables) { this.nroVariables = nroVariables; }
    public int getNroRestricciones() { return nroRestricciones; }
    public void setNroRestricciones(int nroRestricciones) { this.nroRestricciones = nroRestricciones; }
    public boolean isSolucionEncontrada() { return solucionEncontrada; }
    public double obtenerValorFuncionObjetivo() { return getValor(0, nroColumnas - 1); }
}