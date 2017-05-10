
import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;

public class FiltreAntiSpam {

	public ArrayList<String> dico;
	// apprentissage
	public static int M_SPAM 	= 500;
	public static int M_HAM 	= 2500;
	// test
	public static int NB_HAM 	= 500;
	public static int NB_SPAM 	= 500;
	public static String BASE_APP = "baseapp";
	public static String BASE_TEST = "basetest";
	private double[] appSpam;  //tableau de probabilites de chaque mot present dans les messages de base Spam
	private double[] appHam;  //tableau de probabilites de chaque mot present dans les messages de base Ham
	
	/**
	 * Constructeur de la classe Antispam
	 * @param nbSpam
	 * @param nbHam
	 * @param dic : non du fichier du dictionnaire
	 */
	public FiltreAntiSpam(int nbSpam, int nbHam, String dic){
		charger_dictionnaire(dic);
		M_SPAM	= nbSpam;
		M_HAM	= nbHam;
		appSpam = new double[dico.size()];
		appHam 	= new double[dico.size()];
	}
	
	/**
	 * On charge le dictionnaire dans la variable dico
	 * @param dic
	 */
	public void charger_dictionnaire(String dic) { // lit mot par mot

		String fichier = dic + ".txt";
		dico = new ArrayList<String>();

		// lecture du fichier texte
		try {
			InputStream ips = new FileInputStream(fichier);
			InputStreamReader ipsr = new InputStreamReader(ips);
			BufferedReader br = new BufferedReader(ipsr);

			String[] mots;
			String str = null;

			while ((str = br.readLine()) != null) {
				mots = str.split(" ");
				// On ajoute chaque mot dans le dictionnaire
				for (String mot : mots)
					if(mot.length() > 3)
						dico.add(mot);
			}

			br.close();
		} catch (Exception e) {
			System.out.println(e.toString());
		}
	}

	/**
	 * Renvoie un vecteur contenant des :
	 * 	- 0 : si le mot n'est pas present
	 *  - 1 : si le mot est preésent
	 *  
	 * @param fichier
	 * @return
	 */
	public int[] lire_message(String fichier) {

		int tailleDico = dico.size();
		int[] list = new int[tailleDico]; // Matrice retournee

		String[] mots;
		String str = null;

		for(int j = 0; j < tailleDico; j++)
			list[j] = 0;

		try {
			InputStream ips 		= new FileInputStream(fichier);
			InputStreamReader ipsr 	= new InputStreamReader(ips);
			BufferedReader br 		= new BufferedReader(ipsr);

			while ((str = br.readLine()) != null) {
				// On enleve tous les caracteres speciaux
				mots = str.replaceAll("[\\W]", " ").split(" ");
				for (String mot : mots) {
					mot = mot.toUpperCase();
					if (dico.contains(mot)) 
						// On recupere l'index du mot
						for (int j = 0; j < tailleDico; j++) 
							if (mot.equals(dico.get(j))) {
								list[j] = 1;
								break;
							} 
				}
			}
			
			br.close();
		}
		catch(Exception e) {
			System.out.println(e.toString());
		}
		return list;
	}
	
	/**
	 * On enregistre notre base d'apprentissage
	 * 
	 * @param nbMess : nombre de message
	 * @param base : Soit Ham, soit Spam
	 */
	public void apprentissage(int nbMess, String base) {

		System.out.println("Apprentissage des "+base.toUpperCase()+"S ...");
			
		int[][] tabMess; //ensemble des vecteurs de mot de presence de l'apprentissage
		tabMess = new int[nbMess][dico.size()];
		double epsilon = 1;
		
		try {
			if(dico.isEmpty()){
				System.out.println("dico non charge, apprentissageHam impossible");
			}
			else{	
				// On lit tous les messages de la base d'apprentissage selon le type
				for (int i = 0; i < nbMess;i++){
					// On recupere le vecteur qui determine si chaque mot est present (1) ou non (0)
					tabMess[i] = this.lire_message(BASE_APP + "/" + base + "/" + i + ".txt");
				}
				// On parcourt tous les messages
				for (int i = 0; i < dico.size(); i++){
					double somme = 0;
					// On regarde quel mot apparait dans chaque message
					for (int j = 0; j < nbMess;j++){
						somme = somme + tabMess[j][i];
					}
					switch(base) {
						case "ham":
							appHam[i] = (double)(somme + epsilon) / (nbMess + 2 * epsilon);
							break;
						case "spam":
							appSpam[i] = (double)(somme + epsilon) / (nbMess + 2 * epsilon);
							break;
					}		
				}
			}
		}
		catch(Exception e) {
			System.out.println(e.toString());
		}
			
	}

