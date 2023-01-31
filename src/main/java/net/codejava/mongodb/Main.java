package net.codejava.mongodb;

import com.mongodb.BasicDBObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * PEVAL 4 DE ACDA
 *
 * @author Pablo Navarro Vázquez
 */
public class Main {

    /**
     * Objeto de la clase CRUD que usaremos para realizar las consultas, inserciones, borrados y modificaciones
     */
    private static CRUD CRUD;

    /**
     * Objeto tipo BasicBDObject que nos permitirá almacenar la instrucción de las consultas
     */
    private static BasicDBObject consulta;

    /**
     * Lista de objetos tipo BasicBDObject que nos permitirá varias consultas para luego unirlas todas en una más compleja
     */
    private static List<BasicDBObject> dbObjects;

    /**
     * Array que contiene las opciones del menú
     */
    private static final String[] MENU_OPCIONES = {
            "Insertar una receta nueva",
            "Eliminar una receta introduciendo su nombre por teclado.",
            "Modificar los datos del tiempo de elaboración, introduciéndolos por teclado",
            "Visualizar todos los ingredientes de una receta introduciendo por teclado su nombre.",
            "Dar el nombre de todas las recetas que contengan huevos de ingredientes y tengan más de 500 calorías.",
            "Dar el nombre de todas las recetas y el tiempo de elaboración, cuya elaboración sea inferior a 1 hora y que sean primer plato o segundo plato",
            "Dar el nombre de las recetas y los pasos a seguir en su elaboración, que haya que dejarlas reposar y que sean platos únicos o primeros platos",
            "Visualizar todos los datos de las recetas que se realicen en 5 o menos pasos que sean de una dificultad alta (difícil)",
            "Salir"
    };

    public static void main(String[] args) {
        // Conexión en mongodb Compass --> mongodb+srv://pnav:17072003@cluster0.8awmtte.mongodb.net/test
        CRUD =new CRUD("mongodb+srv://pnav:17072003@cluster0.8awmtte.mongodb.net/?retryWrites=true&w=majority","practica04" , "recetas");

        Colores.imprimirVerde("Conectado a la base de datos");
        int respuesta;
        do {
            respuesta = Utilidades.mostrarMenu(MENU_OPCIONES);

            switch (respuesta) {
                case 1:
                    if (CRUD.insertarReceta()) {
                        Colores.imprimirVerde("La receta se ha insertado con éxito");
                    } else {
                        Colores.imprimirRojo("La receta no se ha insertado");
                    }
                    break;
                case 2:
                    mostrarRecetas();
                    CRUD.borrarReceta();
                    break;
                case 3:
                    mostrarRecetas();
                    CRUD.actualizarElaboracion();
                    break;
                case 4:
                    CRUD.mostrarSoloIngredientes(new BasicDBObject("nombre", Utilidades.solicitarCadenaNoVacia("Introduce el nombre de la receta que desea consultar")));
                    break;
                case 5:

                    CRUD.mostrarSoloNombre(consultaPorIngredienteYCalorias());
                    break;
                case 6:

                    CRUD.mostrarSoloNombreYTiempo(consultarPorTiempoYTipoDePlato());
                    break;
                case 7:
                    CRUD.mostrarSoloNombreYPasos(consultarPorElaboracionYTipoDePlato());
                    break;
                case 8:
                    consulta = consultarPorDificultadYNumPasos();

                    CRUD.mostrarDatos(consulta);
                    break;
            }
        } while (respuesta != MENU_OPCIONES.length);
    }

    /**
     * Método para consultar por ingredientes y calorías
     * @return (Devuelve el objeto tipo BasicBDObject que nos permitirá filtrar en la colección)
     */
    private static BasicDBObject consultaPorIngredienteYCalorias() {
        consulta = new BasicDBObject();

        dbObjects = new ArrayList<>();

        dbObjects.add(new BasicDBObject("ingredientes", new BasicDBObject("$elemMatch", new BasicDBObject("nombre", new BasicDBObject("$regex", "huevo")))));
        dbObjects.add(new BasicDBObject("calorias", new BasicDBObject("$gt", 500)));

        consulta.put("$and", dbObjects);
        return consulta;
    }

    /**
     * Método para consultar por tiempo y tipo de plato las recetas
     * @return (Devuelve el objeto tipo BasicBDObject que nos permitirá filtrar en la colección)
     */
    private static BasicDBObject consultarPorTiempoYTipoDePlato() {
        consulta = new BasicDBObject();

        dbObjects = new ArrayList<>();

        dbObjects.add(new BasicDBObject("tiempo.unidad", "minutos"));
        dbObjects.add(new BasicDBObject("tiempo.valor", new BasicDBObject("$lt", 60)));

        List<String> tiposPlato = new ArrayList<>();
        tiposPlato.add("primer plato");
        tiposPlato.add("segundo plato");

        dbObjects.add(new BasicDBObject("tipo", new BasicDBObject("$in", tiposPlato)));

        consulta.put("$and", dbObjects);
        return consulta;
    }

    /**
     * Método para consultar por dificultad y número de pasos las recetas
     * @return (Devuelve el objeto tipo BasicBDObject que nos permitirá filtrar en la colección)
     */
    private static BasicDBObject consultarPorDificultadYNumPasos() {
        consulta = new BasicDBObject();

        dbObjects = new ArrayList<>();

        dbObjects.add(new BasicDBObject("dificultad", "Difícil"));
        dbObjects.add(new BasicDBObject("$expr", new BasicDBObject("$lte", Arrays.asList(new BasicDBObject("$size", "$pasos"), 5))));

        consulta.put("$and", dbObjects);
        return consulta;
    }

    /**
     * Método para consultar por elaboración y tipo de plato de las recetas
     * @return (Devuelve el objeto tipo BasicBDObject que nos permitirá filtrar en la colección)
     */
    private static BasicDBObject consultarPorElaboracionYTipoDePlato() {
        List<String> tiposPlato;

        consulta = new BasicDBObject();
        dbObjects = new ArrayList<>();

        dbObjects.add(new BasicDBObject("pasos", new BasicDBObject("$elemMatch", new BasicDBObject("elaboracion", new BasicDBObject("$regex", "reposa")))));

        tiposPlato = new ArrayList<>();
        tiposPlato.add("primer plato");
        tiposPlato.add("plato único");
        dbObjects.add(new BasicDBObject("tipo", new BasicDBObject("$in", tiposPlato)));

        consulta.put("$and", dbObjects);
        return consulta;
    }

    /**
     * Método para mostrar todos los nombres de las recetas de la colección
     */
    private static void mostrarRecetas() {
        System.out.println("Estas son las recetas que existen");
        CRUD.mostrarSoloNombre(new BasicDBObject());
        System.out.println("////////////////////////////////////////////////////");
    }


}