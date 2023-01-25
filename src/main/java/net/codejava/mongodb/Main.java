package net.codejava.mongodb;

import com.mongodb.BasicDBObject;
import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;

public class Main {

    private static MongoCollection<Document> col;

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

        MongoClient client = MongoClients.create("mongodb+srv://pnav:17072003@cluster0.8awmtte.mongodb.net/?retryWrites=true&w=majority");
        MongoDatabase db = client.getDatabase("practica04");
        col = db.getCollection("recetas");

        Colores.imprimirVerde("Conectado a la base de datos");

        int respuesta;
        BasicDBObject consulta;
        List<BasicDBObject> dbObjects;

        do {
            Utilidades.crearMenu(MENU_OPCIONES);
            respuesta = Utilidades.solicitarEnteroEnUnRango(1, MENU_OPCIONES.length, "Seleccione una opción");

            switch (respuesta) {
                case 1:
                    insertarReceta();
                    break;
                case 2:
                    borrarReceta();
                    break;
                case 3:
                    actualizarElaboracion();
                    break;
                case 4:
                    consulta = new BasicDBObject();

                    consulta.put("nombre", Utilidades.solicitarCadenaNoVacia("Introduce el nombre de la receta que desea consultar"));

                    mostrarSoloIngredientes(consulta);
                    break;
                case 5:
                    consulta = new BasicDBObject();

                    dbObjects = new ArrayList<>();

                    dbObjects.add(new BasicDBObject("ingredientes", new Document("nombre", "huevos")));
                    dbObjects.add(new BasicDBObject("calorias", new BasicDBObject("$gt", 500)));

                    consulta.put("$and", dbObjects);

                    mostrarSoloNombre(consulta);
                    break;
                case 6:
                    consulta = new BasicDBObject();

                    dbObjects = new ArrayList<>();

                    dbObjects.add(new BasicDBObject("tiempo.unidad", "horas"));
                    dbObjects.add(new BasicDBObject("tiempo.valor", 1));

                    List<String> tiposPlato = new ArrayList<>();
                    tiposPlato.add("primer plato");
                    tiposPlato.add("segundo plato");

                    dbObjects.add(new BasicDBObject("$in", new BasicDBObject("tipo", tiposPlato)));

                    consulta.put("$and", dbObjects);

                    mostrarSoloNombreYTiempo(consulta);
                    break;
                case 7:

                    mostrarDatos();
                    break;
                case 8:
                    consultarPorNombre();
                    break;
            }
        } while (respuesta != MENU_OPCIONES.length);

