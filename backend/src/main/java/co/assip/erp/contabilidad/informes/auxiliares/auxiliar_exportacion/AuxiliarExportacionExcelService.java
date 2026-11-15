package co.assip.erp.contabilidad.informes.auxiliares.auxiliar_exportacion;

import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

/** Genera Excel con datos numéricos y alertas sin omitir movimientos. */
@Service
@RequiredArgsConstructor
public class AuxiliarExportacionExcelService {
    private static final String[] BASE = {"Tipo persona", "Tipo documento", "Documento", "DV",
        "Primer nombre", "Segundo nombre", "Primer apellido", "Segundo apellido", "Razón social",
        "Dirección", "Departamento", "Municipio", "País", "Cuenta", "Nombre cuenta", "Naturaleza"};
    private final AuxiliarExportacionRepository repository;

    public byte[] generar(int agencia, java.time.LocalDate inicio, java.time.LocalDate fin, boolean resumido) {
        List<Map<String,Object>> filas = repository.consultar(agencia, inicio, fin, resumido);
        try (var libro = new XSSFWorkbook(); var salida = new ByteArrayOutputStream()) {
            Sheet datos = libro.createSheet(resumido ? "Auxiliar resumido" : "Auxiliar detallado");
            Sheet alertas = libro.createSheet("Alertas terceros");
            CellStyle encabezado = libro.createCellStyle();
            Font fuente = libro.createFont(); fuente.setBold(true); encabezado.setFont(fuente);
            encabezado.setFillForegroundColor(IndexedColors.PALE_BLUE.getIndex());
            encabezado.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            CellStyle numero = libro.createCellStyle();
            numero.setDataFormat(libro.createDataFormat().getFormat("#,##0.00;[Red](#,##0.00)"));
            List<String> titulos = new ArrayList<>(Arrays.asList(BASE));
            if (!resumido) titulos.addAll(List.of("Fecha", "Tipo comprobante", "Número comprobante", "Detalle"));
            titulos.addAll(List.of("Débito", "Crédito", "Saldo según naturaleza"));
            Row cab = datos.createRow(0);
            for (int i=0;i<titulos.size();i++) { Cell celda=cab.createCell(i); celda.setCellValue(titulos.get(i)); celda.setCellStyle(encabezado); }
            Row cabAlertas=alertas.createRow(0);
            String[] titulosAlerta={"Documento", "Tercero", "Observación"};
            for(int i=0;i<titulosAlerta.length;i++){Cell c=cabAlertas.createCell(i);c.setCellValue(titulosAlerta[i]);c.setCellStyle(encabezado);}
            Set<String> alertasUnicas=new HashSet<>(); int r=1,a=1;
            for(Map<String,Object> fila:filas){
                Row row=datos.createRow(r++); int col=0;
                String tipo=valor(fila,"tipo_persona"); boolean juridica="2".equals(tipo);
                String nombres=valor(fila,"nombres_originales");
                // No se inventa una separación de nombres compuestos: el original queda íntegro en primer nombre.
                String[] valores={tipo,valor(fila,"tipo_documento"),valor(fila,"documento"),valor(fila,"dv"),
                    juridica?"":nombres,"",juridica?"":valor(fila,"apellido1"),
                    juridica?"":valor(fila,"apellido2"),juridica?nombres:"",
                    valor(fila,"direccion"),valor(fila,"departamento"),valor(fila,"municipio"),
                    valor(fila,"pais"),valor(fila,"cuenta"),valor(fila,"nombre_cuenta"),valor(fila,"naturaleza")};
                for(String v:valores) row.createCell(col++).setCellValue(v);
                if(!resumido){
                    row.createCell(col++).setCellValue(valor(fila,"fecha"));
                    row.createCell(col++).setCellValue(valor(fila,"tipo_comprobante"));
                    row.createCell(col++).setCellValue(valor(fila,"numero_comprobante"));
                    row.createCell(col++).setCellValue(valor(fila,"detalle"));
                }
                BigDecimal debito=importe(fila,"debito"),credito=importe(fila,"credito");
                BigDecimal saldo="C".equalsIgnoreCase(valor(fila,"naturaleza"))?credito.subtract(debito):debito.subtract(credito);
                for(BigDecimal v:List.of(debito,credito,saldo)){Cell c=row.createCell(col++);c.setCellValue(v.doubleValue());c.setCellStyle(numero);}
                List<String> avisos=new ArrayList<>();
                if(fila.get("id_ubicacion")==null) avisos.add("Sin ubicación registrada");
                if(valor(fila,"direccion").isBlank()) avisos.add("Sin dirección");
                if(valor(fila,"municipio").isBlank()) avisos.add("Sin municipio");
                if(valor(fila,"departamento").isBlank()) avisos.add("Sin departamento");
                if(valor(fila,"pais").isBlank()) avisos.add("Sin país");
                if(!juridica && !nombres.isBlank() && nombres.trim().contains(" ")) avisos.add("Revisar separación de nombres");
                String clave=valor(fila,"documento");
                if(!avisos.isEmpty() && alertasUnicas.add(clave)){
                    Row ar=alertas.createRow(a++);ar.createCell(0).setCellValue(clave);
                    ar.createCell(1).setCellValue(nombres+" "+valor(fila,"apellido1"));
                    ar.createCell(2).setCellValue(String.join("; ",avisos));
                }
            }
            datos.createFreezePane(0,1); alertas.createFreezePane(0,1);
            datos.setAutoFilter(new org.apache.poi.ss.util.CellRangeAddress(0,Math.max(1,r-1),0,titulos.size()-1));
            for(int i=0;i<titulos.size();i++) datos.setColumnWidth(i,Math.min(8500,Math.max(3800,titulos.get(i).length()*290)));
            alertas.setColumnWidth(0,5500);alertas.setColumnWidth(1,9000);alertas.setColumnWidth(2,15000);
            libro.write(salida);return salida.toByteArray();
        }catch(IOException ex){throw new IllegalStateException("No fue posible generar el Excel",ex);}
    }
    private static String valor(Map<String,Object> fila,String clave){Object v=fila.get(clave);return v==null?"":v.toString();}
    private static BigDecimal importe(Map<String,Object> fila,String clave){Object v=fila.get(clave);return v==null?BigDecimal.ZERO:new BigDecimal(v.toString());}
}
