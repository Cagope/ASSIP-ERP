package co.assip.erp.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

/**
 * ✅ Diagnóstico para listar todos los endpoints registrados.
 * Compatible con Actuator y evita conflictos de beans duplicados.
 */
@Component
public class MappingInspector implements CommandLineRunner {

    @Autowired
    @Qualifier("requestMappingHandlerMapping") // 👈 especifica el bean correcto
    private RequestMappingHandlerMapping handlerMapping;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("\n============================");
        System.out.println("✅ ENDPOINTS REGISTRADOS:");
        System.out.println("============================");

        handlerMapping.getHandlerMethods().forEach((key, value) -> {
            System.out.println(key + " => " + value.getMethod().getDeclaringClass().getSimpleName());
        });

        System.out.println("============================\n");
    }
}
