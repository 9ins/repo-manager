package org.chaostocosmos.repo;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.chaostocosmos.io.console.ConsoleFactory;
import org.chaostocosmos.io.console.ConsoleInput;
import org.chaostocosmos.io.console.ConsoleTrigger;

/**
 * RepoDwonerApp class
 */
public class RepoManagerApp implements ConsoleTrigger {

    ConsoleInput consoleInput;
    RepoManager repoManager;
    XMLHandler xmlHandler;

    /**
     * Constructor
     * @param path
     * @throws Exception
     */
    public RepoManagerApp(String path) throws Exception {
        //Create File object for message.yml
        File messageFile = Paths.get(path).toFile();
        //Create ConsoleInput object using ConsoleFactory
        this.consoleInput = ConsoleFactory.getDefaultConsoleInput(messageFile, this);
    }

    @Override
    public void exit() {
        System.out.println("Working process canceled...");
    }

    @Override
    public boolean trigger(Map<String, Object> map) throws Exception {
        //System.out.println(map.toString());
        List<Repo> repoList = map.entrySet().stream().filter(e -> e.getKey().startsWith("QUERY1")).map(e1 -> {
            String url = map.get(e1.getKey())+"";
            int idx = e1.getKey().lastIndexOf("_");
            idx = idx == -1 ? e1.getKey().length() : idx;
            String id = map.get("QUERY2"+e1.getKey().substring(idx))+"";
            String type = map.get("QUERY3"+e1.getKey().substring(idx))+"";
            id = id.equals("") ? "central" : id;
            type = type.equals("") ? "default" : type;
            if(url.equals("")) {
                throw new RuntimeException("URL must be specified.");
            }
            Repo repo = new Repo(id, type, url);
            return repo;
        }).collect(Collectors.toList());

        String workspace = map.get("QUERY4")+"";
        if(workspace == null || workspace.equals("")) {
            throw new IllegalArgumentException("Scanning pom.xml path must be provided.");
        }
        Path workspacePath = Paths.get(workspace);
        String localRepository = map.get("QUERY5")+"";
        localRepository = localRepository.equals("") ? "./repo" : localRepository;
        Path localRepositoryPath = Paths.get(localRepository);
        if(!workspacePath.toFile().exists()) {
            throw new FileNotFoundException("Project directory not exist!!!");
        }
        if(localRepository.equals("")) {
            throw new RuntimeException("Target LOCAL repository must be specified.");
        }
        if(!localRepositoryPath.toFile().exists()) {
            if(!localRepositoryPath.toFile().mkdirs()) {
                throw new IOException("File to create target LOCAL repository: "+localRepositoryPath.toString());
            }
        }
        String thread = map.get("QUERY6")+"";
        int threadCount = Integer.parseInt(thread);
        download(workspacePath, localRepositoryPath, repoList, threadCount);
        return false;
    }

    /**
     * Download remote repository library to local
     * @param workspacePath
     * @param localRepoPath
     * @param repoList
     * @param threadCount
     * @throws IOException
     * @throws InterruptedException
     */
    public void download(Path workspacePath, Path localRepoPath, List<Repo> repoList, int threadCount) throws IOException, InterruptedException {
        Path processPath = workspacePath;//.resolve("LOCAL_PROJECT").resolve("poms").resolve("jobs").resolve("process");        
        System.out.println("Begin repository download.  Local path: "+localRepoPath.toAbsolutePath().toString()+"   "+new Date());
        ExecutorService threadpool = Executors.newFixedThreadPool(threadCount);
        repoManager = new RepoManager(repoList, localRepoPath);
        Files.walk(processPath)
            .filter(p -> p.toFile().getName().equals("pom.xml"))
            .collect(Collectors.toList())
                .stream()
                .forEach(p -> threadpool.submit(new Runnable() {
                    @Override
                    public void run() {
                        System.out.println("######################################################################################################"+System.lineSeparator()
                        +"JOB pom.xml path: "+p.toFile().getAbsolutePath()+System.lineSeparator()
                        +"######################################################################################################");        
                        try {                
                            Path tp = Files.createTempFile("pom_", ".xml");
                            System.out.println(tp);
                            Files.copy(p, tp, StandardCopyOption.REPLACE_EXISTING);
                            xmlHandler = new XMLHandler(tp.toFile());
                            xmlHandler.removeNotNeededAndSave(tp.toFile());
                            repoManager.download(tp.toFile());
                            tp.toFile().delete();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }                                
                    }
                })
            );
        threadpool.shutdown();
        while(!threadpool.awaitTermination(3, TimeUnit.SECONDS)) {
            System.out.println("Waiting for termination...");
        }
        System.out.println("End download... "+new Date());
    }

    public static void main(String[] args) throws Exception {        
        //repo list
        //https://repo1.maven.org/maven2/
        //https://repo.maven.apache.org/maven2
        if(args.length == 1) {
            new RepoManagerApp(args[0]);
        }
    }
}
