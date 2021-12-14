/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package za.dj.bw.dao;

import java.io.Serializable;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import za.dj.bw.entity.Review;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import za.dj.bw.dao.exceptions.IllegalOrphanException;
import za.dj.bw.dao.exceptions.NonexistentEntityException;
import za.dj.bw.entity.BookOrder;
import za.dj.bw.entity.Customer;

/**
 *
 * @author svkem2
 */
public class CustomerDao implements Serializable {

    public CustomerDao(EntityManagerFactory emf) {
        this.emf = emf;
    }
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(Customer customer) {
        if (customer.getReviewCollection() == null) {
            customer.setReviewCollection(new ArrayList<Review>());
        }
        if (customer.getBookOrderCollection() == null) {
            customer.setBookOrderCollection(new ArrayList<BookOrder>());
        }
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Collection<Review> attachedReviewCollection = new ArrayList<Review>();
            for (Review reviewCollectionReviewToAttach : customer.getReviewCollection()) {
                reviewCollectionReviewToAttach = em.getReference(reviewCollectionReviewToAttach.getClass(), reviewCollectionReviewToAttach.getReviewId());
                attachedReviewCollection.add(reviewCollectionReviewToAttach);
            }
            customer.setReviewCollection(attachedReviewCollection);
            Collection<BookOrder> attachedBookOrderCollection = new ArrayList<BookOrder>();
            for (BookOrder bookOrderCollectionBookOrderToAttach : customer.getBookOrderCollection()) {
                bookOrderCollectionBookOrderToAttach = em.getReference(bookOrderCollectionBookOrderToAttach.getClass(), bookOrderCollectionBookOrderToAttach.getOrderId());
                attachedBookOrderCollection.add(bookOrderCollectionBookOrderToAttach);
            }
            customer.setBookOrderCollection(attachedBookOrderCollection);
            em.persist(customer);
            for (Review reviewCollectionReview : customer.getReviewCollection()) {
                Customer oldCustomerIdOfReviewCollectionReview = reviewCollectionReview.getCustomerId();
                reviewCollectionReview.setCustomerId(customer);
                reviewCollectionReview = em.merge(reviewCollectionReview);
                if (oldCustomerIdOfReviewCollectionReview != null) {
                    oldCustomerIdOfReviewCollectionReview.getReviewCollection().remove(reviewCollectionReview);
                    oldCustomerIdOfReviewCollectionReview = em.merge(oldCustomerIdOfReviewCollectionReview);
                }
            }
            for (BookOrder bookOrderCollectionBookOrder : customer.getBookOrderCollection()) {
                Customer oldCustomerIdOfBookOrderCollectionBookOrder = bookOrderCollectionBookOrder.getCustomerId();
                bookOrderCollectionBookOrder.setCustomerId(customer);
                bookOrderCollectionBookOrder = em.merge(bookOrderCollectionBookOrder);
                if (oldCustomerIdOfBookOrderCollectionBookOrder != null) {
                    oldCustomerIdOfBookOrderCollectionBookOrder.getBookOrderCollection().remove(bookOrderCollectionBookOrder);
                    oldCustomerIdOfBookOrderCollectionBookOrder = em.merge(oldCustomerIdOfBookOrderCollectionBookOrder);
                }
            }
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(Customer customer) throws IllegalOrphanException, NonexistentEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Customer persistentCustomer = em.find(Customer.class, customer.getCustomerId());
            Collection<Review> reviewCollectionOld = persistentCustomer.getReviewCollection();
            Collection<Review> reviewCollectionNew = customer.getReviewCollection();
            Collection<BookOrder> bookOrderCollectionOld = persistentCustomer.getBookOrderCollection();
            Collection<BookOrder> bookOrderCollectionNew = customer.getBookOrderCollection();
            List<String> illegalOrphanMessages = null;
            for (Review reviewCollectionOldReview : reviewCollectionOld) {
                if (!reviewCollectionNew.contains(reviewCollectionOldReview)) {
                    if (illegalOrphanMessages == null) {
                        illegalOrphanMessages = new ArrayList<String>();
                    }
                    illegalOrphanMessages.add("You must retain Review " + reviewCollectionOldReview + " since its customerId field is not nullable.");
                }
            }
            for (BookOrder bookOrderCollectionOldBookOrder : bookOrderCollectionOld) {
                if (!bookOrderCollectionNew.contains(bookOrderCollectionOldBookOrder)) {
                    if (illegalOrphanMessages == null) {
                        illegalOrphanMessages = new ArrayList<String>();
                    }
                    illegalOrphanMessages.add("You must retain BookOrder " + bookOrderCollectionOldBookOrder + " since its customerId field is not nullable.");
                }
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            Collection<Review> attachedReviewCollectionNew = new ArrayList<Review>();
            for (Review reviewCollectionNewReviewToAttach : reviewCollectionNew) {
                reviewCollectionNewReviewToAttach = em.getReference(reviewCollectionNewReviewToAttach.getClass(), reviewCollectionNewReviewToAttach.getReviewId());
                attachedReviewCollectionNew.add(reviewCollectionNewReviewToAttach);
            }
            reviewCollectionNew = attachedReviewCollectionNew;
            customer.setReviewCollection(reviewCollectionNew);
            Collection<BookOrder> attachedBookOrderCollectionNew = new ArrayList<BookOrder>();
            for (BookOrder bookOrderCollectionNewBookOrderToAttach : bookOrderCollectionNew) {
                bookOrderCollectionNewBookOrderToAttach = em.getReference(bookOrderCollectionNewBookOrderToAttach.getClass(), bookOrderCollectionNewBookOrderToAttach.getOrderId());
                attachedBookOrderCollectionNew.add(bookOrderCollectionNewBookOrderToAttach);
            }
            bookOrderCollectionNew = attachedBookOrderCollectionNew;
            customer.setBookOrderCollection(bookOrderCollectionNew);
            customer = em.merge(customer);
            for (Review reviewCollectionNewReview : reviewCollectionNew) {
                if (!reviewCollectionOld.contains(reviewCollectionNewReview)) {
                    Customer oldCustomerIdOfReviewCollectionNewReview = reviewCollectionNewReview.getCustomerId();
                    reviewCollectionNewReview.setCustomerId(customer);
                    reviewCollectionNewReview = em.merge(reviewCollectionNewReview);
                    if (oldCustomerIdOfReviewCollectionNewReview != null && !oldCustomerIdOfReviewCollectionNewReview.equals(customer)) {
                        oldCustomerIdOfReviewCollectionNewReview.getReviewCollection().remove(reviewCollectionNewReview);
                        oldCustomerIdOfReviewCollectionNewReview = em.merge(oldCustomerIdOfReviewCollectionNewReview);
                    }
                }
            }
            for (BookOrder bookOrderCollectionNewBookOrder : bookOrderCollectionNew) {
                if (!bookOrderCollectionOld.contains(bookOrderCollectionNewBookOrder)) {
                    Customer oldCustomerIdOfBookOrderCollectionNewBookOrder = bookOrderCollectionNewBookOrder.getCustomerId();
                    bookOrderCollectionNewBookOrder.setCustomerId(customer);
                    bookOrderCollectionNewBookOrder = em.merge(bookOrderCollectionNewBookOrder);
                    if (oldCustomerIdOfBookOrderCollectionNewBookOrder != null && !oldCustomerIdOfBookOrderCollectionNewBookOrder.equals(customer)) {
                        oldCustomerIdOfBookOrderCollectionNewBookOrder.getBookOrderCollection().remove(bookOrderCollectionNewBookOrder);
                        oldCustomerIdOfBookOrderCollectionNewBookOrder = em.merge(oldCustomerIdOfBookOrderCollectionNewBookOrder);
                    }
                }
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                Integer id = customer.getCustomerId();
                if (findCustomer(id) == null) {
                    throw new NonexistentEntityException("The customer with id " + id + " no longer exists.");
                }
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void destroy(Integer id) throws IllegalOrphanException, NonexistentEntityException {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Customer customer;
            try {
                customer = em.getReference(Customer.class, id);
                customer.getCustomerId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The customer with id " + id + " no longer exists.", enfe);
            }
            List<String> illegalOrphanMessages = null;
            Collection<Review> reviewCollectionOrphanCheck = customer.getReviewCollection();
            for (Review reviewCollectionOrphanCheckReview : reviewCollectionOrphanCheck) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("This Customer (" + customer + ") cannot be destroyed since the Review " + reviewCollectionOrphanCheckReview + " in its reviewCollection field has a non-nullable customerId field.");
            }
            Collection<BookOrder> bookOrderCollectionOrphanCheck = customer.getBookOrderCollection();
            for (BookOrder bookOrderCollectionOrphanCheckBookOrder : bookOrderCollectionOrphanCheck) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("This Customer (" + customer + ") cannot be destroyed since the BookOrder " + bookOrderCollectionOrphanCheckBookOrder + " in its bookOrderCollection field has a non-nullable customerId field.");
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            em.remove(customer);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<Customer> findCustomerEntities() {
        return findCustomerEntities(true, -1, -1);
    }

    public List<Customer> findCustomerEntities(int maxResults, int firstResult) {
        return findCustomerEntities(false, maxResults, firstResult);
    }

    private List<Customer> findCustomerEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(Customer.class));
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

    public Customer findCustomer(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Customer.class, id);
        } finally {
            em.close();
        }
    }

    public int getCustomerCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<Customer> rt = cq.from(Customer.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
