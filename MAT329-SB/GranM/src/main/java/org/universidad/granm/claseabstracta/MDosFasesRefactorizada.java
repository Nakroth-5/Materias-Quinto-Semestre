package org.universidad.granm.claseabstracta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MDosFasesRefactorizada extends SimplexBase {

    private int artificiales;
    private double[] funcionObjetivoOriginal;
    private Map<Integer, Integer> indexParaNuevoZ;
    private Map<Integer, Integer> indiceSolucionFase2;
    private boolean fase1Completada;

    public MDosFasesRefactorizada(double[] funcionObjetivo, double[][] restricciones,
                                  double[] terminosIndependientes, String[] tipoRestricciones,
                                  boolean maximizar) {
        super();
        this.maximizar = maximizar;
        this.nroRestricciones = restricciones.length;
        this.nroVariables = funcionObjetivo.length;
        this.artificiales = 0;
        this.fase1Completada = false;
        this.indiceSolucionFase2 = new HashMap<>();
        this.indexParaNuevoZ = new HashMap<>();

        // Guardar función objetivo original
        almacenarFuncionObjetivoOriginal(funcionObjetivo);

        // Normalizar restricciones con términos independientes negativos
        normalizarRestricciones(restricciones, terminosIndependientes, tipoRestricciones);

        // Inicializar tableau para Fase 1
        inicializarTableauBase(funcionObjetivo, restricciones, terminosIndependientes, tipoRestricciones);
        // Construir tableau
        construirTableauFase1(funcionObjetivo, restricciones, terminosIndependientes, tipoRestricciones);

        // Actualizar Z para Fase 1
        actualizarZFase1();
        guardarPaso("Tabla inicial - Fase 1 (Método Dos Fases)", "", "");
    }

    private void almacenarFuncionObjetivoOriginal(double[] funcionObjetivo) {
        funcionObjetivoOriginal = new double[nroVariables + 1];
        funcionObjetivoOriginal[0] = 1; // Coeficiente de identidad
        for (int i = 0; i < nroVariables; i++) {
            funcionObjetivoOriginal[i + 1] = maximizar ? -funcionObjetivo[i] : funcionObjetivo[i];
        }
    }

    private void normalizarRestricciones(double[][] restricciones, double[] terminosIndependientes,
                                         String[] tipoRestricciones) {
        for (int i = 0; i < tipoRestricciones.length; i++) {
            if (tipoRestricciones[i].equals("≤") && terminosIndependientes[i] < 0) {
                // Multiplicar por -1 para convertir ≤ con RHS negativo a ≥ con RHS positivo
                for (int j = 0; j < nroVariables; j++) {
                    restricciones[i][j] = -restricciones[i][j];
                }
                terminosIndependientes[i] = -terminosIndependientes[i];
                tipoRestricciones[i] = "≥";
            }
        }
    }


    private void construirTableauFase1(double[] funcionObjetivo, double[][] restricciones,
                                       double[] terminosIndependientes, String[] tipoRestricciones) {

        int colHolgura = nroVariables + 1;
        int colExceso = colHolgura + holguras;
        int colArtificial = colExceso + excesos;

        int idxHolgura = 0, idxExceso = 0, idxArtificial = 0;

        // Fila función objetivo para Fase 1 (minimizar suma de variables artificiales)
        setValor(0, 0, 1);

        // Coeficientes de variables originales = 0 en Fase 1
        for (int j = 0; j < nroVariables; j++) {
            setValor(0, j + 1, 0);
        }

        setValor(0, nroColumnas - 1, 0); // RHS

        // Filas de restricciones
        for (int i = 0; i < nroRestricciones; i++) {
            setValor(i + 1, 0, 0); // Columna identidad

            // Coeficientes de variables originales
            for (int j = 0; j < nroVariables; j++) {
                setValor(i + 1, j + 1, restricciones[i][j]);
            }

            // Agregar variables de holgura, exceso y artificiales según el tipo
            switch (tipoRestricciones[i]) {
                case "≤":
                    setValor(i + 1, colHolgura + idxHolgura, 1);
                    indiceSolucionFase2.put(colHolgura + idxHolgura, i + 1);
                    idxHolgura++;
                    break;

                case "≥":
                    setValor(i + 1, colExceso + idxExceso, -1);
                    setValor(i + 1, colArtificial + idxArtificial, 1);
                    setValor(0, colArtificial + idxArtificial, -1); // Coeficiente en función objetivo
                    indexParaNuevoZ.put(i + 1, colArtificial + idxArtificial);
                    indiceSolucionFase2.put(colArtificial + idxArtificial, i + 1);
                    idxExceso++;
                    idxArtificial++;
                    break;

                case "=":
                    setValor(i + 1, colArtificial + idxArtificial, 1);
                    setValor(0, colArtificial + idxArtificial, -1); // Coeficiente en función objetivo
                    indexParaNuevoZ.put(i + 1, colArtificial + idxArtificial);
                    indiceSolucionFase2.put(colArtificial + idxArtificial, i + 1);
                    idxArtificial++;
                    break;
            }

            setValor(i + 1, nroColumnas - 1, terminosIndependientes[i]);
        }
    }

    private void actualizarZFase1() {
        // Actualizar Z para eliminar variables artificiales de la función objetivo
        indexParaNuevoZ.forEach((fila, _) -> {
            for (int j = 0; j < nroColumnas; j++) {
                setValor(0, j, getValor(0, j) + getValor(fila, j));
            }
        });
    }

    @Override
    public void resolver() {
        // Fase 1: Encontrar solución básica factible
        resolverFase1();

        if (!solucionEncontrada) {
            return; // No hay solución factible
        }

        // Verificar si hay variables artificiales en la base
        if (hayVariablesArtificialesEnBase()) {
            guardarPaso("Solución no factible: variables artificiales en la base", "", "");
            solucionEncontrada = false;
            return;
        }

        // Fase 2: Resolver problema original
        resolverFase2();
    }

    private void resolverFase1() {
        guardarPaso("Iniciando Fase 1", "", "");

        while (existenPositivosEnlaFuncionObjetivo() && solucionEncontrada) {
            iteracionSimplex();
        }

        if (solucionEncontrada) {
            fase1Completada = true;
            guardarPaso("Fase 1 completada - Solución básica factible encontrada", "", "");
        }
    }

    private void resolverFase2() {
        guardarPaso("Iniciando Fase 2", "", "");

        // Preparar tableau para Fase 2
        prepararTableauFase2();

        // Resolver problema original
        while (!esOptimo() && solucionEncontrada) {
            iteracionSimplex();
        }

        if (solucionEncontrada) {
            finalizarSolucion();
        }
    }

    private void prepararTableauFase2() {
        // Actualizar indiceSolucionFase2 eliminando variables artificiales
        indiceSolucionFase2.clear();
        for (Map.Entry<Integer, Integer> entry : indiceSolucion.entrySet()) {
            int columna = entry.getKey();
            int fila = entry.getValue();
            if (!esVariableArtificial(columna)) {
                indiceSolucionFase2.put(columna, fila);
            }
        }

        // Eliminar columnas de variables artificiales (ponerlas en cero)
        List<Integer> columnasArtificiales = new ArrayList<>(indexParaNuevoZ.values());
        for (int i = 0; i < nroFilas; i++) {
            for (int columna : columnasArtificiales) {
                setValor(i, columna, 0);
            }
        }

        // Restaurar función objetivo original
        for (int i = 0; i <= nroVariables; i++) {
            setValor(0, i, funcionObjetivoOriginal[i]);
        }

        // Limpiar resto de coeficientes en función objetivo (excepto RHS)
        for (int j = nroVariables + 1; j < nroColumnas - 1; j++) {
            if (!columnasArtificiales.contains(j)) {
                setValor(0, j, 0);
            }
        }
        setValor(0, nroColumnas - 1, 0); // RHS

        // Actualizar Z para Fase 2
        actualizarZFase2();
        guardarPaso("Tabla preparada para Fase 2", "", "");
    }

    private void actualizarZFase2() {
        for (Map.Entry<Integer, Integer> entry : indiceSolucionFase2.entrySet()) {
            int columna = entry.getKey();
            int fila = entry.getValue();

            double coeficienteZ = getValor(0, columna);
            if (Math.abs(coeficienteZ) > epsilon) {
                for (int j = 0; j < nroColumnas; j++) {
                    double nuevoValor = getValor(0, j) - coeficienteZ * getValor(fila, j);
                    setValor(0, j, nuevoValor);
                }
            }
        }
    }

    private boolean hayVariablesArtificialesEnBase() {
        // Verificar el valor de la función objetivo de Fase 1
        double valorFase1 = getValor(0, nroColumnas - 1);
        if (Math.abs(valorFase1) > epsilon) {
            return true; // Si la suma de artificiales > 0, no es factible
        }
        // Verificación adicional por variable
        for (Map.Entry<Integer, Integer> entry : indiceSolucion.entrySet()) {
            int columna = entry.getKey();
            if (esVariableArtificial(columna)) {
                double valor = getValor(entry.getValue(), nroColumnas - 1);
                if (Math.abs(valor) > epsilon) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean esVariableArtificial(int columna) {
        return indexParaNuevoZ.containsValue(columna);
    }

    @Override
    protected int obtenerColumnaPivote() {
        if (fase1Completada) {
            return maximizar ? obtenerColumnaPivoteMaximizar() : obtenerColumnaPivoteMinimizar();
        } else {
            return obtenerColumnaPivoteMinimizar();
        }
    }

    @Override
    protected boolean esOptimo() {
        if (fase1Completada) {
            return maximizar ? !existenNegativosEnlaFuncionObjetivo() : !existenPositivosEnlaFuncionObjetivo();
        } else {
            return !existenPositivosEnlaFuncionObjetivo();
        }
    }


    @Override
    protected double calcularValorFuncionObjetivo() {
        double valor = getValor(0, nroColumnas - 1);
        if (!fase1Completada) {
            return valor;
        }
        return maximizar ? valor : -valor;
    }

    public boolean isFase1Completada() {
        return fase1Completada;
    }

    public int getNumeroVariablesArtificiales() {
        return artificiales;
    }
}