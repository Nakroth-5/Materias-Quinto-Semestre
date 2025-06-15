package org.universidad.granm.claseabstracta;

import java.util.HashMap;
import java.util.Map;

import static org.universidad.granm.metodos.MGranM.indexParaNuevoZ;

public class MGranMRefactorizada extends SimplexBase {

    public static double M_valor = 1e6;
    protected Map<Integer, Integer> indexParaNuevoZ;

    public MGranMRefactorizada(double[] funcionObjetivo, double[][] restricciones,
                               double[] terminosIndependientes, String[] tipoRestricciones,
                               boolean maximizar) {
        super();
        this.maximizar = maximizar;
        this.nroRestricciones = restricciones.length;
        this.nroVariables = funcionObjetivo.length;
        this.indexParaNuevoZ = new HashMap<>();

        inicializarTableauBase(funcionObjetivo, restricciones, terminosIndependientes, tipoRestricciones);

        construirTableauGranM(funcionObjetivo, restricciones, terminosIndependientes, tipoRestricciones);

        actualizarZ();
        guardarPaso("Tabla inicial con Gran M", "", "");
    }

    private void construirTableauGranM(double[] funcionObjetivo, double[][] restricciones,
                                       double[] terminosIndependientes, String[] tipoRestricciones) {
        int colHolgura = nroVariables + 1;
        int colExceso = colHolgura + holguras;
        int colArtificial = colExceso + excesos;

        int idxHolgura = 0, idxExceso = 0, idxArtificial = 0;

        // Fila función objetivo
        setValor(0, 0, 1); // Cambiar de -1 a 1 para consistencia
        for (int j = 0; j < nroVariables; j++) {
            double coeficiente = maximizar ? -funcionObjetivo[j] : funcionObjetivo[j];
            setValor(0, j + 1, coeficiente);
        }
        setValor(0, nroColumnas - 1, 0);

        // Filas de restricciones
        for (int i = 0; i < nroRestricciones; i++) {
            setValor(i + 1, 0, 0);

            for (int j = 0; j < nroVariables; j++)
                setValor(i + 1, j + 1, restricciones[i][j]);

            switch (tipoRestricciones[i]) {
                case "≤":
                    setValor(i + 1, colHolgura + idxHolgura, 1);
                    idxHolgura++;
                    break;
                case "≥":
                    setValor(i + 1, colExceso + idxExceso, -1);
                    setValor(i + 1, colArtificial + idxArtificial, 1);
                    double coefM = maximizar ? M_valor : -M_valor;
                    setValor(0, colArtificial + idxArtificial, coefM);
                    indexParaNuevoZ.put(i + 1, colArtificial + idxArtificial);
                    idxExceso++;
                    idxArtificial++;
                    break;
                case "=":
                    setValor(i + 1, colArtificial + idxArtificial, 1);
                    double coefMIgual = maximizar ? M_valor : -M_valor;
                    setValor(0, colArtificial + idxArtificial, coefMIgual);
                    indexParaNuevoZ.put(i + 1, colArtificial + idxArtificial);
                    idxArtificial++;
                    break;
            }
            setValor(i + 1, nroColumnas - 1, terminosIndependientes[i]);
        }
    }

    @Override
    public void resolver() {
        while (!esOptimo() && solucionEncontrada) {
            iteracionSimplex();
        }

        if (solucionEncontrada) {
            if (hayVariablesArtificialesEnBase()) {
                guardarPaso("Solución no factible: variables artificiales en la base", "", "");
                solucionEncontrada = false;
            } else {
                finalizarSolucion();
            }
        }
    }

    private boolean hayVariablesArtificialesEnBase() {
        for (Map.Entry<Integer, Integer> entry : indiceSolucion.entrySet()) {
            int columna = entry.getKey();
            if (indexParaNuevoZ.containsValue(columna)) {
                double valor = getValor(entry.getValue(), nroColumnas - 1);
                if (Math.abs(valor) > 0) {
                    return true;
                }
            }
        }
        return false;
    }

    private void actualizarZ() {
        indexParaNuevoZ.forEach((fila, columnaArtificial) -> {
            double factorM = maximizar ? -M_valor : M_valor;
            for (int i = 0; i < nroColumnas; i++) {
                setValor(0, i, getValor(0, i) + getValor(fila, i) * factorM);
            }
        });
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
        double valor = getValor(0, nroColumnas - 1);
        return maximizar ? valor : -valor;
    }
}