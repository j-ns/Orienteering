/**
 *
 *  Copyright (c) 2016, Jens Stroh
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 *  ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED. IN NO EVENT SHALL JENS STROH BE LIABLE FOR ANY
 *  DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */


import java.util.concurrent.CountDownLatch;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;

import javax.swing.SwingUtilities;

import org.junit.Rule;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 * A JUnit {@link Rule} for running tests on the JavaFX thread and performing
 * JavaFX initialisation. To include in your test case, add the following code:
 *
 * <pre>
 * {@literal @}Rule
 * public JavaFXThreadingRule jfxRule = new JavaFXThreadingRule();
 * </pre>
 *
 * @author Andy Till
 *
 */
public class JavaFXThreadingRule implements TestRule {

   /**
    * Flag for setting up the JavaFX, we only need to do this once for all tests.
    */
   private static boolean jfxIsSetup;

   @Override
   public Statement apply(Statement statement, Description description) {
      return new OnJFXThreadStatement(statement);
   }

   private static class OnJFXThreadStatement extends Statement {

      private final Statement statement;

      public OnJFXThreadStatement(Statement aStatement) {
         statement = aStatement;
      }

      private Throwable rethrownException = null;

      @Override
      public void evaluate() throws Throwable {
         if (!jfxIsSetup) {
            setupJavaFX();
            jfxIsSetup = true;
         }
         final CountDownLatch countDownLatch = new CountDownLatch(1);
         Platform.runLater(new Runnable() {

            @Override
            public void run() {
               try {
                  statement.evaluate();
               } catch (Throwable e) {
                  rethrownException = e;
               }
               countDownLatch.countDown();
            }
         });
         countDownLatch.await();
         // if an exception was thrown by the statement during evaluation,
         // then re-throw it to fail the test
         if (rethrownException != null) {
            throw rethrownException;
         }
      }

      protected void setupJavaFX() throws InterruptedException {
         long timeMillis = System.currentTimeMillis();
         final CountDownLatch latch = new CountDownLatch(1);
         SwingUtilities.invokeLater(new Runnable() {

            public void run() {
               // initializes JavaFX environment
               new JFXPanel();
               latch.countDown();
            }
         });
         System.out.println("javafx initialising...");
         latch.await();
         System.out.println("javafx is initialised in " + (System.currentTimeMillis() - timeMillis) + "ms");
      }
   }
}