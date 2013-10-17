package org.gradle.frege;

import java.io.Serializable;

/**
 * // TODO: Document this
 *
 * @author Galder Zamarreño
 * @since // TODO
 */
public class FregeResult implements Serializable {

   private final int bugCount;
   private final int missingClassCount;
   private final int errorCount;
   private final Exception exception;

   public FregeResult(int bugCount, int missingClassCount, int errorCount) {
      this(bugCount, missingClassCount, errorCount, null);
   }

   public FregeResult(int bugCount, int missingClassCount, int errorCount, Exception exception) {
      this.bugCount = bugCount;
      this.missingClassCount = missingClassCount;
      this.errorCount = errorCount;
      this.exception = exception;
   }

   public int getBugCount() {
      return bugCount;
   }

   public int getMissingClassCount() {
      return missingClassCount;
   }

   public int getErrorCount() {
      return errorCount;
   }

   public Exception getException() {
      return exception;
   }

}
