import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class FiltreMail {


	/**
	 * Compilation de l'apprentissage pour le filtre avec l'execution suivante :
	 *      javac FiltreMail.java
	 * @param args
	 */
	public static void main(String[] args) {
		String message = "basetest\\ham\\0.txt", nomClassifieur = "mon_classifieur", res = "";
		
		if(args.length > 1) {
			nomClassifieur = (String) args[0];
			message = (String) args[1];
		}
		
		FiltreAntiSpam antiSpam = new FiltreAntiSpam(FiltreAntiSpam.M_SPAM, FiltreAntiSpam.M_HAM, "dictionnaire1000en");
		
		try {
			InputStream ips = new FileInputStream(nomClassifieur + ".txt");
			InputStreamReader ipsr = new InputStreamReader(ips);
			BufferedReader br = new BufferedReader(ipsr);

			String[] mots;
			double[] tabApp = new double[antiSpam.dico.size()];

			mots = br.readLine().split(":");
			FiltreAntiSpam.M_HAM = Integer.parseInt(mots[1]);
			mots = br.readLine().split(";");
			int i = 0;
			// On recupere les proba des Ham
			for(String mot : mots) {
				tabApp[i] = Double.parseDouble(mot);
				i++;
			}
			antiSpam.setAppHam(tabApp);

			mots = br.readLine().split(":");
			FiltreAntiSpam.M_SPAM = Integer.parseInt(mots[1]);
			mots = br.readLine().split(";");
			i = 0;
			tabApp = new double[antiSpam.dico.size()];
			// On recupere les proba des Spams
			for(String mot : mots) {
				tabApp[i] = Double.parseDouble(mot);
				i++;
			}
			antiSpam.setAppSpam(tabApp);
				
			br.close();
		} catch (FileNotFoundException e) {
	        e.printStackTrace();
	    } catch (Exception e) {
			System.out.println(e.toString());
		}
		
		
		// On calcule le résultat
		res = antiSpam.probDeSachant(message);
		
		System.out.println("D apres '" + nomClassifieur + "', le message '" + message + "' est un " + res +" !");

	}
}
