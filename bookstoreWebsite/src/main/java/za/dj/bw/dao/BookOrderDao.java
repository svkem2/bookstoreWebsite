/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package za.dj.bw.dao;

import java.io.Serializable;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import za.dj.bw.dao.exceptions.NonexistentEntityException;
import za.dj.bw.entity.BookOrder;
import za.dj.bw.entity.Customer;

/**
 *
 * @author svkem2
 */
public class BookOrderDao implements Serializable {

    public BookOrderDao(EntityManagerFactory emf) {
        this.emf = emf;
    }
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(BookOrder bookOrder) {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Customer customerId = bookOrder.getCustomerId();
            if (customerId != null) {
                customerId = em.getReference(customerId.getClass(), customerId.getCustomerId());
                bookOrder.setCustomerId(customerId);
            }
            em.persist(bookOrder);
            if (customerId != null) {
                customerId.getBookOrderCollection().add(bookOrder);
                customerId = em.merge(customerId);
            }
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(BookOrder bookOrder) throws NonexistentEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            BookOrder persistentBookOrder = em.find(BookOrder.class, bookOrder.getOrderId());
            Customer customerIdOld = persistentBookOrder.getCustomerId();
            Customer customerIdNew = bookOrder.getCustomerId();
            if (customerIdNew != null) {
                customerIdNew = em.getReference(customerIdNew.getClass(), customerIdNew.getCustomerId());
                bookOrder.setCustomerId(customerIdNew);
            }
            bookOrder = em.merge(bookOrder);
            if (customerIdOld != null && !customerIdOld.equals(customerIdNew)) {
                customerIdOld.getBookOrderCollection().remove(bookOrder);
                customerIdOld = em.merge(customerIdOld);
            }
            if (customerIdNew != null && !customerIdNew.equals(customerIdOld)) {
                customerIdNew.getBookOrderCollection().add(bookOrder);
                customerIdNew = em.merge(customerIdNew);
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                Integer id = bookOrder.getOrderId();
                if (findBookOrder(id) == null) {
                    throw new NonexistentEntityException("The bookOrder with id " + id + " no longer exists.");
                }
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void destroy(Integer id) throws NonexistentEntityException {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            BookOrder bookOrder;
            try {
                bookOrder = em.getReference(BookOrder.class, id);
                bookOrder.getOrderId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The bookOrder with id " + id + " no longer exists.", enfe);
            }
            Customer customerId = bookOrder.getCustomerId();
            if (customerId != null) {
                customerId.getBookOrderCollection().remove(bookOrder);
                customerId = em.merge(customerId);
            }
            em.remove(bookOrder);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<BookOrder> findBookOrderEntities() {
        return findBookOrderEntities(true, -1, -1);
    }

    public List<BookOrder> findBookOrderEntities(int maxResults, int firstResult) {
        return findBookOrderEntities(false, maxResults, firstResult);
    }

    private List<BookOrder> findBookOrderEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(BookOrder.class));
            Query q = em.createQuery(cq);
            if (!all) {
                q.setMaxResults(maxResults);
                q.setFirstResult(firstResult);
            }
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    public BookOrder findBookOrder(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(BookOrder.class, id);
        } finally {
            em.close();
        }
    }

    public int getBookOrderCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<BookOrder> rt = cq.from(BookOrder.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
