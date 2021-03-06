package com.example.demoupload;

import com.en.Action;
import com.entity.Picture;
import com.entity.User;
import com.model.Form;
import com.repository.PictureRepo;
import com.repository.UserRepo;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.poi.POIXMLProperties;
import org.apache.poi.xwpf.converter.pdf.PdfConverter;
import org.apache.poi.xwpf.converter.pdf.PdfOptions;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.tomcat.util.http.fileupload.ByteArrayOutputStream;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.Base64Utils;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.resource.PathResourceResolver;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletResponse;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.security.Principal;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@SpringBootApplication
@EntityScan("com.entity")
@EnableJpaRepositories("com.repository")
public class DemoUploadApplication {
    @Autowired
    private UserRepo userRepo;
    @Autowired
    private PictureRepo pictureRepo;

    public static void main(String[] args) {
        SpringApplication.run(DemoUploadApplication.class, args);
    }

    @Bean
    public CommandLineRunner initialData() {
        return (x) -> {
            User user = new User();
            user.setUsername("bana");
            user.setNama("syabana");
            user.setPassword("123");
            user.setRole("ROLE_USER");
            userRepo.save(user);
        };
    }

    @Controller
    public class MyController extends WebMvcConfigurerAdapter {


        @Value("${file.location}")
        String UPLOADED_PATH;

        @GetMapping("/home")
        public String showIndex(Model model, Principal principal) {
            model.addAttribute("mahasiswas", userRepo.findOne(principal.getName()));
            return "home";
        }

        @GetMapping("/detail")
        public String showDetail(@RequestParam("id") String id, Model model) {
            User user = userRepo.findOne(id);
            model.addAttribute("username", user.getUsername());
            model.addAttribute("pictures", user.getPictures());
            return "detail";
        }

        @GetMapping("/add")
        public String showForm(@RequestParam("id") String id, Model model) {
            model.addAttribute("action", Action.ADD);
            model.addAttribute("username", id);
            return "form";
        }

        /*@GetMapping("/view")
        public String showView(@RequestParam("id") String id, Model model) {
            //Integer integerId = Integer.valueOf(id);
            Picture picture = pictureRepo.findOne(id);
            model.addAttribute("src","/pictures/" + picture.getDescription());
            return "view";
        }*/

        @GetMapping("/delete")
        public String hapus(@RequestParam("id") String id,@RequestParam("idm") String idm) {
            Picture picture = pictureRepo.findOne(id);
            File file = new File(UPLOADED_PATH + picture.getIdPicture());
            file.delete();
            pictureRepo.delete(id);
            return "redirect:/detail?id=" + idm;
        }