        client.close();
    }


    public static void insertarReceta() {
//        document1.append("nombre", "Alubias arrocinas con verduras");

        Document docReceta = new Document();

        docReceta.append("tipo", insertarTipos());
        docReceta.append("dificultad", Utilidades.solicitarCadenaNoVacia("Introduce la facilidad de la receta"));
        docReceta.append("nombre", Utilidades.solicitarCadenaNoVacia("Introduce el nombre de la receta"));
        docReceta.append("ingredientes", insertarIngredientes());
        docReceta.append("calorias", Utilidades.solicitarEnteroEnUnRango(1, 1500, "Introduce las calorías"));
        docReceta.append("pasos", insertarPasos());

        Document docTiempo = new Document();
        docTiempo.append("valor", Utilidades.solicitarEnteroEnUnRango(5, 59, "Introduce el tiempo que se tarda en realizar la receta"));
        docTiempo.append("unidad", Utilidades.solicitarCadenaNoVacia("Introduce la unidad en la que se va a medir el tiempo"));

        docReceta.append("tiempo", docTiempo);
        docReceta.append("electrodomestico", Utilidades.solicitarCadenaNoVacia("Introduce el electrodoméstico usado"));

        System.out.println(col.insertOne(docReceta).wasAcknowledged());
    }

    public static void borrarReceta() {

        int numRegistrosBorrados = (int) col.deleteOne(Filters.eq("nombre", Utilidades.solicitarCadenaNoVacia("Introduce el nombre de la receta que deseas borrar"))).getDeletedCount();
        System.out.println("Se han borrado " + numRegistrosBorrados);
    }

    public static void actualizarElaboracion() {
        String nombreRecetaSel = Utilidades.solicitarCadenaNoVacia("Introduce el nombre de la receta que desea modificar");
        col.updateOne(Filters.eq("nombre", nombreRecetaSel), Updates.set("tiempo.valor", Utilidades.solicitarEnteroEnUnRango(1, 60, "Introduce el nuevo tiempo de elaboración de la receta")));
        col.updateOne(Filters.eq("nombre", nombreRecetaSel), Updates.set("tiempo.valor", Utilidades.solicitarCadenaNoVacia("Introduce la nueva medida de tiempo de la receta")));

        int numRegistrosBorrados = (int) col.deleteOne(Filters.eq("nombre", Utilidades.solicitarCadenaNoVacia("Introduce el nombre de la receta que deseas borrar"))).getDeletedCount();
        System.out.println("Se han borrado " + numRegistrosBorrados);
    }

    private static List<String> insertarTipos() {
        List<String> tipos = new ArrayList<>();

        String respuestaBucle = "";

        while (!respuestaBucle.trim().equals("*")) {
            tipos.add(Utilidades.solicitarCadenaNoVacia("Indica el tipo de plato que es la receta (primer plato, plato único, etc.)"));
            respuestaBucle = Utilidades.solicitarCadena("Introduce un asterisco si desea parar de agregar tipos de plato");
        }

//        ingredientes.add(insertarIngrediente("Dientes de ajo", 2, ""));
//        ingredientes.add(insertarIngrediente("Cebolla", 0.5f, ""));
//        ingredientes.add(insertarIngrediente("Zanahorias", 2, ""));
//        ingredientes.add(insertarIngrediente("Patatas", 1, ""));
//        ingredientes.add(insertarIngrediente("Pimentón dulce", 4, "gramos"));
//        ingredientes.add(insertarIngrediente("Laurel", 1, "hoja"));
//        ingredientes.add(insertarIngrediente("Alubia arrocina", 250, "gramos"));
//        ingredientes.add(insertarIngrediente("Agua", 500, "mililitros"));
//        ingredientes.add(insertarIngrediente("Sal", 1, "pizca"));
//        ingredientes.add(insertarIngrediente("Pimienta negra molida", 1, "cucharadita"));

        return tipos;
    }

    private static List<Document> insertarPasos() {
        List<Document> pasos = new ArrayList<>();

        String respuestaBucle = "";

        while (!respuestaBucle.trim().equals("*")) {
            pasos.add(insertarPaso());
            respuestaBucle = Utilidades.solicitarCadena("Introduce un asterisco si desea parar de agregar pasos");
        }

//        pasos.add(insertarPaso("1", "Calentamos un poco de aceite de oliva virgen extra en una cazuela."));
//        pasos.add(insertarPaso("2", "Pochamos los dientes de ajo, pelados y picados, y la cebolla, también pelada y finamente picada"));
//        pasos.add(insertarPaso("3", "Cuando la cebolla esté traslúcida, añadimos la zanahoria y la patata, peladas y troceadas, una cucharadita de pimentón, la hoja de laurel y el caldo de verduras (o agua)"));
//        pasos.add(insertarPaso("4", "Llevamos a ebullición antes de agregar las alubias"));
//        pasos.add(insertarPaso("5", "Cocemos a fuego suave durante una hora o hasta que las alubias estén tiernas. Podemos añadir líquido durante la cocción si vemos que se quedan secas"));
//        pasos.add(insertarPaso("6", "Una vez cocidas, salpimentamos al gusto. Podemos dejar que reposen unas horas o, incluso, hasta el día siguiente. En cualquier caso servimos calientes."));

        return pasos;
    }

    private static Document insertarPaso() {
        Document paso = new Document();

        paso.append("orden", String.valueOf(Utilidades.solicitarEnteroEnUnRango(1, 20, "Introduce el orden del paso")));
        paso.append("elaboracion", Utilidades.solicitarCadenaNoVacia("Introduce la elaboración del paso"));

        return paso;
    }

    private static List<Document> insertarIngredientes() {
        List<Document> ingredientes = new ArrayList<>();

        String respuestaBucle = "";

        while (!respuestaBucle.trim().equals("*")) {
            ingredientes.add(insertarIngrediente());
            respuestaBucle = Utilidades.solicitarCadena("Introduce un asterisco si desea parar de agregar ingredientes");
        }

//        ingredientes.add(insertarIngrediente("Dientes de ajo", 2, ""));
//        ingredientes.add(insertarIngrediente("Cebolla", 0.5f, ""));
//        ingredientes.add(insertarIngrediente("Zanahorias", 2, ""));
//        ingredientes.add(insertarIngrediente("Patatas", 1, ""));
//        ingredientes.add(insertarIngrediente("Pimentón dulce", 4, "gramos"));
//        ingredientes.add(insertarIngrediente("Laurel", 1, "hoja"));
//        ingredientes.add(insertarIngrediente("Alubia arrocina", 250, "gramos"));
//        ingredientes.add(insertarIngrediente("Agua", 500, "mililitros"));
//        ingredientes.add(insertarIngrediente("Sal", 1, "pizca"));
//        ingredientes.add(insertarIngrediente("Pimienta negra molida", 1, "cucharadita"));

        return ingredientes;
    }

    private static Document insertarIngrediente() {
        Document ingrediente = new Document();

        ingrediente.append("nombre", Utilidades.solicitarCadenaNoVacia("Introduce el nombre del ingrediente"));
        ingrediente.append("cantidad", Utilidades.solicitarFloatEnUnRango(0, 5000, "Introduce la cantidad del ingrediente"));
        ingrediente.append("unidades", Utilidades.solicitarCadena("Introduce el tipo de unidad (gramos, cucharadita, etc.)"));

        return ingrediente;
    }

    private static void mostrarSoloIngredientes(BasicDBObject consulta) {
        FindIterable<Document> resultDocument = col.find(consulta);

        MongoCursor<Document> cursor = resultDocument.cursor();
        while (cursor.hasNext()) {
            Document receta = cursor.next();
            System.out.println("Nombre: " + receta.get("nombre"));
            mostrarIngredientes(receta);
        }
        cursor.close();
    }

    private static void mostrarSoloNombre(BasicDBObject consulta) {
        FindIterable<Document> resultDocument = col.find(consulta);

        MongoCursor<Document> cursor = resultDocument.cursor();
        while (cursor.hasNext()) {
            Document receta = cursor.next();
            System.out.println("Nombre: " + receta.get("nombre"));
        }
        cursor.close();
    }

    private static void mostrarSoloNombreYTiempo(BasicDBObject consulta) {
        FindIterable<Document> resultDocument = col.find(consulta);

        MongoCursor<Document> cursor = resultDocument.cursor();
        while (cursor.hasNext()) {
            Document receta = cursor.next();
            System.out.println("Nombre: " + receta.get("nombre"));
            Document tiempo = (Document) receta.get("tiempo");
            System.out.println(tiempo.get("valor") + " " + tiempo.get("unidad"));
        }
        cursor.close();
    }

    private static void mostrarSoloNombreYPasos(BasicDBObject consulta) {
        FindIterable<Document> resultDocument = col.find(consulta);

        MongoCursor<Document> cursor = resultDocument.cursor();
        while (cursor.hasNext()) {
            Document receta = cursor.next();
            System.out.println("Nombre: " + receta.get("nombre"));
            Document tiempo = (Document) receta.get("tiempo");
            System.out.println(tiempo.get("valor") + " " + tiempo.get("unidad"));
        }
        cursor.close();
    }

    private static void mostrarDatos(BasicDBObject consulta) {
        FindIterable<Document> resultDocument = col.find(consulta);

        MongoCursor<Document> cursor = resultDocument.cursor();
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
        }
        cursor.close();
    }

    private static void mostrarIngredientes(Document receta) {
        System.out.println("Ingredientes");
        List<Document> ingredientes = receta.getList("ingredientes", Document.class);

        for (Document ingrediente : ingredientes) {
            System.out.println("\tNombre: " + ingrediente.get("nombre"));
            System.out.println("\tCantidad: " + ingrediente.get("cantidad") + "\tUnidad: " + ingrediente.get("unidades"));
        }
    }

    private static void mostrarPasos(Document receta) {
        System.out.println("Pasos");
        List<Document> pasos = receta.getList("pasos", Document.class);

        for (Document paso : pasos) {
            System.out.println("\t" + paso.get("orden") + ". " + paso.get("elaboracion"));
        }
    }
}