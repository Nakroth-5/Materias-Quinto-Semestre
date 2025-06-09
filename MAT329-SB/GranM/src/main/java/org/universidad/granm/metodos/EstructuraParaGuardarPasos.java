package org.universidad.granm.metodos;

/**
 * Clase que representa la estructura de datos para almacenar la información de cada paso
 * en el método Simplex. Guarda la tabla, las variables de entrada y salida, y una descripción
 * del paso realizado.
 */
public class EstructuraParaGuardarPasos {
    /**
     * Matriz que representa la tabla del método Simplex en un paso específico.
     */
    private double[][] tablaPasos;

    /**
     * Variable que entra a la base en este paso del método Simplex.
     */
    private String variableEntrada;

    /**
     * Variable que sale de la base en este paso del método Simplex.
     */
    private String variableSalida;

    /**
     * Descripción textual que explica lo que ocurre en este paso.
     */
    private String descripcionPaso;

    /**
     * Constructor que inicializa todos los atributos de la estructura.
     * 
     * @param tablaPasos Matriz que representa la tabla del método Simplex
     * @param variableEntrada Variable que entra a la base
     * @param variableSalida Variable que sale de la base
     * @param descripcionPaso Descripción textual del paso
     */
    public EstructuraParaGuardarPasos(double[][] tablaPasos, String variableEntrada, String variableSalida,  String descripcionPaso) {
        this.tablaPasos = copiarTabla(tablaPasos);
        this.variableEntrada = variableEntrada;
        this.variableSalida = variableSalida;
        this.descripcionPaso = descripcionPaso;
    }

    /**
     * Método privado que realiza una copia profunda de la matriz de la tabla.
     * Esto evita problemas de referencia cuando la tabla original cambia.
     * 
     * @param tablaPasos Matriz original a copiar
     * @return Una copia independiente de la matriz original
     */
    private double[][] copiarTabla(double[][] tablaPasos) {
        double[][] copia = new double[tablaPasos.length][];
        for (int i = 0; i < tablaPasos.length; i++) {
            copia[i] = tablaPasos[i].clone();
        }
        return copia;
    }

    /**
     * Obtiene la tabla del método Simplex almacenada en este paso.
     * 
     * @return Matriz que representa la tabla del método Simplex
     */
    public double[][] getTablaPasos() {
        return tablaPasos;
    }

    /**
     * Establece una nueva tabla para este paso.
     * 
     * @param tablaPasos Nueva matriz que representa la tabla del método Simplex
     */
    public void setTablaPasos(double[][] tablaPasos) {
        this.tablaPasos = tablaPasos;
    }

    /**
     * Obtiene la variable que entra a la base en este paso.
     * 
     * @return Nombre de la variable que entra a la base
     */
    public String getVariableEntrada() {
        return variableEntrada;
    }

    /**
     * Establece la variable que entra a la base en este paso.
     * 
     * @param variableEntrada Nuevo nombre de la variable que entra a la base
     */
    public void setVariableEntrada(String variableEntrada) {
        this.variableEntrada = variableEntrada;
    }

    /**
     * Obtiene la variable que sale de la base en este paso.
     * 
     * @return Nombre de la variable que sale de la base
     */
    public String getVariableSalida() {
        return variableSalida;
    }

    /**
     * Establece la variable que sale de la base en este paso.
     * 
     * @param variableSalida Nuevo nombre de la variable que sale de la base
     */
    public void setVariableSalida(String variableSalida) {
        this.variableSalida = variableSalida;
    }

    /**
     * Obtiene la descripción textual de este paso.
     * 
     * @return Descripción del paso
     */
    public String getDescripcionPaso() {
        return descripcionPaso;
    }

    /**
     * Establece una nueva descripción para este paso.
     * 
     * @param descripcionPaso Nueva descripción textual del paso
     */
    public void setDescripcionPaso(String descripcionPaso) {
        this.descripcionPaso = descripcionPaso;
    }

}
