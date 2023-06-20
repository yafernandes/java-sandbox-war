package com.datadog.ese.playground;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.slf4j.LoggerFactory;

import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.statsd.StatsdConfig;
import io.micrometer.statsd.StatsdFlavor;
import io.micrometer.statsd.StatsdMeterRegistry;

@WebServlet("/micrometer")
public class Micrometer extends HttpServlet {

  final private String prefix = "micrometer.cardinality.high.";
  final static org.slf4j.Logger slf4jLogger = LoggerFactory.getLogger("sandbox");
  final static org.apache.logging.log4j.Logger log4j2Logger = LogManager.getLogger("sandbox");
  static private MeterRegistry registry = null;

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    resp.setContentType("text/html");

    if (registry == null) {
      StatsdConfig config = new StatsdConfig() {
        @Override
        public String get(String k) {
          slf4jLogger.info("Requesting key [" + k + "]");
          switch (k) {
            case "statsd.host":
              return "datadog.datadog";
            case "statsd.step":
              return "15s";
            default:
              return null;
          }
        }

        @Override
        public StatsdFlavor flavor() {
          return StatsdFlavor.DATADOG;
        }
      };

      registry = new StatsdMeterRegistry(config, Clock.SYSTEM);

      Gauge
          .builder(prefix + "memory", Runtime.getRuntime(), Runtime::freeMemory)
          .baseUnit("bytes")
          .description("Free memory")
          .register(registry);

      resp.getWriter().println("<h1>Micrometer started.</h1>");
    } else {
      resp.getWriter().println("<h1>Micrometer was active - " + prefix + "</h1>");
    }
  }

}
