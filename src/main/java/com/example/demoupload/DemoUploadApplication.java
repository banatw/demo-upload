package com.example.demoupload;

import com.en.Action;
import com.entity.Mahasiswa;
import com.entity.Picture;
import com.model.Form;
import com.repository.MahasiswaRepo;
import com.repository.PictureRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
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

			Picture pictureA = new Picture();
			pictureA.setFilename("Snapshot_20150504_2.JPG");
			pictureRepo.save(pictureA);

			Picture pictureB = new Picture();
			pictureB.setFilename("Snapshot_20150504_3.JPG");
			pictureRepo.save(pictureB);


			Set<Picture> pictureSet = new HashSet<>();
			pictureSet.add(pictureA);
			pictureSet.add(pictureB);


			mahasiswa.setPictures(pictureSet);
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
			try {
				byte[] bytes = frm.getFile().getBytes();
				Path path = Paths.get(UPLOADED_PATH + frm.getFile().getOriginalFilename());
				Files.write(path,bytes);
				Mahasiswa mahasiswa = mahasiswaRepo.findOne(1);
				Picture picture = new Picture();
				picture.setFilename(frm.getFile().getOriginalFilename());
				pictureRepo.save(picture);

				Set<Picture> pictureSet = mahasiswa.getPictures();
				pictureSet.add(picture);
				mahasiswa.setPictures(pictureSet);
				mahasiswaRepo.save(mahasiswa);
			} catch (IOException e) {
				e.printStackTrace();
			}
			return "redirect:/detail?id=1";
		}

		@Override
		public void addResourceHandlers(ResourceHandlerRegistry registry) {
			registry
					.addResourceHandler("/pictures/**")
					.addResourceLocations("file:///" + UPLOADED_PATH);
		}
	}
}
