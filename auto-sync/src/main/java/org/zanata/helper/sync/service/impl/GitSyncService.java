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
package org.zanata.helper.sync.service.impl;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import org.eclipse.jgit.api.AddCommand;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.CommitCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PushCommand;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.StatusCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.helper.sync.exception.RepoSyncException;
import org.zanata.helper.sync.service.Credentials;
import org.zanata.helper.sync.service.RepoSyncService;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class GitSyncService implements RepoSyncService<String> {
    private static final Logger log =
            LoggerFactory.getLogger(GitSyncService.class);

    private final Credentials<String> credentials;

    // TODO use CDI injection
    public GitSyncService(Credentials<String> credentials) {
        this.credentials = credentials;
    }

    public Credentials<String> getCredentials() {
        return credentials;
    }

    public void cloneRepo(String repoUrl, File destPath)
            throws RepoSyncException {
        CloneCommand clone = Git.cloneRepository();
        clone.setBare(false);
        clone.setCloneAllBranches(false);
        clone.setDirectory(destPath).setURI(repoUrl);
        UsernamePasswordCredentialsProvider user =
                new UsernamePasswordCredentialsProvider(
                        getCredentials().getUsername(),
                        getCredentials().getSecret());
        clone.setCredentialsProvider(user);
        try {
            clone.call();
        } catch (GitAPIException e) {
            throw new RepoSyncException(e);
        }
    }

    @Override
    public void syncTranslationToRepo(String repoUrl, File baseDir)
            throws RepoSyncException {
        try {
            Git git = Git.open(baseDir);
            StatusCommand statusCommand = git.status();
            Status status = statusCommand.call();
            Set<String> uncommittedChanges = status.getUncommittedChanges();
            if (!uncommittedChanges.isEmpty()) {
                log.info("uncommitted files in git repo: {}", uncommittedChanges);
                AddCommand addCommand = git.add();
                addCommand.addFilepattern(".");
                addCommand.call();

                log.info("commit changed files");
                CommitCommand commitCommand = git.commit();
                commitCommand.setCommitter("Zanata Auto Repo Sync", "zanata-users@redhat.com");
                commitCommand.setMessage("Zanata Auto Repo Sync (pushing translations)");
                commitCommand.call();

                log.info("push to remote repo");
                PushCommand pushCommand = git.push();
                UsernamePasswordCredentialsProvider user =
                        new UsernamePasswordCredentialsProvider(
                                getCredentials().getUsername(),
                                getCredentials().getSecret());
                pushCommand.setCredentialsProvider(user);
                pushCommand.call();
            } else {
                log.info("nothing changed so nothing to do");
            }
        } catch (IOException e) {
            throw new RepoSyncException(
                    "failed opening " + baseDir + " as git repo", e);
        } catch (GitAPIException e) {
            throw new RepoSyncException("Failed committing translations into the repo", e);
        }
    }
}