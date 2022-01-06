package org.chaostocosmos.repo;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.maven.model.Model;
import org.apache.maven.model.building.DefaultModelBuilderFactory;
import org.apache.maven.model.building.DefaultModelBuildingRequest;
import org.apache.maven.model.building.ModelBuilder;
import org.apache.maven.model.building.ModelBuildingException;
import org.apache.maven.model.building.ModelBuildingResult;
import org.apache.maven.repository.internal.MavenRepositorySystemUtils;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.connector.basic.BasicRepositoryConnectorFactory;
import org.eclipse.aether.impl.DefaultServiceLocator;
import org.eclipse.aether.repository.LocalRepository;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.ArtifactRequest;
import org.eclipse.aether.resolution.ArtifactResolutionException;
import org.eclipse.aether.resolution.ArtifactResult;
import org.eclipse.aether.spi.connector.RepositoryConnectorFactory;
import org.eclipse.aether.spi.connector.transport.TransporterFactory;
import org.eclipse.aether.transport.file.FileTransporterFactory;
import org.eclipse.aether.transport.http.HttpTransporterFactory;

/**
 * Repository Manager Object
 * 
 * @author 9ins
 */
public class RepoManager {  

    List<Repo> repoList;
    Path TARGET_LOCAL_REPOSITORY;

    final RepositorySystem repositorySystem;
    final RepositorySystemSession repositorySystemSession;
    final Logger logger = Logger.getInstance();

    /**
     * Constructor
     * 
     * @param localRepoPath
     * @param pomFile
     */
    public RepoManager(List<Repo> repoList, Path localRepoPath) {
        this.repoList = repoList;
        this.TARGET_LOCAL_REPOSITORY = localRepoPath;
        this.repositorySystem = getRepositorySystem();
        this.repositorySystemSession = getRepositorySystemSession(repositorySystem);
    }

    /**
     * Download Repository
     * 
     * @throws ModelBuildingException
     */
    public void download(File POM) throws ModelBuildingException {
        logger.debug(String.format("loading this sample project's Maven descriptor from %s\n", POM.toPath().toString()));
        logger.debug(String.format("local Maven repository set to %s\n", this.TARGET_LOCAL_REPOSITORY.toString()));
        final DefaultModelBuildingRequest modelBuildingRequest = new DefaultModelBuildingRequest().setPomFile(POM);
        final ModelBuilder modelBuilder = new DefaultModelBuilderFactory().newInstance(); 
        final ModelBuildingResult modelBuildingResult = modelBuilder.build(modelBuildingRequest); 
        final Model model = modelBuildingResult.getEffectiveModel();

        logger.debug(String.format("Maven model resolved: %s, parsing its dependencies..\n", model));
        model.getDependencies().forEach(d -> {
            logger.debug(String.format("processing dependency: %s\n", d));
            Artifact artifact = new DefaultArtifact(d.getGroupId(), d.getArtifactId(), d.getType(), d.getVersion());
            ArtifactRequest artifactRequest = new ArtifactRequest(); 
            artifactRequest.setArtifact(artifact);
            artifactRequest.setRepositories(getRepositories(this.repoList));
            try {
                ArtifactResult artifactResult = repositorySystem.resolveArtifact(repositorySystemSession, artifactRequest);                
                artifact = artifactResult.getArtifact();
                logger.debug(String.format("artifact %s resolved to %s\n", artifact, artifact.getFile()));
            } catch (ArtifactResolutionException e) {
                logger.debug(String.format("error resolving artifact: %s\n", e.getMessage()));
            }
        });
    }

    /**
     * Get repository system
     * @return
     */
    public RepositorySystem getRepositorySystem() {
        DefaultServiceLocator serviceLocator = MavenRepositorySystemUtils.newServiceLocator(); 
        serviceLocator.addService(RepositoryConnectorFactory.class, BasicRepositoryConnectorFactory.class); 
        serviceLocator.addService(TransporterFactory.class, FileTransporterFactory.class); 
        serviceLocator.addService(TransporterFactory.class, HttpTransporterFactory.class); 
        serviceLocator.setErrorHandler(new DefaultServiceLocator.ErrorHandler() {
            @Override
            public void serviceCreationFailed(Class<?> type, Class<?> impl, Throwable exception) {
                logger.debug(String.format("error creating service: %s\n", exception.getMessage()));
                logger.throwable(exception);
            }
        });   
        return serviceLocator.getService(RepositorySystem.class);
    }
   
    /**
     * Get repository system session
     * @param system
     * @return
     */
    public DefaultRepositorySystemSession getRepositorySystemSession(RepositorySystem system) { 
        DefaultRepositorySystemSession repositorySystemSession = MavenRepositorySystemUtils.newSession();   
        LocalRepository localRepository = new LocalRepository(this.TARGET_LOCAL_REPOSITORY.toAbsolutePath().toString());
        repositorySystemSession.setLocalRepositoryManager(system.newLocalRepositoryManager(repositorySystemSession, localRepository));   
        repositorySystemSession.setRepositoryListener(new ConsoleRepoEventListener());   
        return repositorySystemSession; 
    }
    
    /**
     * Get repositories
     * @param system
     * @param session
     * @return
     */
    public List<RemoteRepository> getRepositories(List<Repo> repoList) { 
        return repoList.stream().map(r -> (RemoteRepository)new RemoteRepository.Builder(r.getId(), r.getType(), r.getUrl()).build()).collect(Collectors.toList());
    }    
}

