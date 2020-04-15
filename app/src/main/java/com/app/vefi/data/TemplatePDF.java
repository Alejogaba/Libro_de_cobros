package com.app.vefi.data;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;


import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.app.vefi.DetallesPersonaActivity;
import com.app.vefi.R;
import com.app.vefi.data.model.Registro;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Currency;
import java.util.Locale;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class TemplatePDF {
    private Context context;
    private File pdfFile;
    private Document document;
    private PdfWriter pdfWriter;
    private Paragraph paragraph;
    private Font ftitle = new Font(Font.FontFamily.TIMES_ROMAN,20,Font.BOLD);
    private Font fsubtitle = new Font(Font.FontFamily.TIMES_ROMAN,18,Font.BOLD);
    private Font fText = new Font(Font.FontFamily.TIMES_ROMAN,12,Font.BOLD);
    private Font fHighText = new Font(Font.FontFamily.TIMES_ROMAN,15,Font.BOLD, BaseColor.LIGHT_GRAY);

    public TemplatePDF(Context context) {
        this.context = context;
    }

    public void openDocument(Activity activity){
       createFile();
        try {
            document = new Document(PageSize.LETTER);
            pdfWriter = PdfWriter.getInstance(document,new FileOutputStream(pdfFile));
            document.open();
        }catch (Exception e){
            Log.e("ERROR-PDF",e.toString());
        }
    }

    public boolean validarPermisos(Activity activity){
        if(Build.VERSION.SDK_INT<Build.VERSION_CODES.M){
            return true;
        }

        if((activity.checkSelfPermission(WRITE_EXTERNAL_STORAGE)==PackageManager.PERMISSION_GRANTED)&&
                (activity.checkSelfPermission(READ_EXTERNAL_STORAGE)==PackageManager.PERMISSION_GRANTED)){
            return true;
        }

        if((activity.shouldShowRequestPermissionRationale(WRITE_EXTERNAL_STORAGE)||
                (activity.shouldShowRequestPermissionRationale(READ_EXTERNAL_STORAGE)))){
            cargarDialogoRecomendacion(activity);
        }else{
            activity.requestPermissions(new String[]{WRITE_EXTERNAL_STORAGE,READ_EXTERNAL_STORAGE},100);
        }

        return false;
    }

    public void cargarDialogoRecomendacion(final Activity activity){
        AlertDialog.Builder dialogo = new AlertDialog.Builder(activity);
        dialogo.setTitle("Permisos desactivados");
        dialogo.setMessage("Debe aceptar los permisos para utilizar esta funcion");

        dialogo.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    activity.requestPermissions(new String[]{WRITE_EXTERNAL_STORAGE,READ_EXTERNAL_STORAGE},100);
                }
            }
        }).setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        dialogo.show();
    }


    private void createFile(){
        try{
            String rootPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/PDF";
            File root = new File(rootPath);
            if(!root.exists()){
                root.mkdirs();
            }
            pdfFile = new File(rootPath + "TemplatePDF.pdf");
            if (pdfFile.exists()) {
                pdfFile.delete();
            }
            pdfFile.createNewFile();
            FileOutputStream out = new FileOutputStream(pdfFile);

            out.flush();
            out.close();
        }catch (Exception e){
            Log.e("CREACION-PDF: ",e.toString());
        }
    }

    public void closeDocument(){
        document.close();
    }

    public void addMetaData(String title,String subject,String author){
        document.addTitle(title);
        document.addSubject(subject);
        document.addAuthor(author);
    }

    public void addTitles(String title,String subtitle,String date){
        try{
            paragraph= new Paragraph();
            addChild(new Paragraph(title,ftitle));
            addChild(new Paragraph(subtitle,fsubtitle));
            addChild(new Paragraph("Generado: "+date,fHighText));
            paragraph.setSpacingAfter(30);
            document.add(paragraph);
        }catch (Exception e){
            Log.e("ERROR-PDF:",e.toString());
        }
    }

    private void addChild(Paragraph childParagraph){
        childParagraph.setAlignment(Element.ALIGN_CENTER);
        paragraph.add(childParagraph);
    }

    public void addParagraph(String text){
        try {
            paragraph = new Paragraph(text,fText);
            paragraph.setSpacingBefore(5);
            paragraph.setSpacingAfter(5);
            document.add(paragraph);
        }catch (Exception e){
            Log.e("ERROR-PDF:",e.toString());
        }
    }

    public String convertirMoneda(float valor,Activity mactivity){
        Locale locale =mactivity.getResources().getConfiguration().locale;
        Currency currency = Currency.getInstance(locale);
        NumberFormat col = NumberFormat.getCurrencyInstance(locale);
        if(valor == (int) valor)
            return col.format((int)valor).replace(currency.getSymbol(),"   "+currency.getSymbol());
        else
            return col.format(valor).replace(currency.getSymbol(),"   "+currency.getSymbol());
    }

    public void createTable(String[] header, ArrayList<Registro> registros,Activity activity){
        try{
            paragraph =  new Paragraph();
            paragraph.setFont(fText);
            PdfPTable pdfPTable = new PdfPTable(header.length);
            pdfPTable.setWidthPercentage(100);
            pdfPTable.setSpacingBefore(20);
            PdfPCell pdfPCell;
            int indexC = 0;
            while (indexC<header.length){
                pdfPCell = new PdfPCell(new Phrase(header[indexC++],fsubtitle));
                pdfPCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                pdfPCell.setBackgroundColor(BaseColor.LIGHT_GRAY);
                pdfPTable.addCell(pdfPCell);
            }

            String celda;
            for(int indexRow=0;indexRow<registros.size();indexRow++){
                Registro row = registros.get(indexRow);
                for (int indexCol=0;indexCol<header.length;indexCol++){
                    switch (indexCol){
                        case 0:
                            if(row.getDay()==0){
                                celda="";
                            }else{
                                celda = String.valueOf(row.getDay())+" de "+generarMes(row,activity);
                            }
                            pdfPCell = new PdfPCell(new Phrase(celda));
                            pdfPCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                            pdfPCell.setFixedHeight(10);
                            pdfPTable.addCell(pdfPCell);
                            break;
                        case 1:
                            celda = row.getDescripcion();
                            pdfPCell = new PdfPCell(new Phrase(celda));
                            pdfPCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                            pdfPCell.setMinimumHeight(10);
                            pdfPTable.addCell(pdfPCell);
                            break;
                        case 2:
                            celda = convertirMoneda(row.getValor(),activity);
                            pdfPCell = new PdfPCell(new Phrase(celda));
                            pdfPCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                            pdfPCell.setMinimumHeight(10);
                            pdfPTable.addCell(pdfPCell);
                            break;
                    }
                }
            }
            paragraph.add(pdfPTable);
            document.add(paragraph);
        }catch (Exception e){
            Log.e("ERROR-PDF:",e.toString());
        }
    }

    private String generarMes(Registro registro,Activity activity){
        String mes=null;
        switch (registro.getMonth()){
            case 0:
                mes = activity.getString(R.string.month_1);
                break;
            case 1:
                mes = activity.getString(R.string.month_2);
                break;
            case 2:
                mes = activity.getString(R.string.month_3);
                break;
            case 3:
                mes = activity.getString(R.string.month_4);
                break;
            case 4:
                mes = activity.getString(R.string.month_5);
                break;
            case 5:
                mes = activity.getString(R.string.month_6);
                break;
            case 6:
                mes = activity.getString(R.string.month_7);
                break;
            case 7:
                mes = activity.getString(R.string.month_8);
                break;
            case 8:
                mes = activity.getString(R.string.month_9);
                break;
            case 9:
                mes = activity.getString(R.string.month_10);
                break;
            case 10:
                mes = activity.getString(R.string.month_11);
                break;
            case 11:
                mes = activity.getString(R.string.month_12);
                break;
        }
        return mes;
    }


    public void appViewPDF(Activity activity){
        if(pdfFile.exists()){
            Uri uri = Uri.fromFile(pdfFile);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(uri,"application/pdf");
            try{
                activity.startActivity(intent);

            }catch (ActivityNotFoundException e){
                activity.startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse("market://details?id=com.adobe.reader")));
                Toast.makeText(activity.getApplicationContext(),"No hay ninguna aplicacion instalada que permita visualizar archivos PDF",Toast.LENGTH_LONG);
                Log.e("ERROR-PDF:",e.toString());
            }
        }else{
            Toast.makeText(activity.getApplicationContext(),"No se encontro el archivo",Toast.LENGTH_LONG);
        }
    }

}
