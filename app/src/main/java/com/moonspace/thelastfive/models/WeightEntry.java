package com.moonspace.thelastfive.models;

import com.moonspace.thelastfive.helpers.MathHelper;

import java.io.Serializable;
import java.util.Date;

public class WeightEntry implements Serializable
{
    private Long id;
    private Date date;
    private Double weight;
    private String notes;
    private byte[] photoImage;
    private Boolean isNew;
    private Boolean isModified;
    private Boolean isDeleted;

    /**
     * Instantiates a new instance of the WeightEntry class.
     */
    public WeightEntry()
    {
        // Flag as being a "new" entry by default
        isNew = true;
        isModified = false;
        isDeleted = false;
    }

    /**
     * Gets the ID associated with this entry.
     *
     * @return the ID associated with this entry.
     */
    public Long getId()
    {
        return id;
    }

    /**
     * Sets the ID associated with this entry.
     *
     * @param id the ID to be associated with this entry.
     */
    public void setId(Long id)
    {
        this.id = id;
    }

    /**
     * Gets the entry date associated with this entry.
     *
     * @return the entry date associated with this entry.
     */
    public Date getDate()
    {
        return date;
    }

    /**
     * Sets the date associated with this entry.
     *
     * @param date the date to be associated with this entry.
     */
    public void setDate(Date date)
    {
        this.date = date;
    }

    /**
     * Gets the weight associated with this entry.
     *
     * @return the weight associated with this entry.
     */
    public Double getWeight()
    {
        return MathHelper.roundToNearestTenth(weight);
    }

    /**
     * Sets the weight associated with this entry.
     *
     * @param weight the weight to be associated with this entry.
     */
    public void setWeight(Double weight)
    {
        this.weight = weight;
    }

    /**
     * Gets the notes associated with this entry.
     *
     * @return the notes associated with this entry.
     */
    public String getNotes()
    {
        return notes;
    }

    /**
     * Sets the notes associated with this entry.
     *
     * @param notes the notes to be associated with this entry.
     */
    public void setNotes(String notes)
    {
        this.notes = notes;
    }

    /**
     * Gets the photo/image associated with this entry. NOTE: This accessor performs lazy-loading
     * of the photo/image to reduce memory demands.
     *
     * @return a byte array containing the photo/image associated with this entry.
     */
    public byte[] getPhotoImage()
    {
        return photoImage;
    }

    /**
     * Sets the photo/image associated with this entry.
     *
     * @param photoImage a byte array containing the photo/image to be associated with this entry.
     */
    public void setPhotoImage(byte[] photoImage)
    {
        this.photoImage = photoImage;
    }

    /**
     * Indicates if this is a new entry (i.e. it hasn't been saved to the database yet).
     */
    public Boolean getIsNew()
    {
        return isNew;
    }

    /**
     * Sets a flag indicating if this is a new entry.
     *
     * @param isNew true if this is a new entry; otherwise, false.
     */
    public void setIsNew(Boolean isNew)
    {
        this.isNew = isNew;
    }

    /**
     * Indicates if this is a modified entry (i.e. it has unsaved changes).
     */
    public Boolean getIsModified()
    {
        return isModified;
    }

    /**
     * Sets a flag indicating if this is a modified entry.
     *
     * @param isModified true if this is a modified entry; otherwise, false.
     */
    public void setIsModified(Boolean isModified)
    {
        this.isModified = isModified;
    }

    /**
     * Indicates if this is a deleted entry (i.e. it hasn't been removed from the database yet).
     */
    public Boolean getIsDeleted()
    {
        return isDeleted;
    }

    /**
     * Sets a flag indicating if this is a deleted entry.
     *
     * @param isDeleted true if this is a deleted entry; otherwise, false.
     */
    public void setIsDeleted(Boolean isDeleted)
    {
        this.isDeleted = isDeleted;
    }
}
