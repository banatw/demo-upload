package com.model;

import com.en.Action;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public class Form {


    private MultipartFile[] file;

    private UUID idMahasiswa;

    public Action getAction() {
        return action;
    }

    public void setAction(Action action) {
        this.action = action;
    }

    private Action action;

    public MultipartFile[] getFile() {
        return file;
    }

    public void setFile(MultipartFile[] file) {
        this.file = file;
    }

    public UUID getIdMahasiswa() {
        return idMahasiswa;
    }

    public void setIdMahasiswa(UUID idMahasiswa) {
        this.idMahasiswa = idMahasiswa;
    }
}
