package net.codejava.mongodb;

import com.mongodb.BasicDBObject;
import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;

/**
 * Clase para realizar consultas, insertar y modificar datos y borrar datos
 */
public class CRUD {
    /**
     * Objeto tipo MongoCollection que contiene todos los datos de la colección obtenida en el constructor
     */
    private final MongoCollection<Document> col;

    /**
     * Constructor parametrizado de la clase
     *
     * @param urlConexion (Cadena de texto que contiene la url en la que se encuentra alojado el servidor MongoDB)
     * @param nombreBD    (Cadena de texto que indica el nombre de la base de datos que vamos a usar)
     * @param nombreCol   (Cadena de texto que indica el nombre de la colección (tabla) que vamos a usar)
     */
    public CRUD(String urlConexion, String nombreBD, String nombreCol) {
        MongoClient client = MongoClients.create(urlConexion);
        MongoDatabase db = client.getDatabase(nombreBD);
        this.col = db.getCollection(nombreCol);
    }

    /**
     * Método para insertar recetas pidiéndole al usuario los datos por teclado
     *
     * @return (devuelve un true o un false según si se ha introducido la receta correctamente o no)
     */
    public boolean insertarReceta() {
        Document docReceta = new Document();

        docReceta.append("tipo", insertarTipos());
        docReceta.append("dificultad", Utilidades.solicitarCadenaNoVacia("Introduce la facilidad de la receta"));
        docReceta.append("nombre", Utilidades.solicitarCadenaNoVacia("Introduce el nombre de la receta"));
        docReceta.append("ingredientes", insertarIngredientes());
        docReceta.append("calorias", Utilidades.solicitarEnteroEnUnRango(1, 1500, "Introduce las calorías"));
        docReceta.append("pasos", insertarPasos());

        Document docTiempo = new Document();
        int valor = 0;

        String unidad = Utilidades.solicitarCadenaNoVacia("Introduce la unidad en la que se va a medir el tiempo");
        switch (unidad) {
            case "minutos":
                valor = Utilidades.solicitarEnteroEnUnRango(1, 60, "Introduce el nuevo tiempo de elaboración de la receta (minutos)");
                break;
            case "horas":
                valor = Utilidades.solicitarEnteroEnUnRango(1, 20, "Introduce el nuevo tiempo de elaboración de la receta (horas)");
                break;
            default:
                Colores.imprimirRojo("Debes introducir una de las unidades de tiempo 'minutos' u 'horas'");
        }
        docTiempo.append("unidad", unidad);
        docTiempo.append("valor", valor);

        docReceta.append("tiempo", docTiempo);
        docReceta.append("electrodomestico", Utilidades.solicitarCadenaNoVacia("Introduce el electrodoméstico usado"));

        return col.insertOne(docReceta).wasAcknowledged();
    }

    /**
     * Método para insertar los tipos de la receta pidiéndole al usuario los datos por teclado
     *
     * @return (Devuelve el array de tipos, que se usará para insertarlo en la receta)
     */
    private List<String> insertarTipos() {
        List<String> tipos = new ArrayList<>();

        String respuestaBucle = "";

        while (!respuestaBucle.trim().equals("*")) {
            tipos.add(Utilidades.solicitarCadenaNoVacia("Indica el tipo de plato que es la receta (primer plato, plato único, etc.)"));
            respuestaBucle = Utilidades.solicitarCadena("Introduce un asterisco si desea parar de agregar tipos de plato");
        }
        return tipos;
    }

    /**
     * Método para insertar los pasos de la receta pidiéndole al usuario los datos por teclado
     *
     * @return (Devuelve el array de pasos, que se usará para insertarlo en la receta)
     */
    private List<Document> insertarPasos() {
        List<Document> pasos = new ArrayList<>();

        String respuestaBucle = "";

        int orden = 0;

        while (!respuestaBucle.trim().equals("*")) {
            orden++;
            pasos.add(insertarPaso(orden));
            respuestaBucle = Utilidades.solicitarCadena("Introduce un asterisco si desea parar de agregar pasos");
        }
        return pasos;
    }

