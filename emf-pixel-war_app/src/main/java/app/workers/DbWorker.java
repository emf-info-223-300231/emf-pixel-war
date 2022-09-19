package main.java.app.workers;

import main.java.app.exceptions.MyDBException;
import main.java.app.helpers.SystemLib;
import main.java.app.beans.Pixel;

import java.util.ArrayList;
import java.util.List;
import javax.persistence.*;

public class DbWorker implements DbWorkerItf {

  private EntityManagerFactory emf;
  private EntityManager em;
  private EntityTransaction et;

  /**
   * Constructeur du worker
   */
  public DbWorker() {
  }

  @Override
  public void connecter(String pu) throws MyDBException {
    try {
      emf = Persistence.createEntityManagerFactory(pu);
      em = emf.createEntityManager();
      et = em.getTransaction();
      if (!em.isOpen()) {
        throw new MyDBException(SystemLib.getFullMethodName(), "Erreur à l'ouverture de la db");
      }
    } catch (Exception ex) {
      throw new MyDBException(SystemLib.getFullMethodName(), ex.getMessage());
    }
  }

  @Override
  public void deconnecter() throws MyDBException {
    try {
      em.close();
      emf.close();
    } catch (Exception ex) {
      throw new MyDBException(SystemLib.getFullMethodName(), ex.getMessage());
    }
  }

  /**
   * Modifier par : Valentino
   * Modification : J'ai fait en sorte que la query n'es pas dans le cache.
   * @return list de pixels
   * @throws MyDBException
   */
  @Override
  public List<Pixel> lirePixels() throws MyDBException {
    List<Pixel> listePixels = new ArrayList<>();
    try {
      Query query = em.createQuery("SELECT p FROM Pixel p").setHint("javax.persistence.cache.storeMode", "REFRESH");
      listePixels = query.getResultList();
    } catch (Exception ex) {
      throw new MyDBException(SystemLib.getFullMethodName(), ex.getMessage());
    }
    return listePixels;
  }

  @Override
  public long compter() throws MyDBException {
    try {
      Query query = em.createQuery("SELECT count(p) FROM Pixel p");
      return (Long) query.getSingleResult();
    } catch (Exception ex) {
      throw new MyDBException(SystemLib.getFullMethodName(), ex.getMessage());
    }
  }
  
  @Override
  public void creer(Pixel p) throws MyDBException{
    try {
        
      et.begin();
      em.persist(p);
      et.commit();
    } catch (Exception ex) {
      et.rollback();
      throw new MyDBException(SystemLib.getFullMethodName(), ex.getMessage());
    }
  }
  
  @Override
  public Pixel lire(int pk) throws MyDBException{
    Pixel p = null;
    try {
      p = em.find(Pixel.class, pk);
      if (p != null) {
        em.refresh(p);
      }
    } catch (Exception ex) {
      throw new MyDBException(SystemLib.getFullMethodName(), ex.getMessage());
    }
    return p;
  }

  /**
   * Modifier par : Valentino
   * Modification : Je supprime le et.rollback car quand le merge ne fonctionne pas cela annule la transaction seul.
   * Ce n'est pas une OptimisticLockException qui est lever met une autre avec plusieurs je mets donc Exception à la place.
   * @param p
   * @throws MyDBException
   */
  @Override
  public void modifier(Pixel p) throws MyDBException {
    try {
      et.begin();
      em.find(Pixel.class, p.getPkPixel(), LockModeType.PESSIMISTIC_WRITE);
      em.merge(p);
      et.commit();
    } catch (Exception ex) {
      //et.rollback();
      throw new MyDBException(SystemLib.getFullMethodName() + ex.getMessage(), "\n concurrence de mise à jour");
    }
  }
  
  @Override
  public void effacer(Pixel p) throws MyDBException {
    Pixel p2 = lire(p.getPkPixel());
    try {
      et.begin();
      em.remove(p2);
      et.commit();
    } catch (OptimisticLockException ex) {
      et.rollback();
      throw new MyDBException(SystemLib.getFullMethodName(), ex.getMessage() + "\n concurrence de mise à jour");
    } catch (Exception ex) {
      et.rollback();
      throw new MyDBException(SystemLib.getFullMethodName(), ex.getMessage());
    }
  }

  

  

}
