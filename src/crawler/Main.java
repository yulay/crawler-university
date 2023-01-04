package crawler;

import java.io.IOException;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Main {

    private static DB db = new DB();
    private static String strSql = "";

    public static void main(String[] args) {
        ResultSet rsResultados = null;

        if (db == null) {
            System.out.println("No hay conexión con la base de datos");
            return;
        }

        try {
            /*db.runSql2("TRUNCATE palabras;");  //Eliminamos todos las palabras
            db.runSql2("TRUNCATE urls;");  //Eliminamos todos los registros de las URLS

            strSql = "SELECT id,url FROM universidades";
            rsResultados = db.runSql(strSql);

            while(rsResultados.next())
                processPage(rsResultados.getString("url").trim(), rsResultados.getInt("id"));*/

            strSql = "SELECT id_universidad,url FROM urls";
            rsResultados = db.runSql(strSql);
            while(rsResultados.next())
                processWords(rsResultados.getString("url"), rsResultados.getInt("id_universidad"));

            System.out.println("**************************************");
            System.out.println("***********RESUMEN**************");
            System.out.println("**************************************");
            System.out.println("***********10 PALABRAS MÁS USADAS**************");
            //Mostrar las 10 palabras más usadas de cada universidad
            diezPorUniversidad(1);
            //Mostrar las 10 palabras menos usadas de cada universidad
            System.out.println("***********10 PALABRAS MENOS USADAS**************");
            diezPorUniversidad(2);
            //Mostrar las 10 palabras más usadas por las universidades
            //Mostrar las 10 palabras menos usadas por las universidades
        } catch (Exception ex) {
            System.out.println("------main------");
            ex.printStackTrace();
        }
    }

    public static void processPage(String strUrl, int iId) {
        Document doc = null;

        if (strUrl.length() == 0) {
            return;
        }

        if (strUrl.startsWith("https://")) {
            return;
        }

        if (strUrl.contains(".pdf") || strUrl.contains("@") || strUrl.contains(".jpg") || strUrl.contains(".png") || strUrl.contains(".swf") || strUrl.contains(".xls") || strUrl.contains(".doc") || strUrl.contains(".docx")) {
            return;
        }

        if (strUrl.endsWith("/")) {
            strUrl = strUrl.substring(0, strUrl.length() - 1);
        }

        //Verificar si la URL a agregar ya existe
        if (existeURL(strUrl, iId)) {
            return;
        }

        strSql = "INSERT INTO crawler.urls VALUES (NULL,'" + String.valueOf(iId) + "','" + strUrl + "');";

        try {
            //Ejecutar la consulta
            db.runSql2(strSql);

            //get useful information
            //doc = Jsoup.connect(strUrl).get();
            doc = get(strUrl);

            if (doc.text().contains("research")) {
                System.out.println(strUrl);
            }

            //get all links and recursively call the processPage method
            Elements questions = doc.select("a[href]");

            for (Element link : questions) {
                if (link.attr("href").contains(strUrl.substring(11, strUrl.length()))) {
                    processPage(link.attr("abs:href").trim(), iId);
                }
            }
        } catch (Exception ex) {
            System.out.println("------processPage------");
            ex.printStackTrace();
        }
    }

    public static Document get(String url) throws IOException {
        Connection connection = Jsoup.connect(url).userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6");
        Connection.Response response = connection.execute();
        connection.cookies(response.cookies());
        return connection.get();
}

    private static boolean existeURL(String strUrl, int iId) {
        ResultSet rsResultados = null;
        boolean bRetorno = false;

        strSql = "SELECT * FROM urls WHERE id_universidad = '" + iId + "' AND url = '" + strUrl + "';";

        try {
            rsResultados = db.runSql(strSql);

            if (rsResultados.next()) {
                bRetorno = true;
            }
        } catch (Exception ex) {
            System.out.println("------existeURL------");
            ex.printStackTrace();
        }

        return bRetorno;
    }

    private static boolean existePalabra(String strPalabra, int iId) {
        ResultSet rsResultados = null;
        boolean bRetorno = false;

        strSql = "SELECT * FROM palabras WHERE id_universidad = '" + iId + "' AND palabra = '" + strPalabra + "';";

        try {
            rsResultados = db.runSql(strSql);

            if (rsResultados.next()) {
                bRetorno = true;
            }
        } catch (Exception ex) {
            System.out.println("------existeURL------");
            ex.printStackTrace();
        }

        return bRetorno;
    }

    private static void processWords(String strUrl, int iId) {
        try {
            URL url = new URL(strUrl);
            String pageText = Jsoup.parse(url, 4000).text();

            //Eliminar la aparición de las comas
            pageText = pageText.trim().replaceAll(",", "");

            //Eliminar la aparición de los puntos
            //pageText = pageText.trim().replaceAll(".", "");

            //Eliminar la aparición de las comillas dobles
            pageText = pageText.trim().replaceAll("\"", "");

            //Eliminar la aparición de multiples espacios en blanco consecutivos
            pageText = pageText.trim().replaceAll("^ +| +$|( )+", "$1");

            String palabras[] = pageText.trim().toLowerCase().split(" ");
            for(int i=0; i < palabras.length; i++)
            {
                //Verificar que sea una palabra válida
                if(!esPalabraValida(palabras[i].trim()))
                    continue;

                //Verificar si la palabra a agregar ya existe
                if (existePalabra(palabras[i].trim(), iId)) {
                    //Incrementar cantidad en una unidad
                    strSql = "UPDATE crawler.palabras SET cantidad=cantidad+1 WHERE id_universidad='" + String.valueOf(iId) + "' AND palabra='" + palabras[i].trim() + "';";
                    System.out.println(strSql);
                    
                    //Ejecutar la consulta
                    db.runSql2(strSql);
                }
                else {
                    //Insertar la nueva palabra
                    strSql = "INSERT INTO crawler.palabras VALUES (NULL,'" + String.valueOf(iId) + "','" + palabras[i].trim() + "','" + String.valueOf(1) + "');";
                    System.out.println(strSql);

                    //Ejecutar la consulta
                    db.runSql2(strSql);
                }
            }
        }
        catch (Exception ex) {
            System.out.println("------processWords------");
            ex.printStackTrace();
        }
    }

    private static boolean esPalabraValida(String trim) {
        boolean bRetorno = false;

        char subPalabras[] = trim.toCharArray();
        for(int i=0; i < subPalabras.length; i++) {
            if(Character.isLetter(subPalabras[i]))
                return true;
        }

        return bRetorno;
    }

    private static void diezPorUniversidad(int i) {
        ResultSet rsResultados, rsResultados2 = null;
        strSql = "SELECT id,nombre FROM universidades";
        try {
            rsResultados = db.runSql(strSql);
            
            while(rsResultados.next()) {
                System.out.println(rsResultados.getString("nombre"));
                switch(i){
                    case 1:  //1 es más usada
                        strSql = "SELECT palabra,cantidad FROM palabras WHERE id_universidad='" + String.valueOf(rsResultados.getInt("id")) + "' ORDER BY cantidad DESC LIMIT 0,10;";
                        rsResultados2 = db.runSql(strSql);
                        while(rsResultados2.next()) {
                            System.out.println(rsResultados2.getString("palabra") + " - " + rsResultados2.getInt("cantidad"));
                        }
                        break;
                    case 2:  //2 es menos usada
                        strSql = "SELECT palabra,cantidad FROM palabras WHERE id_universidad='" + String.valueOf(rsResultados.getInt("id")) + "' ORDER BY cantidad ASC LIMIT 0,10;";
                        rsResultados2 = db.runSql(strSql);
                        while(rsResultados2.next()) {
                            System.out.println(rsResultados2.getString("palabra") + " - " + rsResultados2.getInt("cantidad"));
                        }
                        break;
                }
            }
        }
        catch (SQLException ex) {
            System.out.println("------diezPorUniversidad------");
            ex.printStackTrace();
        }
    }
}