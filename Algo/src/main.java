import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

public class main {

	static String reok = "OK";
	static String refail = "FAILED";
	static Boolean stat = true;

	static Connection verbindung; // Speichert die Verbindung zum Server
	static Statement anweisung;
	static ResultSet schülerresult;

	static Map<String, Integer> zusammenfassung = new HashMap<String, Integer>();

	public static void main(String[] args) throws SQLException {
		System.out.println("Starte ....");

		System.out.println("################## Config ##################");
		System.out.println("Host: " + confi.dburl);
		System.out.println("Datenbank: " + confi.databasename);
		System.out.println("User: " + confi.dbuser);
		System.out.println("Passwort: " + confi.dbpwd);
		System.out.println("");
		System.out.println("Tabele mit dem Schüler: " + confi.tableschüler);
		System.out.println("Tabele mit dem Lehrern: " + confi.tableschüler);
		System.out.println("Tabele der Projekte: " + confi.tableprojekte);
		System.out.println("");
		System.out.println("Spaltennamen: " + confi.getSpalten());
		System.out.println("Finales Projekt: " + confi.endProjektname);
		System.out.println("Notfallprojekt: " + confi.notfallprojekt);
		System.out.println("");
		initZusammenfassung();
		System.out.println("############################################");
		System.out.println("################# Start ####################");
		System.out.println("#                                          #");
		System.out.println("# Verbindung aufbauen: " + connectToDatabase());

		System.out.println("");

		if (stat) {
			System.out.println("# Let's go !...");
			boolean algostat = letsGo();
			if (algostat) {
				// Es hat alles Funktioniert
				System.out.println("* Verteilung: " + reok);
				System.out.println("***************************");
				printZusammenfassung();
			} else {
				// Es gab fehler
				System.out.println("* Verteilung: " + refail);
			}
		} else {
			System.out.println("# ABBRUCH - Fehler");
		}

	}

	public static String connectToDatabase() {
		try {
			Class.forName("com.mysql.jdbc.Driver");
			verbindung = DriverManager.getConnection("jdbc:" + confi.dburl, confi.dbuser, confi.dbpwd);
			anweisung = verbindung.createStatement();
		} catch (Exception e) {
			confi.LogError(e);
			stat = false; // Damit er nicht weitermacht
			return refail + " ---- " + e;
		}
		return reok;// Verbindung aufgebaut :)
	}

	public static void testSchülerliste() {
		try {
			ResultSet result = anweisung.executeQuery("SELECT * FROM " + confi.tableschüler + ";");

			while (result.next()) {
				System.out.println("Schüler: " + result.getString("vorname") + " " + result.getString("nachname"));
			}

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			connectionClose();
		}
	}

	public static String connectionClose() {
		try {
			verbindung.close();
		} catch (SQLException e) { // Verb. schließen fehlgechlagene
			confi.LogError(e);
			System.err.println("Fehler beim Schließen der Verbindung. Detail im ErrorLog");
			return refail + "\n" + e;
		}
		return reok;
	}

