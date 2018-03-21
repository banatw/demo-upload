package com.example.demoupload;

import com.en.Action;
import com.entity.Mahasiswa;
import com.entity.Picture;
import com.model.Form;
import com.repository.MahasiswaRepo;
import com.repository.PictureRepo;
import net.coobird.thumbnailator.Thumbnails;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.tomcat.util.http.fileupload.ByteArrayOutputStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.Base64Utils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Set;

@SpringBootApplication
@EntityScan("com.entity")
@EnableJpaRepositories("com.repository")
public class DemoUploadApplication {
    @Autowired
    private MahasiswaRepo mahasiswaRepo;
    @Autowired
    private PictureRepo pictureRepo;

    public static void main(String[] args) {
        SpringApplication.run(DemoUploadApplication.class, args);
    }

    @Bean
    public CommandLineRunner initialData() {
        return (x) -> {
            Mahasiswa mahasiswa = new Mahasiswa();
            mahasiswa.setNama("syabana");
            mahasiswaRepo.save(mahasiswa);
        };
    }

    @Controller
    public class MyController extends WebMvcConfigurerAdapter {


        @Value("${file.location}")
        String UPLOADED_PATH;

        @GetMapping("/home")
        public String showIndex(Model model) {
            model.addAttribute("mahasiswas", mahasiswaRepo.findAll());
            return "home";
        }

        @GetMapping("/detail")
        public String showDetail(@RequestParam("id") Integer id, Model model) {
            Mahasiswa mahasiswa = mahasiswaRepo.findOne(id);
            model.addAttribute("idMahasiswa", id);
            model.addAttribute("pictures", mahasiswa.getPictures());
            return "detail";
        }

        @GetMapping("/add")
        public String showForm(@RequestParam("id") Integer id, Model model) {
            model.addAttribute("action", Action.ADD);
            model.addAttribute("idMahasiswa", id);
            return "form";
        }

        @GetMapping("/view")
        public String showView(@RequestParam("id") Integer id, Model model) {
            //Integer integerId = Integer.valueOf(id);
            Picture picture = pictureRepo.findOne(id);
            model.addAttribute("src","/pictures/" + picture.getDescription());
            return "view";
        }

        @GetMapping("/delete")
        public String hapus(@RequestParam("id") Integer id,@RequestParam("idm") Integer idm) {
            Picture picture = pictureRepo.findOne(id);
            File file = new File(UPLOADED_PATH + picture.getDescription());
            file.delete();
            pictureRepo.delete(id);
            return "redirect:/detail?id=" + idm;
        }

        @PostMapping("/simpan")
        public String simpan(Form frm) {
            ByteArrayOutputStream byteArrayOutputStreamThumbnail = null;
            Mahasiswa mahasiswa = mahasiswaRepo.findOne(Integer.valueOf(frm.getIdMahasiswa()));
            File file = null;
            for (int i = 0; i < frm.getFile().length; i++) {
                file = new File(UPLOADED_PATH + frm.getFile()[i].getOriginalFilename());
                //copy file to server
                //System.out.println(frm.getFile()[i].getOriginalFilename());
                Picture picture = new Picture();
                String filenameWithoutExt = frm.getFile()[i].getOriginalFilename().indexOf(".")>0?
                        frm.getFile()[i].getOriginalFilename().substring(0, frm.getFile()[i].getOriginalFilename().lastIndexOf("."))
                        :frm.getFile()[i].getOriginalFilename();
                picture.setFilename(frm.getFile()[i].getOriginalFilename());
                picture.setDescription(filenameWithoutExt);
                picture.setContentType(frm.getFile()[i].getContentType());

                FileOutputStream fileOutputStream = null;
                try {
                    fileOutputStream = new FileOutputStream(file);
                    fileOutputStream.write(frm.getFile()[i].getBytes());
                    fileOutputStream.flush();
                    fileOutputStream.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                //Create thumbnail
                byteArrayOutputStreamThumbnail = new ByteArrayOutputStream();
                if (frm.getFile()[i].getContentType().equalsIgnoreCase("application/pdf")) {
                    try {
                        PDDocument pdDocument = PDDocument.load(file);
                        PDFRenderer renderer = new PDFRenderer(pdDocument);
                        BufferedImage image = renderer.renderImage(0);
                        pdDocument.close();
                        java.io.ByteArrayOutputStream byteArrayOutputStream = new java.io.ByteArrayOutputStream();
                        ImageIO.write(image, "png", byteArrayOutputStream);
                        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
                        Thumbnails.of(byteArrayInputStream)
                                .size(100, 100)
                                .outputFormat("png")
                                .toOutputStream(byteArrayOutputStreamThumbnail);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    try {
                        Thumbnails.of(file)
                                .size(100, 100)
                                .outputFormat("png")
                                .toOutputStream(byteArrayOutputStreamThumbnail);
                    } catch (IOException e) {

                    }
                }

                //save to database
                try {
                    byteArrayOutputStreamThumbnail.flush();
                    picture.setImage(Base64Utils.encodeToString(byteArrayOutputStreamThumbnail.toByteArray()));

                    byteArrayOutputStreamThumbnail.close();
                    pictureRepo.save(picture);

                    Set<Picture> pictureSet = mahasiswa.getPictures();
                    pictureSet.add(picture);
                    mahasiswa.setPictures(pictureSet);
                    mahasiswaRepo.save(mahasiswa);
                } catch (IOException e1) {
                    e1.printStackTrace();
                }

            }

            return "redirect:/detail?id=" + frm.getIdMahasiswa();
        }

        @Override
        public void addResourceHandlers(ResourceHandlerRegistry registry) {
            registry
                    .addResourceHandler("/pictures/**")
                    .addResourceLocations("file:///" + UPLOADED_PATH);
        }
    }
}
