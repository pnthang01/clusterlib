/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.cluster.dao.derby;

import io.cluster.model.SuperviseModel;
import io.cluster.util.StringUtil;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.hibernate.query.criteria.internal.CriteriaQueryImpl;

/**
 *
 * @author thangpham
 */
public class SuperviseDAO {

    private static final Logger LOGGER = LogManager.getLogger(SuperviseDAO.class.getName());
    private HibernateSessionManager connManager = HibernateSessionManager.load();

    private static SuperviseDAO _instance;

    static {
        _instance = new SuperviseDAO();
    }

    public static SuperviseDAO load() {
        return _instance;
    }

    public List<SuperviseModel> loadAllSuperviseBean() {
        Session session = null;
        List<SuperviseModel> result = null;
        try {
            SessionFactory sf = connManager.getSessionFactory();
            session = sf.openSession();
            Query query = session.createQuery("from Supervise");
            result = query.list();
        } catch (RuntimeException e) {
            throw e;
        } finally {
            if (session != null) {
                session.close();
            }
        }
        return result;
    }

    public void insertSuperviseBean(SuperviseModel bean) {
        if (null == bean || StringUtil.isNullOrEmpty(bean.getProcessId())) {
            throw new IllegalArgumentException("Either supervise bean or its Id is null or empty, could not persist");
        }
        Session session = null;
        Transaction txn = null;
        try {
            SessionFactory sf = connManager.getSessionFactory();
            session = sf.openSession();
            txn = session.beginTransaction();
            session.persist(bean);
            txn.commit();
        } catch (RuntimeException e) {
            if (txn != null && txn.isActive()) {
                txn.rollback();
            }
            throw e;
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    public boolean updateSuperviseBean(SuperviseModel bean) {
        if (null == bean || StringUtil.isNullOrEmpty(bean.getProcessId())) {
            throw new IllegalArgumentException("Either supervise bean or its Id is null or empty, could not update");
        }
        Session session = null;
        Transaction txn = null;
        boolean succ = false;
        try {
            SessionFactory sf = connManager.getSessionFactory();
            session = sf.openSession();
            txn = session.beginTransaction();
            succ = session.merge(bean) != null;
            txn.commit();
        } catch (RuntimeException e) {
            if (txn != null && txn.isActive()) {
                txn.rollback();
            }
            succ = false;
            throw e;
        } finally {
            if (session != null) {
                session.close();
            }
        }
        return true;
    }

    public void deleteSuperviseBean(String processId) {
        if (StringUtil.isNullOrEmpty(processId)) {
            throw new IllegalArgumentException("This supervise bean does not exist, could not update");
        }
        Session session = null;
        Transaction txn = null;
        try {
            SessionFactory sf = connManager.getSessionFactory();
            session = sf.openSession();
            txn = session.beginTransaction();
            SuperviseModel deleteObj = new SuperviseModel().setProcessId(processId);
            session.delete(deleteObj);
            txn.commit();
        } catch (RuntimeException e) {
            if (txn != null && txn.isActive()) {
                txn.rollback();
            }
            throw e;
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }
}