        @GetMapping(value = "/view")
        public void viewPict(@RequestParam("id") String id, HttpServletResponse response) {
            Picture picture = pictureRepo.findOne(id);
            File file = new File(UPLOADED_PATH + picture.getIdPicture());
            response.setContentType(picture.getContentType());
            response.setContentLength((int) file.length());
            response.setHeader("Content-Disposition", "inline; filename=\"" + picture.getFilename() +"\"");
            try {
                InputStream inputStream = new BufferedInputStream(new FileInputStream(file));
                FileCopyUtils.copy(inputStream,response.getOutputStream());
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        /*@GetMapping(value = "/view")
        public ResponseEntity<InputStreamResource> view(@RequestParam("id") String id) {
            Picture picture = pictureRepo.findOne(id);
            File file = new File(UPLOADED_PATH + picture.getIdPicture());
            InputStreamResource inputStreamResource = null;
            try {
                inputStreamResource = new InputStreamResource(new FileInputStream(file));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            String[] type = picture.getContentType().split("/");
            System.out.println(picture.getContentType());
            System.out.println(type[0]);
            MediaType mediaType = new MediaType(type[0],type[1]);
            return ResponseEntity.ok()
                    .contentLength(file.length())
                    .contentType(MediaType.IMAGE_GIF)
                    .header(HttpHeaders.CONTENT_DISPOSITION,"attachment;filename=" + picture.getFilename())
                    .body(inputStreamResource);

        }*/

        /*@GetMapping(value = "/view")
        public void getImageAsByteArray(HttpServletResponse response,@RequestParam("id") String id) throws IOException {
            Picture picture = pictureRepo.findOne(id);
            File file = new File(UPLOADED_PATH + picture.getIdPicture());
            InputStream in = getClass().getResourceAsStream(UPLOADED_PATH + picture.getIdPicture());
            response.setContentType(MediaType.IMAGE_GIF_VALUE);
            IOUtils.copy(in, response.getOutputStream());
        }*/

        @PostMapping("/simpan")
        public String simpan(Form frm) {
            final int HEIGHT = 200;
            final int WIDTH = 200;

            ByteArrayOutputStream byteArrayOutputStreamThumbnail = null;
            User user = userRepo.findOne(frm.getUsername());
            File file;
            for (int i = 0; i < frm.getFile().length; i++) {
                String stringUUID = UUID.randomUUID().toString().replace("-","");
                file = new File(UPLOADED_PATH + stringUUID);
                //copy file to server
                //System.out.println(frm.getFile()[i].getOriginalFilename());
                Picture picture = new Picture();
                picture.setIdPicture(stringUUID);
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
                //System.out.println(frm.getFile()[i].getContentType().toString());
                //Create thumbnail
                byteArrayOutputStreamThumbnail = new ByteArrayOutputStream();
                if (frm.getFile()[i].getContentType().equalsIgnoreCase("application/pdf")) {
                    try {
                        PDDocument pdDocument = PDDocument.load(file);
                        PDFRenderer renderer = new PDFRenderer(pdDocument);
                        BufferedImage image = renderer.renderImage(0);
                        Image tmpImage = image.getScaledInstance(WIDTH, HEIGHT, Image.SCALE_DEFAULT);
                        BufferedImage resizedImage = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB);
                        Graphics2D g = resizedImage.createGraphics();
                        g.drawImage(tmpImage, 0, 0, WIDTH, HEIGHT, null);
                        g.dispose();
                        pdDocument.close();
                        ImageIO.write(resizedImage, "png", byteArrayOutputStreamThumbnail);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else if(frm.getFile()[i].getContentType().equalsIgnoreCase("application/vnd.openxmlformats-officedocument.wordprocessingml.document")
                        || frm.getFile()[i].getContentType().equalsIgnoreCase("application/msword")
                        || frm.getFile()[i].getContentType().equalsIgnoreCase("application/vnd.openxmlformats-officedocument.presentationml.presentation")) {
                    try {
                        InputStream is = new FileInputStream(file);
                        XWPFDocument document = new XWPFDocument(is);

                        // 2) Prepare Pdf options
                        PdfOptions options = PdfOptions.create();;

                        // 3) Convert XWPFDocument to Pdf
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        PdfConverter.getInstance().convert(document,baos,options);
                        PDDocument pdDocument = PDDocument.load(baos.toByteArray());
                        PDFRenderer renderer = new PDFRenderer(pdDocument);
                        BufferedImage image = renderer.renderImage(0);
                        Image tmpImage = image.getScaledInstance(WIDTH, HEIGHT, Image.SCALE_DEFAULT);
                        BufferedImage resizedImage = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB);
                        Graphics2D g = resizedImage.createGraphics();
                        g.drawImage(tmpImage, 0, 0, WIDTH, HEIGHT, null);
                        g.dispose();
                        pdDocument.close();
                        ImageIO.write(resizedImage, "png", byteArrayOutputStreamThumbnail);
                        //String thumbnail = props.getThumbnailFilename();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                } else {
                    try {
                        BufferedImage image = ImageIO.read(file);
                        Image tmpImage = image.getScaledInstance(WIDTH, HEIGHT,Image.SCALE_SMOOTH);
                        BufferedImage resizedImage = new BufferedImage(WIDTH, HEIGHT,BufferedImage.TYPE_INT_ARGB);
                        Graphics2D g = resizedImage.createGraphics();
                        g.drawImage(tmpImage,0,0,WIDTH, HEIGHT,null);
                        g.dispose();
                        ImageIO.write(resizedImage, "png", byteArrayOutputStreamThumbnail);
                    } catch (IOException e) {

                    }
                }

                //save to database
                try {
                    byteArrayOutputStreamThumbnail.flush();
                    picture.setImage(Base64Utils.encodeToString(byteArrayOutputStreamThumbnail.toByteArray()));

                    byteArrayOutputStreamThumbnail.close();
                    pictureRepo.save(picture);

                    Set<Picture> pictureSet = user.getPictures();
                    pictureSet.add(picture);
                    user.setPictures(pictureSet);
                    userRepo.save(user);
                } catch (IOException e1) {
                    e1.printStackTrace();
                }

            }

            return "redirect:/detail?id=" + frm.getUsername();
        }

        @Override
        public void addResourceHandlers(ResourceHandlerRegistry registry) {
            registry
                    .addResourceHandler("/pictures/**")
                    .addResourceLocations("file://" + UPLOADED_PATH)
                    .setCachePeriod(3000)
                    .resourceChain(true)
                    .addResolver(new PathResourceResolver());
        }
    }


    public class MyUserDetailService implements UserDetailsService {

        @Override
        public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException {
            User user = userRepo.findOne(s);
            String username = user.getUsername();
            String password = user.getPassword();
            String role = user.getRole();
            org.springframework.security.core.userdetails.User user1 = new org.springframework.security.core.userdetails.User(username,password,
                    AuthorityUtils.commaSeparatedStringToAuthorityList(role));
            return  user1;
        }
    }

    @EnableWebSecurity
    public class Keamanan extends WebSecurityConfigurerAdapter {

        @Autowired
        protected void configure(AuthenticationManagerBuilder auth) throws Exception {
            auth.userDetailsService(new MyUserDetailService());
        }

        @Override
        protected void configure(HttpSecurity http) throws Exception {
            http
                    .authorizeRequests()
                    .antMatchers("/css/**").permitAll()
                    .antMatchers(("/js/**")).permitAll()
                    .antMatchers("/webjars/**").permitAll()
                    .antMatchers("/**").hasRole("USER")
                    .antMatchers("/pictures/**").permitAll()
                    .and()
                    .formLogin().permitAll()
                    .and()
                    .logout().permitAll()
                    .and()
                    .csrf().disable();
        }
    }

}
