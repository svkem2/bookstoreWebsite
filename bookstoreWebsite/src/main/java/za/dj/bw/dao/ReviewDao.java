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
import za.dj.bw.entity.Book;
import za.dj.bw.entity.Customer;
import za.dj.bw.entity.Review;

/**
 *
 * @author svkem2
 */
public class ReviewDao implements Serializable {

    public ReviewDao(EntityManagerFactory emf) {
        this.emf = emf;
    }
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(Review review) {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Book bookId = review.getBookId();
            if (bookId != null) {
                bookId = em.getReference(bookId.getClass(), bookId.getBookId());
                review.setBookId(bookId);
            }
            Customer customerId = review.getCustomerId();
            if (customerId != null) {
                customerId = em.getReference(customerId.getClass(), customerId.getCustomerId());
                review.setCustomerId(customerId);
            }
            em.persist(review);
            if (bookId != null) {
                bookId.getReviewCollection().add(review);
                bookId = em.merge(bookId);
            }
            if (customerId != null) {
                customerId.getReviewCollection().add(review);
                customerId = em.merge(customerId);
            }
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(Review review) throws NonexistentEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Review persistentReview = em.find(Review.class, review.getReviewId());
            Book bookIdOld = persistentReview.getBookId();
            Book bookIdNew = review.getBookId();
            Customer customerIdOld = persistentReview.getCustomerId();
            Customer customerIdNew = review.getCustomerId();
            if (bookIdNew != null) {
                bookIdNew = em.getReference(bookIdNew.getClass(), bookIdNew.getBookId());
                review.setBookId(bookIdNew);
            }
            if (customerIdNew != null) {
                customerIdNew = em.getReference(customerIdNew.getClass(), customerIdNew.getCustomerId());
                review.setCustomerId(customerIdNew);
            }
            review = em.merge(review);
            if (bookIdOld != null && !bookIdOld.equals(bookIdNew)) {
                bookIdOld.getReviewCollection().remove(review);
                bookIdOld = em.merge(bookIdOld);
            }
            if (bookIdNew != null && !bookIdNew.equals(bookIdOld)) {
                bookIdNew.getReviewCollection().add(review);
                bookIdNew = em.merge(bookIdNew);
            }
            if (customerIdOld != null && !customerIdOld.equals(customerIdNew)) {
                customerIdOld.getReviewCollection().remove(review);
                customerIdOld = em.merge(customerIdOld);
            }
            if (customerIdNew != null && !customerIdNew.equals(customerIdOld)) {
                customerIdNew.getReviewCollection().add(review);
                customerIdNew = em.merge(customerIdNew);
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                Integer id = review.getReviewId();
                if (findReview(id) == null) {
                    throw new NonexistentEntityException("The review with id " + id + " no longer exists.");
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
            Review review;
            try {
                review = em.getReference(Review.class, id);
                review.getReviewId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The review with id " + id + " no longer exists.", enfe);
            }
            Book bookId = review.getBookId();
            if (bookId != null) {
                bookId.getReviewCollection().remove(review);
                bookId = em.merge(bookId);
            }
            Customer customerId = review.getCustomerId();
            if (customerId != null) {
                customerId.getReviewCollection().remove(review);
                customerId = em.merge(customerId);
            }
            em.remove(review);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<Review> findReviewEntities() {
        return findReviewEntities(true, -1, -1);
    }

    public List<Review> findReviewEntities(int maxResults, int firstResult) {
        return findReviewEntities(false, maxResults, firstResult);
    }

    private List<Review> findReviewEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(Review.class));
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

    public Review findReview(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Review.class, id);
        } finally {
            em.close();
        }
    }

    public int getReviewCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<Review> rt = cq.from(Review.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
