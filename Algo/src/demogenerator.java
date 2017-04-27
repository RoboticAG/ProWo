import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Random;

public class demogenerator {

	static int neueschüleranzahl = 1000;
	static Random rand = new Random();

	static String[] vornamen = { "Peter", "Lea", "Joe", "Franz", "Bob", "Thearesa", "Mia", "Emma", "Hannah", "Ben",
			"Paul", "Noah", "Johanna", "Lara", "Felix", "max", "Lena", "Email", "Lukas", "Finn", "Ella", "Lisa", "Till",
			"Maria", "Jan", "Mira", "Helena", "Maria", "Nora", "Fabian", "Maria", "Alina", "Simon", "Aaron", "Samuel",
			"Erik", "Linus", "Leonard", "Isabella", "Pia", "Tim", "Sebatian", "Marc", "Marco", "Paula", "Sarah",
			"Frieda" };

	static String[] nachnamen = { "Lustig", "Müller", "Werner", "Bosch", "Carr", "Waldmann", "Fischer", "Meyer",
			"Becker", "Schulz", "Hoffmann", "Schäfer", "Koch", "Bauer", "Richter", "Klein", "Wolf", "Schröder",
			"Neumann", "Schwarz", "Zimmermann", "Braun", "Krüger", "Hartmann", "Lange", "Krause", "Meier", "Lehmann",
			"Schmid", "Maier", "Köhler", "Herrmann", "König", "Walter", "Mayer", "Huber", "Kaiser", "Fuchs", "Lang",
			"Scholz", "Möller", "Weiß", "Jung", "Hahn", "Keller", "Winker", "Baumann", "Albrecht", "Schuster" };

	static String[] projektliste = { "Q23", "Q25", "Q27" };

	static Connection verbindung;

	public static void main(String[] args) throws ClassNotFoundException {
		try {
			Class.forName("com.mysql.jdbc.Driver");
			verbindung = DriverManager.getConnection("jdbc:" + confi.dburl, confi.dbuser, confi.dbpwd);
			try {
				Statement anweisung = verbindung.createStatement();
				ResultSet liste = anweisung.executeQuery("SELECT * FROM " + confi.tableschüler + ";");

				int nrcounter = 0;
				while (liste.next()) {
					nrcounter++;
				}

				for (int counter = 0; counter <= neueschüleranzahl; counter++) {
					System.out.println("Schülernummer: " + counter);
					anweisung = verbindung.createStatement();
					String rndmvorname = vornamen[rand.nextInt(vornamen.length)];
					String rndmnachname = nachnamen[rand.nextInt(nachnamen.length)];

					String rndmerstesProjekt = projektliste[rand.nextInt(projektliste.length)];
					String rndmzweitesProjekt = projektliste[rand.nextInt(projektliste.length)];
					String rndmdrittesProjekt = projektliste[rand.nextInt(projektliste.length)];

					anweisung.execute("INSERT INTO " + confi.tableschüler + " (id,nr,vorname,nachname,"
							+ confi.spalten[0] + "," + confi.spalten[1] + "," + confi.spalten[2] + ") VALUES ('QZwd"
							+ Integer.toString((nrcounter - 7) * 2) + "','" + Integer.toString(nrcounter++) + "','"
							+ rndmvorname + "','" + rndmnachname + "','" + rndmerstesProjekt + "','"
							+ rndmzweitesProjekt + "','" + rndmdrittesProjekt + "');");
				}
			} catch (SQLException e) {
				confi.LogError(e);
				System.out.println("## Fehler: " + e);
			}

		} catch (SQLException e) {
			confi.LogError(e);
			System.out.println("## Fehler: " + e);
		} finally {
			try {
				verbindung.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
