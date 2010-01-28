/*
 * (C) Copyright 2006-2009 Nuxeo SA (http://nuxeo.com/) and contributors.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * (LGPL) version 2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * Contributors:
 *     Damien Metzler (Leroy Merlin, http://www.leroymerlin.fr/)
 */
package org.nuxeo.ecm.platform.test.guice;

import javax.naming.InitialContext;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hsqldb.jdbc.jdbcDataSource;
import org.nuxeo.common.jndi.NamingContextFactory;
import org.nuxeo.ecm.core.schema.SchemaManager;
import org.nuxeo.ecm.directory.api.DirectoryService;
import org.nuxeo.runtime.api.DataSourceHelper;
import org.nuxeo.runtime.api.Framework;
import org.nuxeo.runtime.test.runner.RuntimeHarness;

import com.google.inject.Inject;
import com.google.inject.Provider;

public class DirectoryServiceProvider implements Provider<DirectoryService> {

    private static final Log log = LogFactory.getLog(DirectoryServiceProvider.class);

    private final RuntimeHarness harness;

    private DataSource dataSource;

    private InitialContext initialCtx;

    @Inject
    public DirectoryServiceProvider(RuntimeHarness harness, SchemaManager sm)
            throws NamingException {
        assert sm != null;
        assert harness != null;
        this.harness = harness;

        // Sets up a datasource
        NamingContextFactory.setAsInitial();
        // Empty initial Context
        dataSource = createDataSource();
        initialCtx = new InitialContext();

        String prefix = DataSourceHelper.getDataSourceJNDIPrefix();
        try {
            initialCtx.lookup(prefix + "/nxsqldirectory");
        } catch (NameNotFoundException e) {
            initialCtx.bind(prefix + "/nxsqldirectory", dataSource);
        }


    }

    private static DataSource createDataSource() {
        jdbcDataSource datasource = new jdbcDataSource();
        datasource.setDatabase("jdbc:hsqldb:mem:directories");
        datasource.setUser("sa");
        datasource.setPassword("");
        return datasource;
    }

    public DirectoryService get() {
        try {
            harness.deployBundle("org.nuxeo.ecm.platform.api");

            // Deploy Directory Service
            harness.deployContrib("org.nuxeo.ecm.directory",
                    "OSGI-INF/DirectoryService.xml");
            harness.deployContrib("org.nuxeo.ecm.directory.sql",
                    "OSGI-INF/SQLDirectoryFactory.xml");

            harness.deployContrib("org.nuxeo.ecm.directory.ldap",
                    "OSGI-INF/LDAPDirectoryFactory.xml");

            // Deploy Shema Contrib
            harness.deployContrib("org.nuxeo.ecm.platform.test",
                    "test-usermanagerimpl/schemas-config.xml");
            harness.deployContrib("org.nuxeo.ecm.platform.test",
                    "test-usermanagerimpl/directory-config.xml");

            return Framework.getService(DirectoryService.class);
        } catch (Exception e) {
            log.error(e.toString(), e);
            return null;
        }
    }

}