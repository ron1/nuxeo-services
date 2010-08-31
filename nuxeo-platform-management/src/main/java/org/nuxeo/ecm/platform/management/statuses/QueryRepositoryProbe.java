/*
 * (C) Copyright 2006-2008 Nuxeo SAS (http://nuxeo.com/) and contributors.
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
 *     mcedica
 */
package org.nuxeo.ecm.platform.management.statuses;

import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.DocumentRef;
import org.nuxeo.ecm.core.api.UnrestrictedSessionRunner;
import org.nuxeo.ecm.core.api.repository.RepositoryManager;
import org.nuxeo.ecm.core.api.security.ACE;
import org.nuxeo.ecm.core.api.security.ACL;
import org.nuxeo.ecm.core.api.security.ACP;
import org.nuxeo.ecm.core.api.security.SecurityConstants;
import org.nuxeo.ecm.core.management.statuses.Probe;
import org.nuxeo.ecm.core.management.statuses.ProbeStatus;
import org.nuxeo.runtime.api.Framework;

public class QueryRepositoryProbe implements Probe {

    protected static final String queryString = "SELECT * FROM Document WHERE ecm:path STARTSWITH '/' LIMIT 10";

    public static class Runner extends UnrestrictedSessionRunner {

        public Runner(String repositoryName) {
            super(repositoryName);
        }

        protected String info;

        @Override
        public void run() throws ClientException {
            String info = "Running " + queryString + ":";
            DocumentModelList list = session.query(queryString);
            for (DocumentModel documentModel : list) {
                info += " " + documentModel.getTitle();
            }
        }

    }

    public void init(Object service) {
        // TODO Auto-generated method stub
    }

    public ProbeStatus run() {
        RepositoryManager mgr = Framework.getLocalService(RepositoryManager.class);
        Runner runner = new Runner(mgr.getDefaultRepository().getName());
        try {
            runner.runUnrestricted();
        } catch (ClientException e) {
            return ProbeStatus.newError(e);
        }
        return ProbeStatus.newSuccess(runner.info);
    }

}