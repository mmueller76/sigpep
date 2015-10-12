package org.sigpep.persistence.dao.impl;

import org.sigpep.persistence.dao.SimpleQueryDaoFactory;
import org.sigpep.persistence.dao.SimpleQueryDao;
import org.sigpep.persistence.rdbms.DataSourceFactory;

import javax.sql.DataSource;

/**
 * Created by IntelliJ IDEA.<br/>
 * User: mmueller<br/>
 * Date: 24-Jun-2008<br/>
 * Time: 19:34:52<br/>
 */
public class SpringJdbcSimpleQueryDaoFactory extends SimpleQueryDaoFactory {

    public SpringJdbcSimpleQueryDaoFactory() {
    }

    public SimpleQueryDao createSimpleQueryDao(int taxonId) {

        DataSource dataSource = DataSourceFactory.getInstance().createDataSource(taxonId);
        return new SpringJdbcSimpleQueryDao(dataSource);

    }
}
