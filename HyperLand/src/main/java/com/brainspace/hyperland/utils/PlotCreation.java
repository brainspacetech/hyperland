package com.brainspace.hyperland.utils;

import com.brainspace.hyperland.dao.IMasterDAO;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.CellType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlotCreation {

    public boolean createPlot(IMasterDAO masterDAO,InputStream inputStream)
    {
        boolean flag = false;
        try {
            String firmName = "Alphabet";
            String projectName = "Project1";
            String blockName = "C-Block";
            String propertyTypeId = "1";
            String propertyName = "Plot";
            String plotNo = "501";
            Double plotSize = 1000.00;
            Double sqftRate = 600.00;

            List<Map> valueList = readFile(inputStream);

            for (int i = 0; i < valueList.size(); i++) {
                //    Firm	Property	Block	Facing	Plot No	Area(in Sqr Ft.)	Rate/Sqr Ft.	Road	Description	PLC	PLC ChargingType

                Map valueMap = valueList.get(i);
                System.out.println(valueMap.get("Firm"));
                firmName = valueMap.get("Firm") != null ? valueMap.get("Firm").toString() : "";
                projectName = valueMap.get("Property") != null ? valueMap.get("Property").toString() : "";
                blockName = valueMap.get("Block") != null ? valueMap.get("Block").toString() : "";
                plotNo = valueMap.get("Plot No") != null ? valueMap.get("Plot No").toString() : "";
                System.out.println("plotsize -- "+valueMap.get("Area(in Sqr Ft.)") );
                plotSize = valueMap.get("Area(in Sqr Ft.)") != null ? Double.parseDouble(valueMap.get("Area(in Sqr Ft.)").toString()) : 0.00;
                sqftRate = valueMap.get("Rate/Sqr Ft") != null ? Double.parseDouble(valueMap.get("Rate/Sqr Ft").toString()) : 0.00;
                if(firmName != null && !firmName.equalsIgnoreCase("")) {
                    String firmId = getFirmId(masterDAO, firmName);
                    String projectId = getProjectId(masterDAO, projectName, firmId);
                    String blockId = getPBlockId(masterDAO, blockName, projectId, firmId);
                    try {
                        createPlot(masterDAO, plotNo, blockId, firmId, blockName, projectId, projectName, firmName, sqftRate, plotSize, propertyName, propertyTypeId);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            flag = true;
        }
        catch(Exception e1)
        {
            e1.printStackTrace();
        }
            return flag;
    }

   private  String  getFirmId (IMasterDAO masterDAO, String firmName) throws Exception
   {
       String firmQuery = "Select Id as id from FirmMaster where FirmName = '"+firmName+"'";
       System.out.println("firmQuery -- "+firmQuery);
       String firmId = "";
       List firmList = masterDAO.getAllData(firmQuery);
       if(firmList.size()>0)
       {
           Map firmMap = (Map) firmList.get(0);
           firmId = firmMap.get("id").toString();
       }
       return firmId;
   }
    private  String  getProjectId (IMasterDAO masterDAO, String projectName, String firmId) throws Exception
    {
        String projectQuery = "Select Id as id from ProjectMaster where FirmId = "+firmId+" and ProjectName = '"+projectName+"'";
        String projectId = "";
        List projectList = masterDAO.getAllData(projectQuery);
        if(projectList.size()>0){
            Map firmMap = (Map) projectList.get(0);
            projectId = firmMap.get("id").toString();
        }
        return projectId;
    }
    private  String  getPBlockId (IMasterDAO masterDAO,String blockName ,String projectId, String firmId) throws Exception
    {
        String blockQuery = "Select Id as id from BlockMaster where FirmId = "+firmId+" and PropertyId = "+projectId+" and Block = '"+blockName+"'";
        String blockId = "";
        List projectList = masterDAO.getAllData(blockQuery);
        if(projectList.size()>0){
            Map blockMap = (Map) projectList.get(0);
            blockId = blockMap.get("id").toString();
        }
        return blockId;
    }

    private  void createPlot(IMasterDAO masterDAO, String plotNo, String blockId , String firmId, String blockName , String projectId, String projectName, String firmName, Double sqFtRate,Double plotSize, String propertyType, String propertyTypeId)
    {
        String query = "INSERT INTO PlotDetails (FirmId,FirmName,ProjectId, ProjectName,BlockId,Block,PlotNo, SqFtRate,PlotSize,Status)" +
                 " VALUES ("+firmId+",'"+firmName+"',"+projectId+",'"+projectName+"',"+blockId+",'"+blockName+"',"+plotNo+","+sqFtRate+","+plotSize+",'Available')";

        masterDAO.updateData(query);
    }
    private  List<Map>  readFile(InputStream inputStream) throws IOException
    {
        List<Map> valueMapList = new ArrayList<>();
        POIFSFileSystem fs = new POIFSFileSystem(inputStream);
        HSSFWorkbook wb = new HSSFWorkbook(fs);
        HSSFSheet sheet = wb.getSheetAt(0);
        HSSFRow row;
        HSSFCell cell;
        int cols =11; // No of columns
        int tmp = 0;
        int rows; // No of rows
        rows = sheet.getPhysicalNumberOfRows();

        HSSFRow tempRow = null;
        Map valueMap = null;
        for(int r = 0; r < rows; r++) {
            valueMap = new HashMap();
            row = sheet.getRow(r);
            if(r ==0)
            {
                tempRow = row;
                continue;
            }
            if(row != null) {
                for(int c = 0; c < cols; c++) {
                    cell = row.getCell((short)c);
                    if(cell != null) {
                        CellType type = cell.getCellType();
                        if(type == CellType.NUMERIC) {
                            System.out.println(tempRow.getCell((short) c).getStringCellValue() +" -===== "+cell.getNumericCellValue());
                            valueMap.put(tempRow.getCell((short) c).getStringCellValue(), cell.getNumericCellValue());
                        }
                        else {
                            System.out.println(tempRow.getCell((short) c).getStringCellValue() +" -===== "+cell.getStringCellValue());
                            valueMap.put(tempRow.getCell((short) c).getStringCellValue(), cell.getStringCellValue());
                        }
                    }
                }
            }
            valueMapList.add(valueMap);
        }
        return valueMapList;
    }
}