    /**
     * Método para insertar un paso de la receta pidiéndole al usuario los datos por teclado
     *
     * @return (Devuelve un objeto tipo Document con los valores del paso pedido al usuario)
     */
    private Document insertarPaso(int i) {
        Document paso = new Document();

        paso.append("orden", String.valueOf(i));
        paso.append("elaboracion", Utilidades.solicitarCadenaNoVacia("Introduce la elaboración del paso " + i));

        return paso;
    }

    /**
     * Método para insertar los ingredientes de la receta pidiéndole al usuario los datos de cada ingrediente por teclado
     *
     * @return (Devuelve el array de ingredientes, que se usará para insertarlo en la receta)
     */
    private List<Document> insertarIngredientes() {
        List<Document> ingredientes = new ArrayList<>();

        String respuestaBucle = "";

        while (!respuestaBucle.trim().equals("*")) {
            ingredientes.add(insertarIngrediente());
            respuestaBucle = Utilidades.solicitarCadena("Introduce un asterisco si desea parar de agregar ingredientes");
        }
        return ingredientes;
    }

    /**
     * Método para insertar un ingrediente de la lista de ingredientes de la receta pidiéndole al usuario los datos por teclado
     *
     * @return (Devuelve el objeto Document con los datos del ingrediente introducido)
     */
    private Document insertarIngrediente() {
        Document ingrediente = new Document();

        ingrediente.append("nombre", Utilidades.solicitarCadenaNoVacia("Introduce el nombre del ingrediente"));
        ingrediente.append("cantidad", Utilidades.solicitarFloatEnUnRango(0, 5000, "Introduce la cantidad del ingrediente"));
        ingrediente.append("unidades", Utilidades.solicitarCadena("Introduce el tipo de unidad (gramos, cucharadita, etc.)"));

        return ingrediente;
    }

    /**
     * Método para borrar una receta de la base de datos
     */
    public void borrarReceta() {
        int numRegistrosBorrados = (int) col.deleteOne(Filters.eq("nombre", Utilidades.solicitarCadenaNoVacia("Introduce el nombre de la receta que deseas borrar"))).getDeletedCount();
        System.out.println("Se han borrado " + numRegistrosBorrados);
    }


    /**
     * Método para actualizar la elaboración de una receta
     */
    public void actualizarElaboracion() {
        String nombreRecetaSel = Utilidades.solicitarCadenaNoVacia("Introduce el nombre de la receta que desea modificar");
        String unidad = Utilidades.solicitarCadenaNoVacia("Introduce la nueva medida de tiempo de la receta");
        int valor = 0;

        switch (unidad) {
            case "minutos":
                valor = Utilidades.solicitarEnteroEnUnRango(1, 60, "Introduce el nuevo tiempo de elaboración de la receta (minutos)");
                break;
            case "horas":
                valor = Utilidades.solicitarEnteroEnUnRango(1, 20, "Introduce el nuevo tiempo de elaboración de la receta (horas)");
                break;
            default:
                Colores.imprimirRojo("Debes introducir una de las unidades de tiempo 'minutos' u 'horas'");
        }
        //Si el valor es distinto a 0, significa que no ha introducido ni horas ni minutos como unidad de tiempo, por lo tanto no modificamos nada
        if (valor != 0) {
            col.updateOne(Filters.eq("nombre", nombreRecetaSel), Updates.set("tiempo.unidad", unidad));
            col.updateOne(Filters.eq("nombre", nombreRecetaSel), Updates.set("tiempo.valor", valor));
        }
    }

    /**
     * Método para mostrar solo los ingredientes de las recetas obtenidas tras la consulta recibida como parámetro
     *
     * @param consulta (Objeto tipo BasicBDObject que usamos para filtrar los datos de la colección)
     */
    public void mostrarSoloIngredientes(BasicDBObject consulta) {
        FindIterable<Document> datos = col.find(consulta);
        MongoCursor<Document> cursor = datos.cursor();

        while (cursor.hasNext()) {
            Document receta = cursor.next();
            System.out.println("Nombre: " + receta.get("nombre"));
            mostrarIngredientes(receta);
        }
        cursor.close();
    }

