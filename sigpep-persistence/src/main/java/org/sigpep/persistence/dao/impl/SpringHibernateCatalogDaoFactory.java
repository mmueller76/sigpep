package org.sigpep.persistence.dao.impl;

import org.hibernate.SessionFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.sigpep.persistence.dao.CatalogDaoFactory;
import org.sigpep.persistence.dao.CatalogDao;
import org.sigpep.persistence.util.HibernateUtil;

/**
 * Created by IntelliJ IDEA.<br/>
 * User: mmueller<br/>
 * Date: 18-Jul-2008<br/>
 * Time: 10:28:12<br/>
 */
public class SpringHibernateCatalogDaoFactory extends CatalogDaoFactory {

    public SpringHibernateCatalogDaoFactory() {
    }

    public CatalogDao createCatalogDao() {

        SpringHibernateCatalogDao retVal = new SpringHibernateCatalogDao();

        SessionFactory sessionFactory = HibernateUtil.getCatalogSessionFactory();

        retVal.setSessionFactory(sessionFactory);

        return retVal;

    }

}