	public static boolean letsGo() throws SQLException {
		String Vorname;
		String Nachname;
		String SchülerID;

		for (String aktuelleWahl : confi.spalten) {

			System.out.println("************************ " + aktuelleWahl + " ************************");
			System.out.println("*");

			// Stelle 1
			try {
				schülerresult = anweisung.executeQuery("SELECT * FROM " + confi.tableschüler + ";");
			} catch (SQLException e) {
				System.out.println("* FEHLER: " + e);
				confi.LogError(e);
				break;
			}

			// Stelle 2
			while (schülerresult.next()) {
				anweisung = verbindung.createStatement();
				System.out.print("* Schülernr.: " + schülerresult.getInt("nr") + " -> ");
				try {

					Vorname = schülerresult.getString("vorname");
					Nachname = schülerresult.getString("nachname");
					SchülerID = schülerresult.getString("id");
					System.out.println("* " + Vorname + " " + Nachname + " ~ " + SchülerID);

					// Stelle 3
					if (schülerresult.getString(confi.endProjektname).equals("")
							|| schülerresult.getString(confi.endProjektname).equals(" ")) {

						// Die ID des Projektes der Wahl nehmen
						String projektid = schülerresult.getString(aktuelleWahl);
						System.out.print("* ProjektID: " + projektid + " -> ");

						if (!projektid.equals(" ") && projektid != null && !projektid.equals("")) {

							// Stelle 4
							anweisung = verbindung.createStatement();
							ResultSet singleprojektresult = anweisung.executeQuery("SELECT * FROM " + projektid + ";");
							singleprojektresult.next();

							// Ziel: Wähle alle Informationen aus der allg.
							// Projekte Liste von dem Projekt mit der ID
							// [projekt-ID]
							// Stelle 5
							anweisung = verbindung.createStatement();
							ResultSet infoprojektresult = anweisung.executeQuery(
									"SELECT * FROM " + confi.tableprojekte + " WHERE id = '" + projektid + "';");

							// Ist das Projekt voll? Stelle
							if (AnzahlDerTeilnehmer(singleprojektresult) >= infoprojektresult.getInt("maxschueler")) {
								System.out.println("VOLL !");

							} else {
								// Projekt hat noch nen Platz frei
								// Schüler ins Projekt hinzufügen
								// TODO Syntax checken
								anweisung = verbindung.createStatement();
								anweisung.execute(
										"INSERT INTO `" + projektid + "` (`vorname`, `nachname`, `id`) VALUES ('"
												+ Vorname + "', '" + Nachname + "', '" + SchülerID + "')");

								// Schülereintrag UPDATEN || Geht auch am ende
								// ->
								// 'nr=" + schülernummer + ";");
								anweisung = verbindung.createStatement();
								anweisung.execute("UPDATE " + confi.tableschüler + " SET " + confi.endProjektname + "='"
										+ projektid + "' WHERE id='" + SchülerID + "';");
								System.out.println("* ** " + Vorname + " " + Nachname + " ist im Projekt ");
								// Statistik erfassen
								int statistik = zusammenfassung.get(aktuelleWahl);
								zusammenfassung.put(aktuelleWahl, ++statistik);
							}
						} else {
							System.out.println("* !! Kein Projekt angegeben!!");
						}

					}
				} catch (SQLException e) {
					System.err.println("* FEHLER: " + e);
					confi.LogError(e);
					System.out.println("* #### Datail im ERRORLOG.txt ###");
					return false;
				}
				System.out.println("*");
			}
			try {
				Thread.sleep(4000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				System.out.println(e);
			}
		}
		return true;
	}

	public static int AnzahlDerTeilnehmer(ResultSet projekt) {
		int counter = 0;
		try {
			while (projekt.next()) {
				counter++;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return counter++;

	}

	public static void SucheNachNullSchülern() {
		/*
		 * Diese Funktion geht jeden Schüler einmal durch und schaut nach den
		 * Nuller Schülern Das Sind Schüler die kein Projekt haben
		 */
		anweisung = verbindung.createStatement();
		ResultSet SchülerListe = anweisung.executeQuery("SELECT * FROM " + confi.tableschüler + ";");

		while (SchülerListe.next()) {
			if (SchülerListe.getString(confi.endProjektname).equals("")) {
				// Der Schüler hat kein Projekt -> Statistik zusammenfassen
				int statistik = zusammenfassung.get(confi.NameDerSpalteFürNullSchülerInDerStatistik);
				zusammenfassung.put(confi.NameDerSpalteFürNullSchülerInDerStatistik, ++statistik);
			}
		}
	}

	// Braucht man erstamal net
	public static void initZusammenfassung() {
		for (String i : confi.spalten) {
			zusammenfassung.put(i, 0);
		}
		zusammenfassung.put(confi.NameDerSpalteFürNullSchülerInDerStatistik , 0)
	}

	public static int GeneriereScore() {
		int score = 0;

		// ErstWahl
		String SpaltenName = confi.spalten[0];
		score += (zusammenfassung.get(SpaltenName).intValue() * confi.FaktorErstWahl);

		// ZweitWahl
		SpaltenName = confi.spalten[1];
		score += (zusammenfassung.get(SpaltenName).intValue() * confi.FaktorZweitWahl);

		// DrittWahl
		SpaltenName = confi.spalten[2];
		score += (zusammenfassung.get(SpaltenName).intValue() * confi.FaktorDrittWahl);

		// NullWahl
		SpaltenName = confi.NameDerSpalteFürNullSchülerInDerStatistik;
		score += (zusammenfassung.get(SpaltenName).intValue() * confi.FaktorNullWahl);

		return score;
	}

	public static void StarteBackup(int score) {
		URL calledUrl;
		try {
			calledUrl = new URL(confi.URLCreateBackUp);
			URLConnection phpConnection = calledUrl.openConnection();

			HttpURLConnection httpBasedConnection = (HttpURLConnection) phpConnection;
			httpBasedConnection.setRequestMethod("POST");
			httpBasedConnection.setDoOutput(true);
			StringBuffer paramsBuilder = new StringBuffer();

			paramsBuilder.append("&score=");
			paramsBuilder.append(String.valueOf(score));

			PrintWriter requestWriter = new PrintWriter(httpBasedConnection.getOutputStream(), true);
			requestWriter.print(paramsBuilder.toString());
			requestWriter.close();

			BufferedReader responseReader = new BufferedReader(new InputStreamReader(phpConnection.getInputStream()));

			String receivedLine;
			StringBuffer responseAppender = new StringBuffer();

			while ((receivedLine = responseReader.readLine()) != null) {
				responseAppender.append(receivedLine);
				responseAppender.append("\n");
			}
			responseReader.close();
			System.out.println(responseAppender.toString());

		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println(e);
		}

	}
}