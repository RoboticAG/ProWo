import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;

public class confi {

	public static String databasename = "db_35946_1";
	public static String dburl = "mysql://dogsitting-rhein-main.lima-db.de:3306/" + databasename;
	public static String dbuser = "";
	public static String dbpwd = "";

	// Weitere URLs
	public static String URLCreateBackUp = "http://172.16.0.76/projektwoche/java.php";

	public static String tableschüler = "schuelerliste";
	public static String tablelehrer = "";
	public static String tableprojekte = "projekte";

	public static Integer startschüler = 1;
	public static Integer Maxschüler = 5; // +1; ? Der Max Scüler wird nicht
											// mehr abgefragt

	public static String[] spalten = { "erstwahl", "zweitwahl", "drittwahl" };
	public static String endProjektname = "finalProjekt";
	public static String notfallprojekt = "";
	public static String NameDerSpalteFürNullSchülerInDerStatistik = "NullSchüler";

	// Gewichtung der Wahldurchgänge
	public static int FaktorErstWahl = 3;
	public static int FaktorZweitWahl = 2;
	public static int FaktorDrittWahl = 1;
	public static int FaktorNullWahl = 1;

	// TODO Nutzername für [[USER]] eintragen
	public static String errorlogpfad = "C:\\Users\\praktikant\\Documents\\ProWoERRLog.txt";

	// Consolen-Farben
	public static final String CRESET = "\u001B[0m";
	public static final String CBLACK = "\u001B[30m";
	public static final String CRED = "\u001B[34m";
	public static final String CPURPLE = "\u001BB[31m";
	public static final String CGREEN = "\u001B[32m";
	public static final String CYELLOW = "\u001B[33m";
	public static final String CBLUE = "\u001B[35m";
	public static final String CCYAN = "\u001B[36m";
	public static final String CWHITE = "\u001B[37m";

	public static String getSpalten() {
		String re = "";
		for (String x : spalten) {
			re += x + ", ";
		}
		return re;
	}

	public static void LogError(SQLException e) {
		PrintWriter pw;
		try {
			pw = new PrintWriter(new FileWriter(errorlogpfad, true));
			e.printStackTrace(pw);
		} catch (IOException e1) {
			System.out.println("#");
			System.err.println("#- " + e1);
			System.out.println("#");
		}
	}

	public static void LogError(Exception e) {
		PrintWriter pw;
		FileWriter fw;
		try {
			fw = new FileWriter(errorlogpfad, true);
			pw = new PrintWriter(fw);
			e.printStackTrace(pw);
			fw.close();
		} catch (IOException e1) {
			System.out.println("#");
			System.err.println("# Fehler beim schreiben des Error Logs - " + e1);
			System.out.println("#");
		}
	}
}
