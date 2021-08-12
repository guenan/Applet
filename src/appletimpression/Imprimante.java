package appletimpression;



import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.print.*;
import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.print.attribute.*;
import javax.swing.*;


public class Imprimante extends JApplet {
   

   public static int numberOfPages = 0;
   public int numberOfTickets =0;
   public int count=0;
   public int num_colonne=0;
   
   public int hauteur_ticket=140;
   public int largeur_ticket=90;
   
   public EnsembleLigne ens_ligne=new EnsembleLigne();
   public EnsembleLigne ens_ligne_copie=new EnsembleLigne();
   public Resultat resultat=new Resultat();
      
      
   
   public EnsembleLigne list_ligne=new EnsembleLigne();
   
    public ArrayList< EnsembleLigne > double_list=new ArrayList< EnsembleLigne>();
   public ArrayList< EnsembleLigne > double_list_copie=new ArrayList< EnsembleLigne >();
   
   
   public int grand_total=0;
   public Utilitaire tableau[];
   public int indice_precedent=-1;
   
   public int taille_sous_ens=0;
   public int valeur_diff_zero=0;   
   
  
   
   
   
    
    
    public ArrayList<JLabel> liste_label=new ArrayList<JLabel>();
    public ArrayList<JTextField> liste_testfield=new ArrayList<JTextField>();
    public ArrayList<String> liste_immatriculation=null;
    
    
    public JLabel label_message=new JLabel("deroulement du processus");
    public JLabel label_dimenssion=new JLabel("entrer les dimenssions de votre ticket ");
    public JLabel label_largeur=new JLabel("largeur");
    public JLabel label_hauteur=new JLabel("hauteur");
    
    public JTextField tf_hauteur=new JTextField("90");
    public JTextField tf_largeur=new JTextField("140");
    