    /**
     * Método para mostrar solo los nombres de las recetas obtenidas tras la consulta recibida como parámetro
     *
     * @param consulta (Objeto tipo BasicBDObject que usamos para filtrar los datos de la colección)
     */
    public void mostrarSoloNombre(BasicDBObject consulta) {
        FindIterable<Document> datos = col.find(consulta);
        MongoCursor<Document> cursor = datos.cursor();

        while (cursor.hasNext()) {
            Document receta = cursor.next();
            System.out.println("Nombre: " + receta.get("nombre"));
        }
        cursor.close();
    }

    /**
     * Método para mostrar solo el nombre y el tiempo de elaboración de las recetas obtenidas tras la consulta recibida como parámetro
     *
     * @param consulta (Objeto tipo BasicBDObject que usamos para filtrar los datos de la colección)
     */
    public void mostrarSoloNombreYTiempo(BasicDBObject consulta) {
        FindIterable<Document> datos = col.find(consulta);
        MongoCursor<Document> cursor = datos.cursor();

        while (cursor.hasNext()) {
            Document receta = cursor.next();
            System.out.println("Nombre: " + receta.get("nombre"));
            Document tiempo = (Document) receta.get("tiempo");
            System.out.println(tiempo.get("valor") + " " + tiempo.get("unidad"));
        }
        cursor.close();
    }

    /**
     * Método para mostrar solo los nombres y pasos de las recetas obtenidas tras la consulta recibida como parámetro
     *
     * @param consulta (Objeto tipo BasicBDObject que usamos para filtrar los datos de la colección)
     */
    public void mostrarSoloNombreYPasos(BasicDBObject consulta) {
        FindIterable<Document> datos = col.find(consulta);
        MongoCursor<Document> cursor = datos.cursor();

        while (cursor.hasNext()) {
            Document receta = cursor.next();
            System.out.println("Nombre: " + receta.get("nombre"));

            mostrarPasos(receta);
        }
        cursor.close();
    }

    /**
     * Método para mostrar todos los datos de las recetas obtenidas tras la consulta recibida como parámetro
     *
     * @param consulta (Objeto tipo BasicBDObject que usamos para filtrar los datos de la colección)
     */
    public void mostrarDatos(BasicDBObject consulta) {
        FindIterable<Document> datos = col.find(consulta);
        MongoCursor<Document> cursor = datos.cursor();

        while (cursor.hasNext()) {
            Document receta = cursor.next();
            System.out.println("Tipo: " + receta.getList("tipo", String.class));
            System.out.println("Nombre: " + receta.get("nombre") + "\tDificultad: " + receta.get("dificultad"));

            mostrarIngredientes(receta);

            System.out.println("Calorias: " + receta.get("calorias"));

            mostrarPasos(receta);

            System.out.println("Tiempo");
            Document tiempo = (Document) receta.get("tiempo");
            System.out.println("\tValor: " + tiempo.get("valor") + "\tUnidad: " + tiempo.get("unidad"));

            System.out.println("Electrodoméstico: " + receta.get("electrodomestico"));
            System.out.println("//////////////////////////////////////////////////////////////////");
        }
        cursor.close();
    }

    /**
     * Método para mostrar los ingredientes de una receta
     */
    public void mostrarIngredientes(Document receta) {
        System.out.println("Ingredientes");
        List<Document> ingredientes = receta.getList("ingredientes", Document.class);

        for (Document ingrediente : ingredientes) {
            System.out.println("\tNombre: " + ingrediente.get("nombre"));
            System.out.println("\tCantidad: " + ingrediente.get("cantidad") + "\tUnidad: " + ingrediente.get("unidades"));
        }
    }

    /**
     * Método para mostrar los pasos de una receta
     */
    public void mostrarPasos(Document receta) {
        System.out.println("Pasos");
        List<Document> pasos = receta.getList("pasos", Document.class);

        for (Document paso : pasos) {
            System.out.println("\t" + paso.get("orden") + ". " + paso.get("elaboracion"));
        }
    }
}
