package cn.gyu.flux.controller;

import cn.gyu.flux.config.HTTPConstant;
import cn.gyu.flux.ftbl.FTBLFileParser;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

@Controller
public class FTBLFileController {

    @ResponseBody
    @PostMapping(value = "/upload_ftbl_file")
    public Map parseFTBL(@RequestParam("file") MultipartFile file) {
        Map<String, String> resultMap = new HashMap<>();
        if(file.isEmpty()) {
            resultMap.put("status","error");
            return resultMap;
        }
        try{
            String fileName = file.getOriginalFilename();
            String dir= HTTPConstant.UPLOAD_DIR;
            String destFileName=dir+ File.separator + System.currentTimeMillis() + "_" + fileName;
            File destFile = new File(destFileName);
            if(destFile.exists()) {
                destFile.delete();
            }
            file.transferTo(destFile);
            resultMap.put("reaction_contents", FTBLFileParser.parseReactionContents(destFile));
            resultMap.put("flux_net", FTBLFileParser.parseFluxNet(destFile));
            resultMap.put("pool_size", FTBLFileParser.parsePoolSize(destFile));
            resultMap.put("equalities", FTBLFileParser.parseEqualities(destFile));
            resultMap.put("inequalities", FTBLFileParser.parseInEqualities(destFile));
            resultMap.put("label_input", FTBLFileParser.parseLabelInput(destFile));
            resultMap.put("mass_spectrometry", FTBLFileParser.parseMassSpectrometry(destFile));

            resultMap.put("status","success");
        }catch (Exception e) {
            e.printStackTrace();
            resultMap.put("status","error");
        }

        return resultMap;
    }

    @ResponseBody
    @PostMapping(value = "/download_template_ftbl_file")
    public void downloadTemplateFTBL(HttpServletResponse response) {
        String downloadFilePath = HTTPConstant.UPLOAD_DIR;
        String fileName = "Template.ftbl";
        File file = new File(downloadFilePath + File.separator + fileName);
        if (file.exists()) {
            response.setContentType("application/force-download");
            response.addHeader("Content-Disposition", "attachment;fileName=" + fileName);
            byte[] buffer = new byte[1024];
            FileInputStream fis = null;
            BufferedInputStream bis = null;
            try {
                fis = new FileInputStream(file);
                bis = new BufferedInputStream(fis);
                OutputStream os = response.getOutputStream();
                int i = bis.read(buffer);
                while (i != -1) {
                    os.write(buffer, 0, i);
                    i = bis.read(buffer);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (bis != null) {
                    try {
                        bis.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (fis != null) {
                    try {
                        fis.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