    public JPanel panneau_principal=new JPanel();
    public JPanel panneau_message=new JPanel();
    public JPanel panneau_left=new JPanel();
    public JPanel panneau_right=new JPanel();
    public JPanel panneau_north=new JPanel();
    int taille=0;
    
   
   
   
   
   
   private JScrollPane ascenceur = new JScrollPane(panneau_principal, 
                                 ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, 
                                 ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
 
   private JToolBar barre = new JToolBar();
   private Book pages = new Book();
   private PrintRequestAttributeSet attributs = new HashPrintRequestAttributeSet();
   private PrinterJob travail = PrinterJob.getPrinterJob();
   private PageFormat page_principale = travail.getPageFormat(attributs);   
   private Apercu boîte=null;
   
   
    public static void Cloner(ArrayList< EnsembleLigne > db1,ArrayList< EnsembleLigne > db2){
       db2.clear();
       
        for(int i=0;i<db1.size();i++){
            db2.add((EnsembleLigne) db1.get(i).clone());
        }
   }
   
   public EnsembleLigne get_all_value(){
	   EnsembleLigne ens=new EnsembleLigne();
       valeur_diff_zero=0;
       
       String chaine_largeur_ticket=tf_largeur.getText();
       String chaine_hauteur_ticket=tf_hauteur.getText();
       
       if(!chaine_largeur_ticket.isEmpty() && !chaine_hauteur_ticket.isEmpty()){
    	   largeur_ticket=Integer.parseInt(chaine_largeur_ticket);
    	   hauteur_ticket=Integer.parseInt(chaine_hauteur_ticket);
       }else{
    	   largeur_ticket=260;
    	   hauteur_ticket=120;
       }
       
       
      for(int i=0;i<taille;i++) {
          String chaine_quantite=liste_testfield.get(i).getText();
          
          if(chaine_quantite.isEmpty() || chaine_quantite == null || chaine_quantite.equals("")){
              chaine_quantite="0";
              
          }
          
          if(Integer.parseInt(chaine_quantite) != 0){
              valeur_diff_zero++;
              Ligne ligne=resultat.contient(list_ligne, liste_label.get(i).getText());
              ligne.setNbreTicket(Integer.parseInt(chaine_quantite)+1);
               ens.add(ligne);
          }
          
        
         
          
          
      }
      
      if(valeur_diff_zero != 0){
         
          float x1= (float)resultat.total_ticket(ens)/(float)valeur_diff_zero;
          float x2= (float)resultat.total_ticket(ens) / (float)nombreColonne(page_principale);
          
          int val1=Math.round(x1);
          int val2=Math.round(x2);
      taille_sous_ens=val1+val2;       
      
      }else taille_sous_ens=1;
      
      
      
       return ens;
   }
   
   public EnsembleLigne get_all_parameter(){
	   EnsembleLigne liste_ligne =new EnsembleLigne();
	   String chaine_taille=this.getParameter("taille");
	   int taille=Integer.parseInt(chaine_taille);
	   
	   for(int i=0;i<taille;i++){
		   String code_ligne=this.getParameter("par"+i+"l");
		   String depart=this.getParameter("par"+i+"d");
		   String arrivee=this.getParameter("par"+i+"a");
		   
		   liste_ligne.add(new Ligne(code_ligne,depart,arrivee,0));
	   }
	   
	   
	   
	   return liste_ligne;
   }
   
   public void initialiseur(){
       count=0;
       grand_total=0;
  
   
       
       EnsembleLigne  ens_bus1=get_all_value();
     
     
      
      
       
       Algorithme algo;
      
       algo = new Algorithme(ens_bus1,nombreColonne(page_principale),taille_sous_ens);
       
    
       
       if(ens_bus1.size() !=0){
       
       algo.partition();
       
     
        
        // le nouveau
        
        double_list.clear();
        double_list_copie.clear();
        
        double_list=new ArrayList< EnsembleLigne>();
        double_list_copie=new ArrayList< EnsembleLigne>();
        
        double_list=algo.getResultat().getLast_ensemble();
       
        
        Imprimante.Cloner(double_list, double_list_copie);
        
        num_colonne= resultat.taille(double_list);
        
       
        
        numberOfTickets=resultat.total_ticket(double_list);
        
        
        
   
        
         if(numberOfTickets<0){
            numberOfTickets=0;
        }
       
       numberOfPages=(resultat.max_ensemble(double_list)/nombreLigne(page_principale));
       
       if((resultat.max_ensemble(double_list) % nombreLigne(page_principale)) != 0)
		{
    	   numberOfPages++;
		}
       
      
        tableau=new Utilitaire[numberOfPages+1];
          
          for(int i=0;i<tableau.length;i++){
              tableau[i]=new Utilitaire();
          }
          
      
               // initialisation de la memoire de comptage du premier element du tableau d'utilitaire
          
          for(int i=0;i<num_colonne;i++){
              
              if(double_list.get(i).size() != 0){
                  
              tableau[0].tab_current_start[i].num_imprime=1;
               tableau[0].tab_current_start[i].getLigne().setCode_ligne(resultat.min_bus(double_list.get(i)).getCode_ligne());
               tableau[0].tab_current_start[i].getLigne().setPoint_depart(resultat.min_bus(double_list.get(i)).getPoint_depart());
               tableau[0].tab_current_start[i].getLigne().setPoint_arrive(resultat.min_bus(double_list.get(i)).getPoint_arrive());
               tableau[0].tab_current_start[i].getLigne().setNbreTicket(resultat.min_bus(double_list.get(i)).getNbreTicket());
              }
          }
          
          
             pages=null; 
          pages=new Book();          
       
          pages.append(new Impression(), page_principale,numberOfPages);
          
       }
   }
   
   
    
   
  
   
    public int nombreColonne(PageFormat p)
		{
                   
                    int nberOfColumns=1;
                     
                     if(p.getOrientation() == PageFormat.LANDSCAPE){
			
                       nberOfColumns  =(int)p.getImageableWidth()/ largeur_ticket;
                     }
                        
                     if(p.getOrientation() == PageFormat.PORTRAIT){
                                                
                         nberOfColumns=(int)p.getImageableWidth()/largeur_ticket;
                          
                          
                           
                     }
                     
			
                        if(nberOfColumns==0) nberOfColumns++;
                       
			
                        
                      
                        
			
                        
                       
			return nberOfColumns;
		}
   
    
    public int nombreLigne(PageFormat p)
	{
               
    	 int nberOfligne=1;
         
         if(p.getOrientation() == PageFormat.LANDSCAPE){

        	 nberOfligne  =(int)p.getImageableHeight()/ hauteur_ticket;
         }
            
         if(p.getOrientation() == PageFormat.PORTRAIT){
                                    
        	 nberOfligne=(int)p.getImageableHeight()/hauteur_ticket;
              
              
               
         }
         

            if(nberOfligne==0) nberOfligne++;            

            
           
return nberOfligne;
	}
   
   
 
   public void init(){       
     
      
      list_ligne=get_all_parameter();
      
       
       taille=list_ligne.size();
       
     
       panneau_left.add(label_largeur);
       panneau_left.add(tf_largeur);
       panneau_left.add(label_hauteur);
       panneau_left.add(tf_hauteur);
       
       for(int i=0;i<taille;i++){
           liste_label.add(new JLabel(list_ligne.get(i).getCode_ligne()));  
           liste_testfield.add(new JTextField("0"));
       }
    
    panneau_principal.setLayout(new GridLayout(taille+3,2,2,10));

    panneau_principal.add(label_largeur);
    panneau_principal.add(tf_largeur);
    panneau_principal.add(label_hauteur);
    panneau_principal.add(tf_hauteur);
    
    panneau_principal.add(new JLabel("liste des ligne"));
    panneau_principal.add(new JLabel("nombre de ticket"));
    
    
    for(int j=0;j<taille;j++){
        
        panneau_principal.add(liste_label.get(j));
        panneau_principal.add(liste_testfield.get(j));
                
                
                }
    
    
    
    this.getContentPane().setLayout(new BorderLayout());
   
    
    

    panneau_message.add(label_message);
    this.getContentPane().add(ascenceur,BorderLayout.CENTER);
    this.getContentPane().add(panneau_message,BorderLayout.SOUTH);
    this.getContentPane().add(panneau_north,BorderLayout.NORTH);
    this.getContentPane().add(panneau_left,BorderLayout.WEST);
    this.getContentPane().add(panneau_right,BorderLayout.EAST);
    
      
      
      
      
      
      
       initialiseur();    
      
     
      add(barre, BorderLayout.NORTH);
      
      
      barre.add(new AbstractAction("Aperçu avant impression") {
         @Override
         public void actionPerformed(ActionEvent e) {
      
       initialiseur();       
       
       if(numberOfPages<1){
           label_message.setText("aucun aperçu possible");
           
       }else{
           label_message.setText("vous pouvez poursuivre");
             boîte=null;
                             
                boîte = new Apercu();
            
            
            boîte.setVisible(true);
            
            
       }
         }
      });
      
       barre.add(new AbstractAction("Mise en page") {
         @Override
         public void actionPerformed(ActionEvent e) {          
            
         page_principale=travail.pageDialog(attributs) ;
          
         initialiseur();          
       
         }
      });
      
      barre.add(new AbstractAction("Imprimer") {
         @Override
         public void actionPerformed(ActionEvent e) {
        	 initialiseur(); 
            try {
               travail.setPageable(pages);
               if (travail.printDialog(attributs)) {
                  travail.print(attributs);
               }
            } 
            catch (PrinterException ex) { JOptionPane.showMessageDialog(Imprimante.this, ex); }
         }
       });
      
      
      
      
 
    setVisible(true);
    
   }

   
   
 
   public class Impression implements Printable {
   
     
    
     
		//nombre du début et nombre des billets
		public int beginNumber = 1;
		
                
                
		//nombre de colonnes
		public int nberOfColumns = 1;
		//nombre de lignes par page
		public int nberOfLines = 1;
		//espace entre les colones et les lignes en pixels
		public int spaceColumns = 10, spaceRows = 10;
               
                boolean ca_suffit=false;
		//bolean pour éviter de calculer le nbre de pages à chaque fois
		public boolean firstTime = false;
		//lors de l'impression, définie la page actuel
		public int actualPage = -1;
      
       public Impression(){
         
     }
      
      
      @Override
      public int print(Graphics g, PageFormat page, int numero) 
      throws PrinterException {
         
        if (numero>=numberOfPages) return Printable.NO_SUCH_PAGE;
       
           Calendar date = Calendar.getInstance();
        String chaine_date=date.get(date.DAY_OF_MONTH)+"-"+(date.get(date.MONTH)+1)+"-"+date.get(date.YEAR);
          
         int  difference= indice_precedent - numero; 
           indice_precedent=numero;
          
          
          
                    this.GetNumberOfPages(page);
                    
                  
                     
                    Graphics2D surface = (Graphics2D) g;
                   
                    
                    
                  
                    int x=(int)page.getImageableX();
                    int y=(int)page.getImageableY();
                    int largeur =  largeur_ticket;
                    int hauteur = hauteur_ticket;
              
                   
                     int number=tableau[numero].start_number+1;
                      String mon_immatriculation=tableau[numero].getCurrent_immatriculation();
                      
               
                
                
                 Memo[] tab_count=new Memo[num_colonne];
                      
                      //initialisation des compteurs de chaque colonne avec les valeurs provenant des memoires utilitaires
                      for(int i=0;i<num_colonne;i++){
                          tab_count[i]=new Memo();
                          tab_count[i].getLigne().setCode_ligne(tableau[numero].tab_current_start[i].getLigne().getCode_ligne());
                          tab_count[i].getLigne().setPoint_depart(tableau[numero].tab_current_start[i].getLigne().getPoint_depart());
                          tab_count[i].getLigne().setPoint_arrive(tableau[numero].tab_current_start[i].getLigne().getPoint_arrive());
                          tab_count[i].getLigne().setNbreTicket(tableau[numero].tab_current_start[i].getLigne().getNbreTicket());
                          
                          
                           tab_count[i].num_imprime=tableau[numero].tab_current_start[i].getNum_imprime();
                      
                      }
                      
                      
                     
                     if((numero != 0) && (numero == numberOfPages-1)){
                         
                         grand_total=tableau[numero-1].value_grand_total;
                        
                    
                         
                         Imprimante.Cloner(tableau[numero-1].double_list_memoire ,  double_list);
                   
                   
                     }
                    
                     if((difference == 0)&&(numero !=0)){
                         
                 
                    Imprimante.Cloner(tableau[numero-1].double_list_memoire ,  double_list);
                   
                     }
                 
                   if((difference == 0)&&(numero ==0)){
                       
                       
                 
                  
                   Imprimante.Cloner(double_list_copie ,  double_list);
                 
                   
                     }
                   
                    
                  for(int i=0; i<this.nberOfLines ; i++)
			{
				for(int j=0; j<nberOfColumns; j++)
				{
                                  
					
                     if(grand_total == numberOfTickets ) {
                          
                                         break;
                                            
                                        }
                              
                     
                     if(this.est_vide(double_list)){
                                       
                                          break;
                                      }
                     
                                      
                                  
                     
                                      if(double_list.get(j).size()==0){
                                          surface.drawRect(x, y,largeur_ticket,hauteur_ticket);
                                          x += largeur_ticket + this.spaceColumns;
                                          continue;
                                      }else{
                                          
                                          Ligne bus=resultat.contient(double_list.get(j), tab_count[j].num_imprime);
                                          if( bus != null   ){
                                          
                                          double_list.get(j).remove(bus);
                                         
                                          tab_count[j].num_imprime=1;
                                          if(double_list.get(j).size() != 0){
                                          tab_count[j].getLigne().setCode_ligne(resultat.min_bus(double_list.get(j)).getCode_ligne());
                                          tab_count[j].getLigne().setPoint_depart(resultat.min_bus(double_list.get(j)).getPoint_depart());
                                          tab_count[j].getLigne().setPoint_arrive(resultat.min_bus(double_list.get(j)).getPoint_arrive());
                                          tab_count[j].getLigne().setNbreTicket(resultat.min_bus(double_list.get(j)).getNbreTicket());
                                          }
                                         
                                          if(double_list.get(j).size() == 0){
                                             surface.drawRect(x, y,largeur_ticket,hauteur_ticket);
                                              x += largeur_ticket + this.spaceColumns;
                                            
                                              continue;
                                                                                        
                                          }
                                      }
                                          
                                      }
                                      
                                      String chaine_numero = "{0}";
                                      
                                      String mon_numero = MessageFormat.format(chaine_numero,tab_count[j].getNum_imprime());
                                      surface.setColor(Color.ORANGE);
                                  surface.fillRect(x, y, largeur_ticket, hauteur_ticket);
                                       surface.setColor(Color.BLACK);
                                      surface.drawString(mon_numero, x+5,y+15);
                                      surface.drawString("depart :", x+5,y+40);
                                      surface.drawString(tab_count[j].getLigne().getPoint_depart(), x+55,y+40);
                                      surface.drawString("arrivee :", x+5,y+60);
                                      surface.drawString(tab_count[j].getLigne().getPoint_arrive(), x+55,y+60);
                                      surface.drawString("date :", x+5,y+80);    
                                      surface.drawString(chaine_date, x+45,y+80);
                                      
                                      
                                   
					
					 x += largeur_ticket + this.spaceColumns;
                                        
					 count=number;
                                        
					number++;
                                         tab_count[j].num_imprime++;
                                        
                                        if((difference != 0)|| (numero == numberOfPages-1)){
                                        grand_total++;
                                        }
                                        
                                        
				}                               
                             
                                 x = (int)page.getImageableX();					
				y += hauteur_ticket + this.spaceRows;
                                
                        }	
                  
                  
                     
                     
                     if(difference != 0){
                    
                    tableau[numero].value_grand_total=grand_total;
                     tableau[numero+1].start_number=count;
                     
                    tableau[numero+1].current_immatriculation=mon_immatriculation;
                     
                        Imprimante.Cloner(double_list, tableau[numero].double_list_memoire);
                     
                     for(int i=0;i<num_colonne;i++){
                         tableau[numero+1].tab_current_start[i].num_imprime=tab_count[i].num_imprime;
                         tableau[numero+1].tab_current_start[i].getLigne().setCode_ligne(tab_count[i].getLigne().getCode_ligne());
                         tableau[numero+1].tab_current_start[i].getLigne().setPoint_depart(tab_count[i].getLigne().getPoint_depart());
                         tableau[numero+1].tab_current_start[i].getLigne().setPoint_arrive(tab_count[i].getLigne().getPoint_arrive());
                         tableau[numero+1].tab_current_start[i].getLigne().setNbreTicket(tab_count[i].getLigne().getNbreTicket());
                         
                        
                     }
                     
                     }
                    
			
         return Printable.PAGE_EXISTS;
      }
      
      public int GetNumberOfPages(PageFormat p)
		{
                    
                    
                
			nberOfLines = (int) p.getImageableHeight() / hauteur_ticket;
                         nberOfColumns=(int)p.getImageableWidth()/largeur_ticket;
               
                         
                         
			if(nberOfLines==0) nberOfLines++;
                        if(nberOfColumns==0) nberOfColumns++;
			int nberOfTicketsPerPage = num_colonne * nberOfLines;
                        
                      
                        
			int result =  numberOfTickets /  nberOfTicketsPerPage;
                        
                        
			if((numberOfTickets%nberOfTicketsPerPage) != 0)
			{
				result++;
			}
                       
                      
			return result;
		}
      
      
        public boolean est_vide(ArrayList<EnsembleLigne> list){
          
          for(int i=0;i<list.size();i++){
              if(list.get(i).size() != 0) return false;
          }
          
          return true;
          
      }
      
   }  
   
   public class Utilitaire{
       
       
       public int start_number;
       public int value_grand_total;
       public String current_immatriculation;
       public ArrayList<Integer> liste_memoire;
       public EnsembleLigne ens_bus_memoire;
       public Memo tab_current_start[];
       
       public ArrayList< EnsembleLigne > double_list_memoire=new ArrayList< EnsembleLigne >();

        public Utilitaire() {
            
            tab_current_start=new Memo[num_colonne];
            
            for(int i=0;i<num_colonne;i++){
                tab_current_start[i]=new Memo();
            }
            current_immatriculation="";
            start_number=0;
            value_grand_total=0;
            liste_memoire=new ArrayList<Integer>();
            ens_bus_memoire=new EnsembleLigne();
            
        }

        public int getStart_number() {
            return start_number;
        }

        public void setStart_number(int start_number) {
            this.start_number = start_number;
        }

        public int getValue_grand_total() {
            return value_grand_total;
        }

        public void setValue_grand_total(int value_grand_total) {
            this.value_grand_total = value_grand_total;
        }

        public String getCurrent_immatriculation() {
            return current_immatriculation;
        }

        public void setCurrent_immatriculation(String current_immatriculation) {
            this.current_immatriculation = current_immatriculation;
        }
       
       
   }
   
   public class Memo{
    
    public Ligne ligne;
    public int num_imprime;
	public Memo(Ligne ligne, int num_imprime) {
		super();
		this.ligne = ligne;
		this.num_imprime = num_imprime;
	}
	public Memo() {
		super();
		ligne=new Ligne();
	}
	public Ligne getLigne() {
		return ligne;
	}
	public void setLigne(Ligne ligne) {
		this.ligne = ligne;
	}
	public int getNum_imprime() {
		return num_imprime;
	}
	public void setNum_imprime(int num_imprime) {
		this.num_imprime = num_imprime;
	}

     
    
    
    
}
   
  public class Apercu extends JDialog {
      private int pageCourante;
      private Panneau panneau = new Panneau();
      private JToolBar barre = new JToolBar();      
      private String libelle = "Page {0}/{1}";
      private JTextField numero = new JTextField(MessageFormat.format(libelle, pageCourante+1, pages.getNumberOfPages()));
       private JScrollPane ascenceur = new JScrollPane(panneau, 
                                 ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                                 ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
      
      
      
      public Apercu() {
    
        
          pageCourante = 0;
         
          
         add(ascenceur);
        
         
         barre.setFloatable(false);
         
         
         barre.add(new AbstractAction("Précédente") {
            @Override
            public void actionPerformed(ActionEvent e) {
               if (pageCourante>0) {
                  pageCourante--;  
                  if(pageCourante==0){
                      grand_total=0;
                 
                        
                        double_list.clear();
                         Imprimante.Cloner(double_list_copie ,double_list );
                         
                  }else{
                      
                      
                  grand_total=tableau[pageCourante-1].value_grand_total;
               
                   Imprimante.Cloner(tableau[pageCourante-1].double_list_memoire, double_list);
                  }
                  numero.setText(MessageFormat.format(libelle, pageCourante+1, pages.getNumberOfPages()));
                 
                  repaint();
                  
               }
            }
         });
         
         
         
         barre.add(new AbstractAction("Suivante") {
            @Override
            public void actionPerformed(ActionEvent e) {
               if (pageCourante<pages.getNumberOfPages()-1) {
                   grand_total=tableau[pageCourante].value_grand_total;
                 
                     Imprimante.Cloner(tableau[pageCourante].double_list_memoire, double_list);
                     
                  pageCourante++;            
                  
                  numero.setText(MessageFormat.format(libelle, pageCourante+1, pages.getNumberOfPages()));
               
                  repaint();
                  
               }
            }
         });
         
         barre.add(new AbstractAction("Plus") {
            @Override
            public void actionPerformed(ActionEvent e) {
              
            }
         });
         
         numero.setEditable(false);
         numero.setHorizontalAlignment(JTextField.CENTER);
         barre.add(numero);
         add(barre, BorderLayout.SOUTH);
         
        
        
         setSize(900,700);
      //  setSize(1370, 750);
          setVisible(true);
       setDefaultCloseOperation(DISPOSE_ON_CLOSE);
         
      
         
      }

      public class Panneau extends JComponent {
          
          
          
         @Override
         protected void paintComponent(Graphics g) {
            super.paintComponent(g);
        //   this.setPreferredSize(new Dimension(1000,800));
           
            Graphics2D surface = (Graphics2D) g;
        
           double px = page_principale.getWidth();
            double py = page_principale.getHeight();
            double sx = getWidth();
            double sy = getHeight();
            
            
            double xoff, yoff;
            double échelle;
            if (px/py < sx/sy) { // centrer horizontalement
               échelle = sy / py;
               xoff = 0.5*(sx-échelle*px);
               yoff = 0;
            }
            else { // centrer verticalement
               échelle = sx /px;
               xoff = 0;
               yoff = 0.5*(sy-échelle*py);
            }
          
            surface.translate(xoff, yoff);          
            surface.scale(échelle, échelle);
            
      
            
            Rectangle2D contour = new Rectangle2D.Double(0, 0, px,py );
            surface.setPaint(Color.white);
            surface.fill(contour);
            surface.setPaint(Color.BLACK);
            surface.draw(contour);
            
            
            
            Printable aperçu = pages.getPrintable(pageCourante);
            
            
            try {
            
               aperçu.print(surface, page_principale, pageCourante);
             
           
            } 
            catch (PrinterException ex) {
//               surface.draw(new Line2D.Double(0, 0, px, py));
 //              surface.draw(new Line2D.Double(0, px, 0, py));
            }
            
            
         }         
      }  
   }
  
  
 

  public class Algorithme {
	  
	    private ArrayList<Integer> ensemble_s;
	    private SousEnsemble sous_ensemble;
	    private Resultat resultat;
	    private ArrayList<Integer> list_imprimer_ticket;
	    
	    private int K;
	    private int tailleMax;
	    private EnsembleLigne ensembleLigne,list_imprimer_bus;
	    public Algorithme(EnsembleLigne ens_ligne, int K, int tailleMax){
	        this.K = K;
	        this.tailleMax = tailleMax;
	        ensembleLigne = ens_ligne;
	        
	        sous_ensemble = new SousEnsemble(K);
	        ensemble_s = new ArrayList<Integer>();
	        resultat = new Resultat();
	        for(int i = 0; i < ens_ligne.size(); i++){
	           this.ensemble_s.add(ens_ligne.get(i).getNbreTicket());
	         }
	    }
	    
	    public void constrution(){
	        Collections.sort(ensemble_s, Collections.reverseOrder());
	        int indice = 0;
	        while(!ensemble_s.isEmpty()){
	            indice = sous_ensemble.minEnsemble();
	            if(ensemble_s.get(0) <= tailleMax){
	                if((sous_ensemble.get(indice).getTaille() + ensemble_s.get(0)) <= tailleMax){
	                    Ensemble e = sous_ensemble.get(indice);
	                    e.addElement(ensemble_s.get(0));
	                    resultat.addFinal(ensemble_s.remove(0));
	                    
	                }else{
	                   resultat.addRestante(ensemble_s.remove(0));

	                }
	            }else{
	                    resultat.addRestante(ensemble_s.remove(0));
	                    
	            }
	        }
	              
	    }
	    public void ListeTicketImprimer(){
	        list_imprimer_ticket = new ArrayList<Integer>();
	        for(int i = 0; i<sous_ensemble.size(); i++){
	           Ensemble ens = sous_ensemble.get(i);
	           for(int j = 0; j < ens.size(); j++){
	               list_imprimer_ticket.add(ens.get(j));
	           }
	        }
	    }
	    public void partition(){
	        this.constrution();
	          
	      this.contructionBus();
	    }
	    
	    public void contructionBus(){
	        
	        SousEnsemble sous_ens = this.sous_ensemble;
	        EnsembleLigne bus_depart = this.ensembleLigne;
	        
	        while(!sous_ens.isEmpty()){
	            EnsembleLigne bus = new EnsembleLigne();
	            ArrayList<Integer> ensemble = sous_ens.remove(0);
	            for(int j = 0; j<ensemble.size(); j++){
	                for(int k = 0; k<bus_depart.size(); k++){
	                    if(bus_depart.get(k).getNbreTicket() == ensemble.get(j)){
	                        bus.add(bus_depart.remove(k));
	                        break;
	                    }
	                }
	                
	            }
	           this.resultat.addLastEnsemble(bus);             
	        }
	        
	    }
	    public void listeBusImprime(){
	        list_imprimer_bus = new EnsembleLigne();
	        for(int i=0; i<this.sous_ensemble.size();i++){
	            Ensemble ens = sous_ensemble.get(i);
	            for(int j=0; j<ens.size(); j++){
	                for(int k = 0; k<ensembleLigne.size(); k++){
	                    
	                    if(ensembleLigne.get(k).getNbreTicket()==ens.get(j)){
	                        list_imprimer_bus.addLigne(ensembleLigne.get(k));
	                        Ligne bu = ensembleLigne.remove(k);
	                    }
	                }
	            }
	        }
	        resultat.setBus_final(list_imprimer_bus);
	        resultat.setBus_restant(ensembleLigne);
	    }
	    
	    
	    public SousEnsemble getSous_ensemble() {
	        return sous_ensemble;
	    }

	    public ArrayList<Integer> getList_imprimer_ticket() {
	        return list_imprimer_ticket;
	    }

	    public EnsembleLigne getList_imprimer_bus() {
	        return list_imprimer_bus;
	    }

	    public Resultat getResultat() {
	        return resultat;
	    }

	    public ArrayList<Integer> getEnsemble_s() {
	        return ensemble_s;
	    }

	   
	    

	    
	    
	    
	    
	}



  public class Ligne {
	    private String code_ligne;
	    private String point_depart;
	    private String point_arrive;
	    private int nbreTicket;
	    public Ligne(){
	        
	    }

	    public Ligne(String code_ligne, String point_depart, String point_arrive,int nbreTicket) {
	        this.code_ligne = code_ligne;
	        this.point_depart = point_depart;
	        this.point_arrive = point_arrive;
	        this.nbreTicket = nbreTicket;
	    }

	 
	    
	    
	    public int getNbreTicket() {
			return nbreTicket;
		}

		public void setNbreTicket(int nbreTicket) {
			this.nbreTicket = nbreTicket;
		}

		public void setCode_ligne(String code_ligne) {
			this.code_ligne = code_ligne;
		}

		public String getCode_ligne() {
	        return code_ligne;
	    }

	    public String getPoint_arrive() {
	        return point_arrive;
	    }

	    public void setPoint_arrive(String point_arrive) {
	        this.point_arrive = point_arrive;
	    }

	    public String getPoint_depart() {
	        return point_depart;
	    }

	    public void setPoint_depart(String point_depart) {
	        this.point_depart = point_depart;
	    }
	    
	    
	    
	}

  
  public class Ensemble extends ArrayList<Integer> {
	    
	    private int taille;
	    
	    public Ensemble(){
	       taille = 0;
	    }
	    
	    public void addElement(int element){
	        this.add(element);
	        taille = taille + element;
	    }

	    
	    public int getTaille() {
	        return taille;
	    }
	    
	    
	}



  public class EnsembleLigne extends ArrayList<Ligne>{
	    
	    public void addLigne(Ligne ligne){
	        this.add(ligne);
	    }
	    
	    public void addLigne(String code_ligne, String point_depart, String point_arret,int nbreTicket){
	        Ligne ligne = new Ligne(code_ligne,point_depart,point_arret,nbreTicket);
	        this.addLigne(ligne);
	    }
	    
	}



  public class Resultat {
	    private ArrayList<Integer> liste_final;
	    private ArrayList<Integer> liste_restante;
	    private EnsembleLigne bus_final,bus_restant;
	    private ArrayList<EnsembleLigne> last_ensemble;
	    private SousEnsemble sous_ensemble;
	    
	    public Resultat(){
	        liste_final =  new ArrayList<Integer>();
	        liste_restante =  new ArrayList<Integer>();
	        bus_restant = new EnsembleLigne();
	        bus_final = new EnsembleLigne();
	        last_ensemble = new ArrayList<EnsembleLigne>();
	    }
	    public void addLastEnsemble(EnsembleLigne bus){
	        this.last_ensemble.add(bus);
	    }
	    public void addFinal(int element){
	        liste_final.add(element);
	    }
	    
	    public void addRestante(int element){
	        liste_restante.add(element);
	    }

	    public void setBus_final(EnsembleLigne bus_final) {
	        this.bus_final = bus_final;
	    }

	    public void setSous_ensemble(SousEnsemble sous_ensemble) {
	        this.sous_ensemble = sous_ensemble;
	    }

	    public void setBus_restant(EnsembleLigne bus_restant) {
	        this.bus_restant = bus_restant;
	    }

	    public EnsembleLigne getBus_final() {
	        return bus_final;
	    }

	    public SousEnsemble getSous_ensemble() {
	        return sous_ensemble;
	    }

	    public EnsembleLigne getBus_restant() {
	        return bus_restant;
	    }

	    public ArrayList<EnsembleLigne> getLast_ensemble() {
	        return last_ensemble;
	    }

	    
	    public ArrayList<Integer> getListe_final() {
	        return liste_final;
	    }

	    public ArrayList<Integer> getListe_restante() {
	        return liste_restante;
	    }
	    
	    public Ligne contient(EnsembleLigne ens_bus,Ligne bus){
	        
	        int i=0;
	        Ligne bus1=null;
	        while(i<ens_bus.size()){
	           
	          if(ens_bus.get(i).getCode_ligne().equals(bus.getCode_ligne())&& ens_bus.get(i).getPoint_depart().equals(bus.getPoint_depart())&& ens_bus.get(i).getPoint_arrive().equals(bus.getPoint_arrive())&& ens_bus.get(i).getNbreTicket()==bus.getNbreTicket()){
	             
	              return ens_bus.get(i);
	          }
	            i++;
	        }
	        
	        return bus1;
	    }
	    
 public Ligne contient(EnsembleLigne ens_bus,String code_ligne){
	        
	        int i=0;
	        Ligne bus1=null;
	        while(i<ens_bus.size()){
	           
	          if(ens_bus.get(i).getCode_ligne().equals(code_ligne)){
	             
	              return ens_bus.get(i);
	          }
	            i++;
	        }
	        
	        return bus1;
	    }
	    
	    public Ligne min_bus(EnsembleLigne ens_bus){
	        
	        int i=1;
	        
	        Ligne min_bus=ens_bus.get(0);
	        while(i<ens_bus.size()){
	           
	          if(ens_bus.get(i).getNbreTicket() < min_bus.getNbreTicket()){
	             
	               min_bus=ens_bus.get(i);
	          }
	            i++;
	        }
	        
	        return min_bus;
	    }
	    
	    
 public int max_ensemble(ArrayList<EnsembleLigne> ens_bus){
	        
	        int i=1;
	        
	        int max=this.total_ticket(ens_bus.get(0));
	        while(i<ens_bus.size()){
	           
	          if(this.total_ticket(ens_bus.get(i)) > max){
	             
	               max=this.total_ticket(ens_bus.get(i));
	          }
	            i++;
	        }
	        
	        return max;
	    }
	    
	    public Ligne contient(EnsembleLigne ens_bus,int nbreTicket){
	        
	        int i=0;
	        Ligne bus1=null;
	        while(i<ens_bus.size()){
	           
	          if(ens_bus.get(i).getNbreTicket()== nbreTicket){
	              return ens_bus.get(i);
	          }
	            i++;
	        }
	        
	        return bus1;
	    }
	    
	    public int total_ticket(EnsembleLigne ens_bus){
	        int i=0;
	       int nombre_ticket=0;
	        
	        while(i<ens_bus.size()){
	           nombre_ticket+=ens_bus.get(i).getNbreTicket();
	           i++;
	        }
	        
	        return (nombre_ticket-ens_bus.size());
	    }
	    
	    
	    public int total_ticket(ArrayList<EnsembleLigne> ens_bus){
	        int i=0;
	       int nombre_ticket=0;
	        
	        while(i<ens_bus.size()){
	            
	           
	         nombre_ticket+=(this.total_ticket(ens_bus.get(i)));
	          
	            
	         
	           i++;
	        }
	        
	        return nombre_ticket;
	    }
	    
	       public int taille(ArrayList<EnsembleLigne> ens_bus){
	           int i=0;
	          int taille=ens_bus.size();
	           
	           while(i<ens_bus.size()){
	               
	            if(ens_bus.get(i).size()==0)  {
	                taille--;
	            }
	                     
	              i++;
	           }
	           
	           return taille;
	       }
	    
	}

    
  public class SousEnsemble extends ArrayList<Ensemble>{
	    private int n;
	    public SousEnsemble(int K){
	        n = 0;
	        for(int i= 0; i < K; i++ ){
	            Ensemble ens= new Ensemble();
	            this.add(ens);
	        }
	    }

	    public int getN() {
	        return n;
	    }
	    
	   
	    
	    
	    public int minEnsemble(){
	            int indice = 0;
	            for(int i=1; i<this.size(); i++){
	                Ensemble e = this.get(indice);
	                Ensemble e1 = this.get(i);
	                if (e1.getTaille() < e.getTaille())
	                   indice = i;
	               }  
	       return indice;
	   }
	   
	    public int affiche(){
	        for(int i = 0; i<this.size(); i++){
	            System.out.println("sous ens "+(i+1)+" "+this.get(i).toString()+" taille = "+ this.get(i).getTaille());
	        }
	        return 0;
	    }

	    void addEns(Ensemble get, int indice, Integer get0) {
	        get.addElement(get0);
	        this.add(indice, get);
	        n++;
	       }
	}




  
   }
   
