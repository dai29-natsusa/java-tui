package jp.co.santa.soft;

import io.quarkus.picocli.runtime.annotations.TopCommand;
import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.annotations.QuarkusMain;
import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.inject.Inject;
import jp.co.santa.soft.tui.view.MainDashboardView;
import org.jboss.logging.Logger;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@RegisterForReflection(
    targets = {
      dev.tamboui.terminal.BackendProvider.class,
      dev.tamboui.backend.panama.PanamaBackendProvider.class,
      dev.tamboui.backend.panama.PanamaBackend.class
    })
@QuarkusMain
@TopCommand
@Command(
    name = "santa-tui",
    mixinStandardHelpOptions = true,
    version = "1.0.0",
    description = "Quarkus + TamboUI Terminal Application")
public class App implements Runnable, QuarkusApplication {

  private static final Logger LOG = Logger.getLogger(App.class);

  @Inject CommandLine.IFactory factory;

  @Inject MainDashboardView dashboardView;

  @Option(
      names = {"-n", "--non-interactive"},
      description = "Run diagnostics and exit without starting TUI")
  boolean nonInteractive;

  @Override
  public int run(String... args) {
    return new CommandLine(this, factory).execute(args);
  }

  @Override
  public void run() {
    LOG.info("Starting Santa Soft TUI Application...");
    if (nonInteractive) {
      LOG.info("Non-interactive mode requested. Exiting cleanly.");
      return;
    }
    try {
      dashboardView.launch();
    } catch (Exception e) {
      LOG.error("Fatal error in TamboUI event loop", e);
      throw new RuntimeException(e);
    }
  }
}
