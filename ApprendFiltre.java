import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;

public class ApprendFiltre {

	
	/**
	 * Compilation de l'apprentissage pour le filtre avec l'execution suivante :
	 *      javac ApprendFiltre.java
	 * @param args
	 */
	public static void main(String[] args) {
		String nomClassifieur = "mon_classifieur", nomDossierApp = "baseapp";
		int nbSpam = 500, nbHam = 2500;
		if(args.length > 3) {
			nomClassifieur = args[0];
			nomDossierApp = args[1];
			nbSpam = Integer.parseInt(args[2]);
			nbHam = Integer.parseInt(args[3]);
		}
		
		FiltreAntiSpam antiSpam = new FiltreAntiSpam(nbSpam,nbHam,"dictionnaire1000en");
		FiltreAntiSpam.BASE_APP = nomDossierApp;

		System.out.println("Apprentissage sur " + nbSpam + " spams et " + nbHam + " hams...");
		antiSpam.apprentissage(nbHam, "ham");
		System.out.println("... c’est très long ...");
		antiSpam.apprentissage(nbSpam, "spam");

		FileWriter file = null;

	    try {
	    	file = new FileWriter(nomClassifieur + ".txt");

	        //Ecriture des proba des Hams
	 		double[] appTab = antiSpam.getAppHam();
	 		file.write("Ham :" + nbHam + "\n");
	        for(int i = 0; i < appTab.length; i++)
	        	file.write(appTab[i] + ";");

	        //Ecriture des proba des Spams
		 	appTab = antiSpam.getAppSpam();
        	file.write("\nSpam :" + nbSpam + "\n");
	        for(int i = 0; i < appTab.length; i++)
	        	file.write(appTab[i] + ";");
	        file.close();
	        	 
	    } catch (FileNotFoundException e) {
	        e.printStackTrace();
	    } catch (IOException e) {
	        e.printStackTrace();
	    }
		System.out.println("Classifieur enregistre dans '" + nomClassifieur + "'.");

		
	}
}
