/*
 * Copyright 2015, Red Hat, Inc. and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.zanata.helper.quartz;

import java.io.File;

import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.helper.common.plugin.RepoExecutor;
import org.zanata.helper.common.plugin.TranslationServerExecutor;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class TransServerSyncJob extends SyncJob {
    private static final Logger log =
            LoggerFactory.getLogger(TransServerSyncJob.class);

    private static final int syncToServerTotalSteps = 4;

    @Override
    protected void doSync(RepoExecutor repoExecutor,
            TranslationServerExecutor transServerExecutor)
            throws JobExecutionException {
        if (repoExecutor == null || transServerExecutor == null) {
            log.info("No plugin in job. Skipping." + jobConfig.toString());
            return;
        }
        try {
            updateProgress(jobConfig.getId(), 1, syncToServerTotalSteps,
                    "Sync to server starts");
            File destDir = getDestDirectory(jobConfig.getId().toString());
            updateProgress(jobConfig.getId(),
                    2, syncToServerTotalSteps,
                    "Cloning repository to " + destDir);
            repoExecutor.cloneRepo(destDir);
            updateProgress(jobConfig.getId(),
                    3, syncToServerTotalSteps,
                    "Pushing files to server from " + destDir);
            transServerExecutor.pushToServer(destDir,
                    jobConfig.getSyncToServerConfig().getOption());
            updateProgress(jobConfig.getId(), 4, syncToServerTotalSteps,
                    "Sync to server completed");
        } catch (Exception e) {
            throw new JobExecutionException(e);
        }
    }

}