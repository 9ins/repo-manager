package org.chaostocosmos.repo;

import org.eclipse.aether.AbstractRepositoryListener;
import org.eclipse.aether.RepositoryEvent;

/**
 * ConsoleRepoEventListener 
 */
public class ConsoleRepoEventListener extends AbstractRepositoryListener  {

    Logger logger = Logger.getInstance();

    @Override
    public void artifactInstalled(RepositoryEvent event) {
      logger.debug(String.format("artifact %s installed to file %s\n", event.getArtifact(), event.getFile()));
    }
   
    @Override
    public void artifactInstalling(RepositoryEvent event) {
      logger.debug(String.format("installing artifact %s to file %s\n", event.getArtifact(), event.getFile()));
    }
   
    @Override
    public void artifactResolved(RepositoryEvent event) {
      logger.debug(String.format("artifact %s resolved from repository %s\n", event.getArtifact(), event.getRepository()));
    }
   
    @Override
    public void artifactDownloading(RepositoryEvent event) {
      logger.debug(String.format("downloading artifact %s from repository %s\n", event.getArtifact(), event.getRepository()));
    }
   
    @Override
    public void artifactDownloaded(RepositoryEvent event) {
      logger.debug(String.format("downloaded artifact %s from repository %s\n", event.getArtifact(), event.getRepository()));
    }
   
    @Override
    public void artifactResolving(RepositoryEvent event) {
      logger.debug(String.format("resolving artifact %s\n", event.getArtifact()));
    }    
}