	/**
	 * 
	 * @param msg
	 * @param typ
	 * @return
	 */
	public String probDeSachant(String msg){
		// On recupere le vecteur de presence de mot 
		int[] vectMess = lire_message(msg);
		
		double PSpam = 0, PHam = 0;
		double PYSpam = (double) M_SPAM / (M_HAM + M_SPAM), PYHam = (double) M_HAM / (M_HAM + M_SPAM);
		
		for(int i = 0; i < appSpam.length; i++) {
			if(vectMess[i] == 1) {
				PSpam += Math.log(appSpam[i]);
				PHam += Math.log(appHam[i]);
			}
			else {
				PSpam += Math.log(1 - appSpam[i]);
				PHam += Math.log(1 - appHam[i]);
			}
		}
		
		double ZPlus = PSpam + Math.log(PYSpam);
		double ZMoins = PHam + Math.log(PYHam);
		
		double Px = (Math.exp(ZPlus) + Math.exp(ZMoins));
		
		double PSpamFinale = Math.exp(ZPlus) / Px;
		
		double PHamFinale = Math.exp(ZMoins) / Px;
		
		
		System.out.print("P(Y=SPAM | X=x) = " + PSpamFinale);
		System.out.println(", P(Y=HAM | X=x) = " + PHamFinale);
		
		if(PSpamFinale > PHamFinale) return "SPAM";
		else return "HAM";
		
		
			
	}
	
	/**
	 * 
	 * @param NB_SPAM
	 * @param NB_HAM
	 */
	public void test(){
		
		System.out.println("Test:");
		
		int total 		= NB_HAM + NB_SPAM;
		String[] spam 	= new String[NB_SPAM];
		String[] ham 	= new String[NB_HAM];
		double errSpam 	= 0;
		double errHam 	= 0;
		double errtot 	= 0;
		String msgerr	= "";
		
		for(int i = 0; i < NB_SPAM; i++){
			System.out.print("SPAM numero " + (i + 1) + " : ");
			msgerr = "";
			spam[i] = probDeSachant(BASE_TEST + "/spam/"+i + ".txt");
			if(spam[i] == "HAM") {
				msgerr = "  ***erreur"; 
				errSpam++;
			}
			System.out.println("                => identifie comme un " + spam[i] + msgerr);
		}
		
		for(int i = 0; i < NB_HAM; i++){
			System.out.print("HAM numero " + (i + 1) + " : ");
			msgerr = "";
			ham[i] = probDeSachant(BASE_TEST + "/ham/"+i + ".txt");
			if(ham[i] == "SPAM"){
				msgerr="  ***erreur";
				errHam++;
			}
			System.out.println("                => identifie comme un " + ham[i] + msgerr);
		}
		
		// On calcule les pourcentages d'erreur
		errSpam	= (double)(errSpam / NB_SPAM) * 100;
		errHam	= (double)(errHam / NB_HAM) * 100;
		errtot 	= (double)((NB_SPAM * errSpam) + (NB_HAM * errHam)) / (NB_HAM + NB_SPAM);

		// On affiche les erreurs
		System.out.println("Erreur de test sur les " + NB_SPAM + " SPAM: "+errSpam+" %");
		System.out.println("Erreur de test sur les " + NB_HAM + " HAM: "+errHam+" %");
		System.out.println("Erreur de test globale sur les " + total + " mails: "+ errtot+" %");
	}
	
	public double[] getAppSpam() {
		return appSpam;
	}

	public double[] getAppHam() {
		return appHam;
	}
	
	public void setAppSpam(double[] appSpam) {
		this.appSpam = appSpam;
	}

	public void setAppHam(double[] appHam) {
		this.appHam = appHam;
	}

	/**
	 * Compilation du filtre avec l'execution suivante :
	 *      javac FiltreAntiSpam.java
	 * @param args
	 */
	public static void main(String[] args) {
		
		if(args.length > 2) {
			BASE_TEST = args[0];
			NB_SPAM = Integer.parseInt(args[1]);
			if(NB_SPAM > 500) NB_SPAM = 500;
			NB_HAM = Integer.parseInt(args[2]);
			if(NB_HAM > 2500) NB_HAM = 2500;
			Scanner sc = new Scanner(System.in);
			System.out.println("Combien de SPAM dans la base d apprentissage ? ");
			M_SPAM = sc.nextInt();
			System.out.println("Combien de HAM dans la base d apprentissage ? ");
			M_HAM = sc.nextInt();
			sc.close();
		}
		
		FiltreAntiSpam antiSpam = new FiltreAntiSpam(M_SPAM,M_HAM,"dictionnaire1000en");
		antiSpam.apprentissage(M_HAM, "ham");
		antiSpam.apprentissage(M_SPAM, "spam");
		antiSpam.test();

	}

}
