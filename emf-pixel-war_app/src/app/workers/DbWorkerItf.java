package app.workers;

import app.beans.Pixel;
import app.exceptions.MyDBException;
import java.util.List;

public interface DbWorkerItf {
  void connecter(String pu)  throws MyDBException;
  void deconnecter()  throws MyDBException;

  List<Pixel> lirePixels() throws MyDBException;
  
  // opérations CRUD
  void creer( Pixel p ) throws MyDBException;
  Pixel lire( int PK ) throws MyDBException;
  void modifier( Pixel p ) throws MyDBException;
  void effacer( Pixel p) throws MyDBException;
  long compter() throws MyDBException;
}
