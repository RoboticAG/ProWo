import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class main2 {
	static String reok = "OK";// confi.CGREEN + "OK" + confi.CRESET;
	static String refail = "FAILED"; // confi.CRED + "FAILED" +
										// confi.CRESET;
	static Boolean stat = true;

	static Connection verbindung; // Speichert die Verbindung zum Server
	static Statement anweisung;

	/* ---------------------------------------- */

	public static void main(String[] args) {
		System.out.println("Starte ....");

		System.out.println("################## Config ##################");
		System.out.println("Host: " + confi.dburl);
		System.out.println("Datenbank: " + confi.databasename);
		System.out.println("User: " + confi.dbuser);
		System.out.println("Passwort: " + confi.dbpwd);
		System.out.println("");
		System.out.println("Tabele mit dem Sch�ler: " + confi.tablesch�ler);
		System.out.println("Tabele mit dem Lehrern: " + confi.tablesch�ler);
		System.out.println("Tabele der Projekte: " + confi.tableprojekte);
		System.out.println("");
		System.out.println("Spaltennamen: " + confi.getSpalten());
		System.out.println("Finales Projekt: " + confi.endProjektname);
		System.out.println("Notfallprojekt: " + confi.notfallprojekt);
		System.out.println("");
		System.out.println("############################################");
		System.out.println("################# Start ####################");
		System.out.println("#                                          #");
		System.out.println("# Verbindung aufbauen: " + connectToDatabase());
		System.out.println("#");
		connectionClose();
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

	public static String connectionClose() {
		try {
			verbindung.close();
		} catch (SQLException e) { // Verb. schlie�en fehlgechlagene
			confi.LogError(e);
			System.err.println("Fehler beim Schlie�en der Verbindung. Detail im ErrorLog");
			return refail + "\n" + e;
		}
		return reok;
	}

	public static void letsGO() {
		// ----- Trage alle Sch�ler in ihre erstwahle ein -------------
		try {
			anweisung = verbindung.createStatement();
			ResultSet Sch�lerListe = anweisung.executeQuery("SELECT * FROM " + confi.tablesch�ler + ";");
			while (Sch�lerListe.next()) {
				String ProjektID = Sch�lerListe.getString(confi.spalten[0]);
				anweisung = verbindung.createStatement();
				anweisung.execute(
						"INSERT INTO `" + ProjektID + "` (`id`) VALUES ('" + Sch�lerListe.getString("id") + "');");

				anweisung = verbindung.createStatement();
				anweisung.execute("UPDATE " + confi.tablesch�ler + " SET " + confi.endProjektname + "='" + ProjektID
						+ "' WHERE id='" + Sch�lerListe.getString("id") + "';");
			}

			// -------------- END intragung ---------------------

			// TODO !!! LOCK-Funktion einbauen

			// ------------------ Gehe die Projekte nach und nach durch
			// ---------------
			try {
				anweisung = verbindung.createStatement();
				ResultSet ProjekteListe = anweisung.executeQuery("SELECT * FROM " + confi.tableprojekte + ";");

				while (ProjekteListe.next()) {

					try {
						System.out.println("Ausgew�hltes Projekt: " + ProjekteListe.getString("titel"));

						anweisung = verbindung.createStatement();
						ResultSet SProjektListe = anweisung
								.executeQuery("SELECT * FROM " + ProjekteListe.getString("id") + ";");

						while (SProjektListe.next()) {
							try {
								System.out.println("Sch�ler " + SProjektListe.getString("vorname")
										+ SProjektListe.getString("nachname"));

								anweisung = verbindung.createStatement(); // Sch�lerInfos
																			// laden
								ResultSet Sch�lerInfo = anweisung.executeQuery("SELECT * FROM " + confi.tablesch�ler
										+ " WHERE id='" + SProjektListe.getString("id") + "';");

								anweisung = verbindung.createStatement(); // Liste
																			// aller
																			// Eintr�ge
																			// der
																			// 2.
																			// Wahl
								ResultSet zweitesProjektListe = anweisung
										.executeQuery("SELECT * FROM " + Sch�lerInfo.getString(confi.spalten[1]) + ";");

								anweisung = verbindung.createStatement();// Liste
																			// aller
																			// Eintr�ge
																			// der
																			// 3.
																			// Wahl
								ResultSet drittesProjektListe = anweisung
										.executeQuery("SELECT * FROM " + Sch�lerInfo.getString(confi.spalten[2]) + ";");

								anweisung = verbindung.createStatement();
								ResultSet zweitesProjektInfo = anweisung
										.executeQuery("SELECT * FROM " + confi.tableprojekte + " WHERE id="
												+ Sch�lerInfo.getString(confi.spalten[1]) + ";");

								anweisung = verbindung.createStatement();
								ResultSet drittesProjektInfo = anweisung
										.executeQuery("SELECT * FROM " + confi.tableprojekte + " WHERE id="
												+ Sch�lerInfo.getString(confi.spalten[2]) + ";");

								if (AnzahlDerTeilnehmer(zweitesProjektListe) < zweitesProjektInfo
										.getInt("maxschueler")) {
									// Man kann diesen Teilnehmer verschieben
									try {
										anweisung = verbindung.createStatement();
										anweisung.execute("INSERT INTO `" + Sch�lerInfo.getString(confi.spalten[1])
												+ "` (`id`) VALUES ('" + Sch�lerInfo.getString("id") + "');");

										anweisung = verbindung.createStatement();
										anweisung.execute("DELETE FROM " + ProjekteListe.getString("id")
												+ " WHERE `id`='" + Sch�lerInfo.getString("id") + "';");
									} catch (SQLException e) {
										System.err.println(" ##### Fehler ####");
										System.err.println(e);
										confi.LogError(e);
									}
								} else if (AnzahlDerTeilnehmer(drittesProjektListe) < drittesProjektInfo
										.getInt("maxschueler")) {
									try {
										// Man kann diesen Sch�ler Verschieben
										anweisung = verbindung.createStatement();
										anweisung.execute("INSERT INTO `" + Sch�lerInfo.getString(confi.spalten[2])
												+ "` (`id`) VALUES ('" + Sch�lerInfo.getString("id") + "');");

										anweisung = verbindung.createStatement();
										anweisung.execute("DELETE FROM " + ProjekteListe.getString("id")
												+ " WHERE `id`='" + Sch�lerInfo.getString("id") + "';");

									} catch (SQLException e) {
										System.err.println(" ##### Fehler ####");
										System.err.println(e);
										confi.LogError(e);
									}
								} else {
									try {
										// Dieser Sch�ler besitzt keine Anderen
										// Kombinationsm�glichkeiten mehr -> So
										// lassen und Locken f�r Diesen
										// Algorithmus
										anweisung = verbindung.createStatement();
										anweisung.execute(
												"UPDATE " + ProjekteListe.getString("id") + " SET `locked`=1;");

									} catch (SQLException e) {
										System.err.println(" ##### Fehler ####");
										System.err.println(e);
										confi.LogError(e);
									}
								}
							} catch (SQLException e) {
								System.err.println(" ##### Fehler ####");
								System.err.println(e);
								confi.LogError(e);
							}

						}
					} catch (SQLException e) {
						System.err.println(" ##### Fehler ####");
						System.err.println(e);
						confi.LogError(e);
					}

				}
			} catch (SQLException e) {
				System.err.println(" ##### Fehler ####");
				System.err.println(e);
				confi.LogError(e);
			}
		} catch (SQLException e) {
			System.err.println(" ##### Fehler ####");
			System.err.println(e);
			confi.LogError(e);
		}
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
		System.out.println("# Anzahl der Teilnehmer: " + counter);
		return counter;
	}

	public static void Suche�berf�llteProjekte() {
		System.out.println(" ~~~~~~~ �berf�llte Projekte ~~~~~~~~");
		System.out.println("~");
		try {
			anweisung = verbindung.createStatement();
			ResultSet ListProjekte = anweisung.executeQuery("SELECT * FROM " + confi.tableprojekte + ";");
			while (ListProjekte.next()) {
				try {
					String ProjektID = ListProjekte.getString("id");

					anweisung = verbindung.createStatement();
					ResultSet TeilnehmerProjekt = anweisung.executeQuery("SELECT * FROM " + ProjektID + ";");
					int TeilnehmerAnzahl = AnzahlDerTeilnehmer(TeilnehmerProjekt);

					if (TeilnehmerAnzahl > ListProjekte.getInt("maxschueler")) {
						try {
							// Das Projekt ist �berf�llt
							anweisung = verbindung.createStatement();
							anweisung.execute("INSERT INTO overflowprojekte (`titel`, `id`) VALUES ('"
									+ ListProjekte.getString("titel") + "','" + ListProjekte.getString("id") + "');");
							System.out.println(" - �berf�llt: " + ListProjekte.getString("title") + " ~ ID: "
									+ ListProjekte.getString("id"));
							System.out.println("");
						} catch (SQLException e) {
							System.err.println(" ##### Fehler ####");
							System.err.println(e);
							confi.LogError(e);
						}
					}
				} catch (SQLException e) {
					System.err.println(" ##### Fehler ####");
					System.err.println(e);
					confi.LogError(e);
				}
			}
		} catch (SQLException e) {
			System.err.println(" ##### Fehler ####");
			System.err.println(e);
			confi.LogError(e);
		}
	}

	public static void TestAnzahlDerTeilnehmer() {
		try {
			anweisung = verbindung.createStatement();
			ResultSet projekt = anweisung.executeQuery("SELECT * FROM Q23");
			int counter = 0;
			try {
				while (projekt.next()) {
					counter++;
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
			System.out.println("# Anzahl der Teilnehmer: " + counter);
		} catch (SQLException e) { // Verb. schlie�en fehlgechlagene
			confi.LogError(e);
			System.err.println("Fehler beim Schlie�en der Verbindung. Detail im ErrorLog");
		}
	}
}
