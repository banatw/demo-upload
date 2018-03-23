package com.entity;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.id.UUIDGenerator;

import javax.persistence.*;
import java.util.Set;
import java.util.UUID;

@Entity
public class Mahasiswa {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(
            name = "UUID",
            strategy = "org.hibernate.id.UUIDGenerator"
    )
    private UUID idMahasiswa;
    private String nama;

    @OneToMany(cascade = CascadeType.ALL,orphanRemoval = true)
    @JoinColumn(name = "id_mahasiswa")
    private Set<Picture> pictures;

    public Mahasiswa() {
    }

    public UUID getIdMahasiswa() {
        return idMahasiswa;
    }

    public void setIdMahasiswa(UUID idMahasiswa) {
        this.idMahasiswa = idMahasiswa;
    }

    public String getNama() {
        return nama;
    }

    public void setNama(String nama) {
        this.nama = nama;
    }

    public Set<Picture> getPictures() {
        return pictures;
    }

    public void setPictures(Set<Picture> pictures) {
        this.pictures = pictures;
    }
}
