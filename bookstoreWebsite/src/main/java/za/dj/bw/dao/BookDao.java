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
import za.dj.bw.entity.Category;
import za.dj.bw.entity.Review;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import za.dj.bw.dao.exceptions.IllegalOrphanException;
import za.dj.bw.dao.exceptions.NonexistentEntityException;
import za.dj.bw.entity.Book;

/**
 *
 * @author svkem2
 */
public class BookDao implements Serializable {

    public BookDao(EntityManagerFactory emf) {
        this.emf = emf;
    }
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(Book book) {
        if (book.getReviewCollection() == null) {
            book.setReviewCollection(new ArrayList<Review>());
        }
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Category categoryId = book.getCategoryId();
            if (categoryId != null) {
                categoryId = em.getReference(categoryId.getClass(), categoryId.getCategoryId());
                book.setCategoryId(categoryId);
            }
            Collection<Review> attachedReviewCollection = new ArrayList<Review>();
            for (Review reviewCollectionReviewToAttach : book.getReviewCollection()) {
                reviewCollectionReviewToAttach = em.getReference(reviewCollectionReviewToAttach.getClass(), reviewCollectionReviewToAttach.getReviewId());
                attachedReviewCollection.add(reviewCollectionReviewToAttach);
            }
            book.setReviewCollection(attachedReviewCollection);
            em.persist(book);
            if (categoryId != null) {
                categoryId.getBookCollection().add(book);
                categoryId = em.merge(categoryId);
            }
            for (Review reviewCollectionReview : book.getReviewCollection()) {
                Book oldBookIdOfReviewCollectionReview = reviewCollectionReview.getBookId();
                reviewCollectionReview.setBookId(book);
                reviewCollectionReview = em.merge(reviewCollectionReview);
                if (oldBookIdOfReviewCollectionReview != null) {
                    oldBookIdOfReviewCollectionReview.getReviewCollection().remove(reviewCollectionReview);
                    oldBookIdOfReviewCollectionReview = em.merge(oldBookIdOfReviewCollectionReview);
                }
            }
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(Book book) throws IllegalOrphanException, NonexistentEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Book persistentBook = em.find(Book.class, book.getBookId());
            Category categoryIdOld = persistentBook.getCategoryId();
            Category categoryIdNew = book.getCategoryId();
            Collection<Review> reviewCollectionOld = persistentBook.getReviewCollection();
            Collection<Review> reviewCollectionNew = book.getReviewCollection();
            List<String> illegalOrphanMessages = null;
            for (Review reviewCollectionOldReview : reviewCollectionOld) {
                if (!reviewCollectionNew.contains(reviewCollectionOldReview)) {
                    if (illegalOrphanMessages == null) {
                        illegalOrphanMessages = new ArrayList<String>();
                    }
                    illegalOrphanMessages.add("You must retain Review " + reviewCollectionOldReview + " since its bookId field is not nullable.");
                }
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            if (categoryIdNew != null) {
                categoryIdNew = em.getReference(categoryIdNew.getClass(), categoryIdNew.getCategoryId());
                book.setCategoryId(categoryIdNew);
            }
            Collection<Review> attachedReviewCollectionNew = new ArrayList<Review>();
            for (Review reviewCollectionNewReviewToAttach : reviewCollectionNew) {
                reviewCollectionNewReviewToAttach = em.getReference(reviewCollectionNewReviewToAttach.getClass(), reviewCollectionNewReviewToAttach.getReviewId());
                attachedReviewCollectionNew.add(reviewCollectionNewReviewToAttach);
            }
            reviewCollectionNew = attachedReviewCollectionNew;
            book.setReviewCollection(reviewCollectionNew);
            book = em.merge(book);
            if (categoryIdOld != null && !categoryIdOld.equals(categoryIdNew)) {
                categoryIdOld.getBookCollection().remove(book);
                categoryIdOld = em.merge(categoryIdOld);
            }
            if (categoryIdNew != null && !categoryIdNew.equals(categoryIdOld)) {
                categoryIdNew.getBookCollection().add(book);
                categoryIdNew = em.merge(categoryIdNew);
            }
            for (Review reviewCollectionNewReview : reviewCollectionNew) {
                if (!reviewCollectionOld.contains(reviewCollectionNewReview)) {
                    Book oldBookIdOfReviewCollectionNewReview = reviewCollectionNewReview.getBookId();
                    reviewCollectionNewReview.setBookId(book);
                    reviewCollectionNewReview = em.merge(reviewCollectionNewReview);
                    if (oldBookIdOfReviewCollectionNewReview != null && !oldBookIdOfReviewCollectionNewReview.equals(book)) {
                        oldBookIdOfReviewCollectionNewReview.getReviewCollection().remove(reviewCollectionNewReview);
                        oldBookIdOfReviewCollectionNewReview = em.merge(oldBookIdOfReviewCollectionNewReview);
                    }
                }
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                Integer id = book.getBookId();
                if (findBook(id) == null) {
                    throw new NonexistentEntityException("The book with id " + id + " no longer exists.");
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
            Book book;
            try {
                book = em.getReference(Book.class, id);
                book.getBookId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The book with id " + id + " no longer exists.", enfe);
            }
            List<String> illegalOrphanMessages = null;
            Collection<Review> reviewCollectionOrphanCheck = book.getReviewCollection();
            for (Review reviewCollectionOrphanCheckReview : reviewCollectionOrphanCheck) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("This Book (" + book + ") cannot be destroyed since the Review " + reviewCollectionOrphanCheckReview + " in its reviewCollection field has a non-nullable bookId field.");
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            Category categoryId = book.getCategoryId();
            if (categoryId != null) {
                categoryId.getBookCollection().remove(book);
                categoryId = em.merge(categoryId);
            }
            em.remove(book);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<Book> findBookEntities() {
        return findBookEntities(true, -1, -1);
    }

    public List<Book> findBookEntities(int maxResults, int firstResult) {
        return findBookEntities(false, maxResults, firstResult);
    }

    private List<Book> findBookEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(Book.class));
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

    public Book findBook(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Book.class, id);
        } finally {
            em.close();
        }
    }

    public int getBookCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<Book> rt = cq.from(Book.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
