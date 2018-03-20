package com.example.demoupload;

import com.en.Action;
import com.entity.Mahasiswa;
import com.entity.Picture;
import com.model.Form;
import com.repository.MahasiswaRepo;
import com.repository.PictureRepo;
import net.coobird.thumbnailator.Thumbnails;
import net.sf.jmimemagic.Magic;
import net.sf.jmimemagic.MagicMatch;
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
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageOutputStream;
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
		return(x)->{
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
			model.addAttribute("mahasiswas",mahasiswaRepo.findAll());
			return "home";
		}

		@GetMapping("/detail")
		public String showDetail(@RequestParam("id") String id,Model model) {
			Integer integerId = Integer.valueOf(id);
			Mahasiswa mahasiswa = mahasiswaRepo.findOne(integerId);
			model.addAttribute("idMahasiswa",integerId);
			model.addAttribute("pictures",mahasiswa.getPictures());
			//model.addAttribute("pictures",pictureModelSet);
			return "detail";
		}

		@GetMapping("/add")
		public String showForm(@RequestParam("id") String id,Model model) {
			model.addAttribute("action", Action.ADD);
			model.addAttribute("idMahasiswa",id);
			return "form";
		}

		@PostMapping("/simpan")
		public String simpan(Form frm) {
			MagicMatch magicMatch = new MagicMatch();
			try {
				magicMatch.getMimeType(frm.getFile().getBytes());
			} catch (IOException e) {
				e.printStackTrace();
			}
			ByteArrayOutputStream byteArrayOutputStreamThumbnail = new ByteArrayOutputStream();
			File file = new File(UPLOADED_PATH + frm.getFile().getOriginalFilename());
			//copy file to server
			Mahasiswa mahasiswa = mahasiswaRepo.findOne(Integer.valueOf(frm.getIdMahasiswa()));
			Picture picture = new Picture();
			picture.setFilename(frm.getFile().getOriginalFilename());
			picture.setContentType(frm.getFile().getContentType());

			FileOutputStream fileOutputStream = null;
			try {
				fileOutputStream = new FileOutputStream(file);
				fileOutputStream.write(frm.getFile().getBytes());
				fileOutputStream.flush();
				fileOutputStream.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

			//Create thumbnail
			if(frm.getFile().getContentType().equalsIgnoreCase("application/pdf")) {
				try {
					PDDocument pdDocument = PDDocument.load(file);
					PDFRenderer renderer = new PDFRenderer(pdDocument);
					BufferedImage image = renderer.renderImage(0);
					pdDocument.close();
					java.io.ByteArrayOutputStream byteArrayOutputStream = new java.io.ByteArrayOutputStream();
					ImageIO.write(image,"png",byteArrayOutputStream);
					ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
					Thumbnails.of(byteArrayInputStream)
							.size(100, 100)
							.outputFormat("png")
							.toOutputStream(byteArrayOutputStreamThumbnail);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			else {
				try {
					Thumbnails.of(file)
							.size(100, 100)
							.outputFormat("png")
							.toOutputStream(byteArrayOutputStreamThumbnail);
				}
				catch (IOException e) {

				}
			}

			//save to database
			try {
				picture.setImage(Base64Utils.encodeToString(byteArrayOutputStreamThumbnail.toByteArray()));
				byteArrayOutputStreamThumbnail.flush();
				byteArrayOutputStreamThumbnail.close();
				pictureRepo.save(picture);

				Set<Picture> pictureSet = mahasiswa.getPictures();
				pictureSet.add(picture);
				mahasiswa.setPictures(pictureSet);
				mahasiswaRepo.save(mahasiswa);
			} catch (IOException e1) {
				e1.printStackTrace();
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
