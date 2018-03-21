package com.model;

import com.en.Action;
import org.springframework.web.multipart.MultipartFile;

public class Form {


    private MultipartFile[] file;

    private String idMahasiswa;

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

    public String getIdMahasiswa() {
        return idMahasiswa;
    }

    public void setIdMahasiswa(String idMahasiswa) {
        this.idMahasiswa = idMahasiswa;
    }
}
