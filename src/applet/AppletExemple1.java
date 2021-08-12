package applet;

import java.applet.*;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Label;
import javax.swing.JLabel;
public class AppletExemple1 extends Applet
{
    JLabel etiq = new JLabel("Démarrage ");
    JLabel tab_label[];
int nbrparam = 0 ;



    @Override
public void init( ) {
        String chaine=this.getParameter("taille");
        nbrparam=Integer.parseInt(chaine);
        tab_label=new JLabel[nbrparam];
       this.setBackground(Color.yellow);
         this.add(etiq);
          for(int i=0;i<nbrparam;i++){
          
           tab_label[i]=new JLabel(this.getParameter("par"+i));
           this.add(tab_label[i]);
       }


}
  
 public class Ligne{
	 private String code_ligne;
	 private String point_depart;
	 private String point_arrive;
	 
	public Ligne(String code_ligne, String point_depart, String point_arrive) {
		super();
		this.code_ligne = code_ligne;
		this.point_depart = point_depart;
		this.point_arrive = point_arrive;
	}

	public Ligne() {
		super();
	}

	public String getCode_ligne() {
		return code_ligne;
	}

	public void setCode_ligne(String code_ligne) {
		this.code_ligne = code_ligne;
	}

	public String getPoint_depart() {
		return point_depart;
	}

	public void setPoint_depart(String point_depart) {
		this.point_depart = point_depart;
	}

	public String getPoint_arrive() {
		return point_arrive;
	}

	public void setPoint_arrive(String point_arrive) {
		this.point_arrive = point_arrive;
	}
	
	
	 
	 
 }
}
