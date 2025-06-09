package org.universidad.granm.metodos;

import java.util.ArrayList;
import java.util.List;

/**
 * Clase que gestiona el almacenamiento de los pasos realizados durante la ejecución del método Simplex.
 * Permite guardar cada iteración del algoritmo con su respectiva tabla, variables de entrada/salida y descripción.
 */
public class GuardarPasos {
    /**
     * Lista que almacena todos los pasos del método Simplex.
     */
    private List<EstructuraParaGuardarPasos> listaPasos;

    /**
     * Constructor que inicializa la lista de pasos vacía.
     */
    public GuardarPasos() {
        this.listaPasos = new ArrayList<>();
    }

    /**
     * Agrega un nuevo paso a la lista de pasos del método Simplex.
     * 
     * @param tablaPasos Matriz que representa el estado de la tabla Simplex en este paso
     * @param variableEntrada Variable que entra a la base en este paso
     * @param variableSalida Variable que sale de la base en este paso
     * @param descripcionPaso Descripción textual de lo que ocurre en este paso
     */
    public void agregarPaso(double[][] tablaPasos, String variableEntrada, String variableSalida, String descripcionPaso) {
        listaPasos.add(new EstructuraParaGuardarPasos(tablaPasos, variableEntrada, variableSalida, descripcionPaso));
    }

    /**
     * Obtiene la lista completa de pasos almacenados.
     * 
     * @return Lista de objetos EstructuraParaGuardarPasos que contienen todos los pasos
     */
    public List<EstructuraParaGuardarPasos> getListaPasos() {
        return listaPasos;
    }

    /**
     * Elimina todos los pasos almacenados.
     * Útil para reiniciar el proceso o comenzar un nuevo problema.
     */
    public void limpiarPasos(){
        listaPasos.clear();
    }

    /**
     * Obtiene la tabla Simplex de un paso específico.
     * 
     * @param indice Índice del paso del cual se quiere obtener la tabla
     * @return Matriz que representa la tabla Simplex en el paso indicado
     */
    public double[][] getTablaPaso(int indice){
        return listaPasos.get(indice).getTablaPasos();
    }
}
