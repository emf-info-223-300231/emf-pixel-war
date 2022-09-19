package app.workers;

import app.beans.Pixel;
import app.exceptions.MyDBException;
import app.helpers.SystemLib;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.OptimisticLockException;
import javax.persistence.Persistence;
import javax.persistence.Query;
import javax.persistence.RollbackException;
import org.eclipse.persistence.config.HintValues;
import org.eclipse.persistence.config.QueryHints;

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

    @Override
    public List<Pixel> lirePixels() throws MyDBException {
        List<Pixel> listePixels = new ArrayList<>();
        try {
            Query query = em.createNamedQuery("Pixel.findAll");
            query.setHint(QueryHints.REFRESH, HintValues.TRUE);
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
    public void creer(Pixel p) throws MyDBException {
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
    public Pixel lire(int pk) throws MyDBException {
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

    @Override
    public void modifier(Pixel p) throws MyDBException {
        try {
            et.begin();
            em.merge(p);
            et.commit();
        } catch (OptimisticLockException ex) {
            et.rollback();
            throw new MyDBException(SystemLib.getFullMethodName() + ex.getMessage(), "\n concurrence de mise à jour");
        } catch (RollbackException ex) {
            if (et.isActive()) {
                et.rollback();
            }
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
