package org.universidad.granm.metodos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DosFaces extends MSimplex {
    private int artificiales;
    private double[] funcionObjetivoOriginal;
    private Map<Integer, Integer> indexParaNuevoZ;
    private Map<Integer, Integer> indiceSolucionFase2;

    public DosFaces(double[] funcionObjetivo, double[][] restricciones, double[] terminosIndependientes, String[] tipoRestricciones, boolean maximizar) {
        super();
        super.esGranM = false;
        super.nroRestricciones = restricciones.length;
        super.nroVariables = funcionObjetivo.length;
        super.maximizar = maximizar;
        super.solucionEncontrada = true;

        indiceSolucionFase2 = new HashMap<>();

        // Guardamos la función objetivo original con el signo correcto
        funcionObjetivoOriginal = new double[nroVariables + 1];
        funcionObjetivoOriginal[0] = 1; // Siempre 1 para la identidad
        for (int i = 0; i < nroVariables; i++) {
            funcionObjetivoOriginal[i + 1] = maximizar ? -funcionObjetivo[i] : funcionObjetivo[i];
        }

        for (int i = 0; i < tipoRestricciones.length; i++) {
            if (tipoRestricciones[i] == "≤" && terminosIndependientes[i] < 0) {
                for (int j = 0; j < nroVariables; j++) {
                    restricciones[i][j] = -restricciones[i][j];
                }
                terminosIndependientes[i] = -terminosIndependientes[i];
                tipoRestricciones[i] = "≥";
            }
        }

        int holguras = 0, excesos = 0;
        artificiales = 0;
        for (String tipo : tipoRestricciones) {
            if (tipo.equals("≤")) holguras++; // <=
            else if (tipo.equals("≥")) {  //>=
                excesos++;
                artificiales++;
            } else if (tipo.equals("=")) artificiales++;
        }

        super.nroFilas = 1 + nroRestricciones;
        super.nroColumnas = 1 + nroVariables + holguras + excesos + artificiales + 1;

        super.M = new double[nroFilas][nroColumnas];
        super.solucion = new HashMap<>();
        super.indiceSolucion = new HashMap<>();
        super.historialDePasos = new GuardarPasos();
        indexParaNuevoZ = new HashMap<>();


        int colHolgura = nroVariables + 1;
        int colExceso = colHolgura + holguras;
        int colArtificiales = colExceso + excesos;

        int idxHolgura = 0, idxExceso = 0, idxArtificiales = 0;

        setValor(0, 0, 1);
        setValor(0, nroColumnas - 1, 0); //RHS

        for (int i = 0; i < nroRestricciones; i++) {
            setValor(i + 1, 0, 0); // columna identidad

            for (int j = 0; j < nroVariables; j++)
                setValor(i + 1, j + 1, restricciones[i][j]);

            switch (tipoRestricciones[i]) {
                case "≤": //<=
                    setValor(i + 1, colHolgura + idxHolgura, 1);
                    indiceSolucionFase2.put(colHolgura + idxHolgura, i + 1);
                    idxHolgura++;
                    break;
                case "≥": //>=
                    setValor(i + 1, colExceso + idxExceso, -1);
                    setValor(i + 1, colArtificiales + idxArtificiales, 1);
                    setValor(0, colArtificiales + idxArtificiales, -1);
                    indexParaNuevoZ.put(i + 1, colArtificiales + idxArtificiales);
                    indiceSolucionFase2.put(colArtificiales + idxArtificiales, i + 1);
                    idxExceso++;
                    idxArtificiales++;
                    break;
                case "=":
                    setValor(i + 1, colArtificiales + idxArtificiales, 1);
                    setValor(0, colArtificiales + idxArtificiales, -1);
                    indexParaNuevoZ.put(i + 1, colArtificiales + idxArtificiales);
                    indiceSolucionFase2.put(colArtificiales + idxArtificiales, i + 1);
                    idxArtificiales++;
                    break;
            }
            setValor(i + 1, nroColumnas - 1, terminosIndependientes[i]);
        }

        actualizarZ();
        guardarPaso("Tabla inicial con Dos Fases", "", "");
    }

    /**
     * Actualiza la fila de la función objetivo (Z) con los términos de penalización de las variables artificiales.
     */
    protected void actualizarZ() {
        indexParaNuevoZ.forEach((fila, _) -> {
            for (int i = 0; i < nroColumnas; i++) {
                setValor(0, i, getValor(0, i) + getValor(fila, i));
            }
        });
    }

    /**
     * Resuelve el problema de programación lineal utilizando el método de dos fases.
     */
    @Override
    public void resolverMSimplex() {
        resolverMSimplexMinimizacion();

        // Verificar si hay variables artificiales en la base
        if (hayVariablesArtificialesEnBase()) {
            guardarPaso("Solución no factible: variables artificiales en la base", "", "");
            System.out.println("Solución no factible: variables artificiales en la base");
            return;
        }
        faseDos();
    }

    public void faseDos() {
        // Actualizar indiceSolucionFase2 con las variables que están en la base después de la Fase 1
        indiceSolucionFase2.clear();
        for (Map.Entry<Integer, Integer> entry : indiceSolucion.entrySet()) {
            int columna = entry.getKey();
            int fila = entry.getValue();
            // Solo guardamos variables que no son artificiales
            if (!esVariableArtificial(columna)) {
                indiceSolucionFase2.put(columna, fila);
            }
        }

        // Crear nueva matriz sin columnas de variables artificiales
        List<Integer> columnasArtificiales = new ArrayList<>();
        indexParaNuevoZ.forEach((_, col) -> columnasArtificiales.add(col));

        // Copiar columnas relevantes (eliminar columnas de variables artificiales)
        for (int i = 0; i < nroFilas; i++) {
            for (int j = 0; j < nroColumnas; j++) {
                if (columnasArtificiales.contains(j)) {
                    super.M[i][j] = 0; // Eliminar columnas artificiales
                }
            }
        }

        // Restaurar la función objetivo original
        for (int i = 0; i <= nroVariables; i++) {
            super.M[0][i] = funcionObjetivoOriginal[i];
        }

        // Poner ceros en el resto de las columnas de la función objetivo (excepto RHS)
        for (int j = nroVariables + 1; j < nroColumnas - 1; j++) {
            if (!columnasArtificiales.contains(j)) {
                super.M[0][j] = 0;
            }
        }
        super.M[0][nroColumnas - 1] = 0; // RHS de la función objetivo

        actualizarZFase2();
        guardarPaso("Tabla para Fase 2", "", "");
        if (!maximizar && existenPositivosEnlaFuncionObjetivo()) {
            indiceSolucion.forEach((k, v) -> {
                solucion.put("x" + k, getValor(v, 0));
            });
            solucion.put("z", getValor(0, 0));
            resolverMSimplexMinimizacion();
            return;
        } else if (!maximizar) {
            solucion.put("z", getValor(0, nroColumnas - 1));
            return;
        }
        if (maximizar) {
            solucionEncontrada = true;
            resolverMSimplexMaximizacion();
        }
    }

    private boolean esVariableArtificial(int columna) {
        return indexParaNuevoZ.containsValue(columna);
    }

    private void actualizarZFase2() {
        // Actualizar Z para que las variables básicas tengan coeficiente 0
        for (Map.Entry<Integer, Integer> entry : indiceSolucionFase2.entrySet()) {
            int columna = entry.getKey();
            int fila = entry.getValue();

            double coeficienteZ = getValor(0, columna);
            if (Math.abs(coeficienteZ) > 0) { // Si no es cero
                // Hacer operación de fila para que el coeficiente sea 0
                for (int j = 0; j < nroColumnas; j++) {
                    double nuevoValor = getValor(0, j) - coeficienteZ * getValor(fila, j);
                    setValor(0, j, nuevoValor);
                }
            }
        }
        if (!maximizar)
            for (int j = 0; j < nroColumnas; j++) {
                setValor(0, j, -getValor(0, j));
            }
    }

    private void resolverMSimplexMinimizacion() {
        int columnaPivote = obtenerColumnaPivoteMinimizar();

        if (columnaPivote == 0) {
            guardarPaso("Fase 1 completada - Solución óptima encontrada", "", "");
            super.solucionEncontrada = false;
            return;
        }

        int filaPivote = obtenerFilaPivote(columnaPivote);

        // Verificar si el problema es no acotado (no hay elementos positivos en la columna pivote)
        if (filaPivote == 0) {
            guardarPaso("Problema no acotado en Fase 1", "", "");
            super.solucionEncontrada = false;
            return;
        }

        double valorPivote = getValor(filaPivote, columnaPivote);

        if (valorPivote != 1) {
            normalizarFilaPivote(filaPivote, valorPivote);
            guardarPaso("Normalización de la fila pivote",
                    "Variable de entrada X" + columnaPivote,
                    "Variable de salida X" + filaPivote);
        }

        // Eliminación gaussiana
        for (int i = 0; i < nroFilas; i++) {
            if (i == filaPivote) continue;
            double factor = -getValor(i, columnaPivote);
            for (int j = 0; j < nroColumnas; j++) {
                setValor(i, j, getValor(i, j) + factor * getValor(filaPivote, j));
            }
        }

        // Actualizar mapas de solución
        solucion.put("x" + columnaPivote, 0.0);
        indiceSolucion.put(columnaPivote, filaPivote);

        guardarPaso("Eliminación de la columna pivote",
                "Variable de entrada X" + columnaPivote,
                "Variable de salida X" + filaPivote);

        // Continuar si aún hay coeficientes positivos en la función objetivo
        if (existenPositivosEnlaFuncionObjetivo()) {
            resolverMSimplexMinimizacion();
        } else {
            guardarPaso("Fase 1 completada - Solución básica factible encontrada", "", "");
            indiceSolucion.forEach((k, v) -> {
                solucion.put("x" + k, getValor(v, nroColumnas - 1));
            });
            solucion.put("z", getValor(0, nroColumnas - 1));
        }
    }

    public void resolverMSimplexMaximizacion() {
        int columnaPivote = obtenerColumnaPivoteMaximizar();

        if (columnaPivote == 0) {
            guardarPaso("Problema no acotado c", "", "");
            super.solucionEncontrada = false;
            return;
        }

        int filaPivote = obtenerFilaPivote(columnaPivote);

        // Verificar si el problema es no acotado (no hay elementos positivos en la columna pivote)
        if (filaPivote == 0) {
            guardarPaso("Problema no acotado f", "", "");
            super.solucionEncontrada = false;
            return;
        }

        double valorPivote = getValor(filaPivote, columnaPivote);

        if (valorPivote != 1) {
            normalizarFilaPivote(filaPivote, valorPivote);
            guardarPaso("Normalizacion de la fila pivote", "Variable de entrada X" + columnaPivote, "Variable Salida X" + filaPivote);
        }

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
            resolverMSimplexMaximizacion();
        else {
            guardarPaso("Solucion optima encontrada", "", "");
            indiceSolucion.forEach((k, v) -> {
                solucion.put("x" + k, getValor(v, nroColumnas - 1));
            });
            solucion.put("z", getValor(0, nroColumnas - 1));
        }
    }
}