/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package main.java.app.beans;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Date;
import javax.persistence.*;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author ramalhom
 */
@Entity
@Table(name = "t_pixel")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Pixel.findAll", query = "SELECT p FROM Pixel p"),
    @NamedQuery(name = "Pixel.findByPkPixel", query = "SELECT p FROM Pixel p WHERE p.pkPixel = :pkPixel"),
    @NamedQuery(name = "Pixel.findByColumnPixel", query = "SELECT p FROM Pixel p WHERE p.columnPixel = :columnPixel"),
    @NamedQuery(name = "Pixel.findByRowPixel", query = "SELECT p FROM Pixel p WHERE p.rowPixel = :rowPixel"),
    @NamedQuery(name = "Pixel.findByColorPixel", query = "SELECT p FROM Pixel p WHERE p.colorPixel = :colorPixel"),
    @NamedQuery(name = "Pixel.findByDateModification", query = "SELECT p FROM Pixel p WHERE p.dateModification = :dateModification")})
public class Pixel implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @Column(name = "pk_pixel")
    private Integer pkPixel;
    @Column(name = "column_pixel")
    private Integer columnPixel;
    @Column(name = "row_pixel")
    private Integer rowPixel;
    @Column(name = "color_pixel")
    private String colorPixel;

    @Column(name = "date_modification")
    @Version
    private Timestamp dateModification;

    public Pixel() {
    }

    public Pixel(Integer pkPixel) {
        this.pkPixel = pkPixel;
    }

    public Integer getPkPixel() {
        return pkPixel;
    }

    public void setPkPixel(Integer pkPixel) {
        this.pkPixel = pkPixel;
    }

    public Integer getColumnPixel() {
        return columnPixel;
    }

    public void setColumnPixel(Integer columnPixel) {
        this.columnPixel = columnPixel;
    }

    public Integer getRowPixel() {
        return rowPixel;
    }

    public void setRowPixel(Integer rowPixel) {
        this.rowPixel = rowPixel;
    }

    public String getColorPixel() {
        return colorPixel;
    }

    public void setColorPixel(String colorPixel) {
        this.colorPixel = colorPixel;
    }

    public Date getDateModification() {
        return dateModification;
    }

    public void setDateModification(Date dateModification) {
        this.dateModification = (Timestamp) dateModification;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (pkPixel != null ? pkPixel.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Pixel)) {
            return false;
        }
        Pixel other = (Pixel) object;
        if ((this.pkPixel == null && other.pkPixel != null) || (this.pkPixel != null && !this.pkPixel.equals(other.pkPixel))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "app.beans.Pixel[ pkPixel=" + pkPixel + " ]";
    }
    
}
