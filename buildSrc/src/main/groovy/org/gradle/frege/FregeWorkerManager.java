package org.gradle.frege;

//import org.gradle.api.file.FileCollection
//import org.gradle.internal.Factory
//import org.gradle.process.internal.JavaExecHandleBuilder
//import org.gradle.process.internal.WorkerProcess
//import org.gradle.process.internal.WorkerProcessBuilder

/**
 * // TODO: Document this
 *
 * @author Galder Zamarreño
 * @since // TODO
 */
public class FregeWorkerManager {

//   public FindBugsResult runWorker(File workingDir, Factory<WorkerProcessBuilder> workerFactory, FileCollection findBugsClasspath, FindBugsSpec spec) {
//      WorkerProcess process = createWorkerProcess(workingDir, workerFactory, findBugsClasspath, spec);
//      process.start();
//
//      FindBugsWorkerClient clientCallBack = new FindBugsWorkerClient()
//      process.connection.addIncoming(FindBugsWorkerClientProtocol.class, clientCallBack);
//      FindBugsResult result = clientCallBack.getResult();
//
//      process.waitForStop();
//      return result;
//   }
//
//   private WorkerProcess createWorkerProcess(File workingDir, Factory<WorkerProcessBuilder> workerFactory, FileCollection findBugsClasspath, FindBugsSpec spec) {
//      WorkerProcessBuilder builder = workerFactory.create();
//      builder.applicationClasspath(findBugsClasspath);
//      builder.sharedPackages(Arrays.asList("edu.umd.cs.findbugs"));
//      JavaExecHandleBuilder javaCommand = builder.getJavaCommand();
//      javaCommand.setWorkingDir(workingDir);
//      javaCommand.setMaxHeapSize(spec.getMaxHeapSize());
//
//      WorkerProcess process = builder.worker(new FindBugsWorkerServer(spec)).build()
//      return process
//   }

}
